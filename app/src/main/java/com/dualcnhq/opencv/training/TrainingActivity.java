package com.dualcnhq.opencv.training;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.Toast;

import com.dualcnhq.opencv.R;

public class TrainingActivity extends AppCompatActivity {

    public static final String NAME_TAG = "nameTag";
    public static final String TWITTER_TAG = "twitterTag";

    private EditText nameEditText;
    private EditText twitterEditText;
    private Button startTrainingBtn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_training);

        nameEditText = (EditText) findViewById(R.id.name);
        twitterEditText = (EditText) findViewById(R.id.twitter);
        startTrainingBtn = (Button) findViewById(R.id.submitBtn);
        startTrainingBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (isInputValid()) {
                    Intent intent = new Intent(getApplicationContext(), TrainingImageActivity.class);
                    intent.putExtra(NAME_TAG, nameEditText.getText().toString());
                    intent.putExtra(TWITTER_TAG, twitterEditText.getText().toString());
                    startActivity(intent);
                } else {
                    Toast.makeText(getApplicationContext(), "Invalid Input", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private boolean isInputValid() {
        return nameEditText.getText() != null && twitterEditText.getText() != null;
    }
}
