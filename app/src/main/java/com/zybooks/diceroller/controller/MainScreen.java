package com.zybooks.diceroller.controller;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;

import com.google.android.material.button.MaterialButton;
import com.zybooks.diceroller.R;

public class MainScreen extends AppCompatActivity {
    private MaterialButton begin;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_screen);

        begin = findViewById(R.id.begin_button);

        begin.setOnClickListener(view -> {
            Intent intent = new Intent(this, MainActivity.class);
            startActivity(intent);
        });
    }
}