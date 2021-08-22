package net.spirangle.sphinx.views;

import static net.spirangle.sphinx.config.SphinxProperties.APP;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.util.Log;

import net.spirangle.sphinx.R;
import net.spirangle.sphinx.activities.AstroActivity;
import net.spirangle.sphinx.astro.Coordinate;
import net.spirangle.sphinx.astro.Horoscope;
import net.spirangle.sphinx.astro.Symbol;


public class AspectListView extends HoroscopeView {
    private static final String TAG = AspectListView.class.getSimpleName();

    private static final int columns = 3;
    private static final float spacing = 5.0f;
    private static final float padding = 2.0f;
    private static final float header = padding+fontSize1+padding;
    private static final float row = padding+fontSize2+padding;

    private int[] index;

    public AspectListView(Context context) { super(context); }

    public AspectListView(Context context,AttributeSet attrs) { super(context,attrs); }

    public AspectListView(Context context,AttributeSet attrs,int defStyle) { super(context,attrs,defStyle); }

    public AspectListView(Context context,AttributeSet attrs,int defStyle,int defStyleRes) { super(context,attrs,defStyle,defStyleRes); }

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
                int a = horoscope.aspects();
                a = a/columns+((a%columns)==0? 0 : 1);
                h += (int)((float)a*(row+spacing));
            }
            if(hm==MeasureSpec.AT_MOST) h = Math.min(hs,h);
        }
//Log.d(APP,TAG+".onMeasure(w: "+w+", h: "+h+")");
        setMeasuredDimension(w,h);
    }

    @Override
    public void onDraw(Canvas canvas) {
//Log.d(APP,TAG+".onDraw()");
        super.onDraw(canvas);
        if(horoscope==null) return;
        long t1 = System.currentTimeMillis();
        drawAspectList(canvas);
        long t2 = System.currentTimeMillis();
//Log.d(APP,TAG+".onDraw("+(t2-t1)+")");
    }

    @Override
    public void setHoroscope(Horoscope h) {
        super.setHoroscope(h);
        int p = h.planets(), a = h.aspects(), p1, p2, i;
        index = new int[a];
        for(p1 = 0,i = 0; p1<p; ++p1)
            if(p1<p-2)
                for(p2 = p1+1; p2<p; ++p2)
                    if(h.aspect(p1,p2)!=-1)
                        index[i++] = h.aspectIndex(p1,p2);
    }

    protected void drawAspectList(Canvas canvas) {
        if(horoscope==null) return;
        Context context = getContext();
        MapRectF r;
        Horoscope h = horoscope;
        String str;
        int i, p1, p2, m, n, p = h.planets(), a = h.aspects();
        long id;
        float x, y;
        float x1 = 0.0f, y1 = 0.0f, x2, y2, column, center, right, baseline;
        float width = viewport.right-viewport.left;
        float height = viewport.bottom-viewport.top;

//Log.d(APP,TAG+".drawAspectsTable(height: "+height+")");

        clearMap(0,index.length);

        try {

            paint.setStyle(Paint.Style.FILL);
            paint.setAntiAlias(true);
            paint.setTypeface(AstroActivity.symbolFont);

            x = spacing;
            y = 0.0f;
            paint.setTextSize(fontSize1);
            paint.setColor(titleBackground);
            canvas.drawRect(0.0f,0.0f,width,header,paint);
            paint.setColor(titleColor);
            str = context.getString(R.string.table_aspects);
            baseline = (header-fontSize1)*0.5f-paint.ascent();
            canvas.drawText(str,x,y+baseline,paint);

            x = spacing;
            y = header+spacing;
            column = (width-spacing*(float)(columns+1))/(float)columns;
            for(i = 0,x1 = x,y1 = y,m = 0; i<index.length; ++i) {
                n = index[i];
                id = h.aspectSymbolId(n);
//Log.d(APP,TAG+".drawAspectsTable(id: "+id+")");
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

                paint.setColor(textColor);
                paint.setTextSize(fontSize2);
                str = Symbol.getUnicode(h.planetId(h.aspectPlanet1(n)));
                center = padding+7.0f+(fontSize2-paint.measureText(str))*0.5f;
                baseline = (row-fontSize2)*0.5f-paint.ascent();
                canvas.drawText(str,x1+center,y1+baseline,paint);

                paint.setColor(aspectColors[h.aspect(n)&0xffff]);
                str = Symbol.getUnicode(h.aspect(n));
                center = padding+7.0f+(fontSize2-paint.measureText(str))*0.5f;
                canvas.drawText(str,x1+fontSize2+center,y1+baseline,paint);

                paint.setColor(textColor);
                str = Symbol.getUnicode(h.planetId(h.aspectPlanet2(n)));
                center = padding+7.0f+(fontSize2-paint.measureText(str))*0.5f;
                canvas.drawText(str,x1+fontSize2+fontSize2+center,y1+baseline,paint);

                paint.setTextSize(fontSize4);
                str = Coordinate.formatHM(h.aspectOrb(n),'°',360,"");
                right = column-padding-7.0f-paint.measureText(str);
                baseline = (row-fontSize4)*0.5f-paint.ascent();
                canvas.drawText(str,x1+right,y1+baseline,paint);

                y1 += row+spacing;
                if(y1+row+spacing>height) {
                    y1 = y;
                    x1 += column+spacing;
                }
            }

        } catch(Exception e) {
            Log.e(APP,TAG+".drawChartGraph",e);
        }

    }
}

