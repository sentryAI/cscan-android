package com.udacity.cscan.events;

import com.udacity.cscan.daoimage.Image;

/**
 * Created by ibrahimhassan on 11/24/15.
 * CScan Stanford Project
 */
public class AddedImageEvent {
    private Image image;

    public AddedImageEvent(Image capturedImage) {
        this.image = capturedImage;
    }

    public Image getScanMode() {
        return image;
    }

}
