package com.example.motta.recorderspeakerspeech;

import android.content.Context;
import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.MediaRecorder;
import android.os.AsyncTask;
import android.os.Environment;
import android.util.ArrayMap;
import android.util.Log;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
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
import libsvm.svm_parameter;
import libsvm.svm_problem;

import  static com.example.motta.recorderspeakerspeech.SupportFunctions.computeDeltas;
import  static com.example.motta.recorderspeakerspeech.SupportFunctions.convertFloatsToDoubles;
import  static com.example.motta.recorderspeakerspeech.SupportFunctions.printFeaturesOnFile;
import static com.example.motta.recorderspeakerspeech.SupportFunctions.readTestDataFromFormatFile;
import  static com.example.motta.recorderspeakerspeech.SupportFunctions.uniteAllFeaturesInOneList;
import  static com.example.motta.recorderspeakerspeech.SupportFunctions.printFeaturesOnFileFormat;
/**
 * Created by Giulia on 11/04/2018.
 */

public class Rec extends AsyncTask<String,Void,String> {

    private final String TAG = "Rec";
    private final double frameLenght = 0.02;
    private Context context = null;

    private int speaker = 0;
    private int recordingLenghtInSec = 0;
    private int Fs = 0; //freq di campionamento
    private int nSamples = 0;
    private int nSamplesPerFrame = 0;
    private int nSamplesAlreadyProcessed = 0;

    private short[] audioData = null; //java codifica i campioni audio in degli short 16 bit
    private AudioRecord record = null;

    private boolean trainingOrTesting = true;

    public Rec(Context _context, int _recordingLenghtInSec, int _Fs,int _speaker)
    {

        speaker = _speaker;
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

        Toast.makeText(context,"Start Recording", Toast.LENGTH_LONG).show();
    }

    @Override
    protected String doInBackground(String... strings) { //gli ingressi sono quelli passati quando faccio async.Execute//le passo sia il nome della cartella in cui salvare il file e il nome del file --> possono anche essere passati direttamente al costruttore

        String _path = strings[0];
        String _fileName = strings[1]; // usato per il train e test svm
        String _fileName2 = strings[2]; //usato per il file .wav e STT


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

        WavIO writeWav = new WavIO(storeDir + "/" + _fileName2, 16,1,1,Fs,2,16,dataByte);
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

        String numberOfTest = strings[3];
        int numberOfTrainingSpeakers = 2;
        int totalNumberOfFeatures = 2 * (cepCoeffPerFrame.get(0).length);
        int numberOfFramesPerSpeaker = cepCoeffPerFrame.size();
        int totalNumberOfFrames = numberOfFramesPerSpeaker * numberOfTrainingSpeakers;

        if(trainingOrTesting) {//caso training

           /* printFeaturesOnFile(cepCoeffPerFrame, deltadelta, fileDir,speaker);//crea il file che va in ingresso alla svm per il training

            printFeaturesOnFileFormat(cepCoeffPerFrame,deltadelta, fileDir + "WithFormat.txt",speaker);
*/

            ////printFeaturesOnFileFormat(cepCoeffPerFrame,deltadelta, fileDir + "WithFormat" + String.valueOf(speaker) + ".txt",speaker);



            ArrayList<double[]> union = uniteAllFeaturesInOneList(cepCoeffPerFrame, deltadelta);//converto i dati di test in un array di svm_node
            double[] labels = new double[numberOfFramesPerSpeaker];
            svm_node[][] data = new svm_node[numberOfFramesPerSpeaker][totalNumberOfFeatures + 1];


            for (int i = 0; i < numberOfFramesPerSpeaker; i++) {

                labels[i] = (double) speaker;

                for (int j = 0; j < totalNumberOfFeatures; j++) {

                    svm_node node = new svm_node();
                    node.index = j;
                    node.value = union.get(i)[j];

                    data[i][j] = node;
                }

                svm_node finalNode = new svm_node();
                finalNode.index = -1;
                finalNode.value = 0;

                data[i][totalNumberOfFeatures] = finalNode;
            }


            //data = readTestDataFromFormatFile(storeDir + "/trainingDataWithFormat2.txt",numberOfFramesPerSpeaker,totalNumberOfFeatures);
            data = readTestDataFromFormatFile(storeDir + "/testDataFormatMJ1.txt",numberOfFramesPerSpeaker,totalNumberOfFeatures);

            svm_problem problem = new svm_problem();
            problem.x = data;
            problem.y = labels;
            problem.l = labels.length;


            svm_parameter parameters = new svm_parameter();
            parameters.kernel_type = 2;
            parameters.svm_type = 2;
            parameters.gamma = 0.00008;
            parameters.C = 1;
            parameters.nu = 0.003;
            parameters.eps = Math.pow(10,-7);

            svm_model model = svm.svm_train(problem,parameters);

            try {
                svm.svm_save_model(storeDir + "/modelSpeaker" + String.valueOf(speaker) + ".txt",model);
            }
            catch (IOException excepion){

            }

            int a = 0;
        }

/////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
//speaker recognition using svm
/////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

        else {//caso testing



            double[] labels = new double[totalNumberOfFrames];


            svm_node[][] dataToSvm = new svm_node[totalNumberOfFrames][totalNumberOfFeatures + 1];

            try {
                /*FileInputStream fileInputStream = new FileInputStream(fileDir);
                DataInputStream dataInputStream = new DataInputStream(fileInputStream);

                for (int i = 0; i < totalNumberOfFrames; i++) {


                    labels[i] = dataInputStream.readDouble();

                    for (int j = 0; j < totalNumberOfFeatures; j++) {

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
                */

                svm_problem problem = new svm_problem();
                problem.x = dataToSvm;
                problem.y = labels;
                problem.l = labels.length;


                svm_parameter parameters = new svm_parameter();
                parameters.kernel_type = 2;
                parameters.gamma = 0.003;
                parameters.C = 1;
                parameters.nu = 0.038;
                parameters.eps = Math.pow(10,-7);


                svm_model model = new svm_model();


                //model = svm.svm_train(problem, parameters);
                //model = svm.svm_load_model(new BufferedReader(new FileReader(storeDir + "/model.txt")));

                svm_model modelOne;
                svm_model modelTwo;
                modelOne = svm.svm_load_model(new BufferedReader(new FileReader(storeDir + "/modelSpeaker1.txt")));
                modelTwo = svm.svm_load_model(new BufferedReader(new FileReader(storeDir + "/modelSpeaker2.txt")));

                /**/
                ArrayList<svm_model> modelList = new ArrayList<>();

                for(int i = 0; i<numberOfTrainingSpeakers;i++)
                {
                    svm_model loadedModel = svm.svm_load_model(new BufferedReader(new FileReader(storeDir + "/modelSpeaker" + String.valueOf(i) + ".txt")));
                    modelList.add(i,loadedModel);
                }

                /**/


                //svm.svm_save_model(storeDir + "/model.txt",model);


                //printFeaturesOnFileFormat(cepCoeffPerFrame,deltadelta, storeDir + "/testDataFormatMJ" + numberOfTest + ".txt",speaker);


                ArrayList<double[]> union = uniteAllFeaturesInOneList(cepCoeffPerFrame, deltadelta);//converto i dati di test in un array di svm_node
                svm_node[][] testData = new svm_node[numberOfFramesPerSpeaker][totalNumberOfFeatures + 1];

                for (int i = 0; i < numberOfFramesPerSpeaker; i++) {

                    for (int j = 0; j < totalNumberOfFeatures; j++) {

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

                //testData = readTestDataFromFormatFile(storeDir + "/testDataFormat" + numberOfTest + ".txt",numberOfFramesPerSpeaker,totalNumberOfFeatures);

                //int frequency;
                //int mostFrequency = 0;
                //double mostFrequentValue = 0;

                ArrayMap<Double, String> speakers = new ArrayMap<>(numberOfTrainingSpeakers);
                speakers.put(Double.valueOf(1),"Speaker One");
                speakers.put(Double.valueOf(2),"Speaker Two");


                ArrayList<Double> results = new ArrayList<>();
                double res;

                ArrayList<ArrayList<Double>> resultsList = new ArrayList<>();
                ArrayList<Double> resultOne = new ArrayList<>();
                ArrayList<Double> resultTwo = new ArrayList<>();

                resultsList.add(0,resultOne);
                resultsList.add(1,resultTwo);

                /**/
                for(int i = 0; i< numberOfTrainingSpeakers; i++)////////////////////
                {
                    resultsList.add(i,new ArrayList<Double>());
                }
                /**/

                for (int i = 0; i < numberOfFramesPerSpeaker; i++) {

                    //res = svm.svm_predict(model, testData[i]);
                    //results.add(i,res);

                    res = svm.svm_predict(modelOne,testData[i]);
                    resultOne.add(i,res);

                    res = svm.svm_predict(modelTwo,testData[i]);
                    resultTwo.add(i,res);

                    /**/

                    for(int j = 0; j< numberOfTrainingSpeakers; j++)
                    {
                        res = svm.svm_predict(modelList.get(j),testData[i]);
                        resultsList.get(j).add(i,res);
                    }

                    /**/
                }

                /*for (int j = 0; j < results.size(); j++) {

                    frequency = Collections.frequency(results, results.get(j));

                    if (frequency >= mostFrequency) {
                        mostFrequency = frequency;
                        mostFrequentValue = results.get(j);
                    }
                }
               */
                int speaker = 0;
                int maxFrequency = 0;
                int frequencyForList = 0;


                for(int i = 0; i< resultsList.size(); i++){

                    frequencyForList = Collections.frequency(resultsList.get(i), new Double(1));

                    if( frequencyForList >= maxFrequency ){
                        maxFrequency = frequencyForList;
                        speaker = i;
                    }
                }

                ArrayList<String> names = new ArrayList<>();
                names.add(0, "Speaker One");
                names.add(1,"Speaker Two");

                String recognizedSpeaker;
                double percentOfAccuracy = 0.8;


                if(maxFrequency >= percentOfAccuracy*numberOfFramesPerSpeaker) {
                    recognizedSpeaker = names.get(speaker);
                }
                else{
                    recognizedSpeaker = "Unknown";
                }

                return recognizedSpeaker;
                /////////////////////////////////////////////////

                /*if(mostFrequency >= 0.75*results.size()) {

                    recognizedSpeaker = speakers.get(mostFrequentValue);
                }
                else{
                    recognizedSpeaker = "Unknown";
                }
                return  recognizedSpeaker;

                */

                }catch (Exception exception) {
                Log.e("Read from trainingFile", "Error while reading from trainingFile");
            }
        }
        return null;
    }

    @Override
    protected void onPostExecute(String string) {
        super.onPostExecute(string);
        //Toast.makeText(context,"Ended Recording",Toast.LENGTH_LONG).show();
        Toast.makeText(context, string, Toast.LENGTH_LONG).show();
    }



}
