package net.spirangle.sphinx.widgets;

import static net.spirangle.sphinx.config.AstrologyProperties.*;
import static net.spirangle.sphinx.config.SphinxProperties.*;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Typeface;
import android.location.Location;
import android.os.Bundle;
import android.util.Log;
import android.widget.RemoteViews;

import net.spirangle.sphinx.R;
import net.spirangle.sphinx.activities.SplashActivity;
import net.spirangle.sphinx.astro.Horoscope;
import net.spirangle.sphinx.astro.Symbol;
import net.spirangle.sphinx.services.LocationService;

import java.util.Calendar;


public class SphinxWidgetProvider extends AppWidgetProvider {
    private static final String TAG = SphinxWidgetProvider.class.getSimpleName();

    public static final double DTR = 0.017453292519943295769236907684886;
    public static final double RTD = 57.295779513082320876798154814105;
    public static final double RTH = 3.8197186342054880584532103209403;
    public static final double HTR = 0.26179938779914943653855361527329;
    public static final double RTS = 2.0626480624709635516e5;
    public static final double STR = 4.8481368110953599359e-6;

    private static final float density = Resources.getSystem().getDisplayMetrics().density;
    private static final float scale = 1.0f;

    private static final String[] moonPhaseUnicode = {
        "\uD83C\uDF11",
        "\uD83C\uDF12",
        "\uD83C\uDF13",
        "\uD83C\uDF14",
        "\uD83C\uDF15",
        "\uD83C\uDF16",
        "\uD83C\uDF17",
        "\uD83C\uDF18",
    };

    protected static final int lineColor = 0xff000000;
    protected static final int textColor = 0xffffffff;

    protected static final int[] zodiacColors = {
        0xffff6666,0xff66ff66,0xffffff66,0xff6666ff,
        0xffff6666,0xff66ff66,0xffffff66,0xff6666ff,
        0xffff6666,0xff66ff66,0xffffff66,0xff6666ff,
    };

    protected static final int[] aspectColors = {
        0xff00ff00,0xffcc0099,0xff00cc99,0xffcccc00,
        0xffcc0000,0xff99cc00,0xff00cc00,0xffcc9900,
        0xffff0000,0xff0000ff,0xffff0099,0xffff0099,
        0xffcc9900
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

    protected static final int[] planets = {
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
        -1};

    protected static final float TFS = 12.0f*density;
    protected static final float PFS = 18.0f*density;
    protected static final float DFS = 14.0f*density;
    protected static final float RFS = 10.0f*density;
    protected static final float HFS = 10.0f*density;
    protected static final float WFS = 16.0f*density;

    protected static final float spacing = 1.5f*density;
    protected static final float padding = 3.0f*density;

	/*public static int dpToPx(int dp) {
		return (int)(dp*Resources.getSystem().getDisplayMetrics().density);
	}

	public static int pxToDp(int px) {
		return (int)(px/Resources.getSystem().getDisplayMetrics().density);
	}*/

    private class WidgetData {
        public Context context;
        public RemoteViews views;
        public Bitmap bitmap = null;
        public Canvas canvas = null;
        public Paint paint;
        public Typeface symbolFont;
        public float width = 0.0f;
        public float height = 0.0f;
        public Horoscope horoscope = null;
        public WheelGraphPlanet[] planets;

        public WidgetData(Context context) {
            this.context = context;
            this.paint = new Paint();
            this.symbolFont = Typeface.createFromAsset(context.getAssets(),"SeshatSymbols.ttf");
        }

        public void setSize(float w,float h) {
            if(width!=w || height!=h || bitmap==null || canvas==null) {
                bitmap = Bitmap.createBitmap((int)w,(int)h,Bitmap.Config.ARGB_8888);
                canvas = new Canvas(bitmap);
            }
            width = w;
            height = h;
        }

        public void draw() {
            long t1 = System.currentTimeMillis();
            Horoscope h = horoscope;
            paint.setStyle(Paint.Style.FILL);
            paint.setAntiAlias(true);
            paint.setColor(textColor);
            paint.setTextSize(TFS);
            paint.setTypeface(symbolFont);

            String str = h.getName();
            float p = height-TFS, c = p*0.5f;
            canvas.drawText(str,(p-paint.measureText(str))*0.5f,0.0f-paint.ascent(),paint);
            drawMoonPhase(0.0f,height-PFS);
            drawWheel(c,TFS+c,c-spacing);
            drawPlanets(p,0.0f);

            long t2 = System.currentTimeMillis();
            Log.d(APP,TAG+".draw("+(t2-t1)+")");
        }

        private void drawMoonPhase(float x,float y) {
            float baseline;
            int i, m, n;
            double p, d, h;
            String str;
            p = horoscope.moonPhase();
            d = horoscope.moonPhaseDegreeDays();
            i = (int)(p/45.0);
            p = (p-i*45.0);
            if(p>22.5) {
                h = (p-45.0)*d;
                i = (i+1)&7;
            } else {
                h = p*d;
            }
            n = (int)(h*24.0);
            m = (int)Math.abs(h*1440.0)%60;
            paint.setColor(textColor);
            paint.setTextSize(PFS);
            baseline = (PFS-paint.ascent())*0.5f;
            canvas.drawText(moonPhaseUnicode[i],x,y+baseline,paint);
            paint.setTextSize(HFS);
            str = String.format("%1$+d",n);
//			baseline = (PFS-paint.ascent())*0.5f;
            canvas.drawText(str,x+PFS,y+baseline,paint);
        }

        public void drawWheel(float xc,float yc,float r) {
            Horoscope h = horoscope;
            WheelGraphPlanet p1, p2;
            int i, j, a, n, p = h.planets(), x, y;
            float o1, o2;
            float r1 = r-9.0f*density;
            float r2 = r-19.0f*density;
            float r3 = r-25.0f*density;
            float r4 = r-29.0f*density;
            float x1, y1, x2, y2;
            boolean l;
            String str;

            planets = new WheelGraphPlanet[p];
            for(i = 0; i<p; ++i)
                planets[i] = new WheelGraphPlanet(i,(float)(DTR*h.planetAbsoluteLongitude(i)),xc,yc,r1);
            WheelGraphPlanet.organize(planets,xc,yc,r1,WFS,(float)(DTR*1.0));

            paint.setStyle(Paint.Style.STROKE);
            paint.setAntiAlias(true);
            paint.setStrokeWidth(1.0f*density);
            paint.setColor(0x66000000);
            canvas.drawCircle(xc,yc,r,paint);

            paint.setStyle(Paint.Style.FILL);
            paint.setAntiAlias(false);
            paint.setColor(0x99ffffff);
            canvas.drawCircle(xc,yc,r3-1.0f,paint);

            paint.setStyle(Paint.Style.STROKE);
            paint.setAntiAlias(true);
            paint.setStrokeWidth(1.0f*density);
            paint.setColor(0xff000000);
            canvas.drawCircle(xc,yc,r3,paint);

            paint.setTextSize(WFS);
            paint.setStrokeWidth(0.5f*density);
            for(i = 0; i<p; ++i) {
                p1 = planets[i];
                n = h.planetId(p1.index);
                str = Symbol.getUnicode(n);
                paint.setStyle(Paint.Style.FILL);
                paint.setColor(textColor);
                canvas.drawText(str,p1.x-paint.measureText(str)*0.5f,p1.y-paint.ascent()*0.5f,paint);
                x1 = xc-(float)Math.cos(-(double)p1.angle)*r2;
                y1 = yc-(float)Math.sin(-(double)p1.angle)*r2;
                x2 = xc-(float)Math.cos(-(double)p1.longitude)*(r3+3.0f);
                y2 = yc-(float)Math.sin(-(double)p1.longitude)*(r3+3.0f);
                paint.setStyle(Paint.Style.STROKE);
//				paint.setColor(0x99ffffff);
                paint.setColor(0xffff9900);
                canvas.drawLine(x1,y1,x2,y2,paint);
            }

            paint.setStyle(Paint.Style.STROKE);
            paint.setStrokeWidth(1.5f*density);
            for(x = 0; x<p; ++x) {
                p1 = planets[x];
                i = h.planetId(p1.index);
                if(i==ASTRO_ASCENDANT || i==ASTRO_MC) continue;
                for(y = x+1; y<p; ++y) {
                    p2 = planets[y];
                    j = h.planetId(p2.index);
                    if(j==ASTRO_ASCENDANT || j==ASTRO_MC) continue;
                    a = h.aspect(p1.index,p2.index);
//Log.d(APP,"k: "+k+", l: "+l+", a: "+a);
                    if(a>=CONJUNCTION && a<=OPPOSITION && aspectShow[a&0xffff]
//									&& (!aspectShow[13] || horoscope.findPattern(x,y))
                    ) {
                        p2 = planets[y];
                        x1 = xc-(float)Math.cos(-(double)p1.longitude)*r4;
                        y1 = yc-(float)Math.sin(-(double)p1.longitude)*r4;
                        x2 = xc-(float)Math.cos(-(double)p2.longitude)*r4;
                        y2 = yc-(float)Math.sin(-(double)p2.longitude)*r4;
                        paint.setColor(aspectColors[a&0xffff]);
                        canvas.drawLine(x1,y1,x2,y2,paint);
                    }
                }
            }
        }

        public void drawPlanets(float x,float y) {
//			if(horoscope==null) return;
            String str;

            Horoscope h = horoscope;
            int i, p = h.planets(), n = -1, sign;
            float x1 = x, y1 = y, x2, y2;
            float baseline, center;
            float column = (width-x)/2.0f, row = (height-y)/5.0f;
/*			paint.setStyle(Paint.Style.STROKE);
			paint.setAntiAlias(false);
			paint.setColor(lineColor);
			y1 = row+vsp2;
			canvas.drawLine(0,y1,width,y1,paint);
			y1 += row+vspacing;
			canvas.drawLine(0,y1,width,y1,paint);
			x1 = column+hsp2;
			canvas.drawLine(x1,row+vsp2,x1,height,paint);
			x1 += column+hspacing;
			canvas.drawLine(x1,0,x1,height,paint);
			x1 += column+hspacing;
			canvas.drawLine(x1,0,x1,height,paint);*/

            paint.setStyle(Paint.Style.FILL);
            paint.setAntiAlias(true);
            paint.setColor(textColor);
            paint.setTextSize(26.0f);
            paint.setTypeface(symbolFont);

            x1 = x;
            y1 = y;
            paint.setTextSize(PFS);
            for(i = 0; i<p; ++i) {
                n = h.planetId(i);
                x2 = x1;
                y2 = y1;

                paint.setStyle(Paint.Style.STROKE);
                paint.setStrokeWidth(1.0f*density);
                paint.setColor(0x66000000);
                canvas.drawRect(x2+spacing,y2+spacing,x1+column-spacing,y1+row-spacing,paint);

                x2 += padding;
                paint.setStyle(Paint.Style.FILL);
                paint.setColor(textColor);
                paint.setTextSize(PFS);
                str = Symbol.getUnicode(n);
                center = (PFS-paint.measureText(str))*0.6f;
                baseline = (row-paint.ascent())*0.5f;
                canvas.drawText(str,x2+center,y2+baseline,paint);
                x2 += PFS*1.2f;

                sign = h.planetSign(i);
                paint.setColor(zodiacColors[(sign&0xf)]);
                str = Symbol.getUnicode(sign);
                center = (PFS-paint.measureText(str))*0.6f;
                canvas.drawText(str,x2+center,y2+baseline,paint);
                x2 += PFS*1.2f;

                paint.setColor(textColor);
                paint.setTextSize(DFS);
//					str = Coordinate.formatHM(h.planetLongitude(i),'°',30,"");
                str = (int)h.planetLongitude(i)+"°";
                baseline = (row-paint.ascent())*0.5f;
                canvas.drawText(str,x2,y2+baseline,paint);
                x2 += paint.measureText(str);

                if(h.planetIsRetrograde(i)) {
                    paint.setTextSize(RFS);
                    str = "R";
                    canvas.drawText(str,x2+2.0f,y2+baseline,paint);
                }
                y1 += row;
                if(y1>=height) {
                    x1 += column;
                    y1 = 0;
                }
            }
        }
    }

    @Override
    public void onAppWidgetOptionsChanged(Context context,AppWidgetManager appWidgetManager,int appWidgetId,Bundle newOptions) {
        int minW_dp = newOptions.getInt(AppWidgetManager.OPTION_APPWIDGET_MIN_WIDTH);
        int maxW_dp = newOptions.getInt(AppWidgetManager.OPTION_APPWIDGET_MAX_WIDTH);
        int minH_dp = newOptions.getInt(AppWidgetManager.OPTION_APPWIDGET_MIN_HEIGHT);
        int maxH_dp = newOptions.getInt(AppWidgetManager.OPTION_APPWIDGET_MAX_HEIGHT);
        Log.d(APP,TAG+".onAppWidgetOptionsChanged(minW_dp: "+minW_dp+", maxW_dp: "+maxW_dp+", minH_dp: "+minH_dp+", maxH_dp: "+maxH_dp+")");
    }

    @Override
    public void onUpdate(Context context,AppWidgetManager manager,int[] widgetIds) {
        int i, id;
        Horoscope h = new Horoscope();
        Location location = LocationService.getInstance().getLocation();
        if(location!=null) {
            Calendar cal = Calendar.getInstance();
            double lon = location.getLongitude();
            double lat = location.getLatitude();
            double tz = (double)cal.get(Calendar.ZONE_OFFSET)/3600000.0;
            double dst = (double)cal.get(Calendar.DST_OFFSET)/3600000.0;
            h.setDaylightSavingTime(dst);
            h.setLocation(null,null,null,lon,lat,tz);
        }
        h.calculate(planets,0);

        WidgetData wd = new WidgetData(context);
        float width = 294.0f*density-7.0f-9.0f;
        float height = 146.0f*density-6.0f-9.0f;
        Log.d(APP,TAG+".onUpdate(width: "+width+", height: "+height+", density: "+density+")");
        wd.horoscope = h;
        wd.setSize(width,height);
        wd.draw();
        for(i = 0; i<widgetIds.length; i++) {
            id = widgetIds[i];

            Intent intent = new Intent(context,SplashActivity.class);
            intent.putExtra(EXTRA_RADIX1,wd.horoscope);
            intent.putExtra(EXTRA_GRAPH,0);
//			startActivityForResult(intent,ACTIVITY_HOROSCOPE);
//			Intent intent = new Intent(context,ExampleActivity.class);
            PendingIntent pendingIntent = PendingIntent.getActivity(context,ACTIVITY_HOROSCOPE,intent,PendingIntent.FLAG_UPDATE_CURRENT);

            wd.views = new RemoteViews(context.getPackageName(),R.layout.widget_astro_clock);
            wd.views.setImageViewBitmap(R.id.image_view,wd.bitmap);
            wd.views.setOnClickPendingIntent(R.id.image_view,pendingIntent);
            manager.updateAppWidget(id,wd.views);
        }
    }
}

