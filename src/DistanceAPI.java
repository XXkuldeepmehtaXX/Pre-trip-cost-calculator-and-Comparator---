import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import com.google.gson.*;

public class DistanceAPI {
    private static final HttpClient client = HttpClient.newHttpClient();

    // sends a GET request and returns the response text
    private static String get(String url) throws Exception {
        HttpRequest req = HttpRequest.newBuilder()
            .uri(URI.create(url))
            .header("User-Agent", "PreTripCalculator/1.0 (student project)")
            .build();
        return client.send(req, HttpResponse.BodyHandlers.ofString()).body();
    }

    // place name -> {latitude, longitude}
    public static double[] geocode(String place) throws Exception {
        String url = "https://nominatim.openstreetmap.org/search?format=json&limit=1&q="
            + URLEncoder.encode(place, StandardCharsets.UTF_8);
        JsonArray arr = JsonParser.parseString(get(url)).getAsJsonArray();
        if (arr.size() == 0) throw new Exception("Place not found: " + place);
        JsonObject o = arr.get(0).getAsJsonObject();
        return new double[] { o.get("lat").getAsDouble(), o.get("lon").getAsDouble() };
    }

    // two place names -> driving distance in km
    public static double getDistanceKm(String from, String to) throws Exception {
        double[] a = geocode(from);
        Thread.sleep(1100);                 // respect the 1 request/second limit
        double[] b = geocode(to);

        // OSRM wants longitude,latitude order
        String url = "https://router.project-osrm.org/route/v1/driving/"
            + a[1] + "," + a[0] + ";" + b[1] + "," + b[0] + "?overview=false";

        JsonObject root = JsonParser.parseString(get(url)).getAsJsonObject();
        double meters = root.getAsJsonArray("routes").get(0)
            .getAsJsonObject().get("distance").getAsDouble();
        return meters / 1000.0;
    }
}