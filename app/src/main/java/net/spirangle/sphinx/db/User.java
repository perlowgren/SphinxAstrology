package net.spirangle.sphinx.db;

import android.database.Cursor;

import com.google.android.gms.auth.api.signin.GoogleSignInAccount;

import org.json.JSONObject;

import java.util.Locale;

public class User {
    private long id;     /* User id in local database table */
    private Key key;     /* Spirangle API key */
    private String email;
    private String user;
    private String name;
    private String language;
    private String picture;

    public User() {
        clear();
    }

    public void clear() {
        this.id = -1l;
        this.key = null;
        this.email = null;
        this.user = null;
        this.name = null;
        this.language = null;
        this.picture = null;
    }

    public void update(GoogleSignInAccount acct) {
        this.email = acct.getEmail();
        this.user = acct.getDisplayName();
        this.name = acct.getGivenName()+" "+acct.getFamilyName();
        this.language = Locale.getDefault().getLanguage();
        this.picture = acct.getPhotoUrl().toString();
    }

    public void update(long id,JSONObject json) {
        this.id = id;
        update(json);
    }

    public void update(JSONObject json) {
        String k = json.optString("key",null);
        this.key = k==null? null : new Key(k);
        this.email = json.optString("email",null);
        this.user = json.optString("user",null);
        this.name = json.optString("name",null);
        this.language = json.optString("language",null);
        this.picture = json.optString("picture",null);
    }

    public void update(Cursor cursor) {
        this.id = cursor.getLong(0);
        this.key = new Key(cursor.getString(1));
        this.email = cursor.getString(2);
        this.user = cursor.getString(3);
        this.name = cursor.getString(4);
        this.language = cursor.getString(5);
        this.picture = cursor.getString(6);
    }

    public long getId() {
        return id;
    }

    public Key getKey() {
        return key;
    }

    public String getEmail() {
        return email;
    }

    public String getUser() {
        return user;
    }

    public String getName() {
        return name;
    }

    public String getLanguage() {
        return language;
    }

    public String getPicture() {
        return picture;
    }

    @Override
    public String toString() {
        return "key: "+this.key+", user: "+this.user+", name: "+this.name+", "+
               "language: "+this.language+", picture: "+this.picture;
    }
}
