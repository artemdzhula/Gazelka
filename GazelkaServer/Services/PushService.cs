using FirebaseAdmin.Messaging;
using Microsoft.EntityFrameworkCore;
using System.Text.Json;
using System.Threading.Tasks;
using static System.Runtime.InteropServices.JavaScript.JSType;

public class PushService
{
    private readonly AppDbContext _db;

    public PushService(AppDbContext db)
    {
        _db = db;
    }


    public async Task SendPushAsync(string fcmToken, string title, string body, Dictionary<string, string> data)
    {
        
        var message = new FirebaseAdmin.Messaging.Message
        {
            Token = fcmToken,

            Notification = null,

            Data = new Dictionary<string, string>(data)
            {
                ["title"] = title,
                ["body"] = body
            },

            Android = new AndroidConfig
            {
                Priority = Priority.High,
                Notification = new AndroidNotification
                {
                    ChannelId = "default_channel",
                    Sound = "default",
                }
            }
        };

        var jsonMessage = JsonSerializer.Serialize(message, new JsonSerializerOptions { WriteIndented = true });
        Console.WriteLine("FCM message JSON:");
        Console.WriteLine(jsonMessage);

        var response = await FirebaseMessaging.DefaultInstance.SendAsync(message);
        Console.WriteLine($"FCM sent: {response}");
    }


    public async Task SendPushToUserAsync(int userId, string title, string body, Dictionary<string, string> data)
    {
        var tokens = _db.UserTokens
            .Where(t => t.UserId == userId)
            .Select(t => t.FcmToken)
            .ToList();

        foreach (var token in tokens)
        {
            await SendPushAsync(token, title, body, data);
        }
    }

}
