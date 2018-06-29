package com.example.motta.speechtotextv3;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import com.ibm.watson.developer_cloud.http.HttpMediaType;
import com.ibm.watson.developer_cloud.speech_to_text.v1.SpeechToText;
import com.ibm.watson.developer_cloud.speech_to_text.v1.model.RecognizeOptions;
import com.ibm.watson.developer_cloud.speech_to_text.v1.model.SpeechRecognitionResults;
import com.ibm.watson.developer_cloud.speech_to_text.v1.websocket.BaseRecognizeCallback;

public class SpeechToTextExample {
    private static CountDownLatch lock = new CountDownLatch(1);

    public static void main(String[] args) throws FileNotFoundException, InterruptedException {
        SpeechToText service = new SpeechToText();
        service.setUsernameAndPassword("<username>", "<password>");

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

            }
            @Override
            public void onDisconnected() {
                lock.countDown();
            }
        });
        lock.await(1, TimeUnit.MINUTES);
    }
}
