package org.kkdev.v2raygo;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;

/**
 * Created by shelikhoo on 8/19/16.
 */

public class ConnectionChangeReceiver extends BroadcastReceiver {
    Messenger mService = null;
    @Override
    public void onReceive(Context context, Intent intent) {
        Intent intentx = new Intent(context,V2RayDaemon.class);
        intentx.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        context.startService(intentx);
        IBinder bd= peekService(context,intentx);
        if(bd==null){
            return;
        }
        mService = new Messenger(bd);
        Message msg = Message.obtain(null, V2RayDaemon.MSG_NetworkStatusAlter);
        try {
            mService.send(msg);
        } catch (RemoteException e) {
            e.printStackTrace();
        }

    }
}
