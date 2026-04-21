using System.Net.Http;
using System.Text.Json;
using System.Threading.Tasks;

public static class GoogleMapsService
{
    private readonly string ApiKey;
    private readonly HttpClient _httpClient;

    public GoogleMapsService(IConfiguration config, HttpClient httpClient)
    {
        ApiKey = config["GoogleMaps:ApiKey"];
        _httpClient = httpClient;
    }

    public static async Task<double?> GetDistanceKmAsync(string originAddress, string destinationAddress)
    {
        string url = $"https://maps.googleapis.com/maps/api/directions/json?origin={Uri.EscapeDataString(originAddress)}&destination={Uri.EscapeDataString(destinationAddress)}&key={ApiKey}";
        using var client = new HttpClient();
        var response = await client.GetStringAsync(url);

        using var doc = JsonDocument.Parse(response);
        var routes = doc.RootElement.GetProperty("routes");
        if (routes.GetArrayLength() == 0) return null;

        var legs = routes[0].GetProperty("legs");
        if (legs.GetArrayLength() == 0) return null;

        var distanceMeters = legs[0].GetProperty("distance").GetProperty("value").GetDouble();
        return distanceMeters / 1000.0; 
    }
}