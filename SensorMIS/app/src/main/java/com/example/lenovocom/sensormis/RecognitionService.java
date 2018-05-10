package com.example.lenovocom.sensormis;

import android.annotation.SuppressLint;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationManager;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.util.Log;
import android.widget.Toast;

import java.util.List;

public class RecognitionService extends Service {
    Context context;
    Intent intent;
    private MediaPlayer playerJogging;
    private MediaPlayer playerBiking;


    private SensorManager sensorManager;
    private Sensor sensor;
    private int windowsSize = 64;
    private int sampleRate = 500000;
    private double mg;
    private double[] mgdouble;
    private double[] freqCounts;
    private RecentMagnitudeData recentMagnitudeData;
    private SensorEventListener sensorEventListener;
    private String activity = "jogging";
    private static final String TAG = "BOOMBOOMTESTGPS";
    private LocationManager mLocationManager = null;
    private static final int LOCATION_INTERVAL = 1000;
    private static final float LOCATION_DISTANCE = 10f;
    float speed = (float) 0.0;
    public String bestProvider;
    public Criteria criteria;


    public RecognitionService() {
    }

    //reference for location listener, https://stackoverflow.com/questions/28535703/best-way-to-get-user-gps-location-in-background-in-android


    @Override
    public void onCreate() {
        //super.onCreate();
        recentMagnitudeData = new RecentMagnitudeData(windowsSize);
        setUpPlayer();
        sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        if (sensorManager.getDefaultSensor(Sensor.TYPE_GRAVITY) != null) {
            List<Sensor> gravSensors = sensorManager.getSensorList(Sensor.TYPE_GRAVITY);
            for (int i = 0; i < gravSensors.size(); i++) {
                if ((gravSensors.get(i).getVendor().contains("Google Inc.")) &&
                        (gravSensors.get(i).getVersion() == 3)) {
                    // Use the version 3 gravity sensor.
                    sensor = gravSensors.get(i);
                }
            }
        } else {
            // Use the accelerometer.
            if (sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER) != null) {
                sensor = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
            } else {
                // Sorry, there are no accelerometers on your device.
                // You can't play this game.
            }
        }

        initializeLocationManager();

        try {
            criteria = new Criteria();
            bestProvider = String.valueOf(mLocationManager.getBestProvider(criteria, true)).toString();
            LocationListener locationListener = new LocationListener(bestProvider);
            mLocationManager.requestLocationUpdates(
                    LocationManager.NETWORK_PROVIDER, LOCATION_INTERVAL, LOCATION_DISTANCE,
                    locationListener);
        } catch (java.lang.SecurityException ex) {
            Log.i(TAG, "fail to request location update, ignore", ex);
        } catch (IllegalArgumentException ex) {
            Log.d(TAG, "network provider does not exist, " + ex.getMessage());
        }
        /*try {
            mLocationManager.requestLocationUpdates(
                    LocationManager.GPS_PROVIDER, LOCATION_INTERVAL, LOCATION_DISTANCE,
                    mLocationListeners[0]);
        } catch (java.lang.SecurityException ex) {
            Log.i(TAG, "fail to request location update, ignore", ex);
        } catch (IllegalArgumentException ex) {
            Log.d(TAG, "gps provider does not exist " + ex.getMessage());
        }*/

    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        //sensor = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        sensorManager.registerListener(mSensorListener, sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER), sampleRate);


        return Service.START_NOT_STICKY;
        //return Service.START_NOT_STICKY;
    }

    private final SensorEventListener mSensorListener = new SensorEventListener() {
        @Override
        public void onSensorChanged(SensorEvent event) {
            mg = Math.sqrt(event.values[0] * event.values[0] + event.values[1] * event.values[1] + event.values[2] * event.values[2]);
            recentMagnitudeData.addToQueue(mg);
            Log.d("test", mg + "");
            String newActivity = activityRecognition();
            if (newActivity == activity)
                return;
            else
                activity = newActivity;
            switch (activity) {
                case "jogging":

                    if (playerBiking.isPlaying())
                        playerBiking.pause();
                    if (!playerJogging.isPlaying())
                        playerJogging.start();
                    //  Toast.makeText(RecognitionService.this.getApplicationContext(), "" + speed, Toast.LENGTH_SHORT).show();
                    // Toast.makeText(RecognitionService.this.getApplicationContext(), "joginggggggggggggggggggggggggg", Toast.LENGTH_LONG).show();
                    //startPlayer("jogging");
                    break;
                case "biking":
                    if (playerJogging.isPlaying())
                        playerJogging.pause();
                    if (!playerBiking.isPlaying())
                        playerBiking.start();

                    // Toast.makeText(RecognitionService.this.getApplicationContext(), "biking", Toast.LENGTH_LONG).show();
                    //startPlayer("biking");
                    break;
                case "sitting":
                    //  Toast.makeText(RecognitionService.this.getApplicationContext(), "sit", Toast.LENGTH_LONG).show();
                    if (playerBiking.isPlaying()) playerBiking.pause();
                    if (playerJogging.isPlaying()) playerJogging.pause();

            }

        }


        @Override
        public void onAccuracyChanged(Sensor sensor, int accuracy) {

        }
    };

    private String activityRecognition() {
        double[] magnitudeArray = recentMagnitudeData.getRecentWindow();
        double avg = 0.0;

        // the average of magnitude based on window size
        for (int i = 0; i < magnitudeArray.length; i++) {
            avg = avg + magnitudeArray[i];
        }
        avg = avg / magnitudeArray.length;

        if (avg < 11.0) return "sitting";
        else if (avg < 20.0) return "jogging";
        else return "biking";
//        else if (avg < 20.0 /*&& (speed>1.7)*/) return "biking";
//        else if ((speed > 0.7 && speed < 1.7) || true) return "jogging";
//        return "sitting";

    }


    private void setUpPlayer() {


        playerJogging = MediaPlayer.create(RecognitionService.this.getApplicationContext(), R.raw.bensound_cute);  // cute for jogging, jazzy for biking
        playerJogging.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mp) {
                playerJogging.setLooping(true);
                playerJogging.start();
            }
        });

        playerBiking = MediaPlayer.create(RecognitionService.this.getApplicationContext(), R.raw.bensound_jazzyfrenchy);

        playerBiking.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mp) {
                playerBiking.setLooping(true);
                playerBiking.start();
            }
        });

    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        playerBiking.stop();
        playerJogging.stop();
        sensorManager.unregisterListener(sensorEventListener, sensor);
        criteria = new Criteria();
        bestProvider = String.valueOf(mLocationManager.getBestProvider(criteria, true)).toString();
        LocationListener locationListener = new LocationListener(bestProvider);
        mLocationManager.removeUpdates(locationListener);
        /*if (mLocationManager != null) {
            for (int i = 0; i < mLocationListeners.length; i++) {
                try {
                    mLocationManager.removeUpdates(mLocationListeners[i]);
                } catch (Exception ex) {
                    Log.i(TAG, "fail to remove location listners, ignore", ex);
                }
            }
        }*/
    }


    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    private class LocationListener implements android.location.LocationListener {
        Location mLastLocation;

        public LocationListener(String provider) {
            Log.e(TAG, "LocationListener " + provider);
            mLastLocation = new Location(provider);
        }

        @Override
        public void onLocationChanged(Location location) {
            Log.e(TAG, "onLocationChanged: " + location);
            mLastLocation.set(location);
            speed = location.getSpeed();

        }

        @Override
        public void onStatusChanged(String provider, int status, Bundle extras) {
            Log.e(TAG, "onStatusChanged: " + provider);
        }

        @Override
        public void onProviderDisabled(String provider) {
            Log.e(TAG, "onProviderDisabled: " + provider);
        }

        @Override
        public void onProviderEnabled(String provider) {
            Log.e(TAG, "onProviderEnabled: " + provider);
        }


    }

    LocationListener[] mLocationListeners = new LocationListener[]{
            new LocationListener(LocationManager.GPS_PROVIDER),
            new LocationListener(LocationManager.NETWORK_PROVIDER)
    };

    private void initializeLocationManager() {
        Log.e(TAG, "initializeLocationManager");
        if (mLocationManager == null) {
            mLocationManager = (LocationManager) getApplicationContext().getSystemService(Context.LOCATION_SERVICE);
        }
    }
}
