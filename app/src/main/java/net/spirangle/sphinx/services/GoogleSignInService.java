package net.spirangle.sphinx.services;

import static net.spirangle.sphinx.config.SphinxProperties.*;

import android.content.Intent;
import android.util.Log;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.Task;

import net.spirangle.sphinx.activities.BasicActivity;

import java.util.Objects;

public class GoogleSignInService {
    private static final String TAG = "GoogleSignInService";

    public GoogleSignInClient client = null;
    public String id;
    public String tokenId;
    public boolean signingIn;

    public GoogleSignInService() {
        clear();
    }

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
