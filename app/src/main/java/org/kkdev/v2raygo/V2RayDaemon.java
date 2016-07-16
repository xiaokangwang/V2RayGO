package org.kkdev.v2raygo;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;
import android.os.StrictMode;

import go.libv2ray.Libv2ray;
import go.libv2ray.Libv2ray.V2RayPoint;

public class V2RayDaemon extends Service {
    private static final int ONGOING_NOTIFICATION_ID = 1 ;

    Messenger Rmessager;
    V2RayPoint vp = Libv2ray.NewV2RayPoint();
    V2RayCallback vp_callback= new V2RayCallback();

    public V2RayDaemon() {
    }


    static final int MSG_Nil = 1;
    static final int MSG_Start_V2Ray = 2;
    static final int MSG_Stop_V2Ray = 3;
    static final int MSG_CheckLibVer = 4; //Actually status of V2Ray
    static final int MSG_CheckLibVerR = 5;
    static final int MSG_CheckLibVerP = 6;
    class IncomingHandler extends Handler {
        @Override
        public void handleMessage(Message msg)  {
            switch (msg.what) {
                case MSG_Nil:
                break;
                case MSG_Start_V2Ray:
                    if(!vp.getIsRunning()){
                        show_noti("V2Ray running in front.");
                        vp.setCallbacks(vp_callback);
                        SharedPreferences settings = getSharedPreferences("org.kkdev.v2raygo_main",MODE_MULTI_PROCESS);
                        String configureFile = settings.getString("configureFile","");
                        vp.setConfigureFile(configureFile);
                        vp.RunLoop();
                    }
                    break;
                case MSG_CheckLibVer:
                    Long libver=(long)Libv2ray.CheckVersion();
                    Message resp = Message.obtain(null, MSG_CheckLibVerR);
                    Bundle bResp = new Bundle();
                    bResp.putInt("LibVer",libver.intValue());
                    bResp.putBoolean("Running",vp.getIsRunning());
                    resp.setData(bResp);
                    try {
                        msg.replyTo.send(resp);
                        Rmessager=msg.replyTo;
                    } catch (RemoteException e) {
                        e.printStackTrace();
                    }

                    break;
                case MSG_Stop_V2Ray:
                    resign_noti();
                    if(vp.getIsRunning()){
                        vp.StopLoop();
                    }
                    break;
                default:
                    super.handleMessage(msg);
            }
        }
    }
    /**
     * Target we publish for clients to send messages to IncomingHandler.
     */
    final Messenger mMessenger = new Messenger(new IncomingHandler());


    @Override
    public IBinder onBind(Intent intent) {
        // TDO: Return the communication channel to the service.
        return mMessenger.getBinder();
    }

    @Override
    public void onCreate() {
        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();

        StrictMode.setThreadPolicy(policy);
        super.onCreate();
    }

    public int onStartCommand(Intent intent, int flags, int startId) {
        return super.onStartCommand(intent, flags, startId);
    }

    private int show_noti(String ctxtxt){
        Notification status_noti = new Notification.Builder(this)
                .setContentTitle(getText(R.string.app_noti_title))
                .setContentText(ctxtxt)
                .setSmallIcon(R.drawable.ic_vpn_key_black_24dp)
                .build();
        NotificationManager mNotificationManager =
                (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        mNotificationManager.notify(ONGOING_NOTIFICATION_ID, status_noti);
        startForeground(ONGOING_NOTIFICATION_ID, status_noti);
        return 0;
    }
    private int resign_noti(){
        stopForeground(true);
        return 0;
    }

    private void remoteWrite(String ctx){
        if (Rmessager==null){return;}
        try{
            Message resp = Message.obtain(null, MSG_CheckLibVerP);
            Bundle bResp = new Bundle();
            bResp.putString("Status", ctx);
            resp.setData(bResp);
            Rmessager.send(resp);
        } catch (RemoteException e) {
            e.printStackTrace();
        }

    }

    @Override
    public void onDestroy() {

        super.onDestroy();
        startService(new Intent(this, V2RayDaemon.class));

    }

/*    @Override
    public IBinder onBind(Intent intent) {
        // TDO: Return the communication channel to the service.
        throw new UnsupportedOperationException("Not yet implemented");
    }*/

    class V2RayCallback implements Libv2ray.V2RayCallbacks {

        @Override
        public long OnEmitStatus(long l, String s) {
            remoteWrite(s);
            return 0;
        }
    }
}
