package net.spirangle.sphinx.astro;

import android.content.Context;
import android.util.AttributeSet;

import net.spirangle.sphinx.Horoscope;
import net.spirangle.sphinx.Symbol;
import net.spirangle.sphinx.TableView;


public class HoroscopeView extends TableView {
    private static final String TAG = "HoroscopeView";

    protected static final int[] planetColors = {0xff0000ff,0xffffff00,0xffff99ff,0xffff6600,0xffffcc00,0xffff0000,0xffff9966,0xffffcc99,0xff9999ff,0xff99ccff,0xffff66ff};
    protected static final int[] elementColors = {0xffff9999,0xff99ff99,0xffffff99,0xff9999ff};
    protected static final int[] zodiacColors = {0xff990000,0xff006600,0xff999900,0xff000099,0xff990000,0xff006600,0xff999900,0xff000099,0xff990000,0xff006600,0xff999900,0xff000099};
    protected static final int[] aspectColors = {0xff00ff00,0xffcc0099,0xff00cc99,0xffcccc00,0xffcc0000,0xff99cc00,0xff00cc00,0xffcc9900,0xffff0000,0xff0000ff,0xffff0099,0xffff0099,0xffcc9900};
//	protected static final int[] orbitColors    = { 0xff000033,0xff333300,0xff330033,0xff330000,0xff330000,0xff330000,0xff330000,0xff330000,0xff000033,0xff000033,0xff330033 };

    protected static final int titleBackground = 0xfff0f0f0;
    protected static final int titleColor = 0xff666666;
    protected static final int textColor = 0xff000000;
    protected static final int boxColor = 0xffcccccc;
    protected static final int activeBoxColor = 0xffcccccc;

    protected static final float TFS = 20.0f;
    protected static final float PFS = 30.0f;
    protected static final float AFS = 36.5f;
    protected static final float DFS = 26.0f;
    protected static final float RFS = 16.0f;

    protected Horoscope horoscope = null;

    private Symbol.SymbolListener symbolListener = null;

    public HoroscopeView(Context context) { super(context); }

    public HoroscopeView(Context context,AttributeSet attrs) { super(context,attrs); }

    public HoroscopeView(Context context,AttributeSet attrs,int defStyle) { super(context,attrs,defStyle); }

    public HoroscopeView(Context context,AttributeSet attrs,int defStyle,int defStyleRes) { super(context,attrs,defStyle,defStyleRes); }

    @Override
    public void onActivateMapRect(MapRectF rect) {
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
}

