package com.example.artmarketplace.util;

import android.content.Context;
import android.widget.Toast;

import androidx.annotation.NonNull;

import com.example.artmarketplace.BuildConfig;
import com.example.artmarketplace.R;
import com.example.artmarketplace.model.ArtItem;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.firestore.WriteBatch;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Seeds debug sample data into Firestore to accelerate local development.
 */
public final class SeedDataUtil {

    private static final String COLLECTION_ART = "art";
    private static final String PROVIDER_UID = "demo-provider";
    private static final AtomicBoolean ATTEMPTED = new AtomicBoolean(false);

    private SeedDataUtil() {
        // No instances.
    }

    public static void seedIfEmpty(@NonNull final Context context) {
        if (!BuildConfig.DEBUG) {
            return;
        }
        if (!ATTEMPTED.compareAndSet(false, true)) {
            return;
        }

        final FirebaseFirestore firestore = FirebaseFirestore.getInstance();
        firestore.collection(COLLECTION_ART)
                .limit(1)
                .get()
                .addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                    @Override
                    public void onSuccess(QuerySnapshot querySnapshot) {
                        if (querySnapshot == null || !querySnapshot.isEmpty()) {
                            return;
                        }
                        Toast.makeText(context, R.string.seed_toast_populating, Toast.LENGTH_SHORT).show();
                        writeSeedData(context, firestore.collection(COLLECTION_ART));
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        ATTEMPTED.set(false);
                    }
                });
    }

    private static void writeSeedData(@NonNull final Context context,
                                      @NonNull CollectionReference artCollection) {
        long now = System.currentTimeMillis();
        List<ArtItem> samples = new ArrayList<>();
        samples.add(new ArtItem(null, "Sunset Over the Bay", 12500,
                "Vibrant oranges and purples reflecting on calm water.",
                "https://images.unsplash.com/photo-1500530855697-b586d89ba3ee",
                PROVIDER_UID, now));
        samples.add(new ArtItem(null, "Abstract Spectrum", 8900,
                "Bold abstract strokes exploring the color wheel.",
                "https://images.unsplash.com/photo-1500530855697-b586d89ba3ee?fit=crop&w=800",
                PROVIDER_UID, now - 60000));
        samples.add(new ArtItem(null, "Minimalist Mountains", 6400,
                "Geometric peaks in cool blue gradients for modern spaces.",
                "https://images.unsplash.com/photo-1469474968028-56623f02e42e?fit=crop&w=800",
                PROVIDER_UID, now - 120000));
        samples.add(new ArtItem(null, "Botanical Study", 7200,
                "Detailed ink illustration of lush tropical leaves.",
                "https://images.unsplash.com/photo-1470071459604-3b5ec3a7fe05?fit=crop&w=800",
                PROVIDER_UID, now - 180000));
        samples.add(new ArtItem(null, "City Neon Nights", 15800,
                "Cyberpunk skyline awash with neon reflections.",
                "https://images.unsplash.com/photo-1500530855697-b586d89ba3ee?fit=crop&w=900",
                PROVIDER_UID, now - 240000));
        samples.add(new ArtItem(null, "Ocean Calm", 5300,
                "Soft brushwork capturing gentle morning tides.",
                "https://images.unsplash.com/photo-1507525428034-b723cf961d3e?fit=crop&w=900",
                PROVIDER_UID, now - 300000));

        WriteBatch batch = artCollection.getFirestore().batch();
        for (ArtItem sample : samples) {
            batch.set(artCollection.document(), sample);
        }
        batch.commit()
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void unused) {
                        Toast.makeText(context, R.string.seed_toast_complete, Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        ATTEMPTED.set(false);
                    }
                });
    }
}
