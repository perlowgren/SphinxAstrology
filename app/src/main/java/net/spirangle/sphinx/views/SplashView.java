package net.spirangle.sphinx.views;

import static net.spirangle.sphinx.config.SphinxProperties.APP;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;

import net.spirangle.sphinx.R;


public class SplashView extends View {
    private static final String TAG = SplashView.class.getSimpleName();

    private Paint paint = new Paint();
    private String spirangleStudio = null;
    private String presents = null;
    private String appName = null;
    private String labelFormat = null;
    private String label = null;
    private float progress = 0.0f;

    public SplashView(Context context) {
        super(context);
        init(context);
    }

    public SplashView(Context context,AttributeSet attrs) {
        super(context,attrs);
        init(context);
    }

    public SplashView(Context context,AttributeSet attrs,int defStyle) {
        super(context,attrs,defStyle);
        init(context);
    }

/*	public SplashView(Context context,AttributeSet attrs,int defStyle,int defStyleRes) {
		super(context,attrs,defStyle,defStyleRes);
		init(context);
	}*/

    private void init(Context context) {
        spirangleStudio = context.getString(R.string.spirangle_studio);
        presents = context.getString(R.string.presents);
        appName = context.getString(R.string.title_app);
        labelFormat = context.getString(R.string.installing);
    }

    @Override
    public void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        Context context = getContext();
        float x,y;
        float w = (float)getWidth();
        float h = (float)getHeight();
        float s = Math.min(w,h);
        float textSize1 = s*0.04f;
        float textSize2 = s*0.07f;
        float textSize3 = s*0.03f;
        String str;
        Log.d(APP,TAG+".onDraw(w="+w+",h="+h+")");
        paint.setAntiAlias(true);
        paint.setColor(0xffff00ff);
        paint.setStyle(Paint.Style.STROKE);
        canvas.drawRect(1.0f,1.0f,w-2.0f,h-2.0f,paint);
        paint.setColor(0xffffcc99);
        paint.setStyle(Paint.Style.FILL);
        paint.setTextSize(textSize1);
        x = w*0.5f;
        y = h*0.75f-s*0.04f-textSize1;
        str = spirangleStudio;
        canvas.drawText(str,x-paint.measureText(str)*0.5f,y,paint);
        y = h*0.75f;
        str = presents;
        canvas.drawText(str,x-paint.measureText(str)*0.5f,y,paint);
        paint.setTextSize(textSize2);
        y = h*0.75f+s*0.04f+textSize2;
        str = appName;
        canvas.drawText(str,x-paint.measureText(str)*0.5f,y,paint);
        if(progress>0.0f && progress<1.0f && label!=null) {
            paint.setTextSize(textSize3);
            canvas.drawText(label,s*0.01f,h-s*0.025f,paint);
            canvas.drawRect(0.0f,h-s*0.015f,w*progress,h,paint);
        }
    }

    public void setProgress(float progress,String label) {
        if(label!=null) this.label = String.format(labelFormat,label);
        this.progress = progress;
        invalidate();
    }
}

