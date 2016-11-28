package com.udacity.cscan;

import android.database.Cursor;
import android.net.Uri;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.udacity.cscan.daoimage.Image;

import java.io.File;
import java.util.Date;

/**
 * Created by ibrahimhassan on 12/2/15.
 */
public class SimpleCursorRecyclerAdapter extends CursorRecyclerAdapter<SimpleCursorRecyclerAdapter.ViewHolder> {

    private int mLayout;
    private int[] mFrom;
    private int[] mTo;
    private String[] mOriginalFrom;
    private final ImagesFragment.OnListFragmentInteractionListener mListener;


    public SimpleCursorRecyclerAdapter(ImagesFragment.OnListFragmentInteractionListener listener, int layout, Cursor c, String[] from, int[] to) {
        super(c);
        mListener = listener;
        mLayout = layout;
        mTo = to;
        mOriginalFrom = from;
        findColumns(c, from);
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(mLayout, parent, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(final ViewHolder holder, final Cursor cursor) {
        final int[] from = mFrom;

        holder.mTitle.setText(cursor.getString(from[1]));
        holder.mComment.setText(cursor.getString(from[2]));
        holder.mDate.setText(cursor.getString(from[3]));

        holder.mScanImageView.setImageURI(Uri.fromFile(new File(cursor.getString(from[5]))));
        holder.mView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (null != mListener) {
                    // Notify the active callbacks interface (the activity, if the
                    // fragment is attached to one) that an item has been selected.
                    holder.mImage = new Image(cursor.getLong(from[0]), cursor.getString(from[1]), cursor.getString(from[2]), cursor.getString(from[3]), new Date(cursor.getLong(from[4]) * 1000), cursor.getString(from[5]));

                    mListener.onListFragmentInteraction(holder.mImage);
                }
            }
        });

    }

    /**
     * Create a map from an array of strings to an array of column-id integers in cursor c.
     * If c is null, the array will be discarded.
     *
     * @param c    the cursor to find the columns from
     * @param from the Strings naming the columns of interest
     */
    private void findColumns(Cursor c, String[] from) {
        if (c != null) {
            int i;
            int count = from.length;
            if (mFrom == null || mFrom.length != count) {
                mFrom = new int[count];
            }
            for (i = 0; i < count; i++) {
                mFrom[i] = c.getColumnIndexOrThrow(from[i]);
            }
        } else {
            mFrom = null;
        }
    }

    @Override
    public Cursor swapCursor(Cursor c) {
        findColumns(c, mOriginalFrom);
        return super.swapCursor(c);
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        public final View mView;
        public final TextView mTitle;
        public final TextView mComment;
        public final TextView mDate;

        public Image mImage;
        public ImageView mScanImageView;

        public ViewHolder(View view) {
            super(view);
            mView = view;
            mTitle = (TextView) view.findViewById(R.id.title_TextView);
            mComment = (TextView) view.findViewById(R.id.comment_TextView);
            mDate = (TextView) view.findViewById(R.id.date_TextView);
            mScanImageView = (ImageView) view.findViewById(R.id.scan_imageView);

        }

        @Override
        public String toString() {
            return super.toString() + " '" + mComment.getText() + "'";
        }
    }
}

