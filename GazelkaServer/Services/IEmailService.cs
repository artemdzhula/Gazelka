namespace gazelkaTest.Services
{
    public interface IEmailService
    {
        Task SendAsync(string to, string subject, string body);
    }
}
