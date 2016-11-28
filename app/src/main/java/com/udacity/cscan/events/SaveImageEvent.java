package com.udacity.cscan.events;

/**
 * Created by ibrahimhassan on 11/24/15.
 * CScan Stanford Project
 */
public class SaveImageEvent {
    private byte[] mData;

    public SaveImageEvent(byte[] data) {
        mData = data;
    }

    public byte[] getPictureData() {
        return mData;
    }

}
