using Microsoft.AspNetCore.Authorization;
using Microsoft.AspNetCore.Mvc;
using Microsoft.EntityFrameworkCore;
using System.Security.Claims;


[ApiController]
[Route("api/[controller]")]
public class AuthController : ControllerBase
{
    private readonly IAuthService _auth;


    public AuthController(IAuthService auth) => _auth = auth;


    [HttpPost("register")]
    public async Task<IActionResult> Register([FromBody] RegisterDto dto)
    {
        try
        {
            var user = await _auth.RegisterAsync(dto.Email, dto.Password, dto.Name, dto.Surname, dto.Role, dto.CarType, dto.CarColor, dto.CarNumber, dto.PhoneNumber, dto.cityName);
            var ip = HttpContext.Connection.RemoteIpAddress?.ToString();
            var ua = Request.Headers["User-Agent"].ToString();
            var (access, refresh) = await _auth.LoginAsync(dto.Email, dto.Password, ip, ua);

            await _auth.SendEmailConfirmationCodeAsync(dto.Email);

            return Ok(new
            {
                accessToken = access,
                refreshToken = refresh,
                id = user.Id,
                email = user.Email,
                name = user.Name,
                surname = user.Surname,
                role = user.Role,
                carType = user.CarType,
                carColor = user.CarColor,
                carNumber = user.CarNumber,
                phoneNumber = user.PhoneNumber,
                cityName = user.CityName,
            });

        }
        catch (InvalidOperationException ex)
        {
            return BadRequest(new { error = ex.Message });
        }
    }


    [HttpPost("login")]
    public async Task<IActionResult> Login([FromBody] LoginDto dto)
    {
        try
        {
            var ip = HttpContext.Connection.RemoteIpAddress?.ToString();
            var ua = Request.Headers["User-Agent"].ToString();
            var (access, refresh) = await _auth.LoginAsync(dto.Email, dto.Password, ip, ua);
            return Ok(new { accessToken = access, refreshToken = refresh});
        }
        catch (UnauthorizedAccessException ex)
        {
            string msg = ex.Message switch
            {
                "User not found" => "User with this email does not exist",
                "Incorrect password" => "Incorrect password",
                "Account is locked" => "Account is locked. Try later",
                _ => "Invalid credentials"
            };
            return Unauthorized(new { error = msg });
        }
    }


    [HttpPost("refresh")]
    public async Task<IActionResult> Refresh([FromBody] RefreshDto dto)
    {
        try
        {
            var ip = HttpContext.Connection.RemoteIpAddress?.ToString();
            var ua = Request.Headers["User-Agent"].ToString();
            var (access, refresh) = await _auth.RefreshAsync(dto.RefreshToken, ip, ua);
            return Ok(new { accessToken = access, refreshToken = refresh });
        }
        catch (UnauthorizedAccessException)
        {
            return Unauthorized(new { error = "Invalid refresh token" });
        }
    }


    [HttpPost("logout")]
    public async Task<IActionResult> Logout([FromBody] RefreshDto dto)
    {
        await _auth.LogoutAsync(dto.RefreshToken);
        return Ok();
    }


    [HttpGet("userinfo")]
    [Authorize]
    public async Task<IActionResult> GetCurrentUser()
    {
        var email = User.FindFirstValue(ClaimTypes.Email);
        if (string.IsNullOrEmpty(email))
            return Unauthorized(new { error = "Invalid token" });

        var user = await _auth.GetUserByEmailAsync(email);
        if (user == null)
            return NotFound(new { error = "User not found" });

        return Ok(new
        {
            name = user.Name,
            surname = user.Surname,
            email = user.Email,
            phoneNumber = user.PhoneNumber,
            role = user.Role,
            carType = user.CarType,
            carColor = user.CarColor,
            carNumber = user.CarNumber,
            id = user.Id,
            cityName = user.CityName,
        });
    }
    
    [HttpPost("updateProfile")]
    [Authorize]
    public async Task<IActionResult> UpdateProfile([FromBody] UpdateProfileDto dto)
    {
        try
        {
            var currentUserEmail = User.FindFirstValue(ClaimTypes.Email);
            if (string.IsNullOrEmpty(currentUserEmail))
                return Unauthorized(new { error = "Invalid token" });



            var updatedUser = await _auth.UpdateProfileAsync(
                currentUserEmail,
                dto.Email,
                dto.Name,
                dto.Surname,
                dto.PhoneNumber,
                dto.CarType,
                dto.CarColor,
                dto.CarNumber,
                dto.CityName
            );

            await _auth.SendEmailConfirmationCodeAsync(dto.Email);
            return Ok();
        }
        catch (InvalidOperationException ex)
        {
            return BadRequest(new { error = ex.Message });
        }
    }


    [HttpDelete("deleteAccount")]
    [Authorize]
    public async Task<IActionResult> DeleteAccount()
    {
        var userIdClaim = User.FindFirst(ClaimTypes.NameIdentifier)?.Value;
        if (userIdClaim == null)
            return Unauthorized();

        
        
        
        
        var userId = int.Parse(userIdClaim);



        var result = await _auth.DeleteAccountAsync(userId);

        if (!result)
            return NotFound("User not found");

        return Ok(new { message = "Account deleted successfully" });
    }

    [HttpPost("verifyEmail")]
    public async Task<IActionResult> VerifyEmail([FromBody] VerifyEmailDto dto)
    {
        try
        {
            await _auth.VerifyEmailAsync(dto.email, dto.code);
            return Ok(new { message = "Email confirmed" });
        }
        catch (InvalidOperationException ex)
        {
            return BadRequest(new { error = ex.Message });
        }
    }

    [HttpPost("resendEmailCode")]
    public async Task<IActionResult> ResendEmailCode([FromBody] ResendEmailCodeDto dto)
    {
        try
        {
            await _auth.SendEmailConfirmationCodeAsync(dto.email);
            return Ok(new { message = "Code sent" });
        }
        catch (InvalidOperationException ex)
        {
            return BadRequest(new { error = ex.Message });
        }
    }

    [HttpPost("requestPasswordReset")]
    public async Task<IActionResult> RequestPasswordReset([FromBody] RequestPasswordResetDto dto)
    {
        try
        {
            await _auth.RequestPasswordResetAsync(dto.email);
            return Ok(new { message = "Password reset code sent" });
        }
        catch (InvalidOperationException ex)
        {
            return BadRequest(new { error = ex.Message });
        }
    }

    [HttpPost("resetPassword")]
    public async Task<IActionResult> ResetPassword([FromBody] ResetPasswordDto dto)
    {
        try
        {
            await _auth.ResetPasswordAsync(dto.email, dto.code, dto.newPassword);
            return Ok(new { message = "Password reset successfully" });
        }
        catch (InvalidOperationException ex)
        {
            return BadRequest(new { error = ex.Message });
        }
    }

    [HttpGet("userinfo/{userId:int}")]
    [Authorize]
    public async Task<IActionResult> GetUserById(int userId)
    {
        var user = await _auth.GetUserByIdAsync(userId);
        if (user == null)
            return NotFound(new { error = "User not found" });

        return Ok(new
        {
            name = user.Name,
            surname = user.Surname,
            email = user.Email,
            phoneNumber = user.PhoneNumber,
            role = user.Role,
            carType = user.CarType,
            carColor = user.CarColor,
            carNumber = user.CarNumber,
            id = user.Id
        });
    }

}