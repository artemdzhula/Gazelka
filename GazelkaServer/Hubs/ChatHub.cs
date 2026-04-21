using gazelkaTest.Services;
using Microsoft.AspNetCore.Authorization;
using Microsoft.AspNetCore.SignalR;
using System.Security.Claims;
using System.Text.RegularExpressions;

[Authorize]
public class ChatHub : Hub
{
    private readonly ChatPresenceService _presence;

    public ChatHub(ChatPresenceService presence)
    {
        _presence = presence;
    }

    public override Task OnConnectedAsync()
    {
        var userId = int.Parse(Context.UserIdentifier!);
        Groups.AddToGroupAsync(Context.ConnectionId, userId.ToString());
        return base.OnConnectedAsync();
    }

    public Task EnterChat(int chatId)
    {
        var userId = int.Parse(Context.UserIdentifier!);
        _presence.EnterChat(userId, chatId);
        return Task.CompletedTask;
    }

    public Task LeaveChat()
    {
        var userId = int.Parse(Context.UserIdentifier!);
        _presence.LeaveChat(userId);
        return Task.CompletedTask;
    }

    public override Task OnDisconnectedAsync(Exception? exception)
    {
        var userId = int.Parse(Context.UserIdentifier!);
        _presence.LeaveChat(userId);
        return base.OnDisconnectedAsync(exception);
    }
}

