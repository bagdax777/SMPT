package com.smpt;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.fragment.app.FragmentActivity;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;

import java.util.List;

public class ImagesPagerAdapter extends RecyclerView.Adapter<ImagesPagerAdapter.ViewHolder> {

    private List<String> imageUrls;
    private LayoutInflater inflater;
    private Context context;

    public ImagesPagerAdapter(Context context, List<String> imageUrls) {
        this.inflater = LayoutInflater.from(context);
        this.imageUrls = imageUrls;
        this.context = context;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = inflater.inflate(R.layout.image_item, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Glide.with(holder.imageView.getContext())
                .load(imageUrls.get(position))
                .into(holder.imageView);

        // Obsługa kliknięcia na obraz
        holder.imageView.setOnClickListener(v -> {
            // Sprawdzanie, czy kontekst jest instancją FragmentActivity
            if (context instanceof FragmentActivity) {
                FragmentActivity activity = (FragmentActivity) context;
                String imageUrl = imageUrls.get(position);
                FullScreenImageFragment fullScreenImageFragment = FullScreenImageFragment.newInstance(imageUrl);
                // Rozpoczęcie transakcji fragmentu
                activity.getSupportFragmentManager().beginTransaction()
                        .replace(R.id.details_container, fullScreenImageFragment) // Upewnij się, że używasz właściwego ID kontenera fragmentów
                        .addToBackStack(null) // Dodaj fragment do stosu powrotu, aby umożliwić użytkownikowi powrót do poprzedniego widoku
                        .commit();
            }
        });
    }

    @Override
    public int getItemCount() {
        return imageUrls.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        ImageView imageView;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            imageView = itemView.findViewById(R.id.imageView); // Upewnij się, że ID imageView odpowiada ID w Twoim layoutcie
        }
    }
}
