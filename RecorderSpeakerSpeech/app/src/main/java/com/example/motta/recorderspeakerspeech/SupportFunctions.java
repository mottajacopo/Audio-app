package com.example.motta.recorderspeakerspeech;

import android.util.Log;

import java.io.DataOutputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;

/**
 * Created by MyPC on 26/05/2018.
 */

public class SupportFunctions {

/////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
//funzione per il calcolo dei delta degli mfcc
/////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////


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

/////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
//funzione per conversione float to double
/////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////


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

/////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
//fuzione per unire features e delta del parlatore da riconoscere in una lista (utilizzata  per creare poi gli svm_node)
/////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////


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

/////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
//funzione che salva il file contenente le features e i delta dei parlatori autorizzati (utilizzata per creare svm _node e il modello)
/////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////


    public static void printFeaturesOnFile (ArrayList<double[]> mfcc, ArrayList<double[]> deltadelta, String _fileDir)
    {

        //final String label = "2"; //label che va cambiato ad ogni registrazione di un parlatore diverso

        final double label = 1;

        ArrayList<double[]> union = uniteAllFeaturesInOneList(mfcc,deltadelta);

        int totalNumberOfFeatures = union.get(0).length;
/*
        DecimalFormatSymbols symbol = new DecimalFormatSymbols();
        symbol.setDecimalSeparator('.');
        DecimalFormat format = new DecimalFormat("#.0000000",symbol);
        float value;
        String stringValue;
*/
        try
        {
/*
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

/////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
//funzione per salvare il file .wav del parlatore (utilizzata per il riconoscimento della frase )
/////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////


    public static void saveWavFile (int Fs , int nSamples , short[] audioData , String _fileName , String storeDir)
    {
        byte dataByte[] = new byte[2*nSamples];

        for (int i = 0; i< nSamples; i++)
        {
            dataByte[2*i] = (byte)(audioData[i] & 0x00ff);
            dataByte[2*i +1] = (byte)((audioData[i] >> 8) & 0x00ff);
        }

        WavIO writeWav = new WavIO(storeDir + "/" + _fileName, 16,1,1,Fs,2,16,dataByte);
        writeWav.save();
    }
}
