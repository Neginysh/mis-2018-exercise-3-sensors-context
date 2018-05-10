package com.example.lenovocom.sensormis;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.media.MediaPlayer;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.view.View;

public class Player extends Service {
    Context context;
    private MediaPlayer player;

    public Player() {
    }

    public Player(Context context, MediaPlayer player) {
        this.context = context;
        this.player = player;
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override //when service starts
    public int onStartCommand(Intent intent, int flags, int startId) {

        if (player == null) {
            player = MediaPlayer.create(this, R.raw.bensound_cute);  // cute for jogging, jazzy for biking
            player.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                @Override
                public void onCompletion(MediaPlayer mp) {
                    player.setLooping(true);
                    player.start();
                }
            });
        }
        player.start();
        return START_STICKY;
    }


    @Override //when service stops
    public void onDestroy() {
        super.onDestroy();
        player.stop();
    }


    // ref for media player: https://codinginflow.com/tutorials/android/mediaplayer
//    public void play (View view){
//        if (player == null) {
//            player = MediaPlayer.create(context, R.raw.bensound_cute);
//            player.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
//                @Override
//                public void onCompletion(MediaPlayer mp) {
//                    player.setLooping(true);
//                    //stopPlayer();
//                }
//            });
//        }
//        player.start();
//    }
//
//    public void pause (View view){
//        if (player != null)
//            player.pause();
//    }
//
//    public void stop (View view){
//        stopPlayer();
//    }
//
//    private void stopPlayer () {
//        if (player != null) {
//            player.release();
//            player = null;
//        }
//    }
//
//    @Override
//    protected void onStop () {
//        super.onStop();
//        stopPlayer();
//    }


}
