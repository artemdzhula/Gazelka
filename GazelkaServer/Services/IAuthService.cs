public interface IAuthService
{
    
    Task<User> RegisterAsync(string email, string password, string role, string? carType = null, string? carColor = null, string? carNumber = null, string? Name = null, string? Surname = null, string? phoneNumber = null, string? cityName = null);
    Task<(string accessToken, string refreshToken)> LoginAsync(string email, string password, string? ip = null, string? userAgent = null);
    Task<(string accessToken, string refreshToken)> RefreshAsync(string refreshToken, string? ip = null, string? userAgent = null);
    Task LogoutAsync(string refreshToken);
    Task<User?> GetUserByEmailAsync(string email);
    Task<User> UpdateProfileAsync(string currentUserEmail, string email, string name, string surname, string? phoneNumber, string? carType, string? carColor, string? carNumber, string? cityName);
    Task<bool> DeleteAccountAsync(int userId);

    Task SendEmailConfirmationCodeAsync(string email);
    Task VerifyEmailAsync(string email, string code);

    Task RequestPasswordResetAsync(string email);

    Task ResetPasswordAsync(string email, string code, string newPassword);

    Task<User?> GetUserByIdAsync(int userId);

}