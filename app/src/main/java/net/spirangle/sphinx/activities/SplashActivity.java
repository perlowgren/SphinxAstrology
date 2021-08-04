package net.spirangle.sphinx.activities;

import static net.spirangle.sphinx.config.SphinxProperties.*;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.ViewGroup.LayoutParams;
import android.widget.LinearLayout;

import androidx.appcompat.app.AppCompatActivity;

import net.spirangle.sphinx.astro.Horoscope;
import net.spirangle.sphinx.db.AstroDB;
import net.spirangle.sphinx.db.Database;
import net.spirangle.sphinx.db.Database.DatabaseListener;
import net.spirangle.sphinx.views.SplashView;


public class SplashActivity extends AppCompatActivity implements DatabaseListener, Runnable {
    private static final String TAG = "SplashActivity";

    private Intent intent = null;
    private SplashView splashView;
    private String installLabel = null;
    private float installProgress = 0.0f;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        LinearLayout ll;
        LayoutParams lp;
        ll = new LinearLayout(this);
        ll.setOrientation(LinearLayout.VERTICAL);
        lp = new LayoutParams(LayoutParams.MATCH_PARENT,LayoutParams.MATCH_PARENT);
        setContentView(ll,lp);
        lp = new LayoutParams(LayoutParams.MATCH_PARENT,LayoutParams.MATCH_PARENT);
        splashView = new SplashView(this);
        splashView.setLayoutParams(lp);
        ll.addView(splashView);

        Intent i = getIntent();
        Horoscope h = i.getParcelableExtra(EXTRA_RADIX1);
        int g = i.getIntExtra(EXTRA_GRAPH,-1);

        intent = new Intent(this,HoroscopeActivity.class);
        if(h!=null) intent.putExtra(EXTRA_RADIX1,h);
        if(g!=-1) intent.putExtra(EXTRA_GRAPH,g);

        new Thread(() -> {
            do {
                try {
                    Thread.sleep(50);
                } catch(Exception e) {}
                runOnUiThread(SplashActivity.this);
            } while(installProgress<1.0f);
        }).start();

        new Thread(() -> {
            // Make certain the AstroDB is initiated as db instance:
            AstroDB db = AstroDB.getInstance(SplashActivity.this,SplashActivity.this);
            long id = db.queryId(Database.TableDatabase.table,null);
            if(db.getProgress()>=1.0f)
                installProgress = 1.0f;
        }).start();
    }

	/*@Override
	public void onStart() {
		super.onStart();
	}*/

    @Override
    public void onDatabaseInstallProgress(String label,float progress) {
        Log.d(APP,TAG+".onDatabaseInstallProgress(label: "+label+", progress: "+progress+")");
        installLabel = label;
        installProgress = progress;
    }

    @Override
    public void run() {
        Log.d(APP,TAG+".run(label: "+installLabel+", progress: "+installProgress+")");
        if(installProgress>=1.0f) {
            if(intent!=null) {
                Log.d(APP,TAG+".run(start: HoroscopeActivity)");
                startActivity(intent);
                finish();
                intent = null;
            }
        } else {
            splashView.setProgress(installProgress,installLabel);
        }
    }
}

