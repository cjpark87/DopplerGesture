package edu.washington.cs.ubicomp.dopplergesture;

import android.os.Environment;
import android.util.Log;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;

import github.nisrulz.zentone.ToneStoppedListener;
import github.nisrulz.zentone.ZenTone;

public class DopplerRecognizer implements AudioRecorderListener {
    private static int DURATION = 300;
    private static float VOLUME = 1.0f;

    private AudioRecorder ar;

    public DopplerRecognizer() {
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd-HH:mm:ssZ");

        File directory = new File(Environment.getExternalStorageDirectory() + "/doppler/");
        if (! directory.exists()){
            directory.mkdirs();
        }


        String wavPath = Environment.getExternalStorageDirectory() + "/doppler/" + simpleDateFormat.format(new Date()) + ".wav";
        ar = new AudioRecorder(wavPath);
        ar.setListener(this);
    }

    public void start(int frquency) {
        //start the audio
        ZenTone.getInstance().generate(frquency, DURATION, VOLUME, new ToneStoppedListener() {
            @Override
            public void onToneStopped() {
                // Do something when the tone has stopped playing
            }
        });

        ar.startRecording();
    }

    public void stop() {
        ZenTone.getInstance().stop();
        ar.stopRecording();
    }

    @Override
    public void onAudioChanged(byte[] data) {

    }
}
