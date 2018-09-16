package com.example.motta.recorderspeakerspeech;

import android.content.Context;
import android.os.AsyncTask;
import android.os.Environment;
import android.util.Log;
import android.widget.TextView;
import android.widget.Toast;

import com.ibm.watson.developer_cloud.http.HttpMediaType;
import com.ibm.watson.developer_cloud.speech_to_text.v1.SpeechToText;
import com.ibm.watson.developer_cloud.speech_to_text.v1.model.RecognizeOptions;
import com.ibm.watson.developer_cloud.speech_to_text.v1.model.SpeechRecognitionAlternative;
import com.ibm.watson.developer_cloud.speech_to_text.v1.model.SpeechRecognitionResults;
import com.ibm.watson.developer_cloud.speech_to_text.v1.websocket.BaseRecognizeCallback;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.List;


public class STT extends AsyncTask<String, String, Void> {

    private List<SpeechRecognitionAlternative> alternatives = null;
    private static final String TAG = "STT";
    private Context mContext = null;
    private boolean result = false;
    private String recognizedPhrase = null;
    private TextView textView = null;

    public STT(Context context, TextView _textView)
    {
        mContext = context;
        textView = _textView;
    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();

        Toast.makeText(mContext,"Starting verification", Toast.LENGTH_SHORT).show();
    }

    @Override
    protected Void doInBackground(String... strings) {

        SpeechToText service = new SpeechToText();
        service.setUsernameAndPassword(mContext.getString(R.string.username), mContext.getString(R.string.password));

        String _path = strings[0];
        String _filename = strings[1];
        File file = new File(Environment.getExternalStorageDirectory() + "/" + _path + "/" + _filename);

        try{
            InputStream audio = new FileInputStream(file);

            RecognizeOptions options = new RecognizeOptions.Builder()
                    .audio(audio)
                    .contentType(HttpMediaType.AUDIO_WAV)
                    .interimResults(true)
                    .build();

            service.recognizeUsingWebSocket(options, new BaseRecognizeCallback() {
                @Override
                public void onTranscription(SpeechRecognitionResults transcript) {
                    System.out.println(transcript);

                    /*
                    boolean result = false;

                    for(int i =0; i< transcript.getResults().size(); i++)
                    {
                        temp = transcript.getResults().get(i).toString();
                        if(temp.contains("open the door please")){
                            result = true;
                        }
                    }
                    */

                    alternatives = transcript.getResults().get(0).getAlternatives();

                }
            });
            //delay 10 sec
            Thread.currentThread().sleep(10000);

            String expectedPhrase = "my voice is my password open the door";


            result = SupportFunctions.verifyPhrase(alternatives,expectedPhrase);
            recognizedPhrase = SupportFunctions.recognizedPhrase(alternatives,expectedPhrase,result);
        }
        catch (FileNotFoundException e){
            Log.e(TAG,"File not found");
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    protected void onPostExecute(Void aVoid) {
        super.onPostExecute(aVoid);

        if(result) {
            //Toast.makeText(mContext, "Succeeded: " + recognizedPhrase, Toast.LENGTH_LONG).show();
            textView.setText("Succeeded: " + recognizedPhrase);
        }
        else{
            //Toast.makeText(mContext, "Failed: " + recognizedPhrase, Toast.LENGTH_LONG).show();
            textView.setText("Failed: " + recognizedPhrase);
        }
    }
}

