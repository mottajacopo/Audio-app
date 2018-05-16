package com.mafi.andrea.audiorecorder;

import android.os.Environment;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.ibm.watson.developer_cloud.http.HttpMediaType;
import com.ibm.watson.developer_cloud.speech_to_text.v1.SpeechToText;
import com.ibm.watson.developer_cloud.speech_to_text.v1.model.RecognizeOptions;
import com.ibm.watson.developer_cloud.speech_to_text.v1.model.SpeechRecognitionResults;
import com.ibm.watson.developer_cloud.speech_to_text.v1.websocket.BaseRecognizeCallback;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;


import java.util.concurrent.CountDownLatch;

public class MainActivity extends AppCompatActivity {

    Button bttRec = null;
    Button bttStt = null;
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

        bttStt = (Button)findViewById(R.id.bttStt);
        bttStt.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {

                STT stt = new STT(getApplicationContext());
                stt.execute(PATH, FILENAME);
            }
        });
    }
}
