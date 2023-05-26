package com.sevensec.repo;

import static com.sevensec.utils.Constants.DB_ANDROID;
import static com.sevensec.utils.Constants.DB_COLLECTION_APPS;
import static com.sevensec.utils.Constants.DB_COLLECTION_USERS;
import static com.sevensec.utils.Constants.DB_DOCUMENT_KEY_APP_ATTEMPTS;
import static com.sevensec.utils.Constants.DB_DOCUMENT_KEY_APP_NAME;
import static com.sevensec.utils.Constants.DB_DOCUMENT_KEY_APP_PACKAGE;
import static com.sevensec.utils.Constants.DB_DOCUMENT_KEY_TYPE;
import static com.sevensec.utils.Constants.DB_USER_ID;
import static com.sevensec.utils.Constants.PREF_IS_LOGIN;
import static com.sevensec.utils.Utils.check24Hour;
import static com.sevensec.utils.Utils.getTimeInFormat;

import android.content.Context;
import android.content.Intent;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
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

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;

public abstract class FireStoreDataOperation extends AppCompatActivity implements DataOperation {

    FirebaseFirestore firebaseFirestore = FirebaseFirestore.getInstance();

    FirebaseAuth mAuth = FirebaseAuth.getInstance();

    @Override
    public void checkDeviceIsStored(String deviceId) {
        firebaseFirestore.collection(DB_COLLECTION_USERS).get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {

                Dlog.d("FireStore: document Size: " + task.getResult().size());

                if (task.isSuccessful()) {
                    if (task.getResult().size() > 0) {
                        for (DocumentSnapshot document : task.getResult()) {
                            //Dlog.d("FireStore: document: " + document.get(DB_DOCUMENT_KEY_USER));
                            Dlog.d("FireStore: document: " + document.getId());

                            if (Objects.equals(document.getId(), deviceId)) {
                                Dlog.d("FireStore: DEVICE_ID already exists");
                            } else {
                                Dlog.e("FireStore: DEVICE_ID NOT exists");
                            }
                        }
                    } else {
                        Dlog.e("FireStore: Collection Not exists");
                        addUserOnFireStore(deviceId);
                    }
                } else {
                    Dlog.e("FireStore: task NOT successful");
                }
            }
        });
    }

    @Override
    public void addUserOnFireStore(String deviceId) {
        // Create a new user with a first and last name
        Map<String, Object> type = new HashMap<>();
        type.put(DB_DOCUMENT_KEY_TYPE, DB_ANDROID);

        // Add a new document with a generated ID
        firebaseFirestore.collection(DB_COLLECTION_USERS).document(deviceId)
                .set(type)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void unused) {
                        Dlog.d("FireStore: DocumentSnapshot successfully written!");
                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Dlog.w("FireStore: Error adding document: " + e.getMessage());
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

    public void addUserID(Context mContext, String deviceId, AuthFailureListener authFailureListener) {

        Map<String, Object> userID = new HashMap<>();
        userID.put(DB_USER_ID, Objects.requireNonNull(mAuth.getCurrentUser()).getUid());

        firebaseFirestore.collection(DB_COLLECTION_USERS).document(deviceId).set(userID).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                Dlog.d("FireStore: UserID successfully added!");
                SharedPref.writeBoolean(PREF_IS_LOGIN, true);

                Intent intent = new Intent(getApplicationContext(), MainActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(intent);

                finish();
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Dlog.w("FireStore: Error adding Anonymous UserID: " + e.getMessage());
                authFailureListener.authFail();
            }
        });
    }

    public void getDates() {
        firebaseFirestore.collection("users").document("N0DaCZPavlU6asSJoDKmysnQSWn1").collection("device").document("234634345232234").collection("apps").document("com.artiumacademy").get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {

                Dlog.d("Result: " + task);

                if (task.isSuccessful()) {
                    DocumentSnapshot document = task.getResult();
                    if (document.exists()) {
                        Dlog.d("FireStore: Document exists!");
                        Map<String, Map<String, Integer>> dateList = (Map<String, Map<String, Integer>>) document.get("dates");
                        Dlog.d("dateList: " + dateList);

                        SimpleDateFormat df = new SimpleDateFormat("dd-MM-yyyy", Locale.getDefault());
                        String formattedDate = df.format(Calendar.getInstance().getTime());

                        Map<String, Integer> app_usage = dateList.get(formattedDate);
                        Dlog.i("time Spent: " + app_usage.get("total_time_spent"));
                    } else {
                        Dlog.d("FireStore: Document does not exist!");
                    }
                } else {
                    Dlog.e("FireStore: Failed with: " + task.getException());
                }

            }
        });
    }

//    public String collectionName = "myUsers";
    public String collectionName = "myData";
    public String User_UID = FirebaseAuth.getInstance().getUid();
    SimpleDateFormat df = new SimpleDateFormat("dd-MM-yyyy", Locale.getDefault());
    String currentDate = df.format(Calendar.getInstance().getTime());
//    String currentDate = "27-05-2023";

    Map<String, Object> deviceMapData;

    public void addUserNew(String UID) {

        Map<String, Object> userID = new HashMap<>();
        userID.put("user_id", Objects.requireNonNull(UID));

        firebaseFirestore.collection(collectionName).document(UID).set(userID).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                Dlog.d("addUserNew Success: " + UID);

                SharedPref.writeBoolean(PREF_IS_LOGIN, true);

                Intent intent = new Intent(getApplicationContext(), MainActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(intent);

                finish();
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Dlog.e("addUserNew onFailure: " + e.getMessage());
            }
        });
    }

    public void checkDeviceIsStoredNew(String device_id) {

        Dlog.d("checkDeviceIsStoredNew");

        firebaseFirestore.collection(collectionName).document(User_UID).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {

                if (task.isSuccessful()) {
                    DocumentSnapshot document = task.getResult();
                    if (document.exists()) {
                        Dlog.d("checkDeviceIsStoredNew FireStore: Document exists!");

                        Map<String, Object> deviceMapData = (Map<String, Object>) document.get("device");
                        Dlog.d("checkDeviceIsStoredNew deviceMapData: " + deviceMapData);

                        if (deviceMapData == null) {
                            updateDevice(device_id);

                        } else {
                            if (deviceMapData.containsKey(device_id)) {
                                Dlog.d("checkDeviceIsStoredNew: Already added this device_id: " + device_id);

                            } else {
                                Dlog.e("checkDeviceIsStoredNew: Not added this device_id!");
                                //add map of "apps" in this index
                                updateDevice(device_id);
                            }
                        }

                        //----------When device is array----------
                        /*List<Map<String, Object>> deviceData = (List<Map<String, Object>>) document.get("device");

                        if (deviceData == null) {
                            Dlog.d("checkDeviceIsStoredNew: deviceData NULL");
                            updateDevice(device_id);

                        } else {
                            Dlog.d("checkDeviceIsStoredNew: deviceData: " + deviceData);

//                        for (int i = 0; i < deviceData.size(); i++) {
//
//                                Map<String, Object> deviceMap = deviceData.get(i);
//                                Dlog.d("checkDeviceIsStoredNew deviceMap: " + deviceMap);
//
//                                if (deviceMap.get("device_id").equals(device_id)) {
//                                    Dlog.d("checkDeviceIsStoredNew: Already added this device_id: " + device_id);
//
//                                } else {
//                                    Dlog.e("checkDeviceIsStoredNew: Not added this device_id!");
//                                    //add map of "apps" in this index
//                                    updateDevice(device_id);
//                                }
//                            }
                        }*/

                    } else {
                        Dlog.d("FireStore: Document does not exist!");
                    }
                } else {
                    Dlog.d("FireStore: Failed with: " + task.getException());
                }

            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Dlog.e("checkDeviceIsStoredNew onFailure: " + e.getMessage());
            }
        });
    }

    private void updateDevice(String device_id) {

        Map<String, Object> deviceElementMap = new HashMap<>();
        deviceElementMap.put(device_id, new HashMap<>());

        Map<String, Object> deviceMap = new HashMap<>();
        deviceMap.put("device", deviceElementMap);

        Dlog.d("updateDevice deviceMap: " + deviceMap);

        firebaseFirestore.collection(collectionName).document(User_UID).update(deviceMap).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                Dlog.d("addDevice success");
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Dlog.e("addDevice onFailure: " + e.getMessage());
            }
        });

        //--------------------------For Array------------------------

        /*Map<String, Object> deviceElementMap = new HashMap<>();
        deviceElementMap.put("device_id", device_id);

        Map<String, Object> deviceMap = new HashMap<>();
        deviceMap.put("device", FieldValue.arrayUnion(deviceElementMap));

        firebaseFirestore.collection(collectionName).document(User_UID).update(deviceMap).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                Dlog.d("addDevice success");
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Dlog.e("addDevice onFailure: " + e.getMessage());
            }
        });*/
    }

    public void checkAppNew(String device_id, String app_name, String app_package) {

        Map<String, Object> deviceElementMap = new HashMap<>();
        deviceElementMap.put("device_id", device_id);

        firebaseFirestore.collection(collectionName).document(User_UID).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {

                if (task.isSuccessful()) {
                    DocumentSnapshot document = task.getResult();

                    if (document.exists()) {
                        Dlog.d("checkAppNew FireStore: Document exists!");

                        deviceMapData = (Map<String, Object>) document.get("device");
                        Dlog.d("checkAppNew deviceMapData: " + deviceMapData);

                        Map<String, Object> appMapData = (Map<String, Object>) deviceMapData.get(device_id);
                        Dlog.d("checkAppNew appData: " + appMapData);

                        if (appMapData == null) {
                            Dlog.e("checkAppNew: NOT have any app!");

                        } else {
                            Dlog.d("checkAppNew App array exists!");

                            if (appMapData.containsKey(app_package)) {
                                Dlog.d("checkAppNew: " + app_package + " exists!");

                                Map<String, Object> appPackage = (Map<String, Object>) appMapData.get(app_package);
                                Dlog.d("checkAppNew appPackage: " + appPackage);

                                Map<String, Object> dates = (Map<String, Object>) appPackage.get("dates");
                                Dlog.d("checkAppNew dates: " + dates);

                                Map<String, Object> currentDateMap = (Map<String, Object>) dates.get(currentDate);
                                Dlog.d("checkAppNew currentDates: " + currentDateMap);

                                if (currentDateMap == null) {
                                    addDate(dates, device_id, app_package, 0);

                                } else {
                                    long attempts = (long) currentDateMap.get("attempts");
                                    Dlog.d("checkAppNew attempts: " + attempts);

                                    addDate(dates, device_id, app_package, attempts);
                                }

                            } else {
                                Dlog.d("checkAppNew: " + app_package + " NOT exists!");
                                addApps(appMapData, device_id, app_name, app_package, 0);
                            }
                        }

                        //--------------------------For Array------------------------------------
                        /*List<Map<String, Object>> deviceData = (List<Map<String, Object>>) document.get("device");
                        Dlog.d("checkAppNew deviceData: " + deviceData);

                        for (int i = 0; i < deviceData.size(); i++) {

                            Map<String, Object> deviceMap = deviceData.get(i);
                            Dlog.d("checkAppNew appList: " + deviceMap);

                            if (deviceMap.containsKey("apps")) {
                                Dlog.d("checkAppNew App array exists!");

                                List<Map<String, Object>> appList = (List<Map<String, Object>>) deviceMap.get("apps");

                                for (int j = 0; j < appList.size(); j++) {

                                    Map<String, Object> appMap = appList.get(j);
                                    Dlog.d("checkAppNew:appMap " +appMap);

                                    if (Objects.equals(appMap.get("package"), app_package)) {
                                        Dlog.d("checkAppNew: " + app_package + " exists!");

                                        *//*Map<String, Object> appPackage = (Map<String, Object>) appMap.get(app_package);
                                        Dlog.d("checkAppNew appPackage: " + appPackage);

                                        Map<String, Object> dates = (Map<String, Object>) appPackage.get("dates");*//*

                                        Map<String, Object> dates = (Map<String, Object>) appMap.get("dates");
                                        Dlog.d("checkAppNew dates: " + dates);

                                        Map<String, Object> currentDateMap = (Map<String, Object>) dates.get(currentDate);
                                        Dlog.d("checkAppNew currentDates: " + currentDateMap);

                                        if (currentDateMap == null) {
//                                            addDate(dates, device_id, app_name, app_package, 0);
                                            Dlog.i("checkAppNew Add date:");

                                        } else {
                                            long attempts = (long) currentDateMap.get("attempts");
                                            Dlog.d("checkAppNew attempts: " + attempts);
                                            Dlog.i("checkAppNew Update attempts: " + attempts + 1);
                                        }
                                        break;

                                    } else {
                                        //Note: Currently, if the matching package at 2nd position then when loop check
                                        // for 1st position at that time the 2nd position's app not found and then again add
                                        Dlog.e("checkAppNew: " + app_package + " NOT exists!");
                                        addAppArray(deviceData, i, j, deviceMap, device_id, app_name, app_package, 0);
                                    }
                                }

                            } else {
                                Dlog.e("checkAppNew: NOT have any app!");
                                //add map of "apps" in this index

                                addAppArray(deviceData, i, 0, deviceMap, device_id, app_name, app_package, 0);
                                break;
                            }
                        }*/

                    } else {
                        Dlog.d("checkAppNew FireStore: Document does not exist!");
                    }
                } else {
                    Dlog.d("checkAppNew FireStore: Failed with: " + task.getException());
                }
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Dlog.d("checkAppNew onFailure: " + e.getMessage());
            }
        });
    }

    public void addApps(Map<String, Object> appMapData, String device_id, String app_name, String app_package, long attempts) {

        Map<String, Object> attemptMap = new HashMap<>();
        attemptMap.put("attempts", attempts + 1);
        attemptMap.put("total_time_spent", 0);

        Map<String, Object> currentDateMap = new HashMap<>();
        currentDateMap.put(currentDate, attemptMap);

        Map<String, Object> appElementMap = new HashMap<>();
        appElementMap.put("allowed_time", 180);
        appElementMap.put("app_name", app_name);
        appElementMap.put("dates", currentDateMap);

        Map<String, Object> appPackageMap = new HashMap<>();
        appPackageMap.put(app_package, appElementMap);

        // Get the "apps" map and add the new map
        if (appMapData != null) {
            appMapData.put(app_package, appElementMap);
        } else {
            appMapData.put(app_package, appPackageMap);
        }

        Dlog.d("addApps appMapData: " + appMapData);

//        String fieldName = "device." + index;
//        Dlog.d("addApps fieldName: " + fieldName);

        firebaseFirestore.collection(collectionName).document(User_UID).update("device." + device_id, appMapData).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                Dlog.d("addApps success");
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Dlog.e("addApps onFailure: " + e.getMessage());
            }
        });
    }

    /*private void addAppArray(List<Map<String, Object>> deviceData, int index, int appIndex, Map<String, Object> deviceMap, String device_id, String app_name, String app_package, long attempts) {

        Map<String, Object> attemptMap = new HashMap<>();
        attemptMap.put("attempts", attempts + 1);
        attemptMap.put("total_time_spent", 0);

        Map<String, Object> currentDateMap = new HashMap<>();
        currentDateMap.put(currentDate, attemptMap);

        Map<String, Object> appElementMap = new HashMap<>();
        appElementMap.put("allowed_time", 180);
        appElementMap.put("app_name", app_name);
        appElementMap.put("dates", currentDateMap);
        appElementMap.put("package", app_package);

        List<Map<String, Object>> appList = new ArrayList<>();

        // Get the "apps" map and add the new map
        List<Map<String, Object>> appsSavedList = (List<Map<String, Object>>) deviceMap.get("apps");

        Map<String, Object> deviceElementMap = new HashMap<>();
        deviceElementMap.put("device_id", device_id);

        if (appsSavedList != null) {
            Map<String, Object> appMap = appsSavedList.get(appIndex);

            if (Objects.equals(appMap.get("package"), app_package)) {
                appsSavedList.set(appIndex, appElementMap);
            } else {
                appsSavedList.add(appElementMap);
            }

            deviceElementMap.put("apps", appsSavedList);

            Dlog.d("addApps appsSavedMap: " + appMap);

        } else {
            appList.add(appElementMap);
            Dlog.d("addApps appPackageMap: " + appElementMap);
            deviceElementMap.put("apps", appList);
        }

//        String fieldName = "device." + index;
//        Dlog.d("addApps fieldName: " + fieldName);

        Dlog.d("addApps deviceElementMap: " + deviceElementMap);
        deviceData.set(index, deviceElementMap);

        firebaseFirestore.collection(collectionName).document(User_UID).update("device", deviceData).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                Dlog.d("addApps success");
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Dlog.e("addApps onFailure: " + e.getMessage());
            }
        });
    }*/

    private void addDate(Map<String, Object> datesMap, String device_id, String app_package, long attempts) {

        Map<String, Object> attemptMap = new HashMap<>();
        attemptMap.put("attempts", attempts + 1);
        attemptMap.put("total_time_spent", 0);

        datesMap.put(currentDate, attemptMap);

        Dlog.d("addApps datesMap: " + datesMap);

        firebaseFirestore.collection(collectionName).document(User_UID).update(FieldPath.of("device", device_id, app_package, "dates"), datesMap).addOnCompleteListener(new OnCompleteListener<Void>() {
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

    public void addGoogleAuthData(String name, String email_id, String UID) {

        firebaseFirestore.collection(collectionName).document(User_UID).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {

                if (task.isSuccessful()) {
                    DocumentSnapshot document = task.getResult();
                    if (document.exists()) {
                        Dlog.d("addGoogleAuthData FireStore: Document exists!");

                        Map<String, Object> deviceMapData = (Map<String, Object>) document.get("device");
                        Dlog.d("addGoogleAuthData deviceMapData: " + deviceMapData);

                        if (deviceMapData != null) {

                            Map<String, Object> deviceMap = new HashMap<>();
                            deviceMap.put("device", deviceMapData);
                            deviceMap.put("user_name", name);
                            deviceMap.put("user_email", email_id);
                            deviceMap.put("user_id", UID);

                            Dlog.d("addGoogleAuthData deviceMap: " + deviceMap);

                            firebaseFirestore.collection(collectionName).document(User_UID).update(deviceMap).addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task) {
                                    Dlog.d("addGoogleAuthData onComplete");
                                }
                            }).addOnFailureListener(new OnFailureListener() {
                                @Override
                                public void onFailure(@NonNull Exception e) {
                                    Dlog.d("addGoogleAuthData onFailure");
                                }
                            });
                        }

                    } else {
                        Dlog.d("addGoogleAuthData: FireStore: Document does not exist!");
                    }
                } else {
                    Dlog.d("addGoogleAuthData: FireStore: Failed with: " + task.getException());
                }
            }
        });
    }
}
