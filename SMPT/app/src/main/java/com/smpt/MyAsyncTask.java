package com.smpt;

import android.os.AsyncTask;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;

public class MyAsyncTask extends AsyncTask<String, Void, ArrayList<Place>> {

    private static final String TAG = MyAsyncTask.class.getSimpleName();

    public interface AsyncResponse {
        void processFinish(ArrayList<Place> output);
    }

    public AsyncResponse delegate = null;

    public MyAsyncTask(AsyncResponse delegate) {
        this.delegate = delegate;
    }

    @Override
    protected ArrayList<Place> doInBackground(String... params) {
        String apiKey = params[0];
        double latitude = Double.parseDouble(params[1]);
        double longitude = Double.parseDouble(params[2]);
        String placesUrl = "https://maps.googleapis.com/maps/api/place/nearbysearch/json?" +
                "location=" + latitude + "," + longitude +
                "&radius=1000" +
                "&type=museum" +
                "&key=" + apiKey;

        ArrayList<Place> places = new ArrayList<>();

        try {
            URL url = new URL(placesUrl);
            HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
            InputStream in = urlConnection.getInputStream();
            BufferedReader reader = new BufferedReader(new InputStreamReader(in));

            StringBuilder result = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                result.append(line);
            }

            JSONObject jsonObject = new JSONObject(result.toString());
            JSONArray results = jsonObject.getJSONArray("results");

            for (int i = 0; i < results.length(); i++) {
                JSONObject placeJson = results.getJSONObject(i);
                String name = placeJson.getString("name");
                double lat = placeJson.getJSONObject("geometry").getJSONObject("location").getDouble("lat");
                double lng = placeJson.getJSONObject("geometry").getJSONObject("location").getDouble("lng");
                String photoReference = null;

                // Check if photos array is present and not empty
                if (placeJson.has("photos") && !placeJson.getJSONArray("photos").isNull(0)) {
                    JSONArray photosArray = placeJson.getJSONArray("photos");
                    JSONObject photoObject = photosArray.getJSONObject(0);
                    photoReference = photoObject.getString("photo_reference");
                }

                // Construct photo URL if photo reference is available
                String photoUrl = null;
                if (photoReference != null) {
                    photoUrl = "https://maps.googleapis.com/maps/api/place/photo?" +
                            "maxwidth=400" +
                            "&photoreference=" + photoReference +
                            "&key=" + apiKey;
                }

                places.add(new Place(name, lat, lng, photoUrl));
                Log.d(TAG, "TESTOWE: " + photoUrl);
            }

            urlConnection.disconnect();
        } catch (Exception e) {
            Log.e(TAG, "Error parsing JSON response: ", e);
        }

        return places;
    }

    @Override
    protected void onPostExecute(ArrayList<Place> result) {
        super.onPostExecute(result);
        delegate.processFinish(result);
    }
}
