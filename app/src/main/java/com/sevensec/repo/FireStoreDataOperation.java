package com.sevensec.repo;

import static com.sevensec.utils.Constants.DB_APP_ALLOWED_TIME;
import static com.sevensec.utils.Constants.DB_APP_ATTEMPTS;
import static com.sevensec.utils.Constants.DB_APP_DATE_MAP;
import static com.sevensec.utils.Constants.DB_APP_LAST_ATTEMPT_TIME;
import static com.sevensec.utils.Constants.DB_APP_NAME;
import static com.sevensec.utils.Constants.DB_APP_TOTAL_TIME;
import static com.sevensec.utils.Constants.DB_COLLECTION_USERS;
import static com.sevensec.utils.Constants.DB_DEVICE_MAP;
import static com.sevensec.utils.Constants.DB_USER_EMAIL;
import static com.sevensec.utils.Constants.DB_USER_ID;
import static com.sevensec.utils.Constants.DB_USER_NAME;
import static com.sevensec.utils.Constants.PREF_IS_LOGIN;
import static com.sevensec.utils.Utils.getTimeInFormat;

import android.app.Activity;
import android.content.Intent;

import androidx.annotation.NonNull;

import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldPath;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.SetOptions;
import com.sevensec.activities.MainActivity;
import com.sevensec.helper.AuthFailureListener;
import com.sevensec.repo.interfaces.DataOperation;
import com.sevensec.repo.interfaces.SetAttemptLastOpenTime;
import com.sevensec.utils.Dlog;
import com.sevensec.utils.SharedPref;
import com.sevensec.utils.Utils;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class FireStoreDataOperation implements DataOperation {

    FirebaseFirestore firebaseFirestore;
    FirebaseAuth mAuth;
    String currentDate;

    SetAttemptLastOpenTime setAttemptLastOpenTime;

    public FireStoreDataOperation() {
        firebaseFirestore = FirebaseFirestore.getInstance();
        mAuth = FirebaseAuth.getInstance();

        currentDate = Utils.getCurrentDate();
    }

    public void setAttemptInterface(SetAttemptLastOpenTime setAttemptLastOpenTime) {
        this.setAttemptLastOpenTime = setAttemptLastOpenTime;
    }

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

    @Override
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
    public void checkAppAddedOrNot(String device_id, String app_name, String app_package) {

        FirebaseUser currentUser = mAuth.getCurrentUser();

        if (currentUser != null) {
            firebaseFirestore.collection(DB_COLLECTION_USERS).document(currentUser.getUid()).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                @Override
                public void onComplete(@NonNull Task<DocumentSnapshot> task) {

                    if (task.isSuccessful()) {
                        DocumentSnapshot document = task.getResult();

                        if (document.exists()) {
                            Dlog.d("checkAppAddedOrNot: Document exists!");

                            Map<String, Object> deviceMapData = (Map<String, Object>) document.get(DB_DEVICE_MAP);
                            Dlog.d("checkAppAddedOrNot deviceMapData: " + deviceMapData);

                            Map<String, Object> appMapData = (Map<String, Object>) deviceMapData.get(device_id);
                            Dlog.d("checkAppAddedOrNot appMapData: " + appMapData);

                            if (appMapData != null && appMapData.containsKey(app_package)) {
                                Dlog.d("checkAppAddedOrNot: " + app_package + " exists!");

                                Map<String, Object> packageMap = (Map<String, Object>) appMapData.get(app_package);
                                Dlog.d("checkAppAddedOrNot packageMap: " + packageMap);

                                Map<String, Object> datesMap = (Map<String, Object>) packageMap.get(DB_APP_DATE_MAP);
                                Dlog.d("checkAppAddedOrNot packageMap: " + packageMap);

                                Map<String, Object> currentDateMap = (Map<String, Object>) datesMap.get(currentDate);
                                Dlog.d("checkAppAddedOrNot currentDateMap: " + currentDateMap);

                                if (currentDateMap == null) {
                                    addUpdateDateMap(currentUser.getUid(), datesMap, device_id, app_package, 0, 0);

                                } else {
                                    getLastAttemptAndTime(currentUser.getUid(), datesMap, device_id, app_package, currentDateMap);
                                }

                            } else {
                                Dlog.d("checkAppAddedOrNot: " + app_package + " NOT exists!");
                                addAppDataWithCurrentDate(currentUser.getUid(), appMapData, device_id, app_name, app_package);
                            }

                        } else {
                            Dlog.d("checkAppAddedOrNot FireStore: Document does not exist!");
                        }
                    } else {
                        Dlog.d("checkAppAddedOrNot FireStore: Failed with: " + task.getException());
                    }
                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    Dlog.d("checkAppAddedOrNot onFailure: " + e.getMessage());
                }
            });
        }
    }

    private void getLastAttemptAndTime(String userUID, Map<String, Object> datesMap, String device_id, String app_package, Map<String, Object> currentDateMap) {

        long attempts = (long) currentDateMap.get(DB_APP_ATTEMPTS);
        Dlog.d("checkAppAddedOrNot attempts: " + attempts);

        long lastAttemptTime = (long) currentDateMap.get(DB_APP_LAST_ATTEMPT_TIME);
        long lastAttemptTimeDifference = Math.abs(lastAttemptTime - new Date().getTime());

        long lastAppUsageTime = (long) currentDateMap.get(DB_APP_TOTAL_TIME);

        //To show "Attempts" and "Last Used Time" in Warning screen
        setAttemptLastOpenTime.addAttemptAndTimeListener((int) (attempts + 1), getTimeInFormat(lastAttemptTimeDifference));

        addUpdateDateMap(userUID, datesMap, device_id, app_package, attempts, lastAppUsageTime);
    }

    @Override
    public void addAppDataWithCurrentDate(String userUID, Map<String, Object> appMapData, String device_id, String app_name, String app_package) {

        //To show "Attempts" and "Last Used Time" in Warning screen
        setAttemptLastOpenTime.addAttemptAndTimeListener(1, null);

        Map<String, Object> attemptMap = new HashMap<>();
        attemptMap.put(DB_APP_ATTEMPTS, 1);
        attemptMap.put(DB_APP_LAST_ATTEMPT_TIME, new Date().getTime());
        attemptMap.put(DB_APP_TOTAL_TIME, 0);

        Map<String, Object> currentDateMap = new HashMap<>();
        currentDateMap.put(currentDate, attemptMap);

        Map<String, Object> appElementMap = new HashMap<>();
        appElementMap.put(DB_APP_ALLOWED_TIME, 0);
        appElementMap.put(DB_APP_NAME, app_name);
        appElementMap.put(DB_APP_DATE_MAP, currentDateMap);

        Map<String, Object> appPackageMap = new HashMap<>();
        appPackageMap.put(app_package, appElementMap);

        // Get the "apps" map and add the new map
        if (appMapData != null) {
            appMapData.put(app_package, appElementMap);
        } else {
            appMapData = new HashMap<>();
            appMapData.put(app_package, appPackageMap);
        }

        Dlog.d("addAppDataWithAttempt appMapData: " + appMapData);

//        String fieldName = "device." + index;
//        Dlog.d("addApps fieldName: " + fieldName);

        firebaseFirestore.collection(DB_COLLECTION_USERS).document(userUID).update(DB_DEVICE_MAP + "." + device_id, appMapData).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                Dlog.d("addAppDataWithAttempt success");
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Dlog.e("addAppDataWithAttempt onFailure: " + e.getMessage());
            }
        });
    }

    @Override
    public void addUpdateDateMap(String userUID, Map<String, Object> datesMap, String device_id, String app_package, long attempts, long appUsageTime) {

        Map<String, Object> attemptMap = new HashMap<>();
        attemptMap.put(DB_APP_ATTEMPTS, attempts + 1);
        attemptMap.put(DB_APP_LAST_ATTEMPT_TIME, new Date().getTime());
        attemptMap.put(DB_APP_TOTAL_TIME, appUsageTime);

        datesMap.put(currentDate, attemptMap);

        Dlog.d("addApps datesMap: " + datesMap);

        firebaseFirestore.collection(DB_COLLECTION_USERS).document(userUID).update(FieldPath.of("device", device_id, app_package, "dates"), datesMap).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                Dlog.d("addDate onComplete");
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Dlog.d("addDate onFailure");
            }
        });
    }

    @Override
    public void addUserAuthData(Activity activity, FirebaseUser user, AuthFailureListener authFailureListener, GoogleSignInAccount googleSignInAccount) {

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

                Intent intent = new Intent(activity, MainActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                activity.startActivity(intent);

                activity.finish();
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

    @Override
    public void checkAppUsageForCurrentDate(String device_id, String app_package, long appUsageTotalTime) {

        FirebaseUser currentUser = mAuth.getCurrentUser();

        if (currentUser != null) {
            firebaseFirestore.collection(DB_COLLECTION_USERS).document(currentUser.getUid()).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                @Override
                public void onComplete(@NonNull Task<DocumentSnapshot> task) {

                    if (task.isSuccessful()) {
                        DocumentSnapshot document = task.getResult();

                        if (document.exists()) {
                            Dlog.d("checkAppAddedOrNot: Document exists!");

                            Map<String, Object> deviceMapData = (Map<String, Object>) document.get(DB_DEVICE_MAP);
                            Dlog.d("checkAppAddedOrNot deviceMapData: " + deviceMapData);

                            Map<String, Object> appMapData = (Map<String, Object>) deviceMapData.get(device_id);
                            Dlog.d("checkAppAddedOrNot appMapData: " + appMapData);

                            if (appMapData != null && appMapData.containsKey(app_package)) {
                                Dlog.d("checkAppAddedOrNot: " + app_package + " exists!");

                                Map<String, Object> packageMap = (Map<String, Object>) appMapData.get(app_package);
                                Dlog.d("checkAppAddedOrNot packageMap: " + packageMap);

                                Map<String, Object> datesMap = (Map<String, Object>) packageMap.get(DB_APP_DATE_MAP);
                                Dlog.d("checkAppAddedOrNot packageMap: " + packageMap);

                                Map<String, Object> currentDateMap = (Map<String, Object>) datesMap.get(currentDate);
                                Dlog.d("checkAppAddedOrNot currentDateMap: " + currentDateMap);

                                if (currentDateMap == null) {
//                                    addUpdateDateMap(currentUser.getUid(), datesMap, device_id, app_package, 0);

                                } else {
                                    updateAppUsageTime(currentUser.getUid(), datesMap, device_id, app_package, currentDateMap, appUsageTotalTime);
                                }

                            } else {
                                Dlog.d("checkAppAddedOrNot: " + app_package + " NOT exists!");
                            }

                        } else {
                            Dlog.d("checkAppAddedOrNot FireStore: Document does not exist!");
                        }
                    } else {
                        Dlog.d("checkAppAddedOrNot FireStore: Failed with: " + task.getException());
                    }
                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    Dlog.d("checkAppAddedOrNot onFailure: " + e.getMessage());
                }
            });
        }
    }

    private void updateAppUsageTime(String userUID, Map<String, Object> datesMap, String device_id, String app_package, Map<String, Object> currentDateMap, long appUsageTotalTime) {

        long attempts = (long) currentDateMap.get(DB_APP_ATTEMPTS);
        Dlog.d("checkAppAddedOrNot attempts: " + attempts);

        long lastAttemptTime = (long) currentDateMap.get(DB_APP_LAST_ATTEMPT_TIME);

//        long lastAppUsageTime = (long) currentDateMap.get(DB_APP_TOTAL_TIME);
//        long totalAppUsageTime = lastAppUsageTime + appUsageTotalTime;

        Map<String, Object> attemptMap = new HashMap<>();
        attemptMap.put(DB_APP_ATTEMPTS, attempts);
        attemptMap.put(DB_APP_LAST_ATTEMPT_TIME, lastAttemptTime);
        attemptMap.put(DB_APP_TOTAL_TIME, appUsageTotalTime);

        datesMap.put(currentDate, attemptMap);

        Dlog.d("getAppUsageTime datesMap: " + datesMap);

        firebaseFirestore.collection(DB_COLLECTION_USERS).document(userUID).update(FieldPath.of("device", device_id, app_package, "dates"), datesMap).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                Dlog.d("updateAppUsageTime onComplete");
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Dlog.d("updateAppUsageTime onFailure");
            }
        });
    }
}
