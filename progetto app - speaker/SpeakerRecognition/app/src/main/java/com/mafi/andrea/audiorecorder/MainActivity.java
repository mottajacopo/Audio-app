package com.mafi.andrea.audiorecorder;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import libsvm.*;

public class MainActivity extends AppCompatActivity {


    Button btnRec = null;

    private final String PATH = "Speaker recognition files";
    private final String FILENAME = "trainingData";

    private final int Fs = 8000;
    private final int recordingLength = 3;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        btnRec = findViewById(R.id.btt);
        btnRec.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                Rec rec = new Rec(getApplicationContext(), recordingLength, Fs);
                rec.execute(PATH, FILENAME);
            }
        });
    }
}
