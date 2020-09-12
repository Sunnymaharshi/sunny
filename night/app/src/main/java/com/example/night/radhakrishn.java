package com.example.night;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

public class radhakrishn extends AppCompatActivity {
    EditText rk;
    ImageView f,l,k;
    LinearLayout linearLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_radhakrishn);
        f=findViewById(R.id.f_i);
        linearLayout=findViewById(R.id.rk_linear);
        l=findViewById(R.id.l_i);
        k=findViewById(R.id.rk_i);
        rk=findViewById(R.id.rk_t);
        f.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                Bundle dataBundle = new Bundle();
                dataBundle.putInt("id", -1);
                Intent intent = new Intent(getApplicationContext(),
                        DisplayNote.class);
                intent.putExtras(dataBundle);
                startActivity(intent);
                finish();
                return true;
            }
        });
        l.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                rk.setVisibility(View.VISIBLE);
                rk.setFocusable(true);
                rk.setClickable(true);
                return true;
            }

        });

        k.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String s=rk.getText().toString().trim();
                InputMethodManager inputMethodManager=(InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
                if (inputMethodManager != null) {
                    inputMethodManager.hideSoftInputFromWindow(v.getWindowToken(),0);
                }
                if(s.equalsIgnoreCase("Sunny")||s.equalsIgnoreCase("buddaa")||s.equalsIgnoreCase("potti")||s.equalsIgnoreCase("pette")||s.equalsIgnoreCase("chinnodaa")){
                    Intent intent=new Intent(radhakrishn.this,MainActivity.class);
                    startActivity(intent);
                    finish();
                }
            }
        });
        rk.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                InputMethodManager inputMethodManager=(InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
                if (inputMethodManager != null) {
                    inputMethodManager.toggleSoftInput(InputMethodManager.SHOW_FORCED,0);
                }
            }
        });



    }
}
