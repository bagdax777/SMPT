package com.smpt;

import android.graphics.Matrix;
import android.os.Bundle;
import android.view.GestureDetector;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.core.view.GestureDetectorCompat;
import com.bumptech.glide.Glide;
import android.view.ScaleGestureDetector;

public class FullScreenImageFragment extends Fragment {

    private String imageUrl;
    private ImageView imageView;
    private ScaleGestureDetector scaleGestureDetector;
    private GestureDetectorCompat gestureDetector;
    private Matrix matrix = new Matrix();
    private float scaleFactor = 1.0f;
    private float scrollX = 0f;
    private float scrollY = 0f;

    public FullScreenImageFragment() {
        // Wymagany pusty konstruktor publiczny
    }

    public static FullScreenImageFragment newInstance(String imageUrl) {
        FullScreenImageFragment fragment = new FullScreenImageFragment();
        Bundle args = new Bundle();
        args.putString("image_url", imageUrl);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_full_screen_image, container, false);
        imageView = view.findViewById(R.id.fullScreenImageView);
        imageView.setScaleType(ImageView.ScaleType.MATRIX);

        if (getArguments() != null) {
            imageUrl = getArguments().getString("image_url");
            Glide.with(this).load(imageUrl).into(imageView);
        }

        scaleGestureDetector = new ScaleGestureDetector(getContext(), new ScaleListener());
        gestureDetector = new GestureDetectorCompat(getContext(), new GestureListener());

        imageView.setOnTouchListener((v, event) -> {
            scaleGestureDetector.onTouchEvent(event);
            gestureDetector.onTouchEvent(event);
            return true;
        });

        return view;
    }

    private void applyImageTransform() {
        Matrix transform = new Matrix();
        transform.postTranslate(-scrollX, -scrollY);
        transform.postScale(scaleFactor, scaleFactor, imageView.getWidth() / 2f, imageView.getHeight() / 2f);
        imageView.setImageMatrix(transform);
    }

    private class ScaleListener extends ScaleGestureDetector.SimpleOnScaleGestureListener {
        @Override
        public boolean onScale(ScaleGestureDetector detector) {
            scaleFactor *= detector.getScaleFactor();
            scaleFactor = Math.max(0.1f, Math.min(scaleFactor, 5.0f)); // Ogranicz skalowanie
            applyImageTransform();
            return true;
        }
    }

    private class GestureListener extends GestureDetector.SimpleOnGestureListener {
        @Override
        public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {
            scrollX += distanceX;
            scrollY += distanceY;
            applyImageTransform();
            return true;
        }
    }
}
