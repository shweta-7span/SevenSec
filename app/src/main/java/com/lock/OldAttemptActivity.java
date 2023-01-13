package com.lock;

import static com.sevensec.base.AppConstants.STR_LAST_WARN_APP;
import static com.sevensec.utils.Constants.DB_COLLECTION_APPS;
import static com.sevensec.utils.Constants.DB_COLLECTION_USERS;
import static com.sevensec.utils.Constants.DB_DOCUMENT_KEY_APP_ATTEMPTS;
import static com.sevensec.utils.Constants.DB_DOCUMENT_KEY_APP_LAST_ATTEMPT_TIME;
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
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;

import com.sevensec.R;
import com.sevensec.activities.GreyActivity;
import com.sevensec.utils.SharedPref;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.TimeZone;

public class OldAttemptActivity extends Activity {

    private String TAG;
    private String DEVICE_ID;
    private PackageManager packageManager;
    private ApplicationInfo appInfo;

    private Drawable iconDrawable;
    private String appLabel;
    private String lastAppPackage;

    private TextView tvContinue;
    private TextView tvNotGoWithApp;
    private TextView tvAppLabel;
    private TextView tvBreathDesc;
    private TextView tvAttempts;
    private TextView tvAttemptDesc;
    private ImageView ivAppLogo;
    private RelativeLayout rlAttempt;

    FirebaseFirestore db = FirebaseFirestore.getInstance();
    SimpleDateFormat format;

    int lastAttempt;
    String lastAttemptTime;
    boolean moreThanDay = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_attempt);

        TAG = getApplicationContext().getClass().getName();
        DEVICE_ID = SharedPref.readString(STR_DEVICE_ID, "");
        packageManager = getPackageManager();

        tvContinue = findViewById(R.id.tvContinue);
        tvNotGoWithApp = findViewById(R.id.tvNotGoWithApp);
        tvAppLabel = findViewById(R.id.tvAppLabel);
        tvBreathDesc = findViewById(R.id.tvBreathDesc);
        tvAttempts = findViewById(R.id.tvAttempts);
        tvAttemptDesc = findViewById(R.id.tvAttemptDesc);
        ivAppLogo = findViewById(R.id.ivAppLogo);
        rlAttempt = findViewById(R.id.rlAttempt);

        tvBreathDesc.setVisibility(View.VISIBLE);
        rlAttempt.setVisibility(View.GONE);

        format = new SimpleDateFormat("dd-MM-yyyy hh:mm:ss a");
        format.setTimeZone(TimeZone.getTimeZone("UTC"));

        if (getIntent().getStringExtra(STR_LAST_WARN_APP) != null) {
            lastAppPackage = getIntent().getStringExtra(STR_LAST_WARN_APP);
            Log.e(TAG, "Last App's Package: " + lastAppPackage);
        }

        try {
            /*SpUtil.getInstance().getString(STR_LAST_WARN_APP)*/
            appInfo = packageManager.getApplicationInfo(lastAppPackage, PackageManager.GET_UNINSTALLED_PACKAGES);
            if (appInfo != null) {
                iconDrawable = packageManager.getApplicationIcon(appInfo);
                appLabel = packageManager.getApplicationLabel(appInfo).toString();
                ivAppLogo.setImageDrawable(iconDrawable);
                tvAppLabel.setText(appLabel);

                tvContinue.setText(getString(R.string.continue_with) + " " + appLabel);
                tvNotGoWithApp.setText(getString(R.string.not_go) + " " + appLabel);
            }
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
            Log.e(TAG, "getAppName Error: " + e.getMessage());
        }

        tvContinue.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
//                Log.d(TAG, "FireStore: onClick lastAttempt: " + lastAttempt);
//                Log.d(TAG, "FireStore: onClick lastAttemptTime: " + lastAttemptTime);
//                addAppDataWithAttempt(lastAttempt + 1);
                finish();
            }
        });

        tvNotGoWithApp.setOnClickListener(new View.OnClickListener() {
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
                tvBreathDesc.setVisibility(View.GONE);
                rlAttempt.setVisibility(View.VISIBLE);
                tvAttemptDesc.setText(String.format("%s%s%s", getString(R.string.attempt_to_open), " " + appLabel + " ", getString(R.string.within_24_hrs)));
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

    private void checkAppAddedOrNot() {

        db.collection(DB_COLLECTION_USERS).document(DEVICE_ID).collection(DB_COLLECTION_APPS).get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {

                if (task.isSuccessful()) {
                    if (task.getResult().size() > 0) {
                        for (DocumentSnapshot document : task.getResult()) {
                            Log.d("TAG", "FireStore: document: " + document.get(DB_DOCUMENT_KEY_APP_NAME));

                            if (Objects.equals(document.get(DB_DOCUMENT_KEY_APP_NAME), appLabel)) {
                                Log.d("TAG", "FireStore: APP already exists");
                                getLastAttemptAndTime(document);

                            } else {
                                Log.e("TAG", "FireStore: APP NOT exists");
                                addAppDataWithAttempt(lastAttempt + 1);
                            }
                        }
                    } else {
                        Log.e("TAG", "FireStore: Collection Not exists");
                        addAppDataWithAttempt(lastAttempt + 1);
                    }
                } else {
                    Log.e("TAG", "FireStore: task NOT successful");
                }
            }
        });
    }

    private void getLastAttemptAndTime(DocumentSnapshot document) {
        lastAttempt = Integer.parseInt(document.get(DB_DOCUMENT_KEY_APP_ATTEMPTS).toString());
        lastAttemptTime = document.get(DB_DOCUMENT_KEY_APP_LAST_ATTEMPT_TIME).toString();
        Log.d("TAG", "FireStore: lastAttempt: " + lastAttempt);
        Log.d("TAG", "FireStore: lastAttemptTime: " + lastAttemptTime);

        check24Hour(lastAttemptTime);
//        setAttempt(lastAttempt + 1);

        addAppDataWithAttempt(lastAttempt + 1);
    }

    private void setAttempt(int lastAttempt) {
        if (moreThanDay) {
            tvAttempts.setText(String.valueOf(0));
        } else {
            tvAttempts.setText(String.valueOf(lastAttempt));
        }
    }

    private void check24Hour(String lastAttemptTime) {
        try {
            long MILLIS_PER_DAY = 24 * 60 * 60 * 1000L;
            Date date1 = format.parse(lastAttemptTime);
            Date date2 = format.parse(format.format(new Date()));

            moreThanDay = Math.abs(date1.getTime() - date2.getTime()) > MILLIS_PER_DAY;
            Log.d(TAG, "FireStore: updateAppDataWithAttempt moreThanDay: " + moreThanDay);

        } catch (ParseException e) {
            e.printStackTrace();
            Log.e(TAG, "FireStore: updateAppDataWithAttempt ParseException: " + e.getMessage());
        }
    }

    private void addAppDataWithAttempt(int attempt) {
        Log.d(TAG, "FireStore: addAppDataWithAttempt lastAttempt: " + lastAttempt);
        Log.d(TAG, "FireStore: addAppDataWithAttempt lastAttemptTime: " + lastAttemptTime);

        setAttempt(lastAttempt + 1);

        Map<String, Object> apps = new HashMap<>();
        apps.put(DB_DOCUMENT_KEY_APP_NAME, appLabel);
        apps.put(DB_DOCUMENT_KEY_APP_PACKAGE, lastAppPackage);
        apps.put(DB_DOCUMENT_KEY_APP_ATTEMPTS, attempt);
        apps.put(DB_DOCUMENT_KEY_APP_LAST_ATTEMPT_TIME, format.format(new Date()));

        // Add a new document with a generated ID
        db.collection(DB_COLLECTION_USERS).document(DEVICE_ID).collection(DB_COLLECTION_APPS).document(appLabel)
                .set(apps)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void unused) {
                        Log.d(TAG, "FireStore: Apps successfully written!");
                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.w(TAG, "FireStore: FireStore: Error adding App", e);
                    }
                });
    }

    /*private void updateAppDataWithAttempt(int attempt) {
        Map<String, Object> apps = new HashMap<>();
        apps.put(DB_DOCUMENT_KEY_APP_ATTEMPTS, moreThanDay ? 1 : attempt);
        apps.put(DB_DOCUMENT_KEY_APP_LAST_ATTEMPT_TIME, format.format(new Date()));

        db.collection(DB_COLLECTION_USERS).document(DEVICE_ID).collection(DB_COLLECTION_APPS).document(appLabel)
                .set(apps, SetOptions.merge());
    }*/

    @Override
    public void onBackPressed() {
        /*Intent homeIntent = new Intent(Intent.ACTION_MAIN);
        homeIntent.addCategory(Intent.CATEGORY_HOME);
        startActivity(homeIntent);
        finish();*/
    }
}