package com.example.tempo;

import android.app.Activity;
import android.content.Context;
import android.graphics.Color;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.util.Locale;
import java.util.Map;

public class Challenge {

    private final Context context;
    private final Activity activity;
    private ImageView icon;
    private final TextView appTitle;
    private final TextView stepTitle;
    private final TextView musicTitle;
    private final TextView challengeTitle;
    private final TextView todayMission;
    private final TextView todayLevel;
    private final TextView todayStage;
    private final LinearLayout challengeLayout;
    private final TextView remainingGoal;
    private final TextView remainingGoalLabel;
    private final TextView stepCountView;
    private final TextView stepsPerMinuteView;
    private String targetLevel; //레벨
    private String targetStage; //세부 스테이지
    private int targetBPM = 85; //목표 BPM
    private int songsLeft = 0; // 남은 챌린지 곡 수
    private int songsBeforeRest = 0; // 휴식 전에 남은 챌린지 곡 수 (!)0이라면 현재 곡이 휴식곡이라는 것을 의미
    private static final int CHALLENGE_SUCCESS_PERCENTAGE = 50; //성공 커트라인 (%)
    private static final int CHALLENGE_LEVEL_CLEAR_PERCENTAGE = 0;
    private static final int BPM_RANGE = 5; //목표 BPM 범위 (+-)
    private int stepsOutBPM = 0; //목표 BPM 범위에서 벗어난 걸음 수
    private int stepsInBPM = 0; //목표 BPM 범위 안에 들어간 걸음 수
    private MusicSelector musicSelector;
    private StepMeasure stepMeasure;
    int successCount = 0;
    int failedCount = 0;



    private final Map<String, String> ChallengeMission = Map.of(
            "LEVEL1", "BPM 70-80",
            "LEVEL2", "BPM 80-90",
            "LEVEL3", "BPM 90-100",
            "LEVEL4", "BPM 100-110",
            "LEVEL5", "BPM 110-120",
            "-1", "2곡 뛰기\n휴식 없음",
            "-2", "2곡 뛰고\n1곡 쉬고",
            "-3", "3곡 뛰고\n1곡 쉬고",
            "-4", "3곡 뛰고\n1곡 쉬고"
    );

    Challenge(Context context, Activity activity, String targetLevel, String targetStage) {
        this.context = context;
        this.activity = activity;
        this.targetLevel = targetLevel;
        this.targetStage = targetStage;

        challengeLayout = activity.findViewById(R.id.challengeLayout);
        appTitle = activity.findViewById(R.id.title);
        stepTitle = activity.findViewById(R.id.stepTitle);
        musicTitle = activity.findViewById(R.id.musicTitle);
        challengeTitle = activity.findViewById(R.id.challengeTitle);
        todayMission = activity.findViewById(R.id.todayMission);
        todayLevel = activity.findViewById(R.id.todayLevel);
        todayStage = activity.findViewById(R.id.todayStage);
        stepsPerMinuteView = activity.findViewById(R.id.stepsPerMinuteView);
        stepCountView = activity.findViewById(R.id.stepCountView);
        remainingGoal = activity.findViewById(R.id.remainingGoal);
        remainingGoalLabel = activity.findViewById(R.id.remainingGoalLabel);
        icon = activity.findViewById(R.id.level4_1);


    }

    public void enterChallengeMode(MusicSelector musicSel, StepMeasure step){
        challengeLayout.setVisibility(View.VISIBLE);
        appTitle.setTextColor(Color.parseColor("#F37E63"));
        stepTitle.setTextColor(Color.parseColor("#F37E63"));
        musicTitle.setTextColor(Color.parseColor("#F37E63"));
        stepCountView.setTextColor(Color.parseColor("#F37E63"));
        stepsPerMinuteView.setTextColor(Color.parseColor("#F37E63"));
        challengeTitle.setTextColor(Color.parseColor("#F37E63"));
        todayMission.setText(String.format("%s%s", targetLevel, targetStage));
        todayLevel.setText(ChallengeMission.get(targetLevel));
        todayStage.setText(ChallengeMission.get(targetStage));
        musicSelector = musicSel;
        stepMeasure = step;
        musicSelector.setChallenge(this);

        String iconID = "level"+targetLevel.charAt(5)+"_"+targetStage.charAt(1);
        int resID = context.getResources().getIdentifier(iconID, "id", context.getPackageName());
        icon = activity.findViewById(resID);

        setSongCount();
        resetInOutStep();
        startSong();
    }

    public void setSongCount(){
        switch(targetStage){
            case("-1"):
                songsLeft = 2;
                songsBeforeRest = -1;
                break;
            case("-2"):
                songsLeft = 5;
                songsBeforeRest = 2;
                break;
            case("-3"):
            case("-4"):
                songsLeft = 7;
                songsBeforeRest = 3;
                break;
        }
        refreshRemaining();
    }

    public void refreshRemaining(){
        if (songsBeforeRest > 0) {
            remainingGoalLabel.setText("다음 휴식까지");
            remainingGoal.setText(String.format(Locale.KOREA, "%d%s", songsBeforeRest, "곡"));
        }else{
            remainingGoalLabel.setText("챌린지 완료까지");
            remainingGoal.setText(String.format(Locale.KOREA, "%d%s", songsLeft, "곡"));
        }
    }

    public void exitChallengeMode(){
        challengeLayout.setVisibility(View.GONE);
        appTitle.setTextColor(Color.parseColor("#BEF2E4"));
        stepTitle.setTextColor(Color.parseColor("#BEF2E4"));
        musicTitle.setTextColor(Color.parseColor("#BEF2E4"));
        stepCountView.setTextColor(Color.parseColor("#BEF2E4"));
        stepsPerMinuteView.setTextColor(Color.parseColor("#BEF2E4"));
        challengeTitle.setTextColor(Color.parseColor("#BEF2E4"));
    }

    public void startSong(){
        musicSelector.setChallengeSongList(targetLevel);
        musicSelector.selectChallengeSong();
    }

    public void finishSong() {
        float successRate = (float) successCount / (float) (successCount + failedCount) * 100;
        if (songsBeforeRest == 0) { //방금 끝난 곡이 휴식 곡이었다면
            Toast.makeText(context, String.format(Locale.KOREA, "휴식 곡 종료"), Toast.LENGTH_SHORT).show();
        } else if (songsBeforeRest == 1) { //방금 끝난 곡이 휴식 직전 곡이었다면
            Toast.makeText(context, String.format(Locale.KOREA, "휴식 곡 시작"), Toast.LENGTH_SHORT).show();
        } else {
            verifyChallenge();
        }
        resetInOutStep();
        musicSelector.selectChallengeSong();
        songsLeft--;
        songsBeforeRest--;
        refreshRemaining();
        if (songsLeft == 0) {
            musicSelector.pauseSong();
            if (successRate >= CHALLENGE_LEVEL_CLEAR_PERCENTAGE) {
                icon.setImageResource(R.drawable.successicon);
                if (targetLevel == "LEVEL1" && targetStage == "-4") {
                    targetLevel = "LEVEL2";
                    targetStage = "-1";
                    targetBPM += 10;
                }
                else if(targetLevel == "LEVEL2" && targetStage == "-4"){
                    targetLevel = "LEVEL3";
                    targetStage = "-1";
                    targetBPM += 10;
                }
                else if(targetLevel == "LEVEL2" && targetStage == "-4") {
                    targetLevel = "LEVEL4";
                    targetStage = "-1";
                    targetBPM += 10;
                }
                else{
                    switch (targetStage){
                        case ("-1"):
                            targetStage = "-2";
                            break;
                        case ("-2"):
                            targetStage = "-3";
                            break;
                        case ("-3"):
                            targetStage = "-4";
                            break;

                    }
                }
                String iconID = "level"+targetLevel.charAt(5)+"_"+targetStage.charAt(1);
                int resID = context.getResources().getIdentifier(iconID, "id", context.getPackageName());
                icon = activity.findViewById(resID);
                todayMission.setText(String.format("%s%s", targetLevel, targetStage));
                todayLevel.setText(ChallengeMission.get(targetLevel));
                todayStage.setText(ChallengeMission.get(targetStage));
                setSongCount();
                resetInOutStep();
                //startSong();
            }
            else{
                icon.setImageResource(R.drawable.failedicon);
            }

        }
    }
    public void resetInOutStep(){
        stepsInBPM = 0;
        stepsOutBPM = 0;
    }

    public void watchStep(){
        if (songsBeforeRest == 0){ //현재 곡이 휴식 곡이라면
            stepsPerMinuteView.setTextColor(Color.parseColor("#F37E63"));
        }else{
            int stepsPerMinute = stepMeasure.getStepsPerMinute();
            if (Math.abs(targetBPM - stepsPerMinute) > BPM_RANGE){ //현재 걸음 수가 목표 BPM 범위 바깥이라면
                stepsPerMinuteView.setTextColor(Color.parseColor("#922B21")); //약간 더 주황색
                stepsOutBPM += 1;
            }else{
                stepsPerMinuteView.setTextColor(Color.parseColor("#F37E63"));
                stepsInBPM += 1;
            }
        }
    }

    public void verifyChallenge(){
        float score = (float)stepsInBPM / Math.max(1,(float)(stepsOutBPM + stepsInBPM)) * 100;
        if (score >= CHALLENGE_SUCCESS_PERCENTAGE){
            Toast.makeText(context, String.format(Locale.KOREA, "%.1f%s", score, "점: 성공"), Toast.LENGTH_SHORT).show();
            successCount += 1;
        }else{
            Toast.makeText(context, String.format(Locale.KOREA, "%.1f%s", score, "점: 실패"), Toast.LENGTH_SHORT).show();
            failedCount += 1;
        }
    }

}



