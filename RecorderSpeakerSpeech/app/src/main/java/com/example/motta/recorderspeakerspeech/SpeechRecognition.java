package com.example.motta.recorderspeakerspeech;

import android.content.Context;
import android.os.AsyncTask;
import android.os.Environment;
import android.util.Log;
import android.widget.Toast;

import com.ibm.watson.developer_cloud.http.HttpMediaType;
import com.ibm.watson.developer_cloud.speech_to_text.v1.SpeechToText;
import com.ibm.watson.developer_cloud.speech_to_text.v1.model.RecognizeOptions;
import com.ibm.watson.developer_cloud.speech_to_text.v1.model.SpeechRecognitionResults;
import com.ibm.watson.developer_cloud.speech_to_text.v1.websocket.BaseRecognizeCallback;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;

public class SpeechRecognition extends AsyncTask<String, String, Void>  {
    private static final String TAG = "STT";
    private Context mContext;
    private String temp;
    private boolean result;

    public SpeechRecognition(Context context){
        mContext = context;
    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();

        Toast.makeText(mContext,"Start verification", Toast.LENGTH_SHORT).show();
    }

    @Override
    protected Void doInBackground(String... strings) {

/////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
//collegamento al servizio IBM per lo speech to text tramite username e password
/////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////


        SpeechToText service = new SpeechToText();
        service.setUsernameAndPassword(mContext.getString(R.string.username), mContext.getString(R.string.password));

        String _path = strings[0];
        String _filename = strings[1];
        File file = new File(Environment.getExternalStorageDirectory() + "/" + _path + "/" + _filename);


/////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
//lettura e invo del file .wav come InputStream e attesa della risposta dal servizio
/////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

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
///////////////////////////////////////////////////////////////////////////////////////////////////
//controllo che il risultato ricevuto contenga la frase corretta

                    temp = (transcript.getResults().get(0).toString());
                    result = temp.contains(mContext.getString(R.string.check_phrase));
                }
            });

///////////////////////////////////////////////////////////////////////////////////////////////////
//delay 10 sec  (ritardo necessario al comletamento della verifica)
            Thread.currentThread().sleep(10000);
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
            Toast.makeText(mContext, "Verification succeeded", Toast.LENGTH_SHORT).show();
        }
        else{
            Toast.makeText(mContext, "Verification failed", Toast.LENGTH_SHORT).show();
        }
    }
}
