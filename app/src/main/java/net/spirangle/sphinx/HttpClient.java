package net.spirangle.sphinx;

import static net.spirangle.sphinx.SphinxProperties.APP;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Build;
import android.util.Log;

import java.io.*;
import java.net.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;


public class HttpClient {
    private static final String TAG = "HttpClient";

    private static final int CONNECT_TIMEOUT = 15000;
    private static final int READ_TIMEOUT = 10000;

    private static final String DELETE = "DELETE";
    private static final String GET = "GET";
    private static final String HEAD = "HEAD";
    private static final String PATCH = "PATCH";
    private static final String POST = "POST";
    private static final String PUT = "PUT";

    private static HttpClient instance = null;

    public interface RequestListener {
        void result(Map<String,List<String>> headers,String data,int status,long id,Object object);
    }

    private final class Request extends AsyncTask<String,Void,String> {
        private HttpClient client;
        private RequestListener listener;
        private List<KeyValue> headers = null;
        private Map<String,List<String>> responseHeaders = null;
        private String url;
        private long id;
        private Object object;
        private String query = null;
        private int status = -1;
        private boolean write;
        private boolean read;

        private Request(HttpClient client,RequestListener listener,
                        List<KeyValue> headers,
                        String url,String query,long id,Object object,
                        boolean write,boolean read) {
            this.client = client;
            this.listener = listener;
            this.headers = headers;
            this.url = url;
            this.query = query;
            this.id = id;
            this.object = object;
            this.write = write;
            this.read = read;
        }

        @Override
        protected String doInBackground(String... params) {
            String method = params[0];
            return request(method);
        }

        @Override
        protected void onCancelled() {
            Log.d(APP,TAG+"HttpClient.Request.onCancelled()");
        }

        @Override
        protected void onCancelled(String result) {
            Log.d(APP,TAG+"HttpClient.Request.onCancelled(result: "+result+")");
        }

        @Override
        protected void onPostExecute(String result) {
            Log.d(APP,TAG+"HttpClient.Request.onPostExecute(url: "+url+")");
            if(listener!=null)
                listener.result(responseHeaders,result,status,id,object);
        }

        protected String request(String method) {
            status = -1;
            if(client.network==-1) {
                client.network = client.checkNetwork();
                if(client.network==-1) return null;
            }

            String data = null;
            disableConnectionReuseIfNecessary();
            HttpURLConnection conn = null;

            Log.d(APP,TAG+".Request.request(url: "+url+")");

            try {
                URL u = new URL(url);
                conn = (HttpURLConnection)u.openConnection();
                boolean tsl = (conn instanceof HttpsURLConnection);
                Log.d(APP,TAG+".Request.request(tsl: "+tsl+")");
                if(tsl) {
                    SSLContext sc = SSLContext.getInstance("TLS");
                    sc.init(null,null,new java.security.SecureRandom());
                    ((HttpsURLConnection)conn).setSSLSocketFactory(sc.getSocketFactory());
                }
                conn.setConnectTimeout(CONNECT_TIMEOUT);
                conn.setReadTimeout(READ_TIMEOUT);
                conn.setInstanceFollowRedirects(false);
                conn.setRequestMethod(method);
                conn.setDoInput(true);

                if(headers!=null && headers.size()>0)
                    for(KeyValue pair : headers)
                        conn.setRequestProperty(pair.key,pair.value);
//		} catch(Exception e1) {
//Log.e(APP,TAG+".request:1",e1);
//			network = -1;
//			status = -1;
//		}

//		try {
                if(write) {
                    if(query!=null) {
                        byte[] bytes = query.getBytes();
                        conn.setRequestProperty("Content-Length",Integer.toString(bytes.length));
                        conn.setDoOutput(true);
                        conn.connect();
                        OutputStream os = conn.getOutputStream();
//						BufferedWriter writer = new BufferedWriter(
//							new OutputStreamWriter(os,"UTF-8"));
//						writer.write(bytes);
//						writer.flush();
//						writer.close();
                        DataOutputStream dos = new DataOutputStream(os);
                        dos.write(bytes,0,bytes.length);
                        dos.flush();
                        dos.close();
                        os.close();
                        Log.d(APP,TAG+".Request.request(query: "+query+")");
                    }
                } else {
                    conn.setDoOutput(false);
                    conn.connect();
                }

//		} catch(Exception e2) {
//Log.e(APP,TAG+".request:2",e2);
//			network = -1;
//			status = -1;
//try {
//InputStreamReader isr = new InputStreamReader(conn.getInputStream());
//BufferedReader br = new BufferedReader(isr);
//StringBuilder sb = new StringBuilder();
//String line;
//while((line=br.readLine())!=null)
//	sb.append(line).append("\n");
//isr.close();
//br.close();
//String err = sb.toString();
//Log.d(APP,TAG+".request("+err+")");
//} catch(Exception e21) {
//Log.e(APP,TAG+".request:2.1",e21);
//}
//		}

//		try {


                status = conn.getResponseCode();
                responseHeaders = conn.getHeaderFields();
                Log.d(APP,TAG+".request(status: "+status+")");
				/*if(status==HttpURLConnection.HTTP_UNAUTHORIZED) {
				} else if(status!=HttpURLConnection.HTTP_OK) {
				} else {*/
                if(read && status!=204) {
                    InputStream is = status<400? conn.getInputStream() : conn.getErrorStream();
                    InputStreamReader isr = new InputStreamReader(is);
                    BufferedReader br = new BufferedReader(isr);
                    StringBuilder sb = new StringBuilder();
                    String line;
                    while((line = br.readLine())!=null)
                        sb.append(line).append("\n");
                    isr.close();
                    br.close();
                    data = sb.toString();
                    query = null;
                    Log.d(APP,TAG+".request(data: "+data+")");
                }
            } catch(Exception e) {
                Log.e(APP,TAG+".request",e);
                client.network = -1;
                status = -1;
            }
            if(conn!=null)
                conn.disconnect();
            return data;
        }
    }

    private static void disableConnectionReuseIfNecessary() {
        if(Build.VERSION.SDK_INT<Build.VERSION_CODES.FROYO) {
            System.setProperty("http.keepAlive","false");
        }
    }

    public static HttpClient getInstance(Context context) {
        if(instance==null) instance = new HttpClient(context.getApplicationContext());
        return instance;
    }

    private Context context;
    private CookieManager cookies;
    private List<KeyValue> headers = null;
    private List<KeyValue> params = null;
    private int network = -1;

    private HttpClient(Context context) {
        this.context = context;
        this.cookies = new CookieManager();
        CookieHandler.setDefault(cookies);
    }

    public int checkNetwork() {
        long t1 = System.currentTimeMillis();
        ConnectivityManager cm = (ConnectivityManager)context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo ni = cm.getActiveNetworkInfo();
        if(ni==null || !ni.isConnected()) return -1;
        long t2 = System.currentTimeMillis();
        Log.d(APP,TAG+".checkNetwork("+(t2-t1)+")");
        return ni.getType();
    }

    public HttpClient authorization(String type) { return header("Authorization",type); }

    public HttpClient authorizationGoogle(String token) { return authorization("Google "+token); }

    public HttpClient contentType(String type) { return header("Content-Type",type); }

    public HttpClient contentTypeText() { return contentType("text/plain; charset=utf-8"); }

    public HttpClient contentTypeHTML() { return contentType("text/html; charset=utf-8"); }

    public HttpClient contentTypeJSON() { return contentType("application/json; charset=utf-8"); }

    public HttpClient header(String key,String value) {
        if(headers==null) headers = new ArrayList<KeyValue>();
        headers.add(new KeyValue(key,value));
        return this;
    }

    public HttpClient add(String key,String value) {
        if(params==null) params = new ArrayList<KeyValue>();
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

    private void request(String url,String query,long id,Object object,
                         RequestListener listener,String method,
                         boolean write,boolean read) {
        query = getQuery(query);
        new Request(this,listener,headers,url,query,id,object,write,read).execute(method);
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

