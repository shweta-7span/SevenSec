package com.sevensec.repo;

import static com.sevensec.utils.Constants.DB_ANDROID;
import static com.sevensec.utils.Constants.DB_COLLECTION_APPS;
import static com.sevensec.utils.Constants.DB_COLLECTION_USERS;
import static com.sevensec.utils.Constants.DB_DEVICE_MAP;
import static com.sevensec.utils.Constants.DB_DOCUMENT_KEY_APP_ATTEMPTS;
import static com.sevensec.utils.Constants.DB_DOCUMENT_KEY_APP_NAME;
import static com.sevensec.utils.Constants.DB_DOCUMENT_KEY_APP_PACKAGE;
import static com.sevensec.utils.Constants.DB_DOCUMENT_KEY_TYPE;
import static com.sevensec.utils.Constants.DB_USER_EMAIL;
import static com.sevensec.utils.Constants.DB_USER_ID;
import static com.sevensec.utils.Constants.DB_USER_NAME;
import static com.sevensec.utils.Constants.PREF_IS_LOGIN;
import static com.sevensec.utils.Utils.check24Hour;
import static com.sevensec.utils.Utils.getTimeInFormat;

import android.content.Intent;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldPath;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.firestore.SetOptions;
import com.sevensec.activities.MainActivity;
import com.sevensec.helper.AuthFailureListener;
import com.sevensec.repo.interfaces.DataOperation;
import com.sevensec.utils.Dlog;
import com.sevensec.utils.SharedPref;
import com.sevensec.utils.Utils;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public abstract class FireStoreDataOperation extends AppCompatActivity implements DataOperation {

    FirebaseFirestore firebaseFirestore = FirebaseFirestore.getInstance();

    FirebaseAuth mAuth = FirebaseAuth.getInstance();

    @Override
    public void checkDeviceIsStored(String deviceId) {

        FirebaseUser currentUser = mAuth.getCurrentUser();

        if (currentUser != null) {
            firebaseFirestore.collection(DB_COLLECTION_USERS).document(currentUser.getUid()).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                @Override
                public void onComplete(@NonNull Task<DocumentSnapshot> task) {

                    if (task.isSuccessful()) {
                        DocumentSnapshot document = task.getResult();
                        if (document.exists()) {
                            Dlog.d("checkDeviceIsStored: Document exists!");

                            Map<String, Object> deviceMapData = (Map<String, Object>) document.get(DB_DEVICE_MAP);
                            Dlog.d("checkDeviceIsStored deviceMapData: " + deviceMapData);

                            if (deviceMapData == null) {
                                updateDevice(currentUser.getUid(), deviceId);

                            } else {
                                if (deviceMapData.containsKey(deviceId)) {
                                    Dlog.d("checkDeviceIsStored: Already added this device_id: " + deviceId);

                                } else {
                                    Dlog.e("checkDeviceIsStored: Not added this device_id!");
                                    //add map of "apps" in this index
                                    updateDevice(currentUser.getUid(), deviceId);
                                }
                            }

                        } else {
                            Dlog.d("checkDeviceIsStored: Document does not exist!");
                        }
                    } else {
                        Dlog.d("checkDeviceIsStored: Failed with: " + task.getException());
                    }

                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    Dlog.e("checkDeviceIsStored onFailure: " + e.getMessage());
                }
            });
        }
    }

    public void updateDevice(String userUID, String device_id) {

        Map<String, Object> deviceElementMap = new HashMap<>();
        deviceElementMap.put(device_id, new HashMap<>());

        Map<String, Object> deviceMap = new HashMap<>();
        deviceMap.put(DB_DEVICE_MAP, deviceElementMap);

        Dlog.d("updateDevice deviceMap: " + deviceMap);

        //Used `SetOptions.merge()` to update the map with existing data.
        //The new map of "DeviceID" will be add as new key in the "device" map and already added key will be remain as it is.
        firebaseFirestore.collection(DB_COLLECTION_USERS).document(userUID).set(deviceMap, SetOptions.merge()).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                Dlog.d("updateDevice: addDevice success");
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Dlog.e("updateDevice: addDevice onFailure: " + e.getMessage());
            }
        });
    }

    @Override
    public void checkAppAddedOrNot(String deviceId, String appLabel, String lastAppPackage) {
        Dlog.d("App Label -- " + appLabel);
        //Check App is already Added OR Not
        firebaseFirestore.collection(DB_COLLECTION_USERS).document(deviceId).collection(DB_COLLECTION_APPS).document(lastAppPackage).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()) {
                    DocumentSnapshot document = task.getResult();
                    if (document.exists()) {
                        Dlog.d("FireStore: Document exists!");
                        getLastAttemptAndTime(deviceId, appLabel, lastAppPackage, document);
                    } else {
                        Dlog.d("FireStore: Document does not exist!");
                        addAppDataWithAttempt(deviceId, appLabel, lastAppPackage, 0, null);
                    }
                } else {
                    Dlog.d("FireStore: Failed with: " + task.getException());
                }
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Dlog.w("FireStore: checkAppAddedOrNot Error: " + e);
            }
        });
    }

    private void getLastAttemptAndTime(String deviceId, String appLabel, String lastAppPackage, DocumentSnapshot document) {
        List<Long> timeList = (List<Long>) document.get(DB_DOCUMENT_KEY_APP_ATTEMPTS);

        int attemptCount = 0;
        String lastUsedTime = null;

        if (timeList != null) {
            long lastUsedDifference = Math.abs(timeList.get(timeList.size() - 1) - (new Date().getTime()));
            lastUsedTime = getTimeInFormat(lastUsedDifference);

            for (Long timeStamp : timeList) {
                Dlog.v("FireStore: getLastAttemptAndTime: " + timeStamp);
                if (check24Hour(timeStamp)) {
                    removeTimeFromArray(deviceId, lastAppPackage, timeStamp);
                } else {
                    attemptCount++;
                }
            }
        }
        addAppDataWithAttempt(deviceId, appLabel, lastAppPackage, attemptCount, lastUsedTime);
    }

    @Override
    public void addAppDataWithAttempt(String deviceId, String appLabel, String lastAppPackage, int attempt, String lastUsedTime) {
        Dlog.i("FireStore: addAppDataWithAttempt attempt: " + attempt);
        setAttempt(attempt + 1, lastUsedTime);

        Map<String, Object> apps = new HashMap<>();
        apps.put(DB_DOCUMENT_KEY_APP_NAME, appLabel);
        apps.put(DB_DOCUMENT_KEY_APP_PACKAGE, lastAppPackage);
        apps.put(DB_DOCUMENT_KEY_APP_ATTEMPTS, FieldValue.arrayUnion(new Date().getTime()));

        // Add a new document with above fields
        firebaseFirestore.collection(DB_COLLECTION_USERS).document(deviceId).collection(DB_COLLECTION_APPS).document(lastAppPackage)
                .set(apps, SetOptions.merge())
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void unused) {
                        Dlog.d("FireStore: Apps successfully written!");
                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Dlog.w("FireStore: Error adding App: " + e);
                    }
                });
    }

    public void setAttempt(int i, String lastUsedTime) {
    }

    @Override
    public void removeTimeFromArray(String deviceId, String lastAppPackage, long timeStamp) {
        Map<String, Object> apps = new HashMap<>();
        apps.put(DB_DOCUMENT_KEY_APP_ATTEMPTS, FieldValue.arrayRemove(timeStamp));

        // Remove timeStamp from Array
        firebaseFirestore.collection(DB_COLLECTION_USERS).document(deviceId).collection(DB_COLLECTION_APPS).document(lastAppPackage)
                .update(apps)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void unused) {
                        Dlog.d("FireStore: TimeStamp successfully removed!");
                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Dlog.w("FireStore: Error adding App: " + e);
                    }
                });
    }

    @Override
    public void addUserAuthData(FirebaseUser user, AuthFailureListener authFailureListener, GoogleSignInAccount googleSignInAccount) {

        Map<String, Object> userMap = new HashMap<>();
        userMap.put(DB_USER_ID, Objects.requireNonNull(user.getUid()));

        if (googleSignInAccount != null) {
            userMap.put(DB_USER_NAME, googleSignInAccount.getDisplayName());
            userMap.put(DB_USER_EMAIL, googleSignInAccount.getEmail());

            Utils.storeGoogleAuthDataInPreference(googleSignInAccount);
        }

        Dlog.d("addUserAuthData userMap: " + userMap);

        firebaseFirestore.collection(DB_COLLECTION_USERS).document(user.getUid()).set(userMap).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                Dlog.d("addUserAuthData onComplete UID: " + user.getUid());

                Dlog.d("addUserAuthData: UserID successfully added!");
                SharedPref.writeBoolean(PREF_IS_LOGIN, true);

                Intent intent = new Intent(getApplicationContext(), MainActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(intent);

                finish();
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Dlog.w("addUserAuthData: Error adding UserID: " + e.getMessage());
                authFailureListener.authFail();
            }
        });
    }

    @Override
    public void updateUserAuthData(GoogleSignInAccount googleSignInAccount) {

        FirebaseUser user = mAuth.getCurrentUser();

        if (user != null) {
            //Save the data in Preference here to show the image and name in settings screen when login screen closed
            Utils.storeGoogleAuthDataInPreference(googleSignInAccount);

            firebaseFirestore.collection(DB_COLLECTION_USERS).document(user.getUid()).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                @Override
                public void onComplete(@NonNull Task<DocumentSnapshot> task) {

                    if (task.isSuccessful()) {
                        DocumentSnapshot document = task.getResult();
                        if (document.exists()) {
                            Dlog.d("updateUserAuthData: Document exists!");

                            Map<String, Object> deviceMapData = (Map<String, Object>) document.get(DB_DEVICE_MAP);
                            Dlog.d("updateUserAuthData: deviceMapData: " + deviceMapData);

                            if (deviceMapData != null) {

                                Map<String, Object> deviceMap = new HashMap<>();
                                deviceMap.put(DB_DEVICE_MAP, deviceMapData);
                                deviceMap.put(DB_USER_NAME, googleSignInAccount.getDisplayName());
                                deviceMap.put(DB_USER_EMAIL, googleSignInAccount.getEmail());
                                deviceMap.put(DB_USER_ID, user.getUid());

                                Dlog.d("updateUserAuthData: deviceMap: " + deviceMap);

                                firebaseFirestore.collection(DB_COLLECTION_USERS).document(user.getUid()).update(deviceMap).addOnCompleteListener(new OnCompleteListener<Void>() {
                                    @Override
                                    public void onComplete(@NonNull Task<Void> task) {
                                        Dlog.d("updateUserAuthData: onComplete");
                                    }
                                }).addOnFailureListener(new OnFailureListener() {
                                    @Override
                                    public void onFailure(@NonNull Exception e) {
                                        Dlog.d("updateUserAuthData: onFailure");
                                    }
                                });
                            }

                        } else {
                            Dlog.d("updateUserAuthData: Document does not exist!");
                        }
                    } else {
                        Dlog.d("updateUserAuthData: Failed with: " + task.getException());
                    }
                }
            });
        }
    }
}
