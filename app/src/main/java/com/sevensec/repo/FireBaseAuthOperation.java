package com.sevensec.repo;
import android.content.Context;
import android.widget.Toast;
import com.google.firebase.auth.FirebaseUser;
import com.sevensec.helper.AuthFailureListener;
import com.sevensec.repo.interfaces.AuthOperation;
import com.sevensec.utils.Dlog;

abstract public class FireBaseAuthOperation extends FireStoreDataOperation implements AuthOperation {

    @Override
    public void loginAnonymously(Context mContext, String deviceId, AuthFailureListener authFailureListener) {
        mAuth.signInAnonymously().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                // Sign in success, update UI with the signed-in user's information
                Dlog.d("signInAnonymously: success");
                FirebaseUser user = mAuth.getCurrentUser();

                if (user != null) {
                    Dlog.d("signInAnonymously FirebaseUser: " + user.getUid());
                    addUserID(mContext, deviceId, authFailureListener);
                } else {
                    authFailureListener.authFail();
                }
            } else {
                // If sign in fails, display a message to the user.
                Dlog.w("signInAnonymously:failure: " + task.getException());
            }
        });
    }
}
