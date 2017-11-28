package org.kkdev.v2raygo;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;

import libv2ray.Libv2ray;

public class V2RayAdvancedStabilityAssist extends Service {
    public V2RayAdvancedStabilityAssist() {
    }

    final static int V2RayAdvancedStabilityAssist_MONITORME=1;
    final static int V2RayAdvancedStabilityAssist_MONITORSTARTED=2;
    final static int V2RayAdvancedStabilityAssist_MONITORDISENGAGE=3;
    final static int V2RayAdvancedStabilityAssist_MONITORSTOP=4;
    int processid=0;
    final V2RayAdvancedStabilityAssist me = this;
    final SASSS ssassasassas = new SASSS();

    libv2ray.StabilityAssist sa = Libv2ray.getStabilityAssist();

    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        final Messenger mMessenger = new Messenger(new IncomingHandler());
        return mMessenger.getBinder();
    }

    class IncomingHandler extends Handler {
        @Override
        public void handleMessage(Message msg)  {
            switch (msg.what) {
                case V2RayAdvancedStabilityAssist_MONITORME:
                    processid=msg.getData().getInt("ProcessID");
                    sa.setSASS(ssassasassas);
                    regist();
                    sa.start();
                    Message resp = Message.obtain(null, V2RayAdvancedStabilityAssist_MONITORSTARTED);
                    try {
                        msg.replyTo.send(resp);
                    } catch (RemoteException e) {
                        e.printStackTrace();
                    }
                    break;
                case V2RayAdvancedStabilityAssist_MONITORDISENGAGE:
                    processid=0;
                    Message resp2 = Message.obtain(null, V2RayAdvancedStabilityAssist_MONITORSTOP);
                    sa.stop();
                    try {
                        msg.replyTo.send(resp2);
                    } catch (RemoteException e) {
                        e.printStackTrace();
                    }

                default:
                    super.handleMessage(msg);
            }
        }
    }


    /** Messenger for communicating with the service. */
    Messenger mService = null;

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
            startV2Ray();
        }

        public void onServiceDisconnected(ComponentName className) {
            // This is called when the connection with the service has been
            // unexpectedly disconnected -- that is, its process crashed.
            mService = null;
            mBound = false;


        }
    };
    private void restart(){
        if(processid!=0){
            try {
                android.os.Process.killProcess(processid);
                processid=0;
                Intent intent = new Intent(this,V2RayDaemon.class);
                this.startService(intent);
                this.bindService(new Intent(this, V2RayDaemon.class), mConnection,
                        Context.BIND_AUTO_CREATE|Context.BIND_ABOVE_CLIENT);
            }catch(Exception e) {
            e.printStackTrace();
            }


        }
    }

    private void startV2Ray(){
        alterRunningStatus(true);
    }

    private void alterRunningStatus(boolean running){

        // Create and send a message to the service, using a supported 'what' value
        Message msg = Message.obtain(null, running?V2RayDaemon.MSG_Start_V2Ray:V2RayDaemon.MSG_Stop_V2Ray , 0, 0);
        sendMsgToV2RayDaemon(msg);

        Notification status_noti = new Notification.Builder(this)
                .setContentTitle(getText(R.string.app_noti_title))
                .setContentText("Adv. Stability Assist have restarted V2Ray")
                .setSmallIcon(R.drawable.ic_rised_fist)
                .setPriority(Notification.PRIORITY_MIN)
                .build();

        NotificationManager mNotificationManager =
                (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        mNotificationManager.notify(0, status_noti);

    }
    private void sendMsgToV2RayDaemon(Message msg){
        if (!mBound) {
            //showerrtoast();
            return;
        };
        // Create and send a message to the service, using a supported 'what' value
        msg.replyTo = new Messenger(new RunningResponseHandler());
        try {
            mService.send(msg);
        } catch (RemoteException e) {
            e.printStackTrace();
            //showerrtoast();

        }
    }

    private void regist(){
        IntentFilter filter = new IntentFilter();
        filter.addAction(Intent.ACTION_SCREEN_OFF);
        filter.addAction(Intent.ACTION_SCREEN_ON);
        BroadcastReceiver mReceiver = new ScreenReceiver();
        registerReceiver(mReceiver, filter);
    }

    public class ScreenReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            if(intent.getAction().equals(Intent.ACTION_SCREEN_OFF)){
                sa.setForground(false);
            }
            if(intent.getAction().equals(Intent.ACTION_SCREEN_ON)){
                sa.setForground(true);
                sa.probeNowOpi();
            }
        }
    }

    class RunningResponseHandler extends Handler {

        @Override
        public void handleMessage(Message msg) {

            int respCode = msg.what;
            switch (respCode) {
                //legacy data ignored
                case V2RayDaemon.MSG_CheckLibVerP: {
                    String result = msg.getData().getString("Status");
                    break;
                }
                case V2RayDaemon.MSG_CheckLibVerR:{
                    String LibVer = msg.getData().getString("LibVerS");
                    Boolean Running = msg.getData().getBoolean("Running");
                    break;

                }
            }
        }

    }

    public class SASSS implements libv2ray.StabilityAssistSupportSet{
        @Override
        public void onProbeFailed() {
            me.restart();
        }
    }
}
