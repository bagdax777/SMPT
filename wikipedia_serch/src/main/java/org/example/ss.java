package org.example;

import java.io.*;
import java.net.*;
import org.json.*;

public class ss {
    public static void main(String[] args) throws IOException, JSONException {
        String pageTitle = "Szczecin"; // Tytuł strony do wyszukiwania
        String apiUrl2 = "https://pl.wikipedia.org/w/api.php?action=query&titles=" + URLEncoder.encode(pageTitle, "UTF-8") + "&prop=extracts&extracts|images&explaintext&format=json";

        URL url2 = new URL(apiUrl2);
        HttpURLConnection connection2 = (HttpURLConnection) url2.openConnection();
        connection2.setRequestMethod("GET");

        BufferedReader in2 = new BufferedReader(new InputStreamReader(connection2.getInputStream()));
        String inputLine2;
        StringBuilder response2 = new StringBuilder();

        while ((inputLine2 = in2.readLine()) != null) {
            response2.append(inputLine2);
        }
        in2.close();

        // Przetwarzanie JSON
        JSONObject jsonResponse = new JSONObject(response2.toString());
        JSONObject pages = jsonResponse.getJSONObject("query").getJSONObject("pages");
        String pageId = pages.keys().next(); // Pobranie pierwszego klucza (ID strony)
        String extract = pages.getJSONObject(pageId).getString("extract");

        System.out.println(response2); // Wypisanie wstępu strony
    }
}
