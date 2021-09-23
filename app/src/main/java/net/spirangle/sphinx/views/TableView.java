package net.spirangle.sphinx.views;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Paint;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ScrollView;


public abstract class TableView extends View implements GestureDetector.OnGestureListener {
    private static final String TAG = TableView.class.getSimpleName();

    protected static final float density = Resources.getSystem().getDisplayMetrics().density;

    public class Cell extends RectF {
        public int index = 0;
        public long id = -1L;

        public void set(int n,long i,float l,float t,float r,float b) {
            index = n;
            id = i;
            set(l,t,r,b);
        }

        public boolean isActive() {
            return activeCell==this;
        }

        public void activate() {
            activateCell(this);
        }

        public void deactivate() {
            if(activeCell==this)
                deactivateCell();
        }
    }

    protected RectF viewport;
    protected Paint paint;
    protected GestureDetector gestureDetector;
    private Cell[] cellMap = null;
    private int cellIndex = 0;
    private Cell activeCell = null;
    private ScrollView scroll = null;
    protected float width;
    protected float height;

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
        width = 0.0f;
        height = 0.0f;
    }

    @Override
    public void onSizeChanged(int w,int h,int oldw,int oldh) {
//Log.d(APP,TAG+".onSizeChanged(w: "+w+", h: "+h+")");
        super.onSizeChanged(w,h,oldw,oldh);
        viewport.set(0.0f,0.0f,(float)w,(float)h);
        width = viewport.right-viewport.left;
        height = viewport.bottom-viewport.top;
    }

    @Override
    public boolean onTouchEvent(MotionEvent e) {
        if(scroll!=null) scroll.requestDisallowInterceptTouchEvent(true);
        if(gestureDetector.onTouchEvent(e)) return true;
        int a = e.getAction();
        if((a&MotionEvent.ACTION_UP)!=0) {
            Cell r = activeCell;
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
        Cell r = getCell(e.getX(),e.getY());
        if(r!=null) r.activate();
        return true;
    }

    @Override
    public boolean onFling(MotionEvent e1,MotionEvent e2,float velX,float velY) {
        Cell r = activeCell;
        if(r!=null) r.deactivate();
        r = getCell(e2.getX(),e2.getY());
        if(r!=null) r.activate();
        return false;
    }

    @Override
    public void onLongPress(MotionEvent e) {
        Cell r = activeCell;
        if(r!=null) r.deactivate();
    }

    @Override
    public boolean onScroll(MotionEvent e1,MotionEvent e2,float distX,float distY) {
        Cell r = activeCell;
        if(r!=null) r.deactivate();
        r = getCell(e2.getX(),e2.getY());
        if(r!=null) r.activate();
        return false;
    }

    @Override
    public void onShowPress(MotionEvent e) {
    }

    @Override
    public boolean onSingleTapUp(MotionEvent e) {
        Cell r = activeCell;
        if(r!=null) {
            onActivateCell(r);
            r.deactivate();
            return true;
        }
        return false;
    }

    public void onActivateCell(Cell rect) {}

    public void createCellMap(int length) {
        if(length<=0) cellMap = null;
        else cellMap = new Cell[length];
    }

    public void clearCellMap(int offset,int length) {
        int n = offset+length;
        if(cellMap==null || cellMap.length<n) cellMap = new Cell[n];
        for(int i = offset; i<n; ++i) {
            if(cellMap[i]==null) cellMap[i] = new Cell();
            else cellMap[i].set(0,-1l,0.0f,0.0f,0.0f,0.0f);
        }
        cellIndex = -1;
    }

    public Cell[] getCellMap() {
        return cellMap;
    }

    public Cell getCell(float x,float y) {
        if(cellMap!=null)
            for(int i = 0; i<cellMap.length; ++i) {
                if(cellMap[i]!=null && cellMap[i].contains(x,y)) {
                    return cellMap[i];
                }
            }
        return null;
    }

    public Cell getCell(int index) {
        return cellMap!=null? cellMap[index] : null;
    }

    public Cell getNextCell() {
        return cellMap!=null && cellIndex+1<cellMap.length? cellMap[++cellIndex] : null;
    }

    public int getCellIndex() {
        return cellIndex;
    }

    public void activateCell(Cell cell) {
        activeCell = cell;
//        invalidate((int)active.left,(int)active.top,(int)active.right,(int)active.bottom);
    }

    public void deactivateCell() {
//        invalidate((int)active.left,(int)active.top,(int)active.right,(int)active.bottom);
        activeCell = null;
    }

    public void setScroll(ScrollView sv) {
        scroll = sv;
    }
}

