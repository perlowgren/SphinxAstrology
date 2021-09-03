package net.spirangle.sphinx.activities;

import static net.spirangle.sphinx.config.SphinxProperties.APP;
import static net.spirangle.sphinx.config.SphinxProperties.EXTRA_HELP;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import net.spirangle.sphinx.R;
import net.spirangle.sphinx.text.CustomHtml;

import java.util.Collections;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;


public class HelpActivity extends AstroActivity {
    private static final String TAG = HelpActivity.class.getSimpleName();

    private static final Map<String,Integer> index;

    static {
        HashMap<String,Integer> i = new HashMap<String,Integer>();
        i.put("index",R.string.text_help_index);
        i.put("sphinxAstrology",R.string.text_help_sphinx);
        i.put("faq",R.string.text_help_faq);
        i.put("horoscope",R.string.text_help_horoscope);
        i.put("profiles",R.string.text_help_profiles);
        i.put("editProfile",R.string.text_help_edit_profile);
        i.put("editText",R.string.text_help_edit_text);
        i.put("textSyntax",R.string.text_help_syntax);
        i.put("textLinks",R.string.text_help_links);
        i.put("textTags",R.string.text_help_tags);
        i.put("settings",R.string.text_help_settings);
        index = Collections.unmodifiableMap(i);
    }

    private TextView textHelp;
    private int textId;

    public HelpActivity() {
        create_flags = NAVIGATION_ICON_BACK|ACTIONBAR_TITLE;
        activity_layout_id = R.layout.activity_help;
        toolbar_id = R.id.toolbar;
        toolbar_menu_id = R.menu.menu_ab_help;
        navigation_icon_id = R.drawable.ic_ab_back;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        textHelp = (TextView)findViewById(R.id.text_help);
        textId = -1;

        Intent intent = getIntent();
        int id = intent.getIntExtra(EXTRA_HELP,R.string.text_help_index);
        showHelpText(id);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch(item.getItemId()) {
            case R.id.menu_index:
                showHelpText(R.string.text_help_index);
                return true;

            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void onLinkClick(View view,String url) {
        Log.d(APP,TAG+".onLinkClick(TextView: "+(view instanceof TextView? "true" : "false")+", url: "+url+")");
        if(url.startsWith("help:")) {
            url = url.substring(5);
            Log.d(APP,TAG+".onLinkClick(url: "+url+")");
            int id = R.string.text_help_index;
            try {
                id = index.get(url);
            } catch(Exception e) {
                Log.e(APP,TAG+".onLinkClick",e);
            }
            Log.d(APP,TAG+String.format(Locale.ENGLISH,".onLinkClick(id: 0x%x)",id));
            showHelpText(id);
        }
    }

    public void showHelpText(int id) {
        if(id==textId) return;
        Log.d(APP,TAG+String.format(Locale.ENGLISH,".showHelpText(id: 0x%x)",id));
        String html = null;
        try {
            html = getString(id);
        } catch(Exception e) {
            Log.e(APP,TAG+".showHelpText",e);
        }
        if(html!=null)
            setTextViewHTML(textHelp,html,CustomHtml.ALL,20.0f);
        textHelp.scrollTo(0,0);
    }
}
