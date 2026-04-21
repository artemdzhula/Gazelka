
    public class Chat
    {
        public Chat()
        {
            Messages = new List<Message>();
        }
        public int Id { get; set; }
        public int User1Id { get; set; } //customer
        public int? User2Id { get; set; } //driver
        public List<Message> Messages { get; set; }
        public int OrderId { get; set; }
    }

