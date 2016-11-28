package com.udacity.cscan.events;

/**
 * Created by ibrahimhassan on 11/24/15.
 * CScan Stanford Project
 */
public class PostingImageEvent {

    private long localId;

    public PostingImageEvent(long localId) {
        this.localId = localId;
    }

    public long getLocalId() {
        return localId;
    }
}
