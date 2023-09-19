package project.listick.fakegps;

import java.util.concurrent.TimeUnit;

import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;

public class WebClient {

    private static final WebClient sInstance = new WebClient();

    private final OkHttpClient mWebClient;

    public static WebClient getInstance() {
        return sInstance;
    }

    private WebClient() {
        OkHttpClient.Builder builder = new OkHttpClient.Builder();

        builder.connectTimeout(8, TimeUnit.SECONDS)
                .callTimeout(8, TimeUnit.SECONDS)
                .writeTimeout(8, TimeUnit.SECONDS)
                .readTimeout(8, TimeUnit.SECONDS)
                .retryOnConnectionFailure(true);

        mWebClient = builder.build();
    }

    public void makeRequest(Request request, Callback callback) {
        mWebClient.newCall(request).enqueue(callback);
    }

}
