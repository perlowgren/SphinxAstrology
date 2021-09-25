package net.spirangle.sphinx.views;

import static net.spirangle.sphinx.config.SphinxProperties.APP;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.util.Log;

import net.spirangle.sphinx.R;
import net.spirangle.sphinx.activities.AstroActivity;
import net.spirangle.sphinx.astro.Horoscope;
import net.spirangle.sphinx.astro.Symbol;


public class AspectPatternsView extends HoroscopeView {
    private static final String TAG = AspectPatternsView.class.getSimpleName();

    public AspectPatternsView(Context context) {
        super(context);
    }

    public AspectPatternsView(Context context,AttributeSet attrs) {
        super(context,attrs);
    }

    public AspectPatternsView(Context context,AttributeSet attrs,int defStyle) {
        super(context,attrs,defStyle);
    }

    public AspectPatternsView(Context context,AttributeSet attrs,int defStyle,int defStyleRes) {
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
                int a = horoscope.aspectPatterns();
                h += (int)((float)a*(cellHeight+spacing));
            }
            if(hm==MeasureSpec.AT_MOST) h = Math.min(hs,h);
        }
        setMeasuredDimension(w,h);
    }

    @Override
    public void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        if(horoscope==null) return;

        paint.setStyle(Paint.Style.FILL);
        paint.setAntiAlias(true);

        Context context = getContext();
        final float[] headerPositions = { spacing };
        drawHeader(canvas,headerPositions,
                   context.getString(R.string.table_aspect_patterns));

        clearCellMap(0,horoscope.aspectPatterns());

        long t1 = System.currentTimeMillis();
        drawAspectPatternsTable(canvas);
        long t2 = System.currentTimeMillis();
        Log.d(APP,TAG+".onDraw: Time for drawing aspect patterns list: "+(t2-t1)+"ms");
    }

    protected void drawAspectPatternsTable(Canvas canvas) {
        if(horoscope==null) return;
        Horoscope h = horoscope;
        final int p = h.aspectPatterns();
        final float cellWidth = width-spacing*2;

        paint.setTypeface(AstroActivity.symbolFont);

        float x = spacing;
        float y = headerHeight+spacing;
        for(int i = 0; i<p; ++i) {
            int n = h.aspectPattern(i);
            long id = h.aspectPatternSymbolId(i);
            Cell r = getNextCell();
            r.set(getCellIndex(),id,x,y,x+cellWidth,y+cellHeight);

            drawCell(canvas,r);

            drawCellText(canvas,r,Symbol.astrologyAspectPatternName(n),padding,fontSizeText);

            int[] app = h.aspectPatternPlanets(i);
            StringBuilder sb = new StringBuilder();
            for(int j = 0; j<app.length; ++j)
                sb.append(Symbol.getUnicode(app[j]));
            drawCellText(canvas,r,sb.toString(),cellWidth*0.4f,fontSizeSymbol);

            y += cellHeight+spacing;
        }
    }
}

