package net.spirangle.sphinx.services;

import static android.content.pm.PackageManager.PERMISSION_GRANTED;
import static net.spirangle.sphinx.config.SphinxProperties.SERVICE_LOCATION;

import android.app.Activity;
import android.content.Context;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;

import com.google.android.material.snackbar.Snackbar;

import net.spirangle.sphinx.R;

import java.util.HashSet;
import java.util.Set;

public class PermissionService {

    private static PermissionService instance;

    public static PermissionService getInstance() {
        if(instance==null) instance = new PermissionService();
        return instance;
    }

    private PermissionService() {
    }

    public boolean requestPermissions(Activity activity,int requestCode,String... permissions) {
        Set<String> required = null;
        for(String permission : permissions)
            if(!hasPermission(activity,permission)) {
                if(required==null) required = new HashSet<>();
                required.add(permission);
            }
        if(required==null) return true;
        Set<String> request = new HashSet<>();
        for(String permission : required)
            if(ActivityCompat.shouldShowRequestPermissionRationale(activity,permission))
                request.add(permission);
        if(!request.isEmpty()) {
            View focus = activity.getCurrentFocus();
            if(focus!=null) {
                String[] arr = request.toArray(new String[0]);
                String text = activity.getResources().getQuantityString(R.plurals.request_permissions,arr.length);
                Snackbar.make(focus,text,Snackbar.LENGTH_INDEFINITE)
                        .setAction(R.string.cancel,view -> {})
                        .setAction(R.string.ok,view -> ActivityCompat.requestPermissions(activity,arr,requestCode))
                        .show();
            }
        } else {
            String[] arr = required.toArray(new String[0]);
            ActivityCompat.requestPermissions(activity,arr,requestCode);
        }
        return false;
    }

    public boolean hasAllPermissions(Context context,String... permissions) {
        for(String permission : permissions)
            if(!hasPermission(context,permission))
                return false;
        return true;
    }

    public boolean hasAnyPermission(Context context,String... permissions) {
        for(String permission : permissions)
            if(hasPermission(context,permission))
                return true;
        return false;
    }

    public boolean hasPermission(Context context,String permission) {
        return ActivityCompat.checkSelfPermission(context,permission)==PERMISSION_GRANTED;
    }

    public void onRequestPermissionsResult(Activity activity,int requestCode,@NonNull String[] permissions,@NonNull int[] grantResults) {
        boolean any = false;
        boolean all = true;
        for(int i=0; i<grantResults.length; ++i)
            if(grantResults[i]==PERMISSION_GRANTED) any = true;
            else all = false;
        switch(requestCode) {
            case SERVICE_LOCATION:
                if(any) LocationService.getInstance().requestLocationUpdates(activity,null);
                break;
        }
    }
}
