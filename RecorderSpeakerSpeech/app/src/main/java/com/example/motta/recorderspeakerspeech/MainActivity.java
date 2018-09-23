package com.example.motta.recorderspeakerspeech;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.TextView;


public class MainActivity extends AppCompatActivity {

    Button btnRec = null;
    Button bttStt = null;
    Button bttSpk = null;
    CheckBox checkSpeaker = null;
    CheckBox checkSpeech = null;
    CheckBox finalDecision = null;
    TextView textView = null;//textView per stampare i messaggi di risultato

    private final String PATH = "Audio recognition files multi";//nome cartella contenente tutti i file per speaker e speech recog.
    private final String FILENAME = "rec.wav";//nome file .wav

    private final int Fs = 44000;//frequenza di campionamento
    private final int recordingLength = 3;//lunghezza registraz. in secondi


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        btnRec = findViewById(R.id.bttRec);
        bttSpk = findViewById(R.id.bttSpk);
        bttStt = findViewById(R.id.bttStt);
        checkSpeaker = findViewById(R.id.checkSpeaker);
        checkSpeech = findViewById(R.id.checkSpeech);
        finalDecision = findViewById(R.id.finalDecision);
        textView = findViewById(R.id.textView);


        btnRec.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {


                checkSpeaker.setChecked(false);
                checkSpeech.setChecked(false);
                finalDecision.setChecked(false);

                Rec rec = new Rec(getApplicationContext(), recordingLength, Fs);
                rec.execute(PATH, FILENAME);

            }
        });


        bttSpk.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                SpeakerRecog speakerRecog = new SpeakerRecog(getApplicationContext(),Fs,recordingLength,textView,checkSpeaker);
                speakerRecog.execute(PATH,FILENAME);

            }
        });


        bttStt.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {

                STT stt = new STT(getApplicationContext(),textView,checkSpeech);
                stt.execute(PATH, FILENAME);
            }
        });

        checkSpeaker.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {

                if(checkSpeaker.isChecked() && checkSpeech.isChecked())
                {
                    finalDecision.setChecked(true);
                }
            }
        });

        checkSpeech.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {

                if(checkSpeech.isChecked() && checkSpeaker.isChecked())
                {
                    finalDecision.setChecked(true);
                }

            }
        });

    }
}
