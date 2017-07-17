package org.kkdev.v2raygo;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.nbsp.materialfilepicker.filter.CompositeFilter;
import com.nbsp.materialfilepicker.filter.HiddenFilter;
import com.nbsp.materialfilepicker.ui.FilePickerActivity;

import org.kkdev.v2raygo.ProxyFile.ProxyConfigureFile;
import org.kkdev.v2raygo.ProxyFile.ProxyConfigureFile.ProxyConfItem;

import java.io.FileFilter;
import java.util.ArrayList;

import libv2ray.Libv2ray;
import libv2ray.V2RayContext;

import static android.app.Activity.RESULT_OK;

/**
 * A fragment representing a list of Items.
 * <p/>
 * Activities containing this fragment MUST implement the {@link OnListFragmentInteractionListener}
 * interface.
 */

public class ProxyConfigureFileFragment extends Fragment {

    // TODO: Customize parameter argument names
    private static final String ARG_COLUMN_COUNT = "column-count";
    // TODO: Customize parameters
    private int mColumnCount = 1;
    private OnListFragmentInteractionListener mListener;
    private V2RayContext v2RayContext = Libv2ray.newLib2rayContext();
    private final ProxyConfigureFileFragment me =this;
    private MyProxyConfigureFileRecyclerViewAdapter currentad;

    public class VCCallback implements  libv2ray.V2RayContextCallbacks {

        @Override
        public void onFileSelectTriggerd() {

            if (ContextCompat.checkSelfPermission(getContext(),
                    Manifest.permission.WRITE_EXTERNAL_STORAGE)
                    != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(getActivity(),
                        new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                        0);

            }else{
                ConfigureSelector();
            }



        }

        private void ConfigureSelector() {
            CompositeFilter filter = getFilter();

            Intent intent = new Intent(getActivity(), FilePickerActivity.class);
            intent.putExtra(FilePickerActivity.ARG_FILTER, filter);
            startActivityForResult(intent,1);
        }

        @Override
        public void onRefreshNeeded() {
            currentad.notifyDataSetChanged();
        }

        private CompositeFilter getFilter() {
            ArrayList<FileFilter> filters = new ArrayList<>();


            filters.add(new HiddenFilter());



            return new CompositeFilter(filters);
        }


    }


    /**
     * Mandatory empty constructor for the fragment manager to instantiate the
     * fragment (e.g. upon screen orientation changes).
     */
    public ProxyConfigureFileFragment() {
        v2RayContext.setCallbacks(new VCCallback());
    }

    // TODO: Customize parameter initialization
    @SuppressWarnings("unused")
    public static ProxyConfigureFileFragment newInstance(int columnCount) {
        ProxyConfigureFileFragment fragment = new ProxyConfigureFileFragment();
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
        View view = inflater.inflate(R.layout.fragment_proxyconfigurefile_list, container, false);

        reset(view);


        return view;
    }

    private void reset(View view) {
        // Set the adapter
        if (view instanceof RecyclerView) {
            Context context = view.getContext();
            RecyclerView recyclerView = (RecyclerView) view;
            if (mColumnCount <= 1) {
                recyclerView.setLayoutManager(new LinearLayoutManager(context));
            } else {
                recyclerView.setLayoutManager(new GridLayoutManager(context, mColumnCount));
            }
            currentad = new MyProxyConfigureFileRecyclerViewAdapter(ProxyConfigureFile.CreateFromStringArray(v2RayContext.listConfigureFileDir(),v2RayContext), mListener);
            recyclerView.setAdapter(currentad);
        }
    }





    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 1 && resultCode == RESULT_OK) {
            String filePath = data.getStringExtra(FilePickerActivity.RESULT_FILE_PATH);
            // Do anything with file
            v2RayContext.assignConfigureFile(filePath);
            currentad.mValues = ProxyConfigureFile.CreateFromStringArray(v2RayContext.listConfigureFileDir(),v2RayContext);
            currentad.notifyDataSetChanged();
        }
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        /*
        if (context instanceof OnListFragmentInteractionListener) {
            mListener = (OnListFragmentInteractionListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnListFragmentInteractionListener");
        }*/
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

    public class ProxyConfigureHandlerClass implements OnListFragmentInteractionListener {

        @Override
        public void onListFragmentInteraction(ProxyConfItem item) {
        }
    }
    public interface OnListFragmentInteractionListener {
        // TODO: Update argument type and name
        void onListFragmentInteraction(ProxyConfItem item);
    }

    private class CtxToListAdop{
        private V2RayContext v2RayContext;
        public CtxToListAdop(V2RayContext cx){
            v2RayContext=cx;
        }
    }
}
