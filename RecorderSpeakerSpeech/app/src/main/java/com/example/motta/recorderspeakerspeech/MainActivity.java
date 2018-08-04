package com.example.motta.recorderspeakerspeech;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;

public class MainActivity extends AppCompatActivity {

    private String numberOfTest = "11";
    Button btnRec = null;
    Button bttStt = null;
    CheckBox boxOne = null;
    CheckBox boxTwo = null;
    private int speaker = 0;
    private final String PATH = "Audio recognition files multi";
    private final String FILENAME = "trainingData";
    private final String FILENAME2 = "rec.wav";

    private final int Fs = 8000;
    private final int recordingLength = 3;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        boxOne = findViewById(R.id.CheckOne);
        boxTwo = findViewById(R.id.CheckTwo);
        btnRec = findViewById(R.id.btt);
        btnRec.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {



                if(boxOne.isChecked())
                    speaker = 1;
                else
                    speaker = 2;

                Rec rec = new Rec(getApplicationContext(), recordingLength, Fs,speaker);
                rec.execute(PATH, FILENAME , FILENAME2,numberOfTest);

                numberOfTest = String.valueOf(Integer.parseInt(numberOfTest) + 1);
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
