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
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.firestore.SetOptions;
import com.sevensec.activities.MainActivity;
import com.sevensec.helper.AuthFailureListener;
import com.sevensec.repo.interfaces.DataOperation;
import com.sevensec.utils.Dlog;
import com.sevensec.utils.SharedPref;

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

                startActivity(new Intent(getApplicationContext(), MainActivity.class));
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
}
