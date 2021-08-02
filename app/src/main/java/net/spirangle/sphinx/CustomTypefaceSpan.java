package net.spirangle.sphinx;

import android.graphics.Typeface;
import android.text.TextPaint;
import android.text.style.MetricAffectingSpan;


public class CustomTypefaceSpan extends MetricAffectingSpan {
    private final Typeface typeface;
    private final int color;
    private final float size;

    public CustomTypefaceSpan(final Typeface typeface,final int color,final float size) {
        this.typeface = typeface;
        this.color = color;
        this.size = size;
    }

    @Override
    public void updateDrawState(final TextPaint paint) {
        apply(paint);
    }

    @Override
    public void updateMeasureState(final TextPaint paint) {
        apply(paint);
    }

    private void apply(TextPaint paint) {
        paint.setTypeface(typeface);
        if(color!=0)
            paint.setColor(color);
        if(size>0.0f)
            paint.setTextSize(size);
    }
}

