package net.spirangle.sphinx.activities;

import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;

import net.spirangle.sphinx.R;


public class SettingsActivity extends AstroActivity {
    private static final String TAG = SettingsActivity.class.getSimpleName();

    public SettingsActivity() {
        create_flags = ACTIONBAR_TITLE|NAVIGATION_ICON_BACK;
        activity_layout_id = R.layout.activity_settings;
        toolbar_id = R.id.toolbar;
        toolbar_menu_id = R.menu.menu_ab_settings;
        navigation_icon_id = R.drawable.ic_ab_back;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);
/*		ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(
				getSupportActionBar().getThemedContext(),
				R.array.actionbar_graph_views,
				R.layout.actionbar_spinner_item);
		adapter.setDropDownViewResource(R.layout.support_simple_spinner_dropdown_item);
		spinnerNav.setAdapter(adapter);*/
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch(item.getItemId()) {
            case R.id.menu_help:
                openHelp(R.string.text_help_settings);
                return true;

            default:
                return super.onOptionsItemSelected(item);
        }
    }
}

