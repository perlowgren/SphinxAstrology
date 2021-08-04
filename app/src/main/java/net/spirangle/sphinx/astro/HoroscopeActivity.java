package net.spirangle.sphinx.astro;

import static net.spirangle.sphinx.config.AstrologyProperties.*;
import static net.spirangle.sphinx.config.SphinxProperties.APP;
import static net.spirangle.sphinx.config.SphinxProperties.EXTRA_RADIX1;

import android.content.Intent;
import android.database.Cursor;
import android.location.Address;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.ScrollView;
import android.widget.TextView;

import androidx.appcompat.widget.AppCompatSpinner;

import net.spirangle.sphinx.*;
import net.spirangle.sphinx.Symbol.SymbolListener;

import java.util.Calendar;
import java.util.List;
import java.util.Locale;


public class HoroscopeActivity extends AstroActivity implements SymbolListener {
    private static final String TAG = "HoroscopeActivity";

    private static final String[] moonPhasesUnicode = {
        "&#x1F315;","&#x1F316;","&#x1F317;","&#x1F318;","&#x1F311;","&#x1F312;","&#x1F313;","&#x1F314;",
    };

    private AppCompatSpinner toolbarSpinner;
    private MenuItem drawerEdit;
    private MenuItem menuNew = null;
    private MenuItem menuEdit = null;
    private MenuItem menuProfiles = null;
    private TextView textInfo;
    private ScrollView scroll;
    private HoroscopeView[] horoscopeViews;
    private WheelGraphView wheelGraph;
    private PlanetsView planets;
    private AspectTableView aspectTable;
    private AspectListView aspectList;
    private AspectPatternsView aspectPatterns;
    private ArabicPartsView arabicParts;
    private long id = -1;

    public static Horoscope loadHoroscope(long id) {
        String lang = Locale.getDefault().getLanguage();
        AstroDB db = AstroDB.getInstance();
        Cursor cur = db.query("SELECT p._id,p.profileKey,p.name,p.year,p.month,p.day,p.hour,p.minute,p.second,"+
                              "l.locality,l.country,l.countryCode,p.longitude,p.latitude,p.timeZone,p.dst,p.flags "+
                              "FROM Profile as p LEFT JOIN Location as l "+
                              "ON p.longitude=l.longitude AND p.latitude=l.latitude "+
                              "AND l.language='"+lang+"' "+
                              "WHERE p._id="+id);
        if(!cur.moveToFirst()) return null;
        return loadHoroscope(cur);
    }

    public static Horoscope loadHoroscope(Cursor cur) {
        long id = cur.getLong(0);
        Key key = new Key(cur.getLong(1));
        String name = cur.getString(2);
        int year = cur.getInt(3);
        int mon = cur.getInt(4);
        int day = cur.getInt(5);
        int hour = cur.getInt(6);
        int min = cur.getInt(7);
        double sec = (double)cur.getInt(8);
        String ln = cur.getString(9);
        String cn = cur.getString(10);
        String cc = cur.getString(11);
        double lon = (double)cur.getInt(12)/1000000.0;
        double lat = (double)cur.getInt(13)/1000000.0;
        double dst = (double)cur.getInt(14)/3600.0;
        double tz = (double)cur.getInt(15)/3600.0;
        int flags = cur.getInt(16);
        int ctype = (flags&0x0f);
        cur.close();
        Horoscope h = new Horoscope(id,key,name,year,mon,day,hour,min,sec,ctype);
        h.setLocation(ln,cn,cc,lon,lat,tz);
        h.setDaylightSavingTime(dst);
        h.setTimeUnknown((flags&Horoscope.TIME_UNKNOWN)==Horoscope.TIME_UNKNOWN);
        h.setBCE((flags&Horoscope.BCE)==Horoscope.BCE);
        return h;
    }

    public HoroscopeActivity() {
        activity_layout_id = R.layout.activity_horoscope;
        drawer_layout_id = R.id.drawer_layout;
        toolbar_id = R.id.toolbar;
        toolbar_menu_id = R.menu.menu_ab_horoscope;
        navigation_icon_id = R.drawable.ic_ab_menu;
        navigation_view_id = R.id.navigation_view;
        loading_panel_id = R.id.loading_panel;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        try {
            Menu menu = navigationView.getMenu();
            drawerSignIn = (MenuItem)menu.findItem(R.id.drawer_sign_in);
            drawerSignOut = (MenuItem)menu.findItem(R.id.drawer_sign_out);
            drawerEdit = (MenuItem)menu.findItem(R.id.drawer_edit);
            toolbarSpinner = (AppCompatSpinner)findViewById(R.id.toolbar_spinner);
            textInfo = (TextView)findViewById(R.id.text_info);
            scroll = (ScrollView)findViewById(R.id.scroll_view);
            wheelGraph = (WheelGraphView)findViewById(R.id.wheel_graph_view);
            planets = (PlanetsView)findViewById(R.id.planets_view);
            aspectTable = (AspectTableView)findViewById(R.id.aspect_table_view);
            aspectList = (AspectListView)findViewById(R.id.aspect_list_view);
            aspectPatterns = (AspectPatternsView)findViewById(R.id.aspect_patterns_view);
            arabicParts = (ArabicPartsView)findViewById(R.id.arabic_parts_view);

            horoscopeViews = new HoroscopeView[] {
                wheelGraph,
                planets,
                aspectTable,
                aspectList,
                aspectPatterns,
                arabicParts,
            };

            drawerEdit.setVisible(false);
            wheelGraph.setScroll(scroll);

            for(int i = 0; i<horoscopeViews.length; ++i)
                horoscopeViews[i].setSymbolListener(this);

            Intent intent = getIntent();
            Horoscope h = (Horoscope)intent.getParcelableExtra(EXTRA_RADIX1);
            if(h!=null) id = h.getId();
            else {
/*			Calendar cal = Calendar.getInstance();

			int year    = cal.get(Calendar.YEAR);
			int mon     = cal.get(Calendar.MONTH)+1;
			int day     = cal.get(Calendar.DAY_OF_MONTH);
			int hour    = cal.get(Calendar.HOUR_OF_DAY);
			int min     = cal.get(Calendar.MINUTE);
			double sec  = (double)cal.get(Calendar.SECOND)+((double)cal.get(Calendar.MILLISECOND)/1000.0);
			double dst  = (double)cal.get(Calendar.DST_OFFSET)/3600000.0;
			String ln   = getString(R.string.label_greenwich);
			String cn   = getString(R.string.label_england);
			String cc   = getString(R.string.label_gb);
			double lon  = GMT_LON;
			double lat  = GMT_LAT;
			double tz   = (double)cal.get(Calendar.ZONE_OFFSET)/3600000.0;

//			LocationManager locationManager = (LocationManager)this.getSystemService(Context.LOCATION_SERVICE);
//			String locationProvider = LocationManager.NETWORK_PROVIDER;
			// Or use LocationManager.GPS_PROVIDER
//			Location location = locationManager.getLastKnownLocation(locationProvider);

			h = new Horoscope(-1,null,getString(R.string.horoscope_now),year,mon,day,hour,min,sec);
			h.setDaylightSavingTime(dst);
			h.setLocation(ln,cn,cc,lon,lat,tz);*/
                final Horoscope h1 = new Horoscope(getString(R.string.horoscope_now));
                h = h1;

                Log.d(APP,TAG+".onCreate(LocationService.getLocationManager...)");
                final LocationService ls = LocationService.getInstance(this);
                Log.d(APP,TAG+".onCreate(done...)");
                if(ls.location!=null) {
                    Calendar cal = Calendar.getInstance();
                    final double lon = ls.location.getLongitude();
                    final double lat = ls.location.getLatitude();
                    final double tz = (double)cal.get(Calendar.ZONE_OFFSET)/3600000.0;
                    final double dst = (double)cal.get(Calendar.DST_OFFSET)/3600000.0;
                    final String lang = Locale.getDefault().getLanguage();
                    final AstroDB db = AstroDB.getInstance();

                    double m = 0.01+(lat/90.0)*0.0083;
                    Cursor cur = db.query("SELECT locality,country,countryCode FROM Location WHERE "+
                                          "longitude>="+(int)Math.round((lon-m)*1000000.0)+" AND "+
                                          "longitude<="+(int)Math.round((lon+m)*1000000.0)+" AND "+
                                          "latitude>="+(int)Math.round((lat-0.01)*1000000.0)+" AND "+
                                          "latitude<="+(int)Math.round((lat+0.01)*1000000.0)+" AND "+
                                          "language='"+lang+"' LIMIT 1",null);
                    if(!cur.moveToFirst()) {
                        showLoading();
                        Log.d(APP,TAG+".onCreate(LocationService.getFromLocation...)");
                        ls.getFromLocation(lon,lat,10,null,
                                           new LocationService.AddressReceiver() {
                                               @Override
                                               public void receive(List<Address> addresses,int status) {
                                                   if(addresses!=null && addresses.size()>0) {
                                                       Address addr = addresses.get(0);
                                                       h1.setLocation(addr,tz);
                                                       db.insertLocation(addr.getLocality(),addr,lon,lat,lang,0);
                                                       Log.d(APP,TAG+".onCreate => LocationService.AddressReceiver.receive(loc: "+addr.getLocality()+", lon: "+lon+", lat: "+lat+")");
                                                   }
                                                   setHoroscope(h1);
                                                   hideLoading();
                                               }
                                           });
                        return;
                    }
                    h.setLocation(cur.getString(0),cur.getString(1),cur.getString(2),lon,lat,tz);
                    cur.close();
                }

/*			Geocoder geocoder = new Geocoder(getApplicationContext(),Locale.getDefault());
			if(goecoder.isPresent()) {
				try {
					List<Address> listAddresses = geocoder.getFromLocation(lat,lon,1);
					if(listAddresses!=null && listAddresses.size()>0) {
						Address addr;
						for(int i=0; i<listAddresses.size(); ++i) {
							addr = listAddresses.get(i);
							loc = addr.getLocality();
//Log.d(APP,TAG+".onCreate(loc: "+loc+")");
						}
						loc = listAddresses.get(0).getLocality();
					}
				} catch(Exception e) {
//Log.e(APP,TAG+".onCreate",e);
				}
			}*/

                Log.d(APP,TAG+".onCreate(done)");
            }
            setHoroscope(h);
        } catch(Exception e) {
            Log.e(APP,TAG+".onCreate",e);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);

        ArrayAdapter<CharSequence> adapter = new ArrayAdapter<CharSequence>(
            this,R.layout.actionbar_spinner_item,
            android.R.id.text1,getResources().getStringArray(R.array.actionbar_graph_views));

/*		ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(
				getSupportActionBar().getThemedContext(),
				R.array.actionbar_graph_views,
				R.layout.actionbar_spinner_item);*/

        adapter.setDropDownViewResource(R.layout.support_simple_spinner_dropdown_item);
        toolbarSpinner.setAdapter(adapter);
        toolbarSpinner.setOnItemSelectedListener(new OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent,View view,int pos,long id) {
                int y = 0;
                switch(pos) {
                    case 0:
                        y = wheelGraph.getTop();
                        break;
                    case 1:
                    case 2:
                        y = planets.getTop();
                        break;
                    case 3:
                    case 4:
                    case 5:
                        y = aspectTable.getTop();
                        break;
                    case 6:
                        y = aspectList.getTop();
                        break;
                    case 7:
                        y = aspectPatterns.getTop();
                        break;
                    case 8:
                        y = arabicParts.getTop();
                        break;
                }
                scroll.scrollTo(0,y);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        menuEdit = (MenuItem)menu.findItem(R.id.menu_edit);
        menuNew = (MenuItem)menu.findItem(R.id.menu_new);
        menuProfiles = (MenuItem)menu.findItem(R.id.menu_profiles);
        menuNew.setVisible(id==-1);
        menuEdit.setVisible(id!=-1);
        menuProfiles.setVisible(id==-1);
        menu.setGroupVisible(R.id.menu_charts_group,id!=-1);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch(item.getItemId()) {
            case R.id.menu_new:
                openEditProfile(-1);
                return true;

            case R.id.menu_edit:
                openEditProfile(id);
                return true;

            case R.id.menu_profiles:
                openProfiles();
                return true;

            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        switch(item.getItemId()) {
            case R.id.drawer_help:
                openHelp(R.string.text_help_horoscope);
                break;

            case R.id.drawer_new:
                openEditProfile(-1);
                break;

            case R.id.drawer_edit:
                openEditProfile(id);
                break;

            case R.id.drawer_profiles:
                openProfiles();
                break;

            default:
                return super.onNavigationItemSelected(item);
        }
//		item.setChecked(false);
        drawerLayout.closeDrawer(Gravity.LEFT);
        return true;
    }

    @Override
    public void onSymbolAttributeSelected(int id) {
    }

    @Override
    public void onSymbolSelected(long id) {
        String title = Symbol.getTitle(id);
        Log.e(APP,TAG+".onSymbolSelected("+title+")");
        shortToast(title);
        openText(-1,id);
    }

    @Override
    public void onSymbolSelected(Symbol s) {
    }

    @Override
    public void updateUserInterface() {
        super.updateUserInterface();

    }

    private void setHoroscope(Horoscope h) {
        if(h==null) return;

        int[] p = {
            _ASTRO_SUN_,
            _ASTRO_MOON_,
            _ASTRO_MERCURY_,
            _ASTRO_VENUS_,
            _ASTRO_MARS_,
            _ASTRO_JUPITER_,
            _ASTRO_SATURN_,
            _ASTRO_URANUS_,
            _ASTRO_NEPTUNE_,
            _ASTRO_PLUTO_,
            _ASTRO_TNNODE_,
            _ASTRO_CHIRON_,
            _ASTRO_ASCENDANT_,
            _ASTRO_MC_,
            -1};

        h.calculate(p,0);

        Log.d(APP,TAG+".setHoroscope(jd: "+h.getJulianDay()+", dt: "+h.getDeltaT()+", sd: "+h.getSiderealTime()+")");

        String info = "-";
        String n = h.getName();
        String l = h.getLocality();
        String c = h.getCountry();
        if(l==null || c==null) info = "<b>"+n+"</b>";
        else {
            if(n.length()+l.length()+c.length()>46) c = h.getCountryCode();
            info = String.format("<b>%1$s</b>, %2$s, %3$s",n,l,c);
        }
        textInfo.setText(CustomHtml.fromHtml(info,0,0.0f,null));

        for(int i = 0; i<horoscopeViews.length; ++i)
            horoscopeViews[i].setHoroscope(h);

        drawerEdit.setVisible(id!=-1);
    }
}
