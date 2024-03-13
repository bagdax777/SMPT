package com.smpt;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.bumptech.glide.Glide;

public class LocationDetailsFragment extends Fragment {

    public static final String ARG_PLACE = "place"; // Stabilna nazwa klucza

    private Place2 mPlace;

    public LocationDetailsFragment() {
        // Pusty konstruktor wymagany dla fragmentu
    }

    public static LocationDetailsFragment newInstance(Place2 place) {
        LocationDetailsFragment fragment = new LocationDetailsFragment();
        Bundle args = new Bundle();
        args.putParcelable(ARG_PLACE, place);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mPlace = getArguments().getParcelable(ARG_PLACE);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_location_details, container, false);

        ImageView locationImage = view.findViewById(R.id.locationImage);
        TextView textViewName = view.findViewById(R.id.nameTextView);
        TextView textViewAddress = view.findViewById(R.id.addressTextView);

        if (mPlace != null) {
            // Obsługa obrazu głównego
            String mainImageUrl = mPlace.getMainImageUrl();
            String imageUrl = mPlace.getImageUrls() != null && !mPlace.getImageUrls().isEmpty() ? mPlace.getImageUrls().get(0) : null;
            Log.d("LocationDetailsFragment", "mainImageUrl: " + mainImageUrl);
            Log.d("LocationDetailsFragment", "imageUrl: " + imageUrl);

            if (mainImageUrl != null && !mainImageUrl.isEmpty()) {
                Glide.with(this)
                        .load(mainImageUrl)
                        .centerCrop()
                        .into(locationImage);
            } else {
                // W przypadku braku URL obrazu, nie ustawiaj niczego, co spowoduje wyświetlenie miejsca na obraz bez obrazu
                locationImage.setVisibility(View.GONE); // Można też ukryć element ImageView
            }

            // Ustawianie tytułu
            String title = mPlace.getTitle() != null ? mPlace.getTitle() : "";
            textViewName.setText(title);

            // Ustawianie adresu/ekstraktu
            String address = mPlace.getExtract() != null ? mPlace.getExtract() : "";
            textViewAddress.setText(address);
        } else {
            // W przypadku braku danych miejsca, ustawiaj puste ciągi
            textViewName.setText("");
            textViewAddress.setText("");
            locationImage.setVisibility(View.GONE); // Ukryj ImageView, jeśli nie ma obrazu do wyświetlenia
        }

        return view;
    }
}
