package com.smpt;

import android.os.AsyncTask;
import android.util.Log;
import android.widget.Toast;

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
        Log.d(TAG, "Rozpoczynanie pobierania danych z Google Places API.");
        String apiKey = params[0];
        double latitude = Double.parseDouble(params[1]);
        double longitude = Double.parseDouble(params[2]);
        String placesUrl = "https://maps.googleapis.com/maps/api/place/nearbysearch/json?" +
                "location=" + latitude + "," + longitude +
                "&radius=3500" +
                "&type=museum|tourist_attraction|church|city_hall|amusement_park|art_gallery|cemetery|mosque|stadium|synagogue|university|zoo" +
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
                Log.d("MOJ", "JSON:"+ placeJson);

                String name = placeJson.has("name") ? placeJson.getString("name") : "Brak danych";
                String code = placeJson.has("plus_code") ? placeJson.getJSONObject("plus_code").getString("compound_code") : "Brak danych";
                Boolean open = placeJson.has("opening_hours") ? placeJson.getJSONObject("opening_hours").getBoolean("open_now") : null;
                Integer rate = placeJson.has("rating") ? placeJson.getInt("rating") : null;
                String address = placeJson.has("vicinity") ? placeJson.getString("vicinity") : "Brak danych";


                JSONArray typesArray = placeJson.getJSONArray("types");
                String[] types = new String[typesArray.length()];
                for (int j = 0; j < typesArray.length(); j++) {
                    types[j] = typesArray.getString(j);
                }

                double lat = placeJson.getJSONObject("geometry").getJSONObject("location").getDouble("lat");
                double lng = placeJson.getJSONObject("geometry").getJSONObject("location").getDouble("lng");
                String photoReference = null;


                if (placeJson.has("photos") && !placeJson.getJSONArray("photos").isNull(0)) {
                    JSONArray photosArray = placeJson.getJSONArray("photos");
                    JSONObject photoObject = photosArray.getJSONObject(0);
                    photoReference = photoObject.getString("photo_reference");
                }

                String photoUrl = null;
                if (photoReference != null) {
                    photoUrl = "https://maps.googleapis.com/maps/api/place/photo?" +
                            "maxwidth=400" +
                            "&photoreference=" + photoReference +
                            "&key=" + apiKey;
                }

                places.add(new Place(name, lat, lng, photoUrl, code, open, rate, address, types));
                if (photoUrl != null) {
                    Log.d("MOJ2", photoUrl);
                } else {
                    Log.d("MOJ2", "photoUrl jest null");}
            }

            Log.d("MOJ2", "Zakończono pobieranie danych. Liczba pobranych miejsc: " + places.size());
            urlConnection.disconnect();
        } catch (Exception e) {
            Log.e("MOJ2", "Error parsing JSON response: ", e);
        }

        return places;
    }

    @Override
    protected void onPostExecute(ArrayList<Place> result) {
        Log.d("MOJ2", "Zakończono procesowanie danych. Liczba przekazanych miejsc: " + result.size());
        delegate.processFinish(result);
    }
}
