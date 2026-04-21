
    public interface IPushTokenService
    {
        Task SaveTokenAsync(int userId, string token);
        Task<string?> GetTokenAsync(int userId);
    }

