using Microsoft.EntityFrameworkCore;
using Microsoft.Extensions.DependencyInjection;
using Microsoft.Extensions.Hosting;
using System;
using System.Linq;
using System.Threading;
using System.Threading.Tasks;

public class OrderReminderService : BackgroundService
{
    private readonly IServiceProvider _serviceProvider;

    public OrderReminderService(IServiceProvider serviceProvider)
    {
        _serviceProvider = serviceProvider;
    }

    protected override async Task ExecuteAsync(CancellationToken stoppingToken)
    {
        while (!stoppingToken.IsCancellationRequested)
        {
            using var scope = _serviceProvider.CreateScope();

            var dbFactory = scope.ServiceProvider.GetRequiredService<IDbContextFactory<AppDbContext>>();
            var db = dbFactory.CreateDbContext();

            var pushService = scope.ServiceProvider.GetRequiredService<PushService>();

            var now = DateTime.UtcNow;

            var orders = await db.Orders
                .Include(o => o.Customer) 
                .Include(o => o.Driver)
                .ToListAsync(stoppingToken);

            foreach (var order in orders)
            {
                if (order.Customer != null && order.Customer.NotificationSettings?.upcomingEnabled == true)
                {
                    var minutes = order.Customer.NotificationSettings.upcomingMinutes;
                    if (!order.ClientReminderSent && order.DateTime <= now.AddMinutes(minutes))
                    {
                        await pushService.SendPushToUserAsync(
                            order.Customer.Id,
                            "Reminder",
                            $"Your order #{order.OrderId} will start in {minutes} min",
                            new Dictionary<string, string>
                            {
                                ["type"] = "order",
                                ["orderId"] = order.OrderId.ToString()
                            }
                        );
                        order.ClientReminderSent = true;
                        Console.WriteLine($"Client reminder sent for order {order.OrderId}");
                    }
                }
                if (order.Driver != null && order.Driver.NotificationSettings?.upcomingEnabled == true)
                {
                    var minutes = order.Driver.NotificationSettings.upcomingMinutes;
                    if (!order.DriverReminderSent && order.DateTime <= now.AddMinutes(minutes))
                    {
                        await pushService.SendPushToUserAsync(
                            order.Driver.Id,
                            "Reminder",
                            $"Your order #{order.OrderId} will start in {minutes} min",
                            new Dictionary<string, string>
                            {
                                ["type"] = "order",
                                ["orderId"] = order.OrderId.ToString()
                            }
                        );
                        order.DriverReminderSent = true;
                        Console.WriteLine($"Driver reminder sent for order {order.OrderId}");
                    }
                }
            }

            await db.SaveChangesAsync(stoppingToken);

            await Task.Delay(TimeSpan.FromSeconds(60), stoppingToken);
        }
    }
}
