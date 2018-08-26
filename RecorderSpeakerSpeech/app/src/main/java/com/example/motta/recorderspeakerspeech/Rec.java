package com.example.motta.recorderspeakerspeech;

import android.content.Context;
import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.MediaRecorder;
import android.os.AsyncTask;
import android.os.Environment;
import android.os.Handler;
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
import static com.example.motta.recorderspeakerspeech.SupportFunctions.scaleTestData;
import static com.example.motta.recorderspeakerspeech.SupportFunctions.scaleTrainingData;
import  static com.example.motta.recorderspeakerspeech.SupportFunctions.uniteAllFeaturesInOneList;
import  static com.example.motta.recorderspeakerspeech.SupportFunctions.printFeaturesOnFileFormat;
/**
 * Created by Giulia on 11/04/2018.
 */

public class Rec extends AsyncTask<String,Void,String> {

    private final String TAG = "Rec";
    private double frameLength = 0;
    private Context context = null;

    private int speaker = 0;
    private String speakerName = null;
    private int recordingLenghtInSec = 0;
    private int Fs = 0; //freq di campionamento
    private int nSamples = 0;
    private int nSamplesPerFrame = 0;
    private int nSamplesAlreadyProcessed = 0;
    private float accuracySP1 ;
    private float accuracySP2 ;
    private float accuracySP3 ;

    private short[] audioData = null; //java codifica i campioni audio in degli short 16 bit
    private AudioRecord record = null;

    private boolean trainingOrTesting = false;

    public Rec(Context _context, int _recordingLenghtInSec, int _Fs,int _speaker , String _speakerName , Double _frameLength)
    {

        speaker = _speaker;
        speakerName = _speakerName;
        frameLength = _frameLength;
        context = _context;
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
        //Toast.makeText(context,"Start Recording", Toast.LENGTH_SHORT).show();
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

        WavIO writeWav = new WavIO(storeDir + "/" + _fileName2 + ".wav", 16,1,1,Fs,2,16,dataByte);
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

        //String numberOfTest = strings[3];
        int numberOfTrainingSpeakers = 3;
        int totalNumberOfFeatures = 2 * (cepCoeffPerFrame.get(0).length);
        int numberOfFramesPerSpeaker = cepCoeffPerFrame.size();
        int totalNumberOfFrames = numberOfFramesPerSpeaker * numberOfTrainingSpeakers;

        if(trainingOrTesting) {//caso training

           /* printFeaturesOnFile(cepCoeffPerFrame, deltadelta, fileDir,speaker);//crea il file che va in ingresso alla svm per il training

            printFeaturesOnFileFormat(cepCoeffPerFrame,deltadelta, fileDir + "WithFormat.txt",speaker);
*/
            //////printFeaturesOnFileFormat(cepCoeffPerFrame,deltadelta, fileDir + "WithFormat" + String.valueOf(speaker) + ".txt",speaker);


            /**/
            numberOfFramesPerSpeaker = numberOfFramesPerSpeaker*5;
            /**/

            ArrayList<double[]> union = uniteAllFeaturesInOneList(cepCoeffPerFrame, deltadelta);//converto i dati di test in un array di svm_node
            double[] labels = new double[numberOfFramesPerSpeaker];
            svm_node[][] data = new svm_node[numberOfFramesPerSpeaker][totalNumberOfFeatures + 1];
            svm_node[][] scaledData = new svm_node[numberOfFramesPerSpeaker][totalNumberOfFeatures + 1];


            /**/
            numberOfFramesPerSpeaker = numberOfFramesPerSpeaker/5;
            /**/

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
            //data = readTestDataFromFormatFile(storeDir + "/testDataFormatMT2.txt",numberOfFramesPerSpeaker,totalNumberOfFeatures);

            /**/
            numberOfFramesPerSpeaker = numberOfFramesPerSpeaker*5;
            /**/

            data = readTestDataFromFormatFile(storeDir + "/MarcoB6vsAll.txt",numberOfFramesPerSpeaker,totalNumberOfFeatures);


            /**/
            for(int i = 0; i < 299; i++)
            {
                labels[i] = 1;
            }
            for(int i = 299 ; i< numberOfFramesPerSpeaker; i++)
            {
                labels[i] = 2;
            }
            /**/


            //speaker = 3;
            //scaledData = scaleTrainingData(data,storeDir,speaker);

            svm_problem problem = new svm_problem();
            //problem.x = scaledData;
            problem.x = data;
            problem.y = labels;
            problem.l = labels.length;


            svm_parameter parameters = new svm_parameter();
            parameters.kernel_type = 2;
            //parameters.svm_type = 2;
            parameters.gamma = 0.0008;
            parameters.C = 10;
            //parameters.nu = 0.0083;
            parameters.eps = Math.pow(10,-7);

            svm_model model = svm.svm_train(problem,parameters);

            try {
                svm.svm_save_model(storeDir + "/modelSpeaker" + String.valueOf(speaker) + ".txt",model);
            }
            catch (IOException excepion){
                Log.e("Training", "Error while saving model to file");
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

                /**/
                numberOfTrainingSpeakers = 3;
                /**/

                for(int i = 0; i<numberOfTrainingSpeakers;i++)
                {
                    svm_model loadedModel = svm.svm_load_model(new BufferedReader(new FileReader(storeDir + "/modelSpeaker" + String.valueOf(i + 1) + ".txt")));
                    modelList.add(i,loadedModel);
                }

                /**/
                for(int i=0;i<numberOfTrainingSpeakers;i++)
                {
                    svm_node[][] data = new svm_node[modelList.get(i).SV.length][totalNumberOfFeatures +1];

                    for(int j = 0; j< modelList.get(i).SV.length; j++)
                    {
                        for(int k = 0; k < totalNumberOfFeatures; k++)
                        {
                            svm_node node = new svm_node();
                            node.index = modelList.get(i).SV[j][k].index - 1;
                            node.value = modelList.get(i).SV[j][k].value;
                            data[j][k] = node;
                        }

                        svm_node finalNode = new svm_node();
                        finalNode.index = -1;
                        finalNode.value = 0;
                        data[j][totalNumberOfFeatures] = finalNode;
                    }

                    modelList.get(i).SV = data;
                }

                if(speakerName == null){

                    //svm.svm_save_model(storeDir + "/model.txt",model);
                    check = new File( storeDir +"/testDataFormat" + speakerName+ Integer.toString(numberOfTest) + ".txt");

                    while(check.exists()) {
                        numberOfTest++;
                        check = new File( storeDir +"/testDataFormat" + speakerName+ Integer.toString(numberOfTest) + ".txt");
                    }

                    printFeaturesOnFileFormat(cepCoeffPerFrame, deltadelta, storeDir + "/testDataFormat" + speakerName + numberOfTest + ".txt", speaker);

                }
                else{
                    //svm.svm_save_model(storeDir + "/model.txt",model);
                    check = new File( storeDir +"/" + speakerName+ Integer.toString(numberOfTest) + ".txt");

                    while(check.exists()) {
                        numberOfTest++;
                        check = new File( storeDir +"/" + speakerName+ Integer.toString(numberOfTest) + ".txt");
                    }

                    printFeaturesOnFileFormat(cepCoeffPerFrame, deltadelta, storeDir + "/" + speakerName + numberOfTest + ".txt", speaker);
                }

                ArrayList<double[]> union = uniteAllFeaturesInOneList(cepCoeffPerFrame, deltadelta);//converto i dati di test in un array di svm_node
                svm_node[][] testData = new svm_node[numberOfFramesPerSpeaker][totalNumberOfFeatures + 1];
                svm_node[][] scaledTestData = new svm_node[numberOfFramesPerSpeaker][totalNumberOfFeatures +1];

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

                //testData = readTestDataFromFormatFile(storeDir + "/testDataFormatMJ1" + numberOfTest + ".txt",numberOfFramesPerSpeaker,totalNumberOfFeatures);

                //testData = readTestDataFromFormatFile(storeDir + "/Jacopo21.txt",numberOfFramesPerSpeaker,totalNumberOfFeatures);

                //int frequency;
                //int
                //mostFrequency = 0;
                //double mostFrequentValue = 0;

                ArrayMap<Double, String> speakers = new ArrayMap<>(numberOfTrainingSpeakers);
                speakers.put(Double.valueOf(1),"Speaker One");
                speakers.put(Double.valueOf(2),"Speaker Two");


                ArrayList<Double> results = new ArrayList<>();
                double res;


                ArrayList<ArrayList<Double>> resultsList = new ArrayList<>();

                /*
                ArrayList<Double> resultOne = new ArrayList<>();
                ArrayList<Double> resultTwo = new ArrayList<>();

                resultsList.add(0,resultOne);
                resultsList.add(1,resultTwo);
                */

                /******
                for(int i = 0; i< numberOfTrainingSpeakers; i++)////////////////////
                {
                    resultsList.add(i,new ArrayList<Double>());
                }
                ******/

                for(int j = 0; j< numberOfTrainingSpeakers; j++) {

                    int speaker = j + 1;
                    resultsList.add(j,new ArrayList<Double>());


                    //scaledTestData = scaleTestData(testData,speaker,storeDir);

                    for (int i = 0; i < numberOfFramesPerSpeaker; i++) {

                        //res = svm.svm_predict(model, testData[i]);
                        //results.add(i,res);

                    /*res = svm.svm_predict(modelOne,testData[i]);
                    resultOne.add(i,res);

                    res = svm.svm_predict(modelTwo,testData[i]);
                    resultTwo.add(i,res);
                    */
                    /**/

                        /*
                        for (int j = 0; j < numberOfTrainingSpeakers; j++) {
                        */
                            //res = svm.svm_predict(modelList.get(j), scaledTestData[i]);
                            res = svm.svm_predict(modelList.get(j),testData[i]);
                            resultsList.get(j).add(i, res);
                        /*
                        }
                        */
                    /**/
                    }
                }
                /*for (int j = 0; j < results.size(); j++) {

                    frequency = Collections.frequency(results, results.get(j));

                    if (frequency >= mostFrequency) {
                        mostFrequency = frequency;
                        mostFrequentValue = results.get(j);
                    }
                }
               */
                int speaker = - 1;
                int maxFrequency = 0;
                int frequencyForList = 0;
                int frequency = 0;
                int mostFrequency = 0;
                double mostFrequentValue = 0;

                ArrayList<Integer> frequencies = new ArrayList<>();


                for(int i = 0; i< resultsList.size(); i++){

                    frequencyForList = Collections.frequency(resultsList.get(0), new Double(1));

                    frequencies.add(i,frequencyForList);

                    /****************
                    if( frequencyForList >= maxFrequency ){
                        maxFrequency = frequencyForList;
                        speaker = i + 1;
                    }
                    ****************/

                }

                double temp;
                int temp2;

                int counter1 = 0;
                int counter2 = 0;
                int counter3 = 0;

                for (int i = 0; i<numberOfFramesPerSpeaker ; i++) {

                    temp = resultsList.get(0).get(i);
                    temp2 = (int)temp;

                    if (temp2 == 1){
                        counter1++;
                    }
                    accuracySP1 = (counter1*100/numberOfFramesPerSpeaker);
                }

                for (int i = 0; i<numberOfFramesPerSpeaker ; i++) {

                    temp = resultsList.get(1).get(i);
                    temp2 = (int)temp;

                    if (temp2 == 1){
                        counter2++;
                    }
                    accuracySP2 = (counter2*100/numberOfFramesPerSpeaker);
                }

                for (int i = 0; i<numberOfFramesPerSpeaker ; i++) {

                    temp = resultsList.get(2).get(i);
                    temp2 = (int)temp;

                    if (temp2 == 1){
                        counter3++;
                    }
                    accuracySP3 = (counter3*100/numberOfFramesPerSpeaker);
                }

                ArrayList<String> names = new ArrayList<>();
                names.add(0, "Speaker One MB");
                names.add(1,"Speaker Two MJ");
                names.add(2,"Speaker Three MT");

                ArrayList<Double> percentages = new ArrayList<>();
                percentages.add(0,0.90);
                percentages.add(1,0.80);
                percentages.add(2,0.9);

                /*****************
                double percentOfAccuracy = percentages.get(speaker -1);
                *****************/
/*
                ArrayList<Double> relativeFrequencies = new ArrayList<>();
                ArrayList<Double> relativeFrequenciesCopy = new ArrayList<>();

                for(int i=0; i< numberOfTrainingSpeakers; i++)
                {
                    relativeFrequencies.add(i,frequencies.get(i) - percentages.get(i)*numberOfFramesPerSpeaker);
                    relativeFrequenciesCopy.add(i,frequencies.get(i)- percentages.get(i)*numberOfFramesPerSpeaker);
                }

                Collections.sort(relativeFrequenciesCopy,Collections.<Double>reverseOrder());





                int i = Integer.valueOf(relativeFrequenciesCopy.get(0).intValue());
                String recognizedSpeaker = "Unknown" + ", not speaker" + String.valueOf(relativeFrequencies.indexOf(relativeFrequenciesCopy.get(0)) + 1) + " for " + String.valueOf(i) + " frames";

                for(int j = 0; j< numberOfTrainingSpeakers; j++)
                {
                    speaker = relativeFrequencies.indexOf(relativeFrequenciesCopy.get(j)) + 1;

                    if(frequencies.get(j) >= percentages.get(speaker -1)*numberOfFramesPerSpeaker)
                    {
                        int k = Integer.valueOf(relativeFrequenciesCopy.get(j).intValue());
                        recognizedSpeaker = names.get(speaker - 1) + " for " + String.valueOf(k) + " frames";
                        break;
                    }

                }

*/
                String recognizedSpeaker = "Speaker 1 = " +(int)accuracySP1 +"% "+"Speaker 2 = " + (int)accuracySP2 + "% "+"Speaker 3 = " + (int)accuracySP3 + "% " ;
                return recognizedSpeaker;
                /************************
                if(maxFrequency >= percentOfAccuracy*numberOfFramesPerSpeaker) {
                    recognizedSpeaker = names.get(speaker -1);
                }
                else{
                    recognizedSpeaker = "Unknown";
                }

                return recognizedSpeaker;


                 for (int i = 0; i<numberOfFramesPerSpeaker ; i++) {

                 if (resultsList.get(0).get(i) = )



                 }


                **************************/

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
        Toast.makeText(context, string, Toast.LENGTH_SHORT).show();
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
