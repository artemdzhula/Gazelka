using System.Text.Json.Serialization;

public class NotificationSettingsDto
{
    [JsonPropertyName("chatEnabled")]
    public bool chatEnabled { get; set; }

    [JsonPropertyName("newOrdersEnabled")]
    public bool newOrdersEnabled { get; set; }

    [JsonPropertyName("statusEnabled")]
    public bool statusEnabled { get; set; }

    [JsonPropertyName("upcomingEnabled")]
    public bool upcomingEnabled { get; set; }

    [JsonPropertyName("upcomingMinutes")]
    public int upcomingMinutes { get; set; }
}
