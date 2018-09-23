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
import static com.example.motta.recorderspeakerspeech.SupportFunctions.readTestDataFromFormatFile;
import static com.example.motta.recorderspeakerspeech.SupportFunctions.scaleTestData;
import static com.example.motta.recorderspeakerspeech.SupportFunctions.uniteAllFeaturesInOneList;


public class SpeakerRecog extends AsyncTask<String,Void,String> {

    private static final String TAG = "SPEAKERREC";
    private final double frameLength = 0.02;// lunghezza frame in secondi
    private final double overlapPercentage = 0.5;//percentuale sovrapp. dei frame

    private Context context = null;
    private short[] samples = null;//campioni letti dal file .wav
    private int Fs = 0;
    private int recordingLengthInSec = 0;
    private int nSamples = 0;
    private int nSamplesPerFrame = 0;
    private TextView textView = null;

    public SpeakerRecog(Context _context, int _Fs, int _recordingLengthInSec, TextView _textView)
    {
        context = _context;
        Fs = _Fs;
        recordingLengthInSec = _recordingLengthInSec;

        nSamples =  recordingLengthInSec*Fs;
        nSamplesPerFrame = (int)(frameLength*Fs);

        samples = new short[nSamples];

        textView = _textView;
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

        try {//provo a leggere il file .wav

            boolean success = readWav.read();

            if (!success) {//se la lettura non va a buon fine lnacio un'eccezione
                throw new IOException("Error while reading .wav file");
            }

        }
        catch (IOException exception){
            Log.e(TAG,exception.getMessage());
            return null;//se è stata generata l'eccezione esco dal doInBackground
        }


        short value = 0;

        for(int i = 0;  i < nSamples; i++)//ricostruisco i campioni originali a partire dai byte salvati nel file .wav
        {

            value = (short) (((short) readWav.myData[2*i] & 0x00ff) | ((short)readWav.myData[(2*i)+1]<<8 & 0xff00));//ricostruisco il campione a 16 bit unendo i primi 2 byte di myData

            samples[i] = value;
        }


        int nSamplesAlreadyProcessed = 0;
        int nSamplesOverlapped = (int)(overlapPercentage*nSamplesPerFrame);

//////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
//framing audio data
/////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

        ArrayList<float[]> floatSamplesPerFrame = new ArrayList<>();

        while (nSamples - nSamplesAlreadyProcessed >= nSamplesPerFrame-nSamplesOverlapped) {//dato l'overlapping per ogni frame avanzo di nSamplesPerFrame-nSamplesOverlapped frames

            float[] frame = new float[nSamplesPerFrame];

            for (int i = 0; i < nSamplesPerFrame; i++) {//riempio il nuovo frame


                frame[i] = samples[i + nSamplesAlreadyProcessed -(nSamplesOverlapped*(nSamplesAlreadyProcessed/nSamplesPerFrame))];//la sovrapposizione è considerata solo dopo il primo frame
                                                                                                                                   // -> per il primo frame non posso considerare campioni da quello precedente
            }

            floatSamplesPerFrame.add(frame);

            if(nSamplesAlreadyProcessed == 0) {//se sono al primo frame

                nSamplesAlreadyProcessed = nSamplesPerFrame;//i nuovi campioni inseriti nel frame sono nSamplesPerFrame
            }
            else {//se non sono nel primo frame ho overlapping con i campioni del frame precedente
                nSamplesAlreadyProcessed += nSamplesPerFrame-nSamplesOverlapped;//i nuovi campioni considerati sono solo nSamplesPerFrame - nSamplesOverlapped
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


                try{


                ArrayList<double[]> union = uniteAllFeaturesInOneList(cepCoeffPerFrame, deltadelta);//unisco mfcc e delta-delta per un totale di 26 features
                svm_node[][] testData = new svm_node[numberOfFramesPerSpeaker][totalNumberOfFeatures + 1];//converto i dati di test in un array di svm_node
                svm_node[][] scaledTestData = null;

                for (int i = 0; i < numberOfFramesPerSpeaker; i++) {//riempio la matrice degli svm_node con i valori delle features

                    for (int j = 0; j < totalNumberOfFeatures; j++) {

                        svm_node node = new svm_node();
                        node.index = j;
                        node.value = union.get(i)[j];

                        testData[i][j] = node;//aggiungo singolo nodo
                    }

                    svm_node finalNode = new svm_node();
                    finalNode.index = -1;
                    finalNode.value = 0;

                    testData[i][totalNumberOfFeatures] = finalNode;//aggiungo nodo finale
                }

                ArrayList<ArrayList<Double>> resultsList = new ArrayList<>();//lista con tante liste quanti speakers
                                                                             // -> ciascuna lista contiene il risultato
                                                                             //del predict dell i-esimo frame di test

                double res;
                svm_model loadedModel;

                for(int j = 0; j< numberOfTrainingSpeakers; j++) {//per ogni parlatore ammesso

                    int speaker = j + 1;
                    resultsList.add(j,new ArrayList<Double>());//aggiungo una nuova lista per i risultati del predict con il suo specifico modello

                    loadedModel = svm.svm_load_model(new BufferedReader(new FileReader(path + "/modelMultiSpeaker" + String.valueOf(speaker) + ".txt")));//carico il suo modello
                    matlabModelToAndroidModel(loadedModel);//conversione modello caricato in un formato adatto per android

                    scaledTestData = scaleTestData(testData,speaker,path,1);//scalo i dati di test con i valori di scalamento del suo modello

                    for (int i = 0; i < numberOfFramesPerSpeaker; i++) {//per ogni frame di test scalato

                        res = svm.svm_predict(loadedModel,scaledTestData[i]);//predizione tramite svm
                        resultsList.get(j).add(i, res);//aggiungo predizione alla lista dei risultati
                    }
                }


                int speaker = - 1;
                int frequencyForList = 0;

                ArrayList<Integer> frequencies = new ArrayList<>();//lista contenente il numero di corrispondenze dei frame per ogni modello

                for(int i = 0; i< resultsList.size(); i++){//calcolo il numero di uni (corrispondenze) presenti nella lista risultato

                    frequencyForList = Collections.frequency(resultsList.get(i), new Double(1));

                    frequencies.add(i,frequencyForList);

                }

                ArrayList<String> names = new ArrayList<>();//nomi dei parlatori ammessi da stampare a schermo
                names.add(0, "Speaker One MB");
                names.add(1,"Speaker Two MJ");
                names.add(2,"Speaker Three MT");

                ArrayList<Double> percentages = new ArrayList<>();//percentuali di decisione per ogni speaker
                percentages.add(0,0.8);
                percentages.add(1,0.8);
                percentages.add(2,0.9);


                ArrayList<Double> relativeFrequencies = new ArrayList<>();//numero di corrispondenze oltre la soglia di decisione per ogni speaker
                ArrayList<Double> relativeFrequenciesCopy = new ArrayList<>();

                for(int i=0; i< numberOfTrainingSpeakers; i++)
                {
                    relativeFrequencies.add(i,frequencies.get(i) - percentages.get(i)*numberOfFramesPerSpeaker);
                    relativeFrequenciesCopy.add(i,frequencies.get(i)- percentages.get(i)*numberOfFramesPerSpeaker);
                }

                Collections.sort(relativeFrequenciesCopy,Collections.<Double>reverseOrder());//riordino la lista sulla base del maggior numero di corrispondenze oltre la soglia

                /**/

                String recognizedSpeaker = "Unknown" + ", not speaker" + String.valueOf(relativeFrequencies.indexOf(relativeFrequenciesCopy.get(0)) + 1) + " for " + String.valueOf(-relativeFrequenciesCopy.get(0)) + " frames in multi";

                int recogSpeaker = -1;

                for(int j = 0; j< numberOfTrainingSpeakers; j++)//partendo dallo speaker con più corrispondenze rispetto alla soglia
                                                                //verifico se il numero delle corrispondenze è sufficiente
                {
                    speaker = relativeFrequencies.indexOf(relativeFrequenciesCopy.get(j)) + 1;

                    if(frequencies.get(speaker-1/*j*/) > percentages.get(speaker -1)*numberOfFramesPerSpeaker)
                    {
                        recogSpeaker = speaker/*j + 1*/;
                        break;
                    }

                }


                if(recogSpeaker != -1)//se uno degli speaker è stato riconosciuto dalla svm multi class lo testo anche con il one class
                {
                    svm_model oneClassModel = svm.svm_load_model(path + "/modelOneSpeaker" + String.valueOf(recogSpeaker) + ".txt");//carico modello one class dello speaker riconosciuto
                    matlabModelToAndroidModel(oneClassModel);

                    double predictedLabel = 0;
                    int count = 0;

                    ArrayList<Double> oneClassThr = new ArrayList<>();//thresholds per il caso one class
                    oneClassThr.add(0, 0.05);
                    oneClassThr.add(1, 0.08);


                    scaledTestData = scaleTestData(testData,recogSpeaker,path,0);//scalo i dati di test questa volta
                                                                                           //con i valori di normalizzazione usati per la
                                                                                           //creazione del modello one class

                    for (int i = 0; i < numberOfFramesPerSpeaker; i++) {
                        predictedLabel = svm.svm_predict(oneClassModel, scaledTestData[i]);
                        if (predictedLabel == 1) {
                            count++;//conto le corrispondenze trovate
                        }
                    }

                    if (((double) count) / numberOfFramesPerSpeaker >= oneClassThr.get(recogSpeaker - 1)) {//se il numero supera la soglia definita per
                                                                                                           //il modello one class allora si tratta effettivamente
                                                                                                           //di quel parlatore
                        recognizedSpeaker = names.get(recogSpeaker-1) + "for " + String.valueOf(relativeFrequencies.get(recogSpeaker-1) + " frames in multi and " + String.valueOf(count-oneClassThr.get(recogSpeaker-1)*numberOfFramesPerSpeaker) + " frames in oneC");
                    }
                    else
                    {
                        recognizedSpeaker = "Unknown, not speaker " + String.valueOf(recogSpeaker) + " for " + String.valueOf(oneClassThr.get(recogSpeaker-1)*numberOfFramesPerSpeaker-count) + " frames in oneC";
                    }
                }

                return recognizedSpeaker;


            }
            catch (IOException exception) {
                Log.e(TAG, "Error while loading models");
                return null;
            }

    }



    @Override
    protected void onPostExecute(String recognizedSpeaker) {
        super.onPostExecute(recognizedSpeaker);

        textView.setText(recognizedSpeaker);//stampo il nome del parlatore riconosciuto
    }
}
