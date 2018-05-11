package com.mafi.andrea.audiorecorder;

import android.content.Context;
import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.MediaRecorder;
import android.os.AsyncTask;
import android.os.Environment;
import android.util.Log;
import android.widget.Toast;

import java.io.File;
import java.util.ArrayList;

import be.tarsos.dsp.AudioEvent;
import be.tarsos.dsp.io.TarsosDSPAudioFormat;
import be.tarsos.dsp.mfcc.MFCC;
import wav.WavIO;

/**
 * Created by andre on 11/04/2018.
 */

public class Rec extends AsyncTask <String, String, Void> {

    private final String TAG = "REC";
    private final double frameLenght = 0.02;
    private Context mContext = null;

    private int recordingLengthInSec = 0;
    private int Fs = 0;
    private int nSamples = 0;
    private int nSamplesPerFrame = 0;
    private int nSamplesAlreadyProcessed = 0;

    private short[] audioData = null;
    private AudioRecord record = null;

    public Rec(Context context, int _recordingLengthInSec, int _Fs) {
        mContext = context;
        recordingLengthInSec = _recordingLengthInSec;
        Fs = _Fs;
        nSamples = recordingLengthInSec * Fs;
        nSamplesPerFrame = (int) (frameLenght * _Fs);

        audioData = new short[nSamples];

        record = new AudioRecord(MediaRecorder.AudioSource.MIC, Fs, AudioFormat.CHANNEL_IN_MONO,
                AudioFormat.ENCODING_PCM_16BIT, 2*nSamples); //perchè gli short sono da 2 byte
    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();

        Toast.makeText(mContext,"Start recording",Toast.LENGTH_LONG).show();
    }

    @Override
    protected Void doInBackground(String... strings) {
/*
        String _path = strings[0];
        String _filename = strings[1];

        String storeDir = Environment.getExternalStorageDirectory() + "/" + _path;
        File f = new File(storeDir);

        if(!f.exists()){
            if(!f.mkdir()){
                Log.e(TAG,"Cannot create directory.");
            }
        }
*/
        record.startRecording();
        record.read(audioData, 0, nSamples);
/*
        byte dataByte[] = new byte[2*nSamples];
        for(int i = 0; i < nSamples; i++){
            dataByte[2*i] = (byte)(audioData[i] & 0x00ff);
            dataByte[2*i + 1] = (byte)((audioData[i] >> 8) & 0x00ff);
        }

        WavIO writeWav = new WavIO(storeDir + "/" + _filename, 16, 1,
                1, Fs, 2, 16, dataByte);
        writeWav.save();
*/

        ArrayList<float[]> floatSamplesPerFrame = new ArrayList<>();

        ////int flag = 0;
        ////int flag1 = 0;

        //nSamples - 80*flag -80*flag1 >= nSamplesPerFrame/2
        ////nSamplesAlreadyProcessed < 79839
        while (nSamples - nSamplesAlreadyProcessed >= nSamplesPerFrame/2) { //fino a che trovo blocchi lunghi nsamplesperframe mezzi


            //////if(nSamplesAlreadyProcessed == 39840)
            //////{
            //////int i = 0;
            //////}
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
            ////if(nSamplesAlreadyProcessed == nSamplesPerFrame)
            ////{
            ////flag = 1;
            ////flag1 = 1;
            ////}
            ////else
            ////{
            ////flag1 = 0;
            ////}

        }

        ArrayList <float[]> cepCoeffPerFrame = new ArrayList<float[]>();


        TarsosDSPAudioFormat af = new TarsosDSPAudioFormat(Fs,16,record.getChannelCount(),true,true);
        AudioEvent ae = new AudioEvent(af);
        MFCC mfcc = new MFCC(nSamplesPerFrame,Fs,13,30, 133.3334f, ((float)Fs)/2f);

        for(int j =0; j< floatSamplesPerFrame.size(); j++){

            ae.setFloatBuffer(floatSamplesPerFrame.get(j));
            mfcc.process(ae);

            cepCoeffPerFrame.add(mfcc.getMFCC());//dim effettiva degli array (i coeff) ritornati da mfcc

        }

        return null;
    }

    @Override
    protected void onPostExecute(Void aVoid) {
        super.onPostExecute(aVoid);

        Toast.makeText(mContext,"End recording",Toast.LENGTH_LONG).show();
    }
}
