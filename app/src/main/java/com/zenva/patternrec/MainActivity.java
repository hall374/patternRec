package com.zenva.patternrec;

import android.util.SparseArray;
import android.util.SparseIntArray;
import android.animation.ArgbEvaluator;
import android.animation.ObjectAnimator;
import android.content.Context;
import android.content.SharedPreferences;
import android.media.MediaPlayer;
import android.os.Handler;
import android.os.PowerManager;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;

public class MainActivity extends AppCompatActivity {

    // Map for highlight colors
    private SparseIntArray highlightColors;
    // Map for original colors
    private SparseIntArray originalColors;
    // Map for button sounds
    private SparseArray<MediaPlayer> buttonSounds;

    // 500 millisecond delay for color transition
    private int highlightDuration = 500;

    // Delay between highlights during pattern display
    private int displayDelay = 500;

    // For enabling disabling touch on color buttons
    private Boolean isToucheable = false;

    // For enabling/disabline sound
    private Boolean silentMode = false;

    // Enum for handling this stupid fucking threading bullshit
    private enum GameStep{
        START,
        PATTERN_DISPLAY,
        PATTERN_INPUT
    };

    // Pattern object
    Pattern myPattern;

    // Current level (used for score)
    int currentLevel = 5;

    // Yes...this is messy, but allows for instant list creation like in C#
    final ArrayList<Integer> buttonIDs = new ArrayList<>(Arrays.asList(new Integer[] {
            R.id.btnColor0,
            R.id.btnColor1,
            R.id.btnColor2,
            R.id.btnColor3
    }));

    // For sharedPreferences
    private static final String HIGH_SCORE = "high_score";

    PowerManager powerManager = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Set the powerManager
        powerManager = (PowerManager)this.getSystemService(Context.POWER_SERVICE);

        //Hide title bar
        getSupportActionBar().hide();

        // Initialize hashmap for linking buttons to highlight colors
        initMaps();

        // Display high score
        displayHighScore();
    }

    public void colorButtonOnClick(View view){
        if(isToucheable) {
            highlightButton(view);
            myPattern.addToInPtrn(buttonIDs.indexOf(view.getId()));
            checkComplete();
        }
    }

    public void startGame(View view){
        view.setVisibility(View.INVISIBLE);
        runGame(GameStep.START);
    }

    public void runGame(GameStep step){
        switch(step){
            case START:
                displayScore();
                displayLevel();
                displayHighScore();

                Toast.makeText(this, "Get Ready!", Toast.LENGTH_SHORT).show();

                isToucheable = false;

                displayCountDown(3);
                break;
            case PATTERN_DISPLAY:
                myPattern = new Pattern();
                myPattern.generatePattern(currentLevel);

                displayPattern(myPattern.getGeneratedPattern());
                break;
            case PATTERN_INPUT:
                Toast.makeText(this, "Match the Pattern", Toast.LENGTH_SHORT).show();

                isToucheable = true;
                break;
        }
    }

    // Determine whether to display animation or not
    // Animations don't work in power saver
    public void highlightButton(View toHighlight) {
        if(!silentMode) {
            final MediaPlayer currentButtonSound = buttonSounds.get(toHighlight.getId());
            currentButtonSound.seekTo(0);
            currentButtonSound.start();
        }
        if (!powerManager.isPowerSaveMode())
            highlightButtonNormal(toHighlight);
        else
            highlightButtonPowerSaver(toHighlight);
    }

    // Highlight button on click
    public void highlightButtonNormal(View toHighlight){
        ObjectAnimator highlightAnimator = ObjectAnimator.ofObject(toHighlight,
                "backgroundColor",
                new ArgbEvaluator(),
                originalColors.get(toHighlight.getId()),
                highlightColors.get(toHighlight.getId()));
        ObjectAnimator returnToOriginalAnimator = ObjectAnimator.ofObject(toHighlight,
                "backgroundColor",
                new ArgbEvaluator(),
                highlightColors.get(toHighlight.getId()),
                originalColors.get(toHighlight.getId()));
        highlightAnimator.setDuration(highlightDuration);
        highlightAnimator.start();
        returnToOriginalAnimator.setDuration(highlightDuration);
        returnToOriginalAnimator.start();
    }

    // Highlight button in power saver
    public void highlightButtonPowerSaver(final View toHighlight){
        new Handler().postDelayed(new Runnable(){
           @Override
           public void run(){
               toHighlight.setBackgroundColor(highlightColors.get(toHighlight.getId()));
               new Handler().postDelayed(new Runnable(){
                   @Override
                   public void run(){
                       toHighlight.setBackgroundColor(originalColors.get(toHighlight.getId()));
                   }
               }, displayDelay/2);
           }
        }, displayDelay/2);
    }

    // Toggle silent mode
    public void toggleSilentMode(View toggle){
        silentMode = !silentMode;
        if (silentMode) toggle.setBackgroundColor(getResources().getColor(R.color.blockHighlightBlue));
        else toggle.setBackgroundColor(0);
    }

    // Initialize map for highlight color based on ID
    private void initMaps(){
        highlightColors = new SparseIntArray();
        highlightColors.put(R.id.btnColor0, getResources().getColor(R.color.blockHighlightBlue));
        highlightColors.put(R.id.btnColor1, getResources().getColor(R.color.blockHighlightRed));
        highlightColors.put(R.id.btnColor2, getResources().getColor(R.color.blockHighlightPurple));
        highlightColors.put(R.id.btnColor3, getResources().getColor(R.color.blockHighlightGreen));

        originalColors = new SparseIntArray();
        originalColors.put(R.id.btnColor0, getResources().getColor(R.color.blockDefaultBlue));
        originalColors.put(R.id.btnColor1, getResources().getColor(R.color.blockDefaultRed));
        originalColors.put(R.id.btnColor2, getResources().getColor(R.color.blockDefaultPurple));
        originalColors.put(R.id.btnColor3, getResources().getColor(R.color.blockDefaultGreen));

        buttonSounds = new SparseArray<MediaPlayer>();
        buttonSounds.put(R.id.btnColor0, MediaPlayer.create(this, R.raw.block0));
        buttonSounds.put(R.id.btnColor1, MediaPlayer.create(this, R.raw.block1));
        buttonSounds.put(R.id.btnColor2, MediaPlayer.create(this, R.raw.block2));
        buttonSounds.put(R.id.btnColor3, MediaPlayer.create(this, R.raw.block3));
    }

    // Display pattern
    private void displayPattern(final int[] generatedPattern){
        displayPattern(generatedPattern, 0);
    }

    private void displayPattern(final int[] generatedPattern, final int i) {
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                if(i < generatedPattern.length) {
                    Button button = (Button) findViewById(buttonIDs.get(generatedPattern[i]));

                    //initiate the button
                    highlightButton(button);
                    button.setPressed(true);
                    button.invalidate();
                    button.setPressed(false);
                    button.invalidate();
                    displayPattern(generatedPattern, i + 1);
                }
                else{
                    runGame(GameStep.PATTERN_INPUT);
                }
            }
        }, displayDelay);

    }

    private void displayCountDown(final int count){
        final TextView tv = (TextView)findViewById(R.id.tvCountDown);
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                if(count > 0) {
                    tv.setText(Integer.toString(count));
                    displayCountDown(count - 1);
                }
                else{
                    tv.setText("");
                    runGame(GameStep.PATTERN_DISPLAY);
                }
            }
        }, 1000);
    }

    private void checkComplete(){
        if(myPattern.matchLen()){
            if (myPattern.isMatch()){
                Toast.makeText(this, "You win!", Toast.LENGTH_SHORT).show();
                currentLevel++;
                setHighScore(currentLevel-5);
                new Handler().postDelayed(new Runnable(){
                    @Override
                    public void run(){
                        runGame(GameStep.START);
                    }
                }, 2000);
            }
            else {
                Toast.makeText(this, "You lost! HAHAHA", Toast.LENGTH_SHORT).show();
                if (currentLevel > 1)
                    currentLevel--;
                Button startButton = (Button)findViewById(R.id.btnStart);
                startButton.setVisibility(View.VISIBLE);
                startButton.setEnabled(true);
            }
            displayScore();
            displayLevel();
        }
    }

    private void displayScore(){
        TextView score = (TextView)findViewById(R.id.tvScore);
        score.setText("SCORE: " + Integer.toString(currentLevel-5));
    }

    private void displayLevel(){
        TextView level = (TextView)findViewById(R.id.tvLevel);
        level.setText("LEVEL: " + Integer.toString(currentLevel));
    }

    // Return true if new high score is set
    private Boolean setHighScore(int score){
        if(loadHighScore() < score) {
            SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
            SharedPreferences.Editor editor = prefs.edit();
            editor.putInt(HIGH_SCORE, score);
            editor.apply();
            return true;
        }
        return false;
    }

    private int loadHighScore(){
        int score = PreferenceManager.getDefaultSharedPreferences(this).getInt(HIGH_SCORE, 0);
        return (score <= 0) ? 0 : score;
    }

    private void displayHighScore(){
        TextView tvHighScore = (TextView)findViewById(R.id.tvHighScore);
        tvHighScore.setText("HIGH SCORE: " + Integer.toString(loadHighScore()));
    }
}
