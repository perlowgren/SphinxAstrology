package net.spirangle.sphinx.activities;

import static com.android.volley.Request.Method.POST;
import static com.android.volley.Request.Method.PUT;
import static net.spirangle.sphinx.config.SphinxProperties.*;

import android.app.Dialog;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.text.Editable;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.*;

import androidx.core.content.res.ResourcesCompat;

import com.android.volley.RequestQueue;
import com.android.volley.toolbox.JsonObjectRequest;

import net.spirangle.minerva.markdown.Markdown;
import net.spirangle.minerva.util.Base36;
import net.spirangle.sphinx.R;
import net.spirangle.sphinx.astro.Symbol;
import net.spirangle.sphinx.db.Key;
import net.spirangle.sphinx.db.SphinxDatabase;
import net.spirangle.sphinx.services.VolleyService;
import net.spirangle.sphinx.text.CustomHtml;

import org.json.JSONObject;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.regex.Pattern;


public class EditTextActivity extends AstroActivity {
    private static final String TAG = EditTextActivity.class.getSimpleName();

    private MenuItem menuDelete = null;
    private TextView textTitle;
    private Button buttonBold;
    private Button buttonItalic;
    private Button buttonUnderline;
    //	private Button buttonStrike;
    private Button buttonLink;
    private Button buttonPicture;
    private Button buttonOL;
    private Button buttonUL;
    private LinearLayout editLayout;
    private EditText editText;
    private ScrollView previewScroll;
    private TextView preview;

    private long textId = -1l;
    private long symbolId = -1l;
    private Key key = null;
    private String symbolTitle = null;
    private String title = null;
    private String text = null;
    private String writer = null;
    private String language = null;
    private int flags = 0;

    public EditTextActivity() {
        create_flags = NAVIGATION_ICON_BACK|ACTIONBAR_TITLE;
        activity_layout_id = R.layout.activity_edit_text;
        toolbar_id = R.id.toolbar;
        toolbar_menu_id = R.menu.menu_ab_edit_text;
        navigation_icon_id = R.drawable.ic_ab_back;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        textTitle = (TextView)findViewById(R.id.title);
        buttonBold = (Button)findViewById(R.id.button_bold);
        buttonItalic = (Button)findViewById(R.id.button_italic);
        buttonUnderline = (Button)findViewById(R.id.button_underline);
//		buttonStrike     = (Button)findViewById(R.id.button_strike);
        buttonLink = (Button)findViewById(R.id.button_link);
        buttonPicture = (Button)findViewById(R.id.button_picture);
        buttonOL = (Button)findViewById(R.id.button_ol);
        buttonUL = (Button)findViewById(R.id.button_ul);
        editLayout = (LinearLayout)findViewById(R.id.edit_layout);
        editText = (EditText)findViewById(R.id.edit);
        previewScroll = (ScrollView)findViewById(R.id.preview_scroll);
        preview = (TextView)findViewById(R.id.preview);

        buttonBold.setTypeface(iconFont);
        buttonItalic.setTypeface(iconFont);
        buttonUnderline.setTypeface(iconFont);
        buttonLink.setTypeface(iconFont);
        buttonPicture.setTypeface(iconFont);
        buttonOL.setTypeface(iconFont);
        buttonUL.setTypeface(iconFont);

		/*editText.setCustomSelectionActionModeCallback(new ActionMode.Callback() {
			@Override
			public boolean onPrepareActionMode(ActionMode mode,Menu menu) { return false; }

			@Override
			public void onDestroyActionMode(ActionMode mode) {}

			@Override
			public boolean onCreateActionMode(ActionMode mode,Menu menu) { return false; }

			@Override
			public boolean onActionItemClicked(ActionMode mode,MenuItem item) { return false; }
		});*/

//		preview.setVisibility(View.GONE);

        Intent intent = getIntent();
        textId = intent.getLongExtra(EXTRA_TEXT,-1l);
        symbolId = intent.getLongExtra(EXTRA_SYMBOL,-1l);
        if(symbolId!=-1) {
            symbolTitle = Symbol.getTitle(symbolId);
            super.setTitle(symbolTitle);
        }
        loadText(textId);
    }

    @Override
    public void setTitle(CharSequence title) {
        this.title = symbolTitle;
        if(title==null) title = this.title;
        textTitle.setVisibility(title==null? View.GONE : View.VISIBLE);
        if(title!=null) {
            String str = title.toString();
            this.title = str;
            if(symbolId!=-1)
                str = "#"+Base36.encode(symbolId)+": "+str;
            textTitle.setText(str);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);
        menuDelete = (MenuItem)menu.findItem(R.id.menu_delete);
        menuDelete.setVisible(textId!=-1);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch(item.getItemId()) {
            case R.id.menu_delete:
                deleteText();
                return true;

            case R.id.menu_preview:
                if(!previewScroll.isShown()) {
                    String text = editText.getText().toString();
                    String html = "";
                    if(text.length()>0) {
                        try {
                            Markdown st = new Markdown(text);
                            setTitle(st.getTitle());
                            html = st.getHtml();
                        } catch(Exception e) {
                            Log.e(APP,TAG+".onOptionsItemSelected",e);
                        }
                    }

                    Log.d(APP,TAG+".onOptionsItemSelected(\ntext:\n"+text+"\n\nhtml:\n"+html+"\n)");

                    hideKeyboard();
                    setTextViewHTML(preview,html,CustomHtml.ALL,20.0f);
                    previewScroll.scrollTo(0,0);
                    editLayout.setVisibility(View.GONE);
                    previewScroll.setVisibility(View.VISIBLE);
                    item.setIcon(ResourcesCompat.getDrawable(getResources(),R.drawable.ic_ab_edit,null));
                } else {
                    previewScroll.setVisibility(View.GONE);
                    editLayout.setVisibility(View.VISIBLE);
                    item.setIcon(ResourcesCompat.getDrawable(getResources(),R.drawable.ic_ab_eye,null));
                }
                return true;

            case R.id.menu_save:
                saveText();
                return true;

            case R.id.menu_help_text:
                openHelp(R.string.text_help_edit_text);
                return true;

            case R.id.menu_help_syntax:
                openHelp(R.string.text_help_syntax);
                return true;

            case R.id.menu_help_links:
                openHelp(R.string.text_help_links);
                return true;

            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void onLinkClick(View view,String url) {
        Log.d(APP,TAG+".onLinkClick(TextView: "+(view instanceof TextView? "true" : "false")+", url: "+url+")");
    }

    public boolean loadText(long id) {
        boolean ret = false;
        key = null;
        title = null;
        text = null;
        writer = null;
        language = null;
        flags = 0;
        if(id!=-1l && user.getId()!=-1l) {
            Locale locale = Locale.getDefault();
            SphinxDatabase db = SphinxDatabase.getInstance();
            Cursor cur = db.query("SELECT userId,textKey,title,text,writer,language,flags FROM Text WHERE _id="+id);
            if(cur!=null && cur.moveToFirst()) {
                textId = id;
                long uid = cur.getLong(0);
                key = new Key(cur.getLong(1));
                title = cur.getString(2);
                text = cur.getString(3);
                writer = cur.getString(4);
                language = cur.getString(5);
                flags = cur.getInt(6);
                cur.close();
                editText.setText(text);
                ret = true;
            }
        }
        setTitle(title);
        return ret;
    }

    public boolean saveText() {
        if(symbolId==-1l || user.getId()==-1l) return false;
        String newTitle = null;
        String newText = editText.getText().toString();
        String newHtml = "";

        SphinxDatabase db = SphinxDatabase.getInstance();

        if(textId!=-1) {
            flags = db.queryFlags(SphinxDatabase.TableText.table,textId);
            if((flags&FLAG_STATIC)!=0) {
                shortToast(R.string.toast_text_change_static);
                return false;
            }
        }

        if(newText.length()==0) return false;

        try {
            Markdown st = new Markdown(newText);
            newTitle = st.getTitle();
            newHtml = st.getHtml();
        } catch(Exception e) {
            Log.e(APP,TAG+".onOptionsItemSelected",e);
        }

        Log.d(APP,TAG+".saveText(key: "+key+")");
        if(key==null) key = new Key(Key.TEXT);

        if(newTitle==null) newTitle = symbolTitle;

        if(language==null) {
            Locale locale = Locale.getDefault();
            language = locale.getLanguage();
        }

        String url = URL_SPIRANGLE_API+"/users/"+user.getKey()+"/texts/"+key;
        Map<String,Object> params = new HashMap<>();
        params.put("type","symbol");
        params.put("symbol",Base36.encode(symbolId));
        params.put("title",newTitle);
        params.put("text",newText);
        params.put("language",language);
        params.put("flags",flags);
        JSONObject json = new JSONObject(params);

        RequestQueue requestQueue = VolleyService.getInstance().getRequestQueue();
        int method;
        if(textId==-1) {
            textId = db.insertText(user.getId(),key,2,null,symbolId,newTitle,newHtml,newText,null,language,flags);
            method = POST;
        } else {
            db.updateText(textId,newTitle,newHtml,newText,null,flags);
            method = PUT;
        }
        requestQueue.add(new JsonObjectRequest(method,url,json,response -> {
            shortToast(R.string.toast_profile_saved);
        },error -> {
            Log.e(APP,TAG+".saveProfile",error);
            shortToast(R.string.toast_save_failed);
        }) {
            @Override
            public Map<String,String> getHeaders() {
                Map<String,String> headers = new HashMap<>();
                headers.put("Authorization","Google "+google.tokenId);
                return headers;
            }
        });
        return true;
    }

    public boolean deleteText() {
        if(textId==-1l) return false;
        SphinxDatabase db = SphinxDatabase.getInstance();
        db.delete(SphinxDatabase.TableText.table,textId);
        shortToast(R.string.toast_text_deleted);
        setResult(RESULT_OK,null);
        finish();
        return true;
    }

    private static final Pattern selectionStyleStart = Pattern.compile("(_|[^\\\\\\w])\\w");
    private static final Pattern selectionStyleEnd = Pattern.compile("\\w(_|[^\\w])");

    public void setTextStyle(View view) {
        String style = null;
        switch(view.getId()) {
            case R.id.button_bold:
                style = "**";
                break;
            case R.id.button_italic:
                style = "//";
                break;
            case R.id.button_underline:
                style = "__";
                break;
//			case R.id.button_strike:style = "~~";break;
            case R.id.button_link:
                openPickLinkDialog();
                return;
            case R.id.button_picture:
                return;
            case R.id.button_ol:
                return;
            case R.id.button_ul:
                return;
            default:
                return;
        }
        Editable ed = editText.getEditableText();
        int start = editText.getSelectionStart();
        int end = editText.getSelectionEnd();
        int len = ed.length();
        int ls = 2;
        int le = 2;
        String s = ""+(start==0? ' ' : ed.charAt(start-1))+(start<len? ed.charAt(start) : ' ');
        String e = ""+(end==0? ' ' : ed.charAt(end-1))+(end<len? ed.charAt(end) : ' ');
        Log.d(APP,TAG+".setTextStyle(s: ["+s+"], e: ["+e+"])");
        if(selectionStyleStart.matcher(s).matches()) --ls;
        if(selectionStyleEnd.matcher(e).matches()) --le;
        if(start==end) {
            if(ls<=le) {
                if(end==len && le==2) {
                    s = ""+(end==0? ' ' : ed.charAt(end-1))+'a';
                    if(selectionStyleStart.matcher(s).matches()) --ls;
                }
                ed.insert(start,style,0,ls);
            } else {
                if(end==len) {
                    style = ""+style.charAt(0)+' ';
                    le = 2;
                }
                ed.insert(end,style,0,le);
                ls = le;
            }
        } else {
            ed.insert(end,style,0,le);
            ed.insert(start,style,0,ls);
        }
        if(end==len) editText.setSelection(ed.length());
        else {
            start += ls;
            end += ls;
            editText.setSelection(start,end);
        }
    }

    private void setSpinnerAdapter(Spinner spinner,String[] array,boolean addBlank,int sel) {
        String[] list = null;
        if(array!=null) {
            if(addBlank) {
                String[] a2 = new String[array.length+1];
                a2[0] = "";
                for(int i = 0; i<array.length; ++i) a2[i+1] = array[i];
                list = a2;
                if(sel>=0) ++sel;
            } else list = array;
        } else {
            list = new String[] {"-"};
        }
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this,android.R.layout.simple_spinner_item,list);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(adapter);
        if(sel>=0 && sel<array.length) spinner.setSelection(sel,false);
        spinner.setEnabled(array!=null);
    }

    private class ValueSelection implements AdapterView.OnItemSelectedListener {
        public final Spinner spinner;
        public int value = -1;

        public ValueSelection(Spinner s,int v) {
            spinner = s;
            value = v;
        }

        @Override
        public void onItemSelected(AdapterView<?> parent,View view,int pos,long id) {
            value = pos;
        }

        @Override
        public void onNothingSelected(AdapterView<?> parent) {
            value = -1;
        }
    }

    private class ConceptSelection implements AdapterView.OnItemSelectedListener {
        private final ValueSelection value1;
        private final ValueSelection value2;
        private final ValueSelection value3;
        public final Symbol.Concept concept;
        public int id = -1;
        public int value = -1;

        public ConceptSelection(Symbol.Concept c,int n,ValueSelection v1,ValueSelection v2,ValueSelection v3) {
            value1 = v1;
            value2 = v2;
            value3 = v3;
            concept = c;
            id = n;
            value = concept.indexOf(n);
        }

        @Override
        public void onItemSelected(AdapterView<?> parent,View view,int pos,long id) {
            value = pos;
            this.id = concept.getId(pos);
            Symbol.Concept c = concept.getConcept(this.id);
            String[] a1, a2, a3, a4, a5;
            a1 = a2 = a3 = a4 = a5 = null;
            switch(c.number) {
                case 5:
                case 4:
                case 3:
                    a3 = c.third.getNames();
                case 2:
                    a2 = c.second.getNames();
                case 1:
                    a1 = c.first.getNames();
            }
            setSpinnerAdapter(value1.spinner,a1,false,value1.value = (a1!=null? 0 : -1));
            setSpinnerAdapter(value2.spinner,a2,false,value2.value = (a2!=null? 0 : -1));
            setSpinnerAdapter(value3.spinner,a3,false,value3.value = (a3!=null? 0 : -1));
        }

        @Override
        public void onNothingSelected(AdapterView<?> parent) {
            id = -1;
            value = -1;
            setSpinnerAdapter(value1.spinner,null,false,value1.value = -1);
            setSpinnerAdapter(value2.spinner,null,false,value2.value = -1);
            setSpinnerAdapter(value3.spinner,null,false,value3.value = -1);
        }
    }

    public void openPickLinkDialog() {
        final Dialog dialog = new Dialog(this,R.style.SphinxTheme_Dialog);
//		dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setCancelable(true);
        dialog.setCanceledOnTouchOutside(true);
        dialog.setContentView(R.layout.dialog_pick_link);
        dialog.setTitle(getString(R.string.dialog_pick_link_title));

        final Symbol.Concept concept = Symbol.getConcept(Symbol.Concept.ASTRO_CONCEPT);

        final Spinner spinnerRecent = (Spinner)dialog.findViewById(R.id.pick_link_spinner_recent);
        final Spinner spinnerConcept = (Spinner)dialog.findViewById(R.id.pick_link_spinner_concept);
        final Spinner spinnerValue1 = (Spinner)dialog.findViewById(R.id.pick_link_spinner_value1);
        final Spinner spinnerValue2 = (Spinner)dialog.findViewById(R.id.pick_link_spinner_value2);
        final Spinner spinnerValue3 = (Spinner)dialog.findViewById(R.id.pick_link_spinner_value3);

        final ValueSelection rsel = new ValueSelection(spinnerRecent,-1);
        final ValueSelection vsel1 = new ValueSelection(spinnerValue1,-1);
        final ValueSelection vsel2 = new ValueSelection(spinnerValue2,-1);
        final ValueSelection vsel3 = new ValueSelection(spinnerValue3,-1);
        final ConceptSelection csel = new ConceptSelection(concept,Symbol.Concept.ASTRO_PLZ,vsel1,vsel2,vsel3);

        spinnerRecent.setOnItemSelectedListener(rsel);
        spinnerConcept.setOnItemSelectedListener(csel);
        spinnerValue1.setOnItemSelectedListener(vsel1);
        spinnerValue2.setOnItemSelectedListener(vsel2);
        spinnerValue3.setOnItemSelectedListener(vsel3);

        setSpinnerAdapter(spinnerRecent,null,false,rsel.value);
        setSpinnerAdapter(spinnerConcept,Symbol.astrologyConceptNames(),false,csel.value);

        {
            String[] planets = Symbol.astrologyPlanetNames();
            Log.d(APP,TAG+".setSpinnerAdapter(planets: "+planets+")");
            if(planets!=null)
                for(int i = 0; i<planets.length; ++i)
                    Log.d(APP,TAG+".setSpinnerAdapter(planets["+i+"]: "+planets[i]+")");
        }

        Button button;
        button = (Button)dialog.findViewById(R.id.pick_link_button_cancel);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
            }
        });
        button = (Button)dialog.findViewById(R.id.pick_link_button_ok);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
                Symbol symbol = null;
                if(rsel.value>0) {
                } else {
                    Symbol.Concept c = concept.getConcept(csel.id);
                    int v1, v2, v3, v4, v5;
                    v1 = v2 = v3 = v4 = v5 = -1;
                    switch(c.number) {
                        case 5:
                            v5 = 0;
                        case 4:
                            v4 = 0;
                        case 3:
                            v3 = c.third.getId(vsel3.value);
                        case 2:
                            v2 = c.second.getId(vsel2.value);
                        case 1:
                            v1 = c.first.getId(vsel1.value);
                    }
                    symbol = new Symbol(c.id,v1,v2,v3,v4,v5);
                }
                Editable ed = editText.getEditableText();
                int start = editText.getSelectionStart();
                int end = editText.getSelectionEnd();
                String link = String.format(Locale.getDefault(),"[%s #%s]",symbol.getTitle(),symbol.toString());
                ed.replace(start,end,link);
                start += link.length();
                editText.setSelection(start,start);
            }
        });
        dialog.show();
    }
}
