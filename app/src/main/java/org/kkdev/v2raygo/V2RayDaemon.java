package org.kkdev.v2raygo;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;
import android.os.StrictMode;
import android.util.Log;

import libv2ray.InterfaceInfo;
import libv2ray.Libv2ray;
import libv2ray.StatControler;
import libv2ray.V2RayCallbacks;
import libv2ray.V2RayPoint;
import libv2ray.V2RayVPNServiceSupportsSet;


public class V2RayDaemon extends Service {

    Messenger mService = null;

    /** Flag indicating whether we have called bind on the service. */
    boolean mBound;
    final V2RayDaemon me = this;

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

            Message msg = Message.obtain(null,0,0,0);
            msg.replyTo=new Messenger(new Handler(new Handler.Callback() {
                @Override
                public boolean handleMessage(Message message) {
                    switch (message.what){
                        case MSG_return_self:
                            me.vpns=(V2RVPNService) message.obj;
                            VPNCheckifReady();
                            break;
                        case MSG_Stop_V2Ray:
                            stopV2Ray();
                            break;

                    }

                    return true;
                }
            }));
            try {
                mService.send(msg);
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        }

        public void onServiceDisconnected(ComponentName className) {
            // This is called when the connection with the service has been
            // unexpectedly disconnected -- that is, its process crashed.
            mService = null;
            mBound = false;
        }
    };




    private static final int ONGOING_NOTIFICATION_ID = 1 ;

    Messenger Rmessager;
    V2RayPoint vp = Libv2ray.newV2RayPoint();
    V2RayCallback vp_callback= new V2RayCallback();
    V2RVPNService vpns;

    public V2RayDaemon() {
    }


    static final int MSG_Nil = 1;
    static final int MSG_Start_V2Ray = 2;
    static final int MSG_Stop_V2Ray = 3;
    static final int MSG_CheckLibVer = 4; //Actually status of V2Ray
    static final int MSG_CheckLibVerR = 5;
    static final int MSG_CheckLibVerP = 6;
    static final int MSG_VPN_USER_CONSENT = 7;
    static final int MSG_VPN_USER_DROP = 8;
    static final int MSG_return_self = 9;
    static final int MSG_NetworkStatusAlter = 10;
    class IncomingHandler extends Handler {
        @Override
        public void handleMessage(Message msg)  {
            switch (msg.what) {
                case MSG_Nil:
                break;
                case MSG_Start_V2Ray:
                    if(!vp.getIsRunning()){
                        show_noti("Freedom shall be portable.");
                        vp.setCallbacks(vp_callback);
                        vp.setVpnSupportSet(vp_callback);


                        /*Use Next Generation Interface's Behavior: V2RayContext
                        ConfigureFile will be readed by libV2Ray without our knowledge
                        */

                        vp.upgradeToContext();

                        //SharedPreferences settings = getSharedPreferences("org.kkdev.v2raygo_main",MODE_MULTI_PROCESS);
                        //String configureFile = settings.getString("configureFile","");
                        //vp.setConfigureFile(configureFile);
                        vp.runLoop();
                    }
                    break;
                case MSG_CheckLibVer:
                    //Long libver=(long)Libv2ray.CheckVersion();
                    Message resp = Message.obtain(null, MSG_CheckLibVerR);
                    Bundle bResp = new Bundle();
                    bResp.putString("LibVerS",Libv2ray.checkVersionX());
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
                    DeveloperOptionClose();
                    stopV2Ray();
                    break;

                case MSG_VPN_USER_CONSENT:
                    VPNCheckifReady();
                    break;
                case MSG_VPN_USER_DROP:
                    vp.stopLoop();
                    break;
                //case MSG_return_self:
                //    vpns=(V2RVPNService)msg.obj;
                //WARNING: ACTUAL LOCATION: ServiceConnection/~/handleMessage
                case MSG_NetworkStatusAlter:
                    vp.networkInterrupted();
                default:
                    super.handleMessage(msg);
            }
        }
    }

    private void stopV2Ray() {
        resign_noti();
        if(vp.getIsRunning()){
            vp.stopLoop();
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
        Intent resultIntent = new Intent(this, MainScreen.class);
        PendingIntent resultPendingIntent =
                PendingIntent.getActivity(
                        this,
                        0,
                        resultIntent,
                        PendingIntent.FLAG_UPDATE_CURRENT
                );



        Notification status_noti = new Notification.Builder(this)
                .setContentTitle(getText(R.string.app_noti_title))
                .setContentIntent(resultPendingIntent)
                .setContentText(ctxtxt)
                .setSmallIcon(R.drawable.ic_rised_fist)
                .setPriority(Notification.PRIORITY_MIN)
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

    private int VPNPrepare(){

        Intent itx=new Intent(this,V2RVPNService.class);
        itx.setAction(V2RVPNService.intent_communiacate);
        startService(itx);
        bindService(itx, mConnection,
                Context.BIND_AUTO_CREATE);
        Intent prepare = vpns.prepare(this);
        if (prepare == null){
            VPNCheckifReady();
            return 0;
        }
        Intent consent = new Intent(this,VPNServiceUserConsentActivity.class);
        consent.putExtra("Object",prepare);
        consent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(consent);
        return 1;
    }
    private int VPNCheckifReady(){
        Intent prepare = vpns.prepare(this);
        if(prepare==null&&this.vpns!=null){
            vp.vpnSupportReady();
        }
        return 0;
    }

    private void DeveloperOptionClose(){
        if(vp.isDebugTriggered()){
            try {
                showStat();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private void showStat() throws Exception {
        StatControler sc = vp.getStatControler();
        sc.collectInterfaceInfo();
        InterfaceInfo intf= sc.getCollectedInterfaceInfo();
        String datas = String.format("RxMByte: %d, RxPacket: %d, TxMByte: %d, TxPacket: %d", intf.getRxByte()/1024/1024,intf.getRxPacket(),intf.getTxByte()/1024/1024,intf.getTxPacket());
        Log.e("RunSoLib", datas);
    }


    class V2RayCallback implements V2RayCallbacks, V2RayVPNServiceSupportsSet

    {

        @Override
        public long getVPNFd() {
            long fd = vpns.getfd();
            return vpns.getfd();
        }

        @Override
        public long onEmitStatus(long l, String s) {
            remoteWrite(s);
            return 0;
        }

        @Override
        public long prepare() {

            return VPNPrepare();
        }

        @Override
        public long protect(long l) {
            return vpns.protect((int)l)?0:1;
        }

        @Override
        public long setup(String s) {
            try {
                vpns.setup(s);
                return 0;
            } catch (Exception e) {
                e.printStackTrace();
                return -1;
            }
        }

        @Override
        public long shutdown() {
            //vpns.onRevoke();
            return 0;
        }
    }



}
