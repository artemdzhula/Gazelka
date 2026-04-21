using Microsoft.EntityFrameworkCore;
using System.Threading.Tasks;

public class OrderService : IOrderService
{
    private readonly AppDbContext _db;

    public OrderService(AppDbContext db)
    {
        _db = db;
    }

    private readonly Dictionary<string, double> VehicleRates = new()
    {
        { "Small Van", 3.0 },
        { "Medium Van", 4.0 },
        { "Large Van", 5.0 },
        { "Luton Van", 6.0 }
    };

    private const double StandardFee = 20.0;
    private const double ValuableFee = 10.0;
    private const double FragileFee = 10.0;
    private const double HeavyFee = 15.0;
    private const double MinPrice = 50.0;


    public async Task<Order> CreateOrderAsync(int customerId, CreateOrderDto dto)
    {
        double? distance = null;

        if (!string.IsNullOrWhiteSpace(dto.PointA) && !string.IsNullOrWhiteSpace(dto.PointB))
        {
            distance = await GoogleMapsService.GetDistanceKmAsync(dto.PointA, dto.PointB);
        }

        double price = CalculatePrice(distance.GetValueOrDefault(), dto.VehicleType,dto.Standard, dto.Valuable, dto.Fragile, dto.Heavy);

        var order = new Order
        {
            CustomerId = customerId,
            PointA = dto.PointA,
            PointB = dto.PointB,
            Distance = distance,
            Price = price,
            VehicleType = dto.VehicleType,
            DateTime = dto.DateTime,
            Standard = dto.Standard,
            Valuable = dto.Valuable,
            Fragile = dto.Fragile,
            Heavy = dto.Heavy,
            Status = Order.OrderStatus.Pending
        };

        _db.Orders.Add(order);
        await _db.SaveChangesAsync();
        var chat = new Chat
        {
            OrderId = order.OrderId,
            User1Id = customerId             
        };
        _db.Chats.Add(chat);
        await _db.SaveChangesAsync();

        return order;
    }

    public async Task<List<Order>> GetOrdersByCustomerAsync(int customerId)
    {
        return await _db.Orders
            .Where(o => o.CustomerId == customerId)
            .OrderByDescending(o => o.DateTime)
            .ToListAsync();
    }
    public async Task<List<Order>> GetOrdersByDriverAsync(int driverId)
    {
        return await _db.Orders
            .Where(o => o.DriverId == driverId)
            .OrderByDescending(o => o.DateTime)
            .ToListAsync();
    }
    public async Task<List<Order>> GetPendingOrdersAsync(int driverId)
    {
        var driverCarType = await _db.Users
        .Where(u => u.Id == driverId && u.Role == "driver")
        .Select(u => u.CarType)
        .FirstOrDefaultAsync();
        var driverCityName = await _db.Users
        .Where(u => u.Id == driverId && u.Role == "driver")
        .Select(u => u.CityName)
        .FirstOrDefaultAsync();

        if (driverCarType == null)
            return new List<Order>();

        var orders = await _db.Orders
            .Where(o => o.Status == Order.OrderStatus.Pending &&
            o.DriverId == null && 
            o.VehicleType == driverCarType)
            .OrderByDescending(o => o.DateTime)
            .ToListAsync();

        var filteredOrders = new List<Order>();

        foreach (var order in orders)
        {
            double? distanceKm = await GoogleMapsService.GetDistanceKmAsync(driverCityName, order.PointA);
            if (distanceKm.HasValue && distanceKm.Value <= 100)
            {
                filteredOrders.Add(order);
            }
        }

        return filteredOrders;
    }

    public async Task<bool> AcceptOrderAsync(int orderId, int driverId)
    {
        var order = await _db.Orders.FirstOrDefaultAsync(o => o.OrderId == orderId);
        if (order == null)
            throw new KeyNotFoundException();

        if (order.Status != Order.OrderStatus.Pending)
            return false;

        order.Status = Order.OrderStatus.Accepted;
        order.DriverId = driverId;

        var chat = await _db.Chats.FirstOrDefaultAsync(c => c.OrderId == orderId);
        if (chat != null)
        {
            chat.User2Id = driverId;
            _db.Chats.Update(chat);
        }


        await _db.SaveChangesAsync();
        return true;
    }
    public async Task<bool> EditOrderAsync(int customerId, EditOrderDto dto)
    {
        var order = await _db.Orders.FirstOrDefaultAsync(o => o.OrderId == dto.OrderId && o.CustomerId == customerId);

        if (order == null)
            return false; 

        if (order.Status != Order.OrderStatus.Pending)
            return false; 


        double? distance = null;

        if (!string.IsNullOrWhiteSpace(dto.PointA) && !string.IsNullOrWhiteSpace(dto.PointB))
        {
            distance = await GoogleMapsService.GetDistanceKmAsync(dto.PointA, dto.PointB);
        }

        double price = CalculatePrice(distance.GetValueOrDefault(), dto.VehicleType,dto.Standard, dto.Valuable, dto.Fragile, dto.Heavy);

        order.PointA = dto.PointA;
        order.PointB = dto.PointB;
        order.Distance = distance;
        order.Price = price;
        order.VehicleType = dto.VehicleType;
        order.DateTime = dto.DateTime;
        order.Standard = dto.Standard;
        order.Valuable = dto.Valuable;
        order.Fragile = dto.Fragile;
        order.Heavy = dto.Heavy;
        _db.Orders.Update(order);
        await _db.SaveChangesAsync();

        return true;
    }


    public async Task<bool> CancelOrderAsync(int userId, int orderId, bool isDriver)
    {
        Order? order;

        if (isDriver)
        {
            order = await _db.Orders.FirstOrDefaultAsync(o => o.OrderId == orderId && o.DriverId == userId);
            if (order.Status == Order.OrderStatus.Completed || order.Status == Order.OrderStatus.Cancelled)
                return false;

            order.Status = Order.OrderStatus.Pending;
        }
        else
        {
            order = await _db.Orders.FirstOrDefaultAsync(o => o.OrderId == orderId && o.CustomerId == userId);
            if (order.Status == Order.OrderStatus.Completed || order.Status == Order.OrderStatus.Cancelled)
                return false;

            order.Status = Order.OrderStatus.Cancelled;
        }

        if (order == null)
            throw new KeyNotFoundException("Order not found");
       
        _db.Orders.Update(order);
        await _db.SaveChangesAsync();

        return true;
    }


    private double CalculatePrice(double distance, string vehicleType, bool standard, bool valuable, bool fragile, bool heavy)
    {
        if (!VehicleRates.ContainsKey(vehicleType))
            throw new ArgumentException("Unknown vehicle type");

        double price = distance * VehicleRates[vehicleType];

        if (standard) price += StandardFee;
        if (valuable) price += ValuableFee;
        if (fragile) price += FragileFee;
        if (heavy) price += HeavyFee;

        price =  Math.Max(MinPrice, price);

        return Math.Ceiling(price);
    }

    public async Task<Order> GetOrderByIdAsync(int orderId)
    {
        var order = await _db.Orders
            .Include(o => o.Customer)
            .Include(o => o.Driver)
            .FirstOrDefaultAsync(o => o.OrderId == orderId);

        if (order == null)
            throw new KeyNotFoundException("Order not found");

        return order;
    }

    public async Task<bool> ComingOrderAsync(int orderId, int driverId)
    {
        var order = await _db.Orders.FirstOrDefaultAsync(o => o.OrderId == orderId);
        if (order == null)
            throw new KeyNotFoundException();

        if (order.Status != Order.OrderStatus.Accepted)
            return false;

        order.Status = Order.OrderStatus.Coming;
        order.DriverId = driverId;

        await _db.SaveChangesAsync();
        return true;
    }


    public async Task<bool> PickingOrderAsync(int orderId, int driverId)
    {
        var order = await _db.Orders.FirstOrDefaultAsync(o => o.OrderId == orderId);
        if (order == null)
            throw new KeyNotFoundException();

        if (order.Status != Order.OrderStatus.Coming)
            return false;

        order.Status = Order.OrderStatus.Picking;
        order.DriverId = driverId;

        await _db.SaveChangesAsync();
        return true;
    }


    public async Task<bool> DeliveringOrderAsync(int orderId, int driverId)
    {
        var order = await _db.Orders.FirstOrDefaultAsync(o => o.OrderId == orderId);
        if (order == null)
            throw new KeyNotFoundException();

        if (order.Status != Order.OrderStatus.Picking)
            return false;

        order.Status = Order.OrderStatus.Delivering;
        order.DriverId = driverId;

        await _db.SaveChangesAsync();
        return true;
    }

    public async Task<bool> ComplitedOrderAsync(int orderId, int driverId)
    {
        var order = await _db.Orders.FirstOrDefaultAsync(o => o.OrderId == orderId);
        if (order == null)
            throw new KeyNotFoundException();

        if (order.Status != Order.OrderStatus.Delivering)
            return false;

        order.Status = Order.OrderStatus.Completed;
        order.DriverId = driverId;

        await _db.SaveChangesAsync();
        return true;
    }
}
