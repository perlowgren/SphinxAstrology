package net.spirangle.sphinx.astro;

import static net.spirangle.sphinx.config.SphinxProperties.APP;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.util.Log;

import net.spirangle.sphinx.Horoscope;
import net.spirangle.sphinx.R;
import net.spirangle.sphinx.Symbol;


public class AspectTableView extends HoroscopeView {
    private static final String TAG = "AspectTableView";

    private static final int columns = 3;
    private static final float spacing = 5.0f;
    private static final float padding = 2.0f;
    private static final float header = padding+TFS+padding;
    private static final float box = padding+AFS+padding;

    public AspectTableView(Context context) { super(context); }

    public AspectTableView(Context context,AttributeSet attrs) { super(context,attrs); }

    public AspectTableView(Context context,AttributeSet attrs,int defStyle) { super(context,attrs,defStyle); }

    public AspectTableView(Context context,AttributeSet attrs,int defStyle,int defStyleRes) { super(context,attrs,defStyle,defStyleRes); }

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
                h += (int)((float)p*box+spacing);
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
        drawAspectTable(canvas);
        long t2 = System.currentTimeMillis();
//Log.d(APP,TAG+".onDraw("+(t2-t1)+")");
    }

    protected void drawAspectTable(Canvas canvas) {
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

        clearMap(0,a+p);

        try {

            paint.setStyle(Paint.Style.FILL);
            paint.setAntiAlias(true);
            paint.setTypeface(AstroActivity.symbolFont);

            x = spacing;
            y = 0.0f;
            paint.setTextSize(TFS);
            paint.setColor(titleBackground);
            canvas.drawRect(0.0f,0.0f,width,header,paint);
            paint.setColor(titleColor);
            str = context.getString(R.string.table_aspect_table);
            baseline = (header-TFS)*0.5f-paint.ascent();
            canvas.drawText(str,x,y+baseline,paint);
            str = context.getString(R.string.table_elements);
            canvas.drawText(str,x+120.0f,y+baseline,paint);

            paint.setTextSize(AFS);
            baseline = (box-AFS)*0.5f-paint.ascent();
            x = spacing;
            y = header+spacing;
            for(p1 = 0,x1 = x; p1<p; ++p1) {
                n = h.planetId(p1);
                id = h.planetSymbolId(p1);

                if(p1>0 && p1<p-1) x1 += box;
                y1 = y+p1*box;

                if(id!=-1l) {
                    r = map[a+p1];
                    r.set(a+p1,id,x1,y1,x1+box,y1+box);
                    if(r.isActive()) {
                        paint.setColor(0x11000000);
                        canvas.drawRect(r,paint);
                    }
                }

                paint.setColor(0xff000000);
                str = Symbol.getUnicode(n);
                center = padding+(box-paint.measureText(str))*0.5f;
                canvas.drawText(str,x1+center,y1+baseline,paint);

                if(p1==0) canvas.drawLine(x1,y1+box,x1,y1+(p-p1)*box,paint);
                if(p1<p-2) canvas.drawLine(x1+box,y1+box,x1+box,y1+(p-p1)*box,paint);

                canvas.drawLine(x,y1+box,x1+(p1<p-2? box : 0.0f),y1+box,paint);
                if(p1==p-2) canvas.drawLine(x,y1+box+box,x1,y1+box+box,paint);
                if(p1<p-2) {
                    for(p2 = p1+1,y1 += box; p2<p; ++p2,y1 += box) {
                        n = h.aspect(p1,p2);
                        if(n!=-1) {
                            paint.setColor(aspectColors[n&0xffff]);
                            str = Symbol.getUnicode(n);
                            center = padding+(box-paint.measureText(str))*0.5f;
                            canvas.drawText(str,x1+center,y1+baseline,paint);
                        }
                    }
                }
            }

        } catch(Exception e) {
            Log.e(APP,TAG+".drawChartGraph",e);
        }

    }
}

