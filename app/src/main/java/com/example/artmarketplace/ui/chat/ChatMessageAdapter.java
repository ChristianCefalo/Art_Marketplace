package com.example.artmarketplace.ui.chat;

import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.example.artmarketplace.R;
import com.example.artmarketplace.net.ChatMessage;

import java.util.ArrayList;
import java.util.List;

/**
 * Displays chat messages in a conversational layout.
 */
class ChatMessageAdapter extends RecyclerView.Adapter<ChatMessageAdapter.ChatViewHolder> {

    private static final String ROLE_USER = "user";

    private final List<ChatMessage> messages = new ArrayList<>();

    void addMessage(ChatMessage message) {
        messages.add(message);
        notifyItemInserted(messages.size() - 1);
    }

    @NonNull
    @Override
    public ChatViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_chat_message, parent, false);
        return new ChatViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ChatViewHolder holder, int position) {
        holder.bind(messages.get(position));
    }

    @Override
    public int getItemCount() {
        return messages.size();
    }

    static class ChatViewHolder extends RecyclerView.ViewHolder {

        private final TextView messageText;

        ChatViewHolder(@NonNull View itemView) {
            super(itemView);
            messageText = itemView.findViewById(R.id.text_message);
        }

        void bind(ChatMessage message) {
            messageText.setText(message.getContent());
            boolean isUser = ROLE_USER.equals(message.getRole());
            messageText.setBackgroundResource(isUser ? R.drawable.bg_chat_user : R.drawable.bg_chat_bot);
            messageText.setTextColor(ContextCompat.getColor(itemView.getContext(),
                    isUser ? android.R.color.white : R.color.black));

            FrameLayout.LayoutParams params = (FrameLayout.LayoutParams) messageText.getLayoutParams();
            params.gravity = isUser ? Gravity.END : Gravity.START;
            int horizontalMargin = itemView.getResources().getDimensionPixelSize(R.dimen.chat_bubble_horizontal_margin);
            params.setMarginStart(isUser ? horizontalMargin : 0);
            params.setMarginEnd(isUser ? 0 : horizontalMargin);
            messageText.setLayoutParams(params);
        }
    }
}
