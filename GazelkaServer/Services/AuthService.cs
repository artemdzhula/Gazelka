using gazelkaTest.Services;
using Microsoft.EntityFrameworkCore;
using Microsoft.Extensions.Options;
using Microsoft.IdentityModel.Tokens;
using System.ComponentModel.DataAnnotations;
using System.IdentityModel.Tokens.Jwt;
using System.Security.Claims;
using System.Security.Cryptography;
using System.Text;

public class AuthService : IAuthService
{
    private readonly AppDbContext _db;
    private readonly JwtSettings _jwt;
    private readonly IEmailService _emailService;

    public AuthService(AppDbContext db, IOptions<JwtSettings> jwtOptions, IEmailService emailService)
    {
        _db = db;
        _jwt = jwtOptions.Value;
        _emailService = emailService;
    }

    private static string GenerateCode()
    {
        return RandomNumberGenerator
            .GetInt32(100000, 999999)
            .ToString();
    }

    public async Task<User> RegisterAsync(string email, string password, string? Name = null, string? Surname = null, string? Role = null, string? carType = null, string? carColor = null, string? carNumber = null, string? phoneNumber = null, string? cityName = null)
    {
        email = email.ToLowerInvariant().Trim();
        if (await _db.Users.AnyAsync(u => u.Email == email))
            throw new InvalidOperationException("Email already in use");


        var hashed = BCrypt.Net.BCrypt.HashPassword(password);
        var user = new User { Email = email, PasswordHash = hashed, Name = Name, Surname = Surname, Role = Role, CarType = carType, CarColor = carColor, CarNumber = carNumber, PhoneNumber = phoneNumber, CityName = cityName };
        _db.Users.Add(user);
        await _db.SaveChangesAsync();
        return user;
    }

    public async Task<(string accessToken, string refreshToken)> LoginAsync(string email, string password, string? ip = null, string? userAgent = null)
    {
        email = email.ToLowerInvariant().Trim();
        var user = await _db.Users.Include(u => u.RefreshTokens).FirstOrDefaultAsync(u => u.Email == email);
        if (user == null)
            throw new UnauthorizedAccessException("User not found");

        if (user.LockoutUntil.HasValue && user.LockoutUntil > DateTime.UtcNow)
            throw new UnauthorizedAccessException("Account is locked");

        if (!BCrypt.Net.BCrypt.Verify(password, user.PasswordHash))
        {
            user.FailedLoginAttempts += 1;
            if (user.FailedLoginAttempts >= 5)
            {
                user.LockoutUntil = DateTime.UtcNow.AddMinutes(15);
                user.FailedLoginAttempts = 0;
            }
            await _db.SaveChangesAsync();
            throw new UnauthorizedAccessException("Incorrect password");
        }

        // successful
        user.FailedLoginAttempts = 0;
        user.LockoutUntil = null;

        var access = GenerateJwtToken(user);
        var refresh = GenerateRefreshToken();

        var rt = new RefreshToken
        {
            Token = refresh,
            Expires = DateTime.UtcNow.AddDays(_jwt.RefreshTokenExpiresDays),
            Ip = ip,
            UserAgent = userAgent,
            UserId = user.Id
        };
        _db.RefreshTokens.Add(rt);
        await _db.SaveChangesAsync();

        return (access, refresh);
    }


    public async Task<(string accessToken, string refreshToken)> RefreshAsync(string refreshToken, string? ip = null, string? userAgent = null)
    {
        var r = await _db.RefreshTokens.Include(x => x.User).FirstOrDefaultAsync(x => x.Token == refreshToken);
        if (r == null || r.IsRevoked || r.Expires < DateTime.UtcNow)
            throw new UnauthorizedAccessException("Invalid refresh token");


        // revoke old
        r.IsRevoked = true;


        var user = r.User;
        var newAccess = GenerateJwtToken(user);
        var newRefresh = GenerateRefreshToken();


        _db.RefreshTokens.Add(new RefreshToken
        {
            Token = newRefresh,
            Expires = DateTime.UtcNow.AddDays(_jwt.RefreshTokenExpiresDays),
            Ip = ip,
            UserAgent = userAgent,
            UserId = user.Id
        });


        await _db.SaveChangesAsync();
        return (newAccess, newRefresh);
    }

    public async Task LogoutAsync(string refreshToken)
    {
        var r = await _db.RefreshTokens.FirstOrDefaultAsync(x => x.Token == refreshToken);
        if (r == null) return;
        r.IsRevoked = true;
        await _db.SaveChangesAsync();
    }

    private string GenerateJwtToken(User user)
    {
        var claims = new[]
        {
            new Claim(JwtRegisteredClaimNames.Sub, user.Id.ToString()),
            new Claim(JwtRegisteredClaimNames.Email, user.Email),
            new Claim(ClaimTypes.Role, user.Role)
        };


        var key = new SymmetricSecurityKey(Encoding.UTF8.GetBytes(_jwt.Key));
        var creds = new SigningCredentials(key, SecurityAlgorithms.HmacSha256);
        var expires = DateTime.UtcNow.AddMinutes(_jwt.AccessTokenExpiresMinutes);


        var token = new JwtSecurityToken(
            issuer: _jwt.Issuer,
            audience: _jwt.Audience,
            claims: claims,
            expires: expires,
            signingCredentials: creds
        );


        return new JwtSecurityTokenHandler().WriteToken(token);
    }

    public async Task<User?> GetUserByEmailAsync(string email)
    {
        if (string.IsNullOrWhiteSpace(email))
            return null;

        return await _db.Users
            .FirstOrDefaultAsync(u => u.Email.ToLower() == email.ToLower());
    }
    private static string GenerateRefreshToken()
    {
        var bytes = RandomNumberGenerator.GetBytes(64);
        return Convert.ToBase64String(bytes);
    }

    public async Task<User> UpdateProfileAsync(string currentUserEmail, string email, string name, string surname, string? phoneNumber, string? carType, string? carColor, string? carNumber, string? cityName)
    {
        var user = await _db.Users.FirstOrDefaultAsync(u => u.Email == currentUserEmail);
        if (user == null)
            throw new InvalidOperationException("User not found");

        user.Email = email;
        user.Name = name;
        user.Surname = surname;
        user.PhoneNumber = phoneNumber;
        user.CarType = carType;
        user.CarColor = carColor;
        user.CarNumber = carNumber;
        user.CityName = cityName;
        user.EmailConfirmed = false;
        _db.Users.Update(user);
        await _db.SaveChangesAsync();

        return user;
    }

    public async Task<bool> DeleteAccountAsync(int userId)
    {
        var user = await _db.Users.FindAsync(userId);
        if (user == null) return false;

        var orders = new List<Order>();
        if (user.Role == "customer")
        {
            orders = await _db.Orders
            .Where(o => o.CustomerId == userId)
            .ToListAsync();
        }
        else
        {
            orders = await _db.Orders
        .Where(o => o.DriverId == userId)
        .ToListAsync();
        }

        foreach (var order in orders)
        {
            if (order.Status == Order.OrderStatus.Accepted)
                order.Status = Order.OrderStatus.Pending;

            if (user.Role == "customer") _db.Orders.Remove(order);
            else order.DriverId = null;
        }

        _db.Users.Remove(user);
        await _db.SaveChangesAsync();

        return true;
    }

    public async Task SendEmailConfirmationCodeAsync(string email)
    {
        if (string.IsNullOrWhiteSpace(email))
            return;

        if (!new EmailAddressAttribute().IsValid(email))
            return;

        var user = await _db.Users.FirstOrDefaultAsync(x => x.Email == email);
        if (user == null)
            return;

        if (user.EmailConfirmed)
            return;

        user.EmailConfirmationCode = GenerateCode();

        await _db.SaveChangesAsync();

        await _emailService.SendAsync(
            email,
            "Email verification",
            $"Your code: {user.EmailConfirmationCode}"
        );
    }


    public async Task VerifyEmailAsync(string email, string code)
    {
        if (string.IsNullOrWhiteSpace(email) || string.IsNullOrWhiteSpace(code))
            throw new InvalidOperationException("Email and code are required");

        var user = await _db.Users.FirstOrDefaultAsync(x => x.Email == email);
        if (user == null)
            throw new InvalidOperationException("User not found");

        if (user.EmailConfirmed)
            throw new InvalidOperationException("Email already confirmed");

        if (user.EmailConfirmationCode != code)
            throw new InvalidOperationException("Invalid code");

        user.EmailConfirmed = true;
        user.EmailConfirmationCode = null;

        await _db.SaveChangesAsync();
    }

    public async Task RequestPasswordResetAsync(string email)
    {
        var user = await _db.Users.FirstOrDefaultAsync(u => u.Email == email);
        if (user == null)
            throw new InvalidOperationException("User not found");

        if (string.IsNullOrWhiteSpace(email))
            return;

        if (!new EmailAddressAttribute().IsValid(email))
            return;

        user.PasswordResetCode = GenerateCode();

        await _db.SaveChangesAsync();

        await _emailService.SendAsync(
            email,
            "Password reset",
            $"Your password reset code: {user.PasswordResetCode}"
        );
    }

    public async Task ResetPasswordAsync(string email, string code, string newPassword)
    {
        var user = await _db.Users.FirstOrDefaultAsync(u => u.Email == email);
        if (user == null)
            throw new InvalidOperationException("User not found");


        if (user.PasswordResetCode != code)
            throw new InvalidOperationException("Invalid code");

        user.PasswordHash = BCrypt.Net.BCrypt.HashPassword(newPassword);
        user.PasswordResetCode = null;

        await _db.SaveChangesAsync();
    }


    public async Task<User?> GetUserByIdAsync(int userId)
    {
        return await _db.Users
            .FirstOrDefaultAsync(u => u.Id == userId);
    }
}