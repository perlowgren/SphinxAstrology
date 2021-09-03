package net.spirangle.sphinx.activities;

import static com.android.volley.Request.Method.DELETE;
import static net.spirangle.sphinx.config.AstrologyProperties.*;
import static net.spirangle.sphinx.config.SphinxProperties.APP;
import static net.spirangle.sphinx.config.SphinxProperties.URL_SPIRANGLE_API;

import android.database.Cursor;
import android.os.Bundle;
import android.util.Log;
import android.view.*;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ListView;
import android.widget.TextView;

import androidx.appcompat.widget.PopupMenu;
import androidx.appcompat.widget.SearchView;
import androidx.appcompat.widget.SearchView.OnQueryTextListener;
import androidx.core.view.MenuItemCompat;
import androidx.cursoradapter.widget.SimpleCursorAdapter;
import androidx.cursoradapter.widget.SimpleCursorAdapter.ViewBinder;

import com.android.volley.NetworkResponse;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.JsonObjectRequest;

import net.spirangle.sphinx.R;
import net.spirangle.sphinx.astro.Coordinate;
import net.spirangle.sphinx.astro.Horoscope;
import net.spirangle.sphinx.astro.Symbol;
import net.spirangle.sphinx.db.Key;
import net.spirangle.sphinx.db.SphinxDatabase;
import net.spirangle.sphinx.services.VolleyService;
import net.spirangle.sphinx.text.CustomHtml;

import org.json.JSONObject;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;


public class ProfilesActivity extends AstroActivity implements OnQueryTextListener, ViewBinder,
                                                               OnItemClickListener, OnItemLongClickListener,
                                                               OnItemSelectedListener {
    private static final String TAG = ProfilesActivity.class.getSimpleName();

    private ListView listProfiles = null;
    private MenuItem searchItem = null;
    private SearchView search = null;

    public ProfilesActivity() {
        create_flags = ACTIONBAR_TITLE;
        activity_layout_id = R.layout.activity_profiles;
        drawer_layout_id = R.id.drawer_layout;
        toolbar_id = R.id.toolbar;
        toolbar_menu_id = R.menu.menu_ab_profiles;
        navigation_icon_id = R.drawable.ic_ab_menu;
        navigation_view_id = R.id.navigation_view;
        loading_panel_id = R.id.loading_panel;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Menu menu = navigationView.getMenu();
        drawerSignIn = (MenuItem)menu.findItem(R.id.drawer_sign_in);
        drawerSignOut = (MenuItem)menu.findItem(R.id.drawer_sign_out);
        listProfiles = (ListView)findViewById(R.id.listview);
        listProfiles.setOnItemClickListener(this);
        listProfiles.setOnItemLongClickListener(this);
        listProfiles.setOnItemSelectedListener(this);
        loadProfiles();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        if(!super.onCreateOptionsMenu(menu)) return false;
        searchItem = menu.findItem(R.id.menu_search);
        search = (SearchView)MenuItemCompat.getActionView(searchItem);
        search.setOnQueryTextListener(this);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch(item.getItemId()) {
            case R.id.menu_new_profile:
                openEditProfile(-1);
                return true;

            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        switch(item.getItemId()) {
            case R.id.drawer_help:
                openHelp(R.string.text_help_profiles);
                break;

            default:
                return super.onNavigationItemSelected(item);
        }
//		item.setChecked(false);
        drawerLayout.closeDrawer(Gravity.LEFT);
        return true;
    }

    @Override
    public boolean onQueryTextChange(String text) { return false; }

    @Override
    public boolean onQueryTextSubmit(String query) {
        Log.d(APP,TAG+".onQueryTextSubmit(query: "+query+")");
        loadProfiles("name LIKE ('%' || ? || '%')",new String[] {query});
        search.setQuery("",false);
        search.setIconified(true);
//		search.clearFocus();
//		searchItem.collapseActionView();
        return true;
    }

    @Override
    public boolean setViewValue(View view,Cursor cur,int col) {
//try {
//Log.d(APP,TAG+".onCreate(.setViewValue(view: 0x"+Long.toHexString(view.getId())+", col: "+col+"))");
        if(col==0) {
            view.setTag(new Long(cur.getLong(col)));
//			ProfilesActivity.this.registerForContextMenu(view);
            return true;
        } else if(col==1 || col==2) {
            TextView textView = (TextView)view;
//			float sz = textView.getTextSize();
            String text;
            int flags = cur.getInt(12);
            if(col==1) {
                text = String.format(Locale.ENGLISH,"<b>%1$s</b> %2$s%3$s %4$s%5$s %6$s%7$s",cur.getString(1),
                                     Symbol.getUnicode(ASTRO_SUN),Symbol.getUnicode(ASTRO_ARIES+(cur.getInt(9)/30000000)),
                                     Symbol.getUnicode(ASTRO_MOON),Symbol.getUnicode(ASTRO_ARIES+(cur.getInt(10)/30000000)),
                                     Symbol.getUnicode(ASTRO_ASCENDANT),Symbol.getUnicode(ASTRO_ARIES+(cur.getInt(11)/30000000)));
            } else {
                String time;
                if((flags&Horoscope.TIME_UNKNOWN)==0)
                    time = String.format(Locale.ENGLISH," %1$02d:%2$02d",cur.getInt(5),cur.getInt(6));
                else time = "";
                text = String.format(Locale.ENGLISH,"&#xe192; %1$d-%2$02d-%3$02d%4$s &#xE55F; %5$s, %6$s",
                                     cur.getInt(2),cur.getInt(3),cur.getInt(4),time,
                                     Coordinate.formatLongitudeGrade((double)cur.getInt(7)/1000000.0),
                                     Coordinate.formatLatitudeGrade((double)cur.getInt(8)/1000000.0));
            }
            textView.setText(CustomHtml.fromHtml(text,CustomHtml.SYMBOLS|CustomHtml.ICONS,0.0f,null));
            return true;
        }
//} catch(Exception e) {
//Log.e(APP,TAG+".onCreate.setViewValue",e);
//}
        return false;
    }

    @Override
    public void onItemClick(AdapterView<?> parent,View view,int pos,long id) {
        Log.d(APP,TAG+".onItemClick(pos: "+pos+", id: "+id+")");
        openHoroscope(id,0);
        setResult(RESULT_OK,null);
    }

    @Override
    public boolean onItemLongClick(AdapterView<?> parent,View view,int pos,long id) {
        Log.d(APP,TAG+".onItemLongClick(pos: "+pos+", id: "+id+")");
//		view.setActivated(true);
//		view.setPressed(true);
/*		if(selectedItem!=null)
			selectedItem.setBackgroundResource(R.drawable.listview_item);
		view.setBackgroundResource(R.drawable.listview_item_selected);
		selectedItem = view;
		selectedItemId = id;*/

        openEditProfile(id);
        return true;
    }

    @Override
    public void onItemSelected(AdapterView<?> parent,View view,int pos,long id) {
        Log.d(APP,TAG+".onItemSelected(pos: "+pos+", id: "+id+")");
    }

    @Override
    public void onNothingSelected(AdapterView<?> parent) {
        Log.d(APP,TAG+".onNothingSelected()");
    }

    private static final String[] fromColumns = {
        SphinxDatabase.TableProfile.id,
        SphinxDatabase.TableProfile.name,
        SphinxDatabase.TableProfile.year,
    };
    private static final int[] toViews = {
        R.id.listview_item_menu,
        R.id.listview_item_title,
        R.id.listview_item_subtitle,
    };

    public void loadProfiles() {
        loadProfiles(null,null);
    }

    public void loadProfiles(String where,String[] args) {
        if(where==null) where = "";
        else where = " WHERE "+where;
        SphinxDatabase db = SphinxDatabase.getInstance();
        Cursor cur = db.query("SELECT _id,name,year,month,day,hour,minute,longitude,latitude,sun,moon,ascendant,flags "+
                              "FROM Profile"+where+" ORDER BY name",args);
        SimpleCursorAdapter adapter = new SimpleCursorAdapter(this,R.layout.profile_listview_item,
                                                              cur,fromColumns,toViews,0);
        adapter.setViewBinder(this);
        listProfiles.setAdapter(adapter);
    }

    public void deleteProfile(long id) {
        SphinxDatabase db = SphinxDatabase.getInstance();
        Cursor cur = db.query("SELECT profileKey FROM Profile WHERE _id="+id);
        if(cur.moveToFirst()) {
            Key key = new Key(cur.getLong(0));
            String url = URL_SPIRANGLE_API+"/users/"+user.getKey()+"/profiles/"+key;
            RequestQueue requestQueue = VolleyService.getInstance().getRequestQueue();
            requestQueue.add(new JsonObjectRequest(DELETE,url,null,response -> {
                shortToast(R.string.toast_profile_deleted);
            },error -> {
                NetworkResponse response = error.networkResponse;
                String m = null;
                try {
                    JSONObject j = new JSONObject(Arrays.toString(response.data));
                    m = j.optString("message","");
                } catch(Exception e) {
                    Log.e(APP,TAG+".result",e);
                }
                Log.e(APP,TAG+".deleteProfile",error);
                if(m!=null) shortToast(getString(R.string.toast_delete_failed)+": "+m);
                else shortToast(R.string.toast_delete_failed);
            }) {
                @Override
                public Map<String,String> getHeaders() {
                    Map<String,String> headers = new HashMap<>();
                    headers.put("Authorization","Google "+google.tokenId);
                    return headers;
                }
            });
        }
        db.delete(SphinxDatabase.TableProfile.table,SphinxDatabase.TableProfile.id+"="+id);
        loadProfiles();
    }

    public void profilePopup(View view) {
        Log.d(APP,TAG+".profilePopup(view: 0x"+Long.toHexString(view.getId())+")");
        final long id = (Long)view.getTag();
        Log.d(APP,TAG+".profilePopup(id: "+id+")");

        PopupMenu popup = new PopupMenu(this,view);
        MenuInflater inflater = popup.getMenuInflater();
        inflater.inflate(R.menu.menu_popup_profiles,popup.getMenu());

        popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                Log.d(APP,TAG+".profilePopup(.onMenuItemClick(id: "+id+"))");
                switch(item.getItemId()) {
                    case R.id.popup_open:
                        openHoroscope(id,0);
                        setResult(RESULT_OK,null);
                        return true;

                    case R.id.popup_edit:
                        openEditProfile(id);
                        return true;

                    case R.id.popup_delete:
                        deleteProfile(id);
                        return true;
                }
                return false;
            }
        });
        popup.show();
    }
}
