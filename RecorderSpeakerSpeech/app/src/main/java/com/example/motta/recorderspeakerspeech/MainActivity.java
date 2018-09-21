package com.example.motta.recorderspeakerspeech;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;


public class MainActivity extends AppCompatActivity {

    EditText editText = null;
    Button bttSpk = null;
    TextView textView = null;//textView per stampare i messaggi di risultato

    private final String PATH = "Audio recognition files multi";//nome cartella contenente tutti i file per speaker e speech recog.
    private final String FILENAME = "rec.wav";//nome file .wav

    private final int Fs = 44000;//frequenza di campionamento
    private final int recordingLength = 3;//lunghezza registraz. in secondi


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        bttSpk = findViewById(R.id.bttSpk);
        textView = findViewById(R.id.textView);
        editText = findViewById(R.id.editText);


        bttSpk.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                SpeakerRecog speakerRecog = null;

                String numberOfWavs = editText.getText().toString();

                for(int i = 1; i< Integer.valueOf(numberOfWavs) + 1; i++) {

                    speakerRecog = new SpeakerRecog(getApplicationContext(),Fs,recordingLength,textView);
                    speakerRecog.execute(PATH, String.valueOf(i),numberOfWavs);
                }

            }
        });



    }
}
