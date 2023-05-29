package com.sevensec.repo.interfaces;

import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.firebase.auth.FirebaseUser;
import com.sevensec.helper.AuthFailureListener;

import java.util.Map;

public interface DataOperation {

    void checkDeviceIsStored(String deviceId);

    void updateDevice(String userUID, String device_id);

    void checkAppAddedOrNot(String deviceId, String appLabel, String lastAppPackage);

    void addAppDataWithCurrentDate(String userUID, Map<String, Object> appMapData, String device_id, String app_name, String app_package);

    void removeTimeFromArray(String deviceId, String appLabel, long timeStamp);

    void addUserAuthData(FirebaseUser user, AuthFailureListener authFailureListener, GoogleSignInAccount googleSignInAccount);

    void updateUserAuthData(GoogleSignInAccount googleSignInAccount);
}
