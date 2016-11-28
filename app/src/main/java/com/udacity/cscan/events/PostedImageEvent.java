package com.udacity.cscan.events;

/**
 * Created by ibrahimhassan on 11/24/15.
 * CScan Stanford Project
 */
public class PostedImageEvent {
    private Float scanResultValue;
    private String scanResultMessage;

    public PostedImageEvent(Float scanResultValue, String scanResultMessage) {
        this.scanResultValue = scanResultValue;
        this.scanResultMessage = scanResultMessage;
    }

    public Float getScanResultValue() {
        return scanResultValue;
    }

    public String getScanResultMessage() {
        return scanResultMessage;
    }
}
