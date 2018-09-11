package com.example.motta.recorderspeakerspeech;

import android.media.AudioRecord;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;

import be.tarsos.dsp.AudioEvent;

public class MainActivity extends AppCompatActivity {

    Button btnRec = null;
    Button bttStt = null;
    Button bttSpk = null;
    private final String PATH = "Audio recognition files multi";
    private final String FILENAME = "rec.wav";

    private final int Fs = 8000;
    private final int recordingLength = 3;

    private short[] samples = new short[recordingLength*Fs];

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        btnRec = findViewById(R.id.bttRec);
        bttSpk = findViewById(R.id.bttSpk);
        bttStt = findViewById(R.id.bttStt);


        btnRec.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {


                Rec rec = new Rec(getApplicationContext(), recordingLength, Fs,samples);
                rec.execute(PATH, FILENAME);

            }
        });


        bttSpk.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                SpeakerRecog speakerRecog = new SpeakerRecog(getApplicationContext(),Fs,recordingLength);
                speakerRecog.execute(PATH,FILENAME);

            }
        });


        bttStt.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {

                STT stt = new STT(getApplicationContext());
                stt.execute(PATH, FILENAME);
            }
        });


    }
}
