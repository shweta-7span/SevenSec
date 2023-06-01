package com.sevensec.repo.interfaces;

import java.util.Map;

public interface AppUsageFromFireStore {

    void getTotalAppUsageFromFireStore(Map<String, Object> datesMap);
}
