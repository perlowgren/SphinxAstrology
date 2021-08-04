package net.spirangle.sphinx.views;

import android.content.Context;
import android.graphics.Paint;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ScrollView;


public abstract class TableView extends View implements GestureDetector.OnGestureListener {
    private static final String TAG = "TableView";

    public class MapRectF extends RectF {
        public int index = 0;
        public long id = -1l;

        public void set(int n,long i,float l,float t,float r,float b) {
            index = n;
            id = i;
            set(l,t,r,b);
        }

        public boolean isActive() {
            return active==this;
        }

        public void activate() {
            active = this;
//			invalidate((int)left,(int)top,(int)right,(int)bottom);
        }

        public void deactivate() {
            active = null;
//			invalidate((int)left,(int)top,(int)right,(int)bottom);
        }
    }

    protected RectF viewport;
    protected Paint paint;
    protected GestureDetector gestureDetector;
    protected MapRectF[] map = null;
    protected MapRectF active = null;
    private ScrollView scroll = null;

    public TableView(Context context) {
        super(context);
        onCreate(context,null,0,0);
    }

    public TableView(Context context,AttributeSet attrs) {
        super(context,attrs);
        onCreate(context,attrs,0,0);
    }

    public TableView(Context context,AttributeSet attrs,int defStyle) {
        super(context,attrs,defStyle);
        onCreate(context,attrs,defStyle,0);
    }

    public TableView(Context context,AttributeSet attrs,int defStyle,int defStyleRes) {
        super(context,attrs,defStyle,defStyleRes);
        onCreate(context,attrs,defStyle,defStyleRes);
    }

    protected void onCreate(Context context,AttributeSet attrs,int defStyle,int defStyleRes) {
//Log.d(APP,TAG+".onCreate(1)");
//		setBackgroundColor(Color.WHITE);
//Log.d(APP,TAG+".onCreate(2)");
        viewport = new RectF();
//Log.d(APP,TAG+".onCreate(3)");
        paint = new Paint();
        gestureDetector = new GestureDetector(context,this);
    }

    @Override
    public void onSizeChanged(int w,int h,int oldw,int oldh) {
//Log.d(APP,TAG+".onSizeChanged(w: "+w+", h: "+h+")");
        super.onSizeChanged(w,h,oldw,oldh);
        viewport.set(0.0f,0.0f,(float)w,(float)h);
    }

    @Override
    public boolean onTouchEvent(MotionEvent e) {
        if(scroll!=null) scroll.requestDisallowInterceptTouchEvent(true);
        if(gestureDetector.onTouchEvent(e)) return true;
        int a = e.getAction();
        if((a&MotionEvent.ACTION_UP)!=0) {
            MapRectF r = active;
            if(r!=null) {
//				onActivateMapRect(r);
                r.deactivate();
                return true;
            }
        }
        return super.onTouchEvent(e);
    }

    @Override
    public boolean onDown(MotionEvent e) {
        MapRectF r = getMapRect(e.getX(),e.getY());
        if(r!=null) r.activate();
        return true;
    }

    @Override
    public boolean onFling(MotionEvent e1,MotionEvent e2,float velX,float velY) {
        MapRectF r = active;
        if(r!=null) r.deactivate();
        r = getMapRect(e2.getX(),e2.getY());
        if(r!=null) r.activate();
        return false;
    }

    @Override
    public void onLongPress(MotionEvent e) {
        MapRectF r = active;
        if(r!=null) r.deactivate();
    }

    @Override
    public boolean onScroll(MotionEvent e1,MotionEvent e2,float distX,float distY) {
        MapRectF r = active;
        if(r!=null) r.deactivate();
        r = getMapRect(e2.getX(),e2.getY());
        if(r!=null) r.activate();
        return false;
    }

    @Override
    public void onShowPress(MotionEvent e) {
    }

    @Override
    public boolean onSingleTapUp(MotionEvent e) {
        MapRectF r = active;
        if(r!=null) {
            onActivateMapRect(r);
            r.deactivate();
            return true;
        }
        return false;
    }

    public void onActivateMapRect(MapRectF rect) {}

    protected void clearMap(int o,int l) {
        int n = o+l;
        if(map==null || map.length<n) map = new MapRectF[n];
        for(int i = o; i<n; ++i)
            if(map[i]==null) map[i] = new MapRectF();
            else map[i].set(0,-1l,0.0f,0.0f,0.0f,0.0f);
    }

    public MapRectF getMapRect(float x,float y) {
        if(map!=null)
            for(int i = 0; i<map.length; ++i) {
                if(map[i]!=null && map[i].contains(x,y)) {
                    return map[i];
                }
            }
        return null;
    }

    public void setScroll(ScrollView sv) {
        scroll = sv;
    }
}

