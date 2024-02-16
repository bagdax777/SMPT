package org.example;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.*;
import java.net.*;
import java.util.Iterator;

public class Main {
    public static StringBuilder tablica(String name) {
        try {
            String pageTitle = name;
            String apiUrl = "https://pl.wikipedia.org/w/api.php?action=opensearch&search=" + URLEncoder.encode(pageTitle, "UTF-8") + "&limit=10&namespace=0&format=json";

            URL url = new URL(apiUrl);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("GET");

            BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream()));
            String inputLine;
            StringBuilder response = new StringBuilder();

            while ((inputLine = in.readLine()) != null) {
                response.append(inputLine);
            }
            in.close();

            return response;

        } catch (IOException e) {
            System.out.println("Wystąpił błąd podczas komunikacji z serwerem: " + e.getMessage());
            return null;
        }
    }

    private static final double EARTH_RADIUS_KM = 6371.0;

    private static double toRadians(double degrees) {
        return degrees * Math.PI / 180.0;
    }

    public static double calculateDistance(double lat1, double lon1, double lat2, double lon2) {
        lat1 = toRadians(lat1);
        lon1 = toRadians(lon1);
        lat2 = toRadians(lat2);
        lon2 = toRadians(lon2);

        double dLat = lat2 - lat1;
        double dLon = lon2 - lon1;

        double a = Math.pow(Math.sin(dLat / 2), 2) + Math.cos(lat1) * Math.cos(lat2) * Math.pow(Math.sin(dLon / 2), 2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        return EARTH_RADIUS_KM * c;
    }

    public static void main(String[] args) {
        try {
            String zapytanie = "wały chrobrego";
            StringBuilder tab = tablica(zapytanie);
            JSONArray searchResults = new JSONArray(tab.toString()).getJSONArray(1);
            if (searchResults.length() == 0) {
                System.out.println("Brak wyników wyszukiwania.");
                return;
            }

            Double lat = 53.416665;
            Double lon = 14.583331;

            Double temp = Double.MAX_VALUE;
            String sear = "";
            double distance;

            for (int i = 0; i < searchResults.length(); i++) {
                String search = searchResults.getString(i);
                String apiUrl2 = "https://pl.wikipedia.org/w/api.php?action=query&titles=" + URLEncoder.encode(search, "UTF-8") + "&prop=coordinates&format=json";

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

                JSONObject jsonResponse = new JSONObject(response2.toString());
                JSONObject query = jsonResponse.getJSONObject("query");
                JSONObject pages = query.optJSONObject("pages");
                if (pages != null) {
                    Iterator<String> keys = pages.keys();
                    while(keys.hasNext()) {
                        String key = keys.next();
                        JSONObject page = pages.getJSONObject(key);
                        if (page.has("coordinates")) {
                            JSONObject coordinates = page.getJSONArray("coordinates").getJSONObject(0);
                            Double latitude = coordinates.getDouble("lat");
                            Double longitude = coordinates.getDouble("lon");
                            String title = page.getString("title");

                            distance = calculateDistance(lat, lon, latitude, longitude);
                            if (temp > distance) {
                                temp = distance;
                                sear = title;
                            }
                            System.out.println(title + "  " + latitude + " " + longitude + "           DISTANCE: " + distance);
                        } else {
                            System.out.println("Dla strony " + search + " brak danych o współrzędnych.");
                        }
                    }
                } else {
                    System.out.println("Brak stron dla zapytania: " + zapytanie);
                }
            }
            if (sear.equals("")) {
                System.out.println("Nie znaleziono stron z danymi o współrzędnych.");
            } else {
                System.out.println("\nNAJBLIZEJ: " + sear + "    " + temp);

                String apiUrl3 = "https://pl.wikipedia.org/w/api.php?action=query&titles=" + URLEncoder.encode(sear, "UTF-8") + "&prop=extracts|images&explaintext&format=json";

                URL url3 = new URL(apiUrl3);
                HttpURLConnection connection3 = (HttpURLConnection) url3.openConnection();
                connection3.setRequestMethod("GET");

                BufferedReader in3 = new BufferedReader(new InputStreamReader(connection3.getInputStream()));
                String inputLine3;
                StringBuilder response3 = new StringBuilder();

                while ((inputLine3 = in3.readLine()) != null) {
                    response3.append(inputLine3);
                }
                in3.close();

                JSONObject jsonObject = new JSONObject(response3.toString());
                JSONObject query = jsonObject.optJSONObject("query");

                if (query != null) {
                    JSONObject pages = query.optJSONObject("pages");
                    if (pages != null) {
                        Iterator<String> keys = pages.keys();
                        while(keys.hasNext()) {
                            String key = keys.next();
                            JSONObject page = pages.getJSONObject(key);
                            String extract = page.optString("extract");
                            System.out.println("\nExtract:");
                            System.out.println(extract);

                            JSONArray images = page.optJSONArray("images");
                            if (images != null) {
                                for (int i = 0; i < images.length(); i++) {
                                    JSONObject imageObject = images.getJSONObject(i);
                                    String title = imageObject.getString("title");
                                    String formattedTitle = URLEncoder.encode(title.substring(title.lastIndexOf(":") + 1), "UTF-8").replace("+", "_");
                                    String imageLink = "https://commons.wikimedia.org/wiki/File:" + formattedTitle;
                                    System.out.println(imageLink);
                                }
                            }
                        }
                    } else {
                        System.out.println("Nie znaleziono danych w bazie.");
                    }
                } else {
                    System.out.println("Nie znaleziono danych w bazie.");
                }
            }
        } catch (IOException e) {
            System.out.println("Wystąpił błąd podczas komunikacji z serwerem: " + e.getMessage());
        } catch (Exception e) {
            System.out.println("Wystąpił nieoczekiwany błąd: " + e.getMessage());
        }
    }
}
