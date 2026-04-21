using System.ComponentModel.DataAnnotations;

namespace gazelkaTest.Models
{
    public class UserNotificationSettings
    {
        [Key]
        public int userId { get; set; }

        public bool chatEnabled { get; set; } = true;
        public bool newOrdersEnabled { get; set; } = true;
        public bool statusEnabled { get; set; } = true;
        public bool upcomingEnabled { get; set; } = true;

        public int upcomingMinutes { get; set; } = 15;
    }

}
