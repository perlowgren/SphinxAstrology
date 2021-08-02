package net.spirangle.sphinx;

import static net.spirangle.sphinx.SphinxProperties.*;

import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;

import androidx.core.content.ContextCompat;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;


public class LocationService implements LocationListener {
    private static final String TAG = "LocationService";

    public static final String GOOGLE_MAPS_API_TIME_ZONE_URL = "https://maps.googleapis.com/maps/api/timezone/json";
    public static final String GOOGLE_MAPS_API_GEOCODE_URL = "https://maps.googleapis.com/maps/api/geocode/json";

    //The minimum distance to change updates in meters
    private static final long MIN_DISTANCE_CHANGE_FOR_UPDATES = 0; // 10 meters
    //The minimum time beetwen updates in milliseconds
    private static final long MIN_TIME_BW_UPDATES = 1000*60*1; // 1 minute

    private final static boolean forceNetwork = false;

    private static LocationService instance = null;

    public static class TimeZone {
        public int dstOffset = 0;
        public int rawOffset = 0;
        public String timeZoneId = null;
        public String timeZoneName = null;
    }

    ;

    public interface AddressReceiver {
        void receive(List<Address> addresses,int status);
    }

    public interface TimeZoneReceiver {
        void receive(TimeZone timeZone,int status);
    }

    private Context context;
    private LocationManager locationManager;
    private boolean isGPSEnabled;
    private boolean isNetworkEnabled;
    public Location location;

    /**
     * Singleton implementation
     */
    public static LocationService getInstance(Context context) {
        if(instance==null) instance = new LocationService(context.getApplicationContext());
        return instance;
    }

    /**
     * Local constructor
     */
    private LocationService(Context context) {
        Log.d(APP,TAG+"()");
        this.context = context;
        locationManager = null;
        location = null;
        isGPSEnabled = false;
        isNetworkEnabled = false;
        initLocationService();
    }

    /**
     * Sets up location service after permissions is granted
     */
    private void initLocationService() {
        if(Build.VERSION.SDK_INT>=Build.VERSION_CODES.M &&
           ContextCompat.checkSelfPermission(context,android.Manifest.permission.ACCESS_FINE_LOCATION)!=PackageManager.PERMISSION_GRANTED &&
           ContextCompat.checkSelfPermission(context,android.Manifest.permission.ACCESS_COARSE_LOCATION)!=PackageManager.PERMISSION_GRANTED)
            return;

        try {
            locationManager = (LocationManager)context.getSystemService(Context.LOCATION_SERVICE);
            if(locationManager!=null) {
                isNetworkEnabled = locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);
                if(isNetworkEnabled) {
                    locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER,
                                                           MIN_TIME_BW_UPDATES,MIN_DISTANCE_CHANGE_FOR_UPDATES,this);
                    location = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
                } else if(!forceNetwork) {
                    isGPSEnabled = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
                    if(isGPSEnabled) {
                        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER,
                                                               MIN_TIME_BW_UPDATES,MIN_DISTANCE_CHANGE_FOR_UPDATES,this);
                        location = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
                    }
                }
            }
        } catch(Exception e) {
            Log.e(APP,TAG+".initLocationService",e);
        }
    }

    @Override
    public void onLocationChanged(Location location) {
        this.location = location;
    }

    @Override
    public void onProviderDisabled(String provider) {
    }

    @Override
    public void onProviderEnabled(String provider) {
    }

    @Override
    public void onStatusChanged(String provider,int status,Bundle extras) {
    }

    public void getFromLocation(double lon,double lat,int max,Locale locale,
                                AddressReceiver receiver) {
        if(receiver==null) return;
        if(locale==null) locale = Locale.getDefault();
        String language = locale.getLanguage();
        String url = GOOGLE_MAPS_API_GEOCODE_URL+"?latlng="+lat+","+lon+"&language="+language+"&key="+GOOGLE_MAPS_API_KEY;
        getFromLocation(url,max,locale,receiver);
    }

    public void getFromLocationName(String address,int max,Locale locale,
                                    AddressReceiver receiver) {
        if(receiver==null) return;
        if(locale==null) locale = Locale.getDefault();
        try {
            address = URLEncoder.encode(address,"UTF-8");
        } catch(UnsupportedEncodingException e) {
            Log.e(APP,TAG+".getFromLocationName",e);
            return;
        }
        String language = locale.getLanguage();
        String url = GOOGLE_MAPS_API_GEOCODE_URL+"?address="+address+"&language="+language+"&key="+GOOGLE_MAPS_API_KEY;
        getFromLocation(url,max,locale,receiver);
    }

    private void getFromLocation(final String url,final int max,final Locale locale,
                                 final AddressReceiver receiver) {
        Log.d(APP,TAG+".getFromLocation("+url+")");
        HttpClient http = HttpClient.getInstance(context);
        http.get(url,(headers,data,status,id,object) -> {
            List<Address> addresses = parseLocationData(data,max,locale);
            Log.d(APP,TAG+".getFromLocation("+url+", receive...)");
            receiver.receive(addresses,status);
        });
    }

    public void getTimeZone(double lon,double lat,long timestamp,Locale locale,
                            TimeZoneReceiver receiver) {
        if(receiver==null) return;
        if(locale==null) locale = Locale.getDefault();
        String language = locale.getLanguage();
        String url = GOOGLE_MAPS_API_TIME_ZONE_URL+"?location="+lat+","+lon+"&timestamp="+timestamp+"&language="+language+"&key="+GOOGLE_MAPS_API_KEY;
        getTimeZone(url,receiver);
    }

    private void getTimeZone(final String url,final TimeZoneReceiver receiver) {
        Log.d(APP,TAG+".getTimeZone("+url+")");
        HttpClient http = HttpClient.getInstance(context);
        http.get(url,(headers,data,status,id,object) -> {
            TimeZone tz = null;
//Log.d(APP,TAG+".getTimeZone(data: "+data+")");
            try {
                JSONObject json = new JSONObject(data);
                tz = new TimeZone();
                tz.dstOffset = json.optInt("dstOffset",0);
                tz.rawOffset = json.optInt("rawOffset",0);
                tz.timeZoneId = json.optString("timeZoneId",GMT);
                tz.timeZoneName = json.optString("timeZoneName",GMT);
            } catch(Exception e) {
                Log.e(APP,TAG+".getTimeZone",e);
            }
            receiver.receive(tz,status);
        });
    }

    private List<Address> parseLocationData(String data,int max,Locale locale) {
        if(data!=null) {
            try {
//Log.d(APP,TAG+".parseLocationData("+data+")");
                JSONObject json = new JSONObject(data);
                if(!json.optString("status").equals("OK")) return null;
                List<Address> addresses = new ArrayList<>();
                JSONArray results, components, types;
                JSONObject obj, obj2, geometry, location;
                Address addr;
                String type;
                results = (JSONArray)json.get("results");
                for(int i = 0; i<results.length() && i<max; ++i) {
                    addr = new Address(locale);
                    obj = results.getJSONObject(i);
                    components = obj.getJSONArray("address_components");
                    for(int j = 0; j<components.length(); ++j) {
                        obj2 = components.getJSONObject(j);
                        types = obj2.getJSONArray("types");
                        type = types.getString(0);
                        if(type.equals("country")) {
                            addr.setCountryName(obj2.getString("long_name"));
                            addr.setCountryCode(obj2.getString("short_name"));
                        } else if(type.equals("locality")) {
                            addr.setLocality(obj2.getString("long_name"));
                        }
                    }
                    geometry = obj.getJSONObject("geometry");
                    location = geometry.getJSONObject("location");
                    addr.setLongitude(location.getDouble("lng"));
                    addr.setLatitude(location.getDouble("lat"));
                    addresses.add(addr);
                }
                return addresses;
            } catch(Exception e) {
                Log.e(APP,TAG+".parseLocationData",e);
            }
        }
        return null;
    }
}

