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
import java.util.*;


public class LocationService implements LocationListener {
    private static final String TAG = LocationService.class.getSimpleName();

    public static final String GOOGLE_MAPS_API_TIME_ZONE_URL = "https://maps.googleapis.com/maps/api/timezone/json";
    public static final String GOOGLE_MAPS_API_GEOCODE_URL = "https://maps.googleapis.com/maps/api/geocode/json";

    //The minimum distance to change updates in meters
    private static final long MIN_DISTANCE_CHANGE_FOR_UPDATES = 0; // 10 meters
    //The minimum time beetwen updates in milliseconds
    private static final long MIN_TIME_BW_UPDATES = 60*1000; // 1 minute

    private static final boolean forceNetwork = false;

    private static final String[] locationServicePermissions = {
        ACCESS_FINE_LOCATION,
        ACCESS_COARSE_LOCATION
    };

    private static LocationService instance = null;

    public static class TimeZone {
        public int dstOffset = 0;
        public int rawOffset = 0;
        public String timeZoneId = null;
        public String timeZoneName = null;
    }

    public interface LocationServiceCallback<T> {
        void onLocationUpdate(T location);
    }

    private LocationManager locationManager;
    private String locationProvider;
    private Location location;
    private Set<LocationServiceCallback<Location>> locationUpdateListeners;

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
        locationProvider = null;
        location = null;
        locationUpdateListeners = new HashSet<>();
    }

    public void stopLocationUpdates(LocationServiceCallback<Location> listener) {
        locationUpdateListeners.remove(listener);
    }

    /**
     * Sets up location service after permissions is granted
     */
    public boolean requestLocationPermissions(Activity activity) {
        return PermissionService.getInstance().requestPermissions(activity,SERVICE_LOCATION,locationServicePermissions);
    }

    /**
     * Sets up location service after permissions is granted
     */
    @SuppressWarnings("MissingPermission")
    public void requestLocationUpdates(Context context,LocationServiceCallback<Location> callback) {
        if(callback!=null) locationUpdateListeners.add(callback);
        if(locationManager!=null || context==null) return;
        if(!PermissionService.getInstance().hasAnyPermission(context,locationServicePermissions)) return;
        try {
            context = context.getApplicationContext();
            locationManager = (LocationManager)context.getSystemService(LOCATION_SERVICE);
            if(locationManager!=null && getLocationProvider()!=null) {
                locationManager.requestLocationUpdates(locationProvider,
                                                       MIN_TIME_BW_UPDATES,
                                                       MIN_DISTANCE_CHANGE_FOR_UPDATES,this);
                location = locationManager.getLastKnownLocation(locationProvider);
            }
        } catch(Exception e) {
            Log.e(APP,TAG+".requestLocationUpdates",e);
        }
    }

    @Override
    public void onLocationChanged(Location location) {
        this.location = location;
        if(!locationUpdateListeners.isEmpty())
            locationUpdateListeners.forEach(listener -> listener.onLocationUpdate(location));
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

    public String getLocationProvider() {
        if(locationProvider==null && locationManager!=null) {
            if(!forceNetwork && locationManager.isProviderEnabled(GPS_PROVIDER)) {
                locationProvider = GPS_PROVIDER;
            } else if(locationManager.isProviderEnabled(NETWORK_PROVIDER)) {
                locationProvider = NETWORK_PROVIDER;
            }
        }
        return locationProvider;
    }

    public Location getLocation() {
        return location;
    }

    public void getFromLocation(double lon,double lat,int max,Locale locale,LocationServiceCallback<List<Address>> callback) {
        if(callback==null) return;
        if(locale==null) locale = Locale.getDefault();
        String language = locale.getLanguage();
        String url = GOOGLE_MAPS_API_GEOCODE_URL+"?latlng="+lat+","+lon+"&language="+language+"&key="+GOOGLE_MAPS_API_KEY;
        getFromLocation(url,max,locale,callback);
    }

    public void getFromLocationName(String address,int max,Locale locale,LocationServiceCallback<List<Address>> callback) {
        if(callback==null) return;
        if(locale==null) locale = Locale.getDefault();
        try {
            address = URLEncoder.encode(address,"UTF-8");
        } catch(UnsupportedEncodingException e) {
            Log.e(APP,TAG+".getFromLocationName",e);
            return;
        }
        String language = locale.getLanguage();
        String url = GOOGLE_MAPS_API_GEOCODE_URL+"?address="+address+"&language="+language+"&key="+GOOGLE_MAPS_API_KEY;
        getFromLocation(url,max,locale,callback);
    }

    private void getFromLocation(String url,int max,Locale locale,LocationServiceCallback<List<Address>> callback) {
        Log.d(APP,TAG+".getFromLocation("+url+")");
        RequestQueue requestQueue = VolleyService.getInstance().getRequestQueue();
        requestQueue.add(new JsonObjectRequest(url,null,response -> {
            Log.d(APP,TAG+".getFromLocation(url: "+url+", response: "+response+")");
            List<Address> addresses = parseLocationData(response,max,locale);
            callback.onLocationUpdate(addresses);
        },error -> {
            Log.e(APP,TAG+".getFromLocation(url: "+url+")",error);
        }) {
            @Override
            public Map<String,String> getHeaders() {
                Log.d(APP,TAG+".getFromLocation(package: "+VolleyService.getInstance().getPackageName()+
                          ", cert: "+VolleyService.getInstance().getSignatureSHA1()+")");
                Map<String,String> headers = new HashMap<>();
                headers.put("X-Android-Package",VolleyService.getInstance().getPackageName());
                headers.put("X-Android-Cert",VolleyService.getInstance().getSignatureSHA1());
                return headers;
            }
        });
    }

    public void getTimeZone(double lon,double lat,long timestamp,Locale locale,LocationServiceCallback<TimeZone> callback) {
        if(callback==null) return;
        if(locale==null) locale = Locale.getDefault();
        String language = locale.getLanguage();
        String url = GOOGLE_MAPS_API_TIME_ZONE_URL+"?location="+lat+","+lon+"&timestamp="+timestamp+"&language="+language+"&key="+GOOGLE_MAPS_API_KEY;
        getTimeZone(url,callback);
    }

    private void getTimeZone(String url,LocationServiceCallback<TimeZone> callback) {
        Log.d(APP,TAG+".getTimeZone("+url+")");
        RequestQueue requestQueue = VolleyService.getInstance().getRequestQueue();
        requestQueue.add(new JsonObjectRequest(url,null,response -> {
//Log.d(APP,TAG+".getTimeZone(data: "+data+")");
            try {
                TimeZone tz = new TimeZone();
                tz.dstOffset = response.optInt("dstOffset",0);
                tz.rawOffset = response.optInt("rawOffset",0);
                tz.timeZoneId = response.optString("timeZoneId",GMT);
                tz.timeZoneName = response.optString("timeZoneName",GMT);
                callback.onLocationUpdate(tz);
            } catch(Exception e) {
                Log.e(APP,TAG+".getTimeZone(url: "+url+")",e);
            }
        },error -> {
            Log.e(APP,TAG+".getTimeZone(url: "+url+")",error);
        }) {
            @Override
            public Map<String,String> getHeaders() {
                Log.d(APP,TAG+".getTimeZone(package: "+VolleyService.getInstance().getPackageName()+
                          ", cert: "+VolleyService.getInstance().getSignatureSHA1()+")");
                Map<String,String> headers = new HashMap<>();
                headers.put("X-Android-Package",VolleyService.getInstance().getPackageName());
                headers.put("X-Android-Cert",VolleyService.getInstance().getSignatureSHA1());
                return headers;
            }
        });
    }

    private List<Address> parseLocationData(JSONObject json,int max,Locale locale) {
        if(json==null) return null;
        try {
//Log.d(APP,TAG+".parseLocationData("+data+")");
            if(!json.optString("status").equals("OK")) return null;
            List<Address> addresses = new ArrayList<>();
            JSONArray results = (JSONArray)json.get("results");
            for(int i = 0; i<results.length() && i<max; ++i) {
                Address addr = new Address(locale);
                JSONObject obj = results.getJSONObject(i);
                JSONArray components = obj.getJSONArray("address_components");
                for(int j = 0; j<components.length(); ++j) {
                    JSONObject obj2 = components.getJSONObject(j);
                    JSONArray types = obj2.getJSONArray("types");
                    for(int k = 0; k<types.length(); ++k) {
                        String type = types.getString(k);
                        if(type.equals("locality")) {
                            addr.setLocality(obj2.getString("long_name"));
                        } else if(addr.getLocality()==null && (type.equals("sublocality") || type.equals("postal_town"))) {
                            addr.setLocality(obj2.getString("long_name"));
                        } else if(type.equals("country")) {
                            addr.setCountryName(obj2.getString("long_name"));
                            addr.setCountryCode(obj2.getString("short_name"));
                        } else {
                            continue;
                        }
                        break;
                    }
                    if(addr.getCountryName()!=null &&
                       addr.getCountryCode()!=null &&
                       addr.getLocality()!=null) break;
                }
                JSONObject geometry = obj.getJSONObject("geometry");
                JSONObject location = geometry.getJSONObject("location");
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

