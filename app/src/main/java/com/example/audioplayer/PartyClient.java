package com.example.audioplayer;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import java.util.List;

public class PartyClient extends AppCompatActivity {
    private List<Music> list;
    private EditText serverAddressEditText;
    private Button connectButton;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_client);

        Bundle bundle = getIntent().getExtras();
        //list = bundle.getParcelableArrayList("playlist");

        serverAddressEditText = findViewById(R.id.serverAddressEditText);
        connectButton = findViewById(R.id.connectButton);

        connectButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String serverAddress = serverAddressEditText.getText().toString();
            }
        });
    }
}