package com.mafi.andrea.audiorecorder;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

public class MainActivity extends AppCompatActivity {

    Button btnRec = null;

    private final String PATH = "Speaker recognition files";
    private final String FILENAME_TRAINING = "trainingFile";
    private final String FILENAME_SCALED_TRAINING = "scaledTrainingFile";
    private final String FILENAME_SVM_MODEL = "svmModel";
    private final String FILENAME_TESTING = "testingFile";
    private final String FILENAME_SCALED_TESTING = "scaledTestingFile";
    private final String FILENAME_OUTPUT = "outputFile";

    private final int Fs = 8000;
    private final int recordingLength = 5;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        btnRec = findViewById(R.id.btt);
        btnRec.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                Rec rec = new Rec(getApplicationContext(), recordingLength, Fs);
                rec.execute(PATH, FILENAME_TRAINING,FILENAME_SCALED_TRAINING,FILENAME_SVM_MODEL,FILENAME_TESTING,FILENAME_SCALED_TESTING,FILENAME_OUTPUT);
            }
        });
    }
}
