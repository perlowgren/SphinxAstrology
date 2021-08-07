package net.spirangle.sphinx.services;

import android.content.Context;

import com.android.volley.Cache;
import com.android.volley.Network;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.BasicNetwork;
import com.android.volley.toolbox.DiskBasedCache;
import com.android.volley.toolbox.HurlStack;

public class VolleyService {

    private static VolleyService instance = null;

    public static void init(Context context) {
        if(instance==null)
            instance = new VolleyService(context.getApplicationContext());
    }

    public static VolleyService getInstance() {
        return instance;
    }

    public static RequestQueue getRequestQueue() {
        return instance.requestQueue;
    }

    private final RequestQueue requestQueue;

    private VolleyService(Context context) {
        Cache cache = new DiskBasedCache(context.getCacheDir(),1024*1024); // 1MB cap
        Network network = new BasicNetwork(new HurlStack());
        requestQueue = new RequestQueue(cache,network);
        requestQueue.start();
    }
}
