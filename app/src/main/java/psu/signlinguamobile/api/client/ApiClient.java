package psu.signlinguamobile.api.client;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import java.io.IOException;

import okhttp3.Interceptor;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import psu.signlinguamobile.data.Constants;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import okhttp3.logging.HttpLoggingInterceptor;

public class ApiClient {

    private static Retrofit retrofit = null;
    private static final String BASE_URL = Constants.ApiRoutes.BASE;

    /**
     * Build a retrofit API client with an option to include encrypted user id
     * @param context
     * @param attachUserAuthId -> The encrypted id from Laravel's encrypt(Auth::user()->id)
     * @return
     */
    private static Retrofit buildClient(Context context, boolean attachUserAuthId)
    {
        // Create a logging interceptor
        HttpLoggingInterceptor loggingInterceptor = new HttpLoggingInterceptor();
        loggingInterceptor.setLevel(HttpLoggingInterceptor.Level.BODY); // Logs full request/response

        // Create an OkHttpClient with an interceptor that adds the token if it exists
        OkHttpClient client = new OkHttpClient.Builder()
                .addInterceptor(loggingInterceptor) // Logs all requests
                .addInterceptor(new Interceptor() {
                    @Override
                    public Response intercept(Chain chain) throws IOException {
                        Request original = chain.request();

                        Log.d("Retrofit", chain.request().url().toString());

                        SharedPreferences prefs = context.getSharedPreferences(Constants.SharedPrefKeys.AUTH, Context.MODE_PRIVATE);
                        String token = prefs.getString(Constants.SharedPrefKeys.TOKEN, null);

                        Request.Builder builder = original.newBuilder()
                                .header("Accept", "application/json"); // Ensure Laravel treats it as an API request

                        if (attachUserAuthId)
                        {
                            String encryptedUserId = prefs.getString(Constants.SharedPrefKeys.USER_ID, null);

                            if (encryptedUserId != null)
                                builder.header("X-Encrypted-User-ID", encryptedUserId);
                        }

                        if (token != null)
                        {
                            builder.header("Authorization", "Bearer " + token);
                            Log.d("MINE", "Authorization: Bearer " + token);
                        }

                        Request request = builder.build();
                        return chain.proceed(request);
                    }
                })
                .build();

        retrofit = new Retrofit.Builder()
                .baseUrl(BASE_URL)
                .client(client)
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        return retrofit;
    }

    /**
     * Build the api client without an auth id
     */
    public static Retrofit getClient(Context context)
    {
        return buildClient(context, false);
    }

    /**
     * Build the api client with an attached auth id.
     * If false, we return the client with no auth id
     */
    public static Retrofit getClient(Context context, boolean attachUserAuthId)
    {
        return buildClient(context, attachUserAuthId);
    }

    // .header("X-Encrypted-User-ID", encryptedUserId)
}
