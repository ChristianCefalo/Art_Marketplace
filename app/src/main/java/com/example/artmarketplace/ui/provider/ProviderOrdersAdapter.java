package com.example.artmarketplace.ui.provider;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.artmarketplace.R;
import com.example.artmarketplace.model.Order;
import com.example.artmarketplace.util.Formatters;

import java.util.ArrayList;
import java.util.List;

/**
 * Adapter for displaying provider orders.
 */
public class ProviderOrdersAdapter extends RecyclerView.Adapter<ProviderOrdersAdapter.OrderViewHolder> {

    private final List<Order> items = new ArrayList<>();

    public void setItems(List<Order> orders) {
        items.clear();
        if (orders != null) {
            items.addAll(orders);
        }
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public OrderViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_provider_order, parent, false);
        return new OrderViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull OrderViewHolder holder, int position) {
        Order order = items.get(position);
        holder.statusView.setText(holder.itemView.getContext().getString(
                R.string.label_order_status_format, Formatters.formatStatus(order.getStatus())));
        holder.totalView.setText(holder.itemView.getContext().getString(
                R.string.label_order_total_format, Formatters.formatPrice(order.getTotal())));
        holder.orderIdView.setText(holder.itemView.getContext().getString(
                R.string.label_order_id_format, order.getId()));
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    static class OrderViewHolder extends RecyclerView.ViewHolder {
        final TextView orderIdView;
        final TextView statusView;
        final TextView totalView;

        OrderViewHolder(@NonNull View itemView) {
            super(itemView);
            orderIdView = itemView.findViewById(R.id.text_order_id);
            statusView = itemView.findViewById(R.id.text_order_status);
            totalView = itemView.findViewById(R.id.text_order_total);
        }
    }
}
