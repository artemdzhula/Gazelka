using gazelkaTest.Models;
using System.ComponentModel.DataAnnotations;


    public class User
    {
        [Key]
        public int Id { get; set; }


        [Required]
        public string Email { get; set; } = null!;


        [Required]
        public string PasswordHash { get; set; } = null!;


        public string? Name { get; set; }

        public string? Surname { get; set; }

        public string? PhoneNumber { get; set; }

        [Required]
        public string Role { get; set; } = null!;

        public string? CarType { get; set; }
        public string? CarColor { get; set; }

        public string? CarNumber { get; set; }

        public DateTime CreatedAt { get; set; } = DateTime.UtcNow;


        public int FailedLoginAttempts { get; set; }


        public DateTime? LockoutUntil { get; set; }


        public bool EmailConfirmed { get; set; }

        public string? EmailConfirmationCode { get; set; }

        public string? PasswordResetCode { get; set; }

        public List<RefreshToken> RefreshTokens { get; set; } = new();

        public UserNotificationSettings NotificationSettings { get; set; }

        public string? CityName { get; set; }

    }

