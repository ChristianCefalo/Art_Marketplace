package com.example.artmarketplace.ui.provider;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.artmarketplace.R;
import com.example.artmarketplace.model.ArtItem;
import com.example.artmarketplace.util.Formatters;

import java.util.ArrayList;
import java.util.List;

/**
 * Adapter presenting provider-owned art listings with edit controls.
 */
public class ProviderArtAdapter extends RecyclerView.Adapter<ProviderArtAdapter.ProviderArtViewHolder> {

    interface OnItemActionListener {
        void onEditClicked(ArtItem artItem);
    }

    private final OnItemActionListener actionListener;
    private final List<ArtItem> items = new ArrayList<>();

    public ProviderArtAdapter(OnItemActionListener actionListener) {
        this.actionListener = actionListener;
    }

    public void setItems(List<ArtItem> newItems) {
        items.clear();
        if (newItems != null) {
            items.addAll(newItems);
        }
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ProviderArtViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_provider_art, parent, false);
        return new ProviderArtViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ProviderArtViewHolder holder, int position) {
        final ArtItem artItem = items.get(position);
        holder.titleView.setText(artItem.getTitle());
        holder.priceView.setText(Formatters.formatPrice(artItem.getPrice()));
        holder.editButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                actionListener.onEditClicked(artItem);
            }
        });
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    static class ProviderArtViewHolder extends RecyclerView.ViewHolder {
        final TextView titleView;
        final TextView priceView;
        final Button editButton;

        ProviderArtViewHolder(@NonNull View itemView) {
            super(itemView);
            titleView = itemView.findViewById(R.id.text_art_title);
            priceView = itemView.findViewById(R.id.text_art_price);
            editButton = itemView.findViewById(R.id.button_edit_listing);
        }
    }
}
