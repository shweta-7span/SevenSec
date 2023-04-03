package com.sevensec.repo.interfaces;

import android.content.Context;

import com.sevensec.helper.AuthFailureListener;

public interface AuthOperation {

    void loginAnonymously(Context mContext, String DEVICE_ID, AuthFailureListener authFailureListener);
}
