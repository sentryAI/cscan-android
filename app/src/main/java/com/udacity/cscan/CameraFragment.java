package com.udacity.cscan;


import android.content.Context;
import android.content.Intent;
import android.graphics.ImageFormat;
import android.hardware.Camera;
import android.hardware.Camera.CameraInfo;
import android.hardware.Camera.Size;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.util.Log;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.Surface;
import android.view.SurfaceHolder;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.TextView;

import com.path.android.jobqueue.JobManager;
import com.udacity.cscan.dialogs.SaveImageActivity;
import com.udacity.cscan.events.CancelSaveImageEvent;
import com.udacity.cscan.events.PostedImageEvent;
import com.udacity.cscan.events.PostingImageEvent;
import com.udacity.cscan.events.ScanModeChangeEvent;
import com.udacity.cscan.jobs.PostImageJob;

import java.io.IOException;
import java.util.List;

import de.greenrobot.event.EventBus;

/**
 * Created by ibrahimhassan on 12/1/15.
 */
@SuppressWarnings("Deprecated")
public class CameraFragment extends Fragment implements SurfaceHolder.Callback, Camera.PreviewCallback {
    public static final String TAG = "CameraFragment";

    private static final int PICTURE_SIZE_MAX_WIDTH = 1280;
    private static final int PREVIEW_SIZE_MAX_WIDTH = 640;

    private int cameraId;
    private Camera camera;
    private SurfaceHolder surfaceHolder;
    private CameraFragmentListener listener;
    private View rootView;
    private FrameLayout liveScanPreview;

    private TextView imagePreviewData;

    // This will take care of prioritization, persistence, load balancing, delaying, network control, grouping etc
    JobManager jobManager;
    boolean takeNextFrame = true;
    float frameRateLimit;

    boolean mIsLargeLayout;

    FloatingActionButton captureButton;
    /**
     * On activity getting attached.
     */
    @Override
    public void onAttach(Context context) {
        super.onAttach(context);

        if (!(context instanceof CameraFragmentListener)) {
            throw new IllegalArgumentException(
                    "Activity has to implement CameraFragmentListener interface"
            );
        }

        listener = (CameraFragmentListener) context;
        EventBus.getDefault().register(this);
        jobManager = CscanApplication.getInstance().getJobManager();
        mIsLargeLayout = getResources().getBoolean(R.bool.large_layout);


    }

    /**
     * On creating view for fragment.
     */
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        CameraPreview previewView = new CameraPreview(getActivity());
        previewView.getHolder().addCallback(this);

        rootView = inflater.inflate(R.layout.fragment_live_scan, container, false);
        imagePreviewData = (TextView) rootView.findViewById(R.id.image_preview_text_view);
        liveScanPreview = (FrameLayout) rootView.findViewById(R.id.camera_preview);
        liveScanPreview.addView(previewView);

        // Add a listener to the Capture button
        captureButton = (FloatingActionButton) getActivity().findViewById(R.id.fab);
        captureButton.setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        // take picture only if we are on LiveScan fragment
                        Log.d("activePage", CscanApplication.activePage + "");
                        if (CscanApplication.activePage == 0) {
                            // get an image from the camera
                            if (camera != null)
                                camera.takePicture(null, null, mPictureCallback);
                        }
                    }
                }
        );

        TypedValue typedValue = new TypedValue();
        getResources().getValue(R.dimen.frame_rate_limit, typedValue, true);
        frameRateLimit = typedValue.getFloat();

        return rootView;
    }

    /**
     * On fragment getting resumed.
     */
    @Override
    public void onResume() {
        super.onResume();

        try {
            camera = Camera.open(cameraId);
        } catch (Exception exception) {
            Log.e(TAG, "Can't open camera with id " + cameraId, exception);

            listener.onCameraError();
            return;
        }
    }

    /**
     * On fragment getting paused.
     */
    @Override
    public void onPause() {
        super.onPause();

        stopCameraPreview();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        try {
            EventBus.getDefault().unregister(this);
        } catch (Throwable t) {
            //this may crash if registration did not go through. just be safe
        }
    }

    public Fragment getVisibleFragment() {
        FragmentManager fragmentManager = getActivity().getSupportFragmentManager();
        List<Fragment> fragments = fragmentManager.getFragments();
        for (Fragment fragment : fragments) {
            if (fragment != null && fragment.isVisible())
                return fragment;
        }
        return null;
    }

    /**
     * Start the camera preview.
     */
    private synchronized void startCameraPreview() {

        if (camera != null) {
            determineDisplayOrientation();
            setupCamera();
        }

        try {

            camera.setPreviewDisplay(surfaceHolder);
            camera.setPreviewCallback(this);
            camera.startPreview();
        } catch (IOException exception) {
            Log.e(TAG, "Can't start camera preview due to IOException", exception);

            listener.onCameraError();
        }
    }

    /**
     * Stop the camera preview.
     */
    private synchronized void stopCameraPreview() {
        try {
            camera.stopPreview();
            camera.setPreviewCallback(null);
            camera.release();
            camera = null;
        } catch (Exception exception) {
            Log.i(TAG, "Exception during stopping camera preview");
        }
    }

    /**
     * Determine the current display orientation and rotate the camera preview
     * accordingly.
     */
    public void determineDisplayOrientation() {
        CameraInfo cameraInfo = new CameraInfo();
        Camera.getCameraInfo(cameraId, cameraInfo);

        int rotation = getActivity().getWindowManager().getDefaultDisplay().getRotation();
        int degrees = 0;

        switch (rotation) {
            case Surface.ROTATION_0:
                degrees = 0;
                break;

            case Surface.ROTATION_90:
                degrees = 90;
                break;

            case Surface.ROTATION_180:
                degrees = 180;
                break;

            case Surface.ROTATION_270:
                degrees = 270;
                break;
        }

        int displayOrientation;

        if (cameraInfo.facing == Camera.CameraInfo.CAMERA_FACING_FRONT) {
            displayOrientation = (cameraInfo.orientation + degrees) % 360;
            displayOrientation = (360 - displayOrientation) % 360;
        } else {
            displayOrientation = (cameraInfo.orientation - degrees + 360) % 360;
        }

        camera.setDisplayOrientation(displayOrientation);
    }

    /**
     * Setup the camera parameters.
     */
    public void setupCamera() {
        Camera.Parameters parameters = camera.getParameters();

        Size bestPreviewSize = determineBestPreviewSize(parameters);
        Size bestPictureSize = determineBestPictureSize(parameters);

        parameters.setPreviewSize(bestPreviewSize.width, bestPreviewSize.height);
        parameters.setPictureSize(bestPictureSize.width, bestPictureSize.height);

        // Set aut-focus to continues
        if (parameters.getSupportedFocusModes().contains(
                Camera.Parameters.FOCUS_MODE_CONTINUOUS_VIDEO)) {
            parameters.setFocusMode(Camera.Parameters.FOCUS_MODE_CONTINUOUS_VIDEO);
        }

        parameters.setFlashMode(Camera.Parameters.FLASH_MODE_AUTO);
        parameters.setSceneMode(Camera.Parameters.SCENE_MODE_AUTO);
        parameters.setWhiteBalance(Camera.Parameters.WHITE_BALANCE_AUTO);
        parameters.setExposureCompensation(0);
        parameters.setPictureFormat(ImageFormat.JPEG);
        parameters.setJpegQuality(100);
        parameters.setRotation(90);

        camera.setParameters(parameters);
    }

    private Size determineBestPreviewSize(Camera.Parameters parameters) {
        List<Size> sizes = parameters.getSupportedPreviewSizes();

        return determineBestSize(sizes, PREVIEW_SIZE_MAX_WIDTH);
    }

    private Size determineBestPictureSize(Camera.Parameters parameters) {
        List<Size> sizes = parameters.getSupportedPictureSizes();

        return determineBestSize(sizes, PICTURE_SIZE_MAX_WIDTH);
    }

    protected Size determineBestSize(List<Size> sizes, int widthThreshold) {
        Size bestSize = null;

        for (Size currentSize : sizes) {
            boolean isDesiredRatio = (currentSize.width / 4) == (currentSize.height / 3);
            boolean isBetterSize = (bestSize == null || currentSize.width > bestSize.width);
            boolean isInBounds = currentSize.width <= PICTURE_SIZE_MAX_WIDTH;

            if (isDesiredRatio && isInBounds && isBetterSize) {
                bestSize = currentSize;
            }
        }

        if (bestSize == null) {
            listener.onCameraError();

            return sizes.get(0);
        }

        return bestSize;
    }

    private Camera.Size getBestPreviewSize(Camera.Parameters parameters) {
        Camera.Size bestSize = null;
        List<Camera.Size> sizeList = parameters.getSupportedPreviewSizes();

        bestSize = sizeList.get(0);

        for (int i = 1; i < sizeList.size(); i++) {
            if ((sizeList.get(i).width * sizeList.get(i).height) >
                    (bestSize.width * bestSize.height)) {
                bestSize = sizeList.get(i);
            }
        }

        return bestSize;
    }

    /**
     * On camera preview surface created.
     *
     * @param holder
     */
    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        this.surfaceHolder = holder;

        startCameraPreview();
    }

    /**
     * On camera preview surface changed.
     */
    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
        // The interface forces us to have this method but we don't need it
        // up to now.
    }

    /**
     * On camera preview surface getting destroyed.
     */
    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        // We don't need to handle this case as the fragment takes care of
        // releasing the camera when needed.
        if (camera != null) {
            stopCameraPreview();
        }
    }


    private Camera.PictureCallback mPictureCallback = new Camera.PictureCallback() {

        @Override
        public void onPictureTaken(byte[] data, Camera camera) {

            showSaveImageActivity(data);

        }
    };



    /* PreviewCallback()
         *
         * this callback captures the preview at every frame
         * and puts it in a byte buffer. we will evaluate if
         * this is a frame that we want to process, and if so,
         * we will send it to an asynchronous thread that will
         * process it to an ARGB Bitmap and POST it to the server
         *
        */
    private long timestamp = 0;
    double timeGap = 0;
    public void onPreviewFrame(final byte[] data, Camera camera) {
        // Process the data inside the job for higher performance
        // Limit number of frames taken to 1000 milliseconds frame per second
        timeGap = timeGap + System.currentTimeMillis() - timestamp;
        timestamp = System.currentTimeMillis();


        if (timeGap > frameRateLimit && takeNextFrame) {
            Log.v("CameraTest", "Time Gap = " + timeGap);
            takeNextFrame = false;
            timeGap = 0;

            jobManager.addJobInBackground(new PostImageJob(data, camera.getParameters()));
        }
    }

    int getGradientColor(double value) {
        // Inverse 1 to 0 and 0 to 1
        value = 1 - value;
        return android.graphics.Color.HSVToColor(new float[]{(float) value * 120f, 1f, 1f});
    }

    public void onEventMainThread(PostingImageEvent postingImageEvent) {
        //we could just add this to top or replace element instead of refreshing whole list
        imagePreviewData.setText("Job ID: " + postingImageEvent.getLocalId());

    }

    public void onEventMainThread(PostedImageEvent postedImageEvent) {
        // Turn float value to color, 0 --> Green, 1.0 --> Red
//        Log.d(TAG, "Prediction: " + postedImageEvent.getScanResultValue() + " , Message: " + postedImageEvent.getScanResultMessage());

//        imagePreviewData.setBackgroundColor(getGradientColor((double) postedImageEvent.getScanResultValue()));
        liveScanPreview.setBackgroundColor(getGradientColor((double) postedImageEvent.getScanResultValue()));

        // enable processing next frame into a new job
        takeNextFrame = true;
    }

    public void onEventMainThread(ScanModeChangeEvent scanModeChangeEvent) {
        //we could just add this to top or replace element instead of refreshing whole list
        Log.d("isChecked", "event: " + scanModeChangeEvent.getScanMode());
        if (scanModeChangeEvent.getScanMode()) {
            liveScanPreview.setPadding(0, 0, 0, 0);
            imagePreviewData.setVisibility(View.GONE);
            getActivity().findViewById(R.id.saliency_mode_imageView).setVisibility(View.VISIBLE);
            getActivity().setTitle(getActivity().getString(R.string.saliency_mode_activity_title));
        } else {
            liveScanPreview.setPadding(20, 20, 20, 20);
            imagePreviewData.setVisibility(View.VISIBLE);
            getActivity().findViewById(R.id.saliency_mode_imageView).setVisibility(View.GONE);
            getActivity().setTitle(getActivity().getString(R.string.live_scan_activity_title));

        }
    }


    public void showSaveImageActivity(byte[] data) {
        // Call Save Image Activity passing PictureFile and Picture Data
        Intent intent = new Intent(getActivity(), SaveImageActivity.class);
        intent.putExtra("data", data);
        startActivity(intent);
    }



    public void onEventMainThread(CancelSaveImageEvent cancelSaveImageEvent) {
        // return to Preview Mode if the picture is not saved
        captureButton.setVisibility(View.VISIBLE);
        startCameraPreview();

    }

}
