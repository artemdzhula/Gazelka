using Microsoft.AspNetCore.Authorization;
using Microsoft.AspNetCore.Mvc;
using Microsoft.EntityFrameworkCore;
using System;
using System.IdentityModel.Tokens.Jwt;
using System.Security.Claims;


[ApiController]
[Route("api/[controller]")]
public class OrdersController : ControllerBase
{
    private readonly IOrderService _orderService;

    private readonly PushService _pushService;

    private readonly AppDbContext _db;

    public OrdersController(IOrderService orderService, PushService pushService, AppDbContext db)
    {
        _orderService = orderService;
        _pushService = pushService;
        _db = db;
    }

    [HttpPost("create")]
    [Authorize(Roles = "customer")]
    public async Task<IActionResult> CreateOrder([FromBody] CreateOrderDto dto)
    {
        var customerIdClaim = User.Claims.FirstOrDefault(c => c.Type == JwtRegisteredClaimNames.Sub || c.Type == ClaimTypes.NameIdentifier);

        if (!int.TryParse(customerIdClaim?.Value, out var customerId))
            return Unauthorized("Invalid user id in token");

        var order = await _orderService.CreateOrderAsync(customerId, dto); ;



        return Ok();
    }

    [HttpGet("customerScheduled")]
    [Authorize(Roles = "customer")]
    public async Task<IActionResult> GetCustomerActiveOrders()
    {
        var customerIdClaim = User.Claims.FirstOrDefault(c => c.Type == JwtRegisteredClaimNames.Sub || c.Type == ClaimTypes.NameIdentifier);
        if (!int.TryParse(customerIdClaim?.Value, out var customerId))
            return Unauthorized("Invalid user id in token");

        var customerOrders = await _orderService.GetOrdersByCustomerAsync(customerId);
        var activeOrders = customerOrders
            .Where(o => o.Status == Order.OrderStatus.Pending || o.Status == Order.OrderStatus.Accepted || o.Status == Order.OrderStatus.InProgress || o.Status == Order.OrderStatus.Delivering || o.Status == Order.OrderStatus.Picking || o.Status == Order.OrderStatus.Coming)
            .OrderBy(o => o.DateTime)
            .ToList();

        return Ok(activeOrders);
    }

    [HttpGet("customerHistory")]
    [Authorize(Roles = "customer")]
    public async Task<IActionResult> GetCustomerOrdersHistory()
    {
        var customerIdClaim = User.Claims.FirstOrDefault(c => c.Type == JwtRegisteredClaimNames.Sub || c.Type == ClaimTypes.NameIdentifier);
        if (!int.TryParse(customerIdClaim?.Value, out var customerId))
            return Unauthorized("Invalid user id in token");

        var customerOrders = await _orderService.GetOrdersByCustomerAsync(customerId);
        var ordersHistory = customerOrders
            .Where(o => o.Status == Order.OrderStatus.Completed || o.Status == Order.OrderStatus.Cancelled)
            .OrderByDescending(o => o.DateTime)
            .ToList();

        return Ok(ordersHistory);
    }
    [HttpGet("driverScheduled")]
    [Authorize(Roles = "driver")]
    public async Task<IActionResult> GetDriverActiveOrders()
    {
        var driverIdClaim = User.Claims.FirstOrDefault(c => c.Type == JwtRegisteredClaimNames.Sub || c.Type == ClaimTypes.NameIdentifier);
        if (!int.TryParse(driverIdClaim?.Value, out var driverId))
            return Unauthorized("Invalid user id in token");

        var driverOrders = await _orderService.GetOrdersByDriverAsync(driverId);
        var activeOrders = driverOrders
            .Where(o => o.Status == Order.OrderStatus.Accepted || o.Status == Order.OrderStatus.InProgress || o.Status == Order.OrderStatus.Delivering || o.Status == Order.OrderStatus.Picking || o.Status == Order.OrderStatus.Coming)
            .OrderBy(o => o.DateTime)
            .ToList();

        return Ok(activeOrders);
    }

    [HttpGet("driverHistory")]
    [Authorize(Roles = "driver")]
    public async Task<IActionResult> GetDriverOrdersHistory()
    {
        var driverIdClaim = User.Claims.FirstOrDefault(c => c.Type == JwtRegisteredClaimNames.Sub || c.Type == ClaimTypes.NameIdentifier);
        if (!int.TryParse(driverIdClaim?.Value, out var driverId))
            return Unauthorized("Invalid user id in token");

        var driverOrders = await _orderService.GetOrdersByDriverAsync(driverId);
        var ordersHistory = driverOrders
            .Where(o => o.Status == Order.OrderStatus.Completed)
            .OrderByDescending(o => o.DateTime)
            .ToList();

        return Ok(ordersHistory);
    }
    [HttpGet("available")]
    [Authorize(Roles = "driver")]
    public async Task<IActionResult> GetAvailableOrders()
    {
        var driverIdClaim = User.Claims.FirstOrDefault(c => c.Type == JwtRegisteredClaimNames.Sub || c.Type == ClaimTypes.NameIdentifier);
        if (!int.TryParse(driverIdClaim?.Value, out var driverId))
            return Unauthorized("Invalid user id in token");

        var availableOrders = await _orderService.GetPendingOrdersAsync(driverId);

        var sortedOrders = availableOrders
            .OrderByDescending(o => o.DateTime)
            .ToList();


        return Ok(sortedOrders);
    }

    [HttpPost("accept")]
    [Authorize(Roles = "driver")]
    public async Task<IActionResult> AcceptOrder([FromBody] AcceptOrderDto dto)
    {
        if (dto == null)
            return BadRequest("OrderId is required");

        var driverIdClaim = User.Claims.FirstOrDefault(c => c.Type == JwtRegisteredClaimNames.Sub || c.Type == ClaimTypes.NameIdentifier);
        if (!int.TryParse(driverIdClaim?.Value, out var driverId))
            return Unauthorized("Invalid user id in token");



        try
        {
            var success = await _orderService.AcceptOrderAsync(dto.OrderId, driverId);
            if (!success)
                return BadRequest("Order is not available to accept");

            var order = await _orderService.GetOrderByIdAsync(dto.OrderId);
            if (order == null) return NotFound("Order not found");

            var settings = await _db.UserNotificationSettings
            .FirstOrDefaultAsync(x => x.userId == order.CustomerId);
            if (settings?.statusEnabled == true)
            {
                await _pushService.SendPushToUserAsync(
                order.CustomerId,
                "Order accepted",
                $"Your order #{order.OrderId} was accepted by driver",
                new Dictionary<string, string>
                {
                    ["type"] = "order",
                    ["orderId"] = order.OrderId.ToString()
                }
            );
            }           
            return Ok(new { message = "Order accepted"});
        }
        catch (KeyNotFoundException)
        {
            return NotFound("Order not found");
        }
    }

    [HttpPost("edit")]
    [Authorize(Roles = "customer")]
    public async Task<IActionResult> EditOrder([FromBody] EditOrderDto dto)
    {
        var customerIdClaim = User.Claims.FirstOrDefault(c => c.Type == JwtRegisteredClaimNames.Sub || c.Type == ClaimTypes.NameIdentifier);
        if (!int.TryParse(customerIdClaim?.Value, out var customerId))
            return Unauthorized("Invalid user id in token");

        try
        {
            var updated = await _orderService.EditOrderAsync(customerId, dto);
            if (!updated)
                return BadRequest("Order cannot be updated (maybe already accepted or completed)");


            var order = await _orderService.GetOrderByIdAsync(dto.OrderId);
            if (order.DriverId.HasValue)
            {
                var settings = await _db.UserNotificationSettings
                .FirstOrDefaultAsync(x => x.userId == order.DriverId);
                if (settings?.statusEnabled == true)
                {
                    await _pushService.SendPushToUserAsync(
                    order.DriverId.Value,
                    "Order edited",
                    $"Order #{order.OrderId} was edited by customer",
                    new Dictionary<string, string>
                    {
                        ["type"] = "order",
                        ["orderId"] = order.OrderId.ToString()
                    }
                );}          
            }

            return Ok(new { message = "Order updated" });
        }
        catch (KeyNotFoundException)
        {
            return NotFound("Order not found");
        }
    }

    [HttpPost("cancel")]
    [Authorize]
    public async Task<IActionResult> CancelOrder([FromBody] CancelOrderDto dto)
    {
        var userIdClaim = User.Claims.FirstOrDefault(c => c.Type == JwtRegisteredClaimNames.Sub || c.Type == ClaimTypes.NameIdentifier);
        if (!int.TryParse(userIdClaim?.Value, out var userId))
            return Unauthorized("Invalid user id in token");
        var order = await _orderService.GetOrderByIdAsync(dto.OrderId);
        if (order == null) return NotFound("Order not found");

        try
        {
            bool canceled = false;
            string roleMessage = "";

            if (User.IsInRole("customer"))
            {
                canceled = await _orderService.CancelOrderAsync(userId, dto.OrderId, isDriver: false);
                roleMessage = "by customer";
            }
            else if (User.IsInRole("driver"))
            {
                canceled = await _orderService.CancelOrderAsync(userId, dto.OrderId, isDriver: true);
                roleMessage = "by driver";
            }

            if (!canceled)
                return BadRequest("Order cannot be canceled (maybe already in progress or completed)");

            int? otherUserId = User.IsInRole("customer") ? order.DriverId : order.CustomerId;
            if (otherUserId.HasValue)
            {
                var settings = await _db.UserNotificationSettings
                .FirstOrDefaultAsync(x => x.userId == otherUserId);
                if (settings?.statusEnabled == true)
                {
                    await _pushService.SendPushToUserAsync(
                    otherUserId.Value,
                    "Order canceled",
                    $"Order #{order.OrderId} was canceled {roleMessage}",
                    new Dictionary<string, string>
                    {
                        ["type"] = "order",
                        ["orderId"] = order.OrderId.ToString()
                    }
                );}    
            }

            return Ok(new { message = "Order canceled" });
        }
        catch (KeyNotFoundException)
        {
            return NotFound("Order not found");
        }
    }


    [HttpGet("{orderId}")]
    [Authorize]
    public async Task<IActionResult> GetOrderById(int orderId)
    {
        var userIdClaim = User.Claims.FirstOrDefault(c => c.Type == JwtRegisteredClaimNames.Sub || c.Type == ClaimTypes.NameIdentifier);
        if (!int.TryParse(userIdClaim?.Value, out var userId))
            return Unauthorized("Invalid user id in token");

        Order? order = null;

        if (User.IsInRole("customer"))
        {
            var customerOrders = await _orderService.GetOrdersByCustomerAsync(userId);
            order = customerOrders.FirstOrDefault(o => o.OrderId == orderId);
        }
        else if (User.IsInRole("driver"))
        {
            order = await _orderService.GetOrderByIdAsync(orderId);
            bool isAssignedToDriver = order.DriverId == userId;
            bool isAvailableOrder = order.DriverId == null &&
                                    order.Status == Order.OrderStatus.Pending;

            if(!isAssignedToDriver && !isAvailableOrder) return NotFound("Order not found or you don't have access");
        }

        if (order == null)
            return NotFound("Order not found or you don't have access");

        return Ok(order);
    }
    public enum OrderStatus { Pending, Accepted, InProgress, Completed, Cancelled, Coming, Picking, Delivering }

    [HttpPost("coming")]
    [Authorize(Roles = "driver")]
    public async Task<IActionResult> ComingOrder([FromBody] ComingOrderDto dto)
    {
        if (dto == null)
            return BadRequest("OrderId is required");

        var driverIdClaim = User.Claims.FirstOrDefault(c => c.Type == JwtRegisteredClaimNames.Sub || c.Type == ClaimTypes.NameIdentifier);
        if (!int.TryParse(driverIdClaim?.Value, out var driverId))
            return Unauthorized("Invalid user id in token");

        try
        {
            var success = await _orderService.ComingOrderAsync(dto.OrderId, driverId);
            if (!success)
                return BadRequest("Order is not available to change status");

            var order = await _orderService.GetOrderByIdAsync(dto.OrderId);
            if (order == null) return NotFound("Order not found");

            var settings = await _db.UserNotificationSettings
            .FirstOrDefaultAsync(x => x.userId == order.CustomerId);
            if (settings?.statusEnabled == true)
            {
                await _pushService.SendPushToUserAsync(
                order.CustomerId,
                "Order status changed",
                $"Driver is coming for order #{order.OrderId}",
                new Dictionary<string, string>
                {
                    ["type"] = "order",
                    ["orderId"] = order.OrderId.ToString()
                }
            );
            }
            return Ok(new { message = "Order status changed" });
        }
        catch (KeyNotFoundException)
        {
            return NotFound("Order not found");
        }
    }

    [HttpPost("picking")]
    [Authorize(Roles = "driver")]
    public async Task<IActionResult> PickingOrder([FromBody] PickingOrderDto dto)
    {
        if (dto == null)
            return BadRequest("OrderId is required");

        var driverIdClaim = User.Claims.FirstOrDefault(c => c.Type == JwtRegisteredClaimNames.Sub || c.Type == ClaimTypes.NameIdentifier);
        if (!int.TryParse(driverIdClaim?.Value, out var driverId))
            return Unauthorized("Invalid user id in token");

        try
        {
            var success = await _orderService.PickingOrderAsync(dto.OrderId, driverId);
            if (!success)
                return BadRequest("Order is not available to change status");

            var order = await _orderService.GetOrderByIdAsync(dto.OrderId);
            if (order == null) return NotFound("Order not found");

            var settings = await _db.UserNotificationSettings
            .FirstOrDefaultAsync(x => x.userId == order.CustomerId);
            if (settings?.statusEnabled == true)
            {
                await _pushService.SendPushToUserAsync(
                order.CustomerId,
                "Order status changed",
                $"Driver is picking for order #{order.OrderId}",
                new Dictionary<string, string>
                {
                    ["type"] = "order",
                    ["orderId"] = order.OrderId.ToString()
                }
            );
            }
            return Ok(new { message = "Order status changed" });
        }
        catch (KeyNotFoundException)
        {
            return NotFound("Order not found");
        }
    }

    [HttpPost("delivering")]
    [Authorize(Roles = "driver")]
    public async Task<IActionResult> DeliveringOrder([FromBody] DeliveringOrderDto dto)
    {
        if (dto == null)
            return BadRequest("OrderId is required");

        var driverIdClaim = User.Claims.FirstOrDefault(c => c.Type == JwtRegisteredClaimNames.Sub || c.Type == ClaimTypes.NameIdentifier);
        if (!int.TryParse(driverIdClaim?.Value, out var driverId))
            return Unauthorized("Invalid user id in token");

        try
        {
            var success = await _orderService.DeliveringOrderAsync(dto.OrderId, driverId);
            if (!success)
                return BadRequest("Order is not available to change status");

            var order = await _orderService.GetOrderByIdAsync(dto.OrderId);
            if (order == null) return NotFound("Order not found");

            var settings = await _db.UserNotificationSettings
            .FirstOrDefaultAsync(x => x.userId == order.CustomerId);
            if (settings?.statusEnabled == true)
            {
                await _pushService.SendPushToUserAsync(
                order.CustomerId,
                "Order status changed",
                $"Driver is delivering for order #{order.OrderId}",
                new Dictionary<string, string>
                {
                    ["type"] = "order",
                    ["orderId"] = order.OrderId.ToString()
                }
            );
            }
            return Ok(new { message = "Order status changed" });
        }
        catch (KeyNotFoundException)
        {
            return NotFound("Order not found");
        }
    }

    [HttpPost("completed")]
    [Authorize(Roles = "driver")]
    public async Task<IActionResult> ComlitedOrder([FromBody] ComplitedOrderDto dto)
    {
        if (dto == null)
            return BadRequest("OrderId is required");

        var driverIdClaim = User.Claims.FirstOrDefault(c => c.Type == JwtRegisteredClaimNames.Sub || c.Type == ClaimTypes.NameIdentifier);
        if (!int.TryParse(driverIdClaim?.Value, out var driverId))
            return Unauthorized("Invalid user id in token");

        try
        {
            var success = await _orderService.ComplitedOrderAsync(dto.OrderId, driverId);
            if (!success)
                return BadRequest("Order is not available to change status");

            var order = await _orderService.GetOrderByIdAsync(dto.OrderId);
            if (order == null) return NotFound("Order not found");

            var settings = await _db.UserNotificationSettings
            .FirstOrDefaultAsync(x => x.userId == order.CustomerId);
            if (settings?.statusEnabled == true)
            {
                await _pushService.SendPushToUserAsync(
                order.CustomerId,
                "Order complited",
                $"Order #{order.OrderId} complited",
                new Dictionary<string, string>
                {
                    ["type"] = "order",
                    ["orderId"] = order.OrderId.ToString()
                }
            );
            }
            return Ok(new { message = "Order complited" });
        }
        catch (KeyNotFoundException)
        {
            return NotFound("Order not found");
        }
    }
}
