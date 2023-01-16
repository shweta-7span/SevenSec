package com.sevensec.activities;

import static com.sevensec.utils.Constants.DB_COLLECTION_APPS;
import static com.sevensec.utils.Constants.DB_COLLECTION_USERS;
import static com.sevensec.utils.Constants.DB_DOCUMENT_KEY_APP_ATTEMPTS;
import static com.sevensec.utils.Constants.DB_DOCUMENT_KEY_APP_NAME;
import static com.sevensec.utils.Constants.DB_DOCUMENT_KEY_APP_PACKAGE;
import static com.sevensec.utils.Constants.STR_DEVICE_ID;

import android.app.Activity;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;

import androidx.annotation.NonNull;
import androidx.databinding.DataBindingUtil;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.SetOptions;
import com.sevensec.R;
import com.sevensec.base.AppConstants;
import com.sevensec.databinding.ActivityAttemptBinding;
import com.sevensec.utils.SharedPref;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TimeZone;

public class AttemptActivity extends Activity {

    private String TAG;
    ActivityAttemptBinding binding;
    private String DEVICE_ID;

    private String appLabel;
    private String lastAppPackage;

    FirebaseFirestore db = FirebaseFirestore.getInstance();
    SimpleDateFormat format;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        binding = DataBindingUtil.setContentView(this, R.layout.activity_attempt);

        TAG = getApplicationContext().getClass().getName();
        DEVICE_ID = SharedPref.readString(STR_DEVICE_ID, "");
        PackageManager packageManager = getPackageManager();

        binding.tvBreathDesc.setVisibility(View.VISIBLE);
        binding.rlAttempt.setVisibility(View.GONE);

        format = new SimpleDateFormat("dd-MM-yyyy hh:mm:ss a");
        format.setTimeZone(TimeZone.getTimeZone("UTC"));

        if (getIntent().getStringExtra(AppConstants.STR_LAST_WARN_APP) != null) {
            lastAppPackage = getIntent().getStringExtra(AppConstants.STR_LAST_WARN_APP);
            Log.e(TAG, "Last App's Package: " + lastAppPackage);
        }

        try {
            /*SpUtil.getInstance().getString(STR_LAST_WARN_APP)*/
            ApplicationInfo appInfo = packageManager.getApplicationInfo(lastAppPackage, PackageManager.GET_UNINSTALLED_PACKAGES);
            if (appInfo != null) {
                Drawable iconDrawable = packageManager.getApplicationIcon(appInfo);
                appLabel = packageManager.getApplicationLabel(appInfo).toString();
                binding.ivAppLogo.setImageDrawable(iconDrawable);
                binding.tvAppLabel.setText(appLabel);

                binding.tvContinue.setText(String.format("%s %s", getString(R.string.continue_with), appLabel));
                binding.tvNotGoWithApp.setText(String.format("%s %s", getString(R.string.not_go), appLabel));
            }
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
            Log.e(TAG, "getAppName Error: " + e.getMessage());
        }

        binding.tvContinue.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });

        binding.tvNotGoWithApp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent homeIntent = new Intent(Intent.ACTION_MAIN);
                homeIntent.addCategory(Intent.CATEGORY_HOME);
                startActivity(homeIntent);
                finish();
            }
        });

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                binding.tvBreathDesc.setVisibility(View.GONE);
                binding.rlAttempt.setVisibility(View.VISIBLE);
            }
        }, 4000);

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                Intent intent = new Intent(getApplicationContext(), GreyActivity.class);
                startActivity(intent);
            }
        }, 500);

        checkAppAddedOrNot();
    }

    private void setAttempt(int lastAttempt) {
        binding.tvAttempts.setText(String.valueOf(lastAttempt));
        if (lastAttempt == 1) {
            binding.tvAttemptDesc.setText(String.format("%s%s%s", getString(R.string.attempt_to_open), " " + appLabel + " ", getString(R.string.within_24_hrs)));
        }else{
            binding.tvAttemptDesc.setText(String.format("%s%s%s", getString(R.string.attempts_to_open), " " + appLabel + " ", getString(R.string.within_24_hrs)));
        }
    }

    private void checkAppAddedOrNot() {

        //Check App is already Added OR Not
        db.collection(DB_COLLECTION_USERS).document(DEVICE_ID).collection(DB_COLLECTION_APPS).document(appLabel).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()) {
                    DocumentSnapshot document = task.getResult();
                    if (document.exists()) {
                        Log.d(TAG, "FireStore: Document exists!");
                        getLastAttemptAndTime(document);
                    } else {
                        Log.d(TAG, "FireStore: Document does not exist!");
                        addAppDataWithAttempt(0);
                    }
                } else {
                    Log.d(TAG, "FireStore: Failed with: ", task.getException());
                }
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {

            }
        });
    }

    private void getLastAttemptAndTime(DocumentSnapshot document) {
        List<Long> timeList = (List<Long>) document.get(DB_DOCUMENT_KEY_APP_ATTEMPTS);

        int attemptCount = 0;

        for (Long timeStamp : timeList) {
            Log.v(TAG, "FireStore: getLastAttemptAndTime: " + timeStamp);
            if (check24Hour(timeStamp)) {
                removeTimeFromArray(timeStamp);
            } else {
                attemptCount++;
            }
        }

        long lastUsedDifference = Math.abs(timeList.get(timeList.size() - 1) - (new Date().getTime()));
        getLastUSedTime(lastUsedDifference);

        addAppDataWithAttempt(attemptCount);
    }

    private boolean check24Hour(Long lastTimeStamp) {
        long MILLIS_PER_DAY = 24 * 60 * 60 * 1000L;

        boolean moreThanDay = Math.abs(lastTimeStamp - (new Date().getTime())) > MILLIS_PER_DAY;
        Log.d(TAG, "FireStore: check24Hour moreThanDay: " + moreThanDay);

        return moreThanDay;
    }

    private void addAppDataWithAttempt(int attempt) {
        Log.i(TAG, "FireStore: addAppDataWithAttempt attempt: " + attempt);
        setAttempt(attempt + 1);

        Map<String, Object> apps = new HashMap<>();
        apps.put(DB_DOCUMENT_KEY_APP_NAME, appLabel);
        apps.put(DB_DOCUMENT_KEY_APP_PACKAGE, lastAppPackage);
        apps.put(DB_DOCUMENT_KEY_APP_ATTEMPTS, FieldValue.arrayUnion(new Date().getTime()));

        // Add a new document with above fields
        db.collection(DB_COLLECTION_USERS).document(DEVICE_ID).collection(DB_COLLECTION_APPS).document(appLabel)
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

    private void removeTimeFromArray(Long timeStamp) {
        Map<String, Object> apps = new HashMap<>();
        apps.put(DB_DOCUMENT_KEY_APP_ATTEMPTS, FieldValue.arrayRemove(timeStamp));

        // Remove timeStamp from Array
        db.collection(DB_COLLECTION_USERS).document(DEVICE_ID).collection(DB_COLLECTION_APPS).document(appLabel)
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

    public void getLastUSedTime(long difference) {

        System.out.println("difference : " + difference);

        StringBuilder s = new StringBuilder(100);

        long secondsInMilli = 1000;
        long minutesInMilli = secondsInMilli * 60;
        long hoursInMilli = minutesInMilli * 60;
        long daysInMilli = hoursInMilli * 24;

        long elapsedDays = difference / daysInMilli;
        difference = difference % daysInMilli;

        long elapsedHours = difference / hoursInMilli;
        difference = difference % hoursInMilli;

        long elapsedMinutes = difference / minutesInMilli;
        difference = difference % minutesInMilli;

        long elapsedSeconds = difference / secondsInMilli;

        if (elapsedDays != 0) {
            if (elapsedDays == 1)
                s.append(elapsedDays).append(" day ago");
            else
                s.append(elapsedDays).append(" days ago");
        } else if (elapsedHours != 0) {
            if (elapsedHours == 1)
                s.append(elapsedHours).append(" hr ago");
            else
                s.append(elapsedHours).append(" hrs ago");
        } else if (elapsedMinutes != 0) {
            if (elapsedMinutes == 1)
                s.append(elapsedMinutes).append(" min ago");
            else
                s.append(elapsedMinutes).append(" mins ago");
        } else if (elapsedSeconds != 0) {
            if (elapsedSeconds == 1)
                s.append(elapsedSeconds).append(" second ago");
            else
                s.append(elapsedSeconds).append(" seconds ago");
        }

        System.out.printf(
                "DIFFERENCE: %d days, %d hours, %d minutes, %d seconds%n",
                elapsedDays, elapsedHours, elapsedMinutes, elapsedSeconds);

        binding.tvLastUse.setText(String.format("Last use: %s", s));
    }
}