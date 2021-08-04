package net.spirangle.sphinx;

import static net.spirangle.sphinx.config.SphinxProperties.*;

import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.database.Cursor;
import android.graphics.Typeface;
import android.os.Build;
import android.os.Bundle;
import android.os.LocaleList;
import android.text.method.LinkMovementMethod;
import android.util.Log;
import android.view.*;
import android.view.inputmethod.InputMethodManager;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.Task;
import com.google.android.material.navigation.NavigationView;
import com.google.android.material.navigation.NavigationView.OnNavigationItemSelectedListener;

import net.spirangle.sphinx.CustomHtml.CustomHtmlListener;

import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.util.Locale;
import java.util.Objects;


public abstract class BasicActivity extends AppCompatActivity implements OnNavigationItemSelectedListener, CustomHtmlListener {
    private static final String TAG = "BasicActivity";

    public static class Google {
        public GoogleSignInClient client = null;
        public String id;
        public String tokenId;
        public boolean signingIn;

        public Google() { clear(); }

        private void clear() {
            Log.d(APP,TAG+".Google.clear("+client+")");
//			client     = null;
            id = null;
            tokenId = null;
            signingIn = false;
        }

        private void update(final BasicActivity activity,Task<GoogleSignInAccount> result) {
            Log.d(APP,TAG+".Google.update(result:"+(result!=null && result.isSuccessful())+")");
            signingIn = false;
            if(result==null || !result.isSuccessful()) {
                if(result!=null) {
                    result.addOnCompleteListener(task -> {
                        try {
                            updateAccount(activity,Objects.requireNonNull(task.getResult(ApiException.class)));
                        } catch(ApiException e) {
                            // You can get from apiException.getStatusCode() the detailed error code
                            // e.g. GoogleSignInStatusCodes.SIGN_IN_REQUIRED means user needs to take
                            // explicit action to finish sign-in;
                            // Please refer to GoogleSignInStatusCodes Javadoc for details
                            Log.d(APP,TAG+".Google.update(status: "+e.getStatusCode()+", message:"+e.getStatusMessage()+")");
                            signOut(activity);
                        }
                    });
                }
            } else {
                // Signed in successfully, show authenticated UI.
                updateAccount(activity,result.getResult());
            }
        }

        private void updateAccount(final BasicActivity activity,GoogleSignInAccount account) {
            id = account.getId();
            tokenId = account.getIdToken();
            activity.user.update(account);
            activity.updateUserInterface();
            if(tokenId!=null)
                activity.spirangleSignIn();
        }

        private void init(final BasicActivity activity) {
            Log.d(APP,TAG+".Google.init("+client+")");
            if(client!=null) return;
            try {
                GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                    .requestIdToken(SERVER_CLIENT_ID)
                    .requestEmail()
                    .build();
                client = GoogleSignIn.getClient(activity,gso);

                Log.d(APP,TAG+".googleInit:2("+client+")");
            } catch(Exception e) {
                Log.e(APP,TAG+".Google.init",e);
            }
        }

        public void silentSignIn(final BasicActivity activity,final Runnable r) {
            Log.d(APP,TAG+".Google.silentSignIn("+client+")");
            if(client==null) init(activity);
            if(client==null) return;
            final Task<GoogleSignInAccount> result = client.silentSignIn();
            signingIn = true;
            activity.updateUserInterface();
            update(activity,result);
        }

        public void signIn(final BasicActivity activity) {
            Log.d(APP,TAG+".Google.signIn("+client+")");
            if(client==null) init(activity);
            if(client==null) return;
            Intent intent = client.getSignInIntent();
            activity.startActivityForResult(intent,ACTIVITY_SIGN_IN);
        }

        public void signOut(final BasicActivity activity) {
            Log.d(APP,TAG+".Google.signOut("+client+")");
            if(client==null) init(activity);
            if(client==null) return;
            Task<Void> result = client.signOut();
            if(result!=null)
                result.addOnCompleteListener(task -> {
                    if(task.isSuccessful()) clear();
                    activity.updateUserInterface();
                });
        }
    }

    public static class User {
        public long id;     /* User id in local database table */
        public Key key;     /* Spirangle API key */
        public String email;
        public String user;
        public String name;
        public String language;
        public String picture;

        public User() { clear(); }

        public void clear() {
            id = -1l;
            key = null;
            email = null;
            user = null;
            name = null;
            language = null;
            picture = null;
        }

        public void update(GoogleSignInAccount acct) {
            email = acct.getEmail();
            user = acct.getDisplayName();
            name = acct.getGivenName()+" "+acct.getFamilyName();
            language = Locale.getDefault().getLanguage();
            picture = acct.getPhotoUrl().toString();
        }

        public void update(JSONObject json) {
            String k = json.optString("key",null);
            key = k==null? null : new Key(k);
            email = json.optString("email",null);
            user = json.optString("user",null);
            name = json.optString("name",null);
            language = json.optString("language",null);
            picture = json.optString("picture",null);
        }
    }

    protected static final int HOME_AS_UP_ENABLED = 1;
    protected static final int ACTIONBAR_TITLE = 1<<1;
    protected static final int NAVIGATION_ICON_BACK = 1<<2;

    public static Typeface monoFont = null;
    public static Typeface iconFont = null;
    public static Typeface symbolFont = null;

    public static final Google google = new Google();
    public static final User user = new User();

    private static boolean activityInitiated = false;
    private static int activityCounter = 0;

    protected int create_flags = 0;
    protected int activity_layout_id = -1;
    protected int drawer_layout_id = -1;
    protected int toolbar_id = -1;
    protected int toolbar_menu_id = -1;
    protected int navigation_icon_id = -1;
    protected int navigation_view_id = -1;
    protected int loading_panel_id = -1;
    protected Toolbar toolbar = null;
    protected DrawerLayout drawerLayout = null;
    protected MenuItem drawerSignIn = null;
    protected MenuItem drawerSignOut = null;
    protected NavigationView navigationView = null;
    protected RelativeLayout loadingPanel = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        try {

            if(!activityInitiated) {
                activityInitiated = true;
                onInit();
            }
            ++activityCounter;
            Log.d(APP,TAG+".onCreate("+activityCounter+")");

            if(activity_layout_id==-1) return;
            setContentView(activity_layout_id);

            if(toolbar_id!=-1) {
                toolbar = (Toolbar)findViewById(toolbar_id);
                setSupportActionBar(toolbar);
                {
                    ActionBar ab = getSupportActionBar();
                    if((create_flags&HOME_AS_UP_ENABLED)!=0) {
                        ab.setDisplayHomeAsUpEnabled(true);
                        ab.setHomeAsUpIndicator(R.drawable.ic_ab_back);
                    }
                    ab.setDisplayShowTitleEnabled((create_flags&ACTIONBAR_TITLE)!=0);
                }

                if(navigation_icon_id!=-1) {
                    if(drawer_layout_id!=-1 && navigation_view_id!=-1) {
                        drawerLayout = (DrawerLayout)findViewById(drawer_layout_id);
                        navigationView = (NavigationView)findViewById(navigation_view_id);
                        navigationView.setNavigationItemSelectedListener(this);
                        toolbar.setNavigationIcon(navigation_icon_id);
                        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                drawerLayout.openDrawer(GravityCompat.START);
                            }
                        });
                    } else if((create_flags&NAVIGATION_ICON_BACK)!=0) {
                        toolbar.setNavigationIcon(navigation_icon_id);
                        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                finish();
                            }
                        });
                    }
                }
            }

            if(loading_panel_id!=-1) {
                loadingPanel = (RelativeLayout)findViewById(loading_panel_id);
            }

            Log.d(APP,TAG+".onCreate("+toolbar+")");

        } catch(Exception e) {
            Log.e(APP,TAG+".onCreate",e);
        }

    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        --activityCounter;
        Log.d(APP,TAG+".onDestroy("+activityCounter+")");
//		if(activityCounter==0) onExit();
    }

    @Override
    public void onStart() {
        super.onStart();
        Log.d(APP,TAG+".onStart()");
        updateUserInterface();
        if(google.id==null)
            google.silentSignIn(this,null);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        if(toolbar_menu_id!=-1) {
//			try {
            MenuInflater inflater = getMenuInflater();
            inflater.inflate(toolbar_menu_id,menu);
//				return true;
//			} catch(Exception e) {
//Log.e(APP,TAG+".onCreateOptionsMenu",e);
//			}
        }
        return super.onCreateOptionsMenu(menu);
//		return false;
    }

    @Override
    public void onBackPressed() {
        hideLoading();
        super.onBackPressed();
    }

    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        return false;
    }

    @Override
    public void onActivityResult(int request,int result,Intent data) {
        super.onActivityResult(request,result,data);
        switch(request) {
            case ACTIVITY_SIGN_IN:
                // Result returned from launching the Intent from GoogleSignInApi.getSignInIntent(...);
                //TODO: google.update(this,Auth.GoogleSignInApi.getSignInResultFromIntent(data));
                break;
        }
    }

    @Override
    public void onLinkClick(View view,String url) {
    }

    public void onInit() {
        setLanguage(null);

        if(monoFont==null)
            monoFont = Typeface.createFromAsset(getAssets(),"ProFont.ttf");
        if(iconFont==null)
            iconFont = Typeface.createFromAsset(getAssets(),"MaterialIcons-Regular.ttf");
        if(symbolFont==null)
            symbolFont = Typeface.createFromAsset(getAssets(),"SeshatSymbols.ttf");

        Database db = Database.getInstance();
        Cursor cur = db.query("SELECT version,counter,flags FROM Database WHERE _id=1");
        if(!cur.moveToFirst()) shortToast(R.string.toast_db_error);
        else {
            int version = cur.getInt(0);
            int counter = cur.getInt(1);
            int flags = cur.getInt(2);
            if(counter==0) {
                showMessageBox(getString(R.string.msgbox_title_initial),getString(R.string.msgbox_initial));
            }
            db.exec("UPDATE Database SET counter=counter+1,flags="+flags+" WHERE _id=1");
        }
//		google.init(this);
    }

/*	public void onExit() {
		Database.closeInstance();
		google.clear();
		user.clear();
//		Complete exit:
//		android.os.Process.killProcess(android.os.Process.myPid());
//		System.exit(1);
	}*/

    public abstract void spirangleSignIn();

    private void readUserData() {
        Database db = Database.getInstance();
        Cursor cur = db.query("SELECT _id,userKey,email,user,name,language,picture,flags FROM User WHERE _id=0");
        if(cur.moveToFirst()) {
            user.id = cur.getLong(0);
            user.key = new Key(cur.getString(1));
            user.email = cur.getString(2);
            user.user = cur.getString(3);
            user.name = cur.getString(4);
            user.language = cur.getString(5);
            user.picture = cur.getString(6);

            Log.d(APP,TAG+".readUserData(key: "+user.key+", user: "+user.user+", name: "+user.name+
                      ", language: "+user.language+", picture: "+user.picture+")");
        }
        cur.close();
    }

    public void setLanguage(String language) {
        Locale locale = null;
        if(language!=null)
            locale = new Locale(language);
        setLocale(locale);
    }

    @SuppressWarnings("deprecation")
    public void setLocale(Locale locale) {
        Resources res = getApplicationContext().getResources();
        Configuration config = res.getConfiguration();
        if(locale==null) locale = Locale.getDefault();
        else {
            Locale.setDefault(locale);
            if(Build.VERSION.SDK_INT>=Build.VERSION_CODES.N)
                config.setLocales(new LocaleList(locale));
            else
                config.locale = locale;
            res.updateConfiguration(config,res.getDisplayMetrics());
        }

        String[][] symbol_data = {
            res.getStringArray(R.array.concept_names),
            res.getStringArray(R.array.system_names),
            res.getStringArray(R.array.direction_names),
            res.getStringArray(R.array.calendar_names),
            res.getStringArray(R.array.month_names),
            res.getStringArray(R.array.element_names),
            res.getStringArray(R.array.classic_planet_names),
            res.getStringArray(R.array.zodiac_names),
            res.getStringArray(R.array.astrology_category_names),
            res.getStringArray(R.array.quality_names),
            res.getStringArray(R.array.energy_names),
            res.getStringArray(R.array.planet_names),
            res.getStringArray(R.array.minor_planet_names),
            res.getStringArray(R.array.fixed_star_names),
            res.getStringArray(R.array.astro_point_names),
            res.getStringArray(R.array.arabic_part_names),
            res.getStringArray(R.array.house_names),
            res.getStringArray(R.array.aspect_names),
            res.getStringArray(R.array.aspect_pattern_names),
            res.getStringArray(R.array.shaping_names),
            res.getStringArray(R.array.factor_names),
            res.getStringArray(R.array.hsystem_names),
            res.getStringArray(R.array.chart_type_names),
            res.getStringArray(R.array.hebrew_letter_names),
            res.getStringArray(R.array.tarot_arcana_names),
            res.getStringArray(R.array.tarot_minor_arcana_names),
        };
        String[] symbol_formats = res.getStringArray(R.array.symbol_formats);
        Symbol.setStringData(symbol_data,symbol_formats);

        Coordinate.setDirections(res.getString(R.string.coordinate_directions));
    }

	/*@SuppressWarnings("deprecation")
	public void setLanguage(String language) {
		Locale locale = new Locale(language);
		Locale.setDefault(locale);
		Configuration config = new Configuration();
		if(Build.VERSION.SDK_INT>=Build.VERSION_CODES.N) {
//			setSystemLocale(config,locale);
//			config.setLocale(locale);
			config.setLocales(new LocaleList(locale));
		} else {
//			setSystemLocaleLegacy(config,locale);
			config.locale = locale;
		}
		getApplicationContext().getResources().updateConfiguration(config,getResources().getDisplayMetrics());
	}*/

    public void shortToast(int id) { shortToast(getString(id)); }

    public void shortToast(String text) { Toast.makeText(this,text,Toast.LENGTH_SHORT).show(); }

    public void updateUserInterface() {
        if(drawerLayout!=null) {
            Log.d(APP,TAG+".updateUserInterface(google.signingIn: "+google.signingIn+", google.id: "+google.id+")");
            if(drawerSignIn!=null)
                drawerSignIn.setVisible(!google.signingIn && google.id==null);
            if(drawerSignOut!=null)
                drawerSignOut.setVisible(!google.signingIn && google.id!=null);
        }
    }

    public void showMessageBox(String title,String message) {
        AlertDialog.Builder dbld = new AlertDialog.Builder(this);
        if(title!=null) dbld.setTitle(title);
        dbld.setMessage(CustomHtml.fromHtml(message,CustomHtml.ALL,20.0f,null));
        dbld.setPositiveButton(getString(R.string.button_ok),null/*
			new DialogInterface.OnClickListener() {
				public void onClick(DialogInterface dialog,int button) {
					dialog.dismiss();
				}
			}*/);
        dbld.setCancelable(true);
        dbld.create().show();
    }

    public void hideKeyboard() {
        View view = this.getCurrentFocus();
        if(view!=null) {
            InputMethodManager imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(view.getWindowToken(),0);
        }
    }

    public void showLoading() {
        if(loadingPanel==null) return;
        getWindow().setFlags(
            WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE,
            WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE
                            );
        loadingPanel.setVisibility(View.VISIBLE);
    }

    public void hideLoading() {
        if(loadingPanel==null) return;
        loadingPanel.setVisibility(View.GONE);
        getWindow().clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);
    }

    public static String loadFile(Context context,String file) {
        String text = null;
        if(context!=null) {
            try {
                InputStream is = context.getAssets().open(file);
                int size = is.available();
                byte[] buffer = new byte[size];
                is.read(buffer);
                is.close();
                text = new String(buffer,"UTF-8");
            } catch(IOException e) {
                Log.e(APP,TAG+".loadAsset",e);
            }
        }
        return text;
    }

    @SuppressWarnings("deprecation")
    protected void setTextViewHTML(TextView text,String html,int customFlags,float symbolSize) {
        text.setText(CustomHtml.fromHtml(html,customFlags,symbolSize,this),TextView.BufferType.SPANNABLE);
        text.setMovementMethod(LinkMovementMethod.getInstance());
    }
}


//				Snackbar.make(mContentFrame,"Item One",Snackbar.LENGTH_SHORT).show();
//				mCurrentSelectedPosition = 0;


		/*editTime.addTextChangedListener(new TextWatcher() {
			private boolean react = true;
			public void afterTextChanged(Editable s) {}
			public void beforeTextChanged(CharSequence s,int start,int count,int after) {}
			public void onTextChanged(CharSequence s,int start,int before,int count) {
				if(!react || count==0) return;
				String text = editTime.getText().toString();
				int len = text.length();
				if(len==0) return;
				char c = text.charAt(len-1);
				if(c==':') return;
				if(len==2 || len==3 || len==5 || len==6 || len>8) {
					if(len>8) text = text.substring(0,8);
					else if(len==3 || len==5) text = text.substring(0,len-1)+':'+c;
					else text += ':';
					react = false;
					editTime.setText(text);
					editTime.setSelection(text.length());
					react = true;
				}
			}
		});*/

		/*editTime.setKeyListener(new TimeKeyListener() {
			public final char[] CHARS = new char[] { '0','1','2','3','4','5','6','7','8','9',':','.',',','-',' ' };

			@Override
			protected char[] getAcceptedChars() { return CHARS; }

			public CharSequence filter(CharSequence source,int start,int end,Spanned dest,int dstart,int dend) {
				final CharSequence superSource = super.filter(source,start,end,dest,dstart,dend);
				final CharSequence prefilteredSource = superSource!=null? superSource : source;
				return prefilteredSource.toString().replace('.',':');//All("[.,- ]+",":");
			}
		});*/

		/*{
			ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(
					this,R.array.month_array,android.R.layout.simple_spinner_item);
			adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
			spinnerMonth.setAdapter(adapter);
		}
		{
			List<String> list = new ArrayList<String>();
			for(int i=1; i<=31; ++i)
				list.add(String.format("%02d",i));
			ArrayAdapter<String> adapter = new ArrayAdapter<String>(
					this,android.R.layout.simple_spinner_item,list);
			adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
			spinnerDay.setAdapter(adapter);
		}*/


