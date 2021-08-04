package net.spirangle.sphinx.config;


import net.spirangle.sphinx.BuildConfig;

public class SphinxProperties {
    public static final String APP     = "sphinx";
    public static final String PACKAGE = "net.spirangle.sphinx.astro";

    public static final String DB_ASTRO      = "AstroDB.db";
    public static final int DB_ASTRO_VERSION = 1;

    public static final String GMT       = "GMT";
    public static final double GMT_LON   = 0.0;
    public static final double GMT_LAT   = 51.0+(28.0/60.0)+(38.0/3600.0);

    public static final String SERVER_CLIENT_ID      = "50128090633-ip543e06k8uq7g203tmoonoeslsi25p6.apps.googleusercontent.com";
    public static final String GOOGLE_MAPS_API_KEY   = BuildConfig.MAPS_API_KEY;

    public static final int ACTIVITY_HOROSCOPE    = 1;
    public static final int ACTIVITY_PROFILES     = 2;
    public static final int ACTIVITY_EDIT_PROFILE = 3;
    public static final int ACTIVITY_TEXT         = 4;
    public static final int ACTIVITY_EDIT_TEXT    = 5;
    public static final int ACTIVITY_HELP         = 6;
    public static final int ACTIVITY_SETTINGS     = 7;
    public static final int ACTIVITY_SIGN_IN      = 9001;

    public static final String EXTRA_USER    = PACKAGE+".USER";
    public static final String EXTRA_PROFILE = PACKAGE+".PROFILE";
    public static final String EXTRA_RADIX1  = PACKAGE+".RADIX1";
    public static final String EXTRA_RADIX2  = PACKAGE+".RADIX2";
    public static final String EXTRA_GRAPH   = PACKAGE+".GRAPH";
    public static final String EXTRA_SYMBOL  = PACKAGE+".SYMBOL";
    public static final String EXTRA_TEXT    = PACKAGE+".TEXT";
    public static final String EXTRA_HELP    = PACKAGE+".HELP";

    public static final String URL_SPIRANGLE     = "http://sphinx.spirangle.net";
    public static final String URL_SPIRANGLE_API = URL_SPIRANGLE+"/api/v1";

    public static final int FLAG_PUBLIC    = 0x0000;
    public static final int FLAG_PRIVATE   = 0x0001;
    public static final int FLAG_STATIC    = 0x0002;
    public static final int FLAG_CONFIRMED = 0x0004;
    public static final int FLAG_PUBLISHED = 0x0008;

    public static final int USER_ADMIN   = 0x1000;
    public static final int USER_BLOCKED = 0x4000;
    public static final int USER_DELETED = 0x8000;

    public static final int TEXT_TYPE_PROFILE = 1;
    public static final int TEXT_TYPE_SYMBOL  = 2;
}


