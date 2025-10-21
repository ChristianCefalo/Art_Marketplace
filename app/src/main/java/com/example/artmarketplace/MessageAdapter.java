package com.example.artmarketplace;



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
        String body = "user".equals(m.sender)
                ? (m.prompt == null ? "" : m.prompt)
                : (m.response == null ? "" : m.response);
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


