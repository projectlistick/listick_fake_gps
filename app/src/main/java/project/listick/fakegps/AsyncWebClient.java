package project.listick.fakegps;

import android.app.Activity;

import androidx.annotation.NonNull;

import org.json.JSONObject;

import java.io.File;
import java.io.IOException;
import java.util.concurrent.TimeUnit;

import okhttp3.Cache;
import okhttp3.CacheControl;
import okhttp3.Interceptor;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import okhttp3.ResponseBody;

public class AsyncWebClient {

    private Activity mActivity;
    private File mCacheDir;

    public interface Callback {
        void onSuccess(ResponseBody responseBody);

        void onError();
    }

    public AsyncWebClient(Activity activity, boolean cache) {
        this.mActivity = activity;
        this.mCacheDir = cache ? activity.getCacheDir() : null;
    }

    public AsyncWebClient(Activity activity, File cacheDir) {
        this.mActivity = activity;
        this.mCacheDir = cacheDir;
    }

    public void connect(String url, Callback callback) {
        Runnable runnable = () -> {
            int cacheSize = 10 * 1024 * 1024;
            OkHttpClient.Builder builder = new OkHttpClient.Builder()
                    .addInterceptor(new ForceCacheInterceptor());

            if (mCacheDir != null) {
                builder.cache(new Cache(mCacheDir, cacheSize));
            }

            Request request = new Request.Builder()
                    .url(url)
                    .build();

            OkHttpClient client = builder.build();

            try {
                Response response = client.newCall(request).execute();
                if (mActivity != null) {
                    mActivity.runOnUiThread(() -> callback.onSuccess(response.body()));
                } else {
                    callback.onSuccess(response.body());
                }
            } catch (IOException e) {
                android.util.Log.d(project.listick.fakegps.BuildConfig.APPLICATION_ID, null, e);
                if (mActivity != null) {
                    mActivity.runOnUiThread(callback::onError);
                } else {
                    callback.onError();
                }
            }
        };

        Thread thread = new Thread(runnable);
        thread.start();
    }

    public static class Sync {
        Request.Builder rb;
        OkHttpClient client;

        public Sync(@NonNull String url) {
            this.client = new OkHttpClient.Builder()
                    .connectTimeout(8, TimeUnit.SECONDS)
                    .callTimeout(8, TimeUnit.SECONDS)
                    .writeTimeout(8, TimeUnit.SECONDS)
                    .readTimeout(8, TimeUnit.SECONDS)
                    .retryOnConnectionFailure(false)
                    .build();
            this.rb = new Request.Builder().url(url);
        }

        public void post(JSONObject parameter) {
            MediaType JSON = MediaType.parse("application/json; charset=utf-8");
            RequestBody body = RequestBody.create(JSON, parameter.toString());
            rb.post(body);
        }

        public void addHeader(String header, String value) {
            rb.addHeader(header, value);
        }

        public String connect() {
            Request request = rb.build();
            try {
                Response response = client.newCall(request).execute();
                return response.body().string();
            } catch (IOException | NullPointerException e) {
                return null;
            }
        }
    }

    private class ForceCacheInterceptor implements Interceptor {
        @Override
        public Response intercept(Chain chain) throws IOException {
            Request.Builder builder = chain.request().newBuilder();
            if (!NetworkUtils.isNetworkAvailable()) {
                builder.cacheControl(CacheControl.FORCE_CACHE);
            }

            return chain.proceed(builder.build());
        }
    }

}
