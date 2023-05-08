package com.sevensec.activities;

import static com.sevensec.utils.Constants.PREF_DEVICE_ID;
import static com.sevensec.utils.Constants.PREF_IS_LOGIN;

import androidx.databinding.DataBindingUtil;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.Network;
import android.net.NetworkRequest;
import android.os.Bundle;
import android.provider.Settings;
import android.view.View;

import com.sevensec.R;
import com.sevensec.databinding.ActivityLoginBinding;
import com.sevensec.helper.AuthFailureListener;
import com.sevensec.repo.FireBaseAuthOperation;
import com.sevensec.utils.Dlog;
import com.sevensec.utils.SharedPref;
import com.sevensec.utils.Utils;

public class LoginActivity extends FireBaseAuthOperation implements AuthFailureListener {

    ActivityLoginBinding binding;
    String DEVICE_ID;
    ConnectivityManager connMgr;

    // Define a NetworkCallback object
    ConnectivityManager.NetworkCallback networkCallback = new ConnectivityManager.NetworkCallback() {
        @Override
        public void onAvailable(Network network) {
            // The network is available
            // Do something here, such as update your UI
            Dlog.d("Internet: onAvailable");
            runOnUiThread(() -> checkForInternet(true));
        }

        @Override
        public void onLost(Network network) {
            // The network is lost
            // Do something here, such as update your UI
            Dlog.d("Internet: onLost");
            runOnUiThread(() -> checkForInternet(false));
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = DataBindingUtil.setContentView(this, R.layout.activity_login);

        binding.rlLogin.setVisibility(View.GONE);
        binding.llNoInternet.setVisibility(View.GONE);
        binding.progressBar.setVisibility(View.GONE);

        checkForInternet(Utils.isInternetAvailable(this));

        // Get an instance of the ConnectivityManager
        connMgr = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);

        // Register the network callback
        NetworkRequest.Builder builder = new NetworkRequest.Builder();
        connMgr.registerNetworkCallback(builder.build(), networkCallback);

        //Store DEVICE_ID in Preference
        DEVICE_ID = Settings.Secure.getString(getApplicationContext().getContentResolver(), Settings.Secure.ANDROID_ID);
        Dlog.d("OnBoardingActivity onCreate DEVICE_ID: " + DEVICE_ID);
        SharedPref.writeString(PREF_DEVICE_ID, DEVICE_ID);

        binding.tvSkip.setOnClickListener(v -> {
            binding.rlLogin.setVisibility(View.GONE);
            binding.progressBar.setVisibility(View.VISIBLE);
            loginAnonymously(getApplicationContext(), DEVICE_ID, this);
        });
    }

    private void checkForInternet(boolean isInternetAvailable) {
        if (isInternetAvailable) {
            binding.llNoInternet.setVisibility(View.GONE);
            binding.rlLogin.setVisibility(View.VISIBLE);
        } else {
            binding.llNoInternet.setVisibility(View.VISIBLE);
            binding.rlLogin.setVisibility(View.GONE);
        }
    }

    @Override
    public void authFail() {
        SharedPref.writeBoolean(PREF_IS_LOGIN, false);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (connMgr != null) {
            connMgr.unregisterNetworkCallback(networkCallback);
        }
    }
}