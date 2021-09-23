package net.spirangle.sphinx.views;

import static net.spirangle.sphinx.config.AstrologyProperties.*;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.util.AttributeSet;

import net.spirangle.sphinx.R;
import net.spirangle.sphinx.activities.AstroActivity;
import net.spirangle.sphinx.astro.Coordinate;
import net.spirangle.sphinx.astro.Horoscope;
import net.spirangle.sphinx.astro.Symbol;


public class PlanetsView extends HoroscopeView {
    private static final String TAG = PlanetsView.class.getSimpleName();

    private static final int[] houses = {0,1,2,9,10,11};
    private static final int[] houseIds = {ASTRO_ASCENDANT,ASTRO_2ND_HOUSE,ASTRO_3RD_HOUSE,ASTRO_MC,ASTRO_11TH_HOUSE,ASTRO_12TH_HOUSE};

    private static final int columns = 3;

    public PlanetsView(Context context) {
        super(context);
    }

    public PlanetsView(Context context,AttributeSet attrs) {
        super(context,attrs);
    }

    public PlanetsView(Context context,AttributeSet attrs,int defStyle) {
        super(context,attrs,defStyle);
    }

    public PlanetsView(Context context,AttributeSet attrs,int defStyle,int defStyleRes) {
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
                for(int i = p-1, n; i>=0; --i) {
                    n = horoscope.planetId(i);
                    if(n==ASTRO_ASCENDANT || n==ASTRO_MC) --p;
                }
                p = p/(columns-1)+((p%(columns-1))==0? 0 : 1);
                if(p<houses.length) p = houses.length;
                h += (int)((float)p*(cellHeight+spacing));
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

        paint.setStyle(Paint.Style.FILL);
        paint.setAntiAlias(true);

        Context context = getContext();
        final float cellWidth = (width-spacing*(float)(columns+1))/(float)columns;
        final float[] headerPositions = { spacing,spacing+cellWidth+spacing+cellWidth+spacing };
        drawHeader(canvas,headerPositions,
                   context.getString(R.string.table_planets),
                   context.getString(R.string.table_houses));

        clearCellMap(0,horoscope.planets()+houses.length);

        long t1 = System.currentTimeMillis();
        drawPlanetList(canvas,cellWidth);
        long t2 = System.currentTimeMillis();
        drawHouseList(canvas,cellWidth);
        long t3 = System.currentTimeMillis();
//Log.d(APP,TAG+".onDraw("+(t2-t1)+")");
    }

    protected void drawPlanetList(Canvas canvas,float cellWidth) {
        if(horoscope==null) return;
        final Horoscope h = horoscope;
        final int p = h.planets();

        paint.setTypeface(AstroActivity.symbolFont);

        float x = spacing;
        float y = headerHeight+spacing;
        for(int i = 0; i<p; ++i) {
            int n = h.planetId(i);
            long id = h.planetSymbolId(i);
            if(n==ASTRO_ASCENDANT || n==ASTRO_MC || id==-1L) continue;
            Cell r = getNextCell();
            r.set(getCellIndex(),id,x,y,x+cellWidth,y+cellHeight);

            drawCell(canvas,r);

            paint.setColor(textColor);
            drawCellTextCenter(canvas,r,Symbol.getUnicode(n),padding+fontSizeSymbol*0.5f,fontSizeSymbol);

            paint.setTextSize(fontSizeText);
            String str = Coordinate.formatHM(h.planetLongitude(i),'°',30,"");
            int j = str.indexOf('°');
            String str2 = str.substring(0,j+1);
            float x2 = cellWidth*0.42f-paint.measureText(str2);
            drawCellText(canvas,r,str,x2,fontSizeText);
            if(h.planetIsRetrograde(i)) {
                x2 += paint.measureText(str);
                paint.setColor(textColorRetrograde);
                drawCellText(canvas,r,"R",x2,fontSizeSmall,fontSizeText);
                paint.setColor(textColor);
            }

            int sign = h.planetSign(i);
            paint.setColor(zodiacColors[sign&0xf]);
            drawCellTextCenter(canvas,r,Symbol.getUnicode(sign),cellWidth*0.72f,fontSizeSymbol);
            paint.setColor(textColor);

            String str3 = String.valueOf(1+h.planetHouse(i)-ASTRO_ASCENDANT);
            drawCellTextLeft(canvas,r,str3,cellWidth-padding,fontSizeText);

            y += cellHeight+spacing;
            if(y+cellHeight>height) {
                x += cellWidth+spacing;
                y = headerHeight+spacing;
            }
        }
    }

    protected void drawHouseList(Canvas canvas,float cellWidth) {
        if(horoscope==null) return;
        final Horoscope h = horoscope;

        paint.setTypeface(AstroActivity.symbolFont);

        float x = width-spacing-cellWidth;
        float y = headerHeight+spacing;
        for(int i = 0; i<houses.length; ++i) {
            int n = houseIds[i];
            long id = h.houseSymbolId(houses[i]);
            if(id==-1L) continue;
            Cell r = getNextCell();
            r.set(getCellIndex(),id,x,y,x+cellWidth,y+cellHeight);

            drawCell(canvas,r);

            paint.setColor(textColor);
            if(n==ASTRO_ASCENDANT || n==ASTRO_MC) {
                drawCellTextCenter(canvas,r,Symbol.getUnicode(n),padding+fontSizeSymbol*0.5f,fontSizeSymbol);
            } else {
                drawCellText(canvas,r,Integer.toString(Symbol.Attribute.valueOf(n)),padding,fontSizeSymbol);
            }

            paint.setTextSize(fontSizeText);
            String str = Coordinate.formatHM(h.houseCusp(i),'°',30,"");
            int j = str.indexOf('°');
            String str2 = str.substring(0,j+1);
            float x2 = cellWidth*0.42f-paint.measureText(str2);
            drawCellText(canvas,r,str,x2,fontSizeText);

            int sign = h.houseSign(houses[i]);
            paint.setColor(zodiacColors[sign&0xf]);
            drawCellTextCenter(canvas,r,Symbol.getUnicode(sign),cellWidth*0.72f,fontSizeSymbol);
            paint.setColor(textColor);

            y += cellHeight+spacing;
        }
    }
}

