package com.example.motta.recorderspeakerspeech;

import android.content.Context;
import android.os.AsyncTask;
import android.os.Environment;
import android.util.Log;
import android.widget.CheckBox;
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
    private boolean result = false;//dice se la frase è stata riconosciuta o meno
    private String recognizedPhrase = null;//frase riconosciuta
    private TextView textView = null;
    private CheckBox checkSpeech = null;

    public STT(Context context, TextView _textView, CheckBox _checkSpeech)
    {
        mContext = context;
        textView = _textView;
        checkSpeech = _checkSpeech;
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

                    alternatives = transcript.getResults().get(0).getAlternatives();

                }
            });

            Thread.currentThread().sleep(10000);//10 secondi per attendere il risultato del servizio

            String expectedPhrase = mContext.getString(R.string.correctPhrase);//recupero la frase di accesso prestabilita

            result = SupportFunctions.verifyPhrase(alternatives,expectedPhrase);//verifico se la frase è corretta
            recognizedPhrase = SupportFunctions.recognizedPhrase(alternatives,expectedPhrase,result);//ottengo la frase riconosciuta

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

        if(result) {//se la frase è stata riconosciuta
            textView.setText("Succeeded, recognized phrase: " + recognizedPhrase);//stampo testo che indica il successo del riconoscimento e la frase
                                                               //che è stata riconosciuta come corretta
            checkSpeech.setChecked(true);
        }
        else{//se la frase non è stata riconosciuta
            textView.setText("Failed, recognized phrase: " + recognizedPhrase);//stampo testo che indica il fallimento del riconoscimento e la frase
                                                            //che è non è stata riconosciuta come corretta
        }
    }
}

