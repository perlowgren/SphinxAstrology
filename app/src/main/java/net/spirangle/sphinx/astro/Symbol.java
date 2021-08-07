package net.spirangle.sphinx.astro;

import static net.spirangle.sphinx.config.AstrologyProperties.*;

import net.spirangle.minerva.util.Base36;

public class Symbol {
    private static final String TAG = Symbol.class.getSimpleName();

    public interface SymbolListener {
        void onSymbolAttributeSelected(int id);

        void onSymbolSelected(long id);

        void onSymbolSelected(Symbol s);
    }

    /* Symbolic Systems (1): */
    public static final int SYMBOL = 0x00010000; // General symbols, not within any specific attributions system
    public static final int ASTROLOGY = 0x00010001;
    public static final int QABALAH = 0x00010002;
    public static final int TAROT = 0x00010003;
    public static final int ENOCHIAN = 0x00010004;
    public static final int ALCHEMY = 0x00010005;
    public static final int GEOMANCY = 0x00010006;

    /* Directions (2): */
    public static final int LONGITUDE = 0x00020000;
    public static final int LATITUDE = 0x00020001;
    public static final int EAST = 0x00020002;
    public static final int WEST = 0x00020003;
    public static final int NORTH = 0x00020004;
    public static final int SOUTH = 0x00020005;

    /* Calendars (3): */
    public static final int JULIAN_CALENDAR = 0x00030000;
    public static final int JULIAN_DAY = 0x00030001;
    public static final int GREGORIAN = 0x00030002;

    /* Months (4): */
    public static final int JANUARY = 0x00040000;
    public static final int FEBRUARY = 0x00040001;
    public static final int MARCH = 0x00040002;
    public static final int APRIL = 0x00040003;
    public static final int MAY = 0x00040004;
    public static final int JUNE = 0x00040005;
    public static final int JULY = 0x00040006;
    public static final int AUGUST = 0x00040007;
    public static final int SEPTEMBER = 0x00040008;
    public static final int OCTOBER = 0x00040009;
    public static final int NOVEMBER = 0x0004000a;
    public static final int DECEMBER = 0x0004000b;

    /* Genral Elements (5) (for conversion between systems): */
    public static final int SPIRIT = 0x00050000;
    public static final int FIRE = 0x00050001;
    public static final int AIR = 0x00050002;
    public static final int WATER = 0x00050003;
    public static final int EARTH = 0x00050004;

    /* General Classic Planets (6) (for conversion between systems): */
    public static final int SUN = 0x00060000;
    public static final int MOON = 0x00060001;
    public static final int MERCURY = 0x00060002;
    public static final int VENUS = 0x00060003;
    public static final int MARS = 0x00060004;
    public static final int JUPITER = 0x00060005;
    public static final int SATURN = 0x00060006;

    /* General Western Zodiac (7) (for conversion between systems): */
    public static final int ARIES = 0x00070000;
    public static final int TAURUS = 0x00070001;
    public static final int GEMINI = 0x00070002;
    public static final int CANCER = 0x00070003;
    public static final int LEO = 0x00070004;
    public static final int VIRGO = 0x00070005;
    public static final int LIBRA = 0x00070006;
    public static final int SCORPIO = 0x00070007;
    public static final int SAGITTARIUS = 0x00070008;
    public static final int CAPRICORN = 0x00070009;
    public static final int AQUARIUS = 0x0007000a;
    public static final int PISCES = 0x0007000b;

    /* Index of groups of strings in _S: */
    private static final int _CON = 0;  // Concepts
    private static final int _SYS = 1;  // Systems
    private static final int _DIR = 2;  // Directions
    private static final int _CAL = 3;  // Calendars
    private static final int _MON = 4;  // Months
    private static final int _ELE = 5;  // Element
    private static final int _CPL = 6;  // Classical Planets
    private static final int _ZOD = 7;  // Zodiac
    private static final int _ACA = 8;  // Astrological Categories (%3 Singular, %3+1 Plural, %3+2 Definitive Plural)
    private static final int _QUA = 9;  // Quality
    private static final int _ENE = 10;  // Energy
    private static final int _PLA = 11;  // Planets (Tellus, Uranus & Neptune)
    private static final int _MPL = 12;  // Minor Planets
    private static final int _FST = 13;  // Fixed Stars
    private static final int _APT = 14;  // Points (Lunar Nodes, etc.)
    private static final int _LOT = 15;  // Arabic Parts / Lots
    private static final int _HOU = 16;  // Houses (%2 Long Names, %2+1 Short Names)
    private static final int _ASP = 17;  // Aspects
    private static final int _APA = 18;  // Aspect Patterns
    private static final int _SHA = 19;  // Shapings
    private static final int _FAC = 20;  // Factors
    private static final int _HSY = 21;  // House Systems
    private static final int _CHT = 22;  // Chart Types
    private static final int _HEB = 23;  // Hebrew Letters
    private static final int _TAR = 24;  // Tarot Arcanas
    private static final int _TMA = 25;  // Tarot Minor Arcana (%2 Singular, %2+1 Plural)


	/*private static final String[] zodiacUnicode = {
		"\u2648","\u2649","\u264A","\u264B","\u264C","\u264D",
		"\u264E","\u264F","\u2650","\u2651","\u2652","\u2653",
	};*/

	/*private static final String[] planetUnicode = {
		"\u2A01",
		"\u2A00","\u263D","\u263F","\u2640","\u2642","\u2643","\u2644","\u2645","\u2646","\u2647",
		"\u260A","\u260A","\u260B","\u260B",
		"\u26B7","\u26B3","\u26B4","\u26B5","\u26B6",
		"\u260E","\u260F","\u2A02",
		"\u2610","\u2611",
	};*/

	/*private static final String[] planetUnicode = {
		"\u2A00","\u263D","\u263F","\u2640","\u2642","\u2643","\u2644","\u2645","\u2646","\u2647",
	};

	private static final String[] minorPlanetUnicode = {
		"\u26B7","\u26B3","\u26B4","\u26B5","\u26B6",
	};

	private static final String[] pointUnicode = {
		"\u260A","\u260A","\u260B","\u260B","","\u260E","\u260F","\u2A02",
	};

	private static final String[] houseUnicode = {
		"\u2610","2","3","4","5","6","7","8","9","\u2611","11","12",
	};

	private static final String[] aspectUnicode = {
		"\u260C","\u26BA","\u27C2","\u2612","\u2220","\u2721","\u26B9",
		"\u2613","\u25A1","\u25B3","\u26BC","\u26BB","\u260D",
	};*/

    /* Unicode values corresponding with SeshatSymbols.ttf: */
    public static final String getUnicode(int id) {
        switch(id) {
            case SPIRIT:
                return ""; /* TODO! */
            case FIRE:
            case ASTRO_FIRE:
                return "\u22B3";
            case AIR:
            case ASTRO_AIR:
                return "\u22B5";
            case WATER:
            case ASTRO_WATER:
                return "\u22B2";
            case EARTH:
            case ASTRO_EARTH:
                return "\u22B4";

            case ASTRO_CARDINAL:
                return "\u22B6";
            case ASTRO_FIXED:
                return "\u22B7";
            case ASTRO_MUTABLE:
                return "\u22B8";

            case ASTRO_MALE:
                return "\u2642";
            case ASTRO_FEMALE:
                return "\u2640";

            case ARIES:
            case ASTRO_ARIES:
                return "\u2648";
            case TAURUS:
            case ASTRO_TAURUS:
                return "\u2649";
            case GEMINI:
            case ASTRO_GEMINI:
                return "\u264A";
            case CANCER:
            case ASTRO_CANCER:
                return "\u264B";
            case LEO:
            case ASTRO_LEO:
                return "\u264C";
            case VIRGO:
            case ASTRO_VIRGO:
                return "\u264D";
            case LIBRA:
            case ASTRO_LIBRA:
                return "\u264E";
            case SCORPIO:
            case ASTRO_SCORPIO:
                return "\u264F";
            case SAGITTARIUS:
            case ASTRO_SAGITTARIUS:
                return "\u2650";
            case CAPRICORN:
            case ASTRO_CAPRICORN:
                return "\u2651";
            case AQUARIUS:
            case ASTRO_AQUARIUS:
                return "\u2652";
            case PISCES:
            case ASTRO_PISCES:
                return "\u2653";

            case SUN:
            case ASTRO_SUN:
                return "\u2A00";
            case MOON:
            case ASTRO_MOON:
                return "\u263D";
            case MERCURY:
            case ASTRO_MERCURY:
                return "\u263F";
            case VENUS:
            case ASTRO_VENUS:
                return "\u2640";
            case MARS:
            case ASTRO_MARS:
                return "\u2642";
            case JUPITER:
            case ASTRO_JUPITER:
                return "\u2643";
            case SATURN:
            case ASTRO_SATURN:
                return "\u2644";
            case ASTRO_URANUS:
                return "\u2645";
            case ASTRO_NEPTUNE:
                return "\u2646";
            case ASTRO_PLUTO:
                return "\u2647";

            case ASTRO_CHIRON:
                return "\u26B7";
            case ASTRO_CERES:
                return "\u26B3";
            case ASTRO_PALLAS:
                return "\u26B4";
            case ASTRO_JUNO:
                return "\u26B5";
            case ASTRO_VESTA:
                return "\u26B6";

            case ASTRO_NNODE:
                return "\u260A";
            case ASTRO_TNNODE:
                return "\u260A";
            case ASTRO_SNODE:
                return "\u260B";
            case ASTRO_TSNODE:
                return "\u260B";
            case ASTRO_LILITH:
                return "\u26B8";
            case ASTRO_VERTEX:
                return "\u260E";
            case ASTRO_EASTPOINT:
                return "\u260F";

            case ASTRO_FORTUNE:
                return "\u2A02";

            case ASTRO_ASCENDANT:
                return "\u2610";
            case ASTRO_MC:
                return "\u2611";

            case CONJUNCTION:
                return "\u260C";
            case SEMISEXTILE:
                return "\u26BA";
            case DECILE:
                return "\u27C2";
            case NOVILE:
                return "\u2612";
            case SEMISQUARE:
                return "\u2220";
            case SEPTILE:
                return "\u2721";
            case SEXTILE:
                return "\u26B9";
            case QUINTILE:
                return "\u2613";
            case SQUARE:
                return "\u25A1";
            case TRINE:
                return "\u25B3";
            case SESQUIQUADRATE:
                return "\u26BC";
            case QUINCUNX:
                return "\u26BB";
            case OPPOSITION:
                return "\u260D";
        }
        return null;
    }

    /* Constant conversion indexes of string data: */
	/*private static final int[] _ASTRO_ELE = { FIRE,EARTH,AIR,WATER, };
	private static final int[] _ASTRO_CPL = { SUN,MOON,MERCURY,VENUS,MARS,JUPITER,SATURN, };
	private static final int[] _ASTRO_PLA = {
		SUN,MOON,MERCURY,VENUS,MARS,JUPITER,SATURN,
		1,2,3,              // Uranus, Neptune, Pluto
	};
	private static final int[] _ASTRO_PPT = {
		SUN,MOON,MERCURY,VENUS,MARS,JUPITER,SATURN,
		1,2,3,              // Uranus, Neptune, Pluto
		0,1,2,3,4,          // Chiron - Vesta
		0,1,2,3,5,6,        // Lunar Nodes, Vertex, Eastpoint
		0,                  // Fortune
	};*/

    /* Conversion indexes of string data: */
	/*private static int[] _astro_ELE = new int[_ASTRO_ELE.length];
	private static int[] _astro_CPL = new int[_ASTRO_CPL.length];
	private static int[] _astro_PLA = new int[_ASTRO_PLA.length];
	private static int[] _astro_PPT = new int[_ASTRO_PPT.length];*/

    public static final class Attribute {
        public static final int CONCEPT_SHIFT = 16;
        public static final int CONCEPT_MASK = 0xffff0000;

        public static int valueOf(int id) {
            return (id&0x0000ffff);
        }

        public final int id;
        public final int list;
        public final int value;
        protected int index;

        public Attribute(int i,int l,int v) {
            id = i;
            list = l;
            value = v;
            index = -1;
        }

        public void update() {
            index = _N[list]+value;
        }
    }

    public static final class AttributeSet {
        public final int startId;
        public final int list;
        public final int startValue;
        public final int length;
        public final Attribute[] attributes;
        protected int startIndex;

        public AttributeSet(int i,int l,int v,int n) {
            startId = i;
            list = l;
            startValue = v;
            startIndex = -1;
            length = n;
            attributes = null;
        }

        public AttributeSet(Attribute[] a) {
            startId = -1;
            list = -1;
            startValue = -1;
            startIndex = -1;
            length = 0;
            attributes = a;
        }

        public void update() {
            if(attributes!=null) {
                for(int i = 0; i<attributes.length; ++i)
                    attributes[i].update();
            } else if(list>=0 && list<_N.length) {
                startIndex = _N[list]+startValue;
//Log.d(APP,TAG+".AttributeSet.update(startIndex: "+startIndex+")");
            }
        }

        public int getId(int n) {
            if(attributes!=null) {
                if(n>=0 && n<attributes.length)
                    return attributes[n].id;
            } else {
                if(n>=0 && n<length)
                    return startId+n;
            }
//Log.d(APP,TAG+".AttributeSet.getId(n: "+n+", return: -1)");
            return -1;
        }

        public int getIndex(int n) {
            if(attributes!=null) {
                if(n>=0 && n<attributes.length)
                    return attributes[n].index;
            } else {
                if(n>=0 && n<length)
                    return startIndex+n;
            }
//Log.d(APP,TAG+".AttributeSet.getIndex(n: "+n+", return: -1)");
            return -1;
        }

        public int indexOf(int id) {
            if(attributes!=null) {
                for(int i = 0; i<attributes.length; ++i)
                    if(attributes[i].id==id) return i;
            } else {
                if(id>=startId && id<startId+length)
                    return id-startId;
            }
//Log.d(APP,TAG+".AttributeSet.indexOf(id: "+id+", return: -1)");
            return -1;
        }
    }

    public static final AttributeSet[] _A = {
        new AttributeSet(Concept.SYM_SYSTEM,_CON,1,49), /* SYM_SYSTEM */
        /*new AttributeSet(new Attribute[] { // SYM_CONCEPT
            new Attribute(SYM_SYSTEM,         _CON,   1),
            new Attribute(SYM_DIRECTION,      _CON,   2),
            new Attribute(SYM_CALENDAR,       _CON,   3),
            new Attribute(SYM_MONTH,          _CON,   4),
            new Attribute(SYM_ELEMENT,        _CON,   5),
            new Attribute(SYM_PLANET,         _CON,   6),
            new Attribute(SYM_ZODIAC,         _CON,   7),
            new Attribute(ASTRO_CONCEPT,      _CON,   8),
            new Attribute(ASTRO_ELEMENT,      _CON,   9),
            new Attribute(ASTRO_QUALITY,      _CON,  10),
            new Attribute(ASTRO_ENERGY,       _CON,  11),
            new Attribute(ASTRO_ZODIAC,       _CON,  12),
            new Attribute(ASTRO_PLANET,       _CON,  13),
            new Attribute(ASTRO_MPLANET,      _CON,  14),
            new Attribute(ASTRO_FSTAR,        _CON,  15),
            new Attribute(ASTRO_POINT,        _CON,  16),
            new Attribute(ASTRO_LOT,          _CON,  17),
            new Attribute(ASTRO_HOUSE,        _CON,  18),
            new Attribute(ASTRO_ASPECT,       _CON,  19),
            new Attribute(ASTRO_ASPPAT,       _CON,  20),
            new Attribute(ASTRO_SHAPING,      _CON,  21),
            new Attribute(ASTRO_FACTOR,       _CON,  22),
            new Attribute(ASTRO_HSYSTEM,      _CON,  23),
            new Attribute(ASTRO_CHART,        _CON,  24),
            new Attribute(ASTRO_PLZ,          _CON,  25),
            new Attribute(ASTRO_MPZ,          _CON,  26),
            new Attribute(ASTRO_FSZ,          _CON,  27),
            new Attribute(ASTRO_PLH,          _CON,  28),
            new Attribute(ASTRO_MPH,          _CON,  29),
            new Attribute(ASTRO_FSH,          _CON,  30),
            new Attribute(ASTRO_HZ,           _CON,  31),
            new Attribute(ASTRO_PLPLASP,      _CON,  32),
            new Attribute(ASTRO_PLMPASP,      _CON,  33),
            new Attribute(ASTRO_PLFSASP,      _CON,  34),
            new Attribute(ASTRO_MPMPASP,      _CON,  35),
            new Attribute(ASTRO_FSFSASP,      _CON,  36),
            new Attribute(ASTRO_MPFSASP,      _CON,  37),
            new Attribute(ASTRO_PLHASP,       _CON,  38),
            new Attribute(ASTRO_HMPASP,       _CON,  39),
            new Attribute(ASTRO_HFSASP,       _CON,  40),
            new Attribute(ASTRO_PLFAC,        _CON,  41),
            new Attribute(ASTRO_HFAC,         _CON,  42),
            new Attribute(ASTRO_PLPLFAC,      _CON,  43),
            new Attribute(ASTRO_PATELE,       _CON,  44),
            new Attribute(ASTRO_PATQUA,       _CON,  45),
            new Attribute(ASTRO_PLPAT,        _CON,  46),
            new Attribute(ASTRO_MPPAT,        _CON,  47),
            new Attribute(ASTRO_FSPAT,        _CON,  48),
            new Attribute(ASTRO_HPAT,         _CON,  49),
        }),*/
        new AttributeSet(SYMBOL,_SYS,0,7), /* SYM_SYSTEM */
        /*new AttributeSet(new Attribute[] { // SYM_SYSTEM
            new Attribute(SYMBOL,             _SYS,   0),
            new Attribute(ASTROLOGY,          _SYS,   1),
            new Attribute(QABALAH,            _SYS,   2),
            new Attribute(TAROT,              _SYS,   3),
            new Attribute(ENOCHIAN,           _SYS,   4),
            new Attribute(ALCHEMY,            _SYS,   5),
            new Attribute(GEOMANCY,           _SYS,   6),
        }),*/
        new AttributeSet(LONGITUDE,_DIR,0,6), /* SYM_DIRECTION */
        new AttributeSet(JULIAN_CALENDAR,_CAL,0,3), /* SYM_CALENDAR */
        new AttributeSet(JANUARY,_MON,0,12), /* SYM_MONTH */
        new AttributeSet(SPIRIT,_ELE,0,5), /* SYM_PLANET */
        /*new AttributeSet(new Attribute[] { // SYM_ELEMENT
            new Attribute(SPIRIT,             _ELE,   0),
            new Attribute(FIRE,               _ELE,   1),
            new Attribute(AIR,                _ELE,   2),
            new Attribute(WATER,              _ELE,   3),
            new Attribute(EARTH,              _ELE,   4),
        }),*/
        new AttributeSet(SUN,_CPL,0,7), /* SYM_PLANET */
        /*new AttributeSet(new Attribute[] { // SYM_PLANET
            new Attribute(SUN,                _CPL,   0),
            new Attribute(MOON,               _CPL,   1),
            new Attribute(MERCURY,            _CPL,   2),
            new Attribute(VENUS,              _CPL,   3),
            new Attribute(MARS,               _CPL,   4),
            new Attribute(JUPITER,            _CPL,   5),
            new Attribute(SATURN,             _CPL,   6),
        }),*/
        new AttributeSet(ARIES,_ZOD,0,12), /* SYM_ZODIAC */
        /*new AttributeSet(new Attribute[] { // SYM_ZODIAC
            new Attribute(ARIES,              _ZOD,   0),
            new Attribute(TAURUS,             _ZOD,   1),
            new Attribute(GEMINI,             _ZOD,   2),
            new Attribute(CANCER,             _ZOD,   3),
            new Attribute(LEO,                _ZOD,   4),
            new Attribute(VIRGO,              _ZOD,   5),
            new Attribute(LIBRA,              _ZOD,   6),
            new Attribute(SCORPIO,            _ZOD,   7),
            new Attribute(SAGITTARIUS,        _ZOD,   8),
            new Attribute(CAPRICORN,          _ZOD,   9),
            new Attribute(AQUARIUS,           _ZOD,  10),
            new Attribute(PISCES,             _ZOD,  11),
        }),*/
        new AttributeSet(new Attribute[] { /* ASTRO_CONCEPT */
            new Attribute(Concept.ASTRO_CONCEPT,_CON,8),
            new Attribute(Concept.ASTRO_ELEMENT,_CON,9),
            new Attribute(Concept.ASTRO_QUALITY,_CON,10),
            new Attribute(Concept.ASTRO_ENERGY,_CON,11),
            new Attribute(Concept.ASTRO_ZODIAC,_CON,12),
            new Attribute(Concept.ASTRO_PLANET,_CON,13),
            new Attribute(Concept.ASTRO_MPLANET,_CON,14),
            new Attribute(Concept.ASTRO_FSTAR,_CON,15),
            new Attribute(Concept.ASTRO_POINT,_CON,16),
            new Attribute(Concept.ASTRO_LOT,_CON,17),
            new Attribute(Concept.ASTRO_HOUSE,_CON,18),
            new Attribute(Concept.ASTRO_ASPECT,_CON,19),
            new Attribute(Concept.ASTRO_ASPPAT,_CON,20),
            new Attribute(Concept.ASTRO_SHAPING,_CON,21),
            new Attribute(Concept.ASTRO_FACTOR,_CON,22),
            new Attribute(Concept.ASTRO_HSYSTEM,_CON,23),
            new Attribute(Concept.ASTRO_CHART,_CON,24),
            new Attribute(Concept.SYM_DIRECTION,_CON,2),
            new Attribute(Concept.SYM_CALENDAR,_CON,3),
            new Attribute(Concept.SYM_MONTH,_CON,4),
            new Attribute(Concept.ASTRO_PLZ,_CON,25),
            new Attribute(Concept.ASTRO_MPZ,_CON,26),
            new Attribute(Concept.ASTRO_FSZ,_CON,27),
            new Attribute(Concept.ASTRO_PLH,_CON,28),
            new Attribute(Concept.ASTRO_MPH,_CON,29),
            new Attribute(Concept.ASTRO_FSH,_CON,30),
            new Attribute(Concept.ASTRO_HZ,_CON,31),
            new Attribute(Concept.ASTRO_PLPLASP,_CON,32),
            new Attribute(Concept.ASTRO_PLMPASP,_CON,33),
            new Attribute(Concept.ASTRO_PLFSASP,_CON,34),
            new Attribute(Concept.ASTRO_MPMPASP,_CON,35),
            new Attribute(Concept.ASTRO_FSFSASP,_CON,36),
            new Attribute(Concept.ASTRO_MPFSASP,_CON,37),
            new Attribute(Concept.ASTRO_PLHASP,_CON,38),
            new Attribute(Concept.ASTRO_HMPASP,_CON,39),
            new Attribute(Concept.ASTRO_HFSASP,_CON,40),
            new Attribute(Concept.ASTRO_PLFAC,_CON,41),
            new Attribute(Concept.ASTRO_HFAC,_CON,42),
            new Attribute(Concept.ASTRO_PLPLFAC,_CON,43),
            new Attribute(Concept.ASTRO_PATELE,_CON,44),
            new Attribute(Concept.ASTRO_PATQUA,_CON,45),
            new Attribute(Concept.ASTRO_PLPAT,_CON,46),
            new Attribute(Concept.ASTRO_MPPAT,_CON,47),
            new Attribute(Concept.ASTRO_FSPAT,_CON,48),
            new Attribute(Concept.ASTRO_HPAT,_CON,49),
        }),
        new AttributeSet(new Attribute[] { /* ASTRO_ELEMENT */
            new Attribute(ASTRO_FIRE,_ELE,1),
            new Attribute(ASTRO_EARTH,_ELE,4),
            new Attribute(ASTRO_AIR,_ELE,2),
            new Attribute(ASTRO_WATER,_ELE,3),
        }),
        new AttributeSet(ASTRO_CARDINAL,_QUA,0,3), /* ASTRO_QUALITY */
        /*new AttributeSet(new Attribute[] { // ASTRO_QUALITY
            new Attribute(ASTRO_CARDINAL,     _QUA,   0),
            new Attribute(ASTRO_FIXED,        _QUA,   1),
            new Attribute(ASTRO_MUTABLE,      _QUA,   2),
        }),*/
        new AttributeSet(ASTRO_MALE,_ENE,0,2), /* ASTRO_ENERGY */
        /*new AttributeSet(new Attribute[] { // ASTRO_ENERGY
            new Attribute(ASTRO_MALE,         _ENE,   0),
            new Attribute(ASTRO_FEMALE,       _ENE,   1),
        }),*/
        new AttributeSet(ASTRO_ARIES,_ZOD,0,12), /* ASTRO_ZODIAC */
        /*}),new AttributeSet(new Attribute[] { // ASTRO_ZODIAC
            new Attribute(ASTRO_ARIES,        _ZOD,   0),
            new Attribute(ASTRO_TAURUS,       _ZOD,   1),
            new Attribute(ASTRO_GEMINI,       _ZOD,   2),
            new Attribute(ASTRO_CANCER,       _ZOD,   3),
            new Attribute(ASTRO_LEO,          _ZOD,   4),
            new Attribute(ASTRO_VIRGO,        _ZOD,   5),
            new Attribute(ASTRO_LIBRA,        _ZOD,   6),
            new Attribute(ASTRO_SCORPIO,      _ZOD,   7),
            new Attribute(ASTRO_SAGITTARIUS,  _ZOD,   8),
            new Attribute(ASTRO_CAPRICORN,    _ZOD,   9),
            new Attribute(ASTRO_AQUARIUS,     _ZOD,  10),
            new Attribute(ASTRO_PISCES,       _ZOD,  11),
        }),*/
        new AttributeSet(new Attribute[] { // ASTRO_PLANET
            new Attribute(ASTRO_SUN,_CPL,0),
            new Attribute(ASTRO_MOON,_CPL,1),
            new Attribute(ASTRO_MERCURY,_CPL,2),
            new Attribute(ASTRO_VENUS,_CPL,3),
            new Attribute(ASTRO_MARS,_CPL,4),
            new Attribute(ASTRO_JUPITER,_CPL,5),
            new Attribute(ASTRO_SATURN,_CPL,6),
            new Attribute(ASTRO_URANUS,_PLA,1),
            new Attribute(ASTRO_NEPTUNE,_PLA,2),
            new Attribute(ASTRO_PLUTO,_PLA,3),
        }),
        new AttributeSet(new Attribute[] { /* ASTRO_MPLANET */
            new Attribute(ASTRO_CHIRON,_MPL,0),
            new Attribute(ASTRO_CERES,_MPL,1),
            new Attribute(ASTRO_PALLAS,_MPL,2),
            new Attribute(ASTRO_JUNO,_MPL,3),
            new Attribute(ASTRO_VESTA,_MPL,4),
        }),
        new AttributeSet(ASTRO_FIXED_STAR_INDEX,_FST,0,ASTRO_FIXED_STAR_LENGTH), /* ASTRO_FSTAR */
        new AttributeSet(ASTRO_NNODE,_APT,0,7), /* ASTRO_POINT */
        /*new AttributeSet(new Attribute[] { // ASTRO_POINT
            new Attribute(ASTRO_NNODE,        _APT,   0),
            new Attribute(ASTRO_TNNODE,       _APT,   1),
            new Attribute(ASTRO_SNODE,        _APT,   2),
            new Attribute(ASTRO_TSNODE,       _APT,   3),
            new Attribute(ASTRO_LILITH,       _APT,   4),
            new Attribute(ASTRO_VERTEX,       _APT,   5),
            new Attribute(ASTRO_EASTPOINT,    _APT,   6),
        }),*/
        new AttributeSet(ASTRO_FORTUNE,_LOT,0,1), /* ASTRO_LOT */
        /*new AttributeSet(new Attribute[] { // ASTRO_LOT
            new Attribute(ASTRO_FORTUNE,      _LOT,   0),
        }),*/
        new AttributeSet(new Attribute[] { /* ASTRO_HOUSE */
            new Attribute(ASTRO_ASCENDANT,_HOU,0),
            new Attribute(ASTRO_2ND_HOUSE,_HOU,2),
            new Attribute(ASTRO_3RD_HOUSE,_HOU,4),
            new Attribute(ASTRO_4TH_HOUSE,_HOU,6),
            new Attribute(ASTRO_5TH_HOUSE,_HOU,8),
            new Attribute(ASTRO_6TH_HOUSE,_HOU,10),
            new Attribute(ASTRO_7TH_HOUSE,_HOU,12),
            new Attribute(ASTRO_8TH_HOUSE,_HOU,14),
            new Attribute(ASTRO_9TH_HOUSE,_HOU,16),
            new Attribute(ASTRO_MC,_HOU,18),
            new Attribute(ASTRO_11TH_HOUSE,_HOU,20),
            new Attribute(ASTRO_12TH_HOUSE,_HOU,22),
        }),
        new AttributeSet(CONJUNCTION,_ASP,0,13), /* ASTRO_ASPECT */
        /*new AttributeSet(new Attribute[] { // ASTRO_ASPECT
            new Attribute(CONJUNCTION,        _ASP,   0),
            new Attribute(SEMISEXTILE,        _ASP,   1),
            new Attribute(DECILE,             _ASP,   2),
            new Attribute(NOVILE,             _ASP,   3),
            new Attribute(SEMISQUARE,         _ASP,   4),
            new Attribute(SEPTILE,            _ASP,   5),
            new Attribute(SEXTILE,            _ASP,   6),
            new Attribute(QUINTILE,           _ASP,   7),
            new Attribute(SQUARE,             _ASP,   8),
            new Attribute(TRINE,              _ASP,   9),
            new Attribute(SESQUIQUADRATE,     _ASP,  10),
            new Attribute(QUINCUNX,           _ASP,  11),
            new Attribute(OPPOSITION,         _ASP,  12),
        }),*/
        new AttributeSet(STELLIUM,_APA,0,10), /* ASTRO_ASPPAT */
        /*new AttributeSet(new Attribute[] { // ASTRO_ASPPAT
            new Attribute(STELLIUM,           _APA,   0),
            new Attribute(TSQUARE,            _APA,   1),
            new Attribute(GSQUARE,            _APA,   2),
            new Attribute(GCROSS,             _APA,   3),
            new Attribute(GTRINE,             _APA,   4),
            new Attribute(YOD,                _APA,   5),
            new Attribute(MYSTRECT,           _APA,   6),
            new Attribute(KITE,               _APA,   7),
            new Attribute(GQUINTILE,          _APA,   8),
            new Attribute(HEXAGRAM,           _APA,   9),
        }),*/
        new AttributeSet(BOWL,_SHA,0,8), /* ASTRO_SHAPING */
        /*new AttributeSet(new Attribute[] { // ASTRO_SHAPING
            new Attribute(BOWL,               _SHA,   0),
            new Attribute(WEDGE,              _SHA,   1),
            new Attribute(SEESAW,             _SHA,   2),
            new Attribute(SPLAY,              _SHA,   3),
            new Attribute(LOCOMOTIVE,         _SHA,   4),
            new Attribute(BUCKET,             _SHA,   5),
            new Attribute(BUNDLE,             _SHA,   6),
            new Attribute(SPLASH,             _SHA,   7),
        }),*/
        new AttributeSet(new Attribute[] { /* ASTRO_FACTOR */
            new Attribute(RULER_PLANET,_FAC,0),
            new Attribute(RULER_HOUSE,_FAC,1),
            new Attribute(ASCENDING,_FAC,2),
            new Attribute(ANGULAR,_FAC,3),
            new Attribute(MRECEPTIVE,_FAC,4),
        }),
        new AttributeSet(HSYS_PLACIDUS,_HSY,0,2), /* ASTRO_HSYSTEM */
        /*new AttributeSet(new Attribute[] { // ASTRO_HSYSTEM
            new Attribute(HSYS_PLACIDUS,      _HSY,   0),
            new Attribute(HSYS_KOCH,          _HSY,   1),
        }),*/
        new AttributeSet(new Attribute[] { /* ASTRO_CHART */
            new Attribute(NATAL,_CHT,0),
            new Attribute(SYNASTRY,_CHT,1),
            new Attribute(COMPOSITE,_CHT,2),
            new Attribute(GEOCENTRIC,_CHT,3),
            new Attribute(HELIOCENTRIC,_CHT,4),
            new Attribute(SIDEREAL,_CHT,5),
            new Attribute(TROPICAL,_CHT,6),
            new Attribute(ASPECT_IN_SIGN,_CHT,7),
        }),
        /* ASTRO_PLZ - ASTRO_HPAT */
        null,null,null,null,null,null,null,null,null,null,null,null,
        null,null,null,null,null,null,null,null,null,null,null,null,
        null,
    };

    /**
     * The Concept inner class contains data for each group of symbols.
     * A concept should be considered a group of symbols that are inherently
     * connected, e.g. the elements, which consists of spirit, fire, ait, water and
     * earth. There can be many similar concepts, containing the same
     * or similar sets of symbols. For instance, astrology has no spirit element
     * and has a different ordering in the arrangement of the zodiac, than
     * the elemental attributions of the Tetragrammaton. Both are individual
     * concepts of the same set of elements.
     */
    public static class Concept {
        public static final int SYM_CONCEPT = 0; // 0x00
        public static final int SYM_SYSTEM = 1; // 0x01
        public static final int SYM_DIRECTION = 2; // 0x02
        public static final int SYM_CALENDAR = 3; // 0x03
        public static final int SYM_MONTH = 4; // 0x04
        public static final int SYM_ELEMENT = 5; // 0x05
        public static final int SYM_PLANET = 6; // 0x06
        public static final int SYM_ZODIAC = 7; // 0x07
        public static final int ASTRO_CONCEPT = 8; // 0x08
        public static final int ASTRO_ELEMENT = 9; // 0x09
        public static final int ASTRO_QUALITY = 10; // 0x0a
        public static final int ASTRO_ENERGY = 11; // 0x0b
        public static final int ASTRO_ZODIAC = 12; // 0x0c
        public static final int ASTRO_PLANET = 13; // 0x0d
        public static final int ASTRO_MPLANET = 14; // 0x0e
        public static final int ASTRO_FSTAR = 15; // 0x0f
        public static final int ASTRO_POINT = 16; // 0x10
        public static final int ASTRO_LOT = 17; // 0x11
        public static final int ASTRO_HOUSE = 18; // 0x12
        public static final int ASTRO_ASPECT = 19; // 0x13
        public static final int ASTRO_ASPPAT = 20; // 0x14
        public static final int ASTRO_SHAPING = 21; // 0x15
        public static final int ASTRO_FACTOR = 22; // 0x16
        public static final int ASTRO_HSYSTEM = 23; // 0x17
        public static final int ASTRO_CHART = 24; // 0x18
        public static final int ASTRO_PLZ = 25; // 0x19
        public static final int ASTRO_MPZ = 26; // 0x1a
        public static final int ASTRO_FSZ = 27; // 0x1b
        public static final int ASTRO_PLH = 28; // 0x1c
        public static final int ASTRO_MPH = 29; // 0x1d
        public static final int ASTRO_FSH = 30; // 0x1e
        public static final int ASTRO_HZ = 31; // 0x1f
        public static final int ASTRO_PLPLASP = 32; // 0x20
        public static final int ASTRO_PLMPASP = 33; // 0x21
        public static final int ASTRO_PLFSASP = 34; // 0x22
        public static final int ASTRO_MPMPASP = 35; // 0x23
        public static final int ASTRO_FSFSASP = 36; // 0x24
        public static final int ASTRO_MPFSASP = 37; // 0x25
        public static final int ASTRO_PLHASP = 38; // 0x26
        public static final int ASTRO_HMPASP = 39; // 0x27
        public static final int ASTRO_HFSASP = 40; // 0x28
        public static final int ASTRO_PLFAC = 41; // 0x29
        public static final int ASTRO_HFAC = 42; // 0x2a
        public static final int ASTRO_PLPLFAC = 43; // 0x2b
        public static final int ASTRO_PATELE = 44; // 0x2c
        public static final int ASTRO_PATQUA = 45; // 0x2d
        public static final int ASTRO_PLPAT = 46; // 0x2e
        public static final int ASTRO_MPPAT = 47; // 0x2f
        public static final int ASTRO_FSPAT = 48; // 0x30
        public static final int ASTRO_HPAT = 49; // 0x31

        private static int _counter;
        private final int fmt, c1, c2, c3, c4, c5;

        public final int id;
        //		public final Attribute[] attributes;
        public final AttributeSet attributes;
        public final int system;
        public final int mod;
        public final int number;
        public String name = null;
        public String format = null;
        public Concept first = null;
        public Concept second = null;
        public Concept third = null;
        public Concept fourth = null;
        public Concept fifth = null;

        private Concept() {
            id = system = mod = number = fmt = c1 = c2 = c3 = c4 = c5 = -1;
            attributes = null;
        }

        private Concept(int s,int f,int m,int a) {
            this(s,f,m,a,-1,-1,-1,-1,1);
        }

        private Concept(int s,int f,int m,int a,int b) {
            this(s,f,m,a,b,-1,-1,-1,2);
        }

        private Concept(int s,int f,int m,int a,int b,int c) {
            this(s,f,m,a,b,c,-1,-1,3);
        }

        private Concept(int s,int f,int m,int a,int b,int c,int d) {
            this(s,f,m,a,b,c,d,-1,4);
        }

        private Concept(int s,int f,int m,int a,int b,int c,int d,int e) {
            this(s,f,m,a,b,c,d,e,5);
        }

        private Concept(int s,int f,int m,int a,int b,int c,int d,int e,int n) {
            id = _counter++;
            system = s;
            fmt = f;
            mod = m;
            number = n;
            c1 = a;
            c2 = b;
            c3 = c;
            c4 = d;
            c5 = e;
            attributes = _A[id];
        }

        public void connect() {
            if(number>=1) first = _C[c1];
            if(number>=2) second = _C[c2];
            if(number>=3) third = _C[c3];
            if(number>=4) fourth = _C[c4];
            if(number>=5) fifth = _C[c5];
        }

        public void update() {
            name = _S[_N[_CON]+id];
            format = _F[fmt];
            if(attributes!=null)
                attributes.update();
				/*for(int i=0; i<attributes.length; ++i)
					attributes[i].update();*/
        }

        public void split(Symbol s) {
            long id = s.id;
            if((id&0x80)==0) id >>= 8;
            else id >>= 16;
//Log.d(APP,TAG+".Concept.split(id: "+id+", first: "+(first!=null? first.mod : 0)+", second: "+(second!=null? second.mod : 0)+", third: "+(third!=null? third.mod : 0)+")");
            if(number>=1) {
                s.first = first.getId((int)(id%first.mod));
                if(number>=2) {
                    id /= first.mod;
                    s.second = second.getId((int)(id%second.mod));
                    if(number>=3) {
                        id /= second.mod;
                        s.third = third.getId((int)(id%third.mod));
                        if(number>=4) {
                            id /= third.mod;
                            s.fourth = fourth.getId((int)(id%fourth.mod));
                            if(number>=5) {
                                id /= fourth.mod;
                                s.fifth = fifth.getId((int)(id%fifth.mod));
                            }
                        }
                    }
                }
            }
//Log.d(APP,TAG+".Concept.split(id: "+this.id+", s.first: "+s.first+", s.second: "+s.second+", s.third: "+s.third+", s.fourth: "+s.fourth+", s.fifth: "+s.fifth+")");
        }

        public Concept getConcept(int id) {
            if(id<=0xffff) return _C[id];
            else return _C[id >> 16];
			/*if(n>=0 && n<attributes.length) {
				Attribute a = attributes[n];
				if(a.id<=0xffff) return _C[a.id];
				else return _C[a.id>>16];
			}
			return null;*/
        }

        public int indexOf(int id) {
            if(attributes!=null)
                return attributes.indexOf(id);
				/*for(int i=0; i<attributes.length; ++i)
					if(attributes[i].id==n) return i;*/
            return -1;
        }

        public int getId(int n) {
            if(attributes!=null)
                return attributes.getId(n);
			/*if(attributes!=null && n>=0 && n<attributes.length)
				return attributes[n].id;*/
            return -1;
        }

        public long makeId(int s1) {
            if(number!=1) return -1l;
            s1 = first.indexOf(s1);
//Log.d(APP,TAG+".Concept.makeId(id: "+id+", s1: "+s1+")");
            if(s1<0) return -1l;
            long i = s1;
            if(id<=0x7f) i = (i<<8)|id;
            else i = (i<<16)|((id&0x7F80)<<1)|0x80|(id&0x7F);
            return i;
        }

        public long makeId(int s1,int s2) {
            if(number!=2) return -1l;
            s1 = first.indexOf(s1);
            s2 = second.indexOf(s2);
//Log.d(APP,TAG+".Concept.makeId(id: "+id+", s1: "+s1+", s2: "+s2+")");
            if(s1<0 || s2<0) return -1l;
            int n;
            if(first==second && s1>s2) {
                n = s1;
                s1 = s2;
                s2 = n;
            }
//Log.d(APP,TAG+".Concept.makeId(id: "+id+", first: "+first.mod+", second: "+second.mod+", s1: "+s1+", s2: "+s2+")");
            long i = (s2*first.mod)+s1;
            if(id<=0x7f) i = (i<<8)|id;
            else i = (i<<16)|((id&0x7F80)<<1)|0x80|(id&0x7F);
            return i;
        }

        public long makeId(int s1,int s2,int s3) {
            if(number!=3) return -1l;
            s1 = first.indexOf(s1);
            s2 = second.indexOf(s2);
            s3 = third.indexOf(s3);
//Log.d(APP,TAG+".Concept.makeId(id: "+id+", s1: "+s1+", s2: "+s2+", s3: "+s3+")");
            if(s1<0 || s2<0 || s3<0) return -1l;
            int n;
            if(second==third && s2>s3) {
                n = s2;
                s2 = s3;
                s3 = n;
            }
            if(first==second && s1>s2) {
                n = s1;
                s1 = s2;
                s2 = n;
            }
            long i = (s3*second.mod+s2)*first.mod+s1;
            if(id<=0x7f) i = (i<<8)|id;
            else i = (i<<16)|((id&0x7F80)<<1)|0x80|(id&0x7F);
            return i;
        }

        public long makeId(int s1,int s2,int s3,int s4) {
            if(number!=4) return -1l;
            s1 = first.indexOf(s1);
            s2 = second.indexOf(s2);
            s3 = third.indexOf(s3);
            s4 = fourth.indexOf(s4);
//Log.d(APP,TAG+".Concept.makeId(id: "+id+", s1: "+s1+", s2: "+s2+", s3: "+s3+", s4: "+s4+")");
            if(s1<0 || s2<0 || s3<0 || s4<0) return -1l;
            int n;
            if(third==fourth && s3>s4) {
                n = s3;
                s3 = s4;
                s4 = n;
            }
            if(second==third && s2>s3) {
                n = s2;
                s2 = s3;
                s3 = n;
            }
            if(first==second && s1>s2) {
                n = s1;
                s1 = s2;
                s2 = n;
            }
            long i = ((s4*third.mod+s3)*second.mod+s2)*first.mod+s1;
            if(id<=0x7f) i = (i<<8)|id;
            else i = (i<<16)|((id&0x7F80)<<1)|0x80|(id&0x7F);
            return i;
        }

        public long makeId(int s1,int s2,int s3,int s4,int s5) {
            if(number!=5) return -1l;
            s1 = first.indexOf(s1);
            s2 = second.indexOf(s2);
            s3 = third.indexOf(s3);
            s4 = fourth.indexOf(s4);
            s5 = fifth.indexOf(s5);
//Log.d(APP,TAG+".Concept.makeId(id: "+id+", s1: "+s1+", s2: "+s2+", s3: "+s3+", s4: "+s4+", s5: "+s5+")");
            if(s1<0 || s2<0 || s3<0 || s4<0 || s5<0) return -1l;
            int n;
            if(fourth==fifth && s4>s5) {
                n = s4;
                s4 = s5;
                s5 = n;
            }
            if(third==fourth && s3>s4) {
                n = s3;
                s3 = s4;
                s4 = n;
            }
            if(second==third && s2>s3) {
                n = s2;
                s2 = s3;
                s3 = n;
            }
            if(first==second && s1>s2) {
                n = s1;
                s1 = s2;
                s2 = n;
            }
            long i = (((s5*fourth.mod+s4)*third.mod+s3)*second.mod+s2)*first.mod+s1;
            if(id<=0x7f) i = (i<<8)|id;
            else i = (i<<16)|((id&0x7F80)<<1)|0x80|(id&0x7F);
            return i;
        }

        /**
         * Returns the name of the symbol id n, which must be in the
         * attributes list of the concept.
         */
        public String getName(int id) {
            if(attributes!=null) {
                int n = attributes.indexOf(id);
                if(n>=0)
                    return _S[attributes.getIndex(n)];
            }
            return null;
			/*n = indexOf(n);
			if(attributes==null || n<0 || n>=attributes.length) return null;
			return _S[attributes[n].index];*/
        }

        private String getName(int id,int step) {
            if(attributes!=null) {
                int n = attributes.indexOf(id);
                if(n>=0) {
                    n = attributes.getIndex(n);
                    if(n+step>=0 && n+step<_S.length)
                        return _S[n+step];
                }
            }
            return null;
			/*n = indexOf(n);
			if(attributes==null || n<0 || n>=attributes.length || n+step<0 || n+step>=attributes.length) return null;
			return _S[attributes[n].index+step];*/
        }

        public String[] getNames() { return getNames(0); }

        public String[] getNames(int step) {
            if(attributes!=null) {
                if(attributes.attributes!=null) {
                    Attribute[] a = attributes.attributes;
                    String[] s = new String[a.length];
                    for(int i = 0; i<a.length; ++i) s[i] = _S[a[i].index+step];
                    return s;
                } else {
                    String[] s = new String[attributes.length];
                    for(int i = 0; i<attributes.length; ++i)
                        s[i] = _S[attributes.startIndex+i+step];
                    return s;
                }
            }
            return null;
        }
    }

    /**
     * The list of concepts must be static, and cannot be rearranged or all
     * symbol ids will become broken. Add new concepts at the end of the list.
     */
    private static final Concept[] _C = {
        /* System "Concepts" */
        new Concept(SYMBOL,0,0x7FFF,Concept.SYM_CONCEPT),
        /* System "Systems" */
        new Concept(SYMBOL,0,0x7FFF,Concept.SYM_SYSTEM),
        /* System "Directions" */
        new Concept(SYMBOL,0,6,Concept.SYM_DIRECTION),
        /* System "Calendars" */
        new Concept(SYMBOL,0,2,Concept.SYM_CALENDAR),
        /* System "Months" */
        new Concept(SYMBOL,0,12,Concept.SYM_MONTH),
        /* System "Elements" */
        new Concept(SYMBOL,0,5,Concept.SYM_ELEMENT),
        /* System "Planets" */
        new Concept(SYMBOL,0,7,Concept.SYM_PLANET),
        /* System "Zodiac" */
        new Concept(SYMBOL,0,12,Concept.SYM_ZODIAC),
        /* Astrology "Concept" */
        new Concept(ASTROLOGY,0,45,Concept.ASTRO_CONCEPT),
        /* Astrology "Elements" */
        new Concept(ASTROLOGY,0,4,Concept.ASTRO_ELEMENT),
        /* Astrology "Qualities" */
        new Concept(ASTROLOGY,0,3,Concept.ASTRO_QUALITY),
        /* Astrology "Energies" */
        new Concept(ASTROLOGY,0,2,Concept.ASTRO_ENERGY),
        /* Astrology "Zodiac" */
        new Concept(ASTROLOGY,0,12,Concept.ASTRO_ZODIAC),
        /* Astrology "Planets" */
        new Concept(ASTROLOGY,0,10,Concept.ASTRO_PLANET),
        /* Astrology "Minor Planets" */
        new Concept(ASTROLOGY,0,999,Concept.ASTRO_MPLANET),
        /* Astrology "Fixed Stars" */
        new Concept(ASTROLOGY,0,ASTRO_FIXED_STAR_LENGTH,Concept.ASTRO_FSTAR),
        /* Astrology "Point" */
        new Concept(ASTROLOGY,0,7,Concept.ASTRO_POINT),
        /* Astrology "Arabic Parts / Lots" */
        new Concept(ASTROLOGY,0,413,Concept.ASTRO_LOT),
        /* Astrology "Houses" */
        new Concept(ASTROLOGY,0,12,Concept.ASTRO_HOUSE),
        /* Astrology "Aspects" */
        new Concept(ASTROLOGY,0,13,Concept.ASTRO_ASPECT),
        /* Astrology "Aspect Patterns" */
        new Concept(ASTROLOGY,0,10,Concept.ASTRO_ASPPAT),
        /* Astrology "Shapings" */
        new Concept(ASTROLOGY,0,8,Concept.ASTRO_SHAPING),
        /* Astrology "Factors" */
        new Concept(ASTROLOGY,0,0x1F,Concept.ASTRO_FACTOR),
        /* Astrology "House Systems" */
        new Concept(ASTROLOGY,0,2,Concept.ASTRO_HSYSTEM),
        /* Astrology "Chart Types" */
        new Concept(ASTROLOGY,0,0xFF,Concept.ASTRO_CHART),
        /* Astrology "Planet in Zodiac" */
        new Concept(ASTROLOGY,1,0,Concept.ASTRO_ZODIAC,Concept.ASTRO_PLANET),
        /* Astrology "Minor Planet in Zodiac" */
        new Concept(ASTROLOGY,1,0,Concept.ASTRO_ZODIAC,Concept.ASTRO_MPLANET),
        /* Astrology "Fixed Star in Zodiac" */
        new Concept(ASTROLOGY,1,0,Concept.ASTRO_ZODIAC,Concept.ASTRO_FSTAR),
        /* Astrology "Planet in House" */
        new Concept(ASTROLOGY,1,0,Concept.ASTRO_HOUSE,Concept.ASTRO_PLANET),
        /* Astrology "Minor Planet in House" */
        new Concept(ASTROLOGY,1,0,Concept.ASTRO_HOUSE,Concept.ASTRO_MPLANET),
        /* Astrology "Fixed Star in House" */
        new Concept(ASTROLOGY,1,0,Concept.ASTRO_HOUSE,Concept.ASTRO_FSTAR),
        /* Astrology "House in Zodiac" */
        new Concept(ASTROLOGY,1,0,Concept.ASTRO_ZODIAC,Concept.ASTRO_HOUSE),
        /* Astrology "Planet and Planet in Aspect" */
        new Concept(ASTROLOGY,2,0,Concept.ASTRO_ASPECT,Concept.ASTRO_PLANET,Concept.ASTRO_PLANET),
        /* Astrology "Planet and Minor Planet in Aspect" */
        new Concept(ASTROLOGY,2,0,Concept.ASTRO_ASPECT,Concept.ASTRO_PLANET,Concept.ASTRO_MPLANET),
        /* Astrology "Planet and Fixed Star in Aspect" */
        new Concept(ASTROLOGY,2,0,Concept.ASTRO_ASPECT,Concept.ASTRO_PLANET,Concept.ASTRO_FSTAR),
        /* Astrology "Minor Planet and Minor Planet in Aspect" */
        new Concept(ASTROLOGY,2,0,Concept.ASTRO_ASPECT,Concept.ASTRO_MPLANET,Concept.ASTRO_MPLANET),
        /* Astrology "Fixed Star and Fixed Star in Aspect" */
        new Concept(ASTROLOGY,2,0,Concept.ASTRO_ASPECT,Concept.ASTRO_FSTAR,Concept.ASTRO_FSTAR),
        /* Astrology "Minor Planet and Fixed Star in Aspect" */
        new Concept(ASTROLOGY,2,0,Concept.ASTRO_ASPECT,Concept.ASTRO_MPLANET,Concept.ASTRO_FSTAR),
        /* Astrology "Planet and House in Aspect" */
        new Concept(ASTROLOGY,2,0,Concept.ASTRO_ASPECT,Concept.ASTRO_PLANET,Concept.ASTRO_HOUSE),
        /* Astrology "House and Minor Planet in Aspect" */
        new Concept(ASTROLOGY,2,0,Concept.ASTRO_ASPECT,Concept.ASTRO_HOUSE,Concept.ASTRO_MPLANET),
        /* Astrology "House and Fixed Star in Aspect" */
        new Concept(ASTROLOGY,2,0,Concept.ASTRO_ASPECT,Concept.ASTRO_HOUSE,Concept.ASTRO_FSTAR),
        /* Astrology "Planet Factors" */
        new Concept(ASTROLOGY,3,0,Concept.ASTRO_FACTOR,Concept.ASTRO_PLANET),
        /* Astrology "House Factors" */
        new Concept(ASTROLOGY,3,0,Concept.ASTRO_FACTOR,Concept.ASTRO_HOUSE),
        /* Astrology "Factor: Planet and Planet" */
        new Concept(ASTROLOGY,4,0,Concept.ASTRO_FACTOR,Concept.ASTRO_PLANET,Concept.ASTRO_PLANET),
        /* Astrology "Pattern of Element" */
        new Concept(ASTROLOGY,3,0,Concept.ASTRO_ASPPAT,Concept.ASTRO_ELEMENT),
        /* Astrology "Pattern of Quality" */
        new Concept(ASTROLOGY,3,0,Concept.ASTRO_ASPPAT,Concept.ASTRO_QUALITY),
        /* Astrology "Planet in Pattern" */
        new Concept(ASTROLOGY,1,0,Concept.ASTRO_ASPPAT,Concept.ASTRO_PLANET),
        /* Astrology "Minor Planet in Pattern" */
        new Concept(ASTROLOGY,1,0,Concept.ASTRO_ASPPAT,Concept.ASTRO_MPLANET),
        /* Astrology "Fixed Star in Pattern" */
        new Concept(ASTROLOGY,1,0,Concept.ASTRO_ASPPAT,Concept.ASTRO_FSTAR),
        /* Astrology "House in Pattern" */
        new Concept(ASTROLOGY,1,0,Concept.ASTRO_ASPPAT,Concept.ASTRO_HOUSE),
    };

    static {
        for(int i = 0; i<_C.length; ++i)
            _C[i].connect();
    }

	/*private static final int[] symbolToElement = {
		SPIRIT,  FIRE,   AIR,  WATER,  EARTH,        // Elements

		FIRE,    EARTH,  AIR,  WATER,                // Zodiac
		FIRE,    EARTH,  AIR,  WATER,
		FIRE,    EARTH,  AIR,  WATER,

		FIRE,    WATER,  AIR,  EARTH,  FIRE,         // Planets
		FIRE,    EARTH,  AIR,  WATER,  WATER,
	};*/

    /* String data: */
    private static String[] _S;  // Symbol Strings
    private static int[] _N;     // Index of each group
    private static int[] _L;     // Length of each group
    private static String[] _F;  // Symbol ouput formats

    public static void setStringData(String[][] data,String[] formats) {
        int i, j, n;
        String[] d;
        Attribute a;
        Concept c;

        for(i = 0,n = 0; i<data.length; ++i)
            n += data[i].length;
        _S = new String[n];
        _N = new int[data.length];
        _L = new int[data.length];

        for(i = 0,n = 0; i<data.length; ++i) {
            d = data[i];
            for(j = 0; j<d.length; ++j) _S[n+j] = d[j];
            _N[i] = n;
            _L[i] = d.length;
            n += d.length;
        }

//for(i=0; i<_S.length; ++i) {
//Log.d(APP,TAG+".setStringData("+i+": \""+_S[i]+"\")");
//}

        _F = formats;

        for(i = 0; i<_C.length; ++i)
            _C[i].update();
    }

    public static Concept getConcept(int id) {
        if(id<0 || id>=_C.length) return null;
        return _C[id];
    }

    public static long makeId(int concept,int s1) {
        if(concept>=0 && concept<_C.length) {
            Concept c = _C[concept];
            if(c.number==1) return c.makeId(s1);
        }
        return -1L;
    }

    public static long makeId(int concept,int s1,int s2) {
        if(concept>=0 && concept<_C.length) {
            Concept c = _C[concept];
            if(c.number==2) return c.makeId(s1,s2);
            if(c.number==1) return c.makeId(s1);
        }
        return -1L;
    }

    public static long makeId(int concept,int s1,int s2,int s3) {
        if(concept>=0 && concept<_C.length) {
            Concept c = _C[concept];
            if(c.number==3) return c.makeId(s1,s2,s3);
            if(c.number==2) return c.makeId(s1,s2);
            if(c.number==1) return c.makeId(s1);
        }
        return -1L;
    }

    public static long makeId(int concept,int s1,int s2,int s3,int s4) {
        if(concept>=0 && concept<_C.length) {
            Concept c = _C[concept];
            if(c.number==4) return c.makeId(s1,s2,s3,s4);
            if(c.number==3) return c.makeId(s1,s2,s3);
            if(c.number==2) return c.makeId(s1,s2);
            if(c.number==1) return c.makeId(s1);
        }
        return -1L;
    }

    public static long makeId(int concept,int s1,int s2,int s3,int s4,int s5) {
        if(concept>=0 && concept<_C.length) {
            Concept c = _C[concept];
//Log.d(APP,TAG+".makeId(concept: "+concept+", s1: "+s1+", s2: "+s2+", s3: "+s3+", s4: "+s4+", s5: "+s5+")");
            if(c.number==5) return c.makeId(s1,s2,s3,s4,s5);
            if(c.number==4) return c.makeId(s1,s2,s3,s4);
            if(c.number==3) return c.makeId(s1,s2,s3);
            if(c.number==2) return c.makeId(s1,s2);
            if(c.number==1) return c.makeId(s1);
        }
        return -1L;
    }

    public static long parseId(String str) {
        int i = str.indexOf('#');
        if(i>=0) str = str.substring(i+1);
        return Long.parseLong(str,16);
    }

	/*public static int symbolToElement(int s) {
		if(s<0 || s>=symbolToElement.length) return -1;
		return symbolToElement[s];
	}*/

    public static long getSystem(int s) { return makeId(Concept.SYM_SYSTEM,s); }

    public static long getDirection(int d) { return makeId(Concept.SYM_DIRECTION,d); }

    public static long getCalendar(int c) { return makeId(Concept.SYM_CALENDAR,c); }

    public static long getMonth(int m) { return makeId(Concept.SYM_MONTH,m); }

    public static long getElement(int e) { return makeId(Concept.SYM_ELEMENT,e); }

    public static long getPlanet(int p) { return makeId(Concept.SYM_PLANET,p); }

    public static long getZodiac(int z) { return makeId(Concept.SYM_ZODIAC,z); }

    public static long astrologyElement(int e) { return makeId(Concept.ASTRO_ELEMENT,e); }

    public static long astrologyQuality(int q) { return makeId(Concept.ASTRO_QUALITY,q); }

    public static long astrologyEnergy(int e) { return makeId(Concept.ASTRO_ENERGY,e); }

    public static long astrologyZodiac(int z) { return makeId(Concept.ASTRO_ZODIAC,z); }

    public static long astrologyPlanet(int p) { return makeId(Concept.ASTRO_PLANET,p); }

    public static long astrologyMinorPlanet(int m) { return makeId(Concept.ASTRO_MPLANET,m); }

    public static long astrologyFixedStar(int s) { return makeId(Concept.ASTRO_FSTAR,s); }

    public static long astrologyHouse(int h) { return makeId(Concept.ASTRO_HOUSE,h); }

    public static long astrologyAspect(int a) { return makeId(Concept.ASTRO_ASPECT,a); }

    public static long astrologyAspectPattern(int p) { return makeId(Concept.ASTRO_ASPPAT,p); }

    public static long astrologyShaping(int s) { return makeId(Concept.ASTRO_SHAPING,s); }

    public static long astrologyFactor(int f) { return makeId(Concept.ASTRO_FACTOR,f); }

    public static long astrologyHouseSystem(int h) { return makeId(Concept.ASTRO_HSYSTEM,h); }

    public static long astrologyPlanetInZodiac(int p,int z) { return makeId(Concept.ASTRO_PLZ,z,p); }

    public static long astrologyMinorPlanetInZodiac(int m,int z) { return makeId(Concept.ASTRO_MPZ,z,m); }

    public static long astrologyFixedStarInZodiac(int s,int z) { return makeId(Concept.ASTRO_FSZ,z,s); }

    public static long astrologyPlanetInHouse(int p,int h) { return makeId(Concept.ASTRO_PLH,h,p); }

    public static long astrologyMinorPlanetInHouse(int m,int h) { return makeId(Concept.ASTRO_MPH,h,m); }

    public static long astrologyFixedStarInHouse(int s,int h) { return makeId(Concept.ASTRO_FSH,h,s); }

    public static long astrologyHouseInZodiac(int h,int z) { return makeId(Concept.ASTRO_HZ,z,h); }

    public static long astrologyPlanetToPlanetAspect(int p1,int p2,int a) { return makeId(Concept.ASTRO_PLPLASP,a,p1,p2); }

    public static long astrologyPlanetToMinorPlanetAspect(int p,int m,int a) { return makeId(Concept.ASTRO_PLMPASP,a,p,m); }

    public static long astrologyPlanetToFixedStarAspect(int p,int s,int a) { return makeId(Concept.ASTRO_PLFSASP,a,p,s); }

    public static long astrologyMinorPlanetToMinorPlanetAspect(int m1,int m2,int a) { return makeId(Concept.ASTRO_MPMPASP,a,m1,m2); }

    public static long astrologyFixedStarToFixedStarAspect(int s1,int s2,int a) { return makeId(Concept.ASTRO_FSFSASP,a,s1,s2); }

    public static long astrologyMinorPlanetToFixedStarAspect(int m,int s,int a) { return makeId(Concept.ASTRO_MPFSASP,a,m,s); }

    public static long astrologyPlanetToHouseAspect(int p,int h,int a) { return makeId(Concept.ASTRO_PLHASP,a,p,h); }

    public static long astrologyMinorPlanetToHouseAspect(int m,int h,int a) { return makeId(Concept.ASTRO_HMPASP,a,h,m); }

    public static long astrologyFixedStarToHouseAspect(int s,int h,int a) { return makeId(Concept.ASTRO_HFSASP,a,h,s); }

    public static long astrologyPlanetFactor(int p,int f) { return makeId(Concept.ASTRO_PLFAC,f,p); }

    public static long astrologyHouseFactor(int h,int f) { return makeId(Concept.ASTRO_HFAC,f,h); }

    public static long astrologyPlanetToPlanetFactor(int p1,int p2,int f) { return makeId(Concept.ASTRO_PLPLFAC,f,p1,p2); }

    public static long astrologyPatternElement(int pt,int e) { return makeId(Concept.ASTRO_PATELE,pt,e); }

    public static long astrologyPatternQuality(int pt,int q) { return makeId(Concept.ASTRO_PATQUA,pt,q); }

    public static long astrologyPlanetInPattern(int p,int pt) { return makeId(Concept.ASTRO_PLPAT,pt,p); }

    public static long astrologyMinorPlanetInPattern(int m,int pt) { return makeId(Concept.ASTRO_MPPAT,pt,m); }

    public static long astrologyFixedStarInPattern(int s,int pt) { return makeId(Concept.ASTRO_FSPAT,pt,s); }

    public static long astrologyHouseInPattern(int h,int pt) { return makeId(Concept.ASTRO_HPAT,pt,h); }

    public static String[] getNames(int concept) {
        if(concept>=0 && concept<_C.length)
            return _C[concept].getNames();
        return null;
    }

    private static String[] getNames(int concept,int step) {
        if(concept>=0 && concept<_C.length)
            return _C[concept].getNames(step);
        return null;
    }

    public static String[] getConceptNames() { return getNames(Concept.SYM_CONCEPT); }

    public static String[] getSystemNames() { return getNames(Concept.SYM_SYSTEM); }

    public static String[] getDirectionNames() { return getNames(Concept.SYM_DIRECTION); }

    public static String[] getCalendarNames() { return getNames(Concept.SYM_CALENDAR); }

    public static String[] getMonthNames() { return getNames(Concept.SYM_MONTH); }

    public static String[] getElementNames() { return getNames(Concept.SYM_ELEMENT); }

    public static String[] getPlanetNames() { return getNames(Concept.SYM_PLANET); }

    public static String[] getZodiacNames() { return getNames(Concept.SYM_ZODIAC); }

	/*public static String[] astrologyCategoryNames()            { return getNames(_N[_ACA]  ,_L[_ACA],3); }
	public static String[] astrologyCategoryNamesPlur()        { return getNames(_N[_ACA]+1,_L[_ACA],3); }
	public static String[] astrologyCategoryNamesDefPlur()     { return getNames(_N[_ACA]+2,_L[_ACA],3); }*/

    public static String[] astrologyConceptNames() { return getNames(Concept.ASTRO_CONCEPT); }

    public static String[] astrologyElementNames() { return getNames(Concept.ASTRO_ELEMENT); }

    public static String[] astrologyQualityNames() { return getNames(Concept.ASTRO_QUALITY); }

    public static String[] astrologyEnergyNames() { return getNames(Concept.ASTRO_ENERGY); }

    public static String[] astrologyZodiacNames() { return getNames(Concept.ASTRO_ZODIAC); }

    public static String[] astrologyPlanetNames() { return getNames(Concept.ASTRO_PLANET); }

    public static String[] astrologyMinorPlanetNames() { return getNames(Concept.ASTRO_MPLANET); }

    public static String[] astrologyFixedStarNames() { return getNames(Concept.ASTRO_FSTAR); }

    public static String[] astrologyPointNames() { return getNames(Concept.ASTRO_POINT); }

    public static String[] astrologyArabicPartNames() { return getNames(Concept.ASTRO_LOT); }

    public static String[] astrologyHouseNames() { return getNames(Concept.ASTRO_HOUSE); }

    public static String[] astrologyHouseNamesShort() { return getNames(Concept.ASTRO_HOUSE,1); }

    public static String[] astrologyAspectNames() { return getNames(Concept.ASTRO_ASPECT); }

    public static String[] astrologyAspectPatternNames() { return getNames(Concept.ASTRO_ASPPAT); }

    public static String[] astrologyShapingNames() { return getNames(Concept.ASTRO_SHAPING); }

    public static String[] astrologyFactorNames() { return getNames(Concept.ASTRO_FACTOR); }

    public static String[] astrologyHouseSystemNames() { return getNames(Concept.ASTRO_HSYSTEM); }

    public static String[] astrologyChartTypeNames() { return getNames(Concept.ASTRO_CHART); }

	/*public static String[] hebrewLetterNames()                 { return getNames(_N[_HEB],_L[_HEB]); }

	public static String[] tarotArcanaNames()                  { return getNames(_N[_TAR],_L[_TAR]); }
	public static String[] tarotMinorArcanaNames()             { return getNames(_N[_TMA]  ,_L[_TMA],2); }
	public static String[] tarotMinorArcanaNamesPlur()         { return getNames(_N[_TMA]+1,_L[_TMA],2); }*/

    public static String getName(int concept,int n) {
        if(concept<0 || concept>=_C.length) return null;
        Concept c = _C[concept];
        int id = c.getId(n);
        return c.getName(id);
    }

    private static String getName(int concept,int n,int step) {
        if(concept<0 || concept>=_C.length) return null;
        Concept c = _C[concept];
        int id = c.getId(n);
        return c.getName(id,step);
    }

    public static String getName(int s) { return getName((s >> 16),s); }

    public static String getConceptName(int c) { return getName(Concept.SYM_CONCEPT,c); }

    public static String getSystemName(int s) { return getName(Concept.SYM_SYSTEM,s); }

    public static String getDirectionName(int d) { return getName(Concept.SYM_DIRECTION,d); }

    public static String getCalendarName(int c) { return getName(Concept.SYM_CALENDAR,c); }

    public static String getMonthName(int m) { return getName(Concept.SYM_MONTH,m-1); }

    public static String getElementName(int e) { return getName(Concept.SYM_ELEMENT,e); }

    public static String getPlanetName(int p) { return getName(Concept.SYM_PLANET,p); }

    public static String getZodiacName(int z) { return getName(Concept.SYM_ZODIAC,z); }

	/*public static String astrologyCategoryName(int c)          { c=c*3;  return c>=0 && c<_L[_ACA]? _S[_N[_ACA]+c] : null; }
	public static String astrologyCategoryNamePlur(int c)      { c=c*3+1;return c>=0 && c<_L[_ACA]? _S[_N[_ACA]+c] : null; }
	public static String astrologyCategoryNameDefPlur(int c)   { c=c*3+2;return c>=0 && c<_L[_ACA]? _S[_N[_ACA]+c] : null; }*/

    public static String astrologyConceptName(int c) { return getName(Concept.ASTRO_CONCEPT,c); }

    public static String astrologyElementName(int e) { return getName(Concept.ASTRO_ELEMENT,e); }

    public static String astrologyQualityName(int q) { return getName(Concept.ASTRO_QUALITY,q); }

    public static String astrologyEnergyName(int e) { return getName(Concept.ASTRO_ENERGY,e); }

    public static String astrologyZodiacName(int z) { return getName(Concept.ASTRO_ZODIAC,z); }

    public static String astrologyPlanetName(int p) { return getName(Concept.ASTRO_PLANET,p); }

    public static String astrologyMinorPlanetName(int m) { return getName(Concept.ASTRO_MPLANET,m); }

    public static String astrologyFixedStarName(int s) { return getName(Concept.ASTRO_FSTAR,s); }

    public static String astrologyPointName(int p) { return getName(Concept.ASTRO_POINT,p); }

    public static String astrologyArabicPartName(int a) { return getName(Concept.ASTRO_LOT,a); }

    public static String astrologyHouseName(int h) { return getName(Concept.ASTRO_HOUSE,h); }

    public static String astrologyHouseNameShort(int h) { return getName(Concept.ASTRO_HOUSE,h,1); }

    public static String astrologyAspectName(int a) { return getName(Concept.ASTRO_ASPECT,a); }

    public static String astrologyAspectPatternName(int p) { return getName(Concept.ASTRO_ASPPAT,p); }

    public static String astrologyShapingName(int s) { return getName(Concept.ASTRO_SHAPING,s); }

    public static String astrologyFactorName(int f) { return getName(Concept.ASTRO_FACTOR,f); }

    public static String astrologyHouseSystemName(int h) { return getName(Concept.ASTRO_HSYSTEM,h); }

    public static String astrologyChartTypeName(int c) { return getName(Concept.ASTRO_CHART,c); }

	/*public static String hebrewLetterName(int h)               { return h>=0 && h<_L[_HEB]? _S[_N[_HEB]+h] : null; }

	public static String tarotArcanaName(int a)                { return a>=0 && a<_L[_TAR]? _S[_N[_TAR]+a] : null; }
	public static String tarotMinorArcanaName(int m)           { m=m*2;  return m>=0 && m<_L[_TMA]? _S[_N[_TMA]+m] : null; }
	public static String tarotMinorArcanaNamePlur(int m)       { m=m*2+1;return m>=0 && m<_L[_TMA]? _S[_N[_TMA]+m] : null; }*/

    public static String getTitle(long id) {
        Symbol s = new Symbol(id);
        return s.getTitle();
    }


    public long id;
    public Concept concept;
    public int first;
    public int second;
    public int third;
    public int fourth;
    public int fifth;
    private String title;
    private long[] references;

    public Symbol() {
        reset();
    }

    public Symbol(int concept,int s1) {
        set(concept,s1,-1,-1,-1,-1);
    }

    public Symbol(int concept,int s1,int s2) {
        set(concept,s1,s2,-1,-1,-1);
    }

    public Symbol(int concept,int s1,int s2,int s3) {
        set(concept,s1,s2,s3,-1,-1);
    }

    public Symbol(int concept,int s1,int s2,int s3,int s4) {
        set(concept,s1,s2,s3,s4,-1);
    }

    public Symbol(int concept,int s1,int s2,int s3,int s4,int s5) {
        set(concept,s1,s2,s3,s4,s5);
    }

    public Symbol(long id) {
        set(id);
    }

    public Symbol(String str) {
        int i = str.indexOf('#');
        String s = "0";
        if(i>=0) s = str.substring(i+1);
        set(Base36.decode(s));
    }

    private void reset() {
        id = -1l;
        concept = null;
        first = -1;
        second = -1;
        third = -1;
        fourth = -1;
        fifth = -1;
        title = null;
        references = null;
    }

    private void set(int c,int s1,int s2,int s3,int s4,int s5) {
        reset();
        if(c<0 || c>=_C.length) return;
        long id = makeId(c,s1,s2,s3,s4,s5);
//Log.d(APP,TAG+".set(id: "+id+")");
        if(id<0l) return;
        this.id = id;
        concept = _C[c];
        concept.split(this);
    }

    public void set(long id) {
        reset();
        int n = -1;
        if((id&0x80)==0) n = (int)(id&0x7Fl);
        else n = (int)(((id&0xFF00l) >> 1)|(id&0x7Fl));
        if(n<0 || n>=_C.length) return;
        this.id = id;
        concept = _C[n];
        concept.split(this);
    }

    @Override
    public String toString() {
//		String str = Long.toHexString(id).toUpperCase();
        return Base36.encode(id);
    }

    public String getTitle() {
        if(title==null && concept!=null) {
            String s1, s2, s3, s4, s5;
            s1 = s2 = s3 = s4 = s5 = "";
            if(concept.number>=1) s1 = concept.first.getName(first);
            if(concept.number>=2) s2 = concept.second.getName(second);
            if(concept.number>=3) s3 = concept.third.getName(third);
            if(concept.number>=4) s4 = concept.fourth.getName(fourth);
            if(concept.number>=5) s5 = concept.fifth.getName(fifth);
            switch(concept.number) {
                case 1:
                    title = String.format(concept.format,s1);
                    break;
                case 2:
                    title = String.format(concept.format,s1,s2);
                    break;
                case 3:
                    title = String.format(concept.format,s1,s2,s3);
                    break;
                case 4:
                    title = String.format(concept.format,s1,s2,s3,s4);
                    break;
                case 5:
                    title = String.format(concept.format,s1,s2,s3,s4,s5);
                    break;
            }
        }
        return title;
    }

/*
	public long[] getReferences() {
		if(references==null) {
			int n = countReferences();
			if(n==0) return null;
			int index = 0;
			long[] refs = new long[n];
			switch(system) {
				case ASTROLOGY:
					switch(concept) {
						case ASTRO_GENSYM:
							index = getReferences(refs,index,first,second);
							break;

						case ASTRO_PLINZO:
							refs[index++] = astrologyPlanet(first);
							refs[index++] = astrologyZodiac(second);
							break;

						case ASTRO_PLINHO:
							refs[index++] = astrologyPlanet(first);
							refs[index++] = astrologyHouse(second);
							break;

						case ASTRO_HOINZO:
							refs[index++] = astrologyHouse(first);
							refs[index++] = astrologyZodiac(second);
							break;

						case ASTRO_ASPP2P:
							refs[index++] = astrologyPlanet(first);
							refs[index++] = astrologyPlanet(second);
							refs[index++] = astrologyAspect(third);
							break;

						case ASTRO_ASPP2H:
							refs[index++] = astrologyPlanet(first);
							refs[index++] = astrologyHouse(second);
							refs[index++] = astrologyAspect(third);
							break;

						case ASTRO_PLFACT:
							refs[index++] = astrologyFactor(second);
							refs[index++] = astrologyPlanet(first);
							break;

						case ASTRO_HOFACT:
							refs[index++] = astrologyFactor(second);
							refs[index++] = astrologyHouse(first);
							break;

						case ASTRO_MUTRE:
							refs[index++] = astrologyFactor(first);
							refs[index++] = astrologyPlanet(second);
							refs[index++] = astrologyPlanet(third);
							break;

						case ASTRO_PATEL:
						case ASTRO_PATQU:
//							int c1 = (si.c1>>8),c2 = (si.c1&0xff);
//							getCategoryHeader(str,c1,si.c2,0,true);
//							if(refs) *refs++ = conceptCode(c1,si.c2);
//							str.append(", ");
//							getCategoryHeader(str,c2,si.c3,0,false);
//							if(refs) *refs++ = conceptCode(c2,si.c3);
							break;

						case ASTRO_PLPAT:
						case ASTRO_HOPAT:break;
					}
					break; // ASTROLOGY
			}
			references = refs;
		}
		return references;
	}

	private int getReferences(long[] refs,int index,int c1,int c2) {
		switch(system) {
			case ASTROLOGY:
				if(c1!=ASTRO_CATEGORIES && c1!=ASTRO_FACTORS && c1!=ASTRO_DIRECTIONS && c1!=ASTRO_CALENDARS)
					refs[index++] = makeId(ASTRO_GENSYM,ASTRO_CATEGORIES,c1);
				if(c1==ASTRO_CATEGORIES) {
					int min = 0,max = 0;
					switch(c2) {
						case ASTRO_ELEMENTS:   min = ASTRO_FIRE;      max = ASTRO_WATER;break;
						case ASTRO_QUALITIES:  min = ASTRO_CARDINAL;  max = ASTRO_MUTABLE;break;
						case ASTRO_ENERGIES:   min = ASTRO_MALE;      max = ASTRO_FEMALE;break;
						case ASTRO_ZODIAC:     min = ASTRO_ARIES;     max = ASTRO_PISCES;break;
						case ASTRO_PLANETS:    min = ASTRO_SUN;       max = ASTRO_PLUTO;break;
						case ASTRO_HOUSES:     min = 0;               max = 11;break;
						case ASTRO_ASPECTS:    min = CONJUNCTION;     max = OPPOSITION;break;
						case ASTRO_PATTERNS:   min = STELLIUM;        max = HEXAGRAM;break;
						case ASTRO_SHAPINGS:   min = BOWL;            max = SPLASH;break;
						case ASTRO_HSYSTEMS:   min = HSYS_PLACIDUS;   max = HSYS_KOCH;break;
					}
					if(min>=0 && min<max && max>0)
						for(int i=min; i<=max; ++i)
							refs[index++]= makeId(ASTRO_GENSYM,c2,i);
				}
				break; // ASTROLOGY

		}
		return index;
	}

	private int countReferences() {
		int n = 0;
		switch(system) {
			case ASTROLOGY:
				switch(concept) {
					case ASTRO_GENSYM:n += countReferences(first,second);break;
					case ASTRO_PLINZO:
					case ASTRO_PLINHO:
					case ASTRO_HOINZO:n += 2;break;
					case ASTRO_ASPP2P:
					case ASTRO_ASPP2H:n += 3;break;
					case ASTRO_PLFACT:
					case ASTRO_HOFACT:n += 2;break;
					case ASTRO_MUTRE:n += 3;break;

					case ASTRO_PATEL:
					case ASTRO_PATQU:
					case ASTRO_PLPAT:
					case ASTRO_HOPAT:break;
				}
				break; // ASTROLOGY
		}
		return n;
	}

	private int countReferences(int c1,int c2) {
		int n = 0;
		switch(system) {
			case ASTROLOGY:
				if(c1!=ASTRO_CATEGORIES && c1!=ASTRO_FACTORS && c1!=ASTRO_DIRECTIONS && c1!=ASTRO_CALENDARS) ++n;
				if(c1==ASTRO_CATEGORIES) {
					switch(c2) {
						case ASTRO_ELEMENTS:   n += 1+ASTRO_WATER-ASTRO_FIRE;break;
						case ASTRO_QUALITIES:  n += 1+ASTRO_MUTABLE-ASTRO_CARDINAL;break;
						case ASTRO_ENERGIES:   n += 1+ASTRO_FEMALE-ASTRO_MALE;break;
						case ASTRO_ZODIAC:     n += 1+ASTRO_PISCES-ASTRO_ARIES;break;
						case ASTRO_PLANETS:    n += 1+ASTRO_PLUTO-ASTRO_SUN;break;
						case ASTRO_HOUSES:     n += 12;break;
						case ASTRO_ASPECTS:    n += 1+OPPOSITION-CONJUNCTION;break;
						case ASTRO_PATTERNS:   n += 1+HEXAGRAM-STELLIUM;break;
						case ASTRO_SHAPINGS:   n += 1+SPLASH-BOWL;break;
						case ASTRO_HSYSTEMS:   n += 1+HSYS_KOCH-HSYS_PLACIDUS;break;
					}
				}
				break; // ASTROLOGY
		}
		return n;
	}*/
}

