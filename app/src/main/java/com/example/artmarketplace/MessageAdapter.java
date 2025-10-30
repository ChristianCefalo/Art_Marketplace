package com.example.artmarketplace;

import android.content.Intent;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;


public class MessageAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    private static final int USER = 1, AI = 2;
    private List<Message> data = new ArrayList<>();

    public void submitList(List<Message> list) {
        data = (list == null) ? new ArrayList<>() : list;
        notifyDataSetChanged();
    }

    @Override
    public int getItemViewType(int position) {
        return "user".equals(data.get(position).sender) ? USER : AI;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int type) {
        int layout = (type == USER) ? R.layout.item_msg_user : R.layout.item_msg_ai;
        View v = LayoutInflater.from(parent.getContext()).inflate(layout, parent, false);
        return new VH(v);
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int pos) {
        VH vh = (VH) holder;
        Message m = data.get(pos);

        String body;

        if (m.attachment != null && m.attachment.get("url") != null) {
            String name = String.valueOf(m.attachment.get("name"));
            if (name == null || name.trim().isEmpty()) name = "attachment";
            body = "[Attachment] " + name;
            String url = String.valueOf(m.attachment.get("url"));
            vh.itemView.setOnClickListener(v -> {
                try {
                    Intent i = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
                    v.getContext().startActivity(i);
                } catch (Exception ignore) { }
            });
        } else {
            body = "user".equals(m.sender)
                    ? (m.prompt == null ? "" : m.prompt)
                    : (m.response == null ? "" : m.response);
            vh.itemView.setOnClickListener(null);
        }

        vh.tv.setText(body);
    }

    @Override
    public int getItemCount() {
        return (data == null) ? 0 : data.size();
    }

    static class VH extends RecyclerView.ViewHolder {
        TextView tv;
        VH(@NonNull View v) { super(v); tv = v.findViewById(R.id.tv); }
    }
}
