package com.sevensec.repo;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;

import androidx.activity.result.ActivityResultLauncher;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;
import com.sevensec.R;
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

    public void showGoogleAccounts(ActivityResultLauncher<Intent> startActivityIntent) {
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();

        GoogleSignInClient mGoogleSignInClient = GoogleSignIn.getClient(this, gso);

        Intent signInIntent = mGoogleSignInClient.getSignInIntent();
        startActivityIntent.launch(signInIntent);
    }

    public void checkFirebaseUSer(Activity activity, String deviceId, Task<GoogleSignInAccount> task, AuthFailureListener authFailureListener) {

        try {
            // Google Sign In was successful, authenticate with Firebase
            GoogleSignInAccount account = task.getResult(ApiException.class);
            AuthCredential credential = GoogleAuthProvider.getCredential(account.getIdToken(), null);

            Dlog.d("linkWithGoogleAuth account.getIdToken(): " + account.getIdToken());
            FirebaseUser currentUser = mAuth.getCurrentUser();

            if (currentUser == null) {
                Dlog.e("linkWithGoogleAuth currentUser Null");
                signInWithGoogle(credential, activity, deviceId, task, authFailureListener);
            } else {
                linkWithGoogleAccount(currentUser, credential, activity, task, authFailureListener);
            }

        } catch (ApiException e) {
            Dlog.e("linkWithGoogleAuth ApiException: " + e.getMessage());
            throw new RuntimeException(e);
        }
    }

    private void signInWithGoogle(AuthCredential credential, Activity activity, String deviceId, Task<GoogleSignInAccount> task, AuthFailureListener authFailureListener) {
        mAuth.signInWithCredential(credential).addOnCompleteListener(task12 -> {
            if (task.isSuccessful()) {
                // Sign in success, update UI with the signed-in user's information
                FirebaseUser user = mAuth.getCurrentUser();
                if (user != null) {
                    Dlog.d("signInWithCredential FirebaseUser: " + user.getUid());
                    addUserID(activity, deviceId, authFailureListener);
                } else {
                    Dlog.w("signInWithCredential user Null");
                    authFailureListener.authFail();
                }
            } else {
                // If sign in fails, display a message to the user.
                Dlog.e("signInWithCredential:failure: " + task.getException());
            }
        });
    }

    private void linkWithGoogleAccount(FirebaseUser currentUser, AuthCredential credential, Activity activity, Task<GoogleSignInAccount> task, AuthFailureListener authFailureListener) {
        currentUser.linkWithCredential(credential).addOnCompleteListener(activity, task1 -> {
            if (task.isSuccessful()) {
                // Sign in success, update UI with the signed-in user's information
                FirebaseUser user = mAuth.getCurrentUser();
                if (user != null) {
                    Dlog.d("linkWithGoogleAuth FirebaseUser: " + user.getUid());
                } else {
                    Dlog.w("linkWithGoogleAuth user Null");
                    authFailureListener.authFail();
                }
            } else {
                // If sign in fails, display a message to the user.
                Dlog.e("linkWithCredential:failure" + task.getException());
            }
        });
    }
}
