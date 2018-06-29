package com.example.motta.speechtotextv3;

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

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

public class MainActivity extends AppCompatActivity {

    private TextView tv;
    private Button btt;
    private static CountDownLatch lock = new CountDownLatch(1);
    private final static String TAG = "Rec";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        tv = (TextView) findViewById(R.id.tv);
        btt = (Button) findViewById(R.id.btt);

        btt.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                STT();
            }
        });
    }

    private void STT() {
        SpeechToText service = new SpeechToText();
        service.setUsernameAndPassword("<username>", "<password>");

        try{
            FileInputStream audio = new FileInputStream("src/test/resources/speech_to_text/sample1.wav");

            RecognizeOptions options = new RecognizeOptions.Builder()
                    .audio(audio)
                    .interimResults(true)
                    .contentType(HttpMediaType.AUDIO_WAV)
                    .build();

            service.recognizeUsingWebSocket(options, new BaseRecognizeCallback() {
                @Override
                public void onTranscription(SpeechRecognitionResults speechResults) {
                    System.out.println(speechResults);
                    tv.setText((CharSequence) speechResults);
                }
                @Override
                public void onDisconnected() {
                    lock.countDown();
                }
            });
            lock.await(1, TimeUnit.MINUTES);
        }
        catch (FileNotFoundException e){
            Log.e(TAG,"File not found");
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}

