package org.kkdev.v2raygo;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.res.ColorStateList;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;


/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link FragmentStatusBeief.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link FragmentStatusBeief#newInstance} factory method to
 * create an instance of this fragment.
 */
public class FragmentStatusBeief extends Fragment {
    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    private OnFragmentInteractionListener mListener;

    public FragmentStatusBeief() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment FragmentStatusBeief.
     */
    // TODO: Rename and change types and number of parameters
    public static FragmentStatusBeief newInstance(String param1, String param2) {
        FragmentStatusBeief fragment = new FragmentStatusBeief();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        FloatingActionButton enableFab = (FloatingActionButton)view.findViewById(R.id.fab_switchservice);
        enableFab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startAndBindService();
                alterV2RayRunningStatus();
            }
        });
    }

    @Override
    public void onResume() {
        super.onResume();
        startAndBindService();
        if (mBound) {
            RequestRunningStatusUpdate();
        }
    }

    private boolean alterV2RayRunningStatus(){
        alterRunningStatus(!isV2RayRunning);
        return false;
    }
    private boolean V2RayRunningStatusDeliver(){
        FloatingActionButton enableFab = (FloatingActionButton)getView().findViewById(R.id.fab_switchservice);
        enableFab.setBackgroundTintList(ColorStateList.valueOf(getResources().getColor(isV2RayRunning?R.color.green:R.color.darkred)));
        enableFab.setImageResource(isV2RayRunning?R.drawable.ic_done_black_24dp:R.drawable.ic_prison);

        TextView Version = (TextView)getView().findViewById(R.id.Brief_Info);
        Version.setText(V2RayVersion);
        return false;
    }
    StringBuilder Logstrb = new StringBuilder();
    int currentLogLength = 0;
    int lastremovefrom = 0;
    private boolean OnlogDelivered(String NewLog){
        try {
            TextView Log = (TextView)getView().findViewById(R.id.LogOut);
            currentLogLength++;
            if(currentLogLength>=10){
                lastremovefrom=Logstrb.indexOf("\n");
                Logstrb.delete(0,lastremovefrom+1);
            }
            Logstrb.append(NewLog+"\n");
            Log.setText(Logstrb.toString());
        }catch (Exception e){
            e.printStackTrace();
        }

        return false;
    }

    /** Messenger for communicating with the service. */
    Messenger mService = null;
    boolean nocheckact=false;

    /** Flag indicating whether we have called bind on the service. */
    boolean mBound;


    private ServiceConnection mConnection = new ServiceConnection() {
        public void onServiceConnected(ComponentName className, IBinder service) {
            // This is called when the connection with the service has been
            // established, giving us the object we can use to
            // interact with the service.  We are communicating with the
            // service using a Messenger, so here we get a client-side
            // representation of that from the raw IBinder object.
            mService = new Messenger(service);
            mBound = true;
            //getStatus();
            RequestRunningStatusUpdate();
        }

        public void onServiceDisconnected(ComponentName className) {
            // This is called when the connection with the service has been
            // unexpectedly disconnected -- that is, its process crashed.
            mService = null;
            mBound = false;

            startAndBindService();
        }
    };


    private void startAndBindService(){
        Intent intent = new Intent(getContext(),V2RayDaemon.class);
        getContext().startService(intent);

        getContext().bindService(new Intent(getContext(), V2RayDaemon.class), mConnection,
                Context.BIND_AUTO_CREATE|Context.BIND_ABOVE_CLIENT);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_fragment_intent_button, container, false);
    }

    // TODO: Rename method, update argument and hook method into UI event
    public void onButtonPressed(Uri uri) {
        if (mListener != null) {
            mListener.onFragmentInteraction(uri);
        }
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        /*
        if (context instanceof OnFragmentInteractionListener) {
            mListener = (OnFragmentInteractionListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnFragmentInteractionListener");
        }*/
        startAndBindService();
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
     * <p>
     * See the Android Training lesson <a href=
     * "http://developer.android.com/training/basics/fragments/communicating.html"
     * >Communicating with Other Fragments</a> for more information.
     */
    public interface OnFragmentInteractionListener {
        // TODO: Update argument type and name
        void onFragmentInteraction(Uri uri);
    }


    private boolean isV2RayRunning=false;
    private String V2RayVersion;

    class RunningResponseHandler extends Handler {

        @Override
        public void handleMessage(Message msg) {

            int respCode = msg.what;
            switch (respCode) {
                //legacy data ignored
                case V2RayDaemon.MSG_CheckLibVerP: {
                    String result = msg.getData().getString("Status");
                    RequestRunningStatusUpdate();
                    OnlogDelivered(result);
                    break;
                }
                case V2RayDaemon.MSG_CheckLibVerR:{
                    String LibVer = msg.getData().getString("LibVerS");
                    Boolean Running = msg.getData().getBoolean("Running");
                    V2RayVersion = LibVer;
                    isV2RayRunning = Running;
                    V2RayRunningStatusDeliver();
                    break;

                }
            }
        }

    }

    private void alterRunningStatus(boolean running){

        // Create and send a message to the service, using a supported 'what' value
        Message msg = Message.obtain(null, running?V2RayDaemon.MSG_Start_V2Ray:V2RayDaemon.MSG_Stop_V2Ray , 0, 0);
        sendMsgToV2RayDaemon(msg);

    }

    private void RequestRunningStatusUpdate(){

        // Create and send a message to the service, using a supported 'what' value
        Message msg = Message.obtain(null, V2RayDaemon.MSG_CheckLibVer , 0, 0);
        sendMsgToV2RayDaemon(msg);

    }

    private void sendMsgToV2RayDaemon(Message msg){
        if (!mBound) {
            showerrtoast();
            return;
        };
        // Create and send a message to the service, using a supported 'what' value
        msg.replyTo = new Messenger(new RunningResponseHandler());
        try {
            mService.send(msg);
        } catch (RemoteException e) {
            e.printStackTrace();
            showerrtoast();

        }
    }

    private void showerrtoast() {
        Toast.makeText(getActivity(), (String)"Failed to Progress your Request.",
                Toast.LENGTH_LONG).show();
        return;
    }

}
