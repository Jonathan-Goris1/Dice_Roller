package com.zybooks.diceroller.controller;

import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.view.ContextMenu;
import android.view.GestureDetector;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.core.view.GestureDetectorCompat;

import com.zybooks.diceroller.R;
import com.zybooks.diceroller.model.Dice;
import com.zybooks.diceroller.model.RollLengthDialogFragment;


public class MainActivity extends AppCompatActivity implements RollLengthDialogFragment.OnRollLengthSelectedListener {

    public static final int MAX_DICE = 3;

    private int mVisibleDice;
    private Dice[] mDice;
    private ImageView[] mDiceImageViews;
    private Menu mMenu;
    private CountDownTimer mTimer;
    private TextView totalSum;
    private TextView highScore;

    private int total = 0;
    private int Score = 0;

    private long mTimerLength = 2000;

    Boolean oneDice = false;
    Boolean twoDice = false;
    Boolean threeDice = false;

    private int mCurrentDie;

    private int mInitX;
    private int mInitY;

    private GestureDetectorCompat mDetector;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mDetector = new GestureDetectorCompat(this, new DiceGestureListener());

        // Create an array of Dice
        mDice = new Dice[MAX_DICE];
        for (int i = 0; i < MAX_DICE; i++) {
            mDice[i] = new Dice(i + 1);
        }

        totalSum = findViewById(R.id.total_text);
        highScore = findViewById(R.id.highscore_text);

        // Create an array of ImageViews
        mDiceImageViews = new ImageView[MAX_DICE];
        mDiceImageViews[0] = findViewById(R.id.dice1);
        mDiceImageViews[1] = findViewById(R.id.dice2);
        mDiceImageViews[2] = findViewById(R.id.dice3);

        // Register context menus for all dice and tag each die
        for (int i = 0; i < mDiceImageViews.length; i++) {
            registerForContextMenu(mDiceImageViews[i]);
            mDiceImageViews[i].setTag(i);
        }

        // Moving finger left or right changes dice number
        mDiceImageViews[0].setOnTouchListener((v, event) -> {
            int action = event.getAction();
            switch (action) {
                case MotionEvent.ACTION_DOWN:
                    mInitX = (int) event.getX();
                    mInitY = (int) event.getY();
                    return true;
                case MotionEvent.ACTION_MOVE:
                    int x = (int) event.getX();
                    int y = (int) event.getY();

                    // See if movement is at least 20 pixels
                    if (Math.abs(x - mInitX) >= 20) {
                        if (x > mInitX) {
                            mDice[0].addOne();
                        }
                        else {
                            mDice[0].subtractOne();
                        }
                        showDice();
                        mInitX = x;
                    }

                    if (Math.abs(y - mInitY) >= 20) {
                        if (y > mInitY) {
                            mDice[0].addOne();
                        }
                        else {
                            mDice[0].subtractOne();
                        }
                        showDice();
                        mInitY = y;
                    }

                    return true;
            }
            return false;
        });

        // All dice are initially visible
        mVisibleDice = MAX_DICE;

        showDice();
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        mDetector.onTouchEvent(event);
        return super.onTouchEvent(event);
    }

    private class DiceGestureListener extends GestureDetector.SimpleOnGestureListener {

        @Override
        public boolean onDown(MotionEvent event) {
            return true;
        }

        @Override
        public boolean onDoubleTap(MotionEvent e) {
            mDice[0].addOne();
            showDice();
            Toast.makeText(MainActivity.this, "On Double Tap", Toast.LENGTH_SHORT).show();
            return super.onDoubleTap(e);
        }


        @Override
        public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
            if (velocityX > 0) {
                rollDice();
            }
            return true;
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        mMenu = menu;
        return super.onCreateOptionsMenu(menu);
    }

    private void showDice() {
        // Display only the number of dice visible
        threeDice = true;
        for (int i = 0; i < mVisibleDice; i++) {
            Drawable diceDrawable = ContextCompat.getDrawable(this, mDice[i].getImageId());
            mDiceImageViews[i].setImageDrawable(diceDrawable);
            mDiceImageViews[i].setContentDescription(Integer.toString(mDice[i].getNumber()));
        }
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {

        // Determine which menu option was chosen
        if (item.getItemId() == R.id.action_one) {
            changeDiceVisibility(1);
            showDice();
            oneDice = true;
            return true;
        } else if (item.getItemId() == R.id.action_two) {
            changeDiceVisibility(2);
            showDice();
            twoDice = true;
            return true;
        } else if (item.getItemId() == R.id.action_three) {
            changeDiceVisibility(3);
            showDice();
            return true;
        } else if (item.getItemId() == R.id.action_stop) {
            mTimer.cancel();
            item.setVisible(false);
            getTotalResult();
            return true;
        } else if (item.getItemId() == R.id.action_roll) {
            rollDice();
            total = 0;

            return true;
        } else if (item.getItemId() == R.id.action_roll_length) {
            RollLengthDialogFragment dialog = new RollLengthDialogFragment();
            dialog.show(getSupportFragmentManager(), "rollLengthDialog");
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onCreateContextMenu(ContextMenu menu, View v,
                                    ContextMenu.ContextMenuInfo menuInfo) {

        super.onCreateContextMenu(menu, v, menuInfo);
        mCurrentDie = (int) v.getTag();   // Which die is selected?
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.context_menu, menu);
    }

    @Override
    public boolean onContextItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.add_one) {
            mDice[mCurrentDie].addOne();
            showDice();
            total++;
            updateTotal();
            return true;
        } else if (item.getItemId() == R.id.subtract_one) {
            mDice[mCurrentDie].subtractOne();
            showDice();
            total--;
            updateTotal();
            return true;
        } else if (item.getItemId() == R.id.roll) {
            rollDice();
            return true;
        }

        return super.onContextItemSelected(item);
    }

    private void rollDice() {
        total = 0;
        mMenu.findItem(R.id.action_stop).setVisible(true);

        if (mTimer != null) {
            mTimer.cancel();
        }

        mTimer = new CountDownTimer(mTimerLength, 100) {
            public void onTick(long millisUntilFinished) {
                for (int i = 0; i < mVisibleDice; i++) {
                    mDice[i].roll();
                }
                showDice();
            }

            public void onFinish() {
                mMenu.findItem(R.id.action_stop).setVisible(false);
                getTotalResult();

            }
        }.start();

    }

    private void updateTotal() {
        totalSum.setText("Total: " + total);
    }

    private void updateScore(){
        highScore.setText("High Score: " + Score);
    }

    private void getTotalResult() {
        for (int i = 0; i < mVisibleDice; i++) {
            total += mDice[i].getNumber();
        }
        updateTotal();
        checkWinCondition();

    }

    private void checkWinCondition(){
        if (total % 2 == 0 && oneDice) {
            Toast.makeText(MainActivity.this, "You Win", Toast.LENGTH_SHORT).show();
            Score++;
            updateScore();
        } else if ((total == 7 || total == 11) && twoDice) {
            Toast.makeText(MainActivity.this, "You Win", Toast.LENGTH_SHORT).show();
            Score++;
            updateScore();
        } else if ((total % 7 == 0 || total % 11 == 0) && threeDice) {
            Toast.makeText(MainActivity.this, "You Win", Toast.LENGTH_SHORT).show();
            Score++;
            updateScore();
        } else if ((total == 2 || total == 12) && twoDice) {
            Toast.makeText(MainActivity.this, "You Lose", Toast.LENGTH_SHORT).show();
            updateScore();
            Score++;
        } else if ((total == 3 || total == 18) && twoDice) {
            Toast.makeText(MainActivity.this, "You Lose", Toast.LENGTH_SHORT).show();
            updateScore();
            Score++;
        } else {
            Toast.makeText(MainActivity.this, "You Lose", Toast.LENGTH_SHORT).show();
        }
    }

    private void changeDiceVisibility(int numVisible) {
        mVisibleDice = numVisible;

        // Make dice visible
        for (int i = 0; i < numVisible; i++) {
            mDiceImageViews[i].setVisibility(View.VISIBLE);
        }

        // Hide remaining dice
        for (int i = numVisible; i < MAX_DICE; i++) {
            mDiceImageViews[i].setVisibility(View.GONE);
        }
    }

    @Override
    public void onRollLengthClick(int which) {
        mTimerLength = 1000L * (which + 1);

    }
}