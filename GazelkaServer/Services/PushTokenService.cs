using gazelkaTest.Services;
using Microsoft.EntityFrameworkCore;

public class PushTokenService : IPushTokenService
{
    private readonly AppDbContext _context;

    public PushTokenService(AppDbContext context)
    {
        _context = context;
    }

    public async Task SaveTokenAsync(int userId, string token)
    {
        var existing = await _context.UserTokens
            .FirstOrDefaultAsync(t => t.UserId == userId);

        if (existing != null)
        {
            existing.FcmToken = token;
            _context.UserTokens.Update(existing);
        }
        else
        {
            var userToken = new UserToken
            {
                UserId = userId,
                FcmToken = token
            };
            await _context.UserTokens.AddAsync(userToken);
        }

        await _context.SaveChangesAsync();
    }

    public async Task<string?> GetTokenAsync(int userId)
    {
        return await _context.UserTokens
            .Where(t => t.UserId == userId)
            .Select(t => t.FcmToken)
            .FirstOrDefaultAsync();
    }
}
