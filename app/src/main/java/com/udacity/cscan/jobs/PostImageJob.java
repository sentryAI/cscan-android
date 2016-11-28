package com.udacity.cscan.jobs;

import android.hardware.Camera;

import com.path.android.jobqueue.Job;
import com.path.android.jobqueue.Params;
import com.path.android.jobqueue.RetryConstraint;
import com.stanford.Diagnostic;
import com.stanford.Livingstone;
import com.udacity.cscan.events.PostedImageEvent;
import com.udacity.cscan.events.PostingImageEvent;

import de.greenrobot.event.EventBus;

public class PostImageJob extends Job {
    private long localId;
    private byte[] data;
    private Camera.Parameters cameraParameters;

    public PostImageJob(byte[] data, Camera.Parameters cameraParameters) {
        //order of images matter, we don't want to send two in parallel so we use groupBy
        // and should not be persisted to keep performance high.
        super(new Params(Priority.MID).groupBy("post_image"));
        //use a negative id so that it cannot collide w/ images ids (TBD)
        //we have to set local id here so it gets serialized into job (to find tweet later on)
        localId = -System.currentTimeMillis();
        this.data = data;
        this.cameraParameters = cameraParameters;
    }

    @Override
    public void onAdded() {
        // Job has been saved to disk.
        // This is a good place to dispatch a UI event to indicate the job will eventually run.
        // In this example, it would be good to update the UI with the newly posted tweet.
        //job has been secured to disk, update UI

        if (localId != 0) {
            EventBus.getDefault().post(new PostingImageEvent(localId));
        } else {
            // TODO: 11/24/15 error handling
        }

    }

    @Override
    public void onRun() throws Throwable {
        // Job logic goes here. Calling the Java library to process the image data is done here.

//        int width = cameraParameters.getPreviewSize().width;
//        int height = cameraParameters.getPreviewSize().height;
//
//        YuvImage yuv = new YuvImage(data, cameraParameters.getPreviewFormat(), width, height, null);
//
//        ByteArrayOutputStream out = new ByteArrayOutputStream();
//        yuv.compressToJpeg(new Rect(0, 0, width, height), 50, out);
//
//        byte[] bytes = out.toByteArray();
//        final Bitmap bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);


        Diagnostic diagnostic = Livingstone.GeneratePrediction.diagnose(localId, "");
        Float scanResultValue = diagnostic.getScanResult();
        String scanResultMessage = diagnostic.getScanMessage();

        EventBus.getDefault().post(new PostedImageEvent(scanResultValue, scanResultMessage));
    }

    @Override
    protected void onCancel() {
        // Job has exceeded retry attempts or shouldReRunOnThrowable() has returned false.
        // TODO: 11/24/15 delete the job
    }

    @Override
    protected RetryConstraint shouldReRunOnThrowable(Throwable throwable, int runCount,
                                                     int maxRunCount) {
        // An error occurred in onRun.
        // Return value determines whether this job should retry or cancel. You can further
        // specifcy a backoff strategy or change the job's priority. You can also apply the
        // delay to the whole group to preserve jobs' running order.
        return RetryConstraint.createExponentialBackoff(runCount, 1000);
    }
}
