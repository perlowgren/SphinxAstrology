package net.spirangle.sphinx.astro;

import static net.spirangle.sphinx.AstrologyProperties.*;
import static net.spirangle.sphinx.SphinxProperties.APP;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.RectF;
import android.os.Build;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;

import net.spirangle.sphinx.Horoscope;
import net.spirangle.sphinx.Symbol;


public class WheelGraphView extends HoroscopeView {
    private static final String TAG = "WheelGraphView";

    public static final int RING = 0;
    public static final int RULER = 1;
    public static final int ELEMENTS = 2;
    public static final int ZODIAC = 3;
    public static final int PLANETS = 4;
    public static final int HOUSES = 5;
    public static final int ASC_MC_ARROW = 6;
    public static final int ASPECTS = 7;

    public static final int Z = 12;
    public static final int P = 17;
    public static final int P2 = 34;
    public static final int H = 12;
    public static final int H2 = 24;

    public static final double J2000 = 2451545.0;
    public static final double B1950 = 2433282.423;
    public static final double J1900 = 2415020.0;

    public static final double DTR = 0.017453292519943295769236907684886;
    public static final double RTD = 57.295779513082320876798154814105;
    public static final double RTH = 3.8197186342054880584532103209403;
    public static final double HTR = 0.26179938779914943653855361527329;
    public static final double RTS = 2.0626480624709635516e5;
    public static final double STR = 4.8481368110953599359e-6;

    private static final float[] zodiacFontAdjustment = {0.0f,0.02f,-0.1f,-0.09f,0.0f,-0.04f,-0.1f,-0.04f,-0.1f,-0.02f,-0.1f,-0.1f};

    private static final int[] contentHouseColors = {0xffffcc99,0xffcc9966,0xff663300,0xff663300,0xff663300,0xff663300};
    private static final int[] contentPlanetColors = {0xff999999,0xff663300};
    private static final int[] contentFilledRingColors = {0xff000000,0xffffffff};
    private static final int[] contentEmptyRingColors = {0xff000000,0x00000000};
    private static final int[] contentZodiacColors = {0xff000000};
    private static final int[] contentZodiacRulerColors = {0xff000000};
    private static final int[] contentRulerColors = {0xff9999ff};
    private static final int[] contentRuler10Colors = {0xff000066};
    private static final int[] contentAspectColors = {0xff000000};

    private static final String[] moonPhasesUnicode = {
        "\uD83C\uDF15","\uD83C\uDF16","\uD83C\uDF17","\uD83C\uDF18","\uD83C\uDF11","\uD83C\uDF12","\uD83C\uDF13","\uD83C\uDF14",
    };

    public class WheelRectF extends MapRectF {
        public float angle = 0.0f;
    }

    private static abstract class Content {
        public int type;
        public float angle;
        public int[] color;

        public Content(int t,float a,int[] c) {
            type = t;
            angle = a;
            color = c;
        }
    }

    private static class RingContent extends Content {
        public float strokeWidth;

        public RingContent(int[] c,float w) {
            super(RING,0.0f,c);
            strokeWidth = w;
        }
    }

    private static class RulerContent extends Content {
        public float strokeWidth;
        public float degrees;
        public float start;
        public float end;

        public RulerContent(float a,int[] c,float w,float d,float s,float e) {
            super(RULER,a,c);
            strokeWidth = w;
            degrees = d;
            start = s;
            end = e;
        }
    }

    private static class ZodiacContent extends Content {
        public float textSize;

        public ZodiacContent(float a,int[] c,float s) {
            super(ZODIAC,a,c);
            textSize = s;
        }
    }

    private static class HousesContent extends Content {
        public float strokeWidth;
        public float textSize;
        public float arrowSize;
        public float arrowPosition;

        public HousesContent(float a,int[] c,float w,float ts,float as,float ap) {
            super(HOUSES,a,c);
            strokeWidth = w;
            textSize = ts;
            arrowSize = as;
            arrowPosition = ap;
        }
    }

    private static class PlanetsContent extends Content {
        public float planetSize;
        public float planetPosition;
        public float lineMargin;

        public PlanetsContent(float a,int[] c,float ps,float pp,float lm) {
            super(PLANETS,a,c);
            planetSize = ps;
            planetPosition = pp;
            lineMargin = lm;
        }
    }

    private static class AspectsContent extends Content {
        public float strokeWidth;
        public float margin;

        public AspectsContent(float a,int[] c,float w,float m) {
            super(ASPECTS,a,c);
            strokeWidth = w;
            margin = m;
        }
    }

    private static class Level {
        public float size;
        public Content[] content;

        public Level(float s,Content[] c) {
            size = s;
            content = c;
        }
    }

    private static class GraphStyle extends RectF {
        public float scale;
        public float margin;
        public float padding;
        public int[] elementColors;
        public int[] zodiacColors;
        public int[] planetColors;
        public int[] aspectColors;
        public Level[] levels;

        public GraphStyle(int l,int t,int r,int b,float m,float p,Level[] lvl) {
            super(l,t,r,b);
            scale = Math.min(r-l,b-t);
            margin = m;
            padding = p;
            elementColors = WheelGraphView.elementColors;
            zodiacColors = WheelGraphView.zodiacColors;
            planetColors = WheelGraphView.planetColors;
            aspectColors = WheelGraphView.aspectColors;
            levels = lvl;
        }

        public void setViewport(RectF viewport) {
            float x, y, sz, m = margin;
            scale = Math.min(viewport.right-viewport.left,viewport.bottom-viewport.top);
            m *= scale;
            sz = scale-m-m;
            x = viewport.right-sz-m;
            y = viewport.top+m;
            set(x,y,x+sz,y+sz);
        }
    }

    ;

    private static float planetSizes[] = {
        0.03f,
        0.08f,
        0.01f,
        0.02f,
        0.03f,
        0.02f,
        0.04f,
        0.05f,
        0.05f,
        0.04f,
        0.02f
    };

    private static float planetDistances[] = {
        0.35f,
        0.0f,
        0.07f,
        0.15f,
        0.25f,
        0.45f,
        0.55f,
        0.65f,
        0.75f,
        0.85f,
        0.95f
    };

    private static boolean aspectShow[] = {
        true,
        false,
        false,
        false,
        false,
        false,
        true,
        false,
        true,
        true,
        false,
        false,
        true,
        false,
        true,
        true,
        true,
        true,
        true,
        true,
        true,
        true,
        true
    };

    private double ascendant;
    private WheelRectF[] planetLocations1 = null;
    private WheelRectF[] planetLocations2 = null;
    private GraphStyle graphStyle;

    private Rect clipBounds = null;
    private float scaleSX = 1.0f;
    private float scaleSY = 1.0f;
    private float scalePX = 0.0f;
    private float scalePY = 0.0f;

    public WheelGraphView(Context context) { super(context); }

    public WheelGraphView(Context context,AttributeSet attrs) { super(context,attrs); }

    public WheelGraphView(Context context,AttributeSet attrs,int defStyle) { super(context,attrs,defStyle); }

    public WheelGraphView(Context context,AttributeSet attrs,int defStyle,int defStyleRes) { super(context,attrs,defStyle,defStyleRes); }

    @Override
    protected void onCreate(Context context,AttributeSet attrs,int defStyle,int defStyleRes) {
        super.onCreate(context,attrs,defStyle,defStyleRes);
        if(Build.VERSION.SDK_INT>=Build.VERSION_CODES.HONEYCOMB)
            setLayerType(LAYER_TYPE_SOFTWARE,paint);
    }

    @Override
    public void onMeasure(int wms,int hms) {
        super.onMeasure(wms,hms);

        int wm = MeasureSpec.getMode(wms);
        int ws = MeasureSpec.getSize(wms);
        int hm = MeasureSpec.getMode(hms);
        int hs = MeasureSpec.getSize(hms);
        int w, h, sz = 0;

        //Measure Width
        if(wm==MeasureSpec.EXACTLY) w = ws; //Must be this size
        else if(wm==MeasureSpec.AT_MOST) w = ws; //Can't be bigger than...
        else w = 0; //Be whatever you want
        //Measure Height
        if(hm==MeasureSpec.EXACTLY) h = hs; //Must be this size
        else if(hm==MeasureSpec.AT_MOST) h = hs; //Can't be bigger than...
        else h = 0; //Be whatever you want

//Log.d(APP,TAG+".onMeasure(w: "+w+", h: "+h+")");
        if(w>0 && (w<h || h==0)) sz = w;
        else if(h>0 && (h<w || w==0)) sz = h;

//Log.d(APP,TAG+".onMeasure("+sz+")");
        setMeasuredDimension(sz,sz);
    }


    @Override
    public boolean onScroll(MotionEvent e1,MotionEvent e2,float distX,float distY) {
        float x = e2.getX();
        float y = e2.getY();
        if(graphStyle!=null && graphStyle.contains(x,y)) {
            GraphStyle gs = graphStyle;
            float xc = gs.left+(gs.right-gs.left)*0.5f;
            float yc = gs.top+(gs.bottom-gs.top)*0.5f;
            float d = (float)Math.hypot(xc-x,yc-y);
            float sx = scaleSX, sy = scaleSY, px = scalePX, py = scalePY;
            float sz = Math.min(xc,yc);
            float c = sz*0.25f;
            if(d<c) {
                sx = sy = 1.0f;
                px = py = 0.0f;
            } else {
                sx = 1.0f+((d-c)/(sz-c))*2.0f;
                if(sx>3.0f) sx = 3.0f;
                sy = sx;
                px = gs.right-(x<xc? xc-(xc-x)*1.5f : xc+(x-xc)*1.5f);
                if(px<gs.left) px = gs.left;
                else if(px>gs.right) px = gs.right;
                py = gs.bottom-(y<yc? yc-(yc-y)*1.5f : yc+(y-yc)*1.5f);
                if(py<gs.top) py = gs.top;
                else if(py>gs.bottom) py = gs.bottom;
            }
            if(sx!=scaleSX || sy!=scaleSY || px!=scalePX || py!=scalePY) {
                scaleSX = sx;
                scaleSY = sy;
                scalePX = px;
                scalePY = py;
                active = null;
                invalidate();
                return true;
            }
        }
        return false;
    }

    @Override
    public boolean onSingleTapUp(MotionEvent e) {
        if(super.onSingleTapUp(e)) return true;
        if(scaleSX!=1.0f || scaleSY!=1.0f) {
            scaleSX = scaleSY = 1.0f;
            scalePX = scalePY = 0.0f;
            invalidate();
            return true;
        }
        return false;
    }

    @Override
    public void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        if(horoscope==null || graphStyle==null) return;
        long t1 = System.currentTimeMillis();
        drawWheelGraph(canvas,graphStyle);
        long t2 = System.currentTimeMillis();
//Log.d(APP,TAG+".onDraw("+(t2-t1)+")");
    }

    @Override
    public MapRectF getMapRect(float x,float y) {
        if(scaleSX!=1.0f || scaleSY!=1.0f) {
            if(clipBounds==null) return null;
            x = x/scaleSX+clipBounds.left;
            y = y/scaleSY+clipBounds.top;
        }
        return super.getMapRect(x,y);
    }

    @Override
    public void setHoroscope(Horoscope h) {
//Log.d(APP,TAG+".setHoroscope("+h+")");
        createGraphStyle(h,null,h.getStyle());
        super.setHoroscope(h);
    }

    private void createGraphStyle(Horoscope h1,Horoscope h2,int st) {
        ascendant = 0.0;
        map = null;
        planetLocations1 = null;
        planetLocations2 = null;
        graphStyle = null;
        if(h1==null) return;
        int p = h1.planets();
        float a = (float)h1.houseAbsoluteCusp(0);
        ascendant = h1.houseAbsoluteCusp(0);
        if((st&_HOROSCOPE_NATAL_)==_HOROSCOPE_NATAL_) {
            map = new MapRectF[p+12+12];
            planetLocations1 = new WheelRectF[p];
            graphStyle = new GraphStyle(0,0,0,0,0.0f,0.0074f,new Level[] {
                new Level(0.0555f,new Content[] {
                    new RingContent(contentEmptyRingColors,2.0f),
                    new ZodiacContent(-a,contentZodiacColors,0.0444f),
                    new RulerContent(-a,contentZodiacRulerColors,2.0f,30.0f,0.0f,0.0555f),
                }),
                new Level(0.0185f,new Content[] {
                    new RingContent(contentFilledRingColors,2.0f),
                    new RulerContent(-a,contentRulerColors,1.0f,2.0f,-0.00185f,-0.0074f),
                    new RulerContent(-a,contentRuler10Colors,1.0f,10.0f,-0.00185f,-0.0185f),
                }),
                new Level(0.10185f,new Content[] {
                    new RingContent(contentFilledRingColors,1.0f),
                    new HousesContent(-a,contentHouseColors,2.0f,0.0333f,0.12037f,0.0444f),
                    new PlanetsContent(-a,contentPlanetColors,0.05185f,0.037f,0.0037f),
                }),
                new Level(0.0f,new Content[] {
                    new RingContent(contentFilledRingColors,1.5f),
                    new AspectsContent(-a,contentAspectColors,1.5f,0.00926f),
                }),
            });
        }
    }

    protected void drawWheelGraph(Canvas canvas,GraphStyle gs) {
        if(horoscope==null || gs==null) return;
        gs.setViewport(viewport);

        Horoscope h = horoscope;
//		boolean portrait = main.getResources().getConfiguration().orientation == ActivityInfo.SCREEN_ORIENTATION_PORTRAIT;
        int l, c, i, j, k, oh, oz, x, y, p = h.planets();
        long id;
        float sz, r, r2, r3, r4, a, a2, f1, f2, f3;
        float size = Math.min(gs.right-gs.left,gs.bottom-gs.top);
        float radius = (size*0.5f)-gs.padding*gs.scale*2.0f;
        float xc = gs.left+(gs.right-gs.left)*0.5f;
        float yc = gs.top+(gs.bottom-gs.top)*0.5f;
        float x1, y1, x2, y2;
        RectF rect = new RectF(), rect2;
        MapRectF mr;
        double d, d2;
        String str;

        Log.d(APP,TAG+".drawWheelGraph(size: "+size+", radius: "+radius+")");

        Level lvl;
        Content con;
        RingContent ring;
        RulerContent ruler;
        ZodiacContent zcon;
        HousesContent hcon;
        PlanetsContent pcon;
        AspectsContent acon;

        clearMap(0,p,planetLocations1,h);

        oh = p;
        oz = p+12;
        clearMap(oh,12+12);

        try {
		/*if(gs.background!=0x00000000) {
			paint.setStyle(Paint.Style.FILL);
			paint.setAntiAlias(false);
			paint.setColor(gs.background);
			canvas.drawRect(gs,paint);
		}*/
            canvas.save();
            canvas.clipRect(gs);
            if(scaleSX!=1.0 || scaleSY!=1.0)
                canvas.scale(scaleSX,scaleSY,scalePX,scalePY);
            else
                drawInfoLayer(canvas,gs);
            clipBounds = canvas.getClipBounds();

            // Paint background
            for(l = 0,r = 0.0f; l<gs.levels.length; ++l,r += sz) {
                lvl = gs.levels[l];
                sz = lvl.size*gs.scale;
                r2 = radius-r;
                for(c = 0; c<lvl.content.length; ++c) {
                    con = lvl.content[c];
                    switch(con.type) {
                        case RING:
                            ring = (RingContent)con;
                            if(ring.color[1]!=0x00000000) {
                                paint.setStyle(Paint.Style.FILL);
                                paint.setAntiAlias(false);
                                paint.setColor(ring.color[1]);
                                canvas.drawCircle(xc,yc,r2-1.0f,paint);
                            }
                            break;

                        case ELEMENTS:
                        case ZODIAC:
                            zcon = (ZodiacContent)con;
                            paint.setStyle(Paint.Style.FILL);
                            paint.setAntiAlias(false);
                            rect.set(xc-r2,yc-r2,xc+r2,yc+r2);
                            for(i = 0,a = zcon.angle; i<12; ++i,a += 30.0f) {
                                paint.setColor(gs.elementColors[i&3]);
                                canvas.drawArc(rect,150.0f-a,30.0f,true,paint);
                            }
                            break;

                        case HOUSES:
                            hcon = (HousesContent)con;
                            paint.setStyle(Paint.Style.STROKE);
                            paint.setAntiAlias(true);
                            paint.setStrokeWidth(hcon.strokeWidth);
                            paint.setColor(hcon.color[0]);
                            for(i = 0,r3 = r2-sz; i<12; ++i) {
                                d = DTR*((double)hcon.angle+h.houseAbsoluteCusp(i));
                                x1 = xc-(float)Math.cos(-d)*r2;
                                y1 = yc-(float)Math.sin(-d)*r2;
                                x2 = xc-(float)Math.cos(-d)*r3;
                                y2 = yc-(float)Math.sin(-d)*r3;
                                canvas.drawLine(x1,y1,x2,y2,paint);
                            }
                            break;

                        case RULER:
                            ruler = (RulerContent)con;
                            paint.setStyle(Paint.Style.STROKE);
                            paint.setAntiAlias(true);
                            paint.setColor(ruler.color[0]);
                            paint.setStrokeWidth(ruler.strokeWidth);
                            for(a = 0.0f; a<360.0f; a += ruler.degrees) {
                                f1 = ruler.start*gs.scale;
                                f2 = ruler.end*gs.scale;
                                r3 = r2+(ruler.end<0.0f? -sz : 0.0f);
                                d = DTR*(double)(ruler.angle+a);
                                x1 = xc-(float)Math.cos(-d)*(r3-f1);
                                y1 = yc-(float)Math.sin(-d)*(r3-f1);
                                x2 = xc-(float)Math.cos(-d)*(r3-f2);
                                y2 = yc-(float)Math.sin(-d)*(r3-f2);
                                canvas.drawLine(x1,y1,x2,y2,paint);
                            }
                            break;
                    }
                }
            }

            // Paint foreground
            for(l = 0,r = 0.0f; l<gs.levels.length; ++l,r += sz) {
                lvl = gs.levels[l];
                sz = lvl.size*gs.scale;
                r2 = radius-r;
                for(c = 0; c<lvl.content.length; ++c) {
                    con = lvl.content[c];
                    switch(con.type) {
                        case RING:
                            ring = (RingContent)con;
                            if(ring.color[0]!=-1 && ring.color[0]!=ring.color[1]) {
                                paint.setStyle(Paint.Style.STROKE);
                                paint.setAntiAlias(true);
                                paint.setStrokeWidth(ring.strokeWidth);
                                paint.setColor(ring.color[0]);
                                canvas.drawCircle(xc,yc,r2,paint);
                            }
                            break;

                        case ZODIAC:
                            zcon = (ZodiacContent)con;
                            f1 = zcon.textSize*gs.scale;
                            r3 = r2-sz*0.5f;
                            a = zcon.angle+105.0f;
                            paint.setStyle(Paint.Style.FILL);
                            paint.setAntiAlias(true);
                            paint.setTypeface(AstroActivity.symbolFont);
                            paint.setTextSize(f1);
                            for(i = 0; i<12; ++i,a += 30.0f) {
                                str = Symbol.getUnicode(ASTRO_ARIES+i);
                                if(a>360.0f) a -= 360.0f;
                                if(a<0.0f) a += 360.0f;
                                x1 = paint.measureText(str)*0.5f;
                                if(a>=110.0f && a<250.0f) {
                                    y1 = -r3-f1*0.275f+f1*zodiacFontAdjustment[i];
                                    a2 = a-180.0f;
                                } else {
                                    y1 = r3-f1*0.275f+f1*zodiacFontAdjustment[i];
                                    a2 = a;
                                }
//Log.d(APP,TAG+".drawWheelGraph(a2: "+a2+")");

                                id = Symbol.astrologyZodiac(ASTRO_ARIES+i);
                                d = DTR*((double)a-90.0);
                                r4 = f1*0.5f;
                                x2 = xc-(float)Math.cos(-d)*r3;
                                y2 = yc-(float)Math.sin(-d)*r3;
                                mr = map[oz+i];
                                mr.set(i,id,x2-f1,y2-f1,x2+f1,y2+f1);
							/*if(mr.isActive()) {
								paint.setColor(0x11000000);
								canvas.drawRoundRect(mr,r4,r4,paint);
							}*/

                                paint.setColor(gs.zodiacColors[i]);
                                canvas.save();
                                canvas.rotate(-a2,xc,yc);
                                canvas.drawText(str,xc-x1,yc-y1,paint);
                                canvas.restore();
                            }
                            break;


                        case HOUSES:
                            hcon = (HousesContent)con;
                            f1 = hcon.textSize*gs.scale;
                            f2 = hcon.arrowSize*gs.scale;
                            f3 = hcon.arrowPosition*gs.scale;
                            paint.setStyle(Paint.Style.FILL);
                            paint.setAntiAlias(true);
                            paint.setTypeface(AstroActivity.symbolFont);
                            paint.setTextSize(f1);
                            for(i = 0; i<12; ++i) {
                                str = Integer.toString(i+1);
                                r3 = r2-sz+f1*(i<9? 0.6f : 0.8f);
                                d = DTR*((double)hcon.angle+h.houseAbsoluteCusp(i)+(i<9? 4.0 : 5.0));
                                x2 = paint.measureText(str);
                                x1 = xc-(float)Math.cos(-d)*r3-x2*0.5f;
                                y1 = yc-(float)Math.sin(-d)*r3+f1*0.3f;

                                id = h.houseSymbolId(i);
                                x2 = x1+x2*0.5f-f1;
                                y2 = y1+(paint.ascent()+paint.descent())*0.5f-f1;
                                mr = map[oh+i];
                                mr.set(i,id,x2,y2,x2+f1*2.0f,y2+f1*2.0f);
							/*if(mr.isActive()) {
								r3 = f1*0.5f;
								paint.setColor(0x11000000);
								canvas.drawRoundRect(mr,r3,r3,paint);
							}*/

                                paint.setColor(hcon.color[1]);
                                canvas.drawText(str,x1,y1,paint);
                            }
                            if(f2<=0.0f) break;
                            paint.setAntiAlias(true);
                            paint.setStrokeWidth(3.0f);
                            paint.setTextSize(f2);
                            for(i = 0,r3 = r2+f3,str = "\u227A"; i<2; ++i) {
                                // Draw lines:
                                d = DTR*((double)hcon.angle+h.houseAbsoluteCusp(i==0? 0 : 9));
                                x1 = xc-(float)Math.cos(-d)*(r2-sz);
                                y1 = yc-(float)Math.sin(-d)*(r2-sz);
                                x2 = xc-(float)Math.cos(-d)*r3;
                                y2 = yc-(float)Math.sin(-d)*r3;
                                paint.setStyle(Paint.Style.STROKE);
                                paint.setColor(hcon.color[2+i]);
                                canvas.drawLine(x1,y1,x2,y2,paint);
                                // Draw arrows:
                                a = hcon.angle+(float)h.houseAbsoluteCusp(i==0? 0 : 9)+90.0f;
                                x1 = paint.measureText(str)*0.5f;
                                paint.setStyle(Paint.Style.FILL);
                                paint.setColor(hcon.color[4+i]);
                                canvas.save();
                                canvas.rotate(-a,xc,yc);
                                canvas.drawText(str,xc-x1,yc-r3,paint);
                                canvas.restore();
                            }
/*						paint.setStyle(Paint.Style.STROKE);
						paint.setColor(hcon.color[2+i]);
						for(i=0,r3=r2+hcon.p5; i<2; ++i) {
							// Draw lines:
							d = DTR*((double)hcon.angle+h.houseAbsoluteCusp(i==0? 3 : 6));
							x1 = xc-(float)Math.cos(-d)*r2;
							y1 = yc-(float)Math.sin(-d)*r2;
							x2 = xc-(float)Math.cos(-d)*r3;
							y2 = yc-(float)Math.sin(-d)*r3;
							canvas.drawLine(x1,y1,x2,y2,paint);
						}*/
                            break;

                        case PLANETS:
                            pcon = (PlanetsContent)con;
                            f1 = pcon.planetSize*gs.scale;
                            f2 = pcon.planetPosition*gs.scale;
                            f3 = pcon.lineMargin*gs.scale;
                            r3 = r2-f2+(f2<0.0f? f1 : -f1)*0.5f;
                            for(i = 0; i<p; ++i) {
                                k = h.planetId(i);
                                if(k==ASTRO_ASCENDANT || k==ASTRO_MC) continue;
                                id = h.planetSymbolId(i);
                                if(id==-1l) continue;
                                positionPlanet(h,id,xc,yc,planetLocations1,i,0.0,f1+2.0f,r3,i,10);
                            }

                            paint.setStyle(Paint.Style.FILL);
                            paint.setAntiAlias(true);
                            paint.setStrokeWidth(0.5f);
                            paint.setTypeface(AstroActivity.symbolFont);
//						paint.setColor(0xff000000);//pcon.color[0]);
                            paint.setTextSize(f1);

                            for(i = 0; i<p; ++i) {
                                k = h.planetId(i);
                                if(k==ASTRO_ASCENDANT || k==ASTRO_MC) continue;
                                str = Symbol.getUnicode(k);

                                r3 = r2-f2+(f2<0.0f? -5.0f : 5.0f);
                                d = DTR*((double)pcon.angle+h.planetAbsoluteLongitude(i));
                                d2 = planetLocations1[i].angle;
                                x1 = xc-(float)Math.cos(-d)*(r2-f3);
                                y1 = yc-(float)Math.sin(-d)*(r2-f3);
                                x2 = xc-(float)Math.cos(d2)*r3;
                                y2 = yc-(float)Math.sin(d2)*r3;
                                paint.setColor(pcon.color[0]);
                                canvas.drawLine(x1,y1,x2,y2,paint);

//debug_output("planetLocations1[x=%d,y=%d,a=%f]",planetLocations1[i].x,planetLocations1[i].y,planetAngles1[i]);
                                rect2 = planetLocations1[i];
                                x1 = rect2.left+((rect2.right-rect2.left)-paint.measureText(str))*0.5f+1.0f;
                                y1 = rect2.bottom-1.0f;//+((rect2.bottom-rect2.top)-f1)*0.5f;
//							paint.setColor(0xff999999);
//							canvas.drawRect(rect2.left,rect2.top,rect2.right,rect2.bottom,paint);

                                mr = map[i];
							/*if(mr.isActive()) {
								r3 = (mr.bottom-mr.top)*0.25f;
								paint.setColor(0x11000000);
								canvas.drawRoundRect(mr,r3,r3,paint);
							}*/

                                paint.setColor(pcon.color[1]);
                                canvas.drawText(str,x1,y1,paint);
                            }
                            break;

                        case ASPECTS:
                            acon = (AspectsContent)con;
                            f1 = acon.margin*gs.scale;
                            paint.setStyle(Paint.Style.STROKE);
                            paint.setAntiAlias(true);
                            paint.setStrokeWidth(acon.strokeWidth);
                            for(x = 0,r3 = r2-f1; x<p; ++x) {
                                i = h.planetId(x);
                                if(i==ASTRO_ASCENDANT || i==ASTRO_MC) continue;
                                for(y = x+1; y<p; ++y) {
                                    j = h.planetId(y);
                                    if(j==ASTRO_ASCENDANT || j==ASTRO_MC) continue;
                                    k = h.aspect(x,y);
//Log.d(APP,"k: "+k+", l: "+l+", a: "+a);
                                    if(k>=CONJUNCTION && k<=OPPOSITION && aspectShow[k&0xffff]
//									&& (!aspectShow[13] || horoscope.findPattern(x,y))
                                    ) {
                                        d = DTR*((double)acon.angle+h.planetAbsoluteLongitude(x));
                                        d2 = DTR*((double)acon.angle+h.planetAbsoluteLongitude(y));
                                        x1 = xc-(float)Math.cos(-d)*r3;
                                        y1 = yc-(float)Math.sin(-d)*r3;
                                        x2 = xc-(float)Math.cos(-d2)*r3;
                                        y2 = yc-(float)Math.sin(-d2)*r3;
                                        paint.setColor(gs.aspectColors[k&0xffff]);
                                        canvas.drawLine(x1,y1,x2,y2,paint);
                                    }
                                }
                            }
                            break;
                    }
                }
            }
            canvas.restore();
		/*if(gs.foreground!=0x00000000) {
			paint.setStyle(Paint.Style.STROKE);
			paint.setAntiAlias(false);
			paint.setColor(gs.foreground);
			canvas.drawRect(gs,paint);
		}*/

        } catch(Exception e) {
            Log.e(APP,TAG+".drawWheelGraph",e);
        }

    }

    protected void drawInfoLayer(Canvas canvas,GraphStyle gs) {
        Horoscope h = horoscope;
        float x1, y1;
        String str;
        int i/*,d*/, n, m;
        double x, y, z;
//Log.d(APP,TAG+".drawInfoLayer(paint: "+paint+", symbolFont: "+AstroActivity.symbolFont+")");
        paint.setTypeface(AstroActivity.symbolFont);
        paint.setStyle(Paint.Style.FILL);
        paint.setAntiAlias(true);
        paint.setColor(0xff000000);

        paint.setTextSize(24.0f);
        x1 = viewport.left+5.0f;
        y1 = viewport.top+5.0f-paint.ascent();
        str = h.getDateString();
        canvas.drawText(str,x1,y1,paint);
        paint.setTextSize(20.0f);
        str = h.getTimeAndTimeZoneString();
        canvas.drawText(str,x1,y1+24.0f,paint);

        x = h.moonPhase();
        z = h.moonPhaseDegreeDays();
        i = (int)(x/45.0);
        x = (x-i*45.0);
        if(x>22.5) {
            y = (x-45.0)*z;
            i = (i+1)&7;
        } else {
            y = x*z;
        }
		/*if(y<1.0) d = 0;
		else {
			d = (int)y;
			y -= (double)d;
		}*/
        n = (int)(y*24.0);
        m = (int)Math.abs(y*1440.0)%60;
        paint.setTextSize(24.0f);
        canvas.drawText(moonPhasesUnicode[i],x1,y1+54.0f,paint);
        paint.setTextSize(16.0f);
		/*if(d>0) str = String.format("%1$+dd %2$d:%3$02d",d,n,m);
		else */
        str = String.format("%1$+d:%2$02d",n,m);
        canvas.drawText(str,x1+26.0f,y1+49.0f,paint);

        paint.setTextSize(24.0f);
        x1 = viewport.right-5.0f;
        y1 = viewport.top+5.0f-paint.ascent();
        str = h.getLongitudeString();
        canvas.drawText(str,x1-paint.measureText(str),y1,paint);
        str = h.getLatitudeString();
        canvas.drawText(str,x1-paint.measureText(str),y1+26.0f,paint);
    }

    private void positionPlanet(Horoscope h,long id,float xc,float yc,WheelRectF[] pl,int p,double a,float sz,float r,int endp,int iter) {
//Log.d(APP,TAG+".positionPlanet(p: "+p+", pa: "+pa[p]+", endp: "+endp+", iter: "+iter+")");
        WheelRectF r1 = pl[p];
        float sz2 = sz/2.0f;
        float sz3 = sz/10.0f;
        double a1 = (double)r1.angle+a;
        r1.left = xc-(float)Math.cos(a1)*r-sz2+sz3;
        r1.top = yc-(float)Math.sin(a1)*r-sz2+sz3;
        r1.right = r1.left+sz-sz3*2.0f;
        r1.bottom = r1.top+sz-sz3*2.0f;
        r1.angle += (float)a;
        if(id!=-1l) r1.id = id;
        if(iter>0) {
            int i, n;
            double h1, h2, a2 = DTR*0.5;
            boolean cw;
            h1 = DTR*(ascendant-h.planetAbsoluteLongitude(p));
            for(n = 0; (n = intersectingPlanet(h,pl,p,r1,n,endp))!=-1; ++n) {
                h2 = DTR*(ascendant-h.planetAbsoluteLongitude(n));
                cw = Math.abs(h1-h2)>Math.PI? h1<h2 : h1>h2;
                positionPlanet(h,-1l,xc,yc,pl,p,cw? a2 : -a2,sz,r,endp,0);
                positionPlanet(h,-1l,xc,yc,pl,n,cw? -a2 : a2,sz,r,endp,iter-1);
            }
        }
    }

    private int intersectingPlanet(Horoscope h,WheelRectF[] pl,int p,WheelRectF r1,int startp,int endp) {
//Log.d(APP,TAG+".intersectingPlanet(p: "+p+", startp: "+startp+", endp: "+endp+")");
        int i, pid;
        WheelRectF r2;
        for(i = startp; i<=endp; ++i) {
            if(i==p) continue;
            pid = h.planetId(p);
            if(pid==ASTRO_ASCENDANT || pid==ASTRO_MC) continue;
            r2 = pl[i];
            if(r1.left<r2.right && r1.top<r2.bottom &&
               r1.right>r2.left && r1.bottom>r2.top) return i;
        }
        return -1;
    }

    private void clearMap(int o,int l,WheelRectF[] pl,Horoscope h) {
        int i;
        WheelRectF r;
        if(pl!=null)
            for(i = 0; i<l; ++i) {
                r = pl[i];
                if(r==null) pl[i] = r = new WheelRectF();
                else r.set(0,-1l,0.0f,0.0f,0.0f,0.0f);
                r.angle = (float)(DTR*(ascendant-h.planetAbsoluteLongitude(i)));
                map[o+i] = r;
            }
    }
}

