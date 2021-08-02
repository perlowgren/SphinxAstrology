package net.spirangle.sphinx.astro;

import static net.spirangle.sphinx.SphinxProperties.*;

import android.content.ContentValues;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.location.Address;
import android.util.Log;

import net.spirangle.sphinx.*;
import net.spirangle.sphinx.HttpClient.RequestListener;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


public class AstroDB extends Database implements RequestListener {
    private static final String TAG = "AstroDB";

    private static final KeyValue[] astroDBFiles = {
        new KeyValue("database","AstroDB.sql"),
        new KeyValue("data","AstroDB-data.sql"),
        new KeyValue("profiles","AstroDB-profiles.sql"),
        new KeyValue("texts","AstroDB-texts.sql"),
    };

    //	private static final Pattern profileTime = Pattern.compile("^(BCE)? *(\\d{1,4})-(\\d{1,2})-(\\d{1,2}) *(?:(\\d{1,2}):(\\d{2})(?::(\\d{2}))?)? *([GJ])?$",Pattern.CASE_INSENSITIVE);
    private static final Pattern profileTime = Pattern.compile("^(?:BCE )?(\\d{1,4})-(\\d{1,2})-(\\d{1,2}) (\\d{1,2}):(\\d{2}):(\\d{2})(?: [JG])?$");

    public static final class TableProfile extends Table {
        public static final String table = "Profile";
        public static final String userId = "userId";
        public static final String profileKey = "profileKey";
        public static final String cat1 = "cat1";
        public static final String cat2 = "cat2";
        public static final String name = "name";
        public static final String year = "year";
        public static final String month = "month";
        public static final String day = "day";
        public static final String hour = "hour";
        public static final String minute = "minute";
        public static final String second = "second";
        public static final String longitude = "longitude";
        public static final String latitude = "latitude";
        public static final String timeZone = "timeZone";
        public static final String dst = "dst";
        public static final String sun = "sun";
        public static final String moon = "moon";
        public static final String ascendant = "ascendant";
        public static final String picture = "picture";
        public static final String updated = "updated";
    }

    public static final class TableLocation extends Table {
        public static final String table = "Location";
        public static final String name = "name";
        public static final String locality = "locality";
        public static final String country = "country";
        public static final String countryCode = "countryCode";
        public static final String longitude = "longitude";
        public static final String latitude = "latitude";
        public static final String language = "language";
    }

    public static final class TableTimeZone extends Table {
        public static final String table = "TimeZone";
        public static final String name = "name";
        public static final String longitude = "longitude";
        public static final String latitude = "latitude";
        public static final String timestamp = "timestamp";
        public static final String offset = "offset";
        public static final String dst = "dst";
        public static final String language = "language";
    }

    public static final class TableText extends Table {
        public static final String table = "Text";
        public static final String userId = "userId";
        public static final String textKey = "textKey";
        public static final String type = "type";
        public static final String profileKey = "profileKey";
        public static final String symbol = "symbol";
        public static final String title = "title";
        public static final String html = "html";
        public static final String text = "text";
        public static final String writer = "writer";
        public static final String votes = "votes";
        public static final String rates = "rates";
        public static final String language = "language";
        public static final String updated = "updated";

        public static final String[] types = {null,"profile","symbol"};
    }

    ;

    public static synchronized AstroDB getInstance(Context context,DatabaseListener listener) {
        if(instance==null)
            instance = new AstroDB(context.getApplicationContext(),listener,DB_ASTRO,DB_ASTRO_VERSION);
        return (AstroDB)instance;
    }

    public static synchronized AstroDB getInstance() {
        return (AstroDB)instance;
    }

    protected AstroDB(Context context,DatabaseListener listener,String name,int version) {
        super(context,listener,name,version);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        progress = 0.0f;
        importSQL(db,astroDBFiles);
    }

    @Override
    public void result(Map<String,List<String>> headers,String data,int status,long id,Object object) {
        if(status>=200 && status<=299 && data!=null && data.length()>0) {
            try {
                JSONObject json = new JSONObject(data);
                String s = json.optString("status");
                if(s.equals("OK")) {
                    int count = Integer.parseInt(headers.get("X-Total-Count").get(0));
                    int from = json.optInt("from");
                    int offset = json.optInt("offset");
                    JSONArray arr;
                    arr = (JSONArray)json.opt("profiles");
                    if(arr!=null) {
                        for(int i = 0; i<arr.length(); ++i)
                            insertProfile(arr.getJSONObject(i));
                        if(count>offset+arr.length())
                            downloadProfiles(from,offset,0);
                    }
                    arr = (JSONArray)json.opt("texts");
                    if(arr!=null) {
                        for(int i = 0; i<arr.length(); ++i)
                            insertText(arr.getJSONObject(i));
                        if(count>offset+arr.length())
                            downloadTexts(from,offset,0);
                    }
                    updateDatabase(0,0);
                }
            } catch(Exception e) {
                Log.e(APP,TAG+".spirangleSignIn",e);
            }
        }
//		shortToast(R.string.toast_signin_failed);
    }

    public void downloadProfiles(long from,int offset,int limit) {
        Key key = BasicActivity.user.key;
        String tokenId = BasicActivity.google.tokenId;
        if(key==null || tokenId==null) return;
        HttpClient http = HttpClient.getInstance(context);
        http.authorizationGoogle(tokenId).contentTypeJSON()
            .get(URL_SPIRANGLE_API+"/users/"+key+"/profiles?from="+from+"&offset="+offset+"&limit="+limit,this);
    }

    public void downloadTexts(long from,int offset,int limit) {
        Key key = BasicActivity.user.key;
        String tokenId = BasicActivity.google.tokenId;
        if(key==null || tokenId==null) return;
        HttpClient http = HttpClient.getInstance(context);
        http.authorizationGoogle(tokenId).contentTypeJSON()
            .get(URL_SPIRANGLE_API+"/users/"+key+"/texts?from="+from+"&offset="+offset+"&limit="+limit,this);
    }

    public long insertProfile(long uid,long c1,long c2,Horoscope h) {
        Key pkey = h.getProfileKey();
        int tm = timestamp();
        ContentValues v = new ContentValues();
        h.putContentValues(v);
        v.put(TableProfile.userId,uid);
        v.put(TableProfile.profileKey,pkey==null? -1l : pkey.id);
        v.put(TableProfile.cat1,c1);
        v.put(TableProfile.cat2,c2);
        v.put(TableProfile.updated,tm);
        v.put(TableProfile.created,tm);
        long id = insert(TableProfile.table,v);
        h.setId(id);
        return id;
    }

    public long insertProfile(JSONObject json) {
        String cats = json.optString("categories");
        String time = json.optString("time","1970-01-01 00:00:00");
        Log.d(APP,TAG+".insertProfile(time: "+time+")");
        Matcher m = profileTime.matcher(time);
        m.matches();
        ContentValues v = new ContentValues();
        v.put(TableProfile.userId,BasicActivity.user.id);
        v.put(TableProfile.profileKey,new Key(json.optString("key")).id);
        v.put(TableProfile.cat1,0l);
        v.put(TableProfile.cat2,0l);
        v.put(TableProfile.name,json.optString("name"));
        v.put(TableProfile.year,Integer.parseInt(m.group(1)));
        v.put(TableProfile.month,Integer.parseInt(m.group(2)));
        v.put(TableProfile.day,Integer.parseInt(m.group(3)));
        v.put(TableProfile.hour,Integer.parseInt(m.group(4)));
        v.put(TableProfile.minute,Integer.parseInt(m.group(5)));
        v.put(TableProfile.second,Integer.parseInt(m.group(6)));
        v.put(TableProfile.longitude,json.optInt("longitude"));
        v.put(TableProfile.latitude,json.optInt("latitude"));
        v.put(TableProfile.timeZone,json.optInt("timeZone"));
        v.put(TableProfile.dst,json.optInt("dst"));
        v.put(TableProfile.sun,json.optInt("sun"));
        v.put(TableProfile.moon,json.optInt("moon"));
        v.put(TableProfile.ascendant,json.optInt("ascendant"));
        v.put(TableProfile.flags,json.optInt("flags"));
        v.put(TableProfile.updated,json.optLong("updated"));
        v.put(TableProfile.created,json.optLong("created"));
        return insert(TableProfile.table,v);
    }

    public boolean updateProfile(long c1,long c2,Horoscope h) {
        int tm = timestamp();
        ContentValues v = new ContentValues();
        h.putContentValues(v);
        v.put(TableProfile.cat1,c1);
        v.put(TableProfile.cat2,c2);
        v.put(TableProfile.updated,tm);
        return update(TableProfile.table,v,TableProfile.id+"="+h.getId());
    }

    public long insertLocation(String name,Address addr,String lang,int fl) {
        if(addr==null) return -1;
        if(name==null) name = addr.getLocality();
        return insertLocation(
            name,
            addr.getLocality(),
            addr.getCountryName(),
            addr.getCountryCode(),
            addr.getLongitude(),
            addr.getLatitude(),
            lang,0
                             );
    }

    public long insertLocation(String name,Address addr,double lon,double lat,String lang,int fl) {
        if(addr==null) return -1;
        if(name==null) name = addr.getLocality();
        return insertLocation(
            name,
            addr.getLocality(),
            addr.getCountryName(),
            addr.getCountryCode(),
            lon,lat,lang,0
                             );
    }

    public long insertLocation(String name,String ln,String cn,String cc,double lon,double lat,String lang,int fl) {
        int tm = timestamp();
        ContentValues v = new ContentValues();
        v.put(TableLocation.name,name);
        v.put(TableLocation.locality,ln);
        v.put(TableLocation.country,cn);
        v.put(TableLocation.countryCode,cc);
        v.put(TableLocation.longitude,(int)Math.round(lon*1000000.0));
        v.put(TableLocation.latitude,(int)Math.round(lat*1000000.0));
        v.put(TableLocation.language,lang);
        v.put(TableLocation.flags,fl);
        v.put(TableLocation.created,tm);
        return insert(TableLocation.table,v);
    }

    public long insertTimeZone(String name,double lon,double lat,long time,double offset,double dst,String lang,int fl) {
        int tm = timestamp();
        ContentValues v = new ContentValues();
        v.put(TableTimeZone.name,name);
        v.put(TableTimeZone.longitude,(int)Math.round(lon*1000000.0));
        v.put(TableTimeZone.latitude,(int)Math.round(lat*1000000.0));
        v.put(TableTimeZone.timestamp,time);
        v.put(TableTimeZone.offset,(int)(offset*3600.0));
        v.put(TableTimeZone.dst,(int)(dst*3600.0));
        v.put(TableTimeZone.language,lang);
        v.put(TableTimeZone.flags,fl);
        v.put(TableTimeZone.created,tm);
        return insert(TableTimeZone.table,v);
    }

    public boolean updateText(long id,String title,String html,String txt,String wr,int fl) {
        int tm = timestamp();
        ContentValues v = new ContentValues();
        if(title!=null) v.put(TableText.title,title);
        if(html!=null) v.put(TableText.html,html);
        if(txt!=null) v.put(TableText.text,txt);
        if(wr!=null) v.put(TableText.writer,wr);
        if(fl!=-1) v.put(TableText.flags,fl);
        v.put(TableText.updated,tm);
        return update(TableText.table,v,TableText.id+"="+id);
    }

    public long insertText(long uid,Key tkey,int t,Key pkey,long sym,String title,String html,String txt,String wr,String lang,int fl) {
        if(tkey==null) tkey = new Key(Key.TEXT);
        int tm = timestamp();
        ContentValues v = new ContentValues();
        v.put(TableText.userId,uid);
        v.put(TableText.textKey,tkey.id);
        v.put(TableText.type,t);
        v.put(TableText.profileKey,pkey==null? 0 : pkey.id);
        v.put(TableText.symbol,sym);
        v.put(TableText.title,title);
        v.put(TableText.html,html);
        v.put(TableText.text,txt);
        v.put(TableText.writer,wr);
        v.put(TableText.votes,0);
        v.put(TableText.rates,0);
        v.put(TableText.language,lang);
        v.put(TableText.flags,fl);
        v.put(TableText.updated,tm);
        v.put(TableText.created,tm);
        return insert(TableText.table,v);
    }

    public long insertText(JSONObject json) {
        String type = json.optString("type");
        int t = 0;
        ContentValues v = new ContentValues();
        v.put(TableText.userId,BasicActivity.user.id);
        v.put(TableText.textKey,Base36.decode(json.optString("key")));
        if(type.equals(TableText.types[TEXT_TYPE_PROFILE])) {
            t = TEXT_TYPE_PROFILE;
            Key pkey = new Key(json.optString(TableText.types[t]));
            v.put(TableText.profileKey,pkey.id);
        } else if(type.equals(TableText.types[TEXT_TYPE_SYMBOL])) {
            t = TEXT_TYPE_SYMBOL;
            v.put(TableText.symbol,Base36.decode(json.optString(TableText.types[t])));
        }
        v.put(TableText.type,t);
        v.put(TableText.title,json.optString("title"));
        v.put(TableText.text,json.optString("text"));
        v.put(TableText.writer,json.optString("writer"));
        v.put(TableText.votes,json.optInt("votes"));
        v.put(TableText.rates,json.optInt("rates"));
        v.put(TableText.language,json.optString("language"));
        v.put(TableText.flags,json.optInt("flags"));
        v.put(TableText.updated,json.optLong("updated"));
        v.put(TableText.created,json.optLong("created"));
        return insert(TableText.table,v);
    }
}

