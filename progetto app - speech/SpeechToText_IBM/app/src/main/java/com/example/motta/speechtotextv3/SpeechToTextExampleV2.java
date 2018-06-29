package com.example.motta.speechtotextv3;

import android.util.Log;

import com.ibm.watson.developer_cloud.http.HttpMediaType;
import com.ibm.watson.developer_cloud.speech_to_text.v1.SpeechToText;
import com.ibm.watson.developer_cloud.speech_to_text.v1.model.RecognizeOptions;
import com.ibm.watson.developer_cloud.speech_to_text.v1.model.SpeechRecognitionResults;
import com.ibm.watson.developer_cloud.speech_to_text.v1.websocket.BaseRecognizeCallback;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;

public class SpeechToTextExampleV2 {

    private final static String TAG = "Rec";

    public static void main(String[] args) {

        SpeechToText service = new SpeechToText();
        service.setUsernameAndPassword("<username>", "<password>");

        try{
            InputStream audio = new FileInputStream("src/test/resources/sample1.wav");

            RecognizeOptions options = new RecognizeOptions.Builder()
                    .audio(audio)
                    .contentType(HttpMediaType.AUDIO_WAV)
                    .interimResults(true)
                    .build();

            service.recognizeUsingWebSocket(options, new BaseRecognizeCallback() {
                @Override
                public void onTranscription(SpeechRecognitionResults speechResults) {
                    System.out.println(speechResults);
                }
            });
        }
        catch (FileNotFoundException e){
            Log.e(TAG,"File not found");
        }
    }
}