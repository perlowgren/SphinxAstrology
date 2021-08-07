package net.spirangle.sphinx.net;

import static net.spirangle.sphinx.config.SphinxProperties.APP;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Build;
import android.util.Log;

import java.io.UnsupportedEncodingException;
import java.net.CookieHandler;
import java.net.CookieManager;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;


public class HttpClient {
    private static final String TAG = HttpClient.class.getSimpleName();

    public static class KeyValue {
        public final String key;
        public final String value;

        public KeyValue(String k,String v) {
            key = k;
            value = v;
        }
    }

    static final int CONNECT_TIMEOUT = 15000;
    static final int READ_TIMEOUT = 10000;

    static final String DELETE = "DELETE";
    static final String GET = "GET";
    static final String HEAD = "HEAD";
    static final String PATCH = "PATCH";
    static final String POST = "POST";
    static final String PUT = "PUT";

    static void disableConnectionReuseIfNecessary() {
        if(Build.VERSION.SDK_INT<Build.VERSION_CODES.FROYO) {
            System.setProperty("http.keepAlive","false");
        }
    }

    public static HttpClient getInstance(Context context) {
        return new HttpClient(context.getApplicationContext());
    }

    private final Context context;
    private CookieManager cookies;
    private List<KeyValue> headers;
    private List<KeyValue> params;
    int network = -1;

    private HttpClient(Context context) {
        this.context = context;
        this.cookies = new CookieManager();
        this.headers = null;
        this.params = null;
        clearNetwork();
        CookieHandler.setDefault(cookies);
    }

    void clearNetwork() {
        network = -1;
    }

    boolean updateNetwork() {
        if(network==-1) {
            long t1 = System.currentTimeMillis();
            ConnectivityManager cm = (ConnectivityManager)context.getSystemService(Context.CONNECTIVITY_SERVICE);
            NetworkInfo ni = cm.getActiveNetworkInfo();
            if(ni!=null && ni.isConnected()) {
                long t2 = System.currentTimeMillis();
                Log.d(APP,TAG+".updateNetwork("+(t2-t1)+")");
                network = ni.getType();
            }
        }
        return network!=-1;
    }

    public HttpClient authorization(String type) { return header("Authorization",type); }

    public HttpClient authorizationGoogle(String token) { return authorization("Google "+token); }

    public HttpClient contentType(String type) { return header("Content-Type",type); }

    public HttpClient contentTypeText() { return contentType("text/plain; charset=utf-8"); }

    public HttpClient contentTypeHTML() { return contentType("text/html; charset=utf-8"); }

    public HttpClient contentTypeJSON() { return contentType("application/json; charset=utf-8"); }

    public HttpClient header(String key,String value) {
        if(headers==null) headers = new ArrayList<>();
        headers.add(new KeyValue(key,value));
        return this;
    }

    public HttpClient add(String key,String value) {
        if(params==null) params = new ArrayList<>();
        params.add(new KeyValue(key,value));
        return this;
    }

    public void get(String url,RequestListener listener) { get(url,0l,null,listener); }

    public void get(String url,long id,Object object,RequestListener listener) {
        request(url,null,id,object,listener,GET,false,true);
    }

    public void post(String url,RequestListener listener) { post(url,null,0l,null,listener); }

    public void post(String url,String query,RequestListener listener) { post(url,query,0l,null,listener); }

    public void post(String url,long id,Object object,RequestListener listener) { post(url,null,id,object,listener); }

    public void post(String url,String query,long id,Object object,RequestListener listener) {
        request(url,query,id,object,listener,POST,true,true);
    }

    public void put(String url,RequestListener listener) { put(url,null,0l,null,listener); }

    public void put(String url,String query,RequestListener listener) { put(url,query,0l,null,listener); }

    public void put(String url,long id,Object object,RequestListener listener) { put(url,null,id,object,listener); }

    public void put(String url,String query,long id,Object object,RequestListener listener) {
        request(url,query,id,object,listener,PUT,true,true);
    }

    public void patch(String url,RequestListener listener) { patch(url,null,0l,null,listener); }

    public void patch(String url,String query,RequestListener listener) { patch(url,query,0l,null,listener); }

    public void patch(String url,long id,Object object,RequestListener listener) { patch(url,null,id,object,listener); }

    public void patch(String url,String query,long id,Object object,RequestListener listener) {
        request(url,query,id,object,listener,PATCH,true,true);
    }

    public void delete(String url,RequestListener listener) { delete(url,0l,null,listener); }

    public void delete(String url,long id,Object object,RequestListener listener) {
        request(url,null,id,object,listener,DELETE,false,true);
    }

    private void request(String url,String query,long id,Object object,RequestListener listener,
                         String method,boolean write,boolean read) {
        query = getQuery(query);
        new HttpRequest(this,listener,headers,url,query,id,object,write,read).execute(method);
        headers = null;
        params = null;
    }

    private String getQuery(String query) {
        if(query==null) {
            if(params!=null && params.size()>0) {
                StringBuilder result = new StringBuilder();
                boolean first = true;
                String key, value;
                for(KeyValue pair : params) {
                    try {
                        key = URLEncoder.encode(pair.key,"UTF-8");
                        value = URLEncoder.encode(pair.value,"UTF-8");
                        if(first) first = false;
                        else result.append("&");
                        result.append(key).append("=").append(value);
                    } catch(UnsupportedEncodingException e) {}
                }
                query = result.toString();
            } else {
                query = "";
            }
        }
        return query;
    }
}

