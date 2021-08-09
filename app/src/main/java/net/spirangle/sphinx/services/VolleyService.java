package net.spirangle.sphinx.services;

import android.content.Context;

import com.android.volley.Cache;
import com.android.volley.Network;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.BasicNetwork;
import com.android.volley.toolbox.DiskBasedCache;
import com.android.volley.toolbox.HurlStack;

import net.spirangle.sphinx.activities.BasicActivity;

public class VolleyService {

    private static VolleyService instance = null;

    public static void initialize(Context context) {
        if(instance==null) instance = new VolleyService();
        if(instance.requestQueue==null) {
            Context ac = context.getApplicationContext();
            Cache cache = new DiskBasedCache(ac.getCacheDir(),1024*1024); // 1MB cap
            Network network = new BasicNetwork(new HurlStack());
            instance.requestQueue = new RequestQueue(cache,network);
        }
        instance.requestQueue.start();
        if(context instanceof BasicActivity) {
            instance.setPackageSignature((BasicActivity)context);
        }
    }

    public static VolleyService getInstance() {
        if(instance==null) instance = new VolleyService();
        return instance;
    }

    private RequestQueue requestQueue;

    private String packageName;
    private String signatureSHA1;

    private VolleyService() {
        requestQueue = null;
        packageName = null;
        signatureSHA1 = null;
    }

    public RequestQueue getRequestQueue() {
        return requestQueue;
    }

    private void setPackageSignature(BasicActivity activity) {
        packageName = activity.getPackageName();
        signatureSHA1 = activity.getSignature();
    }

    public String getPackageName() {
        return packageName;
    }

    public String getSignatureSHA1() {
        return signatureSHA1;
    }
}
