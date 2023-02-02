package com.sevensec.repo.interfaces;

public interface DataOperation {

    void checkDeviceIsStored(String deviceId);

    void addUserOnFireStore(String deviceId);

    void checkAppAddedOrNot(String deviceId, String appLabel, String lastAppPackage);

    void addAppDataWithAttempt(String deviceId, String appLabel, String lastAppPackage, int attempt, String lastUsedTime);

    void removeTimeFromArray(String deviceId, String appLabel, long timeStamp);
}
