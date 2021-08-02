package net.spirangle.sphinx;

import static net.spirangle.sphinx.Symbol.Attribute.CONCEPT_SHIFT;

import net.spirangle.sphinx.Symbol.Concept;

public class AstrologyProperties {
    /* Values in the order of libastro.so (must correspond identically): */
    public static final int _ASTRO_TELLUS_       =  0;
    public static final int _ASTRO_SUN_          =  1;
    public static final int _ASTRO_MOON_         =  2;
    public static final int _ASTRO_MERCURY_      =  3;
    public static final int _ASTRO_VENUS_        =  4;
    public static final int _ASTRO_MARS_         =  5;
    public static final int _ASTRO_JUPITER_      =  6;
    public static final int _ASTRO_SATURN_       =  7;
    public static final int _ASTRO_URANUS_       =  8;
    public static final int _ASTRO_NEPTUNE_      =  9;
    public static final int _ASTRO_PLUTO_        = 10;
    public static final int _ASTRO_NNODE_        = 11;
    public static final int _ASTRO_TNNODE_       = 12;
    public static final int _ASTRO_SNODE_        = 13;
    public static final int _ASTRO_TSNODE_       = 14;
    public static final int _ASTRO_CHIRON_       = 15;
    public static final int _ASTRO_CERES_        = 16;
    public static final int _ASTRO_PALLAS_       = 17;
    public static final int _ASTRO_JUNO_         = 18;
    public static final int _ASTRO_VESTA_        = 19;
    public static final int _ASTRO_VERTEX_       = 20;
    public static final int _ASTRO_EASTPOINT_    = 21;
    public static final int _ASTRO_FORTUNE_      = 22;
    public static final int _ASTRO_ASCENDANT_    = 23;
    public static final int _ASTRO_MC_           = 24;
	/*
	public static final int _ASTRO_HOUSE_        = 25;
	public static final int _ASTRO_MINOR_PLANET_ = 26;
	public static final int _ASTRO_FIXED_STAR_   = 27;
	public static final int _ASTRO_ARABIC_PART_  = 28;
	*/

    /* Values in the order of libastro.so (must correspond identically): */
    public static final int _HOROSCOPE_NATAL_          = 0x0001;
    public static final int _HOROSCOPE_SYNASTRY_       = 0x0002;
    public static final int _HOROSCOPE_COMPOSITE_      = 0x0004;
    public static final int _HOROSCOPE_GEOCENTRIC_     = 0x0008;
    public static final int _HOROSCOPE_HELIOCENTRIC_   = 0x0010;
    public static final int _HOROSCOPE_SIDEREAL_       = 0x0020;
    public static final int _HOROSCOPE_TROPICAL_       = 0x0040;
    public static final int _HOROSCOPE_ASPECT_IN_SIGN_ = 0x0080;


    public static final int ASPECT_ORB_LEVELS = 10;

    public static final int ASTRO_ELEMENT        = Concept.ASTRO_ELEMENT<<CONCEPT_SHIFT;
    public static final int ASTRO_QUALITY        = Concept.ASTRO_QUALITY<<CONCEPT_SHIFT;
    public static final int ASTRO_ENERGY         = Concept.ASTRO_ENERGY<<CONCEPT_SHIFT;
    public static final int ASTRO_ZODIAC         = Concept.ASTRO_ZODIAC<<CONCEPT_SHIFT;
    public static final int ASTRO_PLANET         = Concept.ASTRO_PLANET<<CONCEPT_SHIFT;
    public static final int ASTRO_MINOR_PLANET   = Concept.ASTRO_MPLANET<<CONCEPT_SHIFT;
    public static final int ASTRO_FIXED_STAR     = Concept.ASTRO_FSTAR<<CONCEPT_SHIFT;
    public static final int ASTRO_POINT          = Concept.ASTRO_POINT<<CONCEPT_SHIFT;
    public static final int ASTRO_LOT            = Concept.ASTRO_LOT<<CONCEPT_SHIFT;
    public static final int ASTRO_HOUSE          = Concept.ASTRO_HOUSE<<CONCEPT_SHIFT;
    public static final int ASTRO_ASPECT         = Concept.ASTRO_ASPECT<<CONCEPT_SHIFT;
    public static final int ASTRO_ASPECT_PATTERN = Concept.ASTRO_ASPPAT<<CONCEPT_SHIFT;
    public static final int ASTRO_SHAPING        = Concept.ASTRO_SHAPING<<CONCEPT_SHIFT;
    public static final int ASTRO_FACTOR         = Concept.ASTRO_FACTOR<<CONCEPT_SHIFT;
    public static final int ASTRO_HOUSE_SYSTEM   = Concept.ASTRO_HSYSTEM<<CONCEPT_SHIFT;
    public static final int ASTRO_CHART          = Concept.ASTRO_CHART<<CONCEPT_SHIFT;

    /* Elements (9): */
    public static final int ASTRO_FIRE  = ASTRO_ELEMENT;
    public static final int ASTRO_EARTH = ASTRO_ELEMENT|1;
    public static final int ASTRO_AIR   = ASTRO_ELEMENT|2;
    public static final int ASTRO_WATER = ASTRO_ELEMENT|3;

    /* Qualities (A): */
    public static final int ASTRO_CARDINAL = ASTRO_QUALITY;
    public static final int ASTRO_FIXED    = ASTRO_QUALITY|1;
    public static final int ASTRO_MUTABLE  = ASTRO_QUALITY|2;

    /* Energies (B): */
    public static final int ASTRO_MALE   = ASTRO_ENERGY;
    public static final int ASTRO_FEMALE = ASTRO_ENERGY|1;

    /* Zodiac (C): */
    public static final int ASTRO_ARIES       = ASTRO_ZODIAC;
    public static final int ASTRO_TAURUS      = ASTRO_ZODIAC|1;
    public static final int ASTRO_GEMINI      = ASTRO_ZODIAC|2;
    public static final int ASTRO_CANCER      = ASTRO_ZODIAC|3;
    public static final int ASTRO_LEO         = ASTRO_ZODIAC|4;
    public static final int ASTRO_VIRGO       = ASTRO_ZODIAC|5;
    public static final int ASTRO_LIBRA       = ASTRO_ZODIAC|6;
    public static final int ASTRO_SCORPIO     = ASTRO_ZODIAC|7;
    public static final int ASTRO_SAGITTARIUS = ASTRO_ZODIAC|8;
    public static final int ASTRO_CAPRICORN   = ASTRO_ZODIAC|9;
    public static final int ASTRO_AQUARIUS    = ASTRO_ZODIAC|10;
    public static final int ASTRO_PISCES      = ASTRO_ZODIAC|11;

    /* Planets (D): */
    public static final int ASTRO_SUN     = ASTRO_PLANET;
    public static final int ASTRO_MOON    = ASTRO_PLANET|1;
    public static final int ASTRO_MERCURY = ASTRO_PLANET|2;
    public static final int ASTRO_VENUS   = ASTRO_PLANET|3;
    public static final int ASTRO_MARS    = ASTRO_PLANET|4;
    public static final int ASTRO_JUPITER = ASTRO_PLANET|5;
    public static final int ASTRO_SATURN  = ASTRO_PLANET|6;
    public static final int ASTRO_URANUS  = ASTRO_PLANET|7;
    public static final int ASTRO_NEPTUNE = ASTRO_PLANET|8;
    public static final int ASTRO_PLUTO   = ASTRO_PLANET|9;

    /* Minor Planets (E): */
    public static final int ASTRO_CHIRON = ASTRO_MINOR_PLANET|2060;
    public static final int ASTRO_CERES  = ASTRO_MINOR_PLANET|1;
    public static final int ASTRO_PALLAS = ASTRO_MINOR_PLANET|2;
    public static final int ASTRO_JUNO   = ASTRO_MINOR_PLANET|3;
    public static final int ASTRO_VESTA  = ASTRO_MINOR_PLANET|4;

    /* Fixed Stars (F): */
    public static final int ASTRO_FIXED_STAR_INDEX  = ASTRO_FIXED_STAR|1;
    public static final int ASTRO_FIXED_STAR_LENGTH = 386;

    /* Points (10): */
    public static final int ASTRO_NNODE     = ASTRO_POINT;
    public static final int ASTRO_TNNODE    = ASTRO_POINT|1;
    public static final int ASTRO_SNODE     = ASTRO_POINT|2;
    public static final int ASTRO_TSNODE    = ASTRO_POINT|3;
    public static final int ASTRO_LILITH    = ASTRO_POINT|4;
    public static final int ASTRO_VERTEX    = ASTRO_POINT|5;
    public static final int ASTRO_EASTPOINT = ASTRO_POINT|6;

    /* Arabic Parts / Lots (11): */
    public static final int ASTRO_FORTUNE = ASTRO_LOT;

    /* Houses (12): */
    public static final int ASTRO_ASCENDANT  = ASTRO_HOUSE|1;
    public static final int ASTRO_2ND_HOUSE  = ASTRO_HOUSE|2;
    public static final int ASTRO_3RD_HOUSE  = ASTRO_HOUSE|3;
    public static final int ASTRO_4TH_HOUSE  = ASTRO_HOUSE|4;
    public static final int ASTRO_5TH_HOUSE  = ASTRO_HOUSE|5;
    public static final int ASTRO_6TH_HOUSE  = ASTRO_HOUSE|6;
    public static final int ASTRO_7TH_HOUSE  = ASTRO_HOUSE|7;
    public static final int ASTRO_8TH_HOUSE  = ASTRO_HOUSE|8;
    public static final int ASTRO_9TH_HOUSE  = ASTRO_HOUSE|9;
    public static final int ASTRO_MC         = ASTRO_HOUSE|10;
    public static final int ASTRO_11TH_HOUSE = ASTRO_HOUSE|11;
    public static final int ASTRO_12TH_HOUSE = ASTRO_HOUSE|12;

    /* Aspects (13): */
    public static final int CONJUNCTION    = ASTRO_ASPECT|0;    //   0°
    public static final int SEMISEXTILE    = ASTRO_ASPECT|1;    //  30°   1/12
    public static final int DECILE         = ASTRO_ASPECT|2;    //  36°   1/10
    public static final int NOVILE         = ASTRO_ASPECT|3;    //  40°   1/9
    public static final int SEMISQUARE     = ASTRO_ASPECT|4;    //  45°   1/8
    public static final int SEPTILE        = ASTRO_ASPECT|5;    //  51°   1/7
    public static final int SEXTILE        = ASTRO_ASPECT|6;    //  60°   1/6
    public static final int QUINTILE       = ASTRO_ASPECT|7;    //  72°   1/5
    public static final int SQUARE         = ASTRO_ASPECT|8;    //  90°   1/4
    //	public static final int BISEPTILE  = ASTRO_ASPECT|9;    // 103°   2/7
    public static final int TRINE          = ASTRO_ASPECT|9;    // 120°   1/3
    public static final int SESQUIQUADRATE = ASTRO_ASPECT|10;   // 135°   3/8
    //	public static final int BIQUINTILE = ASTRO_ASPECT|11;   // 144°   2/5
    public static final int QUINCUNX       = ASTRO_ASPECT|11;   // 150°   5/12
    //	public static final int TRISEPTILE = ASTRO_ASPECT|12;   // 154°   3/7
    public static final int OPPOSITION     = ASTRO_ASPECT|12;   // 180°   1/2

    /* Aspect Patterns (14): */
    public static final int STELLIUM  = ASTRO_ASPECT_PATTERN;
    public static final int TSQUARE   = ASTRO_ASPECT_PATTERN|1;
    public static final int GSQUARE   = ASTRO_ASPECT_PATTERN|2;
    public static final int GCROSS    = ASTRO_ASPECT_PATTERN|3;
    public static final int GTRINE    = ASTRO_ASPECT_PATTERN|4;
    public static final int YOD       = ASTRO_ASPECT_PATTERN|5;
    public static final int MYSTRECT  = ASTRO_ASPECT_PATTERN|6;
    public static final int KITE      = ASTRO_ASPECT_PATTERN|7;
    public static final int GQUINTILE = ASTRO_ASPECT_PATTERN|8;
    public static final int HEXAGRAM  = ASTRO_ASPECT_PATTERN|9;

    /* Shapings (15): */
    public static final int BOWL       = ASTRO_SHAPING;
    public static final int WEDGE      = ASTRO_SHAPING|1;
    public static final int SEESAW     = ASTRO_SHAPING|2;
    public static final int SPLAY      = ASTRO_SHAPING|3;
    public static final int LOCOMOTIVE = ASTRO_SHAPING|4;
    public static final int BUCKET     = ASTRO_SHAPING|5;
    public static final int BUNDLE     = ASTRO_SHAPING|6;
    public static final int SPLASH     = ASTRO_SHAPING|7;

    /* Factors (16): */
    public static final int RULER_PLANET = ASTRO_FACTOR|0x0001;
    public static final int RULER_HOUSE  = ASTRO_FACTOR|0x0002;
    public static final int ASCENDING    = ASTRO_FACTOR|0x0004;
    public static final int ANGULAR      = ASTRO_FACTOR|0x0008;
    public static final int MRECEPTIVE   = ASTRO_FACTOR|0x0010;

    /* House Systems (17): */
    public static final int HSYS_PLACIDUS = ASTRO_HOUSE_SYSTEM|0;
    public static final int HSYS_KOCH     = ASTRO_HOUSE_SYSTEM|1;

    /* Chart Types (18): */
    public static final int NATAL          = ASTRO_CHART|_HOROSCOPE_NATAL_;
    public static final int SYNASTRY       = ASTRO_CHART|_HOROSCOPE_SYNASTRY_;
    public static final int COMPOSITE      = ASTRO_CHART|_HOROSCOPE_COMPOSITE_;
    public static final int GEOCENTRIC     = ASTRO_CHART|_HOROSCOPE_GEOCENTRIC_;
    public static final int HELIOCENTRIC   = ASTRO_CHART|_HOROSCOPE_HELIOCENTRIC_;
    public static final int SIDEREAL       = ASTRO_CHART|_HOROSCOPE_SIDEREAL_;
    public static final int TROPICAL       = ASTRO_CHART|_HOROSCOPE_TROPICAL_;
    public static final int ASPECT_IN_SIGN = ASTRO_CHART|_HOROSCOPE_ASPECT_IN_SIGN_;

    /* Conversion tables from libastro.so: */
    public static final int[] _ASTRO_PLANET_ = {
        -1,
        ASTRO_SUN,ASTRO_MOON,ASTRO_MERCURY,ASTRO_VENUS,ASTRO_MARS,ASTRO_JUPITER,
        ASTRO_SATURN,ASTRO_URANUS,ASTRO_NEPTUNE,ASTRO_PLUTO,
        ASTRO_NNODE,ASTRO_TNNODE,ASTRO_SNODE,ASTRO_TSNODE,
        ASTRO_CHIRON,ASTRO_CERES,ASTRO_PALLAS,ASTRO_JUNO,ASTRO_VESTA,
        ASTRO_VERTEX,ASTRO_EASTPOINT,
        ASTRO_FORTUNE,
        ASTRO_ASCENDANT,ASTRO_MC,
        -1/*ASTRO_HOUSE*/,
        -1/*ASTRO_MINOR_PLANET*/,
        -1/*ASTRO_FIXED_STAR*/,
        -1/*ASTRO_ARABIC_PART*/,
    };

    public static final int[] _ASTRO_ZODIAC_ = {
        ASTRO_ARIES,ASTRO_TAURUS,ASTRO_GEMINI,ASTRO_CANCER,ASTRO_LEO,ASTRO_VIRGO,
        ASTRO_LIBRA,ASTRO_SCORPIO,ASTRO_SAGITTARIUS,ASTRO_CAPRICORN,ASTRO_AQUARIUS,
        ASTRO_PISCES,
    };

    public static final int[] _ASTRO_HOUSE_ = {
        ASTRO_ASCENDANT,ASTRO_2ND_HOUSE,ASTRO_3RD_HOUSE,ASTRO_4TH_HOUSE,ASTRO_5TH_HOUSE,
        ASTRO_6TH_HOUSE,ASTRO_7TH_HOUSE,ASTRO_8TH_HOUSE,ASTRO_9TH_HOUSE,ASTRO_MC,
        ASTRO_11TH_HOUSE,ASTRO_12TH_HOUSE,
    };

    public static final int[] _ASTRO_ASPECT_ = {
        CONJUNCTION,SEMISEXTILE,DECILE,NOVILE,SEMISQUARE,SEPTILE,SEXTILE,QUINTILE,
        SQUARE,TRINE,SESQUIQUADRATE,QUINCUNX,OPPOSITION
    };

    public static final int[] _ASTRO_ASPECT_PATTERN_ = {
        STELLIUM,TSQUARE,GSQUARE,GCROSS,GTRINE,YOD,MYSTRECT,KITE,GQUINTILE,HEXAGRAM,
    };

    public static final int[] _ASTRO_FACTOR_ = {
        RULER_PLANET,RULER_HOUSE,ASCENDING,ANGULAR,MRECEPTIVE,
    };
}

