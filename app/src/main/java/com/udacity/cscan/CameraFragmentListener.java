package com.udacity.cscan;

/**
 * Listener interface that has to be implemented by activities using
 * {@link CameraFragment} instances.
 * <p>
 * Created by ibrahimhassan on 12/1/15.
 */
public interface CameraFragmentListener {
    /**
     * A non-recoverable camera error has happened.
     */
    public void onCameraError();
}
