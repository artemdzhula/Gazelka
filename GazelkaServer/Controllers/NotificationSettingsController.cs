using gazelkaTest.Models;
using Microsoft.AspNetCore.Authorization;
using Microsoft.AspNetCore.Mvc;
using Microsoft.EntityFrameworkCore;
using System.Security.Claims;

[ApiController]
[Route("api/notifications/settings")]
[Authorize]
public class NotificationSettingsController : ControllerBase
{
    private readonly AppDbContext _db;

    public NotificationSettingsController(AppDbContext db)
    {
        _db = db;
    }

    [HttpGet]
    [Authorize]
    public async Task<IActionResult> Get()
    {
        int userId = int.Parse(
            User.FindFirst(ClaimTypes.NameIdentifier)!.Value
        );

        var settings = await _db.UserNotificationSettings
            .FirstOrDefaultAsync(x => x.userId == userId);

        if (settings == null)
        {
            settings = new UserNotificationSettings
            {
                userId = userId
            };
            _db.UserNotificationSettings.Add(settings);
            await _db.SaveChangesAsync();
        }

        return Ok(new NotificationSettingsDto
        {
            chatEnabled = settings.chatEnabled,
            newOrdersEnabled = settings.newOrdersEnabled,
            statusEnabled = settings.statusEnabled,
            upcomingEnabled = settings.upcomingEnabled,
            upcomingMinutes = settings.upcomingMinutes
        });

    }

    [HttpPost]
    [Authorize]
    public async Task<IActionResult> Update([FromBody]NotificationSettingsDto dto)
    {
        int userId = int.Parse(
            User.FindFirst(ClaimTypes.NameIdentifier)!.Value
        );

        var settings = await _db.UserNotificationSettings
            .FirstOrDefaultAsync(x => x.userId == userId);

        if (settings == null)
        {
            settings = new UserNotificationSettings
            {
                userId = userId
            };
            _db.UserNotificationSettings.Add(settings);
        }

        settings.chatEnabled = dto.chatEnabled;
        settings.newOrdersEnabled = dto.newOrdersEnabled;
        settings.statusEnabled = dto.statusEnabled;
        settings.upcomingEnabled = dto.upcomingEnabled;
        settings.upcomingMinutes = dto.upcomingMinutes;

        await _db.SaveChangesAsync();

        return Ok();
    }
}
