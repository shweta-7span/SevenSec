package com.sevensec.repo.interfaces;

import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.firebase.auth.FirebaseUser;
import com.sevensec.helper.AuthFailureListener;

public interface DataOperation {

    void checkDeviceIsStored(String deviceId);

    void updateDevice(String userUID, String device_id);

    void checkAppAddedOrNot(String deviceId, String appLabel, String lastAppPackage);

    void addAppDataWithAttempt(String deviceId, String appLabel, String lastAppPackage, int attempt, String lastUsedTime);

    void removeTimeFromArray(String deviceId, String appLabel, long timeStamp);

    void addUserAuthData(FirebaseUser user, AuthFailureListener authFailureListener, GoogleSignInAccount googleSignInAccount);

    void updateUserAuthData(GoogleSignInAccount googleSignInAccount);
}
