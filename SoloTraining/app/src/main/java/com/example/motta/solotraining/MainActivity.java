package com.example.motta.solotraining;


import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.TextView;

public class MainActivity extends AppCompatActivity {

    private String numberOfTest = "1";
    Button btnRec = null;
    CheckBox boxOne = null;
    CheckBox boxTwo = null;
    EditText editText = null;
    EditText editText2 = null;
    EditText editText3 = null;
    private int speaker = 0;
    private final String PATH = "Audio recognition files test";
    private final String FILENAME = "trainingData";
    private final String FILENAME2 = "rec";
    private String speakerName = null;

    private int Fs = 8000;
    private int recordingLength = 3;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        editText = findViewById(R.id.editText);
        editText2= findViewById(R.id.editText2);
        editText3= findViewById(R.id.editText3);
        boxOne = findViewById(R.id.CheckOne);
        boxTwo = findViewById(R.id.CheckTwo);
        btnRec = findViewById(R.id.btt);
        btnRec.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if(boxOne.isChecked())
                    speaker = 1;
                else
                    speaker = 2;

                String tvValue = editText.getText().toString();
                if (!tvValue.equals("") ) {
                    Fs = Integer.parseInt(tvValue);
                }

                String tvValue2 = editText2.getText().toString();
                if (!tvValue2.equals("") ) {
                    recordingLength = Integer.parseInt(tvValue2);
                }

                Fs = Fs;
                recordingLength = recordingLength;

                speakerName = editText3.getText().toString();

                Rec rec = new Rec(getApplicationContext(), recordingLength, Fs,speaker , speakerName);
                rec.execute(PATH, FILENAME , FILENAME2,numberOfTest);

                numberOfTest = String.valueOf(Integer.parseInt(numberOfTest) + 1);
            }
        });

    }
}
