using System.ComponentModel.DataAnnotations;


    public class RefreshToken
    {
        [Key]
        public int Id { get; set; }
        [Required]
        public string Token { get; set; } = null!;
        public DateTime Expires { get; set; }
        public bool IsRevoked { get; set; } = false;
        public DateTime CreatedAt { get; set; } = DateTime.UtcNow;


        // optional metadata
        public string? Ip { get; set; }
        public string? UserAgent { get; set; }


        // relations
        public int UserId { get; set; }
        public User User { get; set; } = null!;
    }

