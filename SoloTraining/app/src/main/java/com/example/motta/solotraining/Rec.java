package com.example.motta.solotraining;

import android.content.Context;
import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.MediaRecorder;
import android.os.AsyncTask;
import android.os.Environment;
import android.os.Handler;
import android.util.Log;
import android.widget.Toast;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

import be.tarsos.dsp.AudioEvent;
import be.tarsos.dsp.io.TarsosDSPAudioFormat;
import be.tarsos.dsp.mfcc.MFCC;
import libsvm.svm;
import libsvm.svm_model;
import libsvm.svm_node;
import libsvm.svm_parameter;
import libsvm.svm_problem;

import static com.example.motta.solotraining.SupportFunctions.computeDeltas;
import static com.example.motta.solotraining.SupportFunctions.convertFloatsToDoubles;
import static com.example.motta.solotraining.SupportFunctions.printFeaturesOnFileFormat;
import static com.example.motta.solotraining.SupportFunctions.uniteAllFeaturesInOneList;

/**
 * Created by Giulia on 11/04/2018.
 */

public class Rec extends AsyncTask<String,Void,String> {

    private final String TAG = "Rec";
    private double frameLength = 0;
    private Context context = null;

    private int speaker = 0;
    private String speakerName= null;
    private int recordingLenghtInSec = 0;
    private int Fs = 0; //freq di campionamento
    private int nSamples = 0;
    private int nSamplesPerFrame = 0;
    private int nSamplesAlreadyProcessed = 0;

    private short[] audioData = null; //java codifica i campioni audio in degli short 16 bit
    private AudioRecord record = null;

    public Rec(Context _context, int _recordingLenghtInSec, int _Fs, int _speaker , String _speakerName, Double _frameLength)
    {
        speaker = _speaker;
        speakerName = _speakerName;
        context = _context;
        frameLength = _frameLength;
        recordingLenghtInSec = _recordingLenghtInSec;
        Fs = _Fs;
        nSamples = _recordingLenghtInSec * _Fs;
        nSamplesPerFrame = (int) (frameLength * _Fs);

        audioData = new short[nSamples]; //oppure passo direttamente l'array alla main activity per poi gestirlo li

        record = new AudioRecord(MediaRecorder.AudioSource.MIC, Fs, AudioFormat.CHANNEL_IN_MONO, AudioFormat.ENCODING_PCM_16BIT,2*nSamples);//il buffer in byte dovrà essere il doppio della dimensione dell array
    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();

        showToastMessage("start", 500);
    }

    @Override
    protected String doInBackground(String... strings) { //gli ingressi sono quelli passati quando faccio async.Execute//le passo sia il nome della cartella in cui salvare il file e il nome del file --> possono anche essere passati direttamente al costruttore

        String _path = strings[0];
        String _fileName = strings[1]; // usato per il train e test svm
        String _fileName2 = strings[2]; //usato per il file .wav e STT
        int numberOfTest = (Integer.parseInt(strings[3]));
        String storeDir = Environment.getExternalStorageDirectory() + "/" + _path;
        String fileDir = storeDir + "/" + _fileName;
        File f = new File(storeDir);

        if(!f.exists())
        {
            if(!f.mkdir())
            {
                Log.e(TAG,"Cannot create directory");
            }
        }

        record.startRecording(); //apre il record dalla sorgente indicata con MIC
        record.read(audioData,0,nSamples);//inizia la lettura e la finisce
        record.stop();
        record.release();

/////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
//save .wav file
/////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
        byte dataByte[] = new byte[2*nSamples];

        for (int i = 0; i< nSamples; i++)
        {
            dataByte[2*i] = (byte)(audioData[i] & 0x00ff);
            dataByte[2*i +1] = (byte)((audioData[i] >> 8) & 0x00ff);
        }

        File check = new File( storeDir + "/" + _fileName2 + Integer.toString(numberOfTest) + ".wav");

        while(check.exists()) {
            numberOfTest++;
            check = new File( storeDir + "/" + _fileName2 + Integer.toString(numberOfTest) + ".wav");
        }

        WavIO writeWav = new WavIO(storeDir + "/" + _fileName2 + Integer.toString(numberOfTest) + ".wav", 16,1,1,Fs,2,16,dataByte);
        writeWav.save();

/////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
//framing audio data
/////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
        ArrayList<float[]> floatSamplesPerFrame = new ArrayList<>();

        while (nSamples - nSamplesAlreadyProcessed >= nSamplesPerFrame/2) { //fino a che trovo blocchi lunghi nsamplesperframe mezzi

            float[] temp = new float[nSamplesPerFrame];

            for (int i = 0; i < nSamplesPerFrame; i++) {

                temp[i] = audioData[i + nSamplesAlreadyProcessed -(80*(nSamplesAlreadyProcessed/nSamplesPerFrame))];
            }

            floatSamplesPerFrame.add(temp);

            if(nSamplesAlreadyProcessed == 0) {

                nSamplesAlreadyProcessed = nSamplesPerFrame;
            }
            else {
                nSamplesAlreadyProcessed += nSamplesPerFrame/2;
            }
        }

/////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
//features extraction (mfcc + delta + deltadelta)
/////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
        ArrayList<double[]> cepCoeffPerFrame = new ArrayList<double[]>();
        ArrayList<double[]> deltadelta = new ArrayList<double[]>();

        TarsosDSPAudioFormat af = new TarsosDSPAudioFormat(Fs,16,record.getChannelCount(),true,true);
        AudioEvent ae = new AudioEvent(af);
        MFCC mfcc = new MFCC(nSamplesPerFrame,Fs,13,30, 133.3334f, ((float)Fs)/2f);

        for(int j =0; j< floatSamplesPerFrame.size(); j++){

            ae.setFloatBuffer(floatSamplesPerFrame.get(j));//metto nel buffer di ae un blocco di campioni alla volta (singoli frame)
            mfcc.process(ae);//calcolo mfcc sul singolo frame

            cepCoeffPerFrame.add(convertFloatsToDoubles(mfcc.getMFCC()));//salvo gli mfcc in una lista di array (ciascuno da 13 elementi)

        }

        deltadelta = computeDeltas(computeDeltas(cepCoeffPerFrame,2),2);//calcolo i delta di secondo ordine applicando due volte la funzione delta

/////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
//print features on file
/////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

        check = new File( storeDir +"/testDataFormat" + speakerName+ Integer.toString(numberOfTest) + ".txt");

        while(check.exists()) {
            numberOfTest++;
            check = new File( storeDir +"/testDataFormat" + speakerName+ Integer.toString(numberOfTest) + ".txt");
        }

        printFeaturesOnFileFormat(cepCoeffPerFrame,deltadelta, storeDir + "/testDataFormat" + speakerName + numberOfTest + ".txt",speaker);

        return null;
    }

    @Override
    protected void onPostExecute(String string) {
        super.onPostExecute(string);
        showToastMessage("end", 500);
    }

    public void showToastMessage(String text, int duration) {
        final Toast toast = Toast.makeText(context , text, Toast.LENGTH_SHORT);
        toast.show();
        Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                toast.cancel();
            }
        }, duration);
    }
}




