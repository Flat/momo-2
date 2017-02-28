package io.ph.rest;

import java.io.File;
import java.io.IOException;

import okhttp3.Cache;
import okhttp3.Interceptor;
import okhttp3.OkHttpClient;
import okhttp3.Response;

public class RESTCache {
	public static OkHttpClient client;
	private static final File cacheDir = new File("resources/cache/");
	public static final int CACHE_AGE = 60 * 60 * 24 * 7;
	private static final Interceptor REWRITE_CACHE_CONTROL_INTERCEPTOR = new Interceptor() {
		@Override public Response intercept(Chain chain) throws IOException {
			Response originalResponse = chain.proceed(chain.request());
			
			return originalResponse.newBuilder()
					.header("Cache-Control", "public, max-age=" + CACHE_AGE)
					.build();
		}
	};
	
	static {
		int cacheSize = 30 * 1024 * 1024;
		Cache cache = new Cache(cacheDir, cacheSize);
		client = new OkHttpClient.Builder()
				.cache(cache)
				.addNetworkInterceptor(REWRITE_CACHE_CONTROL_INTERCEPTOR)
				.build();
	}
	
}
