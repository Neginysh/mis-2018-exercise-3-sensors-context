package com.example.lenovocom.sensormis;

import android.content.Intent;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import com.jjoe64.graphview.GraphView;
import com.jjoe64.graphview.series.DataPoint;
import com.jjoe64.graphview.series.LineGraphSeries;
import com.jjoe64.graphview.series.PointsGraphSeries;

import java.util.ArrayList;
import java.util.Random;


public class MainActivity extends AppCompatActivity implements SensorEventListener, SeekBar.OnSeekBarChangeListener {

    TextView x, y, z, magnitude, seekbar1text, seekbar2text;
    Sensor sensor;
    SensorManager sensorManager;
    LineGraphSeries<DataPoint> series;
    GraphView graphView;
    SeekBar seekbarSR;
    SeekBar seekbarFFT;
    int windowsSize = 64;
    int sampleRate = 0;
    double a, mg;
    double[] mgdouble;
    double[] freqCounts;
    RecentMagnitudeData recentMagnitudeData;




    @Override

    // reference for graph : https://github.com/jjoe64/GraphView
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        bindViews();
        //addEntry();
        mgdouble = new double[windowsSize];
        randomFill(mgdouble);

        seekbarSR.setMax(300);
        seekbarFFT.setMax(10);
        seekbarSR.setProgress(sampleRate);
        seekbarFFT.setProgress(8);
        seekbarSR.setOnSeekBarChangeListener(this);
        seekbarFFT.setOnSeekBarChangeListener(this);
        recentMagnitudeData = new RecentMagnitudeData(windowsSize);

        startService(new Intent(MainActivity.this, RecognitionService.class));

//        startservice.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                startService(new Intent(MainActivity.this, Player.class));
//            }
//        });
//        stopservice.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                stopService(new Intent(MainActivity.this, Player.class));
//            }
//        });


    }


    private void bindViews() {
        x = (TextView) findViewById(R.id.x);
        y = (TextView) findViewById(R.id.y);
        z = (TextView) findViewById(R.id.z);
        seekbar1text = (TextView) findViewById(R.id.seekbar1text);
        seekbar2text = (TextView) findViewById(R.id.seekbar2text);
        magnitude = (TextView) findViewById(R.id.magnitude);
        seekbarSR = (SeekBar) findViewById(R.id.seekbarSR);
        seekbarFFT = (SeekBar) findViewById(R.id.seekbarFFT);


        sensorManager = (SensorManager) getSystemService(this.SENSOR_SERVICE);
        sensor = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        sensorManager.registerListener(this, sensor, sampleRate);

        graphView = (GraphView) findViewById(R.id.graphView);
        series = new LineGraphSeries<DataPoint>();




    }


    @Override
    public void onSensorChanged(SensorEvent event) {

        x.setText("X: " + event.values[0]);
        y.setText("Y: " + event.values[1]);
        z.setText("Z: " + event.values[2]);
        mg = magnitude(event.values[0], event.values[1], event.values[2]);
        magnitude.setText(mg + "");

        recentMagnitudeData.addToQueue(mg);
        mgdouble = recentMagnitudeData.getRecentWindow();
        new FFTAsynctask(windowsSize).execute(mgdouble);


    }

    public void syncTaskDone() {
        addEntry();
    }

    private double magnitude(double x, double y, double z) {

        return (double) Math.sqrt(x * x + y * y + z * z);
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }


    @Override
    protected void onResume() {
        super.onResume();
        sensorManager.registerListener(this, sensor, sampleRate * 100000);

    }

//  https://github.com/jjoe64/GraphView
    private void addEntry() {
        LineGraphSeries<DataPoint> series = new LineGraphSeries<>();
        for (int i = 0; i < windowsSize; i++) {
            try {
                series.appendData(new DataPoint(i, freqCounts[i]), true, windowsSize);
            } catch (Exception e) {
                series.appendData(new DataPoint(i, 0), true, windowsSize);
            }

        }
        graphView.removeAllSeries();
        graphView.addSeries(series);


    }

    @Override
    protected void onPause() {
        super.onPause();
        sensorManager.unregisterListener(this);
    }

    @Override
    public void onProgressChanged(SeekBar seekBar, int i, boolean fromUser) {
        if (seekBar.getId() == R.id.seekbarSR) {
            //change sensor sampling rate
            sampleRate = i;
            seekbar1text.setText(sampleRate + "");
            sensorManager.unregisterListener(this);
            sensorManager.registerListener(this, sensor, SensorManager.SENSOR_DELAY_NORMAL * sampleRate * 100000);

        } else if (seekBar.getId() == R.id.seekbarFFT) {
            i = (int) Math.pow(2, i);
            windowsSize = i;
            mgdouble = new double[i];
            recentMagnitudeData.initiateWindow(i);
            seekbar2text.setText(windowsSize + "");

        }

    }

    @Override
    public void onStartTrackingTouch(SeekBar seekBar) {

    }

    @Override
    public void onStopTrackingTouch(SeekBar seekBar) {

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        stopService(new Intent(MainActivity.this, Player.class));
        stopService(new Intent(MainActivity.this, RecognitionService.class));
    }

    /**
     * Implements the fft functionality as an async task
     * FFT(int n): constructor with fft length
     * fft(double[] x, double[] y)
     */

    private class FFTAsynctask extends AsyncTask<double[], Void, double[]> {

        private int wsize; //window size must be power of 2

        // constructor to set window size
        FFTAsynctask(int wsize) {
            this.wsize = wsize;
        }

        @Override
        protected double[] doInBackground(double[]... values) {


            double[] realPart = values[0].clone(); // actual acceleration values
            double[] imagPart = new double[wsize]; // init empty

            /**
             * Init the FFT class with given window size and run it with your input.
             * The fft() function overrides the realPart and imagPart arrays!
             */
            FFT fft = new FFT(wsize);
            fft.fft(realPart, imagPart);
            //init new double array for magnitude (e.g. frequency count)
            double[] magnitude = new double[wsize];


            //fill array with magnitude values of the distribution
            for (int i = 0; wsize > i; i++) {
                magnitude[i] = Math.sqrt(Math.pow(realPart[i], 2) + Math.pow(imagPart[i], 2));
            }
            magnitude[0] = magnitude[1];

            return magnitude;

        }

        @Override
        protected void onPostExecute(double[] values) {
            //hand over values to global variable after background task is finished
            freqCounts = values;
            syncTaskDone();
        }
    }

    /**
     * little helper function to fill example with random double values
     */
    public void randomFill(double[] array) {
        Random rand = new Random();
        for (int i = 0; array.length > i; i++) {
            array[i] = rand.nextDouble();
        }
    }

}


