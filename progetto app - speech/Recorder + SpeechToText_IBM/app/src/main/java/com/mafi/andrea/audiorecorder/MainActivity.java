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
    private final String PATH = "app_records";
    private final String FILENAME = "rec.wav";
    private final int Fs = 44200;
    private final int recordingLength = 3;

    private TextView tv;
    private Button btt;
    private static CountDownLatch lock = new CountDownLatch(1);
    private final static String TAG = "Rec";
    private String args[];
    private String temp;

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

        tv = (TextView) findViewById(R.id.tv);
        btt = (Button) findViewById(R.id.btt);
        btt.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                STT(args);
            }
        });
    }

    private void STT(String[] args) {

        SpeechToText service = new SpeechToText();
        service.setUsernameAndPassword("c492c54d-42a7-484f-97a6-0c9d163d6345", "1lvt77qALCoa");

        File file = new File(Environment.getExternalStorageDirectory() + "/" + PATH + "/" + FILENAME);

        try {
            InputStream audio = new FileInputStream(file);

            RecognizeOptions options = new RecognizeOptions.Builder()
                    .audio(audio)
                    .contentType(HttpMediaType.AUDIO_WAV)
                    .interimResults(true)
                    .build();

            service.recognizeUsingWebSocket(options, new BaseRecognizeCallback() {
                @Override
                public void onTranscription(SpeechRecognitionResults transcript) {
                    System.out.println(transcript.getResults().get(0).toString());
                    temp = (transcript.getResults().get(0).toString());
                }
            });
        } catch (FileNotFoundException e) {
            Log.e(TAG, "File not found");
        }
    }
}