using System.Threading.Tasks;

public interface IOrderService
{
    Task<Order> CreateOrderAsync(int customerId, CreateOrderDto dto);

    Task<List<Order>> GetOrdersByCustomerAsync(int customerId);

    Task<List<Order>> GetOrdersByDriverAsync(int driverId);
    Task<bool> AcceptOrderAsync(int orderId, int driverId);

    Task<bool> EditOrderAsync(int customerId, EditOrderDto dto);
    Task<bool> CancelOrderAsync(int userId, int orderId, bool isDriver);
    Task<List<Order>> GetPendingOrdersAsync(int driverId);

    Task<Order> GetOrderByIdAsync(int orderId);

    Task<bool> ComingOrderAsync(int orderId, int driverId);

    Task<bool> PickingOrderAsync(int orderId, int driverId);

    Task<bool> DeliveringOrderAsync(int orderId, int driverId);

    Task<bool> ComplitedOrderAsync(int orderId, int driverId);
}
