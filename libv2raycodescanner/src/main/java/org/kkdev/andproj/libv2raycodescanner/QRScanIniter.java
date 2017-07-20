package org.kkdev.andproj.libv2raycodescanner;

/**
 * Created by shelikhoo on 7/20/17.
 */
import android.app.Activity;
import android.content.Intent;

public class QRScanIniter {
    public QRScanIniter(){

    }
    public void Open(Activity a){
        Intent intent = new Intent(a, MainActivity.class);
        a.startActivity(intent);
    }
}
