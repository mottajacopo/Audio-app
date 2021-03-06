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
import  static com.example.motta.recorderspeakerspeech.SupportFunctions.uniteAllFeaturesInOneList;
import  static com.example.motta.recorderspeakerspeech.SupportFunctions.saveWavFile;

public class SpeakerRecognition extends AsyncTask<String,Void,String> {
    private final String TAG = "Rec";
    private final double frameLenght = 0.02;
    private Context context;

    private int recordingLenghtInSec;
    private int Fs; //freq di campionamento
    private int nSamples;
    private int nSamplesPerFrame;
    private int nSamplesAlreadyProcessed = 0;

    private short[] audioData; //java codifica i campioni audio in degli short 16 bit
    private AudioRecord record;

    private boolean trainingOrTesting = false;
    private String AutorizedSpeaker;
    private int accuracy ;

    public SpeakerRecognition(Context _context, int _recordingLenghtInSec, int _Fs)
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

        Toast.makeText(context,"Start Recording", Toast.LENGTH_SHORT).show();
    }

    @Override
    protected String doInBackground(String... strings) { //gli ingressi sono quelli passati quando faccio async.Execute//le passo sia il nome della cartella in cui salvare il file e il nome del file --> possono anche essere passati direttamente al costruttore

/////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
//verifica della presenza della directory ed eventuale creazione
/////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

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

/////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
//salva file .wav  (usato poi per il riconoscimento della frase)
/////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

        saveWavFile(Fs , nSamples , audioData , _fileName2 , storeDir);

/////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
//framing del segnale audio (20ms)
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
//estrazione delle features (mfcc + deltadelta)
/////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
        ArrayList<double[]> cepCoeffPerFrame = new ArrayList<double[]>();
        ArrayList<double[]> deltadelta;

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
//caso training SVM
/////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

        if(trainingOrTesting) {

            printFeaturesOnFile(cepCoeffPerFrame, deltadelta, fileDir);//crea il file che va in ingresso alla svm per il training
        }

/////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
//caso testing SVM  (generazione del modello sulla base delle features dei parlatori autorizzati e fase di predizione del nuovo parlatore)
/////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

        else {

            int numberOfTrainingSpeakers = 2;
            int totalNumberOfFeatures = 2 * (cepCoeffPerFrame.get(0).length);
            int numberOfFramesPerSpeaker = cepCoeffPerFrame.size();
            int totalNumberOfFrames = numberOfFramesPerSpeaker * numberOfTrainingSpeakers;

            double[] labels = new double[totalNumberOfFrames];

////////////////////////////////////////////////////////////////////////////////////////////////////
//generazione degli svm node dei parlatori autorizzati (SVM_NODE)

            svm_node[][] dataToSvm = new svm_node[totalNumberOfFrames][totalNumberOfFeatures + 1];

            try {
                FileInputStream fileInputStream = new FileInputStream(fileDir);
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

////////////////////////////////////////////////////////////////////////////////////////////////////
//generazione del problem (SMV_PROBLEM)

                svm_problem problem = new svm_problem();
                problem.x = dataToSvm;
                problem.y = labels;
                problem.l = labels.length;

////////////////////////////////////////////////////////////////////////////////////////////////////
//settaggio dei parametri della SVM (SVM_PARAMETER)

                svm_parameter parameters = new svm_parameter();
                parameters.kernel_type = 2; //tipi di kernel della SVM (0-lineare-1-polinomiale-2-gaussiano)
                parameters.gamma = 0.03; // gamma tipicamente vale 1/(numero di features) ; nostro caso 1/26
                parameters.C = 5;
                parameters.svm_type = 1;
                parameters.nu = 0.03;
                parameters.eps = Math.pow(10,-8);


////////////////////////////////////////////////////////////////////////////////////////////////////
//generazione del modello (SVM_MODEL)

                svm_model model;
                model = svm.svm_train(problem, parameters);// generazione del modello
                //model = svm.svm_load_model(new BufferedReader(new FileReader(storeDir + "/model.txt")));
                //svm.svm_save_model("model_1.txt" , model);
////////////////////////////////////////////////////////////////////////////////////////////////////
//generazione degli svm node del parlatore da riconoscere (SVM_NODE)


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

/*
                svm_node[][] testData = new svm_node[500][3];
                String[] splitted = new String[2];

                FileReader readTest = new FileReader(storeDir + "/test_data_final.txt");
                BufferedReader bufferReader = new BufferedReader(readTest);

                for(int i=0 ; i<500; i++){

                    splitted = bufferReader.readLine().split(",");
                    for(int j=0;j<2;j++){

                        svm_node test_node = new svm_node();

                        test_node.index = j;
                        test_node.value = Double.parseDouble(splitted[j]);
                        testData[i][j] = test_node;
                    }

                    svm_node final_node = new svm_node();
                    final_node.index = -1;
                    final_node.value = 0;
                    testData[i][2] = final_node;

                }
*/

////////////////////////////////////////////////////////////////////////////////////////////////////
//fase di predizione (SVM_PREDICT)

                int frequency;
                int mostFrequency = 0;
                double mostFrequentValue = 0;

                ArrayMap<Double, String> speakers = new ArrayMap<>(numberOfTrainingSpeakers);
                speakers.put(Double.valueOf(1),"Speaker One");
                speakers.put(Double.valueOf(2),"Speaker Two");

                ArrayList<Double> results = new ArrayList<>();
                double res;

                for (int i = 0; i < numberOfFramesPerSpeaker; i++) {

                    res = svm.svm_predict(model, testData[i]);
                    results.add(i,res);
                }
////////////////////////////////////////////////////////////////////////////////////////////////////
//controllo quale label dei parlatori consciuti è quello con maggiore ricorrenza dopo la predizione

                for (int j = 0; j < results.size(); j++) {

                    frequency = Collections.frequency(results, results.get(j));

                    if (frequency >= mostFrequency) {
                        mostFrequency = frequency;
                        mostFrequentValue = results.get(j);
                        accuracy = (frequency*100)/numberOfFramesPerSpeaker;
                    }
                }
////////////////////////////////////////////////////////////////////////////////////////////////////
//restituisco il label riconosciuto
                String recognizedSpeaker = speakers.get(mostFrequentValue);
                AutorizedSpeaker = recognizedSpeaker;
                return  recognizedSpeaker;

            }
            catch (IOException exception) {
                Log.e("Read from trainingFile", "Error while reading from trainingFile");
            }
        }
        return null;
    }

    @Override
    protected void onPostExecute(String string) {
        super.onPostExecute(string);
        //Toast.makeText(context,"Ended Recording",Toast.LENGTH_SHORT).show();
        Toast.makeText(context,AutorizedSpeaker + " " + accuracy + "%",Toast.LENGTH_SHORT).show();
        Toast.makeText(context, string, Toast.LENGTH_SHORT);
    }

}
