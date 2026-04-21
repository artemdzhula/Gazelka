using Microsoft.EntityFrameworkCore;
using Microsoft.Extensions.DependencyInjection;
using Microsoft.Extensions.Hosting;
using System;
using System.Linq;
using System.Threading;
using System.Threading.Tasks;
using static Order;

public class NewOrderNotificationService : BackgroundService
{
    private readonly IServiceProvider _serviceProvider;

    public NewOrderNotificationService(IServiceProvider serviceProvider)
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
            var newOrders = await db.Orders
                .Where(o => o.Status == OrderStatus.Pending && !o.NewOrderNotificationSent)
                .ToListAsync(stoppingToken);

            foreach (var order in newOrders)
            {
                var drivers = await db.Users
                    .Include(u => u.NotificationSettings)
                    .Where(u => u.Role == "driver" &&
                                u.NotificationSettings != null &&
                                u.NotificationSettings.newOrdersEnabled &&
                                u.CarType == order.VehicleType)
                    .ToListAsync(stoppingToken);

                foreach (var driver in drivers)
                {
                    await pushService.SendPushToUserAsync(
                        driver.Id,
                        "New Order",
                        $"New order #{order.OrderId} is available",
                        new Dictionary<string, string>
                        {
                            ["type"] = "order",
                            ["orderId"] = order.OrderId.ToString()
                        }
                    );
                    Console.WriteLine($"New order notification sent to driver {driver.Id} for order {order.OrderId}");
                }

                order.NewOrderNotificationSent = true;
            }

            await db.SaveChangesAsync(stoppingToken);

            await Task.Delay(TimeSpan.FromSeconds(60), stoppingToken);
        }
    }
}
