package net.spirangle.sphinx.views;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.util.AttributeSet;

import net.spirangle.sphinx.activities.AstroActivity;
import net.spirangle.sphinx.astro.Horoscope;
import net.spirangle.sphinx.astro.Symbol;


public class HoroscopeView extends TableView {
    private static final String TAG = HoroscopeView.class.getSimpleName();

    protected static final int[] planetColors = {0xff0000ff,0xffffff00,0xffff99ff,0xffff6600,0xffffcc00,0xffff0000,0xffff9966,0xffffcc99,0xff9999ff,0xff99ccff,0xffff66ff};
    protected static final int[] elementColors = {0xffff9999,0xff99ff99,0xffffff99,0xff9999ff};
    protected static final int[] zodiacColors = {0xff990000,0xff006600,0xff999900,0xff000099,0xff990000,0xff006600,0xff999900,0xff000099,0xff990000,0xff006600,0xff999900,0xff000099};
    protected static final int[] aspectColors = {0xff00ff00,0xffcc0099,0xff00cc99,0xffcccc00,0xffcc0000,0xff99cc00,0xff00cc00,0xffcc9900,0xffff0000,0xff0000ff,0xffff0099,0xffff0099,0xffcc9900};
//	protected static final int[] orbitColors    = { 0xff000033,0xff333300,0xff330033,0xff330000,0xff330000,0xff330000,0xff330000,0xff330000,0xff000033,0xff000033,0xff330033 };

    protected static final int titleBackground = 0xfff0f0f0;
    protected static final int titleColor = 0xff666666;
    protected static final int textColor = 0xff000000;
    protected static final int cellColor = 0xff666666;
    protected static final int activeCellColor = 0xff999999;

    protected static final int textColorRetrograde = 0xff000099;

    protected static final float fontSizeHeader = 16f*density;
    protected static final float fontSizeSymbol = 22.0f*density;
    protected static final float fontSizeAspect = 24.0f*density;
    protected static final float fontSizeText = 16.25f*density;
    protected static final float fontSizeSmall = 10.0f*density;

    protected static final float spacing = 3.0f*density;
    protected static final float padding = 3.0f*density;
    protected static final float headerHeight = fontSizeHeader*1.5f;
    protected static final float cellHeight = fontSizeSymbol*1.5f;
    protected static final float bottomSpacing = 5.0f*density;

    protected static final float aspectCellPadding = 1.3f*density;
    protected static final float aspectCellHeight = aspectCellPadding*2+fontSizeAspect;

    protected Horoscope horoscope = null;

    private Symbol.SymbolListener symbolListener = null;

    public HoroscopeView(Context context) {
        super(context);
    }

    public HoroscopeView(Context context,AttributeSet attrs) {
        super(context,attrs);
    }

    public HoroscopeView(Context context,AttributeSet attrs,int defStyle) {
        super(context,attrs,defStyle);
    }

    public HoroscopeView(Context context,AttributeSet attrs,int defStyle,int defStyleRes) {
        super(context,attrs,defStyle,defStyleRes);
    }

    @Override
    public void onActivateCell(Cell rect) {
        if(rect.id!=-1l && symbolListener!=null)
            symbolListener.onSymbolSelected(rect.id);
    }

    public void setHoroscope(Horoscope h) {
//Log.d(APP,TAG+".setHoroscope("+h+")");
        horoscope = h;
        invalidate();
    }

    public void setSymbolListener(Symbol.SymbolListener sl) {
        symbolListener = sl;
    }

    protected void drawHeader(Canvas canvas,float[] columns,String... titles) {
        paint.setTypeface(AstroActivity.sansFont);
        paint.setTextSize(fontSizeHeader);
        paint.setColor(titleBackground);
        canvas.drawRect(0.0f,0.0f,width,headerHeight,paint);
        paint.setColor(titleColor);
        float baseline = (headerHeight-fontSizeHeader)*0.5f-paint.ascent();
        for(int i=0; i<titles.length && i<columns.length; ++i)
            canvas.drawText(titles[i],columns[i],baseline,paint);
    }

    protected void drawCell(Canvas canvas,Cell r) {
        if(r.isActive()) {
            paint.setColor(activeCellColor);
            canvas.drawRect(r,paint);
        } else {
            paint.setStyle(Paint.Style.STROKE);
            paint.setColor(cellColor);
            canvas.drawRect(r,paint);
            paint.setStyle(Paint.Style.FILL);
        }
    }

    protected void drawCellText(Canvas canvas,Cell cell,String str,float x,float fontSize) {
        drawCellText(canvas,cell,str,x,fontSize,fontSize);
    }

    protected void drawCellText(Canvas canvas,Cell cell,String str,float x,float fontSize,float baseline) {
        paint.setTextSize(fontSize);
        baseline = (cellHeight-baseline)*0.5f-paint.ascent();
        canvas.drawText(str,cell.left+x,cell.top+baseline,paint);
    }

    protected void drawCellTextLeft(Canvas canvas,Cell cell,String str,float x,float fontSize) {
        drawCellTextLeft(canvas,cell,str,x,fontSize,fontSize);
    }

    protected void drawCellTextLeft(Canvas canvas,Cell cell,String str,float x,float fontSize,float baseline) {
        paint.setTextSize(fontSize);
        baseline = (cellHeight-baseline)*0.5f-paint.ascent();
        canvas.drawText(str,cell.left+x-paint.measureText(str),cell.top+baseline,paint);
    }

    protected void drawCellTextCenter(Canvas canvas,Cell cell,String str,float x,float fontSize) {
        drawCellTextCenter(canvas,cell,str,x,fontSize,fontSize);
    }

    protected void drawCellTextCenter(Canvas canvas,Cell cell,String str,float x,float fontSize,float baseline) {
        paint.setTextSize(fontSize);
        baseline = (cellHeight-baseline)*0.5f-paint.ascent();
        canvas.drawText(str,cell.left+x-paint.measureText(str)*0.5f,cell.top+baseline,paint);
    }
}

