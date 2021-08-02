package net.spirangle.sphinx;

import static net.spirangle.sphinx.SphinxProperties.APP;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import java.util.regex.Pattern;

public abstract class Database extends SQLiteOpenHelper {
    private static final String TAG = "Database";

    private static final Pattern split = Pattern.compile(";\n+");
    private static final Pattern newLines = Pattern.compile("\\\\n");

//	private static final Pattern comments = Pattern.compile("\/\*.*?\*\/|--.*?$",Pattern.MULTILINE|Pattern.DOTALL);

    public static interface DatabaseListener {
        public void onDatabaseInstallProgress(String label,float progress);
    }

    public static class Table {
        public static final String id = "_id";
        public static final String flags = "flags";
        public static final String created = "created";
    }

    ;

    public static final class TableDatabase extends Table {
        public static final String table = "Database";
        public static final String version = "version";
        public static final String counter = "counter";
        public static final String updated = "updated";
    }

    ;

    public static final class TableUser extends Table {
        public static final String table = "User";
        public static final String userKey = "userKey";
        public static final String email = "email";
        public static final String user = "user";
        public static final String name = "name";
        public static final String language = "language";
        public static final String picture = "picture";
        public static final String updated = "updated";
    }

    protected static Database instance = null;

    public static final synchronized void closeInstance() {
        if(instance!=null) {
            instance.close();
            instance = null;
        }
    }

    public static synchronized Database getInstance() {
        return instance;
    }

    public static final int timestamp() {
        return (int)(System.currentTimeMillis()/1000);
    }

    public static String unescape(String text) {
        return Regex.replace(text,newLines,"\n");
    }

    protected static String whereId(long id) { return Table.id+"="+id; }

    protected Context context = null;
    protected DatabaseListener listener = null;
    protected String name = null;
    protected int version = -1;
    protected float progress = 1.0f;

    protected Database(Context context,DatabaseListener listener,String name,int version) {
        super(context,name,null,version);
        this.context = context;
        this.listener = listener;
        this.name = name;
        this.version = version;
    }

    // Called when the database connection is being configured.
    // Configure database settings for things like foreign key support, write-ahead logging, etc.
	/*@Override
	public void onConfigure(SQLiteDatabase db) {
		super.onConfigure(db);
		db.setForeignKeyConstraintsEnabled(true);
	}*/

    @Override
    public abstract void onCreate(SQLiteDatabase db);

    @Override
    public void onUpgrade(SQLiteDatabase db,int oldVersion,int newVersion) {
        Log.w(APP,TAG+".onUpgrade(Upgrading database "+name+", from version "+oldVersion+" to "+newVersion+")");
		/*Resources res = context.getResources();
		String[] arr = res.getStringArray(R.array.SphinxDB_onUpgrade_versions);
		db.beginTransaction();
		try {
			String sql;
			for(int i=oldVersion+1,n; i<newVersion; ++i) {
Log.w(APP,"Upgrading database to version "+i+".");
				n = i-2;
				if(n<0) continue;
				if(n>=arr.length) break;
				sql = arr[n];
				if(sql.length()<=8) {
					if(sql.equals("create")) {
						sql = context.getString(R.string.SphinxDB_drop);
						exec(db,sql);
						onCreate(db);
					}
					continue;
				}
				exec(db,sql);
			}
			db.setTransactionSuccessful();
		} catch (SQLException e) {
Log.e(APP,TAG+".onUpgrade",e);
		} finally {
			db.endTransaction();
Log.w(APP,"Upgrading completed.");
		}*/
    }

    public float getProgress() {
        return progress;
    }

    protected void importSQL(SQLiteDatabase db,KeyValue[] files) {
        float pstart = 0.0f, pend = 0.0f;
        for(int i = 0; i<files.length; ++i) {
            if(listener!=null) {
                progress = pstart = pend;
                pend = (float)(i+1)/(float)files.length;
                listener.onDatabaseInstallProgress(files[i].key,progress);
            }
            String sql = BasicActivity.loadFile(context,files[i].value);
            db.beginTransaction();
            try {
                exec(db,sql,pstart,pend);
                db.setTransactionSuccessful();
            } catch(SQLException e) {
                Log.e(APP,TAG+".loadFile",e);
            } finally {
                db.endTransaction();
            }
        }
        progress = pend;
        if(listener!=null)
            listener.onDatabaseInstallProgress(null,progress);
    }

    /**
     * Execute all of the SQL statements in the String[] array
     *
     * @param db  The database on which to execute the statements
     * @param sql An array of SQL statements to execute
     */
    protected void exec(SQLiteDatabase db,String sql,float pstart,float pend) {
//		sql = comments.matcher(sql).replaceAll("");
        String[] arr = split.split(sql.trim());
        float pi = (pend-pstart)/(arr.length+1);
        progress = pstart;
        for(String s : arr) {
            s = unescape(s.trim());
            if(s.length()>0) {
//Log.d(APP,"SQL: "+s);
                db.execSQL(s);
                progress += pi;
                if(listener!=null)
                    listener.onDatabaseInstallProgress(null,progress);
            }
        }
    }

    public boolean exec(String sql) {
        try {
            SQLiteDatabase db = getWritableDatabase();
            Log.d(APP,"SQL: "+sql);
            db.execSQL(sql);
        } catch(SQLException e) {
            Log.e(APP,TAG+".exec",e);
            return false;
        }
        return true;
    }

    public boolean exec(String sql,Object[] args) {
        try {
            SQLiteDatabase db = getWritableDatabase();
            Log.d(APP,"SQL: "+sql);
            db.execSQL(sql,args);
        } catch(SQLException e) {
            Log.e(APP,TAG+".exec2",e);
            return false;
        }
        return true;
    }

    public long insert(String table,ContentValues values) {
        try {
            SQLiteDatabase db = getWritableDatabase();
            Log.d(APP,"SQL: INSERT INTO "+table);
            return db.insertOrThrow(table,null,values);
        } catch(SQLException e) {
            Log.e(APP,TAG+".insert",e);
            return -1;
        }
    }

    public boolean update(String table,ContentValues values,long id) { return update(table,values,whereId(id),null); }

    public boolean update(String table,ContentValues values,String where) { return update(table,values,where,null); }

    public boolean update(String table,ContentValues values,String where,String[] args) {
        try {
            SQLiteDatabase db = getWritableDatabase();
            Log.d(APP,"SQL: UPDATE "+table+" WHERE "+where);
            db.update(table,values,where,args);
        } catch(SQLException e) {
            Log.e(APP,TAG+".update",e);
            return false;
        }
        return true;
    }

    public boolean delete(String table,long id) { return delete(table,whereId(id),null); }

    public boolean delete(String table,String where) { return delete(table,where,null); }

    public boolean delete(String table,String where,String[] args) {
        try {
            SQLiteDatabase db = getWritableDatabase();
            Log.d(APP,"SQL: DELETE FROM "+table+" WHERE "+where);
            db.delete(table,where,args);
        } catch(SQLException e) {
            Log.e(APP,TAG+".delete",e);
            return false;
        }
        return true;
    }

    public Cursor query(String sql) { return query(sql,null); }

    public Cursor query(String sql,String[] args) {
        Cursor cur = null;
        try {
            SQLiteDatabase db = getReadableDatabase();
            Log.d(APP,"SQL: "+sql);
            cur = db.rawQuery(sql,args);
        } catch(SQLException e) {
            Log.e(APP,TAG+".query",e);
        }
        return cur;
    }

    public long queryId(String table,String where) {
        String sql = "SELECT _id FROM  "+table+(where!=null? " WHERE "+where : "");
        Cursor cur = query(sql,null);
        if(cur.moveToFirst()) return cur.getLong(0);
        return -1l;
    }

    public int queryFlags(String table,long id) { return queryFlags(table,whereId(id)); }

    public int queryFlags(String table,String where) {
        String sql = "SELECT flags FROM  "+table+(where!=null? " WHERE "+where : "");
        Cursor cur = query(sql,null);
        if(cur.moveToFirst()) return cur.getInt(0);
        return 0;
    }

    public boolean updateDatabase(int ver,int fl) {
        int tm = timestamp();
        ContentValues v = new ContentValues();
        if(ver>0) v.put(TableDatabase.version,ver);
        v.put(TableDatabase.flags,fl);
        v.put(TableDatabase.updated,tm);
        return update(TableDatabase.table,v,TableDatabase.id+"=1");
    }

    public void putUserContentValues(ContentValues v,Key key,String email,String user,String name,String lang,String pic,int fl) {
        int tm = timestamp();
        if(key!=null) v.put(TableUser.userKey,key.id);
        if(email!=null) v.put(TableUser.email,email);
        if(user!=null) v.put(TableUser.user,user);
        if(name!=null) v.put(TableUser.name,name);
        if(lang!=null) v.put(TableUser.language,lang);
        if(pic!=null) v.put(TableUser.picture,pic);
        v.put(TableUser.flags,fl);
        v.put(TableUser.updated,tm);
    }

    public long insertUser(Key key,String email,String user,String name,String lang,String pic,int fl) {
        ContentValues v = new ContentValues();
        putUserContentValues(v,key,email,user,name,lang,pic,fl);
        v.put(TableUser.created,timestamp());
        return insert(TableUser.table,v);
    }

    public boolean updateUser(BasicActivity.User u,int fl) {
        return updateUser(u.id,u.key,u.email,u.user,u.name,u.language,u.picture,fl);
    }

    public boolean updateUser(Key key,String email,String user,String name,String lang,String pic,int fl) {
        ContentValues v = new ContentValues();
        putUserContentValues(v,key,email,user,name,lang,pic,fl);
        return update(TableUser.table,v,TableUser.userKey+"="+key);
    }

    public boolean updateUser(long id,Key key,String email,String user,String name,String lang,String pic,int fl) {
        ContentValues v = new ContentValues();
        putUserContentValues(v,key,email,user,name,lang,pic,fl);
        return update(TableUser.table,v,TableUser.id+"="+id);
    }
}

