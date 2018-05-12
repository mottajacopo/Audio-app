package com.example.motta.speechtotextv3;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

public class MainActivity extends AppCompatActivity {

    private TextView tv;
    private Button btt;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        tv = (TextView) findViewById(R.id.tv);
        btt = (Button) findViewById(R.id.btt);

        /*
        btt.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                new SpeechToTextExample();
            }
        });
        */

    }
}
