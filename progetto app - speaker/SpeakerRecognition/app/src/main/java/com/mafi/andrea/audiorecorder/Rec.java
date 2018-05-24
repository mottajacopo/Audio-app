package com.mafi.andrea.audiorecorder;

import android.content.Context;
import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.MediaRecorder;
import android.os.AsyncTask;
import android.os.Environment;
import android.util.ArrayMap;
import android.util.Log;
import android.widget.Toast;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;


import be.tarsos.dsp.AudioEvent;
import be.tarsos.dsp.io.TarsosDSPAudioFormat;
import be.tarsos.dsp.mfcc.MFCC;
import wav.WavIO;

import libsvm.*;

/**
 * Created by Giulia on 11/04/2018.
 */

public class Rec extends AsyncTask <String,Void,Void>{





    private final String TAG = "Rec";
    private final double frameLenght = 0.02;
    private Context context = null;

    private int recordingLenghtInSec = 0;
    private int Fs = 0; //freq di campionamento
    private int nSamples = 0;
    private int nSamplesPerFrame = 0;
    private int nSamplesAlreadyProcessed = 0;

    private short[] audioData = null; //java codifica i campioni audio in degli short 16 bit
    private AudioRecord record = null;

    private boolean trainingOrTesting = false;

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
        String _fileName = strings[1];

        String storeDir = Environment.getExternalStorageDirectory() + "/" + _path;
        String fileDir = storeDir + "/" + _fileName + ".txt";
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

        ArrayList <double[]> cepCoeffPerFrame = new ArrayList<double[]>();
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

        if(trainingOrTesting) {//caso training

            printFeaturesOnFile(cepCoeffPerFrame, deltadelta, fileDir);//crea il file che va in ingresso alla svm per il training

        }
        else {//caso testing

            int numberOfTrainingSpeakers = 2;
            int totalNumberOfFeatures = 2*(cepCoeffPerFrame.get(0).length);
            int numberOfFramesPerSpeaker = cepCoeffPerFrame.size();
            int totalNumberOfFrames = numberOfFramesPerSpeaker*numberOfTrainingSpeakers;

            double[] labels = new double[totalNumberOfFrames];


            svm_node[][] dataToSvm = new svm_node[totalNumberOfFrames][totalNumberOfFeatures + 1];

            try {


                FileInputStream fileInputStream = new FileInputStream(fileDir);
                DataInputStream dataInputStream = new DataInputStream(fileInputStream);

                for( int i=0; i < totalNumberOfFrames; i++){


                    if(i == 499)
                    {
                        int s = dataInputStream.available();
                    }

                    labels[i] = dataInputStream.readDouble();

                    for(int j =0; j<totalNumberOfFeatures; j++){

                        svm_node node = new svm_node();
                        node.index = j;
                        node.value = dataInputStream.readDouble();

                        dataToSvm[i][j] = node;
                    }

                    svm_node finalNode = new svm_node();
                    finalNode.index = -1;
                    finalNode.value = 0;
                    dataToSvm[i][totalNumberOfFeatures] = finalNode;
                }

                dataInputStream.close();
                fileInputStream.close();

                svm_problem problem = new svm_problem();
                problem.x = dataToSvm;
                problem.y = labels;
                problem.l = labels.length;


                svm_parameter parameters = new svm_parameter();
                parameters.kernel_type = 2;
                parameters.gamma = 0.1;
                parameters.C = 8;
                parameters.shrinking = 1;


                svm_model model = svm.svm_train(problem,parameters);


                ArrayList<double[]> union = uniteAllFeaturesInOneList(cepCoeffPerFrame,deltadelta);//converto i dati di test in un array di svm_node
                svm_node[][] testData = new svm_node[numberOfFramesPerSpeaker][totalNumberOfFeatures + 1];

                for(int i = 0; i< numberOfFramesPerSpeaker; i++){

                    for(int j=0; j< totalNumberOfFeatures; j++){

                        svm_node node = new svm_node();
                        node.index = j;
                        node.value = union.get(i)[j];

                        testData[i][j] = node;
                    }

                    svm_node finalNode = new svm_node();
                    finalNode.index = -1;
                    finalNode.value = 0;

                    testData[i][totalNumberOfFeatures] = finalNode;
                }


                //double[] result = new double[numberOfFramesPerSpeaker];

                //for(int i = 0; i<numberOfFramesPerSpeaker; i++){

                    //result[i] = svm.svm_predict(model,testData[i]);

                //}

                //result = result;

                int frequency;
                int mostFrequency = 0;
                double mostFrequentValue = 0;


                ArrayMap<Double,String> speakers = new ArrayMap<>();
                speakers.keySet().add(new Double(1));
                speakers.keySet().add(new Double(2));
                speakers.setValueAt(0,"Speaker One");
                speakers.setValueAt(1,"Speaker Two");

                ArrayList<Double> results = new ArrayList<>();
                double res;

                for(int i=0; i < numberOfFramesPerSpeaker ; i++) {


                    res = svm.svm_predict(model, testData[i]);
                    results.set(i,res);
                }


                for(int j=0; j< results.size(); j++) {

                    frequency = Collections.frequency(results,results.get(j));

                    if(frequency >= mostFrequency){
                        mostFrequency=frequency;
                        mostFrequentValue = results.get(j);
                    }
                }


                String recognizedSpeaker = speakers.get(mostFrequentValue);

                int a = 0;

            }
            catch (IOException exception){
                Log.e("Read from trainingFile","Error while reading from trainingFile");
            }
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

    public ArrayList<double[]> computeDeltas (ArrayList<double[]> mfccCoeff, int n)
    {

        final int mfccCoeffNumber = mfccCoeff.get(0).length;

        ArrayList<double[]> deltas = new ArrayList<double[]>(mfccCoeff);//inizializzo i delta con i valori degli mfcc
        double[] deltaRaw = new double[mfccCoeffNumber];
        double num = 0;
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

    public void printFeaturesOnFile (ArrayList<double[]> mfcc,ArrayList<double[]> deltadelta, String _fileDir)
    {

        //final String label = "1"; //label che va cambiato ad ogni registrazione di un parlatore diverso

        final double label = 1;

        ArrayList<double[]> union = uniteAllFeaturesInOneList(mfcc,deltadelta);

        int totalNumberOfFeatures = union.get(0).length;

        try
        {
            //FileWriter writeOnTrainingFile = new FileWriter(_fileDir,true);

            //for(int b=0; b < union.size(); b++){//per ogni vettore di features da 26 elementi

                //writeOnTrainingFile.write(label + " ");

                //for(int i=0; i< mfccCoeffNumber*2; i++){

                    //writeOnTrainingFile.write( Integer.toString(i+1) + ":" + Double.toString(union.get(b)[i]) + " ");
                //}

                //writeOnTrainingFile.write("\n");
            //}

            //writeOnTrainingFile.flush();
            //writeOnTrainingFile.close();


            FileOutputStream fileOutputStream = new FileOutputStream(_fileDir,true); //controlla
            DataOutputStream dataOutputStream = new DataOutputStream(fileOutputStream);


            for(int i=0; i< union.size(); i++){

                dataOutputStream.writeDouble(label);


                for(int j=0; j < totalNumberOfFeatures; j++){

                    dataOutputStream.writeDouble(union.get(i)[j]);
                }
            }

            dataOutputStream.flush();
            dataOutputStream.close();
            fileOutputStream.flush();
            fileOutputStream.close();



        }
        catch (IOException exception)
        {

            Log.e("printOnTrainingFile","Training file not exists");
        }
    }

    public static double[] convertFloatsToDoubles(float[] input)
    {
        if (input == null)
        {
            return null; // Or throw an exception - your choice
        }
        double[] output = new double[input.length];
        for (int i = 0; i < input.length; i++)
        {
            output[i] = input[i];
        }
        return output;
    }

    public ArrayList<double[]> uniteAllFeaturesInOneList (ArrayList<double[]> mfcc, ArrayList<double[]> deltadelta)
    {
        final int mfccCoeffNumber = mfcc.get(0).length;

        double[] temp = new double[2*mfccCoeffNumber];
        ArrayList<double[]> union = new ArrayList<double[]>(); //nuova lista dove ogni elemento sarà un array di double a 26 elementi per contenere sia gli mfcc che i delta

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

        return union;
    }
}
