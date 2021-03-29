package com.example.night;

import android.content.Intent;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

public class recieveInfo extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Intent intent = getIntent();
        String action = intent.getAction();
        String type=intent.getType();
        if(Intent.ACTION_SEND.equals(action) && type!=null){
            if("text/plain".equals(type)){
                handleSendText(intent);
            }
        }

    }
    public void handleSendText(Intent intent){
        String string=intent.getStringExtra(Intent.EXTRA_TEXT);
        if(string != null){
            Bundle dataBundle = new Bundle();
            dataBundle.putInt("id", -3);
            dataBundle.putString("data",string);
            Intent intents = new Intent(recieveInfo.this,
                    DisplayNote.class);
            intents.putExtras(dataBundle);
            startActivity(intents);
            finish();
        }


    }
}
