package com.sevensec.activities;

import static com.sevensec.utils.Constants.PREF_IS_APP_LAUNCH_FIRST_TIME;
import static com.sevensec.utils.Constants.SPLASH_DELAY;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.WindowManager;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.databinding.DataBindingUtil;

import com.android.billingclient.api.BillingClient;
import com.android.billingclient.api.BillingClientStateListener;
import com.android.billingclient.api.BillingResult;
import com.android.billingclient.api.QueryPurchasesParams;
import com.sevensec.BuildConfig;
import com.sevensec.R;
import com.sevensec.databinding.ActivitySplashBinding;
import com.sevensec.utils.SharedPref;
import com.sevensec.utils.Utils;

/**
 * An example full-screen activity that shows and hides the system UI (i.e.
 * status bar and navigation/system bar) with user interaction.
 */
@SuppressLint("CustomSplashScreen")
public class SplashActivity extends AppCompatActivity {

    ActivitySplashBinding binding;

    @SuppressLint("HardwareIds")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS,
                WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS);
        binding = DataBindingUtil.setContentView(this, R.layout.activity_splash);

        SharedPref.init(getApplicationContext());

        binding.appVersion.setText(String.format("v %s", BuildConfig.VERSION_NAME));

        new Handler().postDelayed(() -> {
            if (SharedPref.readBoolean(PREF_IS_APP_LAUNCH_FIRST_TIME, true)) {
                startActivity(new Intent(getApplicationContext(), OnBoardingActivity.class));
            } else {
                Utils.isLogin(this);
            }
            finish();
        }, SPLASH_DELAY);
    }

//    void checkSubscription(){
//
//        billingClient = BillingClient.newBuilder(this).enablePendingPurchases().setListener((billingResult, list) -> {}).build();
//        final BillingClient finalBillingClient = billingClient;
//        billingClient.startConnection(new BillingClientStateListener() {
//            @Override
//            public void onBillingServiceDisconnected() {
//
//            }
//
//            @Override
//            public void onBillingSetupFinished(@NonNull BillingResult billingResult) {
//
//                if(billingResult.getResponseCode() == BillingClient.BillingResponseCode.OK){
//                    finalBillingClient.queryPurchasesAsync(
//                            QueryPurchasesParams.newBuilder().setProductType(BillingClient.ProductType.SUBS).build(), (billingResult1, list) -> {
//                                if (billingResult1.getResponseCode() == BillingClient.BillingResponseCode.OK){
//                                    Log.d("testOffer",list.size() +" size");
//                                    if(list.size()>0){
//                                        prefs.setPremium(1); // set 1 to activate premium feature
//                                        int i = 0;
//                                        for (Purchase purchase: list){
//                                            //Here you can manage each product, if you have multiple subscription
//                                            Log.d("testOffer",purchase.getOriginalJson()); // Get to see the order information
//                                            Log.d("testOffer", " index" + i);
//                                            i++;
//                                        }
//                                    }else {
//                                        prefs.setPremium(0); // set 0 to de-activate premium feature
//                                    }
//                                }
//                            });
//
//                }
//
//            }
//        });
//    }

}