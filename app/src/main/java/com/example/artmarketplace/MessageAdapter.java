package com.example.artmarketplace;


import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.util.ArrayList;
import java.util.List;

public class MessageAdapter extends RecyclerView.Adapter<MessageAdapter.VH> {
    private final List<Message> items = new ArrayList<>();

    public void submitList(List<Message> list) {
        items.clear();
        items.addAll(list);
        notifyDataSetChanged();
    }

    @NonNull @Override
    public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_message, parent, false);
        return new VH(v);
    }

    @Override
    public void onBindViewHolder(@NonNull VH h, int position) {
        Message m = items.get(position);
        String text = "user".equals(m.sender)
                ? (m.prompt != null ? m.prompt : "")
                : (m.response != null && !m.response.isEmpty()
                ? m.response
                : (m.prompt != null ? m.prompt : "…"));

        h.tv.setText(text);

        // Align right for user; left for bot/system
        FrameLayout.LayoutParams lp = (FrameLayout.LayoutParams) h.tv.getLayoutParams();
        lp.gravity = "user".equals(m.sender) ? Gravity.END : Gravity.START;
        h.tv.setLayoutParams(lp);

        // Basic colors without extra drawables
        int pad = (int) (10 * h.tv.getResources().getDisplayMetrics().density);
        h.tv.setPadding(pad, pad, pad, pad);
        if ("user".equals(m.sender)) {
            h.tv.setBackgroundColor(0xFF2196F3); // blue
            h.tv.setTextColor(0xFFFFFFFF);
        } else { // bot/system
            h.tv.setBackgroundColor(0xFF9E9E9E); // gray
            h.tv.setTextColor(0xFFFFFFFF);
        }
    }

    @Override public int getItemCount() { return items.size(); }

    static class VH extends RecyclerView.ViewHolder {
        TextView tv;
        VH(@NonNull View itemView) { super(itemView); tv = itemView.findViewById(R.id.tvText); }
    }
}

