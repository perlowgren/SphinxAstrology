package net.spirangle.sphinx;

import android.graphics.Paint;
import android.text.Spanned;
import android.text.style.LineHeightSpan;


public class CustomParagraphMarginSpan implements LineHeightSpan {
    private int marginTop;
    private int marginBottom;

    public CustomParagraphMarginSpan(int top,int bottom) {
        marginTop = top;
        marginBottom = bottom;
    }

    @Override
    public void chooseHeight(CharSequence text,int start,int end,int spanstartv,int v,Paint.FontMetricsInt fm) {
        Spanned spanned = (Spanned)text;
        int st = spanned.getSpanStart(this);
        int en = spanned.getSpanEnd(this);
        if(start==st) {
            fm.ascent -= marginTop;
            fm.top -= marginTop;
        }
        if(end==en) {
            fm.descent += marginBottom;
            fm.bottom += marginBottom;
        }
    }
}

