package net.spirangle.sphinx.services;

import static android.Manifest.permission.ACCESS_COARSE_LOCATION;
import static android.Manifest.permission.ACCESS_FINE_LOCATION;
import static android.content.Context.LOCATION_SERVICE;
import static android.location.LocationManager.GPS_PROVIDER;
import static android.location.LocationManager.NETWORK_PROVIDER;
import static net.spirangle.sphinx.config.SphinxProperties.*;

import android.app.Activity;
import android.content.Context;
import android.location.Address;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.util.Log;

import com.android.volley.RequestQueue;
import com.android.volley.toolbox.JsonObjectRequest;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;


public class LocationService implements LocationListener {
    private static final String TAG = LocationService.class.getSimpleName();

    public static final String GOOGLE_MAPS_API_TIME_ZONE_URL = "https://maps.googleapis.com/maps/api/timezone/json";
    public static final String GOOGLE_MAPS_API_GEOCODE_URL = "https://maps.googleapis.com/maps/api/geocode/json";

    //The minimum distance to change updates in meters
    private static final long MIN_DISTANCE_CHANGE_FOR_UPDATES = 0; // 10 meters
    //The minimum time beetwen updates in milliseconds
    private static final long MIN_TIME_BW_UPDATES = 60*1000; // 1 minute

    private final static boolean forceNetwork = false;

    private static LocationService instance = null;

    public static class TimeZone {
        public int dstOffset = 0;
        public int rawOffset = 0;
        public String timeZoneId = null;
        public String timeZoneName = null;
    }

    public interface AddressReceiver {
        void receive(List<Address> addresses);
    }

    public interface TimeZoneReceiver {
        void receive(TimeZone timeZone);
    }

    private LocationManager locationManager;
    private boolean isGPSEnabled;
    private boolean isNetworkEnabled;

    private Location location;

    /**
     * Singleton implementation
     */
    public static LocationService getInstance() {
        if(instance==null) instance = new LocationService();
        return instance;
    }

    /**
     * Local constructor
     */
    private LocationService() {
        Log.d(APP,TAG+"()");
        locationManager = null;
        location = null;
        isGPSEnabled = false;
        isNetworkEnabled = false;
    }

    /**
     * Sets up location service after permissions is granted
     */
    @SuppressWarnings("MissingPermission")
    public void requestLocationUpdates(Activity activity) {
        if(locationManager!=null) return;
        if(!PermissionService.hasAnyPermission(activity,ACCESS_FINE_LOCATION,ACCESS_COARSE_LOCATION)) return;
        try {
            Context context = activity.getApplicationContext();
            locationManager = (LocationManager)context.getSystemService(LOCATION_SERVICE);
            if(locationManager!=null) {
                isNetworkEnabled = locationManager.isProviderEnabled(NETWORK_PROVIDER);
                if(isNetworkEnabled) {
                    locationManager.requestLocationUpdates(NETWORK_PROVIDER,
                                                           MIN_TIME_BW_UPDATES,
                                                           MIN_DISTANCE_CHANGE_FOR_UPDATES,this);
                    location = locationManager.getLastKnownLocation(NETWORK_PROVIDER);
                } else if(!forceNetwork) {
                    isGPSEnabled = locationManager.isProviderEnabled(GPS_PROVIDER);
                    if(isGPSEnabled) {
                        locationManager.requestLocationUpdates(GPS_PROVIDER,
                                                               MIN_TIME_BW_UPDATES,
                                                               MIN_DISTANCE_CHANGE_FOR_UPDATES,this);
                        location = locationManager.getLastKnownLocation(GPS_PROVIDER);
                    }
                }
            }
        } catch(Exception e) {
            Log.e(APP,TAG+".requestLocationUpdates",e);
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

    public Location getLocation() {
        return location;
    }

    public void getFromLocation(double lon,double lat,int max,Locale locale,AddressReceiver receiver) {
        if(receiver==null) return;
        if(locale==null) locale = Locale.getDefault();
        String language = locale.getLanguage();
        String url = GOOGLE_MAPS_API_GEOCODE_URL+"?latlng="+lat+","+lon+"&language="+language+"&key="+GOOGLE_MAPS_API_KEY;
        getFromLocation(url,max,locale,receiver);
    }

    public void getFromLocationName(String address,int max,Locale locale,AddressReceiver receiver) {
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

    private void getFromLocation(String url,int max,Locale locale,AddressReceiver receiver) {
        Log.d(APP,TAG+".getFromLocation("+url+")");
        RequestQueue requestQueue = VolleyService.getRequestQueue();
        requestQueue.add(new JsonObjectRequest(url,null,response -> {
            Log.d(APP,TAG+".getFromLocation(url: "+url+", response: "+response+")");
            List<Address> addresses = parseLocationData(response,max,locale);
            receiver.receive(addresses);
        },error -> {
            Log.e(APP,TAG+".getFromLocation(url: "+url+")",error);
        }));
    }

    public void getTimeZone(double lon,double lat,long timestamp,Locale locale,TimeZoneReceiver receiver) {
        if(receiver==null) return;
        if(locale==null) locale = Locale.getDefault();
        String language = locale.getLanguage();
        String url = GOOGLE_MAPS_API_TIME_ZONE_URL+"?location="+lat+","+lon+"&timestamp="+timestamp+"&language="+language+"&key="+GOOGLE_MAPS_API_KEY;
        getTimeZone(url,receiver);
    }

    private void getTimeZone(final String url,final TimeZoneReceiver receiver) {
        Log.d(APP,TAG+".getTimeZone("+url+")");
        RequestQueue requestQueue = VolleyService.getRequestQueue();
        requestQueue.add(new JsonObjectRequest(url,null,response -> {
//Log.d(APP,TAG+".getTimeZone(data: "+data+")");
            try {
                TimeZone tz = new TimeZone();
                tz.dstOffset = response.optInt("dstOffset",0);
                tz.rawOffset = response.optInt("rawOffset",0);
                tz.timeZoneId = response.optString("timeZoneId",GMT);
                tz.timeZoneName = response.optString("timeZoneName",GMT);
                receiver.receive(tz);
            } catch(Exception e) {
                Log.e(APP,TAG+".getTimeZone(url: "+url+")",e);
            }
        },error -> {
            Log.e(APP,TAG+".getTimeZone(url: "+url+")",error);
        }));
    }

    private List<Address> parseLocationData(JSONObject json,int max,Locale locale) {
        if(json==null) return null;
        try {
//Log.d(APP,TAG+".parseLocationData("+data+")");
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
        return null;
    }
}

