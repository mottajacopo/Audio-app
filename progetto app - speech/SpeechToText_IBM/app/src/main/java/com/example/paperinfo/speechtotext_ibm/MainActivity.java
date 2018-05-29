package com.example.paperinfo.speechtotext_ibm;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.Button;
import android.view.View;

public class MainActivity extends AppCompatActivity {

    Button bttStt = null;
    private final String PATH = "app_records";
    private final String FILENAME = "rec.wav";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        bttStt = findViewById(R.id.bttStt);
        bttStt.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {

                STT stt = new STT(getApplicationContext());
                stt.execute(PATH, FILENAME);
            }
        });

    }
}
