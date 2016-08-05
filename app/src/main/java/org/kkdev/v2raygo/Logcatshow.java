package org.kkdev.v2raygo;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.method.ScrollingMovementMethod;
import android.widget.TextView;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

public class Logcatshow extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_logcatshow);
        setTitle("V2RayGO logcat");
    }

    @Override
    protected void onPostResume() {
        super.onPostResume();
        try {
            Process process = Runtime.getRuntime().exec("logcat -d");
            BufferedReader bufferedReader = new BufferedReader(
                    new InputStreamReader(process.getInputStream()));

            StringBuilder log=new StringBuilder();
            String line = "";
            while ((line = bufferedReader.readLine()) != null) {
                log.append(line);
                log.append("\n");
            }
            TextView tv = (TextView)findViewById(R.id.textView_logcat);
            tv.setMovementMethod(new ScrollingMovementMethod());
            tv.setText(log.toString());
            //final int scrollAmount = tv.getLayout().getLineTop(tv.getLineCount()) - tv.getHeight();
            //tv.scrollTo(0, scrollAmount);
        } catch (IOException e) {
        }
    }
}
