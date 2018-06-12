package com.example.motta.recorderspeakerspeech;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

public class MainActivity extends AppCompatActivity {

    Button btnRec = null;
    Button bttStt = null;
    private final String PATH = "Audio recognition files";
    private final String FILENAME = "trainingData";//file utilizzato per il training della SVM (contiene mfcc + deltadelta dei parlatori)
    private final String FILENAME2 = "rec.wav";// file .wav che serve allo speech to text

    private final int Fs = 44200;
    private final int recordingLength = 3;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

/////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
//SAVE FILE .WAV + SPEAKER RECOGNITION (LIBSVM)
/////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

        btnRec = findViewById(R.id.btt);
        btnRec.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                SpeakerRecognition speaker = new SpeakerRecognition(getApplicationContext(), recordingLength, Fs);
                speaker.execute(PATH, FILENAME , FILENAME2);
            }
        });

/////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
//SPEECH RECOGNITION (API IBM (watson))
/////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

        bttStt = findViewById(R.id.bttStt);
        bttStt.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {

                SpeechRecognition speech = new SpeechRecognition(getApplicationContext());
                speech.execute(PATH, FILENAME2);
            }
        });

    }
}
