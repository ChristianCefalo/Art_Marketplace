package com.example.artmarketplace.ui.chat;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.inputmethod.EditorInfo;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.artmarketplace.R;
import com.example.artmarketplace.net.ChatApi;
import com.example.artmarketplace.net.ChatMessage;
import com.example.artmarketplace.net.ChatRequest;
import com.example.artmarketplace.net.ChatResponse;
import com.example.artmarketplace.net.ChatServiceFactory;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * Simple conversational interface backed by the Gemini Cloud Function proxy.
 */
public class ChatActivity extends AppCompatActivity {

    private static final String ROLE_USER = "user";
    private static final String ROLE_ASSISTANT = "assistant";

    private ChatMessageAdapter adapter;
    private TextInputEditText input;
    private MaterialButton sendButton;
    private ProgressBar progressBar;
    private RecyclerView recyclerView;

    private ChatApi chatApi;
    private final List<ChatMessage> conversation = new ArrayList<>();
    private boolean isSending;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        recyclerView = findViewById(R.id.recycler_chat);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        adapter = new ChatMessageAdapter();
        recyclerView.setAdapter(adapter);

        input = findViewById(R.id.input_chat);
        sendButton = findViewById(R.id.button_send);
        progressBar = findViewById(R.id.progress_chat);

        chatApi = ChatServiceFactory.create(getString(R.string.chat_base_url));

        sendButton.setOnClickListener(v -> submitMessage());
        if (input != null) {
            input.setOnEditorActionListener((v, actionId, event) -> {
                if (actionId == EditorInfo.IME_ACTION_SEND) {
                    submitMessage();
                    return true;
                }
                return false;
            });
        }
    }

    private void submitMessage() {
        if (isSending || input == null) {
            return;
        }
        CharSequence text = input.getText();
        if (TextUtils.isEmpty(text)) {
            return;
        }
        String message = text.toString().trim();
        if (TextUtils.isEmpty(message)) {
            return;
        }

        ChatMessage userMessage = new ChatMessage(ROLE_USER, message);
        conversation.add(userMessage);
        adapter.addMessage(userMessage);
        scrollToBottom();

        input.setText(null);
        setSending(true);

        ChatRequest request = new ChatRequest(new ArrayList<>(conversation));
        chatApi.sendMessage(request).enqueue(new Callback<ChatResponse>() {
            @Override
            public void onResponse(@NonNull Call<ChatResponse> call,
                                   @NonNull Response<ChatResponse> response) {
                setSending(false);
                if (!response.isSuccessful()) {
                    showError(getString(R.string.chat_error_response, response.code()));
                    return;
                }
                ChatResponse body = response.body();
                if (body == null || TextUtils.isEmpty(body.getReply())) {
                    showError(getString(R.string.chat_error_empty));
                    return;
                }
                ChatMessage botMessage = new ChatMessage(ROLE_ASSISTANT, body.getReply());
                conversation.add(botMessage);
                adapter.addMessage(botMessage);
                scrollToBottom();
            }

            @Override
            public void onFailure(@NonNull Call<ChatResponse> call, @NonNull Throwable t) {
                setSending(false);
                showError(getString(R.string.chat_error_network, t.getMessage()));
            }
        });
    }

    private void setSending(boolean sending) {
        isSending = sending;
        sendButton.setEnabled(!sending);
        progressBar.setVisibility(sending ? View.VISIBLE : View.GONE);
    }

    private void scrollToBottom() {
        recyclerView.post(() -> recyclerView.smoothScrollToPosition(Math.max(0, adapter.getItemCount() - 1)));
    }

    private void showError(String message) {
        if (TextUtils.isEmpty(message)) {
            message = getString(R.string.chat_error_generic);
        }
        Toast.makeText(this, message, Toast.LENGTH_LONG).show();
    }
}
