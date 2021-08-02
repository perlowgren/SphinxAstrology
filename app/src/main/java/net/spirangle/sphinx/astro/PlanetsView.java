package net.spirangle.sphinx.astro;

import static net.spirangle.sphinx.SphinxProperties.APP;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.util.Log;

import net.spirangle.sphinx.Coordinate;
import net.spirangle.sphinx.Horoscope;
import net.spirangle.sphinx.R;
import net.spirangle.sphinx.Symbol;


public class PlanetsView extends HoroscopeView {
    private static final String TAG = "PlanetsView";

    private static final int[] houses = {0,1,2,9,10,11};
    private static final int[] houseIds = {ASTRO_ASCENDANT,ASTRO_2ND_HOUSE,ASTRO_3RD_HOUSE,ASTRO_MC,ASTRO_11TH_HOUSE,ASTRO_12TH_HOUSE};

    private static final int columns = 3;
    private static final float spacing = 5.0f;
    private static final float padding = 2.0f;
    private static final float header = padding+TFS+padding;
    private static final float row = padding+PFS+padding;

    public PlanetsView(Context context) { super(context); }

    public PlanetsView(Context context,AttributeSet attrs) { super(context,attrs); }

    public PlanetsView(Context context,AttributeSet attrs,int defStyle) { super(context,attrs,defStyle); }

    public PlanetsView(Context context,AttributeSet attrs,int defStyle,int defStyleRes) { super(context,attrs,defStyle,defStyleRes); }

    @Override
    public void onMeasure(int wms,int hms) {
        super.onMeasure(wms,hms);
        int w = MeasureSpec.getSize(wms);
        int hm = MeasureSpec.getMode(hms);
        int hs = MeasureSpec.getSize(hms);
        int h = hs;
        if(hm!=MeasureSpec.EXACTLY) {
            if(horoscope!=null) {
                h = (int)(header+spacing);
                int p = horoscope.planets();
                for(int i = p-1, n; i>=0; --i) {
                    n = horoscope.planetId(i);
                    if(n==ASTRO_ASCENDANT || n==ASTRO_MC) --p;
                }
                p = p/(columns-1)+((p%(columns-1))==0? 0 : 1);
                if(p<6) p = 6;
                h += (int)((float)p*(row+spacing));
            }
            if(hm==MeasureSpec.AT_MOST) h = Math.min(hs,h);
        }
//Log.d(APP,TAG+".onMeasure(w: "+w+", h: "+h+")");
        setMeasuredDimension(w,h);
    }

    @Override
    public void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        if(horoscope==null) return;
        long t1 = System.currentTimeMillis();
        drawPlanetList(canvas);
        long t2 = System.currentTimeMillis();
//Log.d(APP,TAG+".onDraw("+(t2-t1)+")");
    }

    protected void drawPlanetList(Canvas canvas) {
        if(horoscope==null) return;
        Context context = getContext();
        MapRectF r;
        Horoscope h = horoscope;
        String str;
        int i, j, m = 0, n, z, p = h.planets(), sign;
        long id;
        float x, y;
        float x1 = 0.0f, y1 = 0.0f, x2, y2, column, center, right, baseline;
        float width = viewport.right-viewport.left;
        float height = viewport.bottom-viewport.top;

        clearMap(0,p+houses.length);

        try {

            paint.setStyle(Paint.Style.FILL);
            paint.setAntiAlias(true);
            paint.setTypeface(AstroActivity.symbolFont);

            column = (width-spacing*(float)(columns+1))/(float)columns;

            x = spacing;
            y = 0.0f;
            paint.setTextSize(TFS);
            paint.setColor(titleBackground);
            canvas.drawRect(0.0f,0.0f,width,header,paint);
            paint.setColor(titleColor);
            str = context.getString(R.string.table_planets);
            baseline = (header-TFS)*0.5f-paint.ascent();
            canvas.drawText(str,x,y+baseline,paint);
            str = context.getString(R.string.table_houses);
            canvas.drawText(str,x+column+spacing+column+spacing,y+baseline,paint);

            paint.setTextSize(PFS);
            x = spacing;
            y = header+spacing;
//Log.d(APP,TAG+".onDraw(PFS: "+PFS+", ascent+descent: "+(-paint.ascent()+paint.descent())+")");
            for(i = 0,x1 = x,y1 = y; i<p; ++i) {
                n = h.planetId(i);
                id = h.planetSymbolId(i);
                if(n==ASTRO_ASCENDANT || n==ASTRO_MC || id==-1l) continue;
                r = map[m];
                r.set(m,id,x1,y1,x1+column,y1+row);
                if(r.isActive()) {
                    paint.setColor(activeBoxColor);
                    canvas.drawRect(r,paint);
                } else {
                    paint.setStyle(Paint.Style.STROKE);
                    paint.setColor(boxColor);
                    canvas.drawRect(r,paint);
                    paint.setStyle(Paint.Style.FILL);
                }
                ++m;

                x2 = x1+(column-(PFS+65.0f+12.0f+PFS+30.0f))*0.5f;

                paint.setColor(textColor);
                paint.setTextSize(PFS);
                str = Symbol.getUnicode(n);
                baseline = (row-PFS)*0.5f-paint.ascent();
                canvas.drawText(str,x2,y1+baseline,paint);

                x2 += PFS+65.0f;
                paint.setTextSize(DFS);
                str = Coordinate.formatHM(h.planetLongitude(i),'°',30,"");
                baseline = (row-DFS)*0.5f-paint.ascent();
                canvas.drawText(str,x2-paint.measureText(str),y1+baseline,paint);

                if(h.planetIsRetrograde(i)) {
                    paint.setTextSize(RFS);
                    str = "R";
                    canvas.drawText(str,x2+2.0f,y1+baseline,paint);
                }

                x2 += 12.0f;
                paint.setTextSize(PFS);
                sign = h.planetSign(i);
                paint.setColor(zodiacColors[(sign&0xf)]);
                str = Symbol.getUnicode(sign);
                baseline = (row-PFS)*0.5f-paint.ascent();
                canvas.drawText(str,x2,y1+baseline,paint);

                x2 += PFS+30.0f;
                paint.setTextSize(DFS);
                paint.setColor(textColor);
                str = String.valueOf(1+h.planetHouse(i)-ASTRO_ASCENDANT);
                baseline = (row-DFS)*0.5f-paint.ascent();
                canvas.drawText(str,x2-paint.measureText(str),y1+baseline,paint);

                y1 += row+spacing;
                if(y1+row+spacing>height) {
                    x1 += column+spacing;
                    y1 = y;
                }
            }

            paint.setTextSize(PFS);
            x = width-spacing-column;
            y = header+spacing;
            for(i = 0,x1 = x,y1 = y; i<houses.length; ++i) {
                j = houses[i];
                n = houseIds[i];
                z = h.houseSign(i);
                id = h.houseSymbolId(j);
                if(id==-1l) continue;
                r = map[m];
                r.set(m,id,x1,y1,x1+column,y1+row);
                if(r.isActive()) {
                    paint.setColor(activeBoxColor);
                    canvas.drawRect(r,paint);
                } else {
                    paint.setStyle(Paint.Style.STROKE);
                    paint.setColor(boxColor);
                    canvas.drawRect(r,paint);
                    paint.setStyle(Paint.Style.FILL);
                }
                ++m;

                x2 = x1+(column-(40.0f+65.0f+12.0f+PFS))*0.5f;

                paint.setColor(textColor);
                paint.setTextSize(PFS);
                if(n==ASTRO_ASCENDANT || n==ASTRO_MC) str = Symbol.getUnicode(n);
                else str = Integer.toString(Symbol.Attribute.valueOf(n));
                baseline = (row-PFS)*0.5f-paint.ascent();
                canvas.drawText(str,x2,y1+baseline,paint);

                x2 += 40.0f+65.0f;
                paint.setTextSize(DFS);
                str = Coordinate.formatHM(h.planetLongitude(i),'°',30,"");
                baseline = (row-DFS)*0.5f-paint.ascent();
                canvas.drawText(str,x2-paint.measureText(str),y1+baseline,paint);

                x2 += 12.0f;
                paint.setTextSize(PFS);
                sign = h.planetSign(i);
                paint.setColor(zodiacColors[(sign&0xf)]);
                str = Symbol.getUnicode(sign);
                baseline = (row-PFS)*0.5f-paint.ascent();
                canvas.drawText(str,x2,y1+baseline,paint);

                y1 += row+spacing;
            }


/*		for(i=0,x=370.0f,y=10.0f-paint.ascent(); i<houses.length; ++i) {
			j   = houses[i];
			n   = houseIds[i];
			z   = h.houseSign(i);
			id  = h.houseSymbolId(j);
			if(id==-1l) continue;
			r   = map[m];
			r.set(m,id,x-padding,y+paint.ascent()-padding,x+145.0f+padding,y+paint.descent()+padding);
			if(r.isActive()) {
				paint.setColor(activeBoxColor);
				canvas.drawRect(r,paint);
			} else {
				paint.setStyle(Paint.Style.STROKE);
				paint.setColor(boxColor);
				canvas.drawRect(r,paint);
				paint.setStyle(Paint.Style.FILL);
			}
			++m;

			paint.setColor(textColor);
			paint.setTextSize(PFS);
			if(n==ASTRO_ASCENDANT || n==ASTRO_MC) str = Symbol.getUnicode(n);
			else str = Integer.toString(Symbol.Attribute.valueOf(n));
			canvas.drawText(str,x,y,paint);

			paint.setTextSize(DFS);
			str = Coordinate.formatHM(h.houseCusp(j),'°',30,"");
			canvas.drawText(str,x+100.0f-paint.measureText(str),y,paint);

			paint.setTextSize(PFS);
			sign = h.houseSign(j);
			paint.setColor(zodiacColors[(sign&0xf)]);
			str = Symbol.getUnicode(sign);
			canvas.drawText(str,x+115.0f,y,paint);

			y += 6.0f+PFS;
		}*/

        } catch(Exception e) {
            Log.e(APP,TAG+".drawChartGraph",e);
        }

    }
}

