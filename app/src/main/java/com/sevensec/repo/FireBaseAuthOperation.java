package com.sevensec.repo;

import static com.sevensec.utils.Constants.PREF_GOOGLE_AUTH_USER_NAME;
import static com.sevensec.utils.Constants.PREF_GOOGLE_AUTH_USER_PIC;
import static com.sevensec.utils.Constants.PREF_IS_GOOGLE_LOGIN_DONE;
import static com.sevensec.utils.Constants.PREF_IS_LOGIN;

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
import com.sevensec.activities.LoginActivity;
import com.sevensec.helper.AuthFailureListener;
import com.sevensec.repo.interfaces.AuthOperation;
import com.sevensec.utils.Dlog;
import com.sevensec.utils.SharedPref;

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
                    addUserAuthData(user, authFailureListener, null);
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
        Intent signInIntent = getGoogleSignInClient().getSignInIntent();
        startActivityIntent.launch(signInIntent);
    }

    public void checkFirebaseUSer(Activity activity, String deviceId, Task<GoogleSignInAccount> task, AuthFailureListener authFailureListener) {

        try {
            // Google Sign In was successful, authenticate with Firebase
            GoogleSignInAccount account = task.getResult(ApiException.class);
            AuthCredential credential = GoogleAuthProvider.getCredential(account.getIdToken(), null);

            Dlog.d("checkFirebaseUSer account.getIdToken(): " + account.getIdToken());
            FirebaseUser currentUser = mAuth.getCurrentUser();

            if (currentUser == null) {
                Dlog.e("checkFirebaseUSer currentUser Null");
                signInWithGoogle(credential, task, authFailureListener, account);
            } else {
                linkWithGoogleAccount(currentUser, credential, activity, task, authFailureListener, account);
            }

        } catch (ApiException e) {
            Dlog.e("checkFirebaseUSer ApiException: " + e.getMessage());
            throw new RuntimeException(e);
        }
    }

    //It will call, when user do the "GoogleSignIn" directly
    private void signInWithGoogle(AuthCredential credential, Task<GoogleSignInAccount> task, AuthFailureListener authFailureListener, GoogleSignInAccount googleSignInAccount) {
        mAuth.signInWithCredential(credential).addOnCompleteListener(task12 -> {
            if (task.isSuccessful()) {
                // Sign in success, update UI with the signed-in user's information
                FirebaseUser user = mAuth.getCurrentUser();
                if (user != null) {
                    Dlog.d("signInWithCredential FirebaseUser: " + user.getUid());
                    addUserAuthData(user, authFailureListener, googleSignInAccount);
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

    //It will call If user already SignIn "Anonymously" and then do the "GoogleSignIn"
    private void linkWithGoogleAccount(FirebaseUser currentUser, AuthCredential credential, Activity activity, Task<GoogleSignInAccount> task, AuthFailureListener authFailureListener, GoogleSignInAccount googleSignInAccount) {
        currentUser.linkWithCredential(credential).addOnCompleteListener(activity, task1 -> {
            if (task.isSuccessful()) {
                // Sign in success, update UI with the signed-in user's information
                FirebaseUser user = mAuth.getCurrentUser();
                if (user != null) {
                    Dlog.d("linkWithGoogleAuth FirebaseUser: " + user.getUid());

                    updateUserAuthData(googleSignInAccount);
                    finish();
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

    private void clearGoogleAuthData() {
        SharedPref.clear(PREF_IS_LOGIN);
        SharedPref.clear(PREF_IS_GOOGLE_LOGIN_DONE);
        SharedPref.clear(PREF_GOOGLE_AUTH_USER_NAME);
        SharedPref.clear(PREF_GOOGLE_AUTH_USER_PIC);
    }

    GoogleSignInClient getGoogleSignInClient() {
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();

        return GoogleSignIn.getClient(this, gso);
    }

    public void logout(Activity activity) {
        Dlog.d("Firebase logout");
        mAuth.signOut();

        if (mAuth.getCurrentUser() == null) {
            getGoogleSignInClient().revokeAccess().addOnCompleteListener(activity, task -> {

                clearGoogleAuthData();
                Intent intent = new Intent(activity, LoginActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(intent);
                Dlog.d("Firebase logout Success");

            });

        } else {
            Dlog.e("Firebase logout Failed");
        }
    }
}
