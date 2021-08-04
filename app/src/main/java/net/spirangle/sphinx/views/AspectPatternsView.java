package net.spirangle.sphinx.views;

import static net.spirangle.sphinx.config.AstrologyProperties.*;
import static net.spirangle.sphinx.config.SphinxProperties.APP;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.util.Log;

import net.spirangle.sphinx.activities.AstroActivity;
import net.spirangle.sphinx.astro.Coordinate;
import net.spirangle.sphinx.astro.Horoscope;
import net.spirangle.sphinx.astro.Symbol;


public class AspectPatternsView extends HoroscopeView {
    private static final String TAG = "AspectPatternsView";

    public AspectPatternsView(Context context) { super(context); }

    public AspectPatternsView(Context context,AttributeSet attrs) { super(context,attrs); }

    public AspectPatternsView(Context context,AttributeSet attrs,int defStyle) { super(context,attrs,defStyle); }

    public AspectPatternsView(Context context,AttributeSet attrs,int defStyle,int defStyleRes) { super(context,attrs,defStyle,defStyleRes); }

    @Override
    public void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        if(horoscope==null) return;
        long t1 = System.currentTimeMillis();
        drawPlanetsTable(canvas);
        long t2 = System.currentTimeMillis();
        Log.d(APP,TAG+".onDraw("+(t2-t1)+")");
    }

    protected void drawPlanetsTable(Canvas canvas) {
        if(horoscope==null) return;
        MapRectF r;
        Horoscope h = horoscope;
        String str;
        int i, j, m = 0, n, z, p = h.planets(), sign;
        long id;
        float x, y, width = viewport.right, height = viewport.bottom, fs = 30.0f, dfs = 26.0f, rfs = 16.0f;
        float padding = 2.0f;

        int[] houses = {0,1,2,9,10,11};
        int[] houseIds = {ASTRO_ASCENDANT,ASTRO_2ND_HOUSE,ASTRO_3RD_HOUSE,ASTRO_MC,ASTRO_11TH_HOUSE,ASTRO_12TH_HOUSE};

        clearMap(0,p+houses.length);

        try {

            paint.setStyle(Paint.Style.FILL);
            paint.setAntiAlias(true);
            paint.setTextSize(fs);
            paint.setTypeface(AstroActivity.symbolFont);
            for(i = 0,x = 20.0f,y = 10.0f-paint.ascent(); i<p; ++i) {
                n = h.planetId(i);
                id = h.planetSymbolId(i);
                if(id==-1l) continue;
                r = map[m];
                r.set(m,id,x-padding,y+paint.ascent()-padding,x+145.0f+padding,y+paint.descent()+padding);
                paint.setColor(r.isActive()? 0xffd0d0d0 : 0xfff0f0f0);
                canvas.drawRect(r,paint);
                ++m;

                paint.setColor(0xff000000);
                paint.setTextSize(fs);
                str = Symbol.getUnicode(n);
                canvas.drawText(str,x,y,paint);

                paint.setTextSize(dfs);
                str = Coordinate.formatHM(h.planetLongitude(i),'°',30,"");
                canvas.drawText(str,x+100.0f-paint.measureText(str),y,paint);

                if(h.planetIsRetrograde(i)) {
                    paint.setTextSize(rfs);
                    str = "R";
                    canvas.drawText(str,x+100.0f,y,paint);
                }

                paint.setTextSize(fs);
                sign = h.planetSign(i);
                paint.setColor(zodiacColors[(sign&0xf)]);
                str = Symbol.getUnicode(sign);
                canvas.drawText(str,x+115.0f,y,paint);

                if(y+15.0f+fs<height) y += 6.0f+fs;
                else {
                    y = 10.0f-paint.ascent();
                    x += 180.0f;
                    if(x>=225.0f) break;
                }
            }
            for(i = 0,x = 370.0f,y = 10.0f-paint.ascent(); i<houses.length; ++i) {
                j = houses[i];
                n = houseIds[i];
                z = h.houseSign(i);
                id = h.houseSymbolId(j);
                if(id==-1l) continue;
                r = map[m];
                r.set(m,id,x-padding,y+paint.ascent()-padding,x+145.0f+padding,y+paint.descent()+padding);
                paint.setColor(r.isActive()? 0xffd0d0d0 : 0xfff0f0f0);
                canvas.drawRect(r,paint);
                ++m;

                paint.setColor(0xff000000);
                paint.setTextSize(fs);
                if(n==ASTRO_ASCENDANT || n==ASTRO_MC) str = Symbol.getUnicode(n);
                else str = Integer.toString(Symbol.Attribute.valueOf(n));
                canvas.drawText(str,x,y,paint);

                paint.setTextSize(dfs);
                str = Coordinate.formatHM(h.houseCusp(j),'°',30,"");
                canvas.drawText(str,x+100.0f-paint.measureText(str),y,paint);

                paint.setTextSize(fs);
                sign = h.houseSign(j);
                paint.setColor(zodiacColors[(sign&0xf)]);
                str = Symbol.getUnicode(sign);
                canvas.drawText(str,x+115.0f,y,paint);

                y += 6.0f+fs;
            }

        } catch(Exception e) {
            Log.e(APP,TAG+".drawChartGraph",e);
        }

    }
}

