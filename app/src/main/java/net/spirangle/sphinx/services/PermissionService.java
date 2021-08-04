package net.spirangle.sphinx.services;

import android.app.Activity;
import android.content.pm.PackageManager;
import android.view.View;

import androidx.core.app.ActivityCompat;

import com.google.android.material.snackbar.Snackbar;

import java.util.HashSet;
import java.util.Set;

public class PermissionService {

    public static boolean requestPermissions(Activity activity,int requestCode,String... permissions) {
        Set<String> p1 = null;
        for(String permission : permissions)
            if(!hasPermission(activity, permission)) {
                if(p1==null) p1 = new HashSet<>();
                p1.add(permission);
            }
        if(p1==null) return true;
        Set<String> p2 = new HashSet<>();
        for(String permission : p1)
            if(ActivityCompat.shouldShowRequestPermissionRationale(activity,permission)) {
                p2.add(permission);
            }
        if(!p2.isEmpty()) {
            View focus = activity.getCurrentFocus();
            if(focus!=null) {
                Snackbar.make(focus,"R.string.permission_text",Snackbar.LENGTH_INDEFINITE)
                        .setAction("R.string.ok",
                                   view -> ActivityCompat.requestPermissions(activity,p2.toArray(new String[0]),requestCode))
                        .show();
            }
            return false;
        }
        ActivityCompat.requestPermissions(activity,p1.toArray(new String[0]),requestCode);
        return true;
    }

    public static boolean hasPermissions(Activity activity,String... permissions) {
        for(String permission : permissions)
            if(!hasPermission(activity, permission))
                return false;
        return true;
    }

    public static boolean hasPermission(Activity activity,String permission) {
        return ActivityCompat.checkSelfPermission(activity,permission)==PackageManager.PERMISSION_GRANTED;
    }
}
