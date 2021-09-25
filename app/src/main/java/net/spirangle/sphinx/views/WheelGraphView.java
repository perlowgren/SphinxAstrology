package net.spirangle.sphinx.views;

import static net.spirangle.sphinx.config.AstrologyProperties.*;
import static net.spirangle.sphinx.config.SphinxProperties.APP;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;

import net.spirangle.sphinx.activities.AstroActivity;
import net.spirangle.sphinx.astro.Horoscope;
import net.spirangle.sphinx.astro.Symbol;

import java.util.Locale;


public class WheelGraphView extends HoroscopeView {
    private static final String TAG = WheelGraphView.class.getSimpleName();

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
        "\uD83C\uDF15",
        "\uD83C\uDF16",
        "\uD83C\uDF17",
        "\uD83C\uDF18",
        "\uD83C\uDF11",
        "\uD83C\uDF12",
        "\uD83C\uDF13",
        "\uD83C\uDF14",
    };

    public class WheelCell extends Cell {
        public float angle = 0.0f;
    }

    private abstract static class WheelGraph {
        public int type;
        public float angle;
        public int[] color;

        public WheelGraph(float a,int[] c) {
            angle = a;
            color = c;
        }

        public void drawBackground(Canvas canvas,GraphStyle gs,GraphLevel lvl,float r) {}

        public void drawForeground(Canvas canvas,GraphStyle gs,GraphLevel lvl,float r) {}
    }

    private class RingGraph extends WheelGraph {
        public float strokeWidth;

        public RingGraph(int[] c,float w) {
            super(0.0f,c);
            strokeWidth = w;
        }

        @Override
        public void drawBackground(Canvas canvas,GraphStyle gs,GraphLevel lvl,float r) {
            if(color[1]!=0x00000000) {
                paint.setStyle(Paint.Style.FILL);
                paint.setAntiAlias(false);
                paint.setColor(color[1]);
                canvas.drawCircle(gs.centerX,gs.centerY,r-1.0f,paint);
            }
        }

        @Override
        public void drawForeground(Canvas canvas,GraphStyle gs,GraphLevel lvl,float r) {
            if(color[0]!=-1 && color[0]!=color[1]) {
                paint.setStyle(Paint.Style.STROKE);
                paint.setAntiAlias(true);
                paint.setStrokeWidth(strokeWidth);
                paint.setColor(color[0]);
                canvas.drawCircle(gs.centerX,gs.centerY,r,paint);
            }
        }
    }

    private class RulerGraph extends WheelGraph {
        public float strokeWidth;
        public float degrees;
        public float start;
        public float end;

        public RulerGraph(float a,int[] c,float w,float d,float s,float e) {
            super(a,c);
            strokeWidth = w;
            degrees = d;
            start = s;
            end = e;
        }

        @Override
        public void drawBackground(Canvas canvas,GraphStyle gs,GraphLevel lvl,float r) {
            paint.setStyle(Paint.Style.STROKE);
            paint.setAntiAlias(true);
            paint.setColor(color[0]);
            paint.setStrokeWidth(strokeWidth);
            float sz = lvl.size*gs.scale;
            float a = 0.0f;
            for(; a<360.0f; a += degrees) {
                float f1 = start*gs.scale;
                float f2 = end*gs.scale;
                float r3 = r+(end<0.0f? -sz : 0.0f);
                double d = DTR*(double)(angle+a);
                float x1 = gs.centerX-(float)Math.cos(-d)*(r3-f1);
                float y1 = gs.centerY-(float)Math.sin(-d)*(r3-f1);
                float x2 = gs.centerX-(float)Math.cos(-d)*(r3-f2);
                float y2 = gs.centerY-(float)Math.sin(-d)*(r3-f2);
                canvas.drawLine(x1,y1,x2,y2,paint);
            }
        }
    }

    private class ZodiacGraph extends WheelGraph {
        public float textSize;

        public ZodiacGraph(float a,int[] c,float s) {
            super(a,c);
            textSize = s;
        }

        @Override
        public void drawBackground(Canvas canvas,GraphStyle gs,GraphLevel lvl,float r) {
            paint.setStyle(Paint.Style.FILL);
            paint.setAntiAlias(false);
            RectF rect = new RectF(gs.centerX-r,gs.centerY-r,gs.centerX+r,gs.centerY+r);
            float a = angle;
            for(int i = 0; i<12; ++i,a += 30.0f) {
                paint.setColor(gs.elementColors[i&3]);
                canvas.drawArc(rect,150.0f-a,30.0f,true,paint);
            }
        }

        @Override
        public void drawForeground(Canvas canvas,GraphStyle gs,GraphLevel lvl,float r) {
            Horoscope h = horoscope;
            int oz = h.planets()+12;
            float sz = lvl.size*gs.scale;
            float f1 = textSize*gs.scale;
            float r3 = r-sz*0.5f;
            float a = angle+105.0f;
            paint.setStyle(Paint.Style.FILL);
            paint.setAntiAlias(true);
            paint.setTypeface(AstroActivity.symbolFont);
            paint.setTextSize(f1);
            for(int i = 0; i<12; ++i,a += 30.0f) {
                String str = Symbol.getUnicode(ASTRO_ARIES+i);
                if(a>360.0f) a -= 360.0f;
                if(a<0.0f) a += 360.0f;
                float x1 = paint.measureText(str)*0.5f,y1,a2;
                if(a>=110.0f && a<250.0f) {
                    y1 = -r3-f1*0.275f+f1*zodiacFontAdjustment[i];
                    a2 = a-180.0f;
                } else {
                    y1 = r3-f1*0.275f+f1*zodiacFontAdjustment[i];
                    a2 = a;
                }
//Log.d(APP,TAG+".drawWheelGraph(a2: "+a2+")");

                long id = Symbol.astrologyZodiac(ASTRO_ARIES+i);
                double d = DTR*((double)a-90.0);
                float x2 = gs.centerX-(float)Math.cos(-d)*r3;
                float y2 = gs.centerY-(float)Math.sin(-d)*r3;
                Cell mr = getCell(oz+i);
                mr.set(i,id,x2-f1,y2-f1,x2+f1,y2+f1);

                paint.setColor(gs.zodiacColors[i]);
                canvas.save();
                canvas.rotate(-a2,gs.centerX,gs.centerY);
                canvas.drawText(str,gs.centerX-x1,gs.centerY-y1,paint);
                canvas.restore();
            }
        }
    }

    private class HousesGraph extends WheelGraph {
        public float strokeWidth;
        public float textSize;
        public float arrowSize;
        public float arrowPosition;

        public HousesGraph(float a,int[] c,float w,float ts,float as,float ap) {
            super(a,c);
            strokeWidth = w;
            textSize = ts;
            arrowSize = as;
            arrowPosition = ap;
        }

        @Override
        public void drawBackground(Canvas canvas,GraphStyle gs,GraphLevel lvl,float r) {
            Horoscope h = horoscope;
            paint.setStyle(Paint.Style.STROKE);
            paint.setAntiAlias(true);
            paint.setStrokeWidth(strokeWidth);
            paint.setColor(color[0]);
            float sz = lvl.size*gs.scale;
            float r3 = r-sz;
            for(int i = 0; i<12; ++i) {
                double d = DTR*((double)angle+h.houseAbsoluteCusp(i));
                float x1 = gs.centerX-(float)Math.cos(-d)*r;
                float y1 = gs.centerY-(float)Math.sin(-d)*r;
                float x2 = gs.centerX-(float)Math.cos(-d)*r3;
                float y2 = gs.centerY-(float)Math.sin(-d)*r3;
                canvas.drawLine(x1,y1,x2,y2,paint);
            }
        }

        @Override
        public void drawForeground(Canvas canvas,GraphStyle gs,GraphLevel lvl,float r) {
            Horoscope h = horoscope;
            float f1 = textSize*gs.scale;
            float f2 = arrowSize*gs.scale;
            float f3 = arrowPosition*gs.scale;
            paint.setStyle(Paint.Style.FILL);
            paint.setAntiAlias(true);
            paint.setTypeface(AstroActivity.symbolFont);
            paint.setTextSize(f1);
            float sz = lvl.size*gs.scale;
            float x1,y1,x2,y2;
            int oh = h.planets();
            String str;
            for(int i = 0; i<12; ++i) {
                str = Integer.toString(i+1);
                float r3 = r-sz+f1*(i<9? 0.6f : 0.8f);
                double d = DTR*((double)angle+h.houseAbsoluteCusp(i)+(i<9? 4.0 : 5.0));
                x2 = paint.measureText(str);
                x1 = gs.centerX-(float)Math.cos(-d)*r3-x2*0.5f;
                y1 = gs.centerY-(float)Math.sin(-d)*r3+f1*0.3f;

                long id = h.houseSymbolId(i);
                x2 = x1+x2*0.5f-f1;
                y2 = y1+(paint.ascent()+paint.descent())*0.5f-f1;
                Cell mr = getCell(oh+i);
                mr.set(i,id,x2,y2,x2+f1*2.0f,y2+f1*2.0f);

                paint.setColor(color[1]);
                canvas.drawText(str,x1,y1,paint);
            }
            if(f2<=0.0f) return;
            paint.setAntiAlias(true);
            paint.setStrokeWidth(3.0f);
            paint.setTextSize(f2);
            float r3 = r+f3;
            str = "\u227A";
            for(int i = 0; i<2; ++i) {
                // Draw lines:
                double d = DTR*((double)angle+h.houseAbsoluteCusp(i==0? 0 : 9));
                x1 = gs.centerX-(float)Math.cos(-d)*(r-sz);
                y1 = gs.centerY-(float)Math.sin(-d)*(r-sz);
                x2 = gs.centerX-(float)Math.cos(-d)*r3;
                y2 = gs.centerY-(float)Math.sin(-d)*r3;
                paint.setStyle(Paint.Style.STROKE);
                paint.setColor(color[2+i]);
                canvas.drawLine(x1,y1,x2,y2,paint);
                // Draw arrows:
                float a = angle+(float)h.houseAbsoluteCusp(i==0? 0 : 9)+90.0f;
                x1 = paint.measureText(str)*0.5f;
                paint.setStyle(Paint.Style.FILL);
                paint.setColor(color[4+i]);
                canvas.save();
                canvas.rotate(-a,gs.centerX,gs.centerY);
                canvas.drawText(str,gs.centerX-x1,gs.centerY-r3,paint);
                canvas.restore();
            }
        }
    }

    private class PlanetsGraph extends WheelGraph {
        public float planetSize;
        public float planetPosition;
        public float lineMargin;

        public PlanetsGraph(float a,int[] c,float ps,float pp,float lm) {
            super(a,c);
            planetSize = ps;
            planetPosition = pp;
            lineMargin = lm;
        }

        @Override
        public void drawForeground(Canvas canvas,GraphStyle gs,GraphLevel lvl,float r) {
            Horoscope h = horoscope;
            float f1 = planetSize*gs.scale;
            float f2 = planetPosition*gs.scale;
            float f3 = lineMargin*gs.scale;
            float r3 = r-f2+(f2<0.0f? f1 : -f1)*0.5f;
            int p = h.planets();
            for(int i = 0; i<p; ++i) {
                int k = h.planetId(i);
                if(k==ASTRO_ASCENDANT || k==ASTRO_MC) continue;
                long id = h.planetSymbolId(i);
                if(id==-1l) continue;
                positionPlanet(h,id,gs.centerX,gs.centerY,planetLocations1,i,0.0,f1+2.0f,r3,i,10);
            }

            paint.setStyle(Paint.Style.FILL);
            paint.setAntiAlias(true);
            paint.setStrokeWidth(0.5f);
            paint.setTypeface(AstroActivity.symbolFont);
//						paint.setColor(0xff000000);//pcon.color[0]);
            paint.setTextSize(f1);

            for(int i = 0; i<p; ++i) {
                int k = h.planetId(i);
                if(k==ASTRO_ASCENDANT || k==ASTRO_MC) continue;
                String str = Symbol.getUnicode(k);

                r3 = r-f2+(f2<0.0f? -5.0f : 5.0f);
                double d = DTR*((double)angle+h.planetAbsoluteLongitude(i));
                float d2 = planetLocations1[i].angle;
                float x1 = gs.centerX-(float)Math.cos(-d)*(r-f3);
                float y1 = gs.centerY-(float)Math.sin(-d)*(r-f3);
                float x2 = gs.centerX-(float)Math.cos(d2)*r3;
                float y2 = gs.centerY-(float)Math.sin(d2)*r3;
                paint.setColor(color[0]);
                canvas.drawLine(x1,y1,x2,y2,paint);

                WheelCell rect2 = planetLocations1[i];
                x1 = rect2.left+((rect2.right-rect2.left)-paint.measureText(str))*0.5f+1.0f;
                y1 = rect2.bottom-1.0f;//+((rect2.bottom-rect2.top)-f1)*0.5f;

                Cell mr = getCell(i);

                paint.setColor(color[1]);
                canvas.drawText(str,x1,y1,paint);
            }
        }
    }

    private class AspectsGraph extends WheelGraph {
        public float strokeWidth;
        public float margin;

        public AspectsGraph(float a,int[] c,float w,float m) {
            super(a,c);
            strokeWidth = w;
            margin = m;
        }

        @Override
        public void drawForeground(Canvas canvas,GraphStyle gs,GraphLevel lvl,float r) {
            Horoscope h = horoscope;
            float f1 = margin*gs.scale;
            paint.setStyle(Paint.Style.STROKE);
            paint.setAntiAlias(true);
            int p = h.planets();
            float r3 = r-f1;
            for(int x = 0; x<p; ++x) {
                int i = h.planetId(x);
                for(int y = x+1; y<p; ++y) {
                    int j = h.planetId(y);
                    int k = h.aspect(x,y);
                    if(k>=CONJUNCTION && k<=OPPOSITION && aspectShow[k&0xffff]) {
                        int inAspectPattern = h.isInAspectPattern(i,j);
                        if((i==ASTRO_ASCENDANT || i==ASTRO_MC ||
                            j==ASTRO_ASCENDANT || j==ASTRO_MC) && inAspectPattern==0) continue;
                        double d = DTR*((double)angle+h.planetAbsoluteLongitude(x));
                        double d2 = DTR*((double)angle+h.planetAbsoluteLongitude(y));
                        float x1 = gs.centerX-(float)Math.cos(-d)*r3;
                        float y1 = gs.centerY-(float)Math.sin(-d)*r3;
                        float x2 = gs.centerX-(float)Math.cos(-d2)*r3;
                        float y2 = gs.centerY-(float)Math.sin(-d2)*r3;
                        float strokeMod = 1.0f;
                        if(inAspectPattern>0) strokeMod += 0.7f+(float)inAspectPattern*0.3f;
                        paint.setStrokeWidth(strokeWidth*strokeMod);
                        paint.setColor(gs.aspectColors[k&0xffff]);
                        canvas.drawLine(x1,y1,x2,y2,paint);
                    }
                }
            }
        }
    }

    private static class GraphLevel {
        public float size;
        public WheelGraph[] content;

        public GraphLevel(float s,WheelGraph[] c) {
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
        public GraphLevel[] levels;

        public float size;
        public float radius;
        public float centerX;
        public float centerY;

        public GraphStyle(int l,int t,int r,int b,float m,float p,GraphLevel[] lvl) {
            super(l,t,r,b);
            scale = Math.min(r-l,b-t);
            margin = m;
            padding = p;
            elementColors = WheelGraphView.elementColors;
            zodiacColors = WheelGraphView.zodiacColors;
            planetColors = WheelGraphView.planetColors;
            aspectColors = WheelGraphView.aspectColors;
            levels = lvl;
            update();
        }

        public void setViewport(RectF viewport) {
            float x, y, sz, m = margin;
            scale = Math.min(viewport.right-viewport.left,viewport.bottom-viewport.top);
            m *= scale;
            sz = scale-m-m;
            x = viewport.right-sz-m;
            y = viewport.top+m;
            set(x,y,x+sz,y+sz);
            update();
        }

        private void update() {
            size = Math.min(right-left,bottom-top);
            radius = (size*0.5f)-padding*scale*2.0f;
            centerX = left+(right-left)*0.5f;
            centerY = top+(bottom-top)*0.5f;
        }
    }

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
    private WheelCell[] planetLocations1 = null;
    private WheelCell[] planetLocations2 = null;
    private GraphStyle graphStyle;

    private Rect clipBounds = null;
    private float scaleSX = 1.0f;
    private float scaleSY = 1.0f;
    private float scalePX = 0.0f;
    private float scalePY = 0.0f;

    public WheelGraphView(Context context) {
        super(context);
    }

    public WheelGraphView(Context context,AttributeSet attrs) {
        super(context,attrs);
    }

    public WheelGraphView(Context context,AttributeSet attrs,int defStyle) {
        super(context,attrs,defStyle);
    }

    public WheelGraphView(Context context,AttributeSet attrs,int defStyle,int defStyleRes) {
        super(context,attrs,defStyle,defStyleRes);
    }

    @Override
    protected void onCreate(Context context,AttributeSet attrs,int defStyle,int defStyleRes) {
        super.onCreate(context,attrs,defStyle,defStyleRes);
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

        if(w>0 && (w<h || h==0)) sz = w;
        else if(h>0 && (h<w || w==0)) sz = h;

        setMeasuredDimension(sz,sz);
    }


    @Override
    public boolean onScroll(MotionEvent e1,MotionEvent e2,float distX,float distY) {
        float x = e2.getX();
        float y = e2.getY();
        if(graphStyle!=null && graphStyle.contains(x,y)) {
            GraphStyle gs = graphStyle;
            float xc = gs.centerX;
            float yc = gs.centerY;
            float d = (float)Math.hypot(xc-x,yc-y);
            float sx, sy, px, py;
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
                deactivateCell();
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

        int p = horoscope.planets();
        clearMap(0,p,planetLocations1,horoscope);
        clearCellMap(p,12+12);

        long t1 = System.currentTimeMillis();
        drawWheelGraph(canvas,graphStyle);
        long t2 = System.currentTimeMillis();
        Log.d(APP,TAG+".onDraw: Time for drawing wheel graph: "+(t2-t1)+"ms");
    }

    @Override
    public Cell getCell(float x,float y) {
        if(scaleSX!=1.0f || scaleSY!=1.0f) {
            if(clipBounds==null) return null;
            x = x/scaleSX+clipBounds.left;
            y = y/scaleSY+clipBounds.top;
        }
        return super.getCell(x,y);
    }

    @Override
    public void setHoroscope(Horoscope h) {
//Log.d(APP,TAG+".setHoroscope("+h+")");
        createGraphStyle(h,null,h.getStyle());
        super.setHoroscope(h);
    }

    private void createGraphStyle(Horoscope h1,Horoscope h2,int st) {
        ascendant = 0.0;
        planetLocations1 = null;
        planetLocations2 = null;
        graphStyle = null;
        createCellMap(0);
        if(h1==null) return;
        int p = h1.planets();
        float a = (float)h1.houseAbsoluteCusp(0);
        ascendant = h1.houseAbsoluteCusp(0);
        if((st&_HOROSCOPE_NATAL_)==_HOROSCOPE_NATAL_) {
            createCellMap(p+12+12);
            planetLocations1 = new WheelCell[p];
            graphStyle = new GraphStyle(0,0,0,0,0.0f,0.0074f,new GraphLevel[] {
                new GraphLevel(0.0555f,new WheelGraph[] {
                    new RingGraph(contentEmptyRingColors,2.0f),
                    new ZodiacGraph(-a,contentZodiacColors,0.0444f),
                    new RulerGraph(-a,contentZodiacRulerColors,2.0f,30.0f,0.0f,0.0555f),
                }),
                new GraphLevel(0.0185f,new WheelGraph[] {
                    new RingGraph(contentFilledRingColors,2.0f),
                    new RulerGraph(-a,contentRulerColors,1.0f,2.0f,-0.00185f,-0.0074f),
                    new RulerGraph(-a,contentRuler10Colors,1.0f,10.0f,-0.00185f,-0.0185f),
                }),
                new GraphLevel(0.10185f,new WheelGraph[] {
                    new RingGraph(contentFilledRingColors,1.0f),
                    new HousesGraph(-a,contentHouseColors,2.0f,0.0333f,0.12037f,0.0444f),
                    new PlanetsGraph(-a,contentPlanetColors,0.05185f,0.037f,0.0037f),
                }),
                new GraphLevel(0.0f,new WheelGraph[] {
                    new RingGraph(contentFilledRingColors,1.5f),
                    new AspectsGraph(-a,contentAspectColors,1.5f,0.00926f),
                }),
            });
        }
    }

    protected void drawWheelGraph(Canvas canvas,GraphStyle gs) {
        if(horoscope==null || gs==null) return;
        gs.setViewport(viewport);
        Log.d(APP,TAG+".drawWheelGraph(size: "+gs.size+", radius: "+gs.radius+")");

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

            for(int i = 0; i<=1; ++i) {
                float r = 0.0f;
                for(int l = 0; l<gs.levels.length; ++l) {
                    GraphLevel lvl = gs.levels[l];
                    float r2 = gs.radius-r;
                    for(int c = 0; c<lvl.content.length; ++c) {
                        WheelGraph con = lvl.content[c];
                        if(i==0) con.drawBackground(canvas,gs,lvl,r2);
                        else con.drawForeground(canvas,gs,lvl,r2);
                    }
                    r += lvl.size*gs.scale;
                }
            }
            canvas.restore();
        } catch(Exception e) {
            Log.e(APP,TAG+".drawWheelGraph",e);
        }
    }

    protected void drawInfoLayer(Canvas canvas,GraphStyle gs) {
        Horoscope h = horoscope;
        float textSize1 = 16.0f*density;//s*0.04f;
        float textSize2 = 14.0f*density;//s*0.035f;
        float textSize3 = 12.0f*density;//s*0.03f;
        float lineHeight1 = textSize1+1.33f*density;//s*0.003f;
        float lineHeight2 = textSize2+1.33f*density;//s*0.003f;
        float margin1 = 4.0f*density;//s*0.01f;
        float x1, y1;
        String str;
        int i/*,d*/, n, m;
        double x, y, z;
//Log.d(APP,TAG+".drawInfoLayer(paint: "+paint+", symbolFont: "+AstroActivity.symbolFont+")");
        paint.setTypeface(AstroActivity.symbolFont);
        paint.setStyle(Paint.Style.FILL);
        paint.setAntiAlias(true);
        paint.setColor(0xff000000);

        paint.setTextSize(textSize2/*24.0f*/);
        x1 = viewport.left+margin1;
        y1 = viewport.top+margin1-paint.ascent();
        str = h.getFormalDateString();
        canvas.drawText(str,x1,y1,paint);
        paint.setTextSize(textSize2/*20.0f*/);
        str = h.getTimeAndTimeZoneString();
        canvas.drawText(str,x1,y1+textSize2,paint);
        y1 += lineHeight2+lineHeight2;

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
        paint.setTextSize(textSize1/*24.0f*/);
        canvas.drawText(moonPhasesUnicode[i],x1,y1+4.8f*density/*s*0.012f*/,paint);
        paint.setTextSize(textSize3/*16.0f*/);
		/*if(d>0) str = String.format(Locale.ENGLISH,"%1$+dd %2$d:%3$02d",d,n,m);
		else */
        str = String.format(Locale.ENGLISH,"%1$+d:%2$02d",n,m);
        canvas.drawText(str,x1+lineHeight1,y1+(textSize1-textSize3)*0.5f,paint);

        paint.setTextSize(textSize2/*24.0f*/);
        x1 = viewport.right-margin1;
        y1 = viewport.top+margin1-paint.ascent();
        str = h.getLongitudeString()+" "+h.getLatitudeString();
        canvas.drawText(str,x1-paint.measureText(str),y1,paint);
    }

    private void positionPlanet(Horoscope h,long id,float xc,float yc,WheelCell[] pl,int p,double a,float sz,float r,int endp,int iter) {
//Log.d(APP,TAG+".positionPlanet(p: "+p+", pa: "+pa[p]+", endp: "+endp+", iter: "+iter+")");
        WheelCell r1 = pl[p];
        float sz2 = sz*0.5f;
        float sz3 = sz*0.1f;
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

    private int intersectingPlanet(Horoscope h,WheelCell[] pl,int p,WheelCell r1,int startp,int endp) {
//Log.d(APP,TAG+".intersectingPlanet(p: "+p+", startp: "+startp+", endp: "+endp+")");
        int i, pid;
        WheelCell r2;
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

    private void clearMap(int offset,int length,WheelCell[] pl,Horoscope h) {
        final Cell[] cellMap = getCellMap();
        WheelCell cell;
        if(pl!=null)
            for(int i = 0; i<length; ++i) {
                cell = pl[i];
                if(cell==null) pl[i] = cell = new WheelCell();
                else cell.set(0,-1l,0.0f,0.0f,0.0f,0.0f);
                cell.angle = (float)(DTR*(ascendant-h.planetAbsoluteLongitude(i)));
                cellMap[offset+i] = cell;
            }
    }
}

