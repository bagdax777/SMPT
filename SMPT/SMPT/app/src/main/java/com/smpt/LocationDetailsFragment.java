package com.smpt;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

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
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mPlace = getArguments().getParcelable(ARG_PLACE);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_location_details, container, false);

        if (mPlace != null) {
            ImageView locationImage = view.findViewById(R.id.locationImage);
            String mainImageUrl = mPlace.getMainImageUrl();
            Glide.with(this)
                    .load(mainImageUrl)
                    .centerCrop()
                    .into(locationImage);

            TextView textViewName = view.findViewById(R.id.nameTextView);
            textViewName.setText(mPlace.getTitle());

            TextView textViewAddress = view.findViewById(R.id.addressTextView);
            // Zakładając, że `getCode()` zwraca adres
            textViewAddress.setText(mPlace.getExtract());
        }

        return view;
    }
}
