package com.example.motta.recorderspeakerspeech;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

public class MainActivity extends AppCompatActivity {

    Button btnRec = null;
    Button bttStt = null;
    private final String PATH = "Audio recognition files";
    private final String FILENAME = "trainingData";
    private final String FILENAME2 = "rec.wav";

    private final int Fs = 44200;
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
                rec.execute(PATH, FILENAME , FILENAME2);
            }
        });

        bttStt = findViewById(R.id.bttStt);
        bttStt.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {

                STT stt = new STT(getApplicationContext());
                stt.execute(PATH, FILENAME2);
            }
        });

    }
}
