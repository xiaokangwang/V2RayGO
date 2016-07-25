package org.kkdev.v2raygo;

import android.app.Service;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.VpnService;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.os.ParcelFileDescriptor;
import android.os.RemoteException;
import android.util.Log;

import go.libv2ray.Libv2ray;

public class V2RVPNService extends VpnService {
    final static String intent_communiacate = "org.kkdev.v2raygo.V2RVPNService_af0dc150-a47d-4fb1-971b-3a5ece9080fa";
    final V2RVPNService me = this;
    public V2RVPNService() {
    }

        private ParcelFileDescriptor mInterface;
        private String mParameters;
        @Override
        public IBinder onBind(Intent intent) {
            if(intent.getAction().equals(intent_communiacate)){
                final Messenger mMessenger = new Messenger(new IncomingHandler());
                return mMessenger.getBinder();
            }
            return super.onBind(intent);
        }

    class IncomingHandler extends Handler {
        @Override
        public void handleMessage(Message msg)  {
            switch (msg.what) {
                case 0:
                    Message resp = Message.obtain(null, V2RayDaemon.MSG_return_self);
                    resp.obj=me;
                    try {
                        msg.replyTo.send(resp);
                    } catch (RemoteException e) {
                        e.printStackTrace();
                    }
                default:
                    super.handleMessage(msg);
            }
        }
    }

        @Override
        public void onRevoke() {
            super.onRevoke();
        }

        public int getfd(){
            return mInterface.getFd();
        }


        public void setup(String parameters) throws Exception {
            // If the old interface has exactly the same parameters, use it!
            if (mInterface != null && parameters.equals(mParameters)) {
                Log.i("VPNService", "Using the previous interface");
                return;
            }
            // Configure a builder while parsing the parameters.
            Builder builder = new Builder();
            for (String parameter : parameters.split(" ")) {
                String[] fields = parameter.split(",");
                try {
                    switch (fields[0].charAt(0)) {
                        case 'm':
                            builder.setMtu(Short.parseShort(fields[1]));
                            break;
                        case 'a':
                            builder.addAddress(fields[1], Integer.parseInt(fields[2]));
                            break;
                        case 'r':
                            builder.addRoute(fields[1], Integer.parseInt(fields[2]));
                            break;
                        case 'd':
                            builder.addDnsServer(fields[1]);
                            break;
                        case 's':
                            builder.addSearchDomain(fields[1]);
                            break;
                    }
                } catch (Exception e) {
                    throw new IllegalArgumentException("Bad parameter: " + parameter);
                }
            }
            // Close the old interface since the parameters have been changed.
            try {
                mInterface.close();
            } catch (Exception e) {
                // ignore
            }
            // Create a new interface using the builder and save the parameters.
            mInterface = builder.establish();
            mParameters = parameters;
            Log.i("VPNService", "New interface: " + parameters);
        }


}
