package net.spirangle.sphinx.widgets;

import static net.spirangle.sphinx.config.SphinxProperties.APP;

import android.util.Log;


public class WheelGraphPlanet {
    private static final String TAG = WheelGraphPlanet.class.getSimpleName();

    public static final float PI = (float)(Math.PI);
    public static final float PI2 = (float)(Math.PI*2.0);

    public int index;
    public float longitude;
    public float angle;
    public float x;
    public float y;
    private WheelGraphPlanet left;
    private WheelGraphPlanet right;

    public static void organize(WheelGraphPlanet[] pa,float xc,float yc,float r,float sz,float a) {
        int i, n;
        boolean l = true;
        sort(pa);
        for(n = 0; n<10 && l; ++n)
            for(i = 0,l = false; i<pa.length; ++i)
                if(pa[i].makeRoom(xc,yc,r,sz,a)) l = true;
        Log.d(APP,TAG+".organize(n: "+n+")");
    }

    public static void sort(WheelGraphPlanet[] pa) {
        int i, j, p = pa.length;
        float o1, o2;
        WheelGraphPlanet p1;
        for(i = 0; i<p-2; ++i) {
            o1 = pa[i].orb(pa[i+1]);
            for(j = i+2; j<p; ++j) {
                o2 = pa[i].orb(pa[j]);
                if((o1<0.0f && ((o2<0.0f && o1>o2) || o2>0.0f)) || (o1>0.0f && o2>0.0f && o1>o2)) {
                    p1 = pa[i+1];
                    pa[i+1] = pa[j];
                    pa[j] = p1;
                    o1 = o2;
                }
            }
        }
        pa[0].left = pa[p-1];
        pa[p-1].right = pa[0];
        for(i = 0; i<p-1; ++i) {
            pa[i].right = pa[i+1];
            pa[i+1].left = pa[i];
        }
        for(i = 0; i<p; ++i)
            Log.d(APP,TAG+".sort(i: "+i+", index: "+pa[i].index+", left: "+pa[i].left.index+", right: "+pa[i].right.index+")");
    }

    public WheelGraphPlanet(int i,float l,float xc,float yc,float r) {
        index = i;
        longitude = l;
        angle = l;
        left = null;
        right = null;
        position(xc,yc,r);
    }

    public void position(float xc,float yc,float r) {
        x = xc-(float)Math.cos(-(double)angle)*r;
        y = yc-(float)Math.sin(-(double)angle)*r;
    }

    public boolean makeRoom(float xc,float yc,float r,float sz,float a) {
        WheelGraphPlanet pl = null, pr = null;
        if(overlaps(left,sz)) {
            if(overlaps(right,sz)) {
                pl = left;
                pr = right;
            } else {
                pl = left;
                pr = this;
            }
        } else if(overlaps(right,sz)) {
            if(overlaps(left,sz)) {
                pl = left;
                pr = right;
            } else {
                pl = this;
                pr = right;
            }
        } else return false;
        if(pl!=null) pl.moveLeft(xc,yc,r,sz,a);
        if(pr!=null) pr.moveRight(xc,yc,r,sz,a);
        return true;
    }

    public void moveLeft(float xc,float yc,float r,float sz,float a) {
//Log.d(APP,TAG+".moveLeft("+index+")");
        if(overlaps(left,sz))
            left.moveLeft(xc,yc,r,sz,a);
        angle -= a;
        position(xc,yc,r);
    }

    public void moveRight(float xc,float yc,float r,float sz,float a) {
//Log.d(APP,TAG+".moveRight("+index+")");
        if(overlaps(right,sz))
            right.moveRight(xc,yc,r,sz,a);
        angle += a;
        position(xc,yc,r);
    }

    public boolean overlaps(WheelGraphPlanet p,float sz) {
        float d = (float)Math.hypot((double)(x-p.x),(double)(y-p.y));
        return d<sz;
    }

    public float orb(WheelGraphPlanet p) {
        float a1 = angle, a2 = p.angle;
        if(a2-a1>PI) a1 += PI2;
        else if(a1-a2>PI) a2 += PI2;
        return a2-a1;
    }
}
