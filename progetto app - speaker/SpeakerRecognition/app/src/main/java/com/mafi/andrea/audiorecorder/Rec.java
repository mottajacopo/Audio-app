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
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;


import be.tarsos.dsp.AudioEvent;
import be.tarsos.dsp.io.TarsosDSPAudioFormat;
import be.tarsos.dsp.mfcc.MFCC;
import umich.cse.yctung.androidlibsvm.LibSVM;
import wav.WavIO;

/**
 * Created by Giulia on 11/04/2018.
 */

public class Rec extends AsyncTask <String,Void,Void>{

    private final String TAG = "Rec";
    private final double frameLenght = 0.02;
    private Context context = null;

    private boolean testingOrTraining = false; //indica se si sta facendo il training o il testing -> false indica training
    private int recordingLenghtInSec = 0;
    private int Fs = 0; //freq di campionamento
    private int nSamples = 0;
    private int nSamplesPerFrame = 0;
    private int nSamplesAlreadyProcessed = 0;

    private LibSVM svm = new LibSVM();
    private short[] audioData = null; //java codifica i campioni audio in degli short 16 bit
    private AudioRecord record = null;

    public Rec(Context _context, int _recordingLenghtInSec, int _Fs)
    {

        context = _context;
        recordingLenghtInSec = _recordingLenghtInSec;
        Fs = _Fs;
        nSamples = _recordingLenghtInSec * _Fs;
        nSamplesPerFrame = (int) (frameLenght * _Fs);

        audioData = new short[nSamples]; //oppure passo direttamente l'array alla main activity per poi gestirlo li

        record = new AudioRecord(MediaRecorder.AudioSource.MIC, Fs, AudioFormat.CHANNEL_IN_MONO, AudioFormat.ENCODING_PCM_16BIT,2*nSamples);//il buffer in byte dovrà essere il doppio della dimensione dell array


    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();

        Toast.makeText(context,"Start Recording",Toast.LENGTH_LONG).show();
    }

    @Override
    protected Void doInBackground(String... strings) { //gli ingressi sono quelli passati quando faccio async.Execute//le passo sia il nome della cartella in cui salvare il file e il nome del file --> possono anche essere passati direttamente al costruttore



        String _path = strings[0];
        String _trainingFileName = strings[1];
        String _scaledTrainingFileName = strings[2];
        String _svmModelFileName = strings[3];
        String _testingFileName = strings[4];
        String _scaledTestingFileName = strings[5];
        String _outputFileName = strings[6];


        String storeDir = Environment.getExternalStorageDirectory() + "/" + _path;

        String trainingFilePath = storeDir + "/" + _trainingFileName;
        String scaledTrainingFilePath = storeDir + "/" + _scaledTrainingFileName;
        String svmModelFilePath = storeDir + "/" + _svmModelFileName;
        String testingFilePath = storeDir + "/" + _testingFileName;
        String scaledTestingFilePath = storeDir + "/" + _scaledTestingFileName;
        String outputFilePath = storeDir + "/" + _outputFileName;


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

        //byte dataByte[] = new byte[2*nSamples];

        //for (int i = 0; i< nSamples; i++)
        //{

        //  dataByte[2*i] = (byte)(audioData[i] & 0x00ff);
        //dataByte[2*i +1] = (byte)((audioData[i] >> 8) & 0x00ff);
        //}


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


                temp[i] = audioData[i + nSamplesAlreadyProcessed -((nSamplesPerFrame/2)*(nSamplesAlreadyProcessed/nSamplesPerFrame))];


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
        ArrayList<float[]> deltadelta = new ArrayList<float[]>();


        TarsosDSPAudioFormat af = new TarsosDSPAudioFormat(Fs,16,record.getChannelCount(),true,true);
        AudioEvent ae = new AudioEvent(af);
        MFCC mfcc = new MFCC(nSamplesPerFrame,Fs,13,30, 133.3334f, ((float)Fs)/2f);

        for(int j =0; j< floatSamplesPerFrame.size(); j++){

            ae.setFloatBuffer(floatSamplesPerFrame.get(j));//metto nel buffer di ae un blocco di campioni alla volta (singoli frame)
            mfcc.process(ae);//calcolo mfcc sul singolo frame

            cepCoeffPerFrame.add(mfcc.getMFCC());//salvo gli mfcc in una lista di array (ciascuno da 13 elementi)

        }

        deltadelta = computeDeltas(computeDeltas(cepCoeffPerFrame,2),2);//calcolo i delta di secondo ordine applicando due volte la funzione delta

        if(!testingOrTraining) { //se stiamo facendo il training

            printFeaturesOnFile(cepCoeffPerFrame, deltadelta, trainingFilePath);//crea il file che va in ingresso alla svm per il training



        }
        else{

            svm.scale(trainingFilePath, scaledTrainingFilePath);
            svm.train("-t 2 " + scaledTrainingFilePath + " " + svmModelFilePath);

            printFeaturesOnFile(cepCoeffPerFrame, deltadelta, testingFilePath);
            svm.scale(testingFilePath,scaledTestingFilePath);
            svm.predict(scaledTestingFilePath + " " + svmModelFilePath + " " + outputFilePath);

        }





        // WavIO writeWav = new WavIO(storeDir + "/" + _fileName, 16,1,1,Fs,2,16,dataByte);
        // writeWav.save();

        return null;

    }

    @Override
    protected void onPostExecute(Void aVoid) {
        super.onPostExecute(aVoid);
        Toast.makeText(context,"Ended Recording",Toast.LENGTH_LONG).show();
    }

    public ArrayList<float[]> computeDeltas (ArrayList<float[]> mfccCoeff, int n)
    {

        final int mfccCoeffNumber = mfccCoeff.get(0).length;

        ArrayList<float[]> deltas = new ArrayList<float[]>(mfccCoeff);//inizializzo i delta con i valori degli mfcc
        float[] deltaRaw = new float[mfccCoeffNumber];
        float num = 0;
        int denum = 0;


        for( int a = 1; a <= n;a++) //calcola denominatore delta
        {
            denum += Math.pow(a,n);
        }

        for(int i = n; i<mfccCoeff.size()-n ; i++){ //ripete per ogni array nella lista


            for (int j = 0; j < mfccCoeffNumber; j++) {  //ripete per ogni elemento nell array

                for (int index = 1; index <= n; index++) {//calcola numeratore delta

                    num += (index * (mfccCoeff.get(i + index)[j] - mfccCoeff.get(i - index)[j]));

                }

                deltaRaw[j] = num / (2*denum); //trova effettivamente il delta per l elemento j dell array

                num = 0;//resetto il numeratore
            }

            deltas.set(i, deltaRaw);

        }

        return  deltas;
    }

    public void printFeaturesOnFile (ArrayList<float[]> mfcc,ArrayList<float[]> deltadelta, String _fileDir)
    {
        final int mfccCoeffNumber = mfcc.get(0).length;

        final String label = "1"; //label che va cambiato ad ogni registrazione di un parlatore diverso

        float[] temp = new float[26];
        ArrayList<float[]> union = new ArrayList<float[]>(); //nuova lista dove ogni elemento sarà un array di float a 26 elementi per contenere sia gli mfcc che i delta

        for(int j=0; j<mfcc.size(); j++){

            for(int k=0; k< mfccCoeffNumber*2; k++){
                if(k < mfccCoeffNumber){
                    temp[k] = mfcc.get(j)[k];
                }
                else{
                    temp[k] = deltadelta.get(j)[k - mfccCoeffNumber];
                }
            }


            union.add(temp.clone());
        }

        try
        {
            FileWriter writeOnTrainingFile = new FileWriter(_fileDir,true);


            for(int b=0; b < union.size(); b++){//per ogni vettore di features da 26 elementi

                writeOnTrainingFile.write(label + " ");

                for(int i=0; i< mfccCoeffNumber*2; i++){

                    writeOnTrainingFile.write( Integer.toString(i+1) + ":" + Float.toString(union.get(b)[i]) + " ");
                }

                writeOnTrainingFile.write("\n");
            }

            writeOnTrainingFile.flush();
            writeOnTrainingFile.close();



        }
        catch (IOException exception)
        {

            Log.e("printOnTrainingFile","Training file not exists");
        }
    }
}
