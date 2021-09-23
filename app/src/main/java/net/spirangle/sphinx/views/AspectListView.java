package net.spirangle.sphinx.views;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.util.AttributeSet;

import net.spirangle.sphinx.R;
import net.spirangle.sphinx.activities.AstroActivity;
import net.spirangle.sphinx.astro.Coordinate;
import net.spirangle.sphinx.astro.Horoscope;
import net.spirangle.sphinx.astro.Symbol;


public class AspectListView extends HoroscopeView {
    private static final String TAG = AspectListView.class.getSimpleName();

    private static final int columns = 3;

    private int[] index;

    public AspectListView(Context context) {
        super(context);
    }

    public AspectListView(Context context,AttributeSet attrs) {
        super(context,attrs);
    }

    public AspectListView(Context context,AttributeSet attrs,int defStyle) {
        super(context,attrs,defStyle);
    }

    public AspectListView(Context context,AttributeSet attrs,int defStyle,int defStyleRes) {
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
                int a = horoscope.aspects();
                a = a/columns+((a%columns)==0? 0 : 1);
                h += (int)((float)a*(cellHeight+spacing));
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
        final float cellWidth = (width-spacing*(float)(columns+1))/(float)columns;
        final float[] headerPositions = { spacing };
        drawHeader(canvas,headerPositions,
                   context.getString(R.string.table_aspects));

        clearCellMap(0,index.length);

        long t1 = System.currentTimeMillis();
        drawAspectList(canvas,cellWidth);
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

    protected void drawAspectList(Canvas canvas,float cellWidth) {
        if(horoscope==null) return;
        Cell r;
        Horoscope h = horoscope;
        String str, str2;
        int i, j, n;
        long id;
        float x, y, x2;
        double orb;

        paint.setTypeface(AstroActivity.symbolFont);

        x = spacing;
        y = headerHeight+spacing;
        for(i = 0; i<index.length; ++i) {
            n = index[i];
            id = h.aspectSymbolId(n);
            r = getNextCell();
            r.set(getCellIndex(),id,x,y,x+cellWidth,y+cellHeight);

            drawCell(canvas,r);

            paint.setColor(textColor);
            str = Symbol.getUnicode(h.planetId(h.aspectPlanet1(n)));
            drawCellTextCenter(canvas,r,str,cellWidth*0.15f,fontSizeSymbol);

            paint.setColor(aspectColors[h.aspect(n)&0xffff]);
            str = Symbol.getUnicode(h.aspect(n));
            drawCellTextCenter(canvas,r,str,cellWidth*0.35f,fontSizeSymbol);

            paint.setColor(textColor);
            str = Symbol.getUnicode(h.planetId(h.aspectPlanet2(n)));
            drawCellTextCenter(canvas,r,str,cellWidth*0.55f,fontSizeSymbol);

            paint.setTextSize(fontSizeText);
            orb = h.aspectOrb(n);
            if(orb<0.0d) orb = -orb;
            str = Coordinate.formatHM(orb,'°',360,"");
            j = str.indexOf('°');
            str2 = str.substring(0,j+1);
            x2 = cellWidth*0.82f-paint.measureText(str2);
            drawCellText(canvas,r,str,x2,fontSizeText);

            y += cellHeight+spacing;
            if(y+cellHeight+spacing>height) {
                x += cellWidth+spacing;
                y = headerHeight+spacing;
            }
        }
    }
}

