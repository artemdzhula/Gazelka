using Microsoft.AspNetCore.Authorization;
using Microsoft.AspNetCore.Mvc;
using System.Security.Claims;

[ApiController]
[Route("api/push")]
public class PushController : ControllerBase
{
    private readonly IPushTokenService _pushTokenService;

    public PushController(IPushTokenService pushTokenService)
    {
        _pushTokenService = pushTokenService;
    }

    [HttpPost("token")]
    [Authorize]
    public async Task<IActionResult> SaveToken([FromBody] FcmTokenDto dto)
    {
        if (string.IsNullOrEmpty(dto.Token))
            return BadRequest("Token is required");

        var userIdClaim = User.FindFirst(ClaimTypes.NameIdentifier);
        if (userIdClaim == null)
            return Unauthorized();

        if (!int.TryParse(userIdClaim.Value, out int userId))
            return BadRequest("Invalid user ID");

        await _pushTokenService.SaveTokenAsync(userId, dto.Token);

        return Ok(new { message = "FCM token saved" });
    }
}
