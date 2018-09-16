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


/**
 * Created by Giulia on 11/04/2018.
 */

public class Rec extends AsyncTask<String,Void,Void> {

    private final String TAG = "Rec";
    private final double frameLenght = 0.02;
    private Context context = null;

    private int recordingLenghtInSec = 0;
    private int Fs = 0; //freq di campionamento
    private int nSamples = 0;
    //private  int nSamplesPerFrame = 0;
    private short[] audioData = null; //java codifica i campioni audio in degli short 16 bit
    private AudioRecord record = null;

    public Rec(Context _context, int _recordingLenghtInSec, int _Fs)
    {

        context = _context;
        recordingLenghtInSec = _recordingLenghtInSec;
        Fs = _Fs;
        nSamples = recordingLenghtInSec * Fs;
        //nSamplesPerFrame = (int) (frameLenght * _Fs);

        audioData = new short[nSamples]; //oppure passo direttamente l'array alla main activity per poi gestirlo li

        record = new AudioRecord(MediaRecorder.AudioSource.MIC, Fs, AudioFormat.CHANNEL_IN_MONO, AudioFormat.ENCODING_PCM_16BIT,2*nSamples);//il buffer in byte dovrà essere il doppio della dimensione dell array

    }


    @Override
    protected void onPreExecute() {
        super.onPreExecute();

        Toast.makeText(context,"Start Recording", Toast.LENGTH_LONG).show();
    }

    @Override
    protected Void doInBackground(String... strings) { //gli ingressi sono quelli passati quando faccio async.Execute//le passo sia il nome della cartella in cui salvare il file e il nome del file --> possono anche essere passati direttamente al costruttore

        String _path = strings[0];
        String _fileName = strings[1]; //usato per il file .wav e STT


/*
        svm_node[][] data =  readTestDataFromFormatFile(Environment.getExternalStorageDirectory() + "/" + _path + "/1.txt",299*2,26);
        svm_node[][] scaledData = scaleTrainingData(data,Environment.getExternalStorageDirectory() + "/" + _path,1,0);

        svm_problem p = new svm_problem();
        double[] lab = new double[299*2];
        for(int i = 0; i< 299*2;i++)
        {
            lab[i] = 1;
        }
        p.x = scaledData;
        p.l = 1;
        p.y = lab;

        svm_parameter par = new svm_parameter();
        par.svm_type = 2;
        par.kernel_type = 2;
        par.gamma = 0.005;
        par.nu = 0.8;

        svm_model mod = new svm_model();
        mod = svm.svm_train(p,par);

        try {
            svm.svm_save_model(Environment.getExternalStorageDirectory() + "/" + _path + "/ex1.txt", mod);
        }
        catch (IOException e)
        {
          int a = 0;
        }

        data = readTestDataFromFormatFile(Environment.getExternalStorageDirectory() + "/" + _path + "/2.txt",299*3,26);
        scaledData = scaleTrainingData(data,Environment.getExternalStorageDirectory() + "/" + _path,2,0);
*/

/*
        for(int i = 0; i< 2;i++)
        {
            int speaker = i + 1;
            svm_node[][] data = readTestDataFromFormatFile(Environment.getExternalStorageDirectory() + "/" + _path + "/" + String.valueOf(speaker) + ".txt",1196,26);
            svm_node[][] scaledData = scaleTrainingData(data,Environment.getExternalStorageDirectory() + "/" + _path,speaker);

        }
*/

        String storeDir = Environment.getExternalStorageDirectory() + "/" + _path;

        File f = new File(storeDir);

        if (!f.exists()) {
            if (!f.mkdir()) {
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
        byte dataByte[] = new byte[2 * nSamples];

        for (int i = 0; i < nSamples; i++) {
            dataByte[2 * i] = (byte) (audioData[i] & 0x00ff);
            dataByte[2 * i + 1] = (byte) ((audioData[i] >> 8) & 0x00ff);
        }

        WavIO writeWav = new WavIO(storeDir + "/" + _fileName, 16, 1, 1, Fs, 2, 16, dataByte);
        writeWav.save();


        return null;
    }

    @Override
    protected void onPostExecute(Void aVoid) {
        super.onPostExecute(aVoid);

        Toast.makeText(context,"Ended recording",Toast.LENGTH_LONG).show();

    }
}


