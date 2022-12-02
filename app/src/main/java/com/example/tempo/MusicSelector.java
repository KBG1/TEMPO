package com.example.tempo;

import android.app.Activity;
import android.content.Context;
import android.media.MediaPlayer;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.Random;

public class MusicSelector {
    private static final String[][] bpmUnder80 = {{"oursparklingbeginning", "forloverswhohesitate"}, {"반짝이던 안녕", "주저하는 연인들을 위해"}, {"Fromm", "잔나비"},};
    private static final String[][] bpm80to89 = {{"eightletters", "strawberries_cigarettes"}, {"8 Letters", "Strawberries & \nCigarettes"}, {"Why Don't We", "Troye Sivan"},};
    private static final String[][] bpm90to99 = {{"loser", "spring"}, {"Loser", "봄봄봄"}, {"BIGBANG", "로이킴"},};
    private static final String[][] bpm100to109 = {{"fromtoday", "blueming"}, {"오늘부터 우리는", "Blueming"}, {"여자친구", "IU"},};
    private static final String[][] bpmOver110 = {{"luckystar", "tiktok"}, {"Lucky Star", "Tik Tok"}, {"Madonna", "Kesha"}};

    private final Context context;
    private final Activity activity;
    private StepMeasure stepMeasure;
    private Challenge challenge;
    private MediaPlayer mediaPlayer;
    String[][] targetSongs;
    int playCount;

    private final ImageView albumArt;
    private final TextView songTitle;
    private final TextView singerName;
    private Random rd = new Random();

    ImageButton btnPlay;

    MusicSelector(Context context, Activity activity){
        this.context = context;
        this.activity = activity;
        albumArt = activity.findViewById(R.id.albumArt);
        songTitle = activity.findViewById(R.id.songTitle);
        singerName = activity.findViewById(R.id.singerName);
        btnPlay = activity.findViewById(R.id.btnPlay);
        int songId = activity.getResources().getIdentifier(bpmUnder80[0][0], "raw", activity.getPackageName());
        playSong(songId);
    }

    public void setStepMeasure(StepMeasure step){
        stepMeasure = step;
    }
    
    public void setChallenge(Challenge cha){
        challenge = cha;
    }
    
    //분당 걸음 수에 따른 음악을 선택 및 재생 (디폴트 모드)
    public void selectSong(int stepsPerMinute){
        int songIdx = rd.nextInt(2);

        if (stepsPerMinute < 80){
            targetSongs = bpmUnder80;
        } else if(stepsPerMinute < 90 ) {
            targetSongs = bpm80to89;
        } else if(stepsPerMinute < 100 ){
            targetSongs = bpm90to99;
        } else if(stepsPerMinute < 110 ) {
            targetSongs = bpm100to109;
        } else{
            targetSongs = bpmOver110;
        }
        int imageId = activity.getResources().getIdentifier(targetSongs[0][songIdx], "drawable",activity.getPackageName());
        int songId = activity.getResources().getIdentifier(targetSongs[0][songIdx], "raw",activity.getPackageName());
        songTitle.setText(String.valueOf(targetSongs[1][songIdx]));
        singerName.setText(String.valueOf(targetSongs[2][songIdx]));
        albumArt.setImageResource(imageId);
        resetSong();
        playSong(songId);
    }

    public void selectChallengeSong(){
        int songIdx = rd.nextInt(2);
        int imageId = activity.getResources().getIdentifier(targetSongs[0][songIdx], "drawable",activity.getPackageName());
        int songId = activity.getResources().getIdentifier(targetSongs[0][songIdx], "raw",activity.getPackageName());
        songTitle.setText(String.valueOf(targetSongs[1][songIdx]));
        singerName.setText(String.valueOf(targetSongs[2][songIdx]));
        albumArt.setImageResource(imageId);
        resetSong();
        playChallengeSong(songId);
    }

    // level에 따른 음악 리스트를 지정하고 난수를 발생 시켜 songId를 도출
    public void setChallengeSongList(String level){
        switch (level) {
            case "LEVEL1":
                targetSongs = bpmUnder80;
                break;
            case "LEVEL2":
                targetSongs = bpm80to89;
                break;
            case "LEVEL3":
                targetSongs = bpm90to99;
                break;
            case "LEVEL4":
                targetSongs = bpm100to109;
                break;
            case "LEVEL5":
                targetSongs = bpmOver110;
                break;
        }
    }

    //mediaPlayer의 resource를 바꾸어 재생
    public void playSong(int songId){
        mediaPlayer = MediaPlayer.create(context, songId);
        mediaPlayer.start();
        mediaPlayer.setOnCompletionListener(mp -> selectSong(stepMeasure.getStepsPerMinute()));
    }

    //챌린지 모드의 세부 스테이지에 따른 음악 재생 횟수 count
    public void playChallengeSong(int songId){
        mediaPlayer = MediaPlayer.create(context, songId);
        mediaPlayer.start();
        mediaPlayer.setOnCompletionListener(mp -> {
            challenge.finishSong();
        });
    }

    public void pauseSong(){
        mediaPlayer.pause();
        btnPlay.setImageResource(android.R.drawable.ic_media_play);
    }

    public void resetSong(){
        mediaPlayer.stop();
        mediaPlayer.release();
    }

    public MediaPlayer getMediaPlayer(){
        return mediaPlayer;
    }
}
