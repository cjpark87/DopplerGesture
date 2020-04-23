package edu.washington.cs.ubicomp.dopplergesture;

import android.os.Environment;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;

import github.nisrulz.zentone.ToneStoppedListener;
import github.nisrulz.zentone.ZenTone;

public class DopplerRecognizer {
    private static int DURATION = 300;
    private static float VOLUME = 1.0f;

    private MainActivity mainActivity;
    private AudioAnalyzer aa;

    public DopplerRecognizer(MainActivity mainActivity) {
        this.mainActivity = mainActivity;

        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd-HH:mm:ssZ");

        File directory = new File(Environment.getExternalStorageDirectory() + "/doppler/");
        if (! directory.exists()){
            directory.mkdirs();
        }


        String wavPath = Environment.getExternalStorageDirectory() + "/doppler/" + simpleDateFormat.format(new Date()) + ".wav";

        aa = new AudioAnalyzer(this.mainActivity);
    }

    public void start(int frquency) {
        //start the audio
        ZenTone.getInstance().generate(frquency, DURATION, VOLUME, new ToneStoppedListener() {
            @Override
            public void onToneStopped() {
                // Do something when the tone has stopped playing
            }
        });

        aa.start(frquency);
    }

    public void stop() {
        ZenTone.getInstance().stop();
        aa.stop();
    }
}
