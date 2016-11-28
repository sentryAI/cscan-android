package com.udacity.cscan.events;

import android.app.Activity;

import com.udacity.cscan.R;

/**
 * Created by ibrahimhassan on 11/24/15.
 * CScan Stanford Project
 */
public class ScanModeChangeEvent {
    private Boolean scanMode;

    public ScanModeChangeEvent(Activity activity, Boolean currentScanMode) {
        this.scanMode = currentScanMode;
        if (currentScanMode) {
            activity.setTitle(activity.getString(R.string.saliency_mode_activity_title));
        } else {
            activity.setTitle(activity.getString(R.string.live_scan_activity_title));
        }
    }

    public Boolean getScanMode() {
        return scanMode;
    }

}
