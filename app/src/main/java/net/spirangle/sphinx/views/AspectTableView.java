package net.spirangle.sphinx.views;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.util.AttributeSet;

import net.spirangle.sphinx.R;
import net.spirangle.sphinx.activities.AstroActivity;
import net.spirangle.sphinx.astro.Horoscope;
import net.spirangle.sphinx.astro.Symbol;


public class AspectTableView extends HoroscopeView {
    private static final String TAG = AspectTableView.class.getSimpleName();

    public AspectTableView(Context context) {
        super(context);
    }

    public AspectTableView(Context context,AttributeSet attrs) {
        super(context,attrs);
    }

    public AspectTableView(Context context,AttributeSet attrs,int defStyle) {
        super(context,attrs,defStyle);
    }

    public AspectTableView(Context context,AttributeSet attrs,int defStyle,int defStyleRes) {
        super(context,attrs,defStyle,defStyleRes);
    }

    @Override
    public void onMeasure(int wms,int hms) {
        super.onMeasure(wms,hms);
        int w = MeasureSpec.getSize(wms);
        int hm = MeasureSpec.getMode(hms);
        int hs = MeasureSpec.getSize(hms);
        int h = hs;
        if(hm!=MeasureSpec.EXACTLY) {
            if(horoscope!=null) {
                h = (int)(headerHeight+spacing+bottomSpacing);
                int p = horoscope.planets();
                h += (int)((float)p*aspectCellHeight+spacing);
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

        paint.setStyle(Paint.Style.FILL);
        paint.setAntiAlias(true);

        Context context = getContext();
        final float distributionX = (viewport.right-viewport.left)-150.0f*density;
        final float[] headerPositions = { spacing,distributionX+spacing };
        drawHeader(canvas,headerPositions,
                   context.getString(R.string.table_aspect_table),
                   context.getString(R.string.table_distribution));

        clearCellMap(0,horoscope.planets()+horoscope.aspects());

        long t1 = System.currentTimeMillis();
        drawAspectTable(canvas);
        long t2 = System.currentTimeMillis();
        drawDistributionTable(canvas,distributionX);
        long t3 = System.currentTimeMillis();
//Log.d(APP,TAG+".onDraw("+(t2-t1)+")");
    }

    protected void drawAspectTable(Canvas canvas) {
        if(horoscope==null) return;
        Cell r;
        Horoscope h = horoscope;
        String str;
        int p1, p2, n, p = h.planets(), a = h.aspects();
        long id;
        float x, y, center, baseline;
        final float ach = aspectCellHeight;
        final float acp = aspectCellPadding;

        paint.setTypeface(AstroActivity.symbolFont);
        paint.setTextSize(fontSizeAspect);

        baseline = (ach-fontSizeAspect)*0.5f-paint.ascent();
        for(p1 = 0,x = spacing; p1<p; ++p1) {
            n = h.planetId(p1);
            id = h.planetSymbolId(p1);

            if(p1>0 && p1<p-1) x += ach;
            y = headerHeight+spacing+p1*ach;

            if(id!=-1L) {
                r = getCell(a+p1);
                r.set(a+p1,id,x,y,x+ach,y+ach);
                if(r.isActive()) {
                    paint.setColor(0x11000000);
                    canvas.drawRect(r,paint);
                }
            }

            paint.setColor(0xff000000);
            str = Symbol.getUnicode(n);
            center = acp+(ach-paint.measureText(str))*0.5f;
            canvas.drawText(str,x+center,y+baseline,paint);

            if(p1==0) canvas.drawLine(x,y+ach,x,y+(p-p1)*ach,paint);
            if(p1<p-2) canvas.drawLine(x+ach,y+ach,x+ach,y+(p-p1)*ach,paint);

            canvas.drawLine(spacing,y+ach,x+(p1<p-2? ach : 0.0f),y+ach,paint);
            if(p1==p-2) canvas.drawLine(spacing,y+ach+ach,x,y+ach+ach,paint);
            if(p1<p-2) {
                for(p2 = p1+1,y += ach; p2<p; ++p2,y += ach) {
                    n = h.aspect(p1,p2);
                    if(n!=-1) {
                        paint.setColor(aspectColors[n&0xffff]);
                        str = Symbol.getUnicode(n);
                        center = acp+(ach-paint.measureText(str))*0.5f;
                        canvas.drawText(str,x+center,y+baseline,paint);
                    }
                }
            }
        }
    }

    protected void drawDistributionTable(Canvas canvas,float x) {
        if(horoscope==null) return;

        paint.setTypeface(AstroActivity.sansFont);

    }
}

