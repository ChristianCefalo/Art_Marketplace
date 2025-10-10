package com.example.artmarketplace.ui.customer;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.artmarketplace.R;
import com.example.artmarketplace.model.ArtItem;
import com.example.artmarketplace.util.Formatters;

import java.util.ArrayList;
import java.util.List;

/**
 * RecyclerView adapter for displaying art listings to customers.
 */
class ArtAdapter extends RecyclerView.Adapter<ArtAdapter.ArtViewHolder> {

    interface OnItemClickListener {
        void onItemClicked(ArtItem item);
    }

    private final List<ArtItem> items = new ArrayList<>();
    private final OnItemClickListener listener;

    ArtAdapter(OnItemClickListener listener) {
        this.listener = listener;
    }

    void setItems(List<ArtItem> newItems) {
        items.clear();
        if (newItems != null) {
            items.addAll(newItems);
        }
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ArtViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_art, parent, false);
        return new ArtViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ArtViewHolder holder, int position) {
        holder.bind(items.get(position));
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    class ArtViewHolder extends RecyclerView.ViewHolder {

        private final TextView titleText;
        private final TextView priceText;

        ArtViewHolder(@NonNull View itemView) {
            super(itemView);
            titleText = itemView.findViewById(R.id.text_art_title);
            priceText = itemView.findViewById(R.id.text_art_price);
        }

        void bind(final ArtItem artItem) {
            titleText.setText(artItem.getTitle());
            priceText.setText(Formatters.formatPrice(artItem.getPrice()));
            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    listener.onItemClicked(artItem);
                }
            });
        }
    }
}
