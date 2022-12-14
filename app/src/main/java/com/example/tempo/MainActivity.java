package com.example.tempo;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.content.res.TypedArray;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Build;
import android.os.Bundle;
import android.widget.CompoundButton;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Toast;
import android.app.Activity;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.widget.ImageView;

public class MainActivity extends AppCompatActivity implements SensorEventListener {

    int stepsPerMinute = 0;
    boolean isChallenge = false;

    StepMeasure stepMeasure;
    AutoPause autoPause;
    MusicSelector musicSelector;

    SensorManager sensorManager;
    Sensor stepCountSensor;
    Challenge challenge;

    ImageButton btnPlay;
    ImageButton btnNext;
    ImageButton btnPrev;

    CompoundButton challengeSwitch;

    private String targetLevel = "LEVEL2"; //레벨
    private String targetStage = "-1"; //세부 스테이지

    @RequiresApi(api = Build.VERSION_CODES.Q)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        stepMeasure = new StepMeasure(MainActivity.this, this);
        autoPause = new AutoPause(MainActivity.this, this);
        musicSelector = new MusicSelector(MainActivity.this, this);
        musicSelector.setStepMeasure(stepMeasure);

        sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        stepCountSensor = sensorManager.getDefaultSensor(Sensor.TYPE_STEP_DETECTOR);

        btnPlay = findViewById(R.id.btnPlay);
        btnNext = findViewById(R.id.btnNext);
        btnPrev = findViewById(R.id.btnPrev);

        challenge = new Challenge(MainActivity.this, this, targetLevel, targetStage);
        challengeSwitch = findViewById(R.id.challengeSwitch);
        challenge.exitChallengeMode();
        challengeSwitch.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if(isChecked) {
                isChallenge = true;
                challenge.enterChallengeMode(musicSelector, stepMeasure);
                autoPause.stopPauseTimer();
            }
            else {
                isChallenge = false;
                challenge.exitChallengeMode();
                musicSelector.selectSong(stepMeasure.getStepsPerMinute());
            }
        });

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
        btnNext.setOnClickListener(v -> musicSelector.selectSong(stepMeasure.getStepsPerMinute()));

        //재생, 중지 버튼
        btnPlay.setOnClickListener(v -> {
            autoPause.stopPauseTimer();
            if(musicSelector.getMediaPlayer().isPlaying()){
                musicSelector.getMediaPlayer().pause();
                btnPlay.setImageResource(android.R.drawable.ic_media_play);
            }
            else{
                musicSelector.getMediaPlayer().start();
                btnPlay.setImageResource(android.R.drawable.ic_media_pause);
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
                stepMeasure.addStep();
                stepMeasure.recordTimeStamp(event);
                stepMeasure.refreshStepsPerMinute();
                stepsPerMinute = stepMeasure.getStepsPerMinute();
                if (isChallenge){
                    challenge.watchStep();
                }
                // 자동 일시정지 상태라면 다시 음악을 재생
                if (autoPause.getAutoPaused()){
                    musicSelector.getMediaPlayer().start();
                    btnPlay.setImageResource(android.R.drawable.ic_media_pause);
                }
                // 자동 일시정지 타이머 초기화
                if(!isChallenge) {
                    autoPause.startPauseTimer(musicSelector.getMediaPlayer(), btnPlay);
                }
            }
        }
    }
    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }
}