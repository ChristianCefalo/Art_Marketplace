package com.example.artmarketplace.net;

import androidx.annotation.NonNull;

import com.example.artmarketplace.BuildConfig;

import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

/**
 * Builds Retrofit services for the chat integration.
 */
public final class ChatServiceFactory {

    private ChatServiceFactory() {
        // No instances.
    }

    /**
     * Creates a {@link ChatApi} instance targeting the supplied base URL.
     *
     * @param baseUrl Cloud Function host (must end with a trailing slash)
     * @return configured Retrofit chat service
     */
    @NonNull
    public static ChatApi create(@NonNull String baseUrl) {
        OkHttpClient.Builder clientBuilder = new OkHttpClient.Builder();
        if (BuildConfig.DEBUG) {
            HttpLoggingInterceptor interceptor = new HttpLoggingInterceptor();
            interceptor.setLevel(HttpLoggingInterceptor.Level.BODY);
            clientBuilder.addInterceptor(interceptor);
        }

        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(baseUrl)
                .client(clientBuilder.build())
                .addConverterFactory(GsonConverterFactory.create())
                .build();
        return retrofit.create(ChatApi.class);
    }
}
