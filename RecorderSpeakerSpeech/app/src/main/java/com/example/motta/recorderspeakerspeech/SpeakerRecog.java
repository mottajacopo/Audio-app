package com.example.motta.recorderspeakerspeech;

import android.content.Context;
import android.os.AsyncTask;
import android.os.Environment;

import android.util.Log;
import android.widget.TextView;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;

import be.tarsos.dsp.AudioEvent;
import be.tarsos.dsp.io.TarsosDSPAudioFormat;
import be.tarsos.dsp.mfcc.MFCC;
import libsvm.svm;
import libsvm.svm_model;
import libsvm.svm_node;

import static com.example.motta.recorderspeakerspeech.SupportFunctions.computeDeltas;
import static com.example.motta.recorderspeakerspeech.SupportFunctions.convertFloatsToDoubles;
import static com.example.motta.recorderspeakerspeech.SupportFunctions.matlabModelToAndroidModel;
import static com.example.motta.recorderspeakerspeech.SupportFunctions.printFeaturesOnFileFormat;
import static com.example.motta.recorderspeakerspeech.SupportFunctions.readTestDataFromFormatFile;
import static com.example.motta.recorderspeakerspeech.SupportFunctions.scaleTestData;
import static com.example.motta.recorderspeakerspeech.SupportFunctions.uniteAllFeaturesInOneList;


public class SpeakerRecog extends AsyncTask<String,Void,Boolean> {

    private final double frameLength = 0.02;// lunghezza frame in secondi
    private final double overlapPercentage = 0.5;//percentuale sovrapp. dei frame

    private Context context = null;
    private short[] samples = null;//campioni letti dal file .wav
    private int Fs = 0;
    private int recordingLengthInSec = 0;
    private int nSamples = 0;
    private int nSamplesPerFrame = 0;

    public SpeakerRecog(Context _context, int _Fs, int _recordingLengthInSec, TextView _textView)
    {
        context = _context;
        Fs = _Fs;
        recordingLengthInSec = _recordingLengthInSec;

        nSamples =  recordingLengthInSec*Fs;
        nSamplesPerFrame = (int)(frameLength*Fs);

        samples = new short[nSamples];

    }


    @Override
    protected void onPreExecute() {
        super.onPreExecute();

    }

    @Override
    protected Boolean doInBackground(String... strings) {

        String path = Environment.getExternalStorageDirectory() + "/" + strings[0];
        String recNumber = strings[1];
        String filePath = path + "/" + "rec" + recNumber + ".wav";
        String numberOfWavFiles = strings[2];

        boolean doToast = false;

        if(Integer.valueOf(numberOfWavFiles) == Integer.valueOf(recNumber))
        {
            doToast = true;
        }


        WavIO readWav = new WavIO(filePath);

        try {//provo a leggere il file .wav

            boolean success = readWav.read();

            if (!success) {//se la lettura non va a buon fine lnacio un'eccezione
                throw new IOException("Error while reading .wav file");
            }

        } catch (IOException exception) {
            Log.e("Speaker Recognition", exception.getMessage());
            return null;//se è stata generata l'eccezione esco dal doInBackground
        }


        short value = 0;

        for (int i = 0; i < nSamples; i++)//ricostruisco i campioni originali a partire dai byte salvati nel file .wav
        {

            value = (short) (((short) readWav.myData[2 * i] & 0x00ff) | ((short) readWav.myData[(2 * i) + 1] << 8 & 0xff00));//ricostruisco il campione a 16 bit unendo i primi 2 byte di myData

            samples[i] = value;
        }


        int nSamplesAlreadyProcessed = 0;
        int nSamplesOverlapped = (int) (overlapPercentage * nSamplesPerFrame);

//////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
//framing audio data
/////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

        ArrayList<float[]> floatSamplesPerFrame = new ArrayList<>();//lista contenete i frame

        int samplesBack = 0;//per il primo frame (da 0 a nSamplesPerFrame campioni) non devo realizzare overlapping con il frame precedente

        while (nSamples - nSamplesAlreadyProcessed >= nSamplesPerFrame - nSamplesOverlapped) { //dato l'overlapping per ogni frame avanzo di nSamplesPerFrame-nSamplesOverlapped frames

            float[] frame = new float[nSamplesPerFrame];


            for (int i = 0; i < nSamplesPerFrame; i++) {

                frame[i] = samples[i + nSamplesAlreadyProcessed - samplesBack];
            }


            floatSamplesPerFrame.add(frame);

            if (nSamplesAlreadyProcessed == 0) {//se sono al primo frame

                nSamplesAlreadyProcessed = nSamplesPerFrame;//i nuovi campioni considerati sono nSamplesPerFrame
                samplesBack = nSamplesOverlapped;//i frame successivi al primo possono considerare nSamplesOverlapped campioni dal frame precedente
            } else {
                nSamplesAlreadyProcessed += nSamplesPerFrame - nSamplesOverlapped;//negli altri casi i nuovi campioni sono nSamplesPerFrame-nSamplesOverlapped
            }
        }

/////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
//features extraction (mfcc + delta + deltadelta)
/////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
        ArrayList<double[]> cepCoeffPerFrame = new ArrayList<double[]>();
        ArrayList<double[]> deltadelta = new ArrayList<double[]>();

        /**//**//**//**/
        TarsosDSPAudioFormat af = new TarsosDSPAudioFormat(Fs, 16, 1, true, true);
        AudioEvent ae = new AudioEvent(af);
        MFCC mfcc = new MFCC(nSamplesPerFrame, Fs, 13, 30, 133.3334f, ((float) Fs) / 2f);

        for (int j = 0; j < floatSamplesPerFrame.size(); j++) {

            ae.setFloatBuffer(floatSamplesPerFrame.get(j));//metto nel buffer di ae un blocco di campioni alla volta (singoli frame)
            mfcc.process(ae);//calcolo mfcc sul singolo frame

            cepCoeffPerFrame.add(convertFloatsToDoubles(mfcc.getMFCC()));//salvo gli mfcc in una lista di array (ciascuno da 13 elementi)

        }

        deltadelta = computeDeltas(computeDeltas(cepCoeffPerFrame, 2), 2);//calcolo i delta di secondo ordine applicando due volte la funzione delta

        printFeaturesOnFileFormat(cepCoeffPerFrame, deltadelta, path + "/rec" + strings[1] + ".txt", 1);
/////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
//print features on file
/////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    return doToast;

    }

    @Override
    protected void onPostExecute(Boolean result) {
        super.onPostExecute(result);

        if(result)
        {
        Toast.makeText(context,"Ended conversion",Toast.LENGTH_LONG).show();
        }
    }
}
