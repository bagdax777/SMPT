package com.smpt;

import android.os.AsyncTask;
import android.os.Bundle;
import android.speech.tts.TextToSpeech;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.viewpager2.widget.ViewPager2;

import com.bumptech.glide.Glide;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Locale;

public class LocationDetailsFragment extends Fragment {

    private Place2 mPlace;
    private TextToSpeech textToSpeech;
    private boolean isSpeaking = false;
    private boolean isTextExpanded = false;
    private String fullText;
    private int tasksCompleted = 0;
    private ViewPager2 imagesViewPager;
    private ArrayList<String> allImageUrls = new ArrayList<>();
    private ImagesPagerAdapter adapter;

    public LocationDetailsFragment() {
        // Wymagany pusty konstruktor publiczny
    }

    public static LocationDetailsFragment newInstance(Place2 place) {
        LocationDetailsFragment fragment = new LocationDetailsFragment();
        Bundle args = new Bundle();
        args.putParcelable("place", place);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mPlace = getArguments().getParcelable("place");
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_location_details, container, false);

        imagesViewPager = view.findViewById(R.id.imagesViewPager);
        adapter = new ImagesPagerAdapter(getContext(), allImageUrls);
        imagesViewPager.setAdapter(adapter);

        TextView textViewName = view.findViewById(R.id.nameTextView);
        TextView textViewAddress = view.findViewById(R.id.addressTextView);
        Button readButton = view.findViewById(R.id.speakButton);
        Button moreButton = view.findViewById(R.id.showMoreButton);

        textViewName.setText(mPlace.getTitle());
        textViewAddress.setText(mPlace.getExtract());
        textViewAddress.setMaxLines(4);

        moreButton.setText("więcej");
        moreButton.setOnClickListener(v -> {
            if (textViewAddress.getMaxLines() == 4) {
                textViewAddress.setMaxLines(Integer.MAX_VALUE);
                moreButton.setText("mniej");
            } else {
                textViewAddress.setMaxLines(4);
                moreButton.setText("więcej");
            }
        });

        textToSpeech = new TextToSpeech(getContext(), status -> {
            if (status == TextToSpeech.SUCCESS) {
                textToSpeech.setLanguage(Locale.getDefault());
                textToSpeech.setSpeechRate(0.7f);
            }
        });

        readButton.setOnClickListener(v -> {
            if (!isSpeaking) {
                String textToRead = mPlace.getExtract().replace("=", " ");
                textToSpeech.speak(textToRead, TextToSpeech.QUEUE_FLUSH, null, "TTS");
                isSpeaking = true;
            } else {
                textToSpeech.stop();
                isSpeaking = false;
            }
        });

        if (mPlace.getImageUrls() != null && !mPlace.getImageUrls().isEmpty()) {
            // Zaktualizowany sposób przetwarzania URLi obrazów
            allImageUrls.addAll(mPlace.getImageUrls()); // Dodaj wszystkie URLi do listy
            adapter = new ImagesPagerAdapter(getContext(), allImageUrls);
            imagesViewPager.setAdapter(adapter);
        }
        if (mPlace != null && mPlace.getImageUrls() != null && !mPlace.getImageUrls().isEmpty()) {
            new FetchImagesTask().execute(mPlace.getImageUrls().toArray(new String[0]));
        }

        return view;
    }

    private String removeUnwantedChars(String text) {
        return text.replace("=", " ");
    }

    @Override
    public void onDestroy() {
        if (textToSpeech != null) {
            textToSpeech.stop();
            textToSpeech.shutdown();
        }
        super.onDestroy();
    }

    private class FetchImagesTask extends AsyncTask<String, Void, ArrayList<String>> {



        public FetchImagesTask() {
        }

        @Override
        protected ArrayList<String> doInBackground(String... imageUrls) {
            ArrayList<String> fetchedUrls = new ArrayList<>();
            for (String imageUrl : imageUrls) {
                String imageTitle = extractImageTitleFromUrl(imageUrl);

                if (imageTitle == null || imageTitle.isEmpty()) {
                    Log.d("LocationDetailsFragment", "Nie udało się wyodrębnić nazwy pliku z URL-a: " + imageUrl);
                    continue; // Przejdź do następnego URLa, jeśli nie udało się przetworzyć obecnego
                }

                try {
                    String apiUrl = "https://commons.wikimedia.org/w/api.php?action=query&titles=" + URLEncoder.encode(imageTitle, "UTF-8") + "&prop=imageinfo&iiprop=url&format=json";
                    URL url = new URL(apiUrl);
                    HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                    connection.setRequestMethod("GET");

                    BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
                    StringBuilder response = new StringBuilder();
                    String line;
                    while ((line = reader.readLine()) != null) {
                        response.append(line);
                    }

                    JSONObject jsonResponse = new JSONObject(response.toString());
                    JSONObject pages = jsonResponse.getJSONObject("query").getJSONObject("pages");
                    Iterator<String> keys = pages.keys();

                    if (keys.hasNext()) {
                        String key = keys.next();
                        if (!"-1".equals(key)) {
                            JSONObject page = pages.getJSONObject(key);
                            JSONArray imageinfo = page.getJSONArray("imageinfo");
                            if(imageinfo.length() > 0) {
                                JSONObject imageInfoObject = imageinfo.getJSONObject(0);
                                fetchedUrls.add(imageInfoObject.getString("url")); // Dodaj URL do listy
                            }
                        }
                    }
                } catch (Exception e) {
                    Log.e("LocationDetailsFragment", "Wyjątek podczas pobierania URL-a obrazu: " + imageUrl, e);
                }
            }
            return fetchedUrls; // Zwróć listę przetworzonych URLi
        }


        @Override
        protected void onPostExecute(ArrayList<String> result) {
            if (result != null && !result.isEmpty()) {
                allImageUrls.clear();
                allImageUrls.addAll(result);
                adapter.notifyDataSetChanged(); // Powiadom adapter o zmianie danych
            }
        }

        private String extractImageTitleFromUrl(String imageUrl) {
            try {
                String decodedUrl = java.net.URLDecoder.decode(imageUrl, "UTF-8");
                String[] parts = decodedUrl.split("/File:");
                if (parts.length > 1) {
                    return "File:" + parts[1];
                }
            } catch (Exception e) {
                Log.e("LocationDetailsFragment", "Błąd podczas dekodowania URL-a: ", e);
            }
            return null;
        }
    }
}
