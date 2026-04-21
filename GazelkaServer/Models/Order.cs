using System.ComponentModel.DataAnnotations;

    public class Order
    {
        [Key]
        public int OrderId { get; set; }

        [Required]
        public string PointA { get; set; } = null!;

        [Required]
        public string PointB { get; set; } = null!;

        public double? Distance { get; set; }
        [Required]
        public string VehicleType { get; set; } = null!;

        [Required]
        public DateTime DateTime { get; set; }

        public bool Standard { get; set;}
        public bool Valuable { get; set; }
        public bool Fragile { get; set; }
        public bool Heavy { get; set; }


        [Required]
        public int CustomerId { get; set; }
        public User Customer { get; set; } = null!;

        public int? DriverId { get; set; }

        public User? Driver { get; set; }

        public enum OrderStatus { Pending, Accepted, InProgress, Completed, Cancelled, Coming, Picking, Delivering}

        public double? Price { get; set; }

        public OrderStatus Status { get; set; }


        public bool ClientReminderSent { get; set; } = false;
        public bool DriverReminderSent { get; set; } = false;

        public bool NewOrderNotificationSent { get; set; } = false;
}
