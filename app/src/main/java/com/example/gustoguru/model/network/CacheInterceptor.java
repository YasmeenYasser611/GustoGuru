package com.example.gustoguru.model.network;

import android.content.Context;

import android.content.Context;
import okhttp3.CacheControl;
import okhttp3.Interceptor;
import okhttp3.Request;
import okhttp3.Response;
import java.io.IOException;

public class CacheInterceptor implements Interceptor {
    private Context context;

    public CacheInterceptor(Context context) {
        this.context = context;
    }

    @Override
    public Response intercept(Chain chain) throws IOException {
        Request request = chain.request();

        // If offline, force cache
        if (!NetworkUtil.isNetworkAvailable(context)) {
            request = request.newBuilder()
                    .cacheControl(CacheControl.FORCE_CACHE)
                    .build();
        }

        Response response = chain.proceed(request);

        // If online, cache for 1 hour
        if (NetworkUtil.isNetworkAvailable(context)) {
            return response.newBuilder()
                    .header("Cache-Control", "public, max-age=" + (60 * 60))
                    .removeHeader("Pragma")
                    .build();
        }
        // If offline, tolerate 1 week stale
        else {
            return response.newBuilder()
                    .header("Cache-Control", "public, only-if-cached, max-stale=" + (60 * 60 * 24 * 7))
                    .removeHeader("Pragma")
                    .build();
        }
    }
}