package com.example.paperinfo.recorder;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

public class MainActivity extends AppCompatActivity {

    Button bttRec = null;
    private final String PATH = "app_records";
    private final String FILENAME = "rec.wav";
    private final int Fs = 44200;
    private final int recordingLength = 3;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        bttRec = findViewById(R.id.bttRec);
        bttRec.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                Rec rec = new Rec(getApplicationContext(), recordingLength, Fs);
                rec.execute(PATH, FILENAME);
            }
        });

    }
}
