package net.spirangle.sphinx.net;

import static net.spirangle.sphinx.config.SphinxProperties.APP;

import android.os.AsyncTask;
import android.util.Log;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.List;
import java.util.Map;

import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;

final class HttpRequest extends AsyncTask<String,Void,String> {
    private static final String TAG = HttpRequest.class.getSimpleName();

    private final HttpClient client;
    private final RequestListener listener;
    private final List<HttpClient.KeyValue> headers;
    private Map<String,List<String>> responseHeaders;
    private final String url;
    private final long id;
    private final Object object;
    private String query;
    private int status;
    private final boolean write;
    private final boolean read;

    HttpRequest(HttpClient client,RequestListener listener,List<HttpClient.KeyValue> headers,
                String url,String query,long id,Object object,boolean write,boolean read) {
        this.client = client;
        this.listener = listener;
        this.headers = headers;
        this.responseHeaders = null;
        this.url = url;
        this.query = query;
        this.id = id;
        this.object = object;
        this.status = -1;
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
        Log.d(APP,TAG+".onCancelled()");
    }

    @Override
    protected void onCancelled(String result) {
        Log.d(APP,TAG+".onCancelled(result: "+result+")");
    }

    @Override
    protected void onPostExecute(String result) {
        Log.d(APP,TAG+".onPostExecute(url: "+url+")");
        if(listener!=null)
            listener.result(responseHeaders,result,status,id,object);
    }

    protected String request(String method) {
        status = -1;
        if(!client.updateNetwork()) return null;

        String data = null;
        HttpClient.disableConnectionReuseIfNecessary();
        HttpURLConnection conn = null;

        Log.d(APP,TAG+".request(url: "+url+")");

        try {
            URL u = new URL(url);
            conn = (HttpURLConnection)u.openConnection();
            boolean tsl = (conn instanceof HttpsURLConnection);
            Log.d(APP,TAG+".request(tsl: "+tsl+")");
            if(tsl) {
                SSLContext sc = SSLContext.getInstance("TLS");
                sc.init(null,null,new java.security.SecureRandom());
                ((HttpsURLConnection)conn).setSSLSocketFactory(sc.getSocketFactory());
            }
            conn.setConnectTimeout(HttpClient.CONNECT_TIMEOUT);
            conn.setReadTimeout(HttpClient.READ_TIMEOUT);
            conn.setInstanceFollowRedirects(false);
            conn.setRequestMethod(method);
            conn.setDoInput(true);

            if(headers!=null && headers.size()>0)
                for(HttpClient.KeyValue pair : headers)
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
            client.clearNetwork();
            status = -1;
        }
        if(conn!=null) conn.disconnect();
        return data;
    }
}
