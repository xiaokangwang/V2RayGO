package org.kkdev.v2raygo;

import android.Manifest;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.os.PersistableBundle;
import android.os.RemoteException;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.Switch;
import android.widget.TextView;

import com.nbsp.materialfilepicker.MaterialFilePicker;
import com.nbsp.materialfilepicker.ui.FilePickerActivity;

import java.util.regex.Pattern;



public class MainScreen extends AppCompatActivity {
    String redeploy="1";
    /** Messenger for communicating with the service. */
    Messenger mService = null;
    boolean nocheckact=false;

    /** Flag indicating whether we have called bind on the service. */
    boolean mBound;

    /**
     * Class for interacting with the main interface of the service.
     */
    private ServiceConnection mConnection = new ServiceConnection() {
        public void onServiceConnected(ComponentName className, IBinder service) {
            // This is called when the connection with the service has been
            // established, giving us the object we can use to
            // interact with the service.  We are communicating with the
            // service using a Messenger, so here we get a client-side
            // representation of that from the raw IBinder object.
            mService = new Messenger(service);
            mBound = true;
            getStatus();
        }

        public void onServiceDisconnected(ComponentName className) {
            // This is called when the connection with the service has been
            // unexpectedly disconnected -- that is, its process crashed.
            mService = null;
            mBound = false;
        }
    };

    @Override
    protected void onResume() {

        bindService(new Intent(this, V2RayDaemon.class), mConnection,
                Context.BIND_AUTO_CREATE);
        final TextView textView_current_Status = (TextView)findViewById(R.id.text_status);
        final Button Pick_Conf = (Button)findViewById(R.id.btn_pick_conf);
        final Button show_logcat = (Button)findViewById(R.id.logcat_view);
        final Switch IsEnabled = (Switch)findViewById(R.id.enable_v2ray);
        final MainScreen me = this;

        Pick_Conf.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                new MaterialFilePicker()
                        .withActivity(me)
                        .withRequestCode(1)
                        .withFilterDirectories(false) // Set directories filterable (false by default)
                        .withHiddenFiles(true) // Show hidden files and folders
                        .start();
            }
        });

        IsEnabled.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                if(nocheckact){
                    return;
                }
                getStatus();
                alterRunningStatus(b);
            }
        });

        show_logcat.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent logcatIntent = new Intent(me,Logcatshow.class);
                startActivity(logcatIntent);
            }
        });

        getStatus();
        super.onResume();
    }

    @Override
    protected void onStart() {
        super.onStart();
        bindService(new Intent(this, V2RayDaemon.class), mConnection,
                Context.BIND_AUTO_CREATE);
        getStatus();
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        bindService(new Intent(this, V2RayDaemon.class), mConnection,
                Context.BIND_AUTO_CREATE);
        getStatus();
    }

    @Override
    public void onContentChanged() {
        super.onContentChanged();
        bindService(new Intent(this, V2RayDaemon.class), mConnection,
                Context.BIND_AUTO_CREATE);
        getStatus();
    }

    @Override
    protected void onPostCreate(@Nullable Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        bindService(new Intent(this, V2RayDaemon.class), mConnection,
                Context.BIND_AUTO_CREATE);
        getStatus();
    }

    @Override
    protected void onPostResume() {
        super.onPostResume();
        bindService(new Intent(this, V2RayDaemon.class), mConnection,
                Context.BIND_AUTO_CREATE);
        getStatus();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 1 && resultCode == RESULT_OK) {
            String filePath = data.getStringExtra(FilePickerActivity.RESULT_FILE_PATH);
            // Do anything with file
            SharedPreferences settings = getSharedPreferences("org.kkdev.v2raygo_main",MODE_MULTI_PROCESS);
            SharedPreferences.Editor ed=settings.edit();
            ed.putString("configureFile",filePath);
            ed.commit();
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_screen);

        final MainScreen me = this;

        Intent intent = new Intent(me,V2RayDaemon.class);
        startService(intent);

        bindService(new Intent(this, V2RayDaemon.class), mConnection,
                Context.BIND_AUTO_CREATE);
        getStatus();

        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.WRITE_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                        0);

            }



    }



    private void alterRunningStatus(boolean running){

        if (!mBound) return;
        // Create and send a message to the service, using a supported 'what' value
        Message msg = Message.obtain(null, running?V2RayDaemon.MSG_Start_V2Ray:V2RayDaemon.MSG_Stop_V2Ray , 0, 0);
        msg.replyTo = new Messenger(new ResponseHandler());
        try {
            mService.send(msg);
        } catch (RemoteException e) {
            e.printStackTrace();
        }

    }

    private void getStatus(){
        Intent intent = new Intent(this,V2RayDaemon.class);
        startService(intent);

        bindService(new Intent(this, V2RayDaemon.class), mConnection,
                Context.BIND_AUTO_CREATE);

        if (!mBound) return;
        // Create and send a message to the service, using a supported 'what' value
        Message msg = Message.obtain(null, V2RayDaemon.MSG_CheckLibVer , 0, 0);
        msg.replyTo = new Messenger(new ResponseHandler());
        try {
            mService.send(msg);
        } catch (RemoteException e) {
            e.printStackTrace();
        }

    }


    class ResponseHandler extends Handler {

        @Override
        public void handleMessage(Message msg) {

            final TextView textView_current_Status = (TextView)findViewById(R.id.text_status);
            final TextView textView_rwt = (TextView)findViewById(R.id.Remotewt);
            final Button Pick_Conf = (Button)findViewById(R.id.btn_pick_conf);
            final Switch IsEnabled = (Switch)findViewById(R.id.enable_v2ray);

            int respCode = msg.what;
            switch (respCode) {
                case V2RayDaemon.MSG_CheckLibVerP: {
                    String result = msg.getData().getString("Status");
                    textView_rwt.setText(result);
                    getStatus();
                    break;
                }
                case V2RayDaemon.MSG_CheckLibVerR:{
                    String LibVer = msg.getData().getString("LibVerS");
                    Boolean Running = msg.getData().getBoolean("Running");

                    SharedPreferences settings = getSharedPreferences("org.kkdev.v2raygo_main",MODE_MULTI_PROCESS);
                    String configureFile = settings.getString("configureFile","");

                    String CurrentU="\n(Using "+configureFile+")";
                    textView_current_Status.setText("LibV2Ray " + LibVer+" is "+ (Running?"Running":"Stopped")+CurrentU );
                    nocheckact=true;
                    IsEnabled.setChecked(Running);
                    nocheckact=false;
                    break;

                }
            }
        }

    }

}
