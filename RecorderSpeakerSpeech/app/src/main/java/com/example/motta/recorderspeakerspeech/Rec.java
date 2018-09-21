package com.example.motta.recorderspeakerspeech;

import android.content.Context;
import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.MediaRecorder;
import android.os.AsyncTask;
import android.os.Environment;
import android.util.Log;
import android.widget.Toast;

import java.io.File;


public class Rec extends AsyncTask<String,Void,Void> {

    private final String TAG = "Rec";
    private Context context = null;

    private int recordingLenghtInSec = 0;
    private int Fs = 0; //freq di campionamento
    private int nSamples = 0;//numero totale di campioni
    private short[] audioData = null; //java codifica i campioni audio in degli short 16 bit
    private AudioRecord record = null;

    public Rec(Context _context, int _recordingLenghtInSec, int _Fs)
    {

        context = _context;
        recordingLenghtInSec = _recordingLenghtInSec;
        Fs = _Fs;
        nSamples = recordingLenghtInSec * Fs;

        audioData = new short[nSamples]; //oppure passo direttamente l'array alla main activity per poi gestirlo li

        record = new AudioRecord(MediaRecorder.AudioSource.MIC, Fs, AudioFormat.CHANNEL_IN_MONO, AudioFormat.ENCODING_PCM_16BIT,2*nSamples);//il buffer in byte dovrà essere il doppio della dimensione dell array

    }


    @Override
    protected void onPreExecute() {
        super.onPreExecute();

        Toast.makeText(context,"Start Recording", Toast.LENGTH_LONG).show();
    }

    @Override
    protected Void doInBackground(String... strings) {

        String _path = strings[0];
        String _fileName = strings[1];


        String storeDir = Environment.getExternalStorageDirectory() + "/" + _path;//genero il percorso dove verrà salvato il file

        File f = new File(storeDir);//prova a creare la cartella in cui salvare il file dato il percorso

        if (!f.exists()) {//se la cartella non esiste già
            if (!f.mkdir()) {//provo a crearne una nuova altrimenti genero messaggio di errore
                Log.e(TAG, "Cannot create directory");
            }
        }


        record.startRecording(); //apre il record dalla sorgente indicata con MIC
        record.read(audioData, 0, nSamples);//inizia la lettura e la finisce
        record.stop();
        record.release();

/////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
//save .wav file
/////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
        byte dataByte[] = new byte[2 * nSamples];//WavIO.java genera il file wav data una sequenza di byte e non di short

        for (int i = 0; i < nSamples; i++) {//ogni campione a 16 bit viene diviso in due parti da 8 bit
            dataByte[2 * i] = (byte) (audioData[i] & 0x00ff);//inserisco i primi 8 bit nel primo elemento di dataByte
            dataByte[2 * i + 1] = (byte) ((audioData[i] >> 8) & 0x00ff);//inserisco i secondi 8 bit nel secondo elemento dataByte
        }

        WavIO writeWav = new WavIO(storeDir + "/" + _fileName, 16, 1, 1, Fs, 2, 16, dataByte);
        writeWav.save();//salva il file .wav


        return null;
    }

    @Override
    protected void onPostExecute(Void aVoid) {
        super.onPostExecute(aVoid);

        Toast.makeText(context,"Ended recording",Toast.LENGTH_LONG).show();

    }
}


