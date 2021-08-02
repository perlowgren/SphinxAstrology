package net.spirangle.sphinx;

import android.graphics.*;
import android.text.style.LineBackgroundSpan;
import android.text.style.LineHeightSpan;


public class CustomHRSpan implements LineBackgroundSpan, LineHeightSpan {
    private final int color;
    private final float height;
    private final float line;
    private final float marginLeft;
    private final float marginRight;
    private Path path = null;
    private PathEffect dash = null;

    public CustomHRSpan(int color,float height,float line,boolean dashed) {
        this.color = color;
        this.height = height;
        this.line = line;
        marginLeft = 5.0f;
        marginRight = 5.0f;
        if(dashed) {
            path = new Path();
            dash = new DashPathEffect(new float[] {10.0f,6.0f},0.0f);
        }
    }

    @Override
    public void drawBackground(Canvas c,Paint p,int left,int right,int top,int baseline,int bottom,
                               CharSequence text,int start,int end,int lnum) {
        Paint.Style paintStyle = p.getStyle();
        int paintFlags = p.getFlags();
        int paintColor = p.getColor();
        float x = (float)left+marginLeft;
        float y = (float)(top+(bottom-top)/2);
        float w = (float)(right-left)-marginRight;
        p.setColor(color);
        p.setAntiAlias(false);
        if(path!=null) {
            PathEffect paintPath = p.getPathEffect();
//Log.d("sphinx","CustomHRSpan.drawBackground(left: "+left+", top: "+top+", right: "+right+", bottom: "+bottom+", y: "+y+", width: "+c.getWidth()+")");
            p.setStyle(Paint.Style.STROKE);
            p.setStrokeWidth(line);
            p.setPathEffect(dash);
            path.moveTo(x,y);
            path.lineTo(w,y);
            c.drawPath(path,p);
            p.setPathEffect(paintPath);
        } else {
            p.setStyle(Paint.Style.FILL);
            c.drawRect(new RectF(x,y-line*0.5f,w,y+line),p);
        }
        p.setStyle(paintStyle);
        p.setFlags(paintFlags);
        p.setColor(paintColor);
    }

    @Override
    public void chooseHeight(CharSequence text,int start,int end,int spanstartv,int v,Paint.FontMetricsInt fm) {
        fm.descent = (int)height/2;
        fm.ascent = (int)height-fm.descent;
        fm.leading = 0;
        fm.top = fm.ascent;
        fm.bottom = fm.descent;
    }
}

