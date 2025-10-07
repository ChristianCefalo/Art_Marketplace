package com.example.artmarketplace.net;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.POST;

/**
 * Retrofit definition for invoking the chat Cloud Function proxy.
 */
public interface ChatApi {

    /**
     * Sends the provided chat history to the Cloud Function and returns the assistant's reply.
     *
     * @param request ordered list of messages exchanged so far
     * @return Retrofit call yielding a {@link ChatResponse} payload
     */
    @POST("api/chat-gemini")
    Call<ChatResponse> sendMessage(@Body ChatRequest request);
}
