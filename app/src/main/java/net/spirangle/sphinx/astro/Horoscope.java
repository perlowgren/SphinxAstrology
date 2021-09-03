package net.spirangle.sphinx.astro;

import static net.spirangle.sphinx.config.AstrologyProperties.*;
import static net.spirangle.sphinx.config.SphinxProperties.*;

import android.content.ContentValues;
import android.location.Address;
import android.os.Parcel;
import android.os.Parcelable;
import android.util.Log;

import net.spirangle.sphinx.db.Key;
import net.spirangle.sphinx.db.SphinxDatabase;

import org.json.JSONObject;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;


public class Horoscope implements Parcelable {
    private static final String TAG = Horoscope.class.getSimpleName();

    public static final int GREGORIAN_CALENDAR = 0;
    public static final int JULIAN_CALENDAR = 1;

    public static final int TIME_UNKNOWN = 0x00000100;
    public static final int BCE = 0x00000200;

    public static class Calendar {
        public int year;
        public int month;
        public int day;
        public int hour;
        public int minute;
        public double second;
        public int type;
        public double julianDay = 0.0;

        public static int daysFromUnixEpoch(int y,int m,int d) {
            y -= (m<=2? 1 : 0);
            int era = (y>=0? y : y-399)/400;
            int yoe = (y-era*400);                      // [0, 399]
            int doy = (153*(m+(m>2? -3 : 9))+2)/5+d-1;  // [0, 365]
            int doe = yoe*365+yoe/4-yoe/100+doy;        // [0, 146096]
            return era*146097+doe-719468;
        }

        public static long secondsFromUnixEpoch(int y,int m,int d,int h,int n,int s) {
            return (long)daysFromUnixEpoch(y,m,d)*86400L+(h*3600L+n*60L+s);
        }

        public Calendar(int y,int m,int d,int h,int n,double s,int t) {
            year = y;
            month = m;
            day = d;
            hour = h;
            minute = n;
            second = s;
            type = t;
        }

        public void set(int y,int m,int d,int h,int n,double s,int t) {
            year = y;
            month = m;
            day = d;
            hour = h;
            minute = n;
            second = s;
            type = t;
        }

        @Override
        public String toString() {
            return formatDateTime();
        }

        public String formatDate() {
            return String.format(Locale.ENGLISH,"%d-%02d-%02d",year,month,day);
        }

        public String formatFormalDate() {
            String monthName = Symbol.getMonthName(month);
            return String.format(Locale.ENGLISH,"%d %s %d",day,monthName,year);
        }

        public String formatTime() {
            int s = (int)second;
            return String.format(Locale.ENGLISH,"%1$02d:%2$02d"+(s>0? ":%3$02d" : ""),hour,minute,s);
        }

        public String formatTime(double tz) {
            return String.format(Locale.ENGLISH,"%1$02d:%2$02d %3$+.1f",hour,minute,tz);
        }

        public String formatDateTime() {
            return String.format(Locale.ENGLISH,"%d-%02d-%02d %02d:%02d",year,month,day,hour,minute);
        }

        public long secondsFromUnixEpoch() {
            return secondsFromUnixEpoch(year,month,day,hour,minute,(int)second);
        }

        public long secondsFromUnixEpoch(double tz,double dst) {
            Log.d(APP,TAG+".secondsFromUnixEpoch(tz: "+tz+", dst: "+dst+")");
            int s = (int)(second-(tz+dst)*3600.0);
            return secondsFromUnixEpoch(year,month,day,hour,minute,s);
        }
    }

    public class Planet {
        public final int index;
        public final int id;
        public final int sign;
        public final double longitude;
        public final double absoluteLongitude;
        public final boolean retrograde;
        public final int house;
        public final int factors;
        public final long symbolId;

        private Planet(int n,int i,int s,double l,int h,int f,boolean r) {
            if(l<0.0) l += 360.0;
            else if(l>=360.0) l -= 360.0;
            index = n;
            id = _ASTRO_PLANET_[i];
            sign = _ASTRO_ZODIAC_[s];
            longitude = l-30.0*s;
            absoluteLongitude = l;
            retrograde = r;
            house = _ASTRO_HOUSE_[h];
            if(f>0) {
                int f1, fn;
                for(f1 = 0,fn = 0; f>0; ++f1,f >>= 1)
                    if((f&1)==1) fn |= _ASTRO_FACTOR_[f1];
                factors = fn;
            } else {
                factors = 0;
            }
            symbolId = getSymbolId();
        }

        private long getSymbolId() {
            switch(id&Symbol.Attribute.CONCEPT_MASK) {
                case ASTRO_PLANET:
                    return Symbol.astrologyPlanetInZodiac(id,sign);
                case ASTRO_MINOR_PLANET:
                    return Symbol.astrologyMinorPlanetInZodiac(id,sign);
                case ASTRO_FIXED_STAR:
                    return Symbol.astrologyFixedStarInZodiac(id,sign);
                case ASTRO_POINT:
                    return Symbol.makeId(Symbol.Concept.ASTRO_POINT,id);
                case ASTRO_LOT:
                    return Symbol.makeId(Symbol.Concept.ASTRO_LOT,id);
                case ASTRO_HOUSE:
                    return Symbol.astrologyHouseInZodiac(id,sign);
                default:
                    return -1l;
            }
        }
    }

    public class House {
        public final int id;
        public final int sign;
        public final double cusp;
        public final double absoluteCusp;
        public final double decl;
        public final long symbolId;

        private House(int h,int s,double c,double d) {
            if(c<0.0) c += 360.0;
            else if(c>=360.0) c -= 360.0;
            id = _ASTRO_HOUSE_[h];
            sign = _ASTRO_ZODIAC_[s];
            cusp = c-30.0*s;
            absoluteCusp = c;
            decl = d;
            symbolId = getSymbolId();
        }

        private long getSymbolId() {
            return Symbol.astrologyHouseInZodiac(id,sign);
        }
    }

    public class Aspect {
        public int x;
        public int y;
        public int type;
        public double orb;
        public long symbolId = -1l;

        private Aspect(int t,double o) {
            type = t;
            orb = o;
        }

        private void set(int x,int y,int t,double o) {
            this.x = x;
            this.y = y;
            type = _ASTRO_ASPECT_[t];
            orb = o;
            symbolId = getSymbolId();
//Log.d(APP,TAG+".Aspect.set(symbolId: "+symbolId+")");
        }

        private long getSymbolId() {
            int p1 = planets[x].id;
            int p2 = planets[y].id;
//Log.d(APP,TAG+".Aspect.getSymbolId(p1: "+p1+", p2: "+p2+", type: "+type+")");
            switch(p1&Symbol.Attribute.CONCEPT_MASK) {
                case ASTRO_PLANET:
                    switch(p2&Symbol.Attribute.CONCEPT_MASK) {
                        case ASTRO_PLANET:
                            return Symbol.astrologyPlanetToPlanetAspect(p1,p2,type);
                        case ASTRO_MINOR_PLANET:
                            return Symbol.astrologyPlanetToMinorPlanetAspect(p1,p2,type);
                        case ASTRO_FIXED_STAR:
                            return Symbol.astrologyPlanetToFixedStarAspect(p1,p2,type);
                        case ASTRO_HOUSE:
                            return Symbol.astrologyPlanetToHouseAspect(p1,p2,type);
                        default:
                            return -1l;
                    }


                case ASTRO_MINOR_PLANET:
                    switch(p2&Symbol.Attribute.CONCEPT_MASK) {
                        case ASTRO_PLANET:
                            return Symbol.astrologyPlanetToMinorPlanetAspect(p2,p1,type);
                        case ASTRO_MINOR_PLANET:
                            return Symbol.astrologyMinorPlanetToMinorPlanetAspect(p1,p2,type);
                        case ASTRO_FIXED_STAR:
                            return Symbol.astrologyMinorPlanetToFixedStarAspect(p1,p2,type);
                        case ASTRO_HOUSE:
                            return Symbol.astrologyMinorPlanetToHouseAspect(p1,p2,type);
                        default:
                            return -1l;
                    }

                case ASTRO_FIXED_STAR:
                    switch(p2&Symbol.Attribute.CONCEPT_MASK) {
                        case ASTRO_PLANET:
                            return Symbol.astrologyPlanetToFixedStarAspect(p2,p1,type);
                        case ASTRO_MINOR_PLANET:
                            return Symbol.astrologyMinorPlanetToFixedStarAspect(p2,p1,type);
                        case ASTRO_FIXED_STAR:
                            return Symbol.astrologyFixedStarToFixedStarAspect(p1,p2,type);
                        case ASTRO_HOUSE:
                            return Symbol.astrologyFixedStarToHouseAspect(p1,p2,type);
                        default:
                            return -1l;
                    }

//				case ASTRO_POINT:
//				case ASTRO_LOT:

                case ASTRO_HOUSE:
                    switch(p2&Symbol.Attribute.CONCEPT_MASK) {
                        case ASTRO_PLANET:
                            return Symbol.astrologyPlanetToHouseAspect(p2,p1,type);
                        case ASTRO_MINOR_PLANET:
                            return Symbol.astrologyMinorPlanetToHouseAspect(p2,p1,type);
                        case ASTRO_FIXED_STAR:
                            return Symbol.astrologyFixedStarToHouseAspect(p2,p1,type);
//						case ASTRO_HOUSE:         return Symbol.astrologyPlanetToHouseAspect(p1,p2,type);
                        default:
                            return -1l;
                    }

                default:
                    return -1l;
            }
        }
    }

    public class AspectPattern {
        public final int index;
        public final int type;
        public final int[] planets;

        private AspectPattern(int n,int t,int[] p) {
            int[] p1 = new int[p.length];
            for(int i = 0; i<p.length; ++i)
                p1[i] = _ASTRO_PLANET_[p[i]];
            index = n;
            type = _ASTRO_ASPECT_PATTERN_[t];
            planets = p1;
        }
    }

    protected long id = -1l;
    protected Key profileKey = null;
    protected String name = null;
    protected Calendar time = null;
    protected Calendar gmt = null;
    protected String locality = null;
    protected String country = null;
    protected String countryCode = null;
    protected double longitude = 0.0;
    protected double latitude = 0.0;
    protected double timeZone = 0.0;
    protected double dst = 0.0;

    protected double siderealTime = 0.0;
    protected double deltaT = 0.0;

    protected int naspects = 0;
    protected int hsystem = 0;
    protected int flags = 0;
    protected int style = 0;

    private int calculatePlanetsTime = 0;
    private int castHoroscopeTime = 0;
    private int transferNativeDataTime = 0;
    private int calculateTime = 0;

    protected Planet[] planets = null;
    protected House[] houses = null;
    protected Aspect[] aspects = null;
    protected AspectPattern[] aspectPatterns = null;

    private int[] iplanets = null;
    private int isun = -1;
    private int imoon = -1;
    private int imer = -1;
    private int iven = -1;
    private int imar = -1;
    private int ijup = -1;
    private int isat = -1;
    private int iura = -1;
    private int inep = -1;
    private int iplu = -1;
    private int iasc = -1;
    private int imc = -1;
    private int irulpl = -1;
    private int irulh = -1;

    private double mphase = 0.0;
    private double mphased = 0.0;

    public Horoscope() {
        this((String)null);
    }

    public Horoscope(String nm) {
        int y, m, d, h, n;
        double s, dst, tz;
        java.util.Calendar c = java.util.Calendar.getInstance();
        y = c.get(c.YEAR);
        m = c.get(c.MONTH)+1;
        d = c.get(c.DAY_OF_MONTH);
        h = c.get(c.HOUR_OF_DAY);
        n = c.get(c.MINUTE);
        s = (double)c.get(c.SECOND)+((double)c.get(c.MILLISECOND)/1000.0);
        dst = (double)c.get(c.DST_OFFSET)/3600000.0;
        tz = 0.0;//(double)c.get(c.ZONE_OFFSET)/3600000.0;
        name = nm;
        time = new Calendar(y,m,d,h,n,s,GREGORIAN_CALENDAR);
        gmt = new Calendar(y,m,d,h,n,s,GREGORIAN_CALENDAR);
        flags = time.type;
        setDaylightSavingTime(dst);
        setLocation(null,null,null,GMT_LON,GMT_LAT,tz);
        if(name==null) name = getDateTimeString();
    }

    public Horoscope(int y,int m,int d,int h,int n,double s) {
        this(-1l,null,null,y,m,d,h,n,s,GREGORIAN_CALENDAR);
    }

    public Horoscope(String nm,int y,int m,int d,int h,int n,double s) {
        this(-1l,null,nm,y,m,d,h,n,s,GREGORIAN_CALENDAR);
    }

    public Horoscope(long i,Key k,String nm,int y,int m,int d,int h,int n,double s) {
        this(i,k,nm,y,m,d,h,n,s,GREGORIAN_CALENDAR);
    }

    public Horoscope(long i,Key k,String nm,int y,int m,int d,int h,int n,double s,int t) {
        id = i;
        profileKey = k;
        name = nm;
        time = new Calendar(y,m,d,h,n,s,t);
        gmt = new Calendar(y,m,d,h,n,s,t);
        flags = time.type;
        setLocation(null,null,null,GMT_LON,GMT_LAT,0.0);
        if(name==null) name = getDateTimeString();
    }

    private Horoscope(Parcel in) {
        int y, m, d, h, n, t;
        long k;
        double s;
        id = in.readLong();
        k = in.readLong();
        profileKey = k==-1l? null : new Key(k);
        name = in.readString();
        y = in.readInt();
        m = in.readInt();
        d = in.readInt();
        h = in.readInt();
        n = in.readInt();
        s = in.readDouble();
        t = in.readInt();
        time = new Calendar(y,m,d,h,n,s,t);
        gmt = new Calendar(y,m,d,h,n,s,t);
        locality = in.readString();
        country = in.readString();
        countryCode = in.readString();
        longitude = in.readDouble();
        latitude = in.readDouble();
        timeZone = in.readDouble();
        dst = in.readDouble();
        hsystem = in.readInt();
        flags = in.readInt();
        style = in.readInt();
        Log.d(APP,TAG+"("+y+"-"+m+"-"+d+" "+h+":"+n+":"+(int)s+
                  ", "+locality+" ["+country+"], "+longitude+", "+latitude+", "+timeZone+", "+dst+")");
    }

    @Override
    public void writeToParcel(Parcel out,int flags) {
        out.writeLong(id);
        out.writeLong(profileKey==null? -1l : profileKey.id);
        out.writeString(name);
        out.writeInt(time.year);
        out.writeInt(time.month);
        out.writeInt(time.day);
        out.writeInt(time.hour);
        out.writeInt(time.minute);
        out.writeDouble(time.second);
        out.writeInt(time.type);
        out.writeString(locality);
        out.writeString(country);
        out.writeString(countryCode);
        out.writeDouble(longitude);
        out.writeDouble(latitude);
        out.writeDouble(timeZone);
        out.writeDouble(dst);
        out.writeInt(hsystem);
        out.writeInt(flags);
        out.writeInt(style);
    }

    public JSONObject getJSONObject(long cat1,long cat2) {
        String ename = JSONObject.quote(name);
        Map<String,Object> params = new HashMap<>();
        params.put("categories",cat1+":"+cat2);
        params.put("name",ename);
        params.put("time",String.format(Locale.ENGLISH,"%1$s%2$d-%3$02d-%4$02d %5$02d:%6$02d:%7$02d%8$s",
                                        (flags&BCE)==BCE? "BCE " : "",time.year,time.month,time.day,
                                        time.hour,time.minute,(int)time.second,time.type==JULIAN_CALENDAR? " J" : ""));
        params.put("longitude",Math.round(longitude*1000000.0));
        params.put("latitude",Math.round(latitude*1000000.0));
        params.put("timeZone",Math.round(timeZone*3600.0));
        params.put("dst",Math.round(dst*3600.0));
        params.put("sun",(isun>-1? Math.round(planets[isun].absoluteLongitude*1000000.0) : 0L));
        params.put("moon",(imoon>-1? Math.round(planets[imoon].absoluteLongitude*1000000.0) : 0L));
        params.put("ascendant",(iasc>-1? Math.round(planets[iasc].absoluteLongitude*1000000.0) : 0L));
        params.put("picture","");
        params.put("flags",flags);
        return new JSONObject(params);
    }

    public void setId(long i) { id = i; }

    public long getId() { return id; }

    public void setProfileKey(Key k) { profileKey = k; }

    public Key getProfileKey() { return profileKey; }

    public String getName() { return name; }

    public String getDateString() { return time.formatDate(); }

    public String getFormalDateString() { return time.formatFormalDate(); }

    public String getTimeString() { return time.formatTime(); }

    public String getTimeAndTimeZoneString() { return time.formatTime(timeZone+dst); }

    public String getDateTimeString() { return time.formatDateTime(); }

    public void setLocation(Address addr,double tz) {
        locality = addr.getLocality();
        country = addr.getCountryName();
        countryCode = addr.getCountryCode();
        longitude = addr.getLongitude();
        latitude = addr.getLatitude();
        timeZone = tz;
    }

    public void setLocation(String ln,String cn,String cc,double lon,double lat,double tz) {
        locality = ln;
        country = cn;
        countryCode = cc;
        longitude = lon;
        latitude = lat;
        timeZone = tz;
    }

    public void setLocation(String ln,String cn,String cc,String lon,String lat,double tz) {
        locality = ln;
        country = cn;
        countryCode = cc;
        longitude = Coordinate.valueOf(lon);
        latitude = Coordinate.valueOf(lat);
        timeZone = tz;
    }

    public void setDaylightSavingTime(double d) {
        dst = d;
    }

    public void setTimeUnknown(boolean timeUnknown) {
        if(timeUnknown) flags |= TIME_UNKNOWN;
        else flags &= ~TIME_UNKNOWN;
    }

    public void setBCE(boolean bce) {
        if(bce) {
            if(time.year<0) {
                if((flags&BCE)==0) flags |= BCE;
            } else {
                flags |= BCE;
                time.year = -time.year;
            }
        } else {
            if(time.year>=0) {
                if((flags&BCE)==BCE) flags &= ~BCE;
            } else {
                flags &= ~BCE;
                time.year = -time.year;
            }
        }
    }

    public String getLocality() { return locality; }

    public String getCountry() { return country; }

    public String getCountryCode() { return countryCode; }

    public double getLongitude() { return longitude; }

    public double getLatitude() { return latitude; }

    public String getLongitudeString() { return Coordinate.formatLongitudeGrade(longitude); }

    public String getLatitudeString() { return Coordinate.formatLatitudeGrade(latitude); }

    public int getHouseSystem() { return hsystem; }

    public double getJulianDay() { return gmt.julianDay; }

    public double getDeltaT() { return deltaT; }

    public double getSiderealTime() { return siderealTime; }

    public void setStyle(int s) { style = s; }

    public int getStyle() { return style; }

    public void putContentValues(ContentValues v) {
        v.put(SphinxDatabase.TableProfile.name,name);
        v.put(SphinxDatabase.TableProfile.year,time.year);
        v.put(SphinxDatabase.TableProfile.month,time.month);
        v.put(SphinxDatabase.TableProfile.day,time.day);
        v.put(SphinxDatabase.TableProfile.hour,time.hour);
        v.put(SphinxDatabase.TableProfile.minute,time.minute);
        v.put(SphinxDatabase.TableProfile.second,(int)time.second);
        v.put(SphinxDatabase.TableProfile.longitude,(int)Math.round(longitude*1000000.0));
        v.put(SphinxDatabase.TableProfile.latitude,(int)Math.round(latitude*1000000.0));
        v.put(SphinxDatabase.TableProfile.timeZone,(int)Math.round(timeZone*3600.0));
        v.put(SphinxDatabase.TableProfile.dst,(int)Math.round(dst*3600.0));
        v.put(SphinxDatabase.TableProfile.sun,(isun>-1? (int)Math.round(planets[isun].absoluteLongitude*1000000.0) : 0));
        v.put(SphinxDatabase.TableProfile.moon,(imoon>-1? (int)Math.round(planets[imoon].absoluteLongitude*1000000.0) : 0));
        v.put(SphinxDatabase.TableProfile.ascendant,(iasc>-1? (int)Math.round(planets[iasc].absoluteLongitude*1000000.0) : 0));
        v.put(SphinxDatabase.TableProfile.flags,flags);
    }

//	public void countElements(int &f,int &e,int &a,int &w) { int n[4];horoscope_elements(h,n);f = n[0],e = n[1],a = n[2],w = n[3]; }
//	public void countQualities(int &c,int &f,int &m) { int n[3];horoscope_qualities(h,n);c = n[0],f = n[1],m = n[2]; }
//	public void countEnergies(int &m,int &f) { int n[2];horoscope_energies(h,n);m = n[0],f = n[1]; }

    public int sun() { return isun; }

    public int moon() { return imoon; }

    public int mercury() { return imer; }

    public int venus() { return iven; }

    public int mars() { return imar; }

    public int jupiter() { return ijup; }

    public int saturn() { return isat; }

    public int uranus() { return iura; }

    public int neptune() { return inep; }

    public int pluto() { return iplu; }

    public int ascendant() { return iasc; }

    public int mc() { return imc; }

    public int planets() { return planets.length; }

    public int planetId(int p) { return planets[p].id; }

    public double planetLongitude(int p) { return planets[p].longitude; }

    public double planetAbsoluteLongitude(int p) { return planets[p].absoluteLongitude; }

    public int planetSign(int p) { return planets[p].sign; }

    public int planetHouse(int p) { return planets[p].house; }

    public long planetSymbolId(int p) { return planets[p].symbolId; }

    public boolean planetIsRetrograde(int p) { return planets[p].retrograde; }

    public boolean planetIsMutualReceptive(int p) { return (planets[p].factors&MRECEPTIVE)==MRECEPTIVE; }

    public boolean planetIsRising(int p) { return (planets[p].factors&ASCENDING)==ASCENDING; }

    public boolean planetIsAngular(int p) { return (planets[p].factors&ANGULAR)==ANGULAR; }

    public int houses() { return houses.length; }

    public double houseCusp(int h) { return houses[h].cusp; }

    public double houseAbsoluteCusp(int h) { return houses[h].absoluteCusp; }

    public int houseSign(int h) { return houses[h].sign; }

    public long houseSymbolId(int h) { return houses[h].symbolId; }

    public int aspects() { return naspects; }

    public int aspect(int a) { return aspects[a].type; }

    public int aspect(int p1,int p2) { return aspects[p1+p2*planets.length].type; }

    public double aspectOrb(int a) { return aspects[a].orb; }

    public double aspectOrb(int p1,int p2) { return aspects[p1+p2*planets.length].orb; }

    public int aspectIndex(int p1,int p2) { return p1+p2*planets.length; }

    public int aspectPlanet1(int a) { return aspects[a].x; }

    public int aspectPlanet2(int a) { return aspects[a].y; }

    public long aspectSymbolId(int a) { return aspects[a].symbolId; }

    public long aspectSymbolId(int p1,int p2) { return aspects[p1+p2*planets.length].symbolId; }

    public int aspectPatterns() { return aspectPatterns.length; }

    public int aspectPattern(int i) { return aspectPatterns[i].type; }

    public int[] aspectPatternPlanets(int i) { return aspectPatterns[i].planets; }

    public int aspectShape() { return -1; }

    public int rulingPlanet() { return irulpl; }

    public int rulingHouse() { return irulh; }

    public double moonPhase() { return mphase; }

    public double moonPhaseDegreeDays() { return mphased; }

    @Override
    public String toString() {
        String text = "";
		/*Planet p,p2;
		House h;
		Aspect a;
		AspectPattern ap;
		int i,x,y;

		text += "Time to calculate planets: "+calculatePlanetsTime+" microseconds\n"+
		        "Time to cast horoscope: "+castHoroscopeTime+" microseconds\n"+
		        "Time to transfer native data: "+transferNativeDataTime+" microseconds\n"+

		        "\nHoroscope: "+gmt.formatDateTime()+" GMT\n"+
		        "           "+getLongitudeString()+"  "+getLatitudeString()+"\n";

		text += String.format(Locale.ENGLISH,"Julian Day: %.6f\nDeltaT: %.2f\nSidereal Time: %s\n",
		                      (gmt.julianDay+deltaT/86400.0),deltaT,formatHMS(siderealTime*24.0,':',24,""));

		text += String.format(Locale.ENGLISH,"\n%-16s%-13s%-12s%s\n","Planet:","Longitude:","Sign:","House:");
		for(i=0; i<planets.length; ++i) {
			p = planets[i];
			text += String.format(Locale.ENGLISH,"%-16s%10s%-3s%-12s%d\n",
			                      planetName[p.id],formatHMS(p.longitude-((double)p.sign*30.0),'°',30,""),
			                      (p.retrograde? "r" : ""),zodiacName[p.sign],p.house);
		}

		text += String.format(Locale.ENGLISH,"\n%-16s%-13s%s\n","House:","Longitude:","Sign:");
		for(i=0; i<12; ++i) {
			h = houses[i];
			text += String.format(Locale.ENGLISH,"%2d%14s%10s%3s%s\n",
			                      (i+1),"",formatHMS(h.cusp-((double)h.sign*30.0),'°',30,""),"",zodiacName[h.sign]);
		}

		text += String.format(Locale.ENGLISH,"\n%-16s%-13s%s\n","Aspect:","Orb:","Planets:");
		for(x=0; x<planets.length; ++x)
			for(y=x+1; y<planets.length; ++y) {
				a = aspects[x+y*planets.length];
				if(a!=null && a.type!=-1) {
					p = planets[x];
					p2 = planets[y];
					text += String.format(Locale.ENGLISH,"%-16s%10s%3s%s / %s\n",aspectName[a.type],formatHMS(a.orb,'°',360,""),"",planetName[p.id],planetName[p2.id]);
				}
			}

		text += "\nAspect Patterns:\n";
		for(i=0; i<aspectPatterns.length; ++i) {
			ap = aspectPatterns[i];
			text += aspectPatternName[ap.type]+": ";
			for(x=0; x<ap.planets.length; ++x) {
				if(x>0) text += ", ";
				text += planetName[ap.planets[x]];
			}
			text += "\n";
		}*/

        return text;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public static final Parcelable.Creator<Horoscope> CREATOR = new Parcelable.Creator<Horoscope>() {
        @Override
        public Horoscope createFromParcel(Parcel in) { return new Horoscope(in); }

        @Override
        public Horoscope[] newArray(int size) { return new Horoscope[size]; }
    };

	/*private void makePlanets(int n,int[] i1,int[] i2) {
		int i;
		planets   = new Planet[n];
		houses    = new House[12];
		aspects   = new Aspect[n*n];
		naspects  = 0;
		for(i=n*n-1; i>=0; --i)
			aspects[i] = new Aspect(-1,0.0);
		iplanets  = i1;
		isun      = i2[0];
		imoon     = i2[1];
		imer      = i2[2];
		iven      = i2[3];
		imar      = i2[4];
		ijup      = i2[5];
		isat      = i2[6];
		iura      = i2[7];
		inep      = i2[8];
		iplu      = i2[9];
		iasc      = i2[10];
		imc       = i2[11];
		irulpl    = i2[12];
		irulh     = i2[13];
	}

	private void makeAspectPatterns(int n) {
		aspectPatterns = new AspectPattern[n];
	}

	private void setPlanet(int n,int i,int s,double l,int h,int f,boolean r) {
		if(planets!=null)
			planets[n] = new Planet(n,i,s,l,h,f,r);
	}

	private void setHouse(int n,int s,double c,double d) {
		if(houses!=null)
			houses[n] = new House(n,s,c,d);
	}

	private void setAspect(int x,int y,int t,double o) {
//Log.d(APP,"setAspect("+x+", "+y+", "+t+", "+o+" ["+planets.length+"])");
		if(t!=-1 && planets!=null && aspects!=null) {
			aspects[x+y*planets.length].set(x,y,t,o);
			aspects[y+x*planets.length].set(y,x,t,o);
			++naspects;
		}
	}

	private void setAspectPattern(int n,int t,int[] p) {
		if(aspectPatterns!=null)
			aspectPatterns[n] = new AspectPattern(n,t,p);
	}*/

    public void calculate(int[] planets,int st) {
        long t1 = System.currentTimeMillis()*1000;

        int n;
        int[] i = new int[6+planets.length];
        i[0] = time.year;
        i[1] = time.month;
        i[2] = time.day;
        i[3] = time.hour;
        i[4] = time.minute;
        i[5] = time.type;
        for(n = 0; n<planets.length; ++n)
            i[6+n] = planets[n];
        double[] d = new double[5];
        d[0] = time.second;
        d[1] = longitude;
        d[2] = latitude;
        d[3] = timeZone;
        d[4] = dst;
        calculateJNI(i,d);

        long t2 = System.currentTimeMillis()*1000;
        calculateTime = (int)(t2-t1);
        Log.d(APP,TAG+".calculate(calculatePlanetsTime: "+calculatePlanetsTime+", castHoroscopeTime: "+castHoroscopeTime+", transferNativeDataTime: "+transferNativeDataTime+" ("+(calculateTime-calculatePlanetsTime-castHoroscopeTime)+"), calculateTime: "+calculateTime+")");
    }

    public void update(int[] i,double[] d) {
        int n, x = 0, y = 0, x1, y1, t;
        int[] v;

        Log.d(APP,TAG+".update(i: "+i.length+", d: "+d.length+")");

        time.julianDay = d[y+0];
        gmt.julianDay = d[y+0];
        gmt.year = i[x+0];
        gmt.month = i[x+1];
        gmt.day = i[x+2];
        gmt.hour = i[x+3];
        gmt.minute = i[x+4];
        gmt.second = d[y+1];
        gmt.type = i[x+5];
        x += 6;
        y += 2;

        siderealTime = d[y+0];
        deltaT = d[y+1];
        style = i[x+0];
        hsystem = i[x+1];
        x += 2;
        y += 2;

        mphase = d[y+0];
        mphased = d[y+1];
        y += 2;

        n = i[x+0];
        planets = new Planet[n];
        houses = new House[12];

        n = n*n;
        aspects = new Aspect[n];
        for(x1 = 0; x1<n; ++x1)
            aspects[x1] = new Aspect(-1,0.0);

        n = i[x+1];
        aspectPatterns = new AspectPattern[n];

        n = i[x+2];
        iplanets = new int[n];
        for(x1 = 0; x1<n; ++x1)
            iplanets[x1] = i[x+3+x1];
        x += 3+x1;

        isun = i[x+0];
        imoon = i[x+1];
        imer = i[x+2];
        iven = i[x+3];
        imar = i[x+4];
        ijup = i[x+5];
        isat = i[x+6];
        iura = i[x+7];
        inep = i[x+8];
        iplu = i[x+9];
        iasc = i[x+10];
        imc = i[x+11];
        irulpl = i[x+12];
        irulh = i[x+13];
        x += 14;

        for(x1 = 0; x1<planets.length; ++x1,x += 5,y += 1)
            planets[x1] = new Planet(x1,i[x+0],i[x+1],d[y+0],i[x+2],i[x+3],(i[x+4]!=0));

        for(x1 = 0; x1<houses.length; ++x1,x += 1,y += 2)
            houses[x1] = new House(x1,i[x+0],d[y+0],d[y+1]);

        naspects = 0;
        for(x1 = 0; x1<planets.length; ++x1)
            for(y1 = x1+1; y1<planets.length; ++y1,++x,++y) {
                t = i[x];
                if(t!=-1) {
                    aspects[x1+y1*planets.length].set(x1,y1,t,d[y]);
                    aspects[y1+x1*planets.length].set(y1,x1,t,d[y]);
                    ++naspects;
                }
            }

        for(x1 = 0; x1<aspectPatterns.length; ++x1,x += 2+n) {
            t = i[x+0];
            n = i[x+1];
            v = new int[n];
            for(y1 = 0; y1<n; ++y1)
                v[y1] = i[x+2+y1];
            aspectPatterns[x1] = new AspectPattern(x1,t,v);
        }

        calculatePlanetsTime = i[x+0];
        castHoroscopeTime = i[x+1];
        transferNativeDataTime = i[x+2];
        x += 3;

        Log.d(APP,TAG+".update(x: "+x+", y: "+y+")");
    }

    private native void calculateJNI(int[] i,double[] d);

    static {
        System.loadLibrary("astro");
    }
}
