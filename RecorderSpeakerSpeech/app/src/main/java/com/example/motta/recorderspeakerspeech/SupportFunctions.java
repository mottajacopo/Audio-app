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

    public static void printFeaturesOnFile (ArrayList<double[]> mfcc, ArrayList<double[]> deltadelta, String _fileDir,int speaker)
    {

        //final String label = "2"; //label che va cambiato ad ogni registrazione di un parlatore diverso

        final double label = (double) speaker;

        ArrayList<double[]> union = uniteAllFeaturesInOneList(mfcc,deltadelta);

        int totalNumberOfFeatures = union.get(0).length;

        /*DecimalFormatSymbols symbol = new DecimalFormatSymbols();
        symbol.setDecimalSeparator('.');
        DecimalFormat format = new DecimalFormat("#.0000000",symbol);
        float value;
        String stringValue;
*/

        try
        {
           /* FileWriter writeOnTrainingFile = new FileWriter(_fileDir,true);

            for(int b=0; b < union.size(); b++){//per ogni vettore di features da 26 elementi

                writeOnTrainingFile.write(label + " ");

                for(int i=0; i< totalNumberOfFeatures; i++){


                    //writeOnTrainingFile.write( Integer.toString(i+1) + ":" + Double.toString(union.get(b)[i]) + " ");
                    //writeOnTrainingFile.write( Integer.toString(i+1) + ":" + format.format(union.get(b)[i]) + " ");

                    writeOnTrainingFile.write( Integer.toString(i+1) + ":" + Float.toString((float)union.get(b)[i]) + " ");


                }

                writeOnTrainingFile.write("\n");
            }

            writeOnTrainingFile.flush();
            writeOnTrainingFile.close();

*/
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

    public static void printFeaturesOnFileFormat (ArrayList<double[]> mfcc, ArrayList<double[]> deltadelta, String _fileDir, int speaker)
    {


        final String label = String.valueOf(speaker); //label che va cambiato ad ogni registrazione di un parlatore diverso

        ArrayList<double[]> union = uniteAllFeaturesInOneList(mfcc,deltadelta);

        int totalNumberOfFeatures = union.get(0).length;

        /*DecimalFormatSymbols symbol = new DecimalFormatSymbols();
        symbol.setDecimalSeparator('.');
        DecimalFormat format = new DecimalFormat("#.0000000",symbol);
        float value;
        String stringValue;
*/

        try
        {
            FileWriter writeOnTrainingFile = new FileWriter(_fileDir,true);

            for(int b=0; b < union.size(); b++){//per ogni vettore di features da 26 elementi

                writeOnTrainingFile.write(label + " ");

                for(int i=0; i< totalNumberOfFeatures; i++){


                    //writeOnTrainingFile.write( Integer.toString(i+1) + ":" + Double.toString(union.get(b)[i]) + " ");
                    //writeOnTrainingFile.write( Integer.toString(i+1) + ":" + format.format(union.get(b)[i]) + " ");

                    writeOnTrainingFile.write( Integer.toString(i+1) + ":" + Float.toString((float)union.get(b)[i]) + " ");


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

    public static double[][] ReadSVsCoeff (String SVsCoeffFileName) {
        String content = null;
        String[] splittedContent = null;
        ArrayList<Double> listedResult = new ArrayList<>();

        try {

            FileReader fileReader = new FileReader(SVsCoeffFileName);
            try {
                while (true) {
                    content += fileReader.read();
                }
            } catch (EOFException eofReached) {
                splittedContent = content.split(",");

                for (int i = 0; i < splittedContent.length; i++) {

                    listedResult.add(i, Double.parseDouble(splittedContent[i]));
                }

                double[][] result = new double[listedResult.size()][0];

                for (int i = 0; i < listedResult.size(); i++) {

                    result[i][0] = listedResult.get(i);

                }

                return result;
            }

        } catch (IOException exception) {

            return null;

        }
    }
    public static svm_node[][] readTestDataFromFormatFile(String fileDir,int framesPerSpeaker,int totalNumberOfFeatures)
    {
        String line = null;
        String[] splittedLine = null;
        svm_node[][] result = new svm_node[framesPerSpeaker][totalNumberOfFeatures + 1];
        try{
            BufferedReader bufferedReader = new BufferedReader(new FileReader(fileDir));

            for(int j=0; j<framesPerSpeaker; j++ ) {

                line = bufferedReader.readLine();
                line = line.substring(line.indexOf(' ') + 1);
                splittedLine = line.split(" ");

                for (int i = 0; i < splittedLine.length; i++) {

                    svm_node node = new svm_node();
                    node.index = i;
                    node.value = Double.parseDouble(splittedLine[i].split(":")[1]);
                    result[j][i] = node;
                }

                svm_node finalNode = new svm_node();
                finalNode.index = -1;
                finalNode.value = 0;
                result[j][totalNumberOfFeatures] = finalNode;
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

        if(typeOfSvm == 1)
        {
            fileSelected = "/normValuesMulti.txt";
        }
        else
        {
            fileSelected = "/normValuesOne.txt";
        }

        String fileDir = storeDir + fileSelected;

        int numberOfFeatures = testDataToBeScaled[0].length -1;
        int numberOfFrames = testDataToBeScaled.length;
        double[][] normValues = new double[2][numberOfFeatures];

        svm_node[][] scaledData = new svm_node[numberOfFrames][numberOfFeatures + 1];

        for(int i = 0; i< numberOfFrames; i++)
        {
            scaledData[i][numberOfFeatures] = testDataToBeScaled[i][numberOfFeatures]; //copia i nodi finali che non vanno scalati
        }

        normValues = readNormValFromFile(fileDir,speaker,numberOfFeatures);

        for(int i=0; i< numberOfFeatures; i++)
        {
            for(int j = 0; j< numberOfFrames; j++)
            {
                //scaledData[j][i].index = i;
                //scaledData[j][i].value = (testDataToBeScaled[j][i].value - normValues[1][i])/(normValues[0][i] - normValues[1][i]);

                svm_node node = new svm_node();
                node.index = i;
                node.value = ((testDataToBeScaled[j][i].value - normValues[1][i])/(normValues[0][i] - normValues[1][i])*2 -1);
                scaledData[j][i] = node;
            }
        }

        return scaledData;
    }

    public static svm_node[][] scaleTrainingData(svm_node[][] trainingDataToBeScaled,String storeDir,int speaker, int typeOfSvm)
    {
        String fileSelected = null;

        if(typeOfSvm == 1)
        {
            fileSelected = "/normValuesMulti.txt";
        }
        else
        {
            fileSelected = "/normValuesOne.txt";
        }

        String normValFileDir = storeDir + fileSelected;

        int numberOfFeatures = trainingDataToBeScaled[0].length -1;
        int numberOfFrames = trainingDataToBeScaled.length;
        double[][] normValues = new double[2][numberOfFeatures];

        svm_node[][] scaledData = new svm_node[numberOfFrames][numberOfFeatures + 1];

        for(int i = 0; i< numberOfFrames; i++)
        {
            scaledData[i][numberOfFeatures] = trainingDataToBeScaled[i][numberOfFeatures]; //copia i nodi finali che non vanno scalati
        }

        ArrayList<Double> values = new ArrayList<>(numberOfFrames);
        double maxValue;
        double minValue;

        for(int i=0;i<numberOfFeatures;i++)
        {
            values.clear();

            for (int j=0;j<numberOfFrames;j++)
            {
                values.add(j,trainingDataToBeScaled[j][i].value);
            }

            maxValue = Collections.max(values);
            minValue = Collections.min(values);

            normValues[0][i] = maxValue;
            normValues[1][i] = minValue;

            for(int k =0; k< numberOfFrames;k++)
            {
                //scaledData[k][i].index = i;
                //scaledData[k][i].value = (trainingDataToBeScaled[k][i].value - minValue) / (maxValue - minValue);

                svm_node node = new svm_node();
                node.index = i;
                node.value = ((trainingDataToBeScaled[k][i].value - minValue)/(maxValue-minValue))*2 - 1;
                scaledData[k][i] = node;
            }
        }

        printNormValOnFile(normValFileDir,speaker,normValues);
        return scaledData;
    }

    private static void printNormValOnFile(String fileDir,int speaker,double[][] normValues)
    {

        int numberOfFeatures = normValues[0].length;

        try {

            FileWriter writer = new FileWriter(fileDir,true);

            writer.write(String.valueOf(speaker) + " ");

            for(int i=0; i< numberOfFeatures; i++)
            {
                writer.write(String.valueOf((float)normValues[0][i]) + ":" + String.valueOf((float)normValues[1][i]) + " ");
            }

            writer.write('\n');

            writer.flush();
            writer.close();
        }
        catch (IOException exception)
        {
            Log.e("printNormValOnFile","Error while opening normVal file");
        }
    }

    private static double[][] readNormValFromFile(String fileDir,int speaker,int numberOfFeatures)
    {
        double[][] normVal = new double[2][numberOfFeatures];

        try{

            BufferedReader reader = new BufferedReader(new FileReader(fileDir));
            String[] splittedLine;

            String line = "";
            int redSpeaker = -1;
            String[] maxAndMin;
            double maxValue;
            double minValue;

            while (redSpeaker != speaker || line == null)
            {
              line = reader.readLine();
              redSpeaker = Integer.parseInt(line.substring(0,line.indexOf(" ")));
            }

            line = line.substring(line.indexOf(" ") + 1);
            splittedLine = line.split(" ");

            for(int i =0; i< splittedLine.length; i++)
            {
                maxAndMin = splittedLine[i].split(":");
                maxValue = Double.parseDouble(maxAndMin[0]);
                minValue = Double.parseDouble(maxAndMin[1]);

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

    public static void matlabModelToAndroidModel(svm_model matlabModel/*matlab model loaded from memory*/)
    {
            int totalNumberOfFeatures = matlabModel.SV[0].length;

            svm_node[][] data = new svm_node[matlabModel.SV.length][totalNumberOfFeatures +1];

            for(int j = 0; j< matlabModel.SV.length; j++)
            {
                for(int k = 0; k < totalNumberOfFeatures; k++)
                {
                    svm_node node = new svm_node();
                    node.index = matlabModel.SV[j][k].index - 1;
                    node.value = matlabModel.SV[j][k].value;
                    data[j][k] = node;
                }

                svm_node finalNode = new svm_node();
                finalNode.index = -1;
                finalNode.value = 0;
                data[j][totalNumberOfFeatures] = finalNode;
            }

            matlabModel.SV = data;
    }

    public static boolean verifyPhrase(List<SpeechRecognitionAlternative> alternatives, String expectedPhrase)
    {

        String[] expectedWords = expectedPhrase.split(" ");
        ArrayList<String> expectedWordsList = new ArrayList<String>(Arrays.asList(expectedWords));

        int totalWords = expectedWords.length;
        int count = 0;

        String recognizedPhrase = null;
        String transcript = null;
        ArrayList<String> wordsArray = null;

        boolean result = false;


        for(int i = 0; i< alternatives.size(); i++)
        {
            transcript = alternatives.get(i).getTranscript();
            recognizedPhrase = transcript.substring(0,transcript.length()-1);


            wordsArray = new ArrayList<String>(Arrays.asList(recognizedPhrase.split(" ")));


            for(int k = 0; k< expectedWordsList.size(); k++)
            {
                if(wordsArray.contains(expectedWordsList.get(k)))
                {
                    wordsArray.remove(expectedWordsList.get(k));
                    count++;
                }

            }

            if(((double)count)/totalWords >= 0.75 )
            {
                result = true;
                break;
            }

            count = 0;

        }

        return result;

    }

    public static String recognizedPhrase(List<SpeechRecognitionAlternative> alternatives, String expectedPhrase, boolean recognitionResult)
    {
        ArrayList<String> expectedWordsList= new ArrayList<>(Arrays.asList(expectedPhrase.split(" ")));
        int totalWords = expectedWordsList.size();
        int count = 0;

        String transcript = null;
        String recognizedPhrase = null;
        ArrayList<String> wordsArray = null;

        if(recognitionResult)
        {
            for(int i = 0; i< alternatives.size(); i++)
            {
                transcript = alternatives.get(i).getTranscript();
                recognizedPhrase = transcript.substring(0,transcript.length()-1);

                wordsArray = new ArrayList<String>(Arrays.asList(recognizedPhrase.split(" ")));

                for(int k = 0; k< expectedWordsList.size(); k++)
                {
                    if(wordsArray.contains(expectedWordsList.get(k)))
                    {
                        wordsArray.remove(expectedWordsList.get(k));
                        count++;
                    }

                }

                if(count/totalWords >= 0.75 )
                {
                    break;
                }

                count = 0;
            }
        }
        else
        {
            double maxConfidence = 0;
            int index = 0;

            for(int i = 0; i< alternatives.size(); i++)
            {
                if(alternatives.get(i).getConfidence() > maxConfidence)
                {
                    maxConfidence = alternatives.get(i).getConfidence();
                    index = i;
                }
            }

            transcript = alternatives.get(index).getTranscript();
            recognizedPhrase = transcript.substring(0,transcript.length()-1);

        }

        return recognizedPhrase;
    }
    }

