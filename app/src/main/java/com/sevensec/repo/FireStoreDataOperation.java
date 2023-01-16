package com.sevensec.repo;

import static com.sevensec.utils.Constants.ANDROID;
import static com.sevensec.utils.Constants.DB_COLLECTION_APPS;
import static com.sevensec.utils.Constants.DB_COLLECTION_USERS;
import static com.sevensec.utils.Constants.DB_DOCUMENT_KEY_APP_ATTEMPTS;
import static com.sevensec.utils.Constants.DB_DOCUMENT_KEY_APP_NAME;
import static com.sevensec.utils.Constants.DB_DOCUMENT_KEY_APP_PACKAGE;
import static com.sevensec.utils.Constants.DB_DOCUMENT_KEY_TYPE;
import static com.sevensec.utils.Utils.check24Hour;
import static com.sevensec.utils.Utils.getLastUsedTime;

import android.util.Log;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.firestore.SetOptions;
import com.sevensec.repo.interfaces.DataOperation;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public abstract class FireStoreDataOperation extends AppCompatActivity implements DataOperation {

    String TAG = getClass().getName();
    FirebaseFirestore db = FirebaseFirestore.getInstance();

    @Override
    public void checkDeviceIsStored(String deviceId) {
        db.collection(DB_COLLECTION_USERS).get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {

                Log.d("TAG", "FireStore: document Size: " + task.getResult().size());

                if (task.isSuccessful()) {
                    if (task.getResult().size() > 0) {
                        for (DocumentSnapshot document : task.getResult()) {
                            //Log.d("TAG", "FireStore: document: " + document.get(DB_DOCUMENT_KEY_USER));
                            Log.d("TAG", "FireStore: document: " + document.getId());

                            if (Objects.equals(document.getId(), deviceId)) {
                                Log.d("TAG", "FireStore: DEVICE_ID already exists");
                            } else {
                                Log.e("TAG", "FireStore: DEVICE_ID NOT exists");
                            }
                        }
                    } else {
                        Log.e("TAG", "FireStore: Collection Not exists");
                        addUserOnFireStore(deviceId);
                    }
                } else {
                    Log.e("TAG", "FireStore: task NOT successful");
                }
            }
        });
    }

    @Override
    public void addUserOnFireStore(String deviceId) {
        // Create a new user with a first and last name
        Map<String, Object> type = new HashMap<>();
        type.put(DB_DOCUMENT_KEY_TYPE, ANDROID);

        // Add a new document with a generated ID
        db.collection(DB_COLLECTION_USERS).document(deviceId)
                .set(type)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void unused) {
                        Log.d(TAG, "FireStore: DocumentSnapshot successfully written!");
                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.w(TAG, "FireStore: Error adding document", e);
                    }
                });
    }

    @Override
    public void checkAppAddedOrNot(String deviceId, String appLabel, String lastAppPackage) {
        //Check App is already Added OR Not
        db.collection(DB_COLLECTION_USERS).document(deviceId).collection(DB_COLLECTION_APPS).document(appLabel).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()) {
                    DocumentSnapshot document = task.getResult();
                    if (document.exists()) {
                        Log.d(TAG, "FireStore: Document exists!");
                        getLastAttemptAndTime(deviceId, appLabel, lastAppPackage, document);
                    } else {
                        Log.d(TAG, "FireStore: Document does not exist!");
                        addAppDataWithAttempt(deviceId, appLabel, lastAppPackage, 0, null);
                    }
                } else {
                    Log.d(TAG, "FireStore: Failed with: ", task.getException());
                }
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Log.w(TAG, "FireStore: checkAppAddedOrNot Error: ", e);
            }
        });
    }

    private void getLastAttemptAndTime(String deviceId, String appLabel, String lastAppPackage, DocumentSnapshot document) {
        List<Long> timeList = (List<Long>) document.get(DB_DOCUMENT_KEY_APP_ATTEMPTS);

        int attemptCount = 0;

        for (Long timeStamp : timeList) {
            Log.v(TAG, "FireStore: getLastAttemptAndTime: " + timeStamp);
            if (check24Hour(timeStamp)) {
                removeTimeFromArray(deviceId, appLabel, timeStamp);
            } else {
                attemptCount++;
            }
        }

        long lastUsedDifference = Math.abs(timeList.get(timeList.size() - 1) - (new Date().getTime()));
        String lastUsedTime = getLastUsedTime(lastUsedDifference);

        addAppDataWithAttempt(deviceId, appLabel, lastAppPackage, attemptCount, lastUsedTime);
    }

    @Override
    public void addAppDataWithAttempt(String deviceId, String appLabel, String lastAppPackage, int attempt, String lastUsedTime) {
        Log.i(TAG, "FireStore: addAppDataWithAttempt attempt: " + attempt);
        setAttempt(attempt + 1, lastUsedTime);

        Map<String, Object> apps = new HashMap<>();
        apps.put(DB_DOCUMENT_KEY_APP_NAME, appLabel);
        apps.put(DB_DOCUMENT_KEY_APP_PACKAGE, lastAppPackage);
        apps.put(DB_DOCUMENT_KEY_APP_ATTEMPTS, FieldValue.arrayUnion(new Date().getTime()));

        // Add a new document with above fields
        db.collection(DB_COLLECTION_USERS).document(deviceId).collection(DB_COLLECTION_APPS).document(appLabel)
                .set(apps, SetOptions.merge())
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void unused) {
                        Log.d(TAG, "FireStore: Apps successfully written!");
                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.w(TAG, "FireStore: Error adding App", e);
                    }
                });
    }

    public void setAttempt(int i, String lastUsedTime) {}

    @Override
    public void removeTimeFromArray(String deviceId, String appLabel, long timeStamp) {
        Map<String, Object> apps = new HashMap<>();
        apps.put(DB_DOCUMENT_KEY_APP_ATTEMPTS, FieldValue.arrayRemove(timeStamp));

        // Remove timeStamp from Array
        db.collection(DB_COLLECTION_USERS).document(deviceId).collection(DB_COLLECTION_APPS).document(appLabel)
                .update(apps)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void unused) {
                        Log.d(TAG, "FireStore: TimeStamp successfully removed!");
                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.w(TAG, "FireStore: Error adding App", e);
                    }
                });
    }
}
