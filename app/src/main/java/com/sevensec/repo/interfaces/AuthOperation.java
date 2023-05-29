package com.sevensec.repo.interfaces;

import android.app.Activity;

import com.sevensec.helper.AuthFailureListener;

public interface AuthOperation {

    void loginAnonymously(Activity activity, String DEVICE_ID, AuthFailureListener authFailureListener);
}
