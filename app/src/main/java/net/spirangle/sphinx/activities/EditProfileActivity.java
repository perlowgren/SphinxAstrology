package net.spirangle.sphinx.activities;

import static com.android.volley.Request.Method.POST;
import static com.android.volley.Request.Method.PUT;
import static net.spirangle.sphinx.config.AstrologyProperties.*;
import static net.spirangle.sphinx.config.SphinxProperties.*;

import android.content.Intent;
import android.database.Cursor;
import android.location.Address;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.*;
import android.widget.AdapterView.OnItemSelectedListener;

import com.android.volley.RequestQueue;
import com.android.volley.toolbox.JsonObjectRequest;

import net.spirangle.sphinx.R;
import net.spirangle.sphinx.astro.Coordinate;
import net.spirangle.sphinx.astro.Horoscope;
import net.spirangle.sphinx.astro.Horoscope.Calendar;
import net.spirangle.sphinx.db.AstroDB;
import net.spirangle.sphinx.db.Key;
import net.spirangle.sphinx.services.LocationService;
import net.spirangle.sphinx.services.VolleyService;

import org.json.JSONObject;

import java.util.*;


public class EditProfileActivity extends AstroActivity {
    private static final String TAG = EditProfileActivity.class.getSimpleName();

    private EditText editName;
    private EditText editDateTime;
    private EditText editLocation;
    private Button buttonFindLocation;
    private Spinner spinnerLocation;
    private EditText editTimeZone;
    private CheckBox checkDST;
    private CheckBox checkJulian;
    private CheckBox checkBCE;

    private long id = -1L;
    private Key key = null;
    private String name = null;
    private String dateTimeText = null;
    private Calendar dateTime = null;
    private boolean timeUnknown = false;
    private String locationText = null;
    private boolean locationSet = false;
    private List<Address> locations = null;
    private String locality = null;
    private String country = null;
    private String countryCode = null;
    private double longitude = 0.0;
    private double latitude = 0.0;
    private String tzName = null;
    private double tzOffset = 0.0;
    private double dstOffset = 0.0;
    private int flags = 0;

    public EditProfileActivity() {
        create_flags = NAVIGATION_ICON_BACK|ACTIONBAR_TITLE;
        activity_layout_id = R.layout.activity_edit_profile;
        toolbar_id = R.id.toolbar;
        toolbar_menu_id = R.menu.menu_ab_edit_profile;
        navigation_icon_id = R.drawable.ic_ab_back;
        loading_panel_id = R.id.loading_panel;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        try {
            editName = (EditText)findViewById(R.id.edit_name);
            editDateTime = (EditText)findViewById(R.id.edit_date_time);
            editLocation = (EditText)findViewById(R.id.edit_location);
            spinnerLocation = (Spinner)findViewById(R.id.spinner_location);
            buttonFindLocation = (Button)findViewById(R.id.button_find_location);
            editTimeZone = (EditText)findViewById(R.id.edit_time_zone);
            checkDST = (CheckBox)findViewById(R.id.check_dst);
            checkJulian = (CheckBox)findViewById(R.id.check_julian);
            checkBCE = (CheckBox)findViewById(R.id.check_bce);

            Log.d(APP,TAG+".onCreate("+toolbar+")");

            buttonFindLocation.setTypeface(iconFont);
            editTimeZone.setFocusable(false);

            Intent intent = getIntent();
            long id = intent.getLongExtra(EXTRA_PROFILE,-1);
            if(!loadProfile(id)) findLocation("");

            editDateTime.addTextChangedListener(new TextWatcher() {
                private boolean react = true;

                @Override
                public void afterTextChanged(Editable s) {}

                @Override
                public void beforeTextChanged(CharSequence s,int start,int count,int after) {}

                @Override
                public void onTextChanged(CharSequence s,int start,int before,int count) {
                    if(!react || count==0) return;
                    String text = editDateTime.getText().toString();
                    int l = text.length();
                    if(l==0) return;
                    char c = text.charAt(l-1);
                    if(c=='-') return;
                    if(l>19) text = text.substring(0,19);
                    else switch(l) {
                        // 0123456789012345678
                        // YYYY-MM-DD HH:MM:SS
                        case 4:
                        case 7:
                            text += '-';
                            break;
                        case 5:
                        case 8:
                            text = text.substring(0,l-1)+'-'+c;
                            break;
                        case 10:
                            text += ' ';
                            break;
                        case 11:
                            text = text.substring(0,l-1)+(c>='3'? "-0" : "-")+c;
                            break;
//					case 13:
//					case 16:text += ':';break;
                        case 14:
                        case 17:
                            text = text.substring(0,l-1)+(c>='6'? ":0" : ":")+c;
                            break;
                        case 6:
                        case 9:
                        case 12:
                        case 15:
                        case 18:
                            if(c>=(l==6? '2' : (l==9? '4' : (l==12? '3' : '6'))))
                                text = text.substring(0,l-1)+'0'+c+(l==6? '-' : (l==9? ' ' : ""));
                            break;
                        default:
                            return;
                    }
                    react = false;
                    editDateTime.setText(text);
                    editDateTime.setSelection(text.length());
                    react = true;
                }
            });

            spinnerLocation.setOnItemSelectedListener(new OnItemSelectedListener() {
                @Override
                public void onItemSelected(AdapterView<?> parent,View view,int pos,long id) {
                    List<Address> list = getLocations();
                    Address addr = list!=null? list.get(pos) : null;
                    setLocation(addr);
                    Log.d(APP,TAG+".onItemSelected(position: "+pos+", id: "+id+
                              ", locality: "+locality+" ["+country+"], lon: "+longitude+", lat: "+latitude+")");
                }

                @Override
                public void onNothingSelected(AdapterView<?> parent) {

                }
            });
        } catch(Exception e) {
            Log.e(APP,TAG+".onCreate",e);
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if(item.getItemId()==R.id.menu_ok) {
            Horoscope h = saveProfile();
            openHoroscope(h,0);
            setResult(RESULT_OK,null);
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

	/*@Override
	public boolean onNavigationItemSelected(MenuItem item) {
		switch(item.getItemId()) {
			case R.id.drawer_help:
				openHelp(R.string.text_help_edit_profile);
				break;

			default:return super.onNavigationItemSelected(item);
		}
//		item.setChecked(false);
		drawerLayout.closeDrawer(Gravity.LEFT);
		return true;
	}*/

    public boolean loadProfile(long id) {
        if(id==-1l) return false;
        String lang = Locale.getDefault().getLanguage();
        AstroDB db = AstroDB.getInstance();
        Cursor cur = db.query("SELECT p._id,p.profileKey,p.name,p.year,p.month,p.day,p.hour,p.minute,p.second,"+
                              "l.locality,l.countryCode,p.longitude,p.latitude,p.timeZone,p.dst,p.flags "+
                              "FROM Profile as p LEFT JOIN Location as l "+
                              "ON p.longitude=l.longitude AND p.latitude=l.latitude "+
                              "AND l.language='"+lang+"' "+
                              "WHERE p._id="+id);
        if(cur==null || !cur.moveToFirst()) return false;
        this.id = id;
        key = new Key(cur.getLong(1));
        name = cur.getString(2);
        int year = cur.getInt(3);
        int mon = cur.getInt(4);
        int day = cur.getInt(5);
        int hour = cur.getInt(6);
        int min = cur.getInt(7);
        int sec = cur.getInt(8);
        String ln = cur.getString(9);
        String cc = cur.getString(10);
        double lon = (double)cur.getDouble(11)/1000000.0;
        double lat = (double)cur.getDouble(12)/1000000.0;
        double tz = (double)cur.getDouble(13)/3600.0;
        double dst = (double)cur.getDouble(14)/3600.0;
        flags = cur.getInt(15);
        int ctype = (flags&0x0f);
        cur.close();

        Locale l = Locale.getDefault();
        String dt;
        if((flags&Horoscope.TIME_UNKNOWN)==Horoscope.TIME_UNKNOWN) {
            timeUnknown = true;
            dt = String.format(l,"%1$d-%2$02d-%3$02d",year,mon,day);
        } else {
            timeUnknown = false;
            if(sec==0) dt = String.format(l,"%1$d-%2$02d-%3$02d %4$02d:%5$02d",year,mon,day,hour,min);
            else dt = String.format(l,"%1$d-%2$02d-%3$02d %4$02d:%5$02d:%6$02d",year,mon,day,hour,min,sec);
        }

        String loc = ln+", "+cc;

        editName.setText(name);
        editDateTime.setText(dt);
        editLocation.setText(loc);

        readDateTime();

        clearLocation();

        if(!loadTimeZone(lon,lat,dateTime.secondsFromUnixEpoch(tz,dst),false)) {
            setDST(dst);
            setTimeZone(GMT,tz);
        }

        if(!loadLocations(lon,lat)) {
            List<Address> addresses = new ArrayList<Address>();
            Locale locale = Locale.getDefault();
            Address addr;
            addr = new Address(locale);
            addr.setLocality(ln);
            addr.setCountryCode(cc);
            addr.setLongitude(lon);
            addr.setLatitude(lat);
            addresses.add(addr);
            setLocations(loc,addresses);
        }
        return true;
    }

    public Horoscope saveProfile() {
        readDateTime();
        long cat1 = 0l;
        long cat2 = 0l;
        name = editName.getText().toString();
        int year = dateTime.year;
        int mon = dateTime.month;
        int day = dateTime.day;
        int hour = dateTime.hour;
        int min = dateTime.minute;
        double sec = dateTime.second;
        int ctype = Horoscope.GREGORIAN_CALENDAR;
        double dst = dstOffset;
        double tz = tzOffset;
        boolean bce = false;

        if(key==null) key = new Key(Key.PROFILE);

        if(checkDST.isChecked() && dst==0.0) dst = 1.0;
        else if(!checkDST.isChecked()) dst = 0.0;
        if(checkJulian.isChecked()) ctype = Horoscope.JULIAN_CALENDAR;
        if(checkBCE.isChecked()) bce = true;

        Horoscope h = new Horoscope(id,key,name,year,mon,day,hour,min,sec,ctype);
        h.setLocation(locality,country,countryCode,longitude,latitude,tz);
        h.setDaylightSavingTime(dst);
        h.setTimeUnknown(timeUnknown);
        h.setBCE(bce);

        int[] planets = {
            _ASTRO_SUN_,
            _ASTRO_MOON_,
            _ASTRO_ASCENDANT_,
            -1};

        h.calculate(planets,0);

        String url = URL_SPIRANGLE_API+"/users/"+user.key+"/profiles/"+key;
        JSONObject json = h.getJSONObject(cat1,cat2);

        AstroDB db = AstroDB.getInstance();
        RequestQueue requestQueue = VolleyService.getRequestQueue();
        int method;
        if(h.getId()==-1) {
            db.insertProfile(user.id,cat1,cat2,h);
            method = POST;
        } else {
            db.updateProfile(cat1,cat2,h);
            method = PUT;
        }
        requestQueue.add(new JsonObjectRequest(method,url,json,response -> {
            shortToast(R.string.toast_profile_saved);
        },error -> {
            Log.e(APP,TAG+".saveProfile",error);
            shortToast(R.string.toast_save_failed);
        }) {
            @Override
            public Map<String,String> getHeaders() {
                Map<String,String> headers = new HashMap<>();
                headers.put("Authorization","Google "+google.tokenId);
                return headers;
            }
        });
        return h;
    }

    /*public void result(Map<String,List<String>> headers,String data,int status,long id,Object object) {
//Log.d(APP,TAG+".result(data: "+data+", status: "+status+")");
        if(status==200 || status==201 || status==204) {
            shortToast(R.string.toast_profile_saved);
        } else {
            String m = null;
            try {
                JSONObject json = new JSONObject(data);
                m = json.optString("message",null);
            } catch(Exception e) {
                Log.e(APP,TAG+".result",e);
            }
            if(m!=null) shortToast(getString(R.string.toast_save_failed)+": "+m);
            else shortToast(R.string.toast_save_failed);
        }
    }*/

    private void readDateTime() {
        readDateTime(editDateTime.getText().toString().trim());
    }

    private void readDateTime(String dt) {
        if(dt!=null && dt.equals(dateTimeText)) return;
        dateTimeText = dt;
        dateTime = new Calendar(1970,1,1,12,0,0.0,Horoscope.GREGORIAN_CALENDAR);
        timeUnknown = true;
        Log.d(APP,TAG+".readDateTime(\""+dt+"\")");
        if(dt!=null && !dt.equals("")) {
            int y = 1970, m = 1, d = 1, h = 12, n = 0, s = 0;
            String[] arr = dt.split("[- :]");
            if(arr.length>=1) {
                int l = arr.length;
//for(int i=0; i<arr.length; ++i)
//Log.d(APP,TAG+".requestTimeZone("+arr[i]+")");
                y = Integer.parseInt(arr[0]);
                if(l>=2) {
                    m = Integer.parseInt(arr[1]);
                    if(m<1) m = 1;
                    else if(m>12) m = 12;
                    if(l>=3) {
                        d = Integer.parseInt(arr[2]);
                        if(d<1) d = 1;
                        else if(m==1 && d>28) d = 28;
                        else if((m==3 || m==5 || m==8 || m==10) && d>30) d = 30;
                        else if(d>31) d = 31;
                        if(l>=4) {
                            h = Integer.parseInt(arr[3]);
                            if(h>=24) h = 0;
                            if(l>=5) {
                                n = Integer.parseInt(arr[4]);
                                if(n>=60) n = 0;
                                if(l>=6) {
                                    s = Integer.parseInt(arr[5]);
                                    if(s>=60) s = 0;
                                }
                            }
                            timeUnknown = false;
                        }
                    }
                }
                dateTime.set(y,m,d,h,n,s,Horoscope.GREGORIAN_CALENDAR);
            }
        }
        Log.d(APP,TAG+".readDateTime("+dateTime.toString()+")");
    }

    private void clearLocation() {
        locationSet = false;
        locationText = null;
        locations = null;
        locality = null;
        country = null;
        countryCode = null;
        longitude = 0.0;
        latitude = 0.0;
        tzName = null;
        tzOffset = 0.0;
        dstOffset = 0.0;
    }

    public void findLocation(double lon,double lat) {
        clearLocation();
        Log.d(APP,TAG+".findLocation(lon: "+lon+", lat: "+lat+")");
        hideKeyboard();
        showLoading();
        LocationService.getInstance().getFromLocation(lon,lat,10,null,addresses -> {
            if(addresses!=null && saveLocations(null,addresses) && setLocations(null,addresses)) return;
            findLocation("");
            hideLoading();
        });
    }

    public void findLocation(View view) {
        findLocation(editLocation.getText().toString().trim());
    }

    public void findLocation(final String loc) {
        if(loc!=null && locationText!=null && locationText.equals(loc)) return;
        clearLocation();
        Log.d(APP,TAG+".findLocation(location: \""+loc+"\")");
        hideKeyboard();
        if(loc==null || loc.equals("")) {
            List<Address> addresses = new ArrayList<>();
            Address addr = new Address(Locale.getDefault());
            addr.setLocality(getString(R.string.label_greenwich));
            addr.setCountryName(getString(R.string.label_gb));
            addr.setCountryCode(getString(R.string.label_gb));
            addr.setLongitude(GMT_LON);
            addr.setLatitude(GMT_LAT);
            addresses.add(addr);
            setLocations(loc,addresses);
//			setTimeZone(0.0,getString(R.string.label_gmt));
//			readDateTime();
//			TimeZone tz = TimeZone.getTimeZone("GMT");
//			setDST(tz.inDaylightTime(dateTime.getTime())? 1.0 : 0.0);
        } else {
            if(loadLocations(loc)) return;
            showLoading();
            LocationService.getInstance().getFromLocationName(loc,10,null,addresses -> {
                if(addresses!=null && saveLocations(loc,addresses) && setLocations(loc,addresses)) return;
                findLocation("");
                hideLoading();
            });

/*			try {
				Geocoder geocoder = new Geocoder(getApplicationContext(),Locale.getDefault());
//				if(!geocoder.isPresent()) return;
				List<Address> addresses = geocoder.getFromLocationName(loc,10);
				if(setLocations(loc,addresses)) {
					Address addr = addresses.get(0);
					requestTimeZone(addr.getLongitude(),addr.getLatitude());
				}
			} catch(Exception e) {
Log.e(APP,TAG+".findLocation",e);
				findLocation("");
			}*/
        }
    }

    public List<Address> getLocations() {
        return locations;
    }

    public boolean saveLocations(String loc,List<Address> addresses) {
        if(addresses==null || addresses.size()==0) return false;
        if(loc!=null && locationText!=null && locationText.equals(loc)) return true;
        Locale locale = Locale.getDefault();
        String lang = locale.getLanguage();
        Address addr;
        AstroDB db = AstroDB.getInstance();
        for(int i = 0; i<addresses.size(); ++i) {
            addr = addresses.get(i);
            db.insertLocation(loc,addr,lang,0);
//Log.d(APP,TAG+".setLocations(loc: "+loc+", lon: "+lon+", lat: "+lat+")");
        }
        return true;
    }

    public boolean loadLocations(String loc) {
        if(loc==null || loc.equals("")) return false;
        if(locationText!=null && locationText.equals(loc)) return true;
        Locale locale = Locale.getDefault();
        AstroDB db = AstroDB.getInstance();
        Cursor cur = db.query("SELECT name,locality,country,countryCode,longitude,latitude FROM Location WHERE "+
                              "(name='"+loc+"' OR locality='"+loc+"') AND language='"+locale.getLanguage()+"'");
        return loadLocations(cur,locale);
    }

    public boolean loadLocations(double lon,double lat) {
        Locale locale = Locale.getDefault();
        AstroDB db = AstroDB.getInstance();
        Cursor cur = db.query("SELECT name,locality,country,countryCode,longitude,latitude FROM Location WHERE "+
                              "longitude="+(int)Math.round(lon*1000000.0)+" AND latitude="+(int)Math.round(lat*1000000.0)+" AND "+
                              "language='"+locale.getLanguage()+"'");
        return loadLocations(cur,locale);
    }

    public boolean loadLocations(Cursor cur,Locale locale) {
        if(cur==null || !cur.moveToFirst()) return false;
        String loc = null;
        List<Address> addresses = new ArrayList<Address>();
        Address addr;
        while(!cur.isAfterLast()) {
            if(loc==null) {
                loc = cur.getString(0);
                if(loc==null || loc.length()==0) loc = cur.getString(1);
                editLocation.setText(loc);
            }
            Log.d(APP,TAG+".loadLocations(name: "+cur.getString(0)+", locality: "+cur.getString(1)+")");
            addr = new Address(locale);
            addr.setLocality(cur.getString(1));
            addr.setCountryName(cur.getString(2));
            addr.setCountryCode(cur.getString(3));
            addr.setLongitude((double)cur.getInt(4)/1000000.0);
            addr.setLatitude((double)cur.getInt(5)/1000000.0);
            addresses.add(addr);
            cur.moveToNext();
        }
        cur.close();
        return addresses.size()>0 && setLocations(loc,addresses);
    }

    public boolean setLocations(String loc,List<Address> addresses) {
        if(addresses==null || addresses.size()==0) return false;
        if(loc!=null && locationText!=null && locationText.equals(loc)) return true;
        List<String> list = new ArrayList<String>();
        Address addr;
        for(int i = 0; i<addresses.size(); ++i) {
            addr = addresses.get(i);
            Log.d(APP,TAG+".setLocations(locality: "+addr.getLocality()+", country: "+addr.getCountryCode()+
                      ", lon: "+Coordinate.formatLongitudeGrade(addr.getLongitude())+
                      ", lat: "+Coordinate.formatLatitudeGrade(addr.getLatitude())+")");
            if(loc==null) loc = addr.getLocality();
            list.add(
                addr.getLocality()+", "+
                addr.getCountryCode()+", "+
                Coordinate.formatLongitudeGrade(addr.getLongitude())+" "+
                Coordinate.formatLatitudeGrade(addr.getLatitude())
                    );
        }
        locationText = loc;
        locationSet = true;
        locations = addresses;
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(
            this,R.layout.simple_spinner_item,list);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerLocation.setAdapter(adapter);
//		setLocation(addresses.get(0));
        return true;
    }

    public void setLocation(Address address) {
        if(address==null) {
            locality = getString(R.string.label_greenwich);
            country = getString(R.string.label_gb);
            countryCode = getString(R.string.label_gb);
            longitude = GMT_LON;
            latitude = GMT_LAT;
        } else {
            locality = address.getLocality();
            country = address.getCountryName();
            countryCode = address.getCountryCode();
            longitude = address.getLongitude();
            latitude = address.getLatitude();
            if(tzName==null)
                requestTimeZone(address.getLongitude(),address.getLatitude());
        }
    }

    private void requestTimeZone(final double lon,final double lat) {
        readDateTime();
        final long time = dateTime.secondsFromUnixEpoch();
        Log.d(APP,TAG+".requestTimeZone(time: "+time+")");
        if(loadTimeZone(lon,lat,time,true)) return;
        showLoading();
        LocationService.getInstance().getTimeZone(lon,lat,time,null,timeZone -> {
            final long utc = time-timeZone.rawOffset;
            Log.d(APP,TAG+".requestTimeZone(utc: "+utc+")");
            LocationService.getInstance().getTimeZone(lon,lat,utc,null,timeZoneUtc -> {
                double tzOffset = (double)timeZoneUtc.rawOffset/3600.0;
                double dst = (double)timeZoneUtc.dstOffset/3600.0;
                String lang = Locale.getDefault().getLanguage();
                AstroDB db = AstroDB.getInstance();
                db.insertTimeZone(timeZoneUtc.timeZoneName,lon,lat,utc,tzOffset,dst,lang,0);
                setTimeZone(timeZoneUtc.timeZoneName,tzOffset);
                setDST(dst);
                hideLoading();
            });
        });
    }

    public boolean loadTimeZone(double lon,double lat,long time,boolean local) {
        Locale locale = Locale.getDefault();
        long n = AstroDB.timestamp();
        long t = 7L*24L*3600L;
        String ts = "timestamp"+(local? "+offset" : "");
        AstroDB db = AstroDB.getInstance();
        Cursor cur = db.query("SELECT name,offset,dst FROM TimeZone WHERE "+
                              "longitude="+(int)Math.round(lon*1000000.0)+" AND latitude="+(int)Math.round(lat*1000000.0)+" AND "+
                              ts+(time<n-t? "="+time : ">="+(time-t))+" "+"AND language='"+locale.getLanguage()+"' "+
                              "ORDER BY ABS("+ts+"-"+time+") ASC LIMIT 1");
        if(cur==null || !cur.moveToFirst()) return false;
        String name = cur.getString(0);
        double offset = (double)cur.getInt(1)/3600.0;
        double dst = (double)cur.getInt(2)/3600.0;
        cur.close();
        setTimeZone(name,offset);
        setDST(dst);
        hideLoading();
        return true;
    }

    private void setTimeZone(String name,double offset) {
        Log.d(APP,TAG+".setDST(name: "+name+", offset: "+offset+")");
        editTimeZone.setText(String.format(Locale.ENGLISH,"%+02.1f %s",offset,name));
        tzName = name;
        tzOffset = offset;
    }

    private void setDST(double offset) {
        Log.d(APP,TAG+".setDST("+offset+")");
        checkDST.setChecked(offset!=0.0);
        dstOffset = offset;
    }
}
