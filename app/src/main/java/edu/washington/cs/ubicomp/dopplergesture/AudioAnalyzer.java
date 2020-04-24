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
    AudioDispatcher dispatcher;

    Thread audioAnalyzerThread;

    MainActivity mainActivity;

    public AudioAnalyzer (MainActivity mainActivity) {
        this.mainActivity = mainActivity;
    }

    public void start (int frequency) {
        dispatcher = AudioDispatcherFactory.fromDefaultMicrophone(Constants.SAMPLE_RATE, Constants.BUFFER_SIZE, 0);
        dispatcher.addAudioProcessor(new BandPass(frequency, 2*Constants.TX_FREQUENCY_BANDWIDTH, Constants.SAMPLE_RATE));
        dispatcher.addAudioProcessor(new AudioProcessor() {

            FFT fft = new FFT(Constants.BUFFER_SIZE);
            final float[] amplitudes = new float[Constants.BUFFER_SIZE/2];

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
                    if ((int) fft.binToHz(i, Constants.SAMPLE_RATE) > 18500 && (int) fft.binToHz(i, Constants.SAMPLE_RATE) < 20500) {
                        //Log.d(TAG, String.format("Amplitude at %3d Hz: %8.3f", (int) fft.binToHz(i, SAMPLE_RATE) , amplitudes[i]));
                    }

                    if ((int) fft.binToHz(i, Constants.SAMPLE_RATE) > Constants.TX_FREQUENCY - Constants.DOPPLER_SHIFT_FREQUENCY_BANDWIDTH
                            && (int) fft.binToHz(i, Constants.SAMPLE_RATE) < Constants.TX_FREQUENCY - Constants.TX_FREQUENCY_BANDWIDTH) {
                        lowerFreq += amplitudes[i];
                    }

                    if ((int) fft.binToHz(i, Constants.SAMPLE_RATE) >= Constants.TX_FREQUENCY - Constants.DOPPLER_SHIFT_FREQUENCY_BANDWIDTH
                            && (int) fft.binToHz(i, Constants.SAMPLE_RATE) <= Constants.TX_FREQUENCY + Constants.DOPPLER_SHIFT_FREQUENCY_BANDWIDTH) {
                        targetFreq += amplitudes[i];
                    }

                    if ((int) fft.binToHz(i, Constants.SAMPLE_RATE) > Constants.TX_FREQUENCY + Constants.TX_FREQUENCY_BANDWIDTH
                            && (int) fft.binToHz(i, Constants.SAMPLE_RATE) < Constants.TX_FREQUENCY + Constants.DOPPLER_SHIFT_FREQUENCY_BANDWIDTH) {
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
                        if (Math.abs(upperFreqFinal - lowerFreqFinal) > Constants.DOPPLER_SHIFT_DIFF_THRESHOLD * Math.min(upperFreqFinal, lowerFreqFinal))
                            mainActivity.gestureText.setText(upperFreqFinal > lowerFreqFinal ? R.string.push : R.string.pull);
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
