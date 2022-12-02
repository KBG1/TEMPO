package com.example.tempo;

import android.app.Activity;
import android.content.Context;
import android.media.MediaPlayer;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;

import java.util.Timer;
import java.util.TimerTask;

public class AutoPause {

    private static final float SECONDS_UNTIL_PAUSE = 5;

    private final Activity activity;

    private final TextView autoPauseView;

    private boolean autoPaused = false;

    private TimerTask timerTask = null;
    private final Timer timer = new Timer();

    AutoPause(Context context, Activity activity){
        this.activity = activity;
        autoPauseView = activity.findViewById(R.id.autoPauseView);
        autoPauseView.setVisibility(View.INVISIBLE);
    }

    public void startPauseTimer(MediaPlayer mediaPlayer, ImageButton btnPlay){
        stopPauseTimer();
        //자동 일시정지
        timerTask = new TimerTask(){
            @Override
            public void run(){
                autoPaused = true;
                activity.runOnUiThread(() -> autoPauseView.setVisibility(View.VISIBLE));
                mediaPlayer.pause();
                btnPlay.setImageResource(android.R.drawable.ic_media_play);

            }
        };
        timer.schedule(timerTask,(int)(SECONDS_UNTIL_PAUSE * 1000));
    }

    public void stopPauseTimer(){
        autoPaused = false;
        autoPauseView.setVisibility(View.INVISIBLE);
        if (timerTask != null){
            timerTask.cancel();
            timerTask = null;
        }
    }

    public boolean getAutoPaused(){
        return autoPaused;
    }

}
