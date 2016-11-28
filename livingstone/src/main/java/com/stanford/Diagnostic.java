package com.stanford;

/**
 * Created by ibrahimhassan on 11/23/15.
 * CScan Stanford Project
 */
public class Diagnostic {
    float scanResult;
    String scanMessage;

    Diagnostic(Long imageReference, String scanMode) {
        // do something with the image reference and set the scan mode
    }
    public float getScanResult() {
        return scanResult;
    }

    public void setScanResult(float scanResult) {
        this.scanResult = scanResult;
    }

    public String getScanMessage() {
        return scanMessage;
    }

    public void setScanMessage(String scanMessage) {
        this.scanMessage = scanMessage;
    }
}
