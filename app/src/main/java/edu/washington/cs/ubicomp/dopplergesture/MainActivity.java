package edu.washington.cs.ubicomp.dopplergesture;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

public class MainActivity extends AppCompatActivity {
    private static int REQUEST_PERMISSION = 100;
    Button startButton;
    boolean started = false;
    DopplerRecognizer dr;
    EditText frequencyText;
    TextView gestureText;
    TextView upperFreqText;
    TextView lowerFreqText;
    TextView targetFreqText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        frequencyText = findViewById(R.id.freq);
        frequencyText.setText(String.format("%d",Constants.TX_FREQUENCY));

        gestureText = findViewById(R.id.gestureText);
        lowerFreqText = findViewById(R.id.lowerFreqText);
        upperFreqText = findViewById(R.id.upperFreqText);
        targetFreqText = findViewById(R.id.targetFreqText);

        dr = new DopplerRecognizer(this);

        startButton = findViewById(R.id.start);
        startButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (!started) {
                    startRecognition();
                    started = true;
                    startButton.setText(R.string.btn_stop);
                } else {
                    stopRecognition();
                    started = false;
                    startButton.setText(R.string.btn_start);
                }
            }
        });

        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED ||
            ContextCompat.checkSelfPermission(this,
                Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {

            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.RECORD_AUDIO, Manifest.permission.WRITE_EXTERNAL_STORAGE},
                    REQUEST_PERMISSION);
        }
    }

    private void startRecognition() {
       int freq = Integer.parseInt(frequencyText.getText().toString());
        dr.start(freq);
    }

    private void stopRecognition() {
        dr.stop();
    }
}
