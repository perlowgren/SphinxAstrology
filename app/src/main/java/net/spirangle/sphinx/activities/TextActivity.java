package net.spirangle.sphinx.activities;

import static net.spirangle.sphinx.config.SphinxProperties.*;

import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.*;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.AdapterView.OnItemSelectedListener;

import androidx.cursoradapter.widget.SimpleCursorAdapter;
import androidx.cursoradapter.widget.SimpleCursorAdapter.ViewBinder;

import net.spirangle.minerva.markdown.Markdown;
import net.spirangle.minerva.util.Base36;
import net.spirangle.sphinx.R;
import net.spirangle.sphinx.astro.Symbol;
import net.spirangle.sphinx.db.AstroDB;
import net.spirangle.sphinx.text.CustomHtml;


public class TextActivity extends AstroActivity implements ViewBinder, OnItemClickListener, OnItemLongClickListener, OnItemSelectedListener {
    private static final String TAG = TextActivity.class.getSimpleName();

    private MenuItem menuEdit = null;
    private MenuItem menuNew = null;
    private MenuItem menuIndex = null;
    private ListView listTexts = null;
    private TextView textEmpty = null;
    private LinearLayout textLayout = null;
    private TextView textWriter = null;
    private ScrollView textScroll = null;
    private TextView textMarkdown = null;
    private long userId = -1l;
    private long textId = -1l;
    private long symbolId = -1l;
    private int textCount = 0;
    private int textFlags = 0;
    private boolean showText = false;

    public TextActivity() {
        create_flags = NAVIGATION_ICON_BACK|ACTIONBAR_TITLE;
        activity_layout_id = R.layout.activity_text;
        toolbar_id = R.id.toolbar;
        toolbar_menu_id = R.menu.menu_ab_text;
        navigation_icon_id = R.drawable.ic_ab_back;
        loading_panel_id = R.id.loading_panel;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        listTexts = (ListView)findViewById(R.id.listview);
        textEmpty = (TextView)findViewById(android.R.id.empty);
        textLayout = (LinearLayout)findViewById(R.id.text_layout);
        textWriter = (TextView)findViewById(R.id.text_writer);
        textScroll = (ScrollView)findViewById(R.id.text_scroll);
        textMarkdown = (TextView)findViewById(R.id.text);

        listTexts.setOnItemClickListener(this);
        listTexts.setOnItemLongClickListener(this);
        listTexts.setOnItemSelectedListener(this);

        Intent intent = getIntent();
        userId = intent.getLongExtra(EXTRA_USER,-1l);
        textId = intent.getLongExtra(EXTRA_TEXT,-1l);
        symbolId = intent.getLongExtra(EXTRA_SYMBOL,-1l);

        if(symbolId!=-1)
            setTitle(Symbol.getTitle(symbolId));

        loadText(textId);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);
        menuEdit = (MenuItem)menu.findItem(R.id.menu_edit);
        menuNew = (MenuItem)menu.findItem(R.id.menu_new);
        menuIndex = (MenuItem)menu.findItem(R.id.menu_index);
        updateUserInterface();
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch(item.getItemId()) {
            case R.id.menu_new:
                openEditText(symbolId,-1);
                return true;

            case R.id.menu_edit:
                openEditText(symbolId,textId);
                return true;

            case R.id.menu_index:
                loadTexts(userId,symbolId);
                return true;

            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void onActivityResult(int request,int result,Intent data) {
        if(request==ACTIVITY_EDIT_TEXT) {
            if(result==RESULT_OK) {
                loadTexts(userId,symbolId);
            }
        }
        super.onActivityResult(request,result,data);
    }

    @Override
    public boolean setViewValue(View view,Cursor cur,int col) {
        Log.d(APP,TAG+".setViewValue(col: "+col+")");
        if(col==0 || col==1) {
            TextView textView = (TextView)view;
            String text;
            int flags = cur.getInt(5);
            if(col==0) {
                text = cur.getString(3);
            } else {
                text = "";
            }
            textView.setText(text);
            return true;
        }
        return false;
    }

    @Override
    public void onItemClick(AdapterView<?> parent,View view,int pos,long id) {
        Log.d(APP,TAG+".onItemClick(pos: "+pos+", id: "+id+")");
//		openHoroscope(id,0);
//		setResult(RESULT_OK,null);
//		finish();
        loadText(id);
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

        openEditText(symbolId,id);
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

    @Override
    public void updateUserInterface() {
        super.updateUserInterface();
        if(showText) {
            textEmpty.setVisibility(View.GONE);
            listTexts.setVisibility(View.GONE);
            textLayout.setVisibility(View.VISIBLE);
            if(menuEdit!=null) menuEdit.setVisible((textFlags&FLAG_STATIC)==0);
            if(menuNew!=null) menuNew.setVisible(textCount==1 && (textFlags&FLAG_STATIC)!=0);
            if(menuIndex!=null) menuIndex.setVisible(textCount>1);
        } else if(textCount==0) {
            textEmpty.setVisibility(View.VISIBLE);
            listTexts.setVisibility(View.GONE);
            textLayout.setVisibility(View.GONE);
            if(menuEdit!=null) menuEdit.setVisible(false);
            if(menuNew!=null) menuNew.setVisible(true);
            if(menuIndex!=null) menuIndex.setVisible(false);
        } else {
            textEmpty.setVisibility(View.GONE);
            listTexts.setVisibility(View.VISIBLE);
            textLayout.setVisibility(View.GONE);
            if(menuEdit!=null) menuEdit.setVisible(false);
            if(menuNew!=null) menuNew.setVisible(true);
            if(menuIndex!=null) menuIndex.setVisible(false);
        }
    }

    private static final String[] fromColumns = {
        AstroDB.TableText.title,
        AstroDB.TableText.userId,
    };
    private static final int[] toViews = {
        R.id.listview_item_title,
        R.id.listview_item_subtitle,
    };

    public boolean loadText(long id) {
        textId = id;
        if(textId==-1)
            return loadTexts(userId,symbolId);

        boolean ret = false;
        AstroDB db = AstroDB.getInstance();
        Cursor cur = db.query("SELECT symbol,title,html,text,writer,flags FROM Text WHERE _id="+textId);
        if(cur.moveToFirst()) {
            symbolId = cur.getLong(0);
            String title = cur.getString(1);
            String html = cur.getString(2);
            Log.d(APP,TAG+".loadText(html: "+html+")");
            if(html==null || html.length()==0) {
                Markdown st = new Markdown(cur.getString(3));
                title = st.getTitle();
                html = st.getHtml();
                db.updateText(id,title,html,null,null,-1);
                Log.d(APP,TAG+".loadText(title: "+title+", html: "+html+")");
            }

            String writer = cur.getString(4);
            if(writer==null || writer.length()==0) {
                if(user.user!=null) writer = user.user;
                else writer = getString(R.string.unknown);
            }
            String str = String.format("<b>#%1$s</b>, by <i>%2$s</i>",Base36.encode(symbolId),writer);
            textWriter.setText(CustomHtml.fromHtml(str,0,0.0f,null));
            textFlags = cur.getInt(5);
            showText = true;
            setTextViewHTML(textMarkdown,html,CustomHtml.ALL,20.0f);
            ret = true;
        }
        cur.close();
        updateUserInterface();
        return ret;
    }

    public boolean loadTexts(long uid,long sid) {
        userId = uid;
        textId = -1;
        symbolId = sid;

        boolean ret = false;
        try {
            String where = "";
            if(userId!=-1) where += (where.length()>0? " AND" : "")+" userId="+userId;
            if(symbolId!=-1) where += (where.length()>0? " AND" : "")+" symbol="+symbolId;
            if(where.length()>0) where = " WHERE"+where;
            AstroDB db = AstroDB.getInstance();
            Cursor cur = db.query("SELECT _id,userId,symbol,title,language,flags,updated "+
                                  "FROM Text"+where+" ORDER BY updated DESC LIMIT 20");
            Log.d(APP,TAG+".loadTexts(count: "+cur.getCount()+")");
            textCount = cur.getCount();
            textFlags = 0;
            showText = false;
            if(textCount==1) {
                cur.moveToFirst();
                long id = cur.getLong(0);
                cur.close();
                return loadText(id);
            }
            if(textCount>1) {
                SimpleCursorAdapter adapter = new SimpleCursorAdapter(this,R.layout.text_listview_item,
                                                                      cur,fromColumns,toViews,0);
                adapter.setViewBinder(this);
                listTexts.setAdapter(adapter);
                ret = true;
            }
            updateUserInterface();
        } catch(Exception e) {
            Log.e(APP,TAG+".loadTexts",e);
        }
        return ret;
    }
}

