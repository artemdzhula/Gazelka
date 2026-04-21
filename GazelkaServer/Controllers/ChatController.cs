using gazelkaTest.Services;
using Microsoft.AspNetCore.Authorization;
using Microsoft.AspNetCore.Mvc;
using Microsoft.AspNetCore.SignalR;
using Microsoft.EntityFrameworkCore;
using System.Security.Claims;

[ApiController]
[Route("api/chat")]
[Authorize]
public class ChatController : ControllerBase
{
    private readonly AppDbContext _db;

    private readonly PushService _pushService;

    private readonly ChatPresenceService _presence;

    public ChatController(AppDbContext db, PushService pushService, ChatPresenceService presence)
    {
        _db = db;
        _pushService = pushService;
        _presence = presence;
    }

    [HttpGet("list")]
    public async Task<IActionResult> GetChats()
    {
        int userId = int.Parse(
            User.FindFirst(ClaimTypes.NameIdentifier)!.Value
        );

        var chats = await _db.Chats
            .Where(c => (c.User1Id == userId || c.User2Id == userId) && c.User2Id != null)
            .Select(c => new {
                ChatId = c.Id,
                OtherUserId = c.User1Id == userId ? c.User2Id : c.User1Id,
                OtherUserName = _db.Users
                    .Where(u => u.Id == (c.User1Id == userId ? c.User2Id : c.User1Id))
                    .Select(u => u.Name + " " + u.Surname)
                    .FirstOrDefault(),
                OrderId = c.OrderId,
                LastMessage = c.Messages
                    .OrderByDescending(m => m.SentAt)
                    .Select(m => new {
                        m.Id,
                        m.Text,
                        m.SenderId,
                        m.SentAt
                    })
                    .FirstOrDefault()
            })
            .ToListAsync();

        return Ok(chats);
    }

    [HttpGet("history/{chatId}")]
    public async Task<IActionResult> GetHistory(int chatId)
    {
        int userId = int.Parse(
            User.FindFirst(ClaimTypes.NameIdentifier)!.Value
        );

        var chatExists = await _db.Chats.AnyAsync(c =>
            c.Id == chatId &&
            (c.User1Id == userId || c.User2Id == userId)
        );

        if (!chatExists)
            return Forbid();

        var msgs = await _db.Messages
            .Where(m => m.ChatId == chatId)
            .OrderBy(m => m.SentAt)
            .ToListAsync();

        return Ok(msgs);
    }


    [HttpPost("send")]
    public async Task<IActionResult> Send([FromBody] SendMessageDto dto, [FromServices] IHubContext<ChatHub> hub)
    {
        if (string.IsNullOrWhiteSpace(dto.Text?.Trim()))
            return BadRequest("Message text is required");

        int senderId = int.Parse(User.FindFirst(ClaimTypes.NameIdentifier)!.Value);

        using var transaction = await _db.Database.BeginTransactionAsync();
        try
        {
            var chat = await _db.Chats
                .FirstOrDefaultAsync(c => c.OrderId == dto.OrderId);

            if (chat == null)
                return NotFound("Chat not found");

            if (!chat.User2Id.HasValue && senderId != chat.User1Id)
            {
                chat.User2Id = senderId;
                _db.Chats.Update(chat);
            }

            var message = new Message
            {
                ChatId = chat.Id,
                SenderId = senderId,
                Text = dto.Text.Trim(),
                SentAt = DateTime.UtcNow
            };

            _db.Messages.Add(message);
            await _db.SaveChangesAsync();

            var payload = new
            {
                id = message.Id,
                chatId = message.ChatId,
                senderId = message.SenderId,
                text = message.Text,
                sentAt = message.SentAt.ToString("yyyy-MM-ddTHH:mm:ssZ")
            };

            var participants = new List<int> { chat.User1Id };
            if (chat.User2Id.HasValue) participants.Add(chat.User2Id.Value);

            foreach (var participantId in participants)
            {
                await hub.Clients.Group(participantId.ToString())
                .SendAsync("ReceiveMessage",
                     message.Id,           // int
                     message.ChatId,       // int  
                     message.SenderId,     // int
                     message.Text,         // string
                     message.SentAt.ToString("yyyy-MM-ddTHH:mm:ssZ")  // string
                );
                if (participantId != senderId)
                {
                    var isUserInChat = _presence.IsUserInChat(participantId, chat.Id);

                    if (!isUserInChat)
                    {
                        var tokens = await _db.UserTokens
                            .Where(t => t.UserId == participantId)
                            .Select(t => t.FcmToken)
                            .ToListAsync();

                        foreach (var token in tokens)
                        {
                            var settings = await _db.UserNotificationSettings
                            .FirstOrDefaultAsync(x => x.userId == participantId);
                            if (settings?.chatEnabled == true)
                                await _pushService.SendPushAsync(
                                token,
                                "New message",
                                message.Text,
                                new Dictionary<string, string>
                                {
                                    ["type"] = "chat",
                                    ["orderId"] = chat.OrderId.ToString(),
                                    ["chatId"] = chat.Id.ToString()
                                }

                            );
                        }
                    }
                }
            }

            await transaction.CommitAsync();
            return Ok(payload);
        }
        catch
        {
            await transaction.RollbackAsync();
            throw;
        }
    }




    [HttpGet("show/{orderId}")]
    public async Task<IActionResult> GetChat(int orderId)
    {
        int userId = int.Parse(User.FindFirst(ClaimTypes.NameIdentifier)!.Value);

        var chat = await _db.Chats
            .Include(c => c.Messages)
            .FirstOrDefaultAsync(c => c.OrderId == orderId);

        var order = await _db.Orders
            .Include(o => o.Customer)
            .Include(o => o.Driver)
            .FirstOrDefaultAsync(o => o.OrderId == orderId);

        if (order == null)
            return NotFound("Order not found");

        int? otherUserId = order.CustomerId == userId ? order.DriverId : order.CustomerId;
        string otherUserName = otherUserId != null
            ? (await _db.Users.Where(u => u.Id == otherUserId).Select(u => u.Name + " " + u.Surname).FirstOrDefaultAsync())
            : "Unknown";

        var lastMessage = chat.Messages
            .OrderByDescending(m => m.SentAt)
            .Select(m => new {
                m.Id,
                m.Text,
                m.SenderId,
                m.SentAt
            })
            .FirstOrDefault();

        return Ok(new
        {
            chatId = chat.Id,
            otherUserId,
            otherUserName,
            orderId = chat.OrderId,
            lastMessage
        });
    }

}
