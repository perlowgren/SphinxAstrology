package net.spirangle.sphinx.db;

import static net.spirangle.sphinx.config.SphinxProperties.APP;

import android.util.Log;

import net.spirangle.minerva.util.Base36;

import java.util.Random;


public class Key {
    private static final String TAG = "Key";

    public static final int USER = 0;
    public static final int PROFILE = 2;
    public static final int TEXT = 5;

    public static final Random rand = new Random();

    public final long id;
    private String key;

    public Key(long id) {
        this.id = id;
        this.key = null;
    }

    public Key(int type) {
        this(type,-1);
    }

    public Key(int type,int index) {
        long m = System.currentTimeMillis();
        long s = m/1000;
        int t = (int)(m%1000);
        t = (t*0x100)/1000;
        if(t==0) t = 1;
        if(type==-1 || type==0) m = (long)t;
        else {
            if(index==-1) index = rand.nextInt(0xf);
            m = (long)(((t&0xff)<<8)|((type&0xf)<<4)|(index&0xf));
        }
        this.id = (m<<32)|(s&0xffffffffl);
        this.key = null;
    }

    public Key(String key) {
        this.id = Base36.decode(key);
        this.key = key;
        Log.d(APP,TAG+"(key: "+key+", id: "+id+")");
    }

    @Override
    public String toString() {
        if(key==null) key = Base36.encode(id);
        return key;
    }
}

