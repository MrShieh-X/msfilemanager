package com.mrshiehx.file.manager;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

public class a extends AppCompatActivity implements View.OnClickListener {
    private TextView content;
    private EditText input;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        content = (TextView) findViewById(R.id.conteant);
        input = (EditText) findViewById(R.id.input);
        Button button = (Button) findViewById(R.id.button);
        button.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        content.setText(getShellResult());
    }

    public String getShellResult() {
        String result = "";
        Runtime mRuntime = Runtime.getRuntime();
        try {
            Process mProcess = mRuntime.exec(input.getText().toString().trim());
            InputStream is = mProcess.getInputStream();
            InputStreamReader isr = new InputStreamReader(is);
            BufferedReader mReader = new BufferedReader(isr);
            String string;
            while ((string = mReader.readLine()) != null) {
                result = result + string + "\n";
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return result;
    }
}
