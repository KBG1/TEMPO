package com.example.tempo;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.media.MediaPlayer;
import android.os.Build;
import android.os.Bundle;
//import android.view.View;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import java.lang.Math;
import java.util.Random;

public class MainActivity extends AppCompatActivity implements SensorEventListener {

    private static final int TIME_STAMP_LENGTH = 10;
    private static final float MAX_SECONDS_BETWEEN_STEPS = 1;

    SensorManager sensorManager;
    Sensor stepCountSensor;
    TextView stepCountView;
    TextView stepsPerMinuteView;
    long lastStepTimeStamp = 0;
    float secondsFromLastStep = 0;

    // 현재 걸음 수
    int currentSteps = 0;
    float[] secondsBetweenSteps = new float[TIME_STAMP_LENGTH];
    int timeStampIndex = 0;
    float timeStampTotalTime = 0;
    int stepsPerMinute = 0;

    ImageButton btnPlay;
    ImageButton btnNext;
    ImageButton btnPrev;
    MediaPlayer mediaPlayer;

    ImageView albumArt;
    TextView songTitle;
    TextView singerName;

    String[][] bpmUnder80 = {{"oursparklingbeginning", "forloverswhohesitate"}, {"반짝이던 안녕", "주저하는 연인들을 위해"}, {"Fromm", "잔나비"},};
    String[][] bpm80to89 = {{"eightlettes", "strawberries_cigarettes"}, {"8 Letters", "Strawberries & Cigarettes"}, {"Why Don't We", "Troye Sivan"},};
    String[][] bpm90to99 = {{"loser", "spring"}, {"Loser", "봄봄봄"}, {"BIGBANG", "로이킴"},};
    String[][] bpm100to109 = {{"fromtoday", "blueming"}, {"오늘부터 우리는", "Blueming"}, {"여자친구", "IU"},};
    String[][] bpmOver110 = {{"luckystar", "tiktok"}, {"Lucky Star", "Tik Tok"}, {"Madonna", "Kesha"}};

    @RequiresApi(api = Build.VERSION_CODES.Q)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        stepCountView = findViewById(R.id.stepCountView);
        stepsPerMinuteView = findViewById(R.id.stepsPerMinuteView);

        sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        stepCountSensor = sensorManager.getDefaultSensor(Sensor.TYPE_STEP_DETECTOR);

        int songId = getResources().getIdentifier(bpmUnder80[0][0], "raw",getPackageName());
        mediaPlayer = MediaPlayer.create(this, songId);
        mediaPlayer.start();


        btnPlay = findViewById(R.id.btnPlay);
        btnNext = findViewById(R.id.btnNext);
        btnPrev = findViewById(R.id.btnPrev);
        albumArt = findViewById(R.id.albumArt);
        songTitle = findViewById(R.id.songTitle);
        singerName = findViewById(R.id.singerName);

        // 활동 퍼미션 체크
        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.ACTIVITY_RECOGNITION) == PackageManager.PERMISSION_DENIED) {

            requestPermissions(new String[]{Manifest.permission.ACTIVITY_RECOGNITION}, 0);
        }

        // 디바이스에 걸음 센서의 존재 여부 체크
        if (stepCountSensor == null) {
            Toast.makeText(this, "No Step Sensor", Toast.LENGTH_SHORT).show();
        }

        //next 버튼 클릭시 곡을 선택
        btnNext.setOnClickListener(v -> selectSong());

        //재생, 중지 버튼
        btnPlay.setOnClickListener(v -> {
            if(mediaPlayer.isPlaying()){
                mediaPlayer.pause();
                btnPlay.setImageResource(android.R.drawable.ic_media_play);

            }
            else{
                mediaPlayer.start();
                btnPlay.setImageResource(android.R.drawable.ic_media_pause);
            }
        });

        mediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mp) {
                selectSong();
            }
        });

    }

    public void onStart() {
        super.onStart();
        if(stepCountSensor !=null) {
            sensorManager.registerListener(this,stepCountSensor,SensorManager.SENSOR_DELAY_FASTEST);
        }
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        // 걸음 센서 이벤트 발생시
        if(event.sensor.getType() == Sensor.TYPE_STEP_DETECTOR){
            if(event.values[0]==1.0f){
                // 센서 이벤트가 발생할때 마다 걸음수 증가
                addStep();
                recordTimeStamp(event);
                refreshStepsPerMinute();
            }
        }
    }


    private void addStep(){
        currentSteps++;
        stepCountView.setText(String.valueOf(currentSteps));
    }

    private void recordTimeStamp(SensorEvent event){
        if (lastStepTimeStamp != 0) {
            secondsFromLastStep = (float)(event.timestamp - lastStepTimeStamp)/1000000000L;
            secondsBetweenSteps[timeStampIndex] = Math.min(secondsFromLastStep, MAX_SECONDS_BETWEEN_STEPS);
            timeStampIndex = getIndex(timeStampIndex + 1);
        }
        lastStepTimeStamp = event.timestamp;
    }

    private void refreshStepsPerMinute(){
        if (currentSteps > TIME_STAMP_LENGTH) {
            timeStampTotalTime = 0;
            for(float times : secondsBetweenSteps){
                timeStampTotalTime += times;
            }
            stepsPerMinute = Math.round(60F / (timeStampTotalTime / TIME_STAMP_LENGTH));
            stepsPerMinuteView.setText(String.valueOf(stepsPerMinute));

        }
    }

    private int getIndex(int val){
        if (val >= TIME_STAMP_LENGTH){
            return 0;
        }else if (val < 0){
            return TIME_STAMP_LENGTH-1;
        }else{
            return val;
        }
    }


    //mediaPlayer의 resource를 바꾸어 재생
    private void playSong(int songId){
        mediaPlayer.pause();
        mediaPlayer.reset();
        mediaPlayer = MediaPlayer.create(this, songId);
        mediaPlayer.start();

        mediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mp) {
                selectSong();
            }
        });

    }

    //분당 걸음 수에 따른 음악을 선택 및 재생
    private void selectSong(){

        Random rd = new Random();
        MediaPlayer nextSong;
        int songIdx = rd.nextInt(2);

        if (stepsPerMinute < 80){
            int imageId = getResources().getIdentifier(bpmUnder80[0][songIdx], "drawable",getPackageName());
            albumArt.setImageResource(imageId);
            int songId = getResources().getIdentifier(bpmUnder80[0][songIdx], "raw",getPackageName());
            playSong(songId);
            songTitle.setText(String.valueOf(bpmUnder80[1][songIdx]));
            singerName.setText(String.valueOf(bpmUnder80[2][songIdx]));

        }

        else if(stepsPerMinute >= 80 && stepsPerMinute < 90 ){
            int iconResId = getResources().getIdentifier(bpm80to89[0][songIdx], "drawable",getPackageName());
            albumArt.setImageResource(iconResId);
            int songId = getResources().getIdentifier(bpm80to89[0][songIdx], "raw",getPackageName());
            playSong(songId);
            songTitle.setText(String.valueOf(bpm80to89[1][songIdx]));
            singerName.setText(String.valueOf(bpm80to89[2][songIdx]));
        }

        else if(stepsPerMinute >= 90 && stepsPerMinute < 100 ){
            int iconResId = getResources().getIdentifier(bpm90to99[0][songIdx], "drawable",getPackageName());
            albumArt.setImageResource(iconResId);
            int songId = getResources().getIdentifier(bpm90to99[0][songIdx], "raw",getPackageName());
            playSong(songId);
            songTitle.setText(String.valueOf(bpm90to99[1][songIdx]));
            singerName.setText(String.valueOf(bpm90to99[2][songIdx]));

        }

        else if(stepsPerMinute >= 100 && stepsPerMinute < 110 ){
            int iconResId = getResources().getIdentifier(bpm100to109[0][songIdx], "drawable",getPackageName());
            albumArt.setImageResource(iconResId);
            int songId = getResources().getIdentifier(bpm100to109[0][songIdx], "raw",getPackageName());
            playSong(songId);
            songTitle.setText(String.valueOf(bpm100to109[1][songIdx]));
            singerName.setText(String.valueOf(bpm100to109[2][songIdx]));

        }

        else if(stepsPerMinute >= 110 ){
            int iconResId = getResources().getIdentifier(bpmOver110[0][songIdx], "drawable",getPackageName());
            albumArt.setImageResource(iconResId);
            int songId = getResources().getIdentifier(bpmOver110[0][songIdx], "raw",getPackageName());
            playSong(songId);
            songTitle.setText(String.valueOf(bpmOver110[1][songIdx]));
            singerName.setText(String.valueOf(bpmOver110[2][songIdx]));

        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }
}