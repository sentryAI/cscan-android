package com.udacity.cscan;

import android.content.Context;
import android.util.AttributeSet;
import android.view.SurfaceView;

/**
 * Created by ibrahimhassan on 12/1/15.
 */
public class CameraPreview extends SurfaceView {
    private static final double ASPECT_RATIO = 3.0 / 4.0;

    public CameraPreview(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    public CameraPreview(Context context, AttributeSet attrs) {
        super(context, attrs);

    }

    public CameraPreview(Context context) {
        super(context);
    }

}