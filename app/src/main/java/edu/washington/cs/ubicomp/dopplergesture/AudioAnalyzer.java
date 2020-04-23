package edu.washington.cs.ubicomp.dopplergesture;

import android.util.Log;

import be.tarsos.dsp.AudioDispatcher;
import be.tarsos.dsp.AudioEvent;
import be.tarsos.dsp.AudioProcessor;
import be.tarsos.dsp.filters.BandPass;
import be.tarsos.dsp.io.android.AudioDispatcherFactory;
import be.tarsos.dsp.util.fft.FFT;

public class AudioAnalyzer {
    String TAG = "AudioAnalyzer";
    int BUFFER_SIZE = 22050;
    int SAMPLE_RATE = 44100;
    AudioDispatcher dispatcher;

    Thread audioAnalyzerThread;

    MainActivity mainActivity;

    public AudioAnalyzer (MainActivity mainActivity) {
        this.mainActivity = mainActivity;
    }

    public void start (int frequency) {
        dispatcher = AudioDispatcherFactory.fromDefaultMicrophone(SAMPLE_RATE, BUFFER_SIZE, 0);
        dispatcher.addAudioProcessor(new BandPass(frequency, 1000, SAMPLE_RATE));
        dispatcher.addAudioProcessor(new AudioProcessor() {

            FFT fft = new FFT(BUFFER_SIZE);
            final float[] amplitudes = new float[BUFFER_SIZE/2];

            @Override
            public boolean process(AudioEvent audioEvent) {
                float[] audioBuffer = audioEvent.getFloatBuffer();
                fft.forwardTransform(audioBuffer);
                fft.modulus(audioBuffer, amplitudes);

                Log.d(TAG, String.format("Frames %d, buffer %d, floatbuffer %d", audioEvent.getFrameLength(), audioEvent.getBufferSize(), audioBuffer.length));

                double lowerFreq = 0;
                double upperFreq = 0;
                double targetFreq = 0;

                for (int i = 0; i < amplitudes.length; i++) {
                    if ((int) fft.binToHz(i, SAMPLE_RATE) > 18500 && (int) fft.binToHz(i, SAMPLE_RATE) < 20500) {
                        //Log.d(TAG, String.format("Amplitude at %3d Hz: %8.3f", (int) fft.binToHz(i, SAMPLE_RATE) , amplitudes[i]));
                    }

                    if ((int) fft.binToHz(i, SAMPLE_RATE) > 18000 && (int) fft.binToHz(i, SAMPLE_RATE) < 18950) {
                        lowerFreq += amplitudes[i];
                    }

                    if ((int) fft.binToHz(i, SAMPLE_RATE) >= 18950 && (int) fft.binToHz(i, SAMPLE_RATE) <= 19050) {
                        targetFreq += amplitudes[i];
                    }

                    if ((int) fft.binToHz(i, SAMPLE_RATE) > 19050 && (int) fft.binToHz(i, SAMPLE_RATE) < 20000) {
                        upperFreq += amplitudes[i];
                    }
                }

                final double lowerFreqFinal = lowerFreq;
                final double upperFreqFinal = upperFreq;
                final double targetFreqFinal = targetFreq;

                mainActivity.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        mainActivity.upperFreqText.setText(String.format("%.3f", upperFreqFinal));
                        mainActivity.lowerFreqText.setText(String.format("%.3f", lowerFreqFinal));
                        mainActivity.targetFreqText.setText(String.format("%.3f", targetFreqFinal));
                        if (Math.abs(upperFreqFinal - lowerFreqFinal) > 100)
                            mainActivity.gestureText.setText(upperFreqFinal > lowerFreqFinal ? "Toward" : "Away");
                    }
                });

                return true;
            }

            @Override
            public void processingFinished() {

            }
        });

        audioAnalyzerThread = new Thread(dispatcher,"Audio Dispatcher");
        audioAnalyzerThread.start();
    }

    public void stop () {
        if (audioAnalyzerThread != null) {
            audioAnalyzerThread.interrupt();
            audioAnalyzerThread = null;
            dispatcher.stop();
        }
    }
}
