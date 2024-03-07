package com.smpt;

import android.os.AsyncTask;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;

public class MyAsyncTask extends AsyncTask<Void, Void, ArrayList<Place2>> {

    private static final String TAG = MyAsyncTask.class.getSimpleName();

    public interface AsyncResponse {
        void processFinish(ArrayList<Place2> output);
    }

    private final double latitude;
    private final double longitude;
    private final AsyncResponse delegate;

    public MyAsyncTask(double latitude, double longitude, AsyncResponse delegate) {
        this.latitude = latitude;
        this.longitude = longitude;
        this.delegate = delegate;
    }

    @Override
    protected ArrayList<Place2> doInBackground(Void... voids) {
        Log.d(TAG, "Rozpoczynanie pobierania danych z Wikipedia API.");
        ArrayList<Place2> places = new ArrayList<>();
        try {
            String radius = "1000";
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
            JSONArray geoSearchResults = jsonResponse.getJSONObject("query").getJSONArray("geosearch");

            for (int i = 0; i < geoSearchResults.length(); i++) {
                JSONObject result = geoSearchResults.getJSONObject(i);
                int pageid = result.getInt("pageid");

                String wikiQueryUrl = "https://pl.wikipedia.org/w/api.php?action=query&prop=coordinates|extracts|images|info|links|pageimages&inprop=url&exlimit=max&explaintext&format=json&pageids=" + pageid;

                URL wikiQueryObj = new URL(wikiQueryUrl);
                HttpURLConnection wikiQueryCon = (HttpURLConnection) wikiQueryObj.openConnection();
                wikiQueryCon.setRequestMethod("GET");

                BufferedReader wikiQueryIn = new BufferedReader(new InputStreamReader(wikiQueryCon.getInputStream()));
                StringBuilder wikiResponse = new StringBuilder();
                String wikiInputLine;

                while ((wikiInputLine = wikiQueryIn.readLine()) != null) {
                    wikiResponse.append(wikiInputLine);
                }
                wikiQueryIn.close();

                JSONObject wikiJsonResponse = new JSONObject(wikiResponse.toString());
                JSONObject pageInfo = wikiJsonResponse.getJSONObject("query").getJSONObject("pages").getJSONObject(Integer.toString(pageid));

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
                        if (imageUrl.endsWith(".jpg")) {
                            String formattedTitle = URLEncoder.encode(imageUrl.substring(imageUrl.lastIndexOf(":") + 1), "UTF-8").replace("+", "_").replace("%20", "_");
                            String imageLink = "https://commons.wikimedia.org/wiki/File:" + formattedTitle;
                            imageUrls.add(imageLink);
                        }
                    }
                }
                String mainImageUrl = "";
                if (pageInfo.has("thumbnail")) {
                    mainImageUrl = pageInfo.getJSONObject("thumbnail").getString("source");
                }

                Place2 locationInfo = new Place2(title, extract, urlPage, latitudeResult, longitudeResult, imageUrls, mainImageUrl);
                places.add(locationInfo);
            }

        } catch (Exception e) {
            Log.e(TAG, "Error fetching data from Wikipedia API: ", e);
        }

        return places;
    }

    @Override
    protected void onPostExecute(ArrayList<Place2> result) {
        if (delegate != null) {
            delegate.processFinish(result);
        }
    }
}
