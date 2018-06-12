package com.example.motta.solotraining;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

public class MainActivity extends AppCompatActivity {

    Button btnRec = null;
    Button btt2 = null;
    TextView tv = null;
    private final String PATH = "Audio recognition files";
    private final String FILENAME = "trainingData";

    private double label = 1;

    private final int Fs = 44200;
    private final int recordingLength = 3;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        btnRec = findViewById(R.id.btt);
        btnRec.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                Training train = new Training(getApplicationContext(), recordingLength, Fs , label);
                train.execute(PATH, FILENAME);
            }
        });

        btt2 = findViewById(R.id.btt2);
        tv = findViewById(R.id.tv);
        btt2.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {

                label++;
                int i = (int)label;
                tv.setText(String.valueOf(i));

            }
        });

    }
}
