package com.example.night;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.bottomnavigation.BottomNavigationView;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

public class DisplayNote extends AppCompatActivity {
    private NotesDb mydb;
    String my_note="My Notes";
    String editNote_t="Edit Note";
    private MainActivity mainActivity;
    EditText note;
    /*TextView note_text_view_v;*/
    TextView datetime;
    Menu fav_i;
    BottomNavigationView bottomNavigationView;
    int id_To_Update;
    int value_f_b;
    int state=1;


    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.display_note);
        setTitle(my_note);
        /*note_text_view_v=findViewById(R.id.note_text_view);*/
        note=findViewById(R.id.note_view);
        bottomNavigationView=findViewById(R.id.bottom_nav_view);
        bottomNavigationView.setOnNavigationItemSelectedListener(navigationItemSelectedListener);
        datetime=findViewById(R.id.date_time_view);
        fav_i=bottomNavigationView.getMenu();
        mydb=new NotesDb(DisplayNote.this);
        note.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if(hasFocus){
                    setTitle(editNote_t);
                }

            }
        });
        Bundle extras=getIntent().getExtras();
        if(extras!=null){
            value_f_b=extras.getInt("id");
            if(value_f_b==-3){
                id_To_Update=0;
            }
            else if(value_f_b>0){
                id_To_Update=value_f_b;
            }
            if (id_To_Update > 0) {
                Cursor rs = mydb.getData(id_To_Update);
                rs.moveToFirst();
                state=rs.getInt(rs.getColumnIndex(NotesDb.State_C));
                String contents = rs.getString(rs.getColumnIndex(NotesDb.Notes_C));
                String date = rs.getString(rs.getColumnIndex(NotesDb.Date_C));
                String time = rs.getString(rs.getColumnIndex(NotesDb.Time_C));
                String date_time_v=date+time;
                if (!rs.isClosed()) {
                    rs.close();
                }
              /*  note_text_view_v.setText(contents);*/
                datetime.setText(date_time_v);
                note.setText(contents);
                changeFavIcon(id_To_Update);
            }
            else {
                if(value_f_b==-3){
                    String data= extras.getString("data");
                    note.setText(data);
                }
                Calendar c = Calendar.getInstance();
                SimpleDateFormat tf=new SimpleDateFormat(" hh:mm a", Locale.US);
                String timeString=tf.format(c.getTime());
                SimpleDateFormat df = new SimpleDateFormat(" ddMMM",Locale.US);
                String dateString= df.format(c.getTime());
                String date_time_v=dateString+timeString;
                datetime.setText(date_time_v);

            }


        }

    }
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            getMenuInflater().inflate(R.menu.save_menu, menu);

        }
        return true;
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        MenuItem restore=menu.findItem(R.id.retore);
        if(state==3){
            restore.setVisible(true);
            return true;
        }
        return super.onPrepareOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if(item.getItemId()==android.R.id.home){
            onBackPressed();
            return true;
        }
        if(item.getItemId()==R.id.save_){
            setTitle(my_note);
            note.onEditorAction(EditorInfo.IME_ACTION_DONE);
            saveNote();
            return true;
        }
        if(item.getItemId()==R.id.retore){
            mydb.changeState(id_To_Update,1);
            Toast.makeText(DisplayNote.this,"note restored",Toast.LENGTH_SHORT).show();
            Intent intent=new Intent(DisplayNote.this,MainActivity.class);
            startActivity(intent);
            finish();

        }
        return super.onOptionsItemSelected(item);

    }

    BottomNavigationView.OnNavigationItemSelectedListener navigationItemSelectedListener=new
            BottomNavigationView.OnNavigationItemSelectedListener() {
                @RequiresApi(api = Build.VERSION_CODES.KITKAT)
                @Override
                public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                    switch (item.getItemId()){
                        case R.id.fav_view:
                           if(id_To_Update==0 ){
                               Toast.makeText(DisplayNote.this,"first save the note",Toast.LENGTH_SHORT).show();
                           }
                           else if(mydb.getState(id_To_Update)==1 || mydb.getState(id_To_Update)==3){
                               if(mydb.changeState(id_To_Update,2)){
                                   changeFavIcon(id_To_Update);
                                   Toast.makeText(DisplayNote.this,"added to favourites",Toast.LENGTH_SHORT).show();
                               }
                           }
                           else  if(mydb.getState(id_To_Update)==2){
                               if(mydb.changeState(id_To_Update,1)){
                                   changeFavIcon(id_To_Update);
                                   Toast.makeText(DisplayNote.this,"removed from favourites",Toast.LENGTH_SHORT).show();
                               }
                           }
                            return true;
                        case R.id.share_view:
                            if(note.getText().toString().trim().equals("")){
                                Toast.makeText(DisplayNote.this,"Can't Send Empty Note",Toast.LENGTH_SHORT).show();
                            }
                            else if(id_To_Update==0){
                                Toast.makeText(DisplayNote.this,"please save before sharing",Toast.LENGTH_SHORT).show();
                            }
                            else {
                                Intent send=new Intent();
                                send.setAction(Intent.ACTION_SEND);
                                send.putExtra(Intent.EXTRA_TEXT,note.getText().toString());
                                send.setType("text/plain");
                                startActivity(Intent.createChooser(send,"Share"));
                            }
                            return true;
                        case R.id.delete_view:
                            if(id_To_Update==0){
                                Toast.makeText(DisplayNote.this,"not saved yet",Toast.LENGTH_LONG).show();
                            }
                            else if(mydb.getState(id_To_Update)==1 || mydb.getState(id_To_Update)==2){
                                mydb.changeState(id_To_Update,3);
                                Toast.makeText(DisplayNote.this,"moved to deleted notes",Toast.LENGTH_LONG).show();
                                if(value_f_b==-3 || value_f_b==-1){
                                    finishAffinity();
                                    System.exit(0);
                                }
                                Intent intent=new Intent(DisplayNote.this,MainActivity.class);
                                startActivity(intent);
                                finish();

                            }
                            else if(mydb.getState(id_To_Update)==3){
                                AlertDialog.Builder builder = new AlertDialog.Builder(DisplayNote.this);
                                builder.setMessage("You can't recover again");
                                builder.setIcon(R.drawable.delete_forever);
                                builder.setTitle("Delete note");
                                builder.setPositiveButton("Delete", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        if(mydb.deleteNotes_per(id_To_Update)>0){
                                            note.setText("");
                                            if(value_f_b==-3){
                                                finishAffinity();
                                                System.exit(0);
                                            }
                                            Intent intent=new Intent(DisplayNote.this,MainActivity.class);
                                            startActivity(intent);
                                            finish();
                                        }
                                    }
                                });
                                builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        dialog.cancel();
                                    }
                                });
                                final AlertDialog alertDialog=builder.create();
                                alertDialog.setOnShowListener(new DialogInterface.OnShowListener() {
                                    @Override
                                    public void onShow(DialogInterface dialog) {
                                        alertDialog.getButton(AlertDialog.BUTTON_NEGATIVE).setGravity(Gravity.START);
                                        alertDialog.getButton(AlertDialog.BUTTON_POSITIVE).setGravity(Gravity.END);
                                        alertDialog.getButton(AlertDialog.BUTTON_NEGATIVE).setTextColor(Color.GRAY);
                                        alertDialog.getButton(AlertDialog.BUTTON_POSITIVE).setTextColor(Color.BLACK);

                                    }
                                });
                                alertDialog.show();
                            }
                            return true;
                    }
                    return false;
                }
            };

    @Override
    public void onBackPressed() {
        if(!note.getText().toString().trim().equals("")){
            saveNote();
            if(value_f_b>=0){
                Intent intent = new Intent(getApplicationContext(),MainActivity.class);
                startActivity(intent);
            }
            finish();
        }
        else {
            AlertDialog.Builder builder = new AlertDialog.Builder(DisplayNote.this);
            builder.setMessage("You can't save empty note!!!");
            builder.setTitle("Exit from editor");
            builder.setPositiveButton("Exit", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    if(value_f_b>=0) {
                        Intent intent = new Intent(DisplayNote.this, MainActivity.class);
                        startActivity(intent);
                    }
                    finish();
                }
            });
            builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    dialog.cancel();
                }
            });
            final AlertDialog alertDialog=builder.create();
            alertDialog.setOnShowListener(new DialogInterface.OnShowListener() {
                @Override
                public void onShow(DialogInterface dialog) {
                    alertDialog.getButton(AlertDialog.BUTTON_NEGATIVE).setGravity(Gravity.START);
                    alertDialog.getButton(AlertDialog.BUTTON_POSITIVE).setGravity(Gravity.END);
                    alertDialog.getButton(AlertDialog.BUTTON_NEGATIVE).setTextColor(Color.GRAY);
                    alertDialog.getButton(AlertDialog.BUTTON_POSITIVE).setTextColor(Color.BLACK);

                }
            });
                /*if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
                    Objects.requireNonNull(alertDialog.getWindow()).getAttributes().gravity= BOTTOM;
                }*/
            alertDialog.show();
        }
    }
    public void saveNote(){
        Calendar c = Calendar.getInstance();
        SimpleDateFormat tf=new SimpleDateFormat(" hh:mm a", Locale.US);
        String timeString=tf.format(c.getTime());
        SimpleDateFormat df = new SimpleDateFormat(" ddMMM",Locale.US);
        String dateString= df.format(c.getTime());
        if (id_To_Update> 0) {
            if (note.getText().toString().trim().equals("")) {
                Toast.makeText(this,"Can't save empty note",Toast.LENGTH_SHORT).show();
            }
            else {
                if(note.getText().toString().equals(mydb.getNote(id_To_Update))){
                }
                else{
                    if (mydb.updateNotes(id_To_Update, note.getText()
                            .toString(), timeString,dateString)) {
                        Toast.makeText(this,"updated successfully",Toast.LENGTH_SHORT).show();


                    } else {
                        Toast.makeText(this,"can't update",Toast.LENGTH_SHORT).show();
                    }
                }
            }
        }
        else {
            if (note.getText().toString().trim().equals("")) {
                Toast.makeText(this,"Can't save empty note",Toast.LENGTH_SHORT).show();

            } else {
                if (mydb.insertNotes(note.getText().toString(), timeString, dateString)) {
                    id_To_Update= mydb.getMaxId();
                    Toast.makeText(this,"saved successfully",Toast.LENGTH_SHORT).show();
                }
                else {
                    Toast.makeText(this,"can't save",Toast.LENGTH_SHORT).show();

                }
            }
        }
        /*mainActivity.refreshList();*/
    }


    public void changeFavIcon(int id){
        if (id > 0) {
            if(mydb.getState(id)==2){
                fav_i.findItem(R.id.fav_view).setIcon(R.drawable.fav_ful);
            }
            else {
                fav_i.findItem(R.id.fav_view).setIcon(R.drawable.star_b );
            }

        }
    }

}




