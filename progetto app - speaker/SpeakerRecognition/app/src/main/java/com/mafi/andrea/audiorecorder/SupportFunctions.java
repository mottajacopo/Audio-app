package com.mafi.andrea.audiorecorder;

import android.util.Log;

import java.io.DataOutputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;

/**
 * Created by MyPC on 26/05/2018.
 */

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

    public static void printFeaturesOnFile (ArrayList<double[]> mfcc,ArrayList<double[]> deltadelta, String _fileDir)
    {

        //final String label = "2"; //label che va cambiato ad ogni registrazione di un parlatore diverso

        final double label = 2;

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
}
