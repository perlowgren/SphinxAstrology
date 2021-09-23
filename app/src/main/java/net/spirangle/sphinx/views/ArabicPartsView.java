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


public class ArabicPartsView extends HoroscopeView {
    private static final String TAG = ArabicPartsView.class.getSimpleName();

    public ArabicPartsView(Context context) {
        super(context);
    }

    public ArabicPartsView(Context context,AttributeSet attrs) {
        super(context,attrs);
    }

    public ArabicPartsView(Context context,AttributeSet attrs,int defStyle) {
        super(context,attrs,defStyle);
    }

    public ArabicPartsView(Context context,AttributeSet attrs,int defStyle,int defStyleRes) {
        super(context,attrs,defStyle,defStyleRes);
    }

    @Override
    public void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        if(horoscope==null) return;

        paint.setStyle(Paint.Style.FILL);
        paint.setAntiAlias(true);

        long t1 = System.currentTimeMillis();
        drawArabicPartsTable(canvas);
        long t2 = System.currentTimeMillis();
        Log.d(APP,TAG+".onDraw("+(t2-t1)+")");
    }

    protected void drawArabicPartsTable(Canvas canvas) {
        if(horoscope==null) return;
        Cell r;
        Horoscope h = horoscope;
        String str;
        int i, n, p = h.planets(), sign;
        long id;
        float x, y, fs = 30.0f, dfs = 26.0f, rfs = 16.0f;

        int[] houses = {0,1,2,9,10,11};
        int[] houseIds = {ASTRO_ASCENDANT,ASTRO_2ND_HOUSE,ASTRO_3RD_HOUSE,ASTRO_MC,ASTRO_11TH_HOUSE,ASTRO_12TH_HOUSE};

        clearCellMap(0,p+houses.length);

        paint.setTypeface(AstroActivity.symbolFont);
        paint.setTextSize(fs);
        for(i = 0,x = 20.0f,y = 10.0f-paint.ascent(); i<p; ++i) {
            n = h.planetId(i);
            id = h.planetSymbolId(i);
            if(id==-1l) continue;
            r = getNextCell();
            r.set(getCellIndex(),id,x-padding,y+paint.ascent()-padding,x+145.0f+padding,y+paint.descent()+padding);
            paint.setColor(r.isActive()? 0xffd0d0d0 : 0xfff0f0f0);
            canvas.drawRect(r,paint);

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
    }
}

