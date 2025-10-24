package com.example.tableahead;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.example.tableahead.search.SelectTypeScreen;

import java.util.Objects;

public class helpScreen extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_help_screen);

        //Toolbar
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        Objects.requireNonNull(getSupportActionBar()).setTitle("");
        ImageButton accountBtn = findViewById(R.id.accountBtn);
        accountBtn.setOnClickListener(c -> startActivity(new Intent(this, accountScreen.class)));
        ImageButton homeBtn = findViewById(R.id.homeBtn);
        homeBtn.setOnClickListener(c -> {
            Intent intent2 = new Intent(this, SelectTypeScreen.class);
            // Kill activity stack
            intent2.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
            startActivity(intent2);
            finish();
        });
        ImageButton helpBtn = findViewById(R.id.helpBtn);
        helpBtn.setOnClickListener(c -> startActivity(new Intent(this, helpScreen.class)));
        ImageButton backBtn = findViewById(R.id.backBtn);
        backBtn.setOnClickListener(c -> finish());

        //Button shows info about app
        Button aboutBtn = findViewById(R.id.aboutBtn);
        aboutBtn.setOnClickListener(c -> {
            AlertDialog aboutDialog = new AlertDialog.Builder(helpScreen.this).create();
            aboutDialog.setTitle(R.string.about);
            aboutDialog.setMessage(getString(R.string.appInfo));
            aboutDialog.setButton(AlertDialog.BUTTON_POSITIVE, getString(R.string.okay), (dialog, which) -> aboutDialog.dismiss());
            aboutDialog.show();
        });

        //Launches tutorial dialog
        Button tutBtn = findViewById(R.id.tutBtn);
        tutBtn.setOnClickListener(x -> {
            TutDialog dialog = new TutDialog(this);
            dialog.show();
        });
    }

    public static class TutDialog extends Dialog implements android.view.View.OnClickListener {

        // --Commented out by Inspection (11/26/2023 4:42 PM):public final Activity act;
        public TextView tutText;
        public Button back, close, next;
        private int page, totalPages; //remember total pages is totalPages - 1 actually

        public TutDialog(Activity act) {
            super(act);
        }

        @Override
        protected void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            requestWindowFeature(Window.FEATURE_NO_TITLE);
            setContentView(R.layout.tutorial);
            back = findViewById(R.id.tutBack);
            close = findViewById(R.id.tutClose);
            next = findViewById(R.id.tutNext);
            back.setOnClickListener(this);
            close.setOnClickListener(this);
            next.setOnClickListener(this);
            tutText = findViewById(R.id.tutText);
            //Set default text
            tutText.setText(R.string.tut0);
            page = 0;
            totalPages = 8; //UPDATE AS NEEDED
        }

        @Override
        public void onClick(View view) {
            if(view.getId() == R.id.tutBack) {
                page = Math.max(0, page - 1);
            } else if(view.getId() == R.id.tutClose) {
                dismiss();
            } else if(view.getId() == R.id.tutNext) {
                page = Math.min(totalPages, page + 1);
            }
            //Each case is a page for the tutorial dialog
            switch(page) {
                case 0:
                    tutText.setText(R.string.tut0);
                    break;
                case 1:
                    tutText.setText(R.string.tut1);
                    break;
                case 2:
                    tutText.setText(R.string.tut2);
                    break;
                case 3:
                    tutText.setText(R.string.tut3);
                    break;
                case 4:
                    tutText.setText(R.string.tut4);
                    break;
                case 5:
                    tutText.setText(R.string.tut5);
                    break;
                case 6:
                    tutText.setText(R.string.tut6);
                    break;
                case 7:
                    tutText.setText(R.string.tut7);
                    break;
                case 8:
                    tutText.setText(R.string.tut8);
                    break;
            }
        }
    }
}