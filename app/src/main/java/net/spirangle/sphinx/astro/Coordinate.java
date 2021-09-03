package net.spirangle.sphinx.astro;


import java.util.Locale;

public class Coordinate {
    public static final char DEG = '°';
    public static final char APOS = '\'';
    public static final char QUOT = '"';

    public static char E = 'E';
    public static char W = 'W';
    public static char N = 'N';
    public static char S = 'S';

    public static final void setDirections(String dirs) {
        dirs = dirs.toUpperCase();
        E = dirs.charAt(0);
        W = dirs.charAt(1);
        N = dirs.charAt(2);
        S = dirs.charAt(3);
    }

    public static double valueOf(String str) {
        return new Coordinate(str).grade;
    }

    public static String formatHMS(char d,double g) {
        return new Coordinate(d,g).formatHMS();
    }

    public static String formatHM(char d,double g) {
        return new Coordinate(d,g).formatHM();
    }

    public static String formatHDMS(char d,double g) {
        return new Coordinate(d,g).formatHDMS();
    }

    public static String formatHDM(char d,double g) {
        return new Coordinate(d,g).formatHDM();
    }

    public static String formatHMSD(char d,double g) {
        return new Coordinate(d,g).formatHMSD();
    }

    public static String formatHMD(char d,double g) {
        return new Coordinate(d,g).formatHMD();
    }

    public static String formatLongitude(double g) {
        return new Coordinate(g<0.0? W : E,g<0.0? -g : g).formatHMS();
    }

    public static String formatLatitude(double g) {
        return new Coordinate(g<0.0? S : N,g<0.0? -g : g).formatHMS();
    }

    public static String formatLongitudeShort(double g) {
        return new Coordinate(g<0.0? W : E,g<0.0? -g : g).formatHM();
    }

    public static String formatLatitudeShort(double g) {
        return new Coordinate(g<0.0? S : N,g<0.0? -g : g).formatHM();
    }

    public static String formatLongitudeGrade(double g) {
        return new Coordinate(g<0.0? W : E,g<0.0? -g : g).formatHDM();
    }

    public static String formatLatitudeGrade(double g) {
        return new Coordinate(g<0.0? S : N,g<0.0? -g : g).formatHDM();
    }

    public static String formatHMS(double g,char sep,int d,String suffix) {
        Coordinate c = new Coordinate(E,g,d);
        String ms = "\'", ss = "\"";
        if(sep==':') {
            ms = ""+sep;
            ss = "";
        }
        if(suffix==null) suffix = "";
        return String.format(Locale.getDefault(),"%d%c%02d%s%02d%s%s",c.hour,sep,c.minute,ms,c.second,ss,suffix);
    }

    public static String formatHM(double g,char sep,int d,String suffix) {
        Coordinate c = new Coordinate(E,g,d);
        if(suffix==null) suffix = "";
        return String.format(Locale.getDefault(),"%d%c%02d%s",c.hour,sep,c.minute,suffix);
    }

    public final double grade;
    public final char dir;
    public final int day;
    public final int hour;
    public final int minute;
    public final int second;

    public Coordinate(String str) {
        int h = 0, m = 0, s = 0, i, l, n, c;
        char r = E;
        double g = 0.0;
        if(str!=null && str.length()>0) {
            for(i = 0,l = str.length(),n = 0; i<l; ++i) {
                c = (int)str.charAt(i);
                if(c>='0' && c<='9') n = (n*10)+(c-'0');
                else {
                    c = (c&0x5f);
                    if(c=='E' || c=='N' || c=='W' || c=='S') {
                        h = n;
                        n = 0;
                        r = (char)c;
                    }
                }
            }
            if(n<100) m = n;
            else if(n<1000) {
                m = n/10;
                s = n-m*10;
            } else {
                m = n/100;
                s = n-m*100;
            }
            g = (double)h+((double)m/60.0)+((double)s/3600.0);
            if(r==W || r==S) g = -g;
            if(s>=60) {
                s -= 60;
                ++m;
            }
            if(m>=60) {
                m -= 60;
                ++h;
            }
            if(h>=360) h -= 360;
        }
        grade = g;
        dir = r;
        day = 360;
        hour = h;
        minute = m;
        second = s;
    }

    public Coordinate(double g) { this(E,g,360); }

    public Coordinate(char r,double g) { this(r,g,360); }

    public Coordinate(char r,double g,int d) {
        int h,m,s,n;
        boolean neg = g<0.0;
        if(g<0.0) g = -g;
        h = (int)g;
        n = (int)Math.round((g-(double)h)*3600.0);
        m = n/60;
        s = n%60;
        if(s>=60) {
            s -= 60;
            ++m;
        }
        if(m>=60) {
            m -= 60;
            ++h;
        }
        if(h>=d) h -= d;
        if(neg) h = -h;
        grade = g;
        dir = r;
        day = d;
        hour = h;
        minute = m;
        second = s;
    }

    public String formatHMS() {
        return String.format(Locale.ENGLISH,"%2$d%1$c%3$02d'%4$02d\"",dir,hour,minute,second);
    }

    public String formatHM() {
        return String.format(Locale.ENGLISH,"%2$d%1$c%3$02d",dir,hour,minute);
    }

    public String formatHDMS() {
        return String.format(Locale.ENGLISH,"%2$d°%1$c %3$02d'%4$02d\"",dir,hour,minute,second);
    }

    public String formatHDM() {
        return String.format(Locale.ENGLISH,"%2$d°%1$c %3$02d'",dir,hour,minute);
    }

    public String formatHMSD() {
        return String.format(Locale.ENGLISH,"%2$d°%3$02d'%4$02d\"%1$c",dir,hour,minute,second);
    }

    public String formatHMD() {
        return String.format(Locale.ENGLISH,"%2$d°%3$02d'%1$c",dir,hour,minute);
    }

    @Override
    public String toString() {
        return formatHDMS();
    }
}


