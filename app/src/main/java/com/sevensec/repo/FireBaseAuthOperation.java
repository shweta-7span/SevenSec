package com.sevensec.repo;

import static com.sevensec.utils.Constants.DB_COLLECTION_APPS;
import static com.sevensec.utils.Constants.DB_COLLECTION_USERS;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.viewpager.widget.PagerAdapter;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.sevensec.activities.MainActivity;
import com.sevensec.repo.interfaces.AuthOperation;
import com.sevensec.utils.Dlog;

abstract public class FireBaseAuthOperation extends FireStoreDataOperation implements AuthOperation {

    @Override
    public void loginAnonymously(Context mContext, String deviceId) {
        mAuth.signInAnonymously().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                // Sign in success, update UI with the signed-in user's information
                Dlog.d("signInAnonymously: success");
                FirebaseUser user = mAuth.getCurrentUser();

                if (user != null) {
                    Dlog.d("signInAnonymously FirebaseUser: " + user.getUid());

                    addUserID(mContext, deviceId);
                }
            } else {
                // If sign in fails, display a message to the user.
                Dlog.w("signInAnonymously:failure: " + task.getException());
                Toast.makeText(mContext, "SignIn Failed. Try again.", Toast.LENGTH_LONG).show();
            }
        });
    }


    /*@Override
    public void logout() {
        mAuth.signOut();
        Dlog.d("signInAnonymously: logout");
    }*/
}
