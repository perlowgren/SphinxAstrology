package net.spirangle.sphinx.astro;

import static net.spirangle.sphinx.SphinxProperties.*;

import android.content.Intent;
import android.database.Cursor;
import android.util.Log;
import android.view.Gravity;
import android.view.MenuItem;

import net.spirangle.sphinx.BasicActivity;
import net.spirangle.sphinx.Horoscope;
import net.spirangle.sphinx.HttpClient;
import net.spirangle.sphinx.R;

import org.json.JSONObject;

import java.util.List;
import java.util.Map;

public abstract class AstroActivity extends BasicActivity {
    private static final String TAG = "AstroActivity";

    public AstroActivity() {
        super();
        AstroDB.getInstance();
    }

    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        if(super.onNavigationItemSelected(item)) return true;
        switch(item.getItemId()) {
            case R.id.drawer_help:
                openHelp(R.string.text_help_profiles);
                break;

            case R.id.drawer_settings:
                openSettings();
                break;

            case R.id.drawer_sign_in:
                google.signIn(this);
                break;

            case R.id.drawer_sign_out:
                google.signOut(this);
                break;

            default:
                return false;
        }
//		item.setChecked(false);
        drawerLayout.closeDrawer(Gravity.LEFT);
        return true;
    }

    @Override
    public void onActivityResult(int request,int result,Intent data) {
        super.onActivityResult(request,result,data);
        switch(request) {
            case ACTIVITY_HOROSCOPE:
            case ACTIVITY_PROFILES:
            case ACTIVITY_EDIT_PROFILE:
            case ACTIVITY_HELP:
            case ACTIVITY_SETTINGS:
                if(result==RESULT_OK) {
                    setResult(RESULT_OK,null);
                    finish();
                }
                break;
        }
    }

    @Override
    public void onInit() {
        super.onInit();
        try {
/*
//		db.exec("CREATE TABLE Database (_id INTEGER PRIMARY KEY AUTOINCREMENT,version INTEGER,flags INTEGER,updated INTEGER,created INTEGER)");
		db.exec("INSERT INTO Database (version,flags,updated,created) VALUES (0,0,0,strftime(\'%s\',\'now\'))");
//		db.exec("ALTER TABLE Profile RENAME TO P2");
//		db.exec("CREATE TABLE Profile (_id INTEGER PRIMARY KEY AUTOINCREMENT,categories TEXT,name TEXT,year INTEGER,month INTEGER,day INTEGER,"+
//		        "hour INTEGER,minute INTEGER,second REAL,locality TEXT,country TEXT,countryCode TEXT,longitude REAL,latitude REAL,"+
//		        "timeZone REAL,dst REAL,picture TEXT,sun REAL,moon REAL,ascendant REAL,flags INTEGER,updated INTEGER,created INTEGER)");
		db.exec("DROP INDEX Profile_Categories");
		db.exec("CREATE INDEX Profile_Categories ON Profile (categories)");
		db.exec("DROP INDEX Profile_Name");
		db.exec("CREATE INDEX Profile_Name ON Profile (name)");
//		db.exec("INSERT INTO Profile (categories,name,year,month,day,hour,minute,second,"+
//		        "locality,country,countryCode,longitude,latitude,timeZone,dst,picture,sun,moon,ascendant,"+
//		        "flags,updated,created) SELECT categories,name,year,month,day,hour,minute,second,"+
//		        "locality,country,countryCode,longitude,latitude,timeZone,dst,picture,sun,moon,ascendant,"+
//		        "flags,updated,created FROM P2");
*/

/*Log.d(APP,TAG+".init(year: "+1969+", month: "+1+", day: "+1+", days: "+Horoscope.Calendar.daysFromUnixEpoch(1969,1,1)+")");
Log.d(APP,TAG+".init(year: "+1569+", month: "+1+", day: "+1+", days: "+Horoscope.Calendar.daysFromUnixEpoch(1569,1,1)+")");
Log.d(APP,TAG+".init(year: "+1971+", month: "+1+", day: "+1+", days: "+Horoscope.Calendar.daysFromUnixEpoch(1971,1,1)+")");
Log.d(APP,TAG+".init(year: "+1972+", month: "+1+", day: "+1+", days: "+Horoscope.Calendar.daysFromUnixEpoch(1972,1,1)+")");
Log.d(APP,TAG+".init(year: "+1975+", month: "+1+", day: "+1+", days: "+Horoscope.Calendar.daysFromUnixEpoch(1975,1,1)+")");
Log.d(APP,TAG+".init(year: "+2016+", month: "+10+", day: "+15+", days: "+Horoscope.Calendar.daysFromUnixEpoch(2016,10,15)+")");
Log.d(APP,TAG+".init(year: "+2016+", month: "+10+", day: "+15+", days: "+(long)(Database.timestamp()/86400)+")");

java.util.Calendar c = java.util.Calendar.getInstance();
int y    = c.get(java.util.Calendar.YEAR);
int m     = c.get(java.util.Calendar.MONTH)+1;
int d     = c.get(java.util.Calendar.DAY_OF_MONTH);
int h    = c.get(java.util.Calendar.HOUR_OF_DAY);
int n     = c.get(java.util.Calendar.MINUTE);
double s  = (double)c.get(java.util.Calendar.SECOND);
double dst  = (double)c.get(java.util.Calendar.DST_OFFSET)/3600000.0;
double tz   = (double)c.get(java.util.Calendar.ZONE_OFFSET)/3600000.0;


Log.d(APP,TAG+".init(year: "+1969+", month: "+1+", day: "+1+", days: "+Horoscope.Calendar.secondsFromUnixEpoch(1969,1,1,0,0,0)+")");
Log.d(APP,TAG+".init(year: "+1569+", month: "+1+", day: "+1+", days: "+Horoscope.Calendar.secondsFromUnixEpoch(1569,1,1,0,0,0)+")");
Log.d(APP,TAG+".init(year: "+1971+", month: "+1+", day: "+1+", days: "+Horoscope.Calendar.secondsFromUnixEpoch(1971,1,1,0,0,0)+")");
Log.d(APP,TAG+".init(year: "+1972+", month: "+1+", day: "+1+", days: "+Horoscope.Calendar.secondsFromUnixEpoch(1972,1,1,0,0,0)+")");
Log.d(APP,TAG+".init(year: "+1975+", month: "+1+", day: "+1+", days: "+Horoscope.Calendar.secondsFromUnixEpoch(1975,1,1,0,0,0)+")");
Log.d(APP,TAG+".init(year: "+y+", month: "+m+", day: "+d+", days: "+Horoscope.Calendar.secondsFromUnixEpoch(y,m,d,h,n,(int)(s-((dst+tz)*3600.0)))+")");
Log.d(APP,TAG+".init(year: "+y+", month: "+m+", day: "+d+", days: "+Database.timestamp()+")");

y    = 1452;
m    = 4;
d    = 22;
h    = 21;
n    = 48;
s    = 0.0;
dst  = 0.0;
tz   = 0.0;

java.util.Calendar c1 = java.util.Calendar.getInstance();
c1.set(y,m,d,h,n,(int)s);

Log.d(APP,TAG+".init(year: "+y+", month: "+m+", day: "+d+", days: "+Horoscope.Calendar.secondsFromUnixEpoch(y,m,d,h,n,(int)(s-((dst+tz)*3600.0)))+")");
Log.d(APP,TAG+".init(year: "+y+", month: "+m+", day: "+d+", days: "+Database.timestamp()+")");*/

		/*int i,x,y,z;
//		long n;
		for(i=0; i<10; ++i)
			Log.d(APP,TAG+String.format(".init(profile key: %1$s)",getKey(KEY_PROFILE)));*/

		/*for(x=ASTRO_FIRE; x<=ASTRO_WATER; ++x) {
			n = Symbol.astrologyElement(x);
			Log.d(APP,TAG+String.format(".init('%1$s',4,%2$d,'%3$s'",getKey(KEY_TEXT),n,Symbol.getTitle(n)));
		}
		for(x=ASTRO_CARDINAL; x<=ASTRO_MUTABLE; ++x) {
			n = Symbol.astrologyQuality(x);
			Log.d(APP,TAG+String.format(".init('%1$s',4,%2$d,'%3$s'",getKey(KEY_TEXT),n,Symbol.getTitle(n)));
		}
		for(x=ASTRO_ARIES; x<=ASTRO_PISCES; ++x) {
			n = Symbol.astrologyZodiac(x);
			Log.d(APP,TAG+String.format(".init('%1$s',4,%2$d,'%3$s'",getKey(KEY_TEXT),n,Symbol.getTitle(n)));
		}
		for(x=ASTRO_SUN; x<=ASTRO_PLUTO; ++x) {
			n = Symbol.astrologyPlanet(x);
			Log.d(APP,TAG+String.format(".init('%1$s',4,%2$d,'%3$s'",getKey(KEY_TEXT),n,Symbol.getTitle(n)));
		}
		for(x=ASTRO_ASCENDANT; x<=ASTRO_12TH_HOUSE; ++x) {
			n = Symbol.astrologyHouse(x);
			Log.d(APP,TAG+String.format(".init('%1$s',4,%2$d,'%3$s'",getKey(KEY_TEXT),n,Symbol.getTitle(n)));
		}
		for(x=CONJUNCTION; x<=OPPOSITION; ++x) {
			n = Symbol.astrologyAspect(x);
			Log.d(APP,TAG+String.format(".init('%1$s',4,%2$d,'%3$s'",getKey(KEY_TEXT),n,Symbol.getTitle(n)));
		}*/
		/*for(x=ASTRO_SUN; x<=ASTRO_PLUTO; ++x) {
			for(y=ASTRO_ARIES; y<=ASTRO_PISCES; ++y) {
				n = Symbol.astrologyPlanetInZodiac(x,y);
				Log.d(APP,TAG+String.format(".init('%1$s',4,%2$d,'%3$s'",getKey(KEY_TEXT),n,Symbol.getTitle(n)));
			}
			for(y=ASTRO_ASCENDANT; y<=ASTRO_12TH_HOUSE; ++y) {
				n = Symbol.astrologyPlanetInHouse(x,y);
				Log.d(APP,TAG+String.format(".init('%1$s',4,%2$d,'%3$s'",getKey(KEY_TEXT),n,Symbol.getTitle(n)));
			}
		}*/
		/*x = ASTRO_ASCENDANT;
		for(y=ASTRO_ARIES; y<=ASTRO_PISCES; ++y) {
			n = Symbol.astrologyHouseInZodiac(x,y);
			Log.d(APP,TAG+String.format(".init('%1$s',4,%2$d,'%3$s'",getKey(KEY_TEXT),n,Symbol.getTitle(n)));
		}*/
		/*int p[] = {ASTRO_SUN,ASTRO_MOON,ASTRO_MERCURY,ASTRO_VENUS,ASTRO_MARS,ASTRO_JUPITER,ASTRO_SATURN,ASTRO_URANUS,ASTRO_NEPTUNE,ASTRO_PLUTO,ASTRO_ASCENDANT};
		int a[] = {CONJUNCTION,SEMISEXTILE,SEMISQUARE,SEXTILE,SQUARE,TRINE,OPPOSITION};

		for(x=0,i=0; x<p.length-1; ++x) {
			for(y=0; y<a.length; ++y) {
				for(z=x+1; z<p.length; ++z,++i) {
					if(p[z]!=ASTRO_ASCENDANT) n = Symbol.astrologyPlanetToPlanetAspect(p[x],p[z],a[y]);
					else n = Symbol.astrologyPlanetToHouseAspect(p[x],p[z],a[y]);
					Log.d(APP,TAG+String.format(".init('%1$s',4,%2$d,'%3$s'",getKey(KEY_TEXT,i),n,Symbol.getTitle(n)));
				}
				Thread.sleep(100l);
			}
		}*/

		/*String b36;
		long n;
		b36 = base36Encode(1234567);
		n = base36Decode(b36);
		Log.d(APP,TAG+String.format(".init(b36: %1$s, n: %2$d)",b36,n));
		b36 = base36Encode(2345678);
		n = base36Decode(b36);
		Log.d(APP,TAG+String.format(".init(b36: %1$s, n: %2$d)",b36,n));
		b36 = base36Encode(7654321);
		n = base36Decode(b36);
		Log.d(APP,TAG+String.format(".init(b36: %1$s, n: %2$d)",b36,n));
		b36 = base36Encode(78912365);
		n = base36Decode(b36);
		Log.d(APP,TAG+String.format(".init(b36: %1$s, n: %2$d)",b36,n));
		b36 = base36Encode(8765432);
		n = base36Decode(b36);
		Log.d(APP,TAG+String.format(".init(b36: %1$s, n: %2$d)",b36,n));*/


        } catch(Exception e) {
            Log.e(APP,TAG+".onInit",e);
        }
    }

    @Override
    public void spirangleSignIn() {
        Log.d(APP,TAG+".spirangleSignIn(google.tokenId: "+google.tokenId+")");
        final AstroDB db = AstroDB.getInstance();
        long t = 0;
        Cursor cur = db.query("SELECT updated FROM Database WHERE _id=1");
        if(cur.moveToFirst()) t = cur.getInt(0);
        final long time = t;
        HttpClient http = HttpClient.getInstance(this);
        http.authorizationGoogle(google.tokenId).contentTypeJSON()
            .get(URL_SPIRANGLE_API+"/status?timestamp="+time,
                 new HttpClient.RequestListener() {
                     @Override
                     public void result(Map<String,List<String>> headers,String data,int status,long id,Object object) {
//Log.d(APP,TAG+".spirangleSignIn(result: "+data+", status: "+status+")");
                         if(data!=null && data.length()>0) {
                             try {
                                 JSONObject json = new JSONObject(data);
                                 String s = json.optString("status");
                                 if(s.equals("OK")) {
                                     user.id = 0l;
                                     user.update(json);
                                     db.updateUser(user,0);
                                     int profiles = json.optInt("profiles",0);
                                     int texts = json.optInt("texts",0);
                                     if(profiles>0) db.downloadProfiles(time,0,0);
                                     if(texts>0) db.downloadTexts(time,0,0);
                                     return;
                                 } else if(s.equals("expired")) {
                                     google.silentSignIn(AstroActivity.this,null);
                                 } else {
                                     String m = json.optString("message",null);
                                     if(m!=null) {
                                         shortToast(s+" ["+status+"]: "+m);
                                         //									return;
                                     }
                                 }
                             } catch(Exception e) {
                                 Log.e(APP,TAG+".spirangleSignIn",e);
                             }
                         }
                         shortToast(R.string.toast_signin_failed);
                     }
                 });
    }

    public void openHoroscope(long id,int graph) {
        Horoscope h = HoroscopeActivity.loadHoroscope(id);
        openHoroscope(h,graph);
    }

    public void openHoroscope(Horoscope h,int graph) {
        Intent intent = new Intent(this,HoroscopeActivity.class);
        if(h!=null) intent.putExtra(EXTRA_RADIX1,h);
        intent.putExtra(EXTRA_GRAPH,graph);
        startActivityForResult(intent,ACTIVITY_HOROSCOPE);
    }

    public void openSynastry(long id1,long id2,int graph) {
        Horoscope h1 = HoroscopeActivity.loadHoroscope(id1);
        Horoscope h2 = HoroscopeActivity.loadHoroscope(id2);
        openSynastry(h1,h2,graph);
    }

    public void openSynastry(Horoscope h1,Horoscope h2,int graph) {
        Intent intent = new Intent(this,HoroscopeActivity.class);
        if(h1!=null) intent.putExtra(EXTRA_RADIX1,h1);
        if(h2!=null) intent.putExtra(EXTRA_RADIX2,h2);
        intent.putExtra(EXTRA_GRAPH,graph);
        startActivityForResult(intent,ACTIVITY_HOROSCOPE);
    }

    public void openProfiles() {
        Intent intent = new Intent(this,ProfilesActivity.class);
        startActivityForResult(intent,ACTIVITY_PROFILES);
    }

    public void openEditProfile(long id) {
        Intent intent = new Intent(this,EditProfileActivity.class);
        intent.putExtra(EXTRA_PROFILE,id);
        startActivityForResult(intent,ACTIVITY_EDIT_PROFILE);
    }

    public void openText(long uid,long id) {
        Intent intent = new Intent(this,TextActivity.class);
        intent.putExtra(EXTRA_USER,uid);
        intent.putExtra(EXTRA_SYMBOL,id);
        startActivityForResult(intent,ACTIVITY_TEXT);
    }

    public void openEditText(long sym,long id) {
        Intent intent = new Intent(this,EditTextActivity.class);
        intent.putExtra(EXTRA_SYMBOL,sym);
        intent.putExtra(EXTRA_TEXT,id);
        startActivityForResult(intent,ACTIVITY_EDIT_TEXT);
    }

    public void openHelp(int id) {
        Intent intent = new Intent(this,HelpActivity.class);
        intent.putExtra(EXTRA_HELP,id);
        startActivityForResult(intent,ACTIVITY_HELP);
    }

    public void openSettings() {
        Intent intent = new Intent(this,SettingsActivity.class);
        startActivityForResult(intent,ACTIVITY_SETTINGS);
    }
}


