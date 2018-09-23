package com.example.motta.recorderspeakerspeech;

import android.util.Log;

import com.ibm.watson.developer_cloud.speech_to_text.v1.model.SpeechRecognitionAlternative;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.EOFException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import libsvm.svm_node;
import libsvm.svm_model;


public class SupportFunctions {

    public static ArrayList<double[]> computeDeltas (ArrayList<double[]> mfccCoeff, int n)
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

    public static ArrayList<double[]> uniteAllFeaturesInOneList (ArrayList<double[]> mfcc, ArrayList<double[]> deltadelta)
    {
        final int mfccCoeffNumber = mfcc.get(0).length;

        double[] temp = new double[2*mfccCoeffNumber];
        ArrayList<double[]> union = new ArrayList<double[]>(); //nuova lista dove ogni elemento sarà un array di double a 26 elementi per contenere sia gli mfcc che i delta

        for(int j=0; j<mfcc.size(); j++){

            for(int k=0; k< mfccCoeffNumber*2; k++){//riempio la lista completa di tutte le features
                if(k < mfccCoeffNumber){//i primi 13 elementi saranno gli mfcc
                    temp[k] = mfcc.get(j)[k];
                }
                else{//i secondi 13 saranno i delta-delta
                    temp[k] = deltadelta.get(j)[k - mfccCoeffNumber];
                }
            }


            union.add(temp.clone());
        }

        return union;
    }

    public static svm_node[][] readTestDataFromFormatFile(String fileDir,int framesPerSpeaker,int totalNumberOfFeatures)//legge i file txt che sono nel formato apposito per la libsvm
    {
        String line = null;
        String[] splittedLine = null;
        svm_node[][] result = new svm_node[framesPerSpeaker][totalNumberOfFeatures + 1];
        try{
            BufferedReader bufferedReader = new BufferedReader(new FileReader(fileDir));//apri il file txt

            for(int j=0; j<framesPerSpeaker; j++ ) {//per ogni frame nella registrazione di test

                line = bufferedReader.readLine();//leggo una linea del txt corrispondente alle prime 26 features
                line = line.substring(line.indexOf(' ') + 1);
                splittedLine = line.split(" ");//ottengo i singoli termini del tipo index:value

                for (int i = 0; i < splittedLine.length; i++) {//per ogni termine della forma index:value

                    svm_node node = new svm_node();//creo il nuovo nodo
                    node.index = i;
                    node.value = Double.parseDouble(splittedLine[i].split(":")[1]);//separo il valore dall indice
                    result[j][i] = node;//inserisco il nuovo nodo
                }

                svm_node finalNode = new svm_node();//creo il nodo finale
                finalNode.index = -1;
                finalNode.value = 0;
                result[j][totalNumberOfFeatures] = finalNode;//aggiungo il nodo finale
            }

            return result;



        }
        catch(IOException exception)
        {

            return null;
        }

    }

    public static svm_node[][] scaleTestData(svm_node[][] testDataToBeScaled,int speaker,String storeDir,int typeOfSvm)//1 for multi 0 for oneClass -> one class and multi class training has different normVals
    {
        String fileSelected = null;

        if(typeOfSvm == 1)//caso multiClass
        {
            fileSelected = "/normValuesMulti.txt";
        }
        else//caso oneClass
        {
            fileSelected = "/normValuesOne.txt";
        }

        String fileDir = storeDir + fileSelected;//creo il percorso del file (diverso a seconda che si tratti di multi o one class)

        int numberOfFeatures = testDataToBeScaled[0].length -1;
        int numberOfFrames = testDataToBeScaled.length;
        double[][] normValues = new double[2][numberOfFeatures];

        svm_node[][] scaledData = new svm_node[numberOfFrames][numberOfFeatures + 1];

        for(int i = 0; i< numberOfFrames; i++)
        {
            scaledData[i][numberOfFeatures] = testDataToBeScaled[i][numberOfFeatures]; //copia i nodi finali che non vanno scalati
        }

        normValues = readNormValFromFile(fileDir,speaker,numberOfFeatures);//carico i valori di normalizzazione dal file apposito

        for(int i=0; i< numberOfFeatures; i++)
        {
            for(int j = 0; j< numberOfFrames; j++)
            {

                svm_node node = new svm_node();
                node.index = i;
                node.value = ((testDataToBeScaled[j][i].value - normValues[1][i])/(normValues[0][i] - normValues[1][i])*2 -1);//scalo con i valori di norm. caricati
                scaledData[j][i] = node;
            }
        }

        return scaledData;
    }

    private static double[][] readNormValFromFile(String fileDir,int speaker,int numberOfFeatures)
    {
        double[][] normVal = new double[2][numberOfFeatures];

        try{

            BufferedReader reader = new BufferedReader(new FileReader(fileDir));
            String[] splittedLine;//leggo ogni riga del file con i valori di normalizzazione

            String line = "";
            int redSpeaker = -1;
            String[] maxAndMin;
            double maxValue;
            double minValue;

            while (redSpeaker != speaker || line == null)//fino a che la riga non è quella dello speaker cercato o fino a che non ho considerato tutte le righe
            {
              line = reader.readLine();//leggo la riga
              redSpeaker = Integer.parseInt(line.substring(0,line.indexOf(" ")));//estraggo l'informazione relativa allo speaker a cui si riferiscono quei valori di normalizz.
            }

            line = line.substring(line.indexOf(" ") + 1);
            splittedLine = line.split(" ");//trova ogni coppia maxVal:minVal per ogni feature

            for(int i =0; i< splittedLine.length; i++)//per ogni coppia maxVal:minVal
            {
                maxAndMin = splittedLine[i].split(":");
                maxValue = Double.parseDouble(maxAndMin[0]);//separo il primo elemento
                minValue = Double.parseDouble(maxAndMin[1]);//separo il secondo elemento

                normVal[0][i] = maxValue;
                normVal[1][i] = minValue;
            }

        }
        catch (IOException exception)
        {
            Log.e("readNormValFromFile","Error while opening normVal file");
        }

        return  normVal;
    }

    public static void matlabModelToAndroidModel(svm_model matlabModel)//converte il modello generato da matlab in modello utilizzabile su android
                                                                       //riportando gli indici da 0 a -1 e inserendo negli SVs i nodi finali
    {
            int totalNumberOfFeatures = matlabModel.SV[0].length;

            svm_node[][] newSV = new svm_node[matlabModel.SV.length][totalNumberOfFeatures +1];//creo una nuova matrice di nodi che possa contenere anche il nodo finale

            for(int j = 0; j< matlabModel.SV.length; j++)//per ogni array di nodi in SV
            {
                for(int k = 0; k < totalNumberOfFeatures; k++)//per ogni nodo
                {
                    svm_node node = new svm_node();//creo un nuovo nodo
                    node.index = matlabModel.SV[j][k].index - 1;//gli associo un indice uguale a quello del nodo del vecchio modello
                    node.value = matlabModel.SV[j][k].value;//gli associo lo stesso valore del nodo del vecchio modello
                    newSV[j][k] = node;//aggiungo il nodo nella nuova matrice di SVs
                }

                svm_node finalNode = new svm_node();
                finalNode.index = -1;
                finalNode.value = 0;
                newSV[j][totalNumberOfFeatures] = finalNode;//aggiungo il nodo finale
            }

            matlabModel.SV = newSV;//sostituisco i vecchi SVs con quelli appena creati
    }

    public static boolean verifyPhrase(List<SpeechRecognitionAlternative> alternatives, String expectedPhrase)//verifica se la frase riconosciuta può essere accettata
    {

        String[] expectedWords = expectedPhrase.split(" ");
        ArrayList<String> expectedWordsList = new ArrayList<String>(Arrays.asList(expectedWords));//trovo le singole parole della frase corretta

        int totalWords = expectedWords.length;
        int count = 0;

        String recognizedPhrase = null;
        String transcript = null;
        ArrayList<String> wordsArray = null;

        boolean result = false;


        for(int i = 0; i< alternatives.size(); i++)//per ciascuna delle alternative riconosciute dalla STT
        {
            transcript = alternatives.get(i).getTranscript();
            recognizedPhrase = transcript.substring(0,transcript.length()-1);//ottengo la frase riconosciuta


            wordsArray = new ArrayList<String>(Arrays.asList(recognizedPhrase.split(" ")));//trovo le singole parole della frase riconosciuta


            for(int k = 0; k< expectedWordsList.size(); k++)//per ogni parola della frase originale
            {
                if(wordsArray.contains(expectedWordsList.get(k)))//se la frase riconosciuta la contiene
                {
                    wordsArray.remove(expectedWordsList.get(k));//la rimuovo dalle parole della frase originale
                    count++;//aumento il conteggio delle corrispondenze tra le due frasi
                }

            }

            if(((double)count)/totalWords >= 0.75 )//se almeno 6 su 8 delle parole totali della frase riconosciuta corrispondono
            {
                result = true;//la frase riconosciuta è considerata come corretta
                break;
            }

            count = 0;

        }

        return result;

    }

    public static String recognizedPhrase(List<SpeechRecognitionAlternative> alternatives, String expectedPhrase, boolean recognitionResult)//ritorna la frase riconosciuta
    {
        ArrayList<String> expectedWordsList= new ArrayList<>(Arrays.asList(expectedPhrase.split(" ")));//lista di parole della frase originale
        int totalWords = expectedWordsList.size();
        int count = 0;

        String transcript = null;
        String recognizedPhrase = null;
        ArrayList<String> wordsArray = null;

        if(recognitionResult)//recupero la frase che era stata riconosciuta come corretta (la prima delle alternative che soddisfa la condizione)
        {
            for(int i = 0; i< alternatives.size(); i++)//per ogni alternativa
            {
                transcript = alternatives.get(i).getTranscript();
                recognizedPhrase = transcript.substring(0,transcript.length()-1);//trovo la frase riconosciuta

                wordsArray = new ArrayList<String>(Arrays.asList(recognizedPhrase.split(" ")));//trovo le parole della frase riconosciuta

                for(int k = 0; k< expectedWordsList.size(); k++)//per ogni parola della frase originale
                {
                    if(wordsArray.contains(expectedWordsList.get(k)))//se è contenuta nella frase riconosciuta
                    {
                        wordsArray.remove(expectedWordsList.get(k));//la rimuovo dalla lista delle parole della frase originale
                        count++;//aumento il conteggio delle corrispondenze
                    }

                }

                if(count/totalWords >= 0.75 )//se la condizione è soddisfatta
                {
                    break;//ho trovato la frase che cercavo -> posso uscire dal ciclo
                }

                count = 0;
            }
        }
        else//se nessuna frase era stata riconosciuta come corretta ritorno quella con la maggiore probabilità
        {
            double maxConfidence = 0;
            int index = 0;

            for(int i = 0; i< alternatives.size(); i++)
            {
                if(alternatives.get(i).getConfidence() > maxConfidence)//se la confidence dell'alternativa attuale è maggiore della confidenza di tutte le precedenti
                {
                    maxConfidence = alternatives.get(i).getConfidence();//setto la nuova confidenza massima
                    index = i;//salvo l'indice dell'alternativa a maggiore confidenza
                }
            }

            transcript = alternatives.get(index).getTranscript();
            recognizedPhrase = transcript.substring(0,transcript.length()-1);//trovo la frase relativa all'alternativa a massima confidenza

        }

        return recognizedPhrase;
    }
    }

