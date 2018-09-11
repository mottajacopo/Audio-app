package com.example.motta.recorderspeakerspeech;

import android.content.Context;
import android.os.AsyncTask;
import android.os.Environment;
import android.util.Log;
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
import static com.example.motta.recorderspeakerspeech.SupportFunctions.readTestDataFromFormatFile;
import static com.example.motta.recorderspeakerspeech.SupportFunctions.uniteAllFeaturesInOneList;

/**
 * Created by MyPC on 30/08/2018.
 */

public class SpeakerRecog extends AsyncTask<String,Void,String> {

    private final double frameLength = 0.02;
    private final double overlapPercentage = 0.5;
    private final boolean trainingOrTesting = false;

    private Context context = null;
    private short[] samples = null;
    private int Fs = 0;
    private int recordingLengthInSec = 0;
    private int nSamples = 0;
    private int nSamplesPerFrame = 0;

    public SpeakerRecog(Context _context/*, /*short[] _samples*/, int _Fs, int _recordingLengthInSec)
    {
        context = _context;
        Fs = _Fs;
        recordingLengthInSec = _recordingLengthInSec;

        nSamples =  recordingLengthInSec*Fs;
        nSamplesPerFrame = (int)(frameLength*Fs);
    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();

        Toast.makeText(context,"Starting recognition",Toast.LENGTH_LONG).show();
    }

    @Override
    protected String doInBackground(String... strings) {

        String path = Environment.getExternalStorageDirectory() + "/" + strings[0];
        String filePath = path + "/" + strings[1];

        WavIO readWav = new WavIO(filePath);

        try {

            boolean success = readWav.read();

            if (!success) {
                throw new IOException("Error while reading .wav file");
            }

        }
        catch (IOException exception){
            Log.e("Speaker Recognition",exception.getMessage());
            return null;
        }


        int length = readWav.myData.length/2;
        samples = new short[length];
        short value = 0;

        for(int i = 0;  i < length; i++)
        {
            if(i == 23998)
            {
                int a = 0;
            }
            value = (short) (((short) readWav.myData[2*i] & 0x00ff) | ((short)readWav.myData[(2*i)+1]<<8 & 0xff00));

            samples[i] = value;
        }


        int nSamplesAlreadyProcessed = 0;
        int nSamplesOverlapped = (int)overlapPercentage*nSamplesPerFrame;

//////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
//framing audio data
/////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

        ArrayList<float[]> floatSamplesPerFrame = new ArrayList<>();

        while (nSamples - nSamplesAlreadyProcessed >= nSamplesPerFrame/2/*nSamplesPerFrame-nSamplesOverlapped*/) { //fino a che trovo blocchi lunghi nsamplesperframe mezzi

            float[] temp = new float[nSamplesPerFrame];

            for (int i = 0; i < nSamplesPerFrame; i++) {


                temp[i] = samples[i + nSamplesAlreadyProcessed -(80*(nSamplesAlreadyProcessed/nSamplesPerFrame))];
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

        /**//**//**//**/
        TarsosDSPAudioFormat af = new TarsosDSPAudioFormat(Fs,16, 1,true,true);
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

        int numberOfTrainingSpeakers = 2;
        int totalNumberOfFeatures = 2 * (cepCoeffPerFrame.get(0).length);
        int numberOfFramesPerSpeaker = cepCoeffPerFrame.size();



                /**/


                try{
                //svm.svm_save_model(storeDir + "/model.txt",model);


                //printFeaturesOnFileFormat(cepCoeffPerFrame,deltadelta, storeDir + "/testDataFormat" + numberOfTest + ".txt",speaker);


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

                ArrayList<ArrayList<Double>> resultsList = new ArrayList<>();

                double res;
                svm_model loadedModel;

                for(int j = 0; j< numberOfTrainingSpeakers; j++) {

                    int speaker = j + 1;
                    resultsList.add(j,new ArrayList<Double>());

                    loadedModel = svm.svm_load_model(new BufferedReader(new FileReader(path + "/modelSpeaker" + String.valueOf(speaker) + ".txt")));
                    matlabModelToAndroidModel(loadedModel);


                    //scaledTestData = scaleTestData(testData,speaker,storeDir);

                    for (int i = 0; i < numberOfFramesPerSpeaker; i++) {

                        res = svm.svm_predict(loadedModel,testData[i]);
                        resultsList.get(j).add(i, res);
                    }
                }


                int speaker = - 1;
                int frequencyForList = 0;

                ArrayList<Integer> frequencies = new ArrayList<>();

                for(int i = 0; i< resultsList.size(); i++){

                    frequencyForList = Collections.frequency(resultsList.get(i), new Double(1));

                    frequencies.add(i,frequencyForList);

                }

                ArrayList<String> names = new ArrayList<>();
                names.add(0, "Speaker One MB");
                names.add(1,"Speaker Two MJ");
                names.add(2,"Speaker Three MT");

                ArrayList<Double> percentages = new ArrayList<>();
                percentages.add(0,0.0);
                percentages.add(1,0.7);
                percentages.add(2,0.9);


                ArrayList<Double> relativeFrequencies = new ArrayList<>();
                ArrayList<Double> relativeFrequenciesCopy = new ArrayList<>();

                for(int i=0; i< numberOfTrainingSpeakers; i++)
                {
                    relativeFrequencies.add(i,frequencies.get(i) - percentages.get(i)*numberOfFramesPerSpeaker);
                    relativeFrequenciesCopy.add(i,frequencies.get(i)- percentages.get(i)*numberOfFramesPerSpeaker);
                }

                Collections.sort(relativeFrequenciesCopy,Collections.<Double>reverseOrder());

                /**/

                String recognizedSpeaker = "Unknown" + ", not speaker" + String.valueOf(relativeFrequencies.indexOf(relativeFrequenciesCopy.get(0)) + 1) + " for " + String.valueOf(-relativeFrequenciesCopy.get(0)) + " frames";

                for(int j = 0; j< numberOfTrainingSpeakers; j++)
                {
                    speaker = relativeFrequencies.indexOf(relativeFrequenciesCopy.get(j)) + 1;

                    if(frequencies.get(j) > percentages.get(speaker -1)*numberOfFramesPerSpeaker)
                    {
                        recognizedSpeaker = names.get(speaker - 1) + " for " + String.valueOf(relativeFrequenciesCopy.get(j)) + " frames";
                        break;
                    }

                }


                return recognizedSpeaker;


            }
            catch (IOException exception) {
                Log.e("Speaker Recognition", "Error while loading models");
                return null;
            }

    }



    @Override
    protected void onPostExecute(String recognizedSpeaker) {
        super.onPostExecute(recognizedSpeaker);

        Toast.makeText(context,recognizedSpeaker,Toast.LENGTH_LONG).show();
    }
}
