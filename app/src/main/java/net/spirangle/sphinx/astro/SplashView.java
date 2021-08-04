package net.spirangle.sphinx.astro;

import static net.spirangle.sphinx.config.SphinxProperties.APP;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;

import net.spirangle.sphinx.R;


public class SplashView extends View {
    private static final String TAG = "SplashView";

    private Paint paint = new Paint();
    private String spiranglePresents = null;
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
        spiranglePresents = context.getString(R.string.spirangle_presents);
        appName = context.getString(R.string.title_app);
        labelFormat = context.getString(R.string.installing);
    }

    @Override
    public void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        Context context = getContext();
        float x, y, w = (float)getWidth(), h = (float)getHeight();
        String str;
        Log.d(APP,TAG+".onDraw()");
        paint.setStyle(Paint.Style.FILL);
        paint.setAntiAlias(true);
        paint.setColor(0xffffcc99);
        paint.setTextSize(16);
        x = w*0.5f;
        y = h*0.5f+180.0f;
        str = spiranglePresents;
        canvas.drawText(str,x-paint.measureText(str)*0.5f,y,paint);
        paint.setTextSize(30);
        y += 40.0f;
        str = appName;
        canvas.drawText(str,x-paint.measureText(str)*0.5f,y,paint);
        if(progress>0.0f && progress<1.0f && label!=null) {
            paint.setTextSize(16);
            canvas.drawText(label,5.0f,h-10.0f,paint);
            canvas.drawRect(0.0f,h-5.0f,w*progress,h,paint);
        }
    }

    public void setProgress(float p,String l) {
        if(l!=null)
            label = String.format(labelFormat,l);
        progress = p;
        invalidate();
    }
}

