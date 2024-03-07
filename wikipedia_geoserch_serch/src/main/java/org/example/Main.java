package org.example;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;

class Place2 {
    private String title;
    private String extract;
    private String url;
    private double latitude;
    private double longitude;
    private List<String> imageUrls;
    private String mainImageUrl;

    public Place2(String title, String extract, String url, double latitude, double longitude, List<String> imageUrls, String mainImageUrl) {
        this.title = title;
        this.extract = extract;
        this.url = url;
        this.latitude = latitude;
        this.longitude = longitude;
        this.imageUrls = imageUrls;
        this.mainImageUrl = mainImageUrl;
    }

    public String getTitle() {
        return title;
    }

    public String getExtract() {
        return extract;
    }

    public String getUrl() {
        return url;
    }

    public double getLatitude() {
        return latitude;
    }

    public double getLongitude() {
        return longitude;
    }

    public List<String> getImageUrls() {
        return imageUrls;
    }

    public String getMainImageUrl() {
        return mainImageUrl;
    }
}

public class Main {
    public static void main(String[] args) throws Exception {
        String latitude = "53.428543"; // Szerokość geograficzna przykładowej lokalizacji
        String longitude = "14.552812"; // Długość geograficzna przykładowej lokalizacji
        String radius = "1000"; // Promień w metrach
        String url = "https://pl.wikipedia.org/w/api.php?action=query&list=geosearch&gscoord="
                + latitude + "%7C" + longitude + "&gsradius=" + radius + "&gslimit=500&format=json";

        URL obj = new URL(url);
        HttpURLConnection con = (HttpURLConnection) obj.openConnection();
        con.setRequestMethod("GET");

        BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream()));
        StringBuilder response = new StringBuilder();
        String inputLine;

        while ((inputLine = in.readLine()) != null) {
            response.append(inputLine);
        }
        in.close();

        JSONObject jsonResponse = new JSONObject(response.toString());
        System.out.println("JSONRESPONSE: " + jsonResponse);
        JSONArray geoSearchResults = jsonResponse.getJSONObject("query").getJSONArray("geosearch");

        // Tworzenie tablicy, do której będą dodawane wszystkie odpowiedzi z Wikipedia
        Place2[] wikiResponses = new Place2[geoSearchResults.length()];

        // Pętla przetwarzająca każdy wynik zapytania geosearch
        for (int i = 0; i < geoSearchResults.length(); i++) {
            JSONObject result = geoSearchResults.getJSONObject(i);
            int pageid = result.getInt("pageid");

            // Wysyłanie zapytania do Wikipedia API dla każdego pageid, aby uzyskać pełne informacje o artykule
            String wikiQueryUrl = "https://pl.wikipedia.org/w/api.php?action=query&prop=coordinates|extracts|images|info|links|pageimages&inprop=url&exlimit=max&explaintext&format=json&pageids=" + pageid;

            URL wikiQueryObj = new URL(wikiQueryUrl);
            HttpURLConnection wikiQueryCon = (HttpURLConnection) wikiQueryObj.openConnection();
            wikiQueryCon.setRequestMethod("GET");

            BufferedReader wikiQueryIn = new BufferedReader(new InputStreamReader(wikiQueryCon.getInputStream()));
            StringBuilder wikiResponse = new StringBuilder();
            String wikiInputLine;

            // Odczytanie odpowiedzi
            while ((wikiInputLine = wikiQueryIn.readLine()) != null) {
                wikiResponse.append(wikiInputLine);
            }
            wikiQueryIn.close();

            // Parsowanie odpowiedzi z Wikipedia
            JSONObject wikiJsonResponse = new JSONObject(wikiResponse.toString());
            JSONObject pageInfo = wikiJsonResponse.getJSONObject("query").getJSONObject("pages").getJSONObject(Integer.toString(pageid));

            // Pobieranie tytułu, fragmentu, URL-a, koordynatów, głównego zdjęcia i linków do obrazków
            String title = pageInfo.getString("title");
            String extract = pageInfo.optString("extract", "");
            String urlPage = pageInfo.getString("fullurl");
            double latitudeResult = 0.0;
            double longitudeResult = 0.0;
            if (pageInfo.has("coordinates")) {
                JSONArray coordinates = pageInfo.getJSONArray("coordinates");
                latitudeResult = coordinates.getJSONObject(0).getDouble("lat");
                longitudeResult = coordinates.getJSONObject(0).getDouble("lon");
            }
            List<String> imageUrls = new ArrayList<>();
            if (pageInfo.has("images")) {
                JSONArray imagesArray = pageInfo.getJSONArray("images");
                for (int j = 0; j < imagesArray.length(); j++) {
                    JSONObject imageObject = imagesArray.getJSONObject(j);
                    String imageUrl = imageObject.getString("title");
                    // Sprawdzenie czy link jest w formacie .jpg
                    if (imageUrl.endsWith(".jpg")) {
                        // Konstruowanie linku do obrazu na podstawie tytułu obrazu
                        String formattedTitle = URLEncoder.encode(imageUrl.substring(imageUrl.lastIndexOf(":") + 1), "UTF-8").replace("+", "_").replace("%20", "_");
                        String imageLink = "https://commons.wikimedia.org/wiki/File:" + formattedTitle;
                        // Dodanie linku do listy
                        imageUrls.add(imageLink);
                    }
                }
            }
            String mainImageUrl = "";
            if (pageInfo.has("thumbnail")) {
                mainImageUrl = pageInfo.getJSONObject("thumbnail").getString("source");
            }

            // Tworzenie obiektu LocationInfo i zapisywanie do tablicy
            Place2 locationInfo = new Place2(title, extract, urlPage, latitudeResult, longitudeResult, imageUrls, mainImageUrl);
            wikiResponses[i] = locationInfo;
        }


        // Wyświetlenie odpowiedzi z Wikipedia dla każdego artykułu
        for (Place2 locationInfo : wikiResponses) {
            System.out.println("\n---------------------------------------------------------------------------------------\n");
            System.out.println("Title: " + locationInfo.getTitle());
            System.out.println("Extract: " + locationInfo.getExtract());
            System.out.println("URL: " + locationInfo.getUrl());
            System.out.println("Latitude: " + locationInfo.getLatitude());
            System.out.println("Longitude: " + locationInfo.getLongitude());
            System.out.println("Main Image URL: " + locationInfo.getMainImageUrl());
            System.out.println("Image URLs:");
            for (String imageUrl : locationInfo.getImageUrls()) {
                System.out.println(imageUrl);
            }
            System.out.println();
        }

        System.out.println("Liczba wyszukanych elementów: " + wikiResponses.length);
    }
}