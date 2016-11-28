package com.udacity.cscan;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.udacity.cscan.daoimage.Image;
import com.udacity.cscan.daoimage.ImageDao;
import com.udacity.cscan.events.AddedImageEvent;

import de.greenrobot.event.EventBus;

/**
 * A fragment representing a list of Items.
 * <p/>
 * Activities containing this fragment MUST implement the {@link OnListFragmentInteractionListener}
 * interface.
 */
public class ImagesFragment extends Fragment {

    // TODO: Customize parameter argument names
    private static final String ARG_COLUMN_COUNT = "column-count";
    // TODO: Customize parameters
    private int mColumnCount = 2;
    private ImagesFragment.OnListFragmentInteractionListener mListener;

    SimpleCursorRecyclerAdapter simpleCursorRecyclerAdapter;
    RecyclerView recyclerView;

    /**
     * Mandatory empty constructor for the fragment manager to instantiate the
     * fragment (e.g. upon screen orientation changes).
     */
    public ImagesFragment() {
    }

    // TODO: Customize parameter initialization
    @SuppressWarnings("unused")
    public static ImagesFragment newInstance(int columnCount) {
        ImagesFragment fragment = new ImagesFragment();
        Bundle args = new Bundle();
        args.putInt(ARG_COLUMN_COUNT, columnCount);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (getArguments() != null) {
            mColumnCount = getArguments().getInt(ARG_COLUMN_COUNT);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_image_list, container, false);

        // Set the adapter
        if (view instanceof RecyclerView) {
            Context context = view.getContext();
            recyclerView = (RecyclerView) view;
            if (mColumnCount <= 1) {
                recyclerView.setLayoutManager(new LinearLayoutManager(context));
            } else {
                recyclerView.setLayoutManager(new GridLayoutManager(context, mColumnCount));
            }
//            recyclerView.setAdapter(new ImageRecyclerViewAdapter(DummyContent.ITEMS, mListener));
            fillRecycleView(recyclerView);
        }
        return view;
    }

    private void fillRecycleView(RecyclerView recyclerView) {

        String idColumn = ImageDao.Properties.Id.columnName;
        String titleColumn = ImageDao.Properties.Title.columnName;
        String commentColumn = ImageDao.Properties.Comment.columnName;
        String dateColumn = ImageDao.Properties.Date.columnName;
        String formateddDateColumn = ImageDao.Properties.Formateddate.columnName;
        String imageUrlColumn = ImageDao.Properties.Imageurl.columnName;

        String orderBy = dateColumn + " COLLATE LOCALIZED ASC";
        CscanApplication.getInstance().cursor = CscanApplication.getInstance().db.query(CscanApplication.getInstance().imageDao.getTablename(), CscanApplication.getInstance().imageDao.getAllColumns(), null, null, null, null, orderBy);
        String[] from = {idColumn, titleColumn, commentColumn, formateddDateColumn, dateColumn, imageUrlColumn};
        int[] to = {R.id.title_TextView};

        simpleCursorRecyclerAdapter = new SimpleCursorRecyclerAdapter(mListener, R.layout.fragment_images, CscanApplication.getInstance().cursor, from, to);

        recyclerView.setAdapter(simpleCursorRecyclerAdapter);
    }

    public void onEventMainThread(AddedImageEvent addedImageEvent) {
        //we could just add this to top or replace element instead of refreshing whole list
        fillRecycleView(recyclerView);
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnListFragmentInteractionListener) {
            mListener = (OnListFragmentInteractionListener) context;
            EventBus.getDefault().register(this);

        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnListFragmentInteractionListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    /**
     * This interface must be implemented by activities that contain this
     * fragment to allow an interaction in this fragment to be communicated
     * to the activity and potentially other fragments contained in that
     * activity.
     * <p/>
     * See the Android Training lesson <a href=
     * "http://developer.android.com/training/basics/fragments/communicating.html"
     * >Communicating with Other Fragments</a> for more information.
     */
    public interface OnListFragmentInteractionListener {
        // TODO: Update argument type and name
        void onListFragmentInteraction(Image image);
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
}
