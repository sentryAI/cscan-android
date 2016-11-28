package com.udacity.cscan.dialogs;

import android.app.Activity;
import android.app.Dialog;
import android.content.DialogInterface;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;

import com.udacity.cscan.R;
import com.udacity.cscan.events.CancelSaveImageEvent;
import com.udacity.cscan.events.SaveImageEvent;

import de.greenrobot.event.EventBus;

/**
 * SaveImage Dialog
 * create an instance of this fragment.
 */
public class SaveImageDialogFragment extends DialogFragment {
    /**
     * The system calls this to get the DialogFragment's layout, regardless
     * of whether it's being displayed as a dialog or an embedded fragment.
     *
     * @param url
     */

    View rootView;

    public SaveImageDialogFragment newInstance(String url, byte[] data) {
        SaveImageDialogFragment frag = new SaveImageDialogFragment();
        Bundle args = new Bundle();
        args.putString("url", url);
        args.putByteArray("data", data);
        frag.setArguments(args);
        return frag;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout to use as dialog or embedded fragment
        rootView = inflater.inflate(R.layout.fragment_save_image_dialog, container, false);
        ImageView save_imageView = (ImageView) rootView.findViewById(R.id.save_imageView);
        Bitmap bmp = BitmapFactory.decodeByteArray(getArguments().getByteArray("data"), 0, getArguments().getByteArray("data").length);
        save_imageView.setImageBitmap(bmp);
        Button cancel_button = (Button) rootView.findViewById(R.id.cancel_button);
        cancel_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                EventBus.getDefault().post(new CancelSaveImageEvent());
                dismiss();
            }
        });
        Button save_button = (Button) rootView.findViewById(R.id.save_button);
        save_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                EventBus.getDefault().post(new SaveImageEvent(getArguments().getByteArray("data")));
                dismiss();
            }
        });
        return rootView;
    }

    /**
     * The system calls this only when creating the layout in a dialog.
     */
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        // The only reason you might override this method when using onCreateView() is
        // to modify any dialog characteristics. For example, the dialog includes a
        // title by default, but your custom layout might not need it. So here you can
        // remove the dialog title, but you must call the superclass to get the Dialog.
        Dialog dialog = super.onCreateDialog(savedInstanceState);
        dialog.setCancelable(true);
        dialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
            @Override
            public void onDismiss(DialogInterface dialog) {
                Log.d("setOnDismissListener", "setOnDismissListener");
                EventBus.getDefault().post(new CancelSaveImageEvent());

            }
        });
        return dialog;
    }

    @Override
    public void onDismiss(final DialogInterface dialog) {
        super.onDismiss(dialog);
        EventBus.getDefault().post(new CancelSaveImageEvent());
        Log.d("setOnDismissListener", "onDismiss");

        final Activity activity = getActivity();
        if (activity instanceof DialogInterface.OnDismissListener) {
            ((DialogInterface.OnDismissListener) activity).onDismiss(dialog);
        }
    }

}
