package com.example.night;

import android.app.AlarmManager;
import android.app.AlertDialog;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.text.util.Linkify;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.bottomnavigation.BottomNavigationView;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.Locale;

public class DisplayNote extends AppCompatActivity {
    private NotesDb mydb;
    String my_note="My Notes";
    String editNote_t="Edit Note";
    EditText note;
    TextView datetime;
    Menu fav_i;
    BottomNavigationView bottomNavigationView;
    int id_To_Update;
    int value_f_b;
    int state=1;
    Boolean is_spinTouch;
    String prev;
    Bundle prevstate=new Bundle();
    ImageView bell_off;
    NotificationManager mNotificationManager;
    public static final String NOTIFICATION_CHANNEL_ID="CS Notes:reminder notification channel";
    Bundle extras;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.display_note);
        setTitle(my_note);
        note=findViewById(R.id.note_view);
        bottomNavigationView=findViewById(R.id.bottom_nav_view);
        bottomNavigationView.setOnNavigationItemSelectedListener(navigationItemSelectedListener);
        datetime=findViewById(R.id.date_time_view);
        fav_i=bottomNavigationView.getMenu();
        bell_off=findViewById(R.id.remind_off);
        bell_off.setImageResource(R.drawable.ic_baseline_notifications_24);
        createNotificationChannel();
        mydb=new NotesDb(DisplayNote.this);
        final View dialogView=View.inflate(DisplayNote.this,R.layout.date_time_picker,null);
        final AlertDialog alertDialog=new AlertDialog.Builder(DisplayNote.this).create();
        dialogView.findViewById(R.id.set_btn_id).setOnClickListener(new View.OnClickListener() {
            @RequiresApi(api = Build.VERSION_CODES.M)
            @Override
            public void onClick(View v) {
                DatePicker datePicker=(DatePicker) dialogView.findViewById(R.id.date_picker_id);
                TimePicker timePicker=(TimePicker) dialogView.findViewById(R.id.time_picker_id);

                createRemind(new GregorianCalendar(datePicker.getYear(),datePicker.getMonth(),datePicker.getDayOfMonth(),
                        timePicker.getHour(),timePicker.getMinute()));
                alertDialog.dismiss();
            }
        });
        alertDialog.setView(dialogView);
        bell_off.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(id_To_Update>0){
                    if(mydb.getRemind(id_To_Update).equals("0")){
                        alertDialog.show();
                    }
                    else {
                        displayToast("Reminder set on\n"+mydb.getRemind(id_To_Update)+"\nLong click to cancel reminder",1);
                    }
                }
                else {
                    displayToast("Save the note before setting reminder",1);
                }
            }
        });
        bell_off.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                if (id_To_Update > 0) {
                    if (mydb.getRemind(id_To_Update).equals("0")) {
                        displayToast("Reminder Not Set",0);
                        return true;
                    } else {
                        AlarmManager alarmManager = (AlarmManager) getSystemService(ALARM_SERVICE);
                        PendingIntent pendingIntent = PendingIntent.getBroadcast(getApplicationContext(),
                                id_To_Update, new Intent(DisplayNote.this, BroadcastRec.class), PendingIntent.FLAG_NO_CREATE);
                        if (pendingIntent != null && alarmManager != null) {
                            alarmManager.cancel(pendingIntent);
                            bell_off.setImageResource(R.drawable.ic_baseline_notifications_24);
                            displayToast("Reminder set on\n" + mydb.getRemind(id_To_Update) + "\nis deleted",1);
                            mydb.updateRemind(id_To_Update, "0");
                            return true;
                        }
                    }
                }

                return false;
            }
        });
        final Spinner spinner= findViewById(R.id.spinner_ed);
        List<String> categories = new ArrayList<String>();
        categories.add("General");
        categories.add("Home");
        categories.add("Work");
        categories.add("Education");
        categories.add("Other");
        categories.add("Personal");
        ArrayAdapter<String> cat_data= new ArrayAdapter<String>(this, android.R.layout.simple_spinner_dropdown_item, categories);
        cat_data.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(cat_data);
        is_spinTouch=false;
        spinner.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                is_spinTouch =true;
                return false;
            }
        });
        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if(!is_spinTouch) return;
                switch (position){
                    case 0:
                        if(id_To_Update==0 ){
                            displayToast("Save the note",0);
                            spinner.setSelection(0);
                        }
                        else if(mydb.getState(id_To_Update)!=1){
                            if(mydb.changeState(id_To_Update,NotesDb.general)){
                                displayToast("added to General",0);
                            }
                        }
                        break;
                    case 1:
                        if(id_To_Update==0 ){
                            displayToast("Save the note",0);
                            spinner.setSelection(0);
                        }
                        else if(mydb.changeState(id_To_Update,NotesDb.home)){
                            displayToast("added to Home",0);
                        }
                        break;
                    case 2:
                        if(id_To_Update==0 ){
                            displayToast("Save the note",0);
                            spinner.setSelection(0);
                        }
                        else if(mydb.changeState(id_To_Update,NotesDb.work)){
                            displayToast("added to Work",0);
                        }
                        break;
                    case 3:
                        if(id_To_Update==0 ){
                            displayToast("Save the note",0);
                            spinner.setSelection(0);
                        }
                        else if(mydb.changeState(id_To_Update,NotesDb.education)){
                            displayToast("added to Education",0);
                        }
                        break;
                    case 4:
                        if(id_To_Update==0 ){
                            displayToast("Save the note",0);
                            spinner.setSelection(0);
                        }
                        else if(mydb.changeState(id_To_Update, NotesDb.other)){
                            displayToast("added to Other",0);
                        }
                        break;
                    case 5:
                        if(id_To_Update==0 ){
                            displayToast("Save the note",0);
                            spinner.setSelection(0);
                        }
                        else if(mydb.changeState(id_To_Update,NotesDb.personal)){
                            displayToast("added to Personal",0);
                        }
                        break;
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        note.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if(hasFocus){
                    setTitle(editNote_t);
                }
            }
        });
        note.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }
            @Override
            public void afterTextChanged(Editable s) {
                Linkify.addLinks(s,Linkify.ALL);
            }
        });
        extras=getIntent().getExtras();
        if(extras!=null){
            value_f_b=extras.getInt("id");
            prev=extras.getString("prev");
            prevstate.putString("prev",prev);
            if(value_f_b==-3){
                id_To_Update=0;
            }
            else if(value_f_b>0){
                id_To_Update=value_f_b;
            }
            if (id_To_Update > 0) {
                if(!mydb.checkAvailability(id_To_Update)){
                    finish();
                }
                Cursor rs = mydb.getData(id_To_Update);
                rs.moveToFirst();
                state=rs.getInt(rs.getColumnIndex(NotesDb.State_C));
                String contents = rs.getString(rs.getColumnIndex(NotesDb.Notes_C));
                String date_time_v = rs.getString(rs.getColumnIndex(NotesDb.Time_C));
                if (!rs.isClosed()) {
                    rs.close();
                }
                if(PendingIntent.getBroadcast(getApplicationContext(),id_To_Update,
                        new Intent(DisplayNote.this,BroadcastRec.class),PendingIntent.FLAG_NO_CREATE)!=null){
                    bell_off.setImageResource(R.drawable.ic_baseline_notifications_active_24);
                }
                else {
                    mydb.updateRemind(id_To_Update,"0");
                    bell_off.setImageResource(R.drawable.ic_baseline_notifications_24);
                }
                datetime.setText(date_time_v);
                note.setText(contents);
                changeFavIcon(mydb.getState(id_To_Update));
                switch (state){
                    case 1:
                        spinner.setSelection(0);
                        break;
                    case 4:
                        spinner.setSelection(1);
                        break;
                    case 5:
                        spinner.setSelection(2);
                        break;
                    case 6:
                        spinner.setSelection(3);
                        break;
                    case 7:
                        spinner.setSelection(4);
                        break;
                    case 8:
                        spinner.setSelection(5);
                        break;
                }
            }
            else {
                if(value_f_b==-3){
                    String data= extras.getString("data");
                    note.setText(data);
                }
                Calendar c = Calendar.getInstance();
                SimpleDateFormat tf=new SimpleDateFormat("ddMMM yyyy hh:mm a", Locale.US);
                String timeString=tf.format(c.getTime());
                datetime.setText(timeString);
            }
        }
    }
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.save_menu, menu);
        return true;
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        MenuItem restore=menu.findItem(R.id.retore);
        MenuItem unarchive=menu.findItem(R.id.unarchive);
        MenuItem archive=menu.findItem(R.id.archive);
        if(state==3){
            restore.setVisible(true);
            return true;
        }
        else{
            if(id_To_Update>0){
                if(mydb.getState(id_To_Update)==NotesDb.archived){
                    unarchive.setVisible(true);
                    return true;
                }
                else {
                    archive.setVisible(true);
                    return true;
                }
            }
            else {
                archive.setVisible(true);
                return true;
            }
        }
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
        if(item.getItemId()==R.id.unarchive){
            if(id_To_Update==0 ){
                displayToast("Save the note",0);
            }
            else {
                mydb.changeState(id_To_Update,NotesDb.general);
                Intent intent=new Intent(DisplayNote.this,MainActivity.class);
                intent.putExtras(prevstate);
                displayToast("unarchived",0);
                startActivity(intent);
                overridePendingTransition(R.anim.slide_out_bottom,R.anim.slide_in_bottom);
                finish();
                return true;
            }

        }
        if(item.getItemId()==R.id.archive){
            if(id_To_Update==0 ){
                displayToast("Save the note",0);
            }
            else {
                mydb.changeState(id_To_Update, NotesDb.archived);
                Intent intent = new Intent(DisplayNote.this, MainActivity.class);
                intent.putExtras(prevstate);
                displayToast("archived",0);
                startActivity(intent);
                overridePendingTransition(R.anim.slide_out_bottom,R.anim.slide_in_bottom);
                finish();
                return true;
            }
        }
        if(item.getItemId()==R.id.retore){
            saveNote();
            mydb.changeState(id_To_Update,NotesDb.general);
            displayToast("note restored",0);
            Intent intent=new Intent(DisplayNote.this,MainActivity.class);
            intent.putExtras(prevstate);
            startActivity(intent);
            overridePendingTransition(R.anim.slide_out_bottom,R.anim.slide_in_bottom);
            finish();
            return true;
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
                               displayToast("Save the note",0);
                           }
                           else  if(mydb.getState(id_To_Update)==NotesDb.favorite){
                               if(mydb.changeState(id_To_Update,NotesDb.general)){
                                   changeFavIcon(mydb.getState(id_To_Update));
                                   displayToast("removed from favourites",0);
                               }
                           }
                           else{
                               if(mydb.changeState(id_To_Update,NotesDb.favorite)){
                                   changeFavIcon(mydb.getState(id_To_Update));
                                   displayToast("added to favourites",0);
                               }
                           }
                            return true;
                        case R.id.share_view:
                            if(note.getText().toString().trim().equals("")){
                                displayToast("Can't Share Empty Note",0);
                            }
                            else if(id_To_Update==0){
                                displayToast("Save the note",0);
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
                                displayToast("not saved yet",0);
                            }
                            else if(mydb.getState(id_To_Update)!=NotesDb.deleted){
                                if(value_f_b==-3 || value_f_b==-1){
                                    finishAffinity();
                                    System.exit(0);
                                }
                                Intent intent=new Intent(DisplayNote.this,MainActivity.class);
                                mydb.changeState(id_To_Update,NotesDb.deleted);
                                displayToast("moved to trash",0);
                                intent.putExtras(prevstate);
                                startActivity(intent);
                                overridePendingTransition(R.anim.slide_out_bottom,R.anim.slide_in_bottom);
                                finish();
                            }
                            else if(mydb.getState(id_To_Update)==NotesDb.deleted){
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
                                            intent.putExtras(prevstate);
                                            startActivity(intent);
                                            overridePendingTransition(R.anim.slide_out_bottom,R.anim.slide_in_bottom);
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
            if (extras.getString("notes")!=null){
                finish();
            }
            else if(value_f_b>=0){
                Intent intent = new Intent(getApplicationContext(),MainActivity.class);
                intent.putExtras(prevstate);
                startActivity(intent);
                overridePendingTransition(R.anim.slide_out_bottom,R.anim.slide_in_bottom);
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
                        overridePendingTransition(R.anim.slide_out_bottom,R.anim.slide_in_bottom);
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

            alertDialog.show();
        }
    }
    public void saveNote(){
        Calendar cn = Calendar.getInstance();
        SimpleDateFormat tfn=new SimpleDateFormat("ddMMM yyyy hh:mm a", Locale.US);
        String timeStringn=tfn.format(cn.getTime());
        SimpleDateFormat dfn = new SimpleDateFormat("ddMMM",Locale.US);
        String dateStringn= dfn.format(cn.getTime());
        if (id_To_Update> 0) {
            if (note.getText().toString().trim().equals("")) {
                displayToast("Can't save empty note",0);
            }
            else {
                if(!note.getText().toString().equals(mydb.getNote(id_To_Update))){
                    datetime.setText(timeStringn);
                    if (mydb.updateNotes(id_To_Update, note.getText()
                            .toString(), timeStringn,dateStringn)) {
                        displayToast("updated successfully",0);

                    } else {
                        displayToast("can't update",0);
                    }

                }

            }
        }
        else {
            if (note.getText().toString().trim().equals("")) {
                displayToast("Can't save empty note",0);

            } else {
                if (mydb.insertNotes(note.getText().toString(), timeStringn, dateStringn)) {
                    id_To_Update= mydb.getMaxId();
                    displayToast("saved successfully",0);
                }
                else {
                    displayToast("can't save",0);
                }
            }
        }

    }


    public void changeFavIcon(int state){
        if(state==NotesDb.favorite){
            fav_i.findItem(R.id.fav_view).setIcon(R.drawable.fav_ful);
        }
        else {
            fav_i.findItem(R.id.fav_view).setIcon(R.drawable.star_b );
        }

    }
    public void createNotificationChannel(){
        mNotificationManager = (NotificationManager) getSystemService( NOTIFICATION_SERVICE ) ;
        if (android.os.Build.VERSION. SDK_INT >= android.os.Build.VERSION_CODES. O ) {
            NotificationChannel notificationChannel = new
                    NotificationChannel( NOTIFICATION_CHANNEL_ID , "Reminder" ,NotificationManager.IMPORTANCE_HIGH) ;
            notificationChannel.enableVibration(true);
            notificationChannel.enableLights(true);
            notificationChannel.setLightColor(Color.GREEN);
            mNotificationManager.createNotificationChannel(notificationChannel) ;
        }

    }
    public void createRemind(Calendar c){
        Intent intent=new Intent(DisplayNote.this,BroadcastRec.class);
        Bundle dataBundle = new Bundle();
        String month = String.valueOf(c.get(Calendar.MONTH)+1);
        String remind=c.get(Calendar.DATE)+"-"+month+"-"+c.get(Calendar.YEAR)+" "+c.get(Calendar.HOUR)+":"+c.get(Calendar.MINUTE);
        if(c.get(Calendar.AM_PM)==Calendar.AM){
            remind += "AM";
        }
        else {
            remind=remind+"PM";
        }
        dataBundle.putInt("id", id_To_Update);
        dataBundle.putString("prev","All Notes");
        dataBundle.putString("notes",mydb.getNote(id_To_Update));
        intent.addFlags(Intent.FLAG_RECEIVER_FOREGROUND);
        intent.putExtras(dataBundle);
        PendingIntent pendingIntent=PendingIntent.getBroadcast(DisplayNote.this,id_To_Update,intent,PendingIntent.FLAG_UPDATE_CURRENT);
        AlarmManager alarmManager=(AlarmManager) getSystemService(ALARM_SERVICE);
        if(alarmManager!=null){
            if(Build.VERSION.SDK_INT>=Build.VERSION_CODES.M){
                alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP,c.getTimeInMillis(),pendingIntent);
                mydb.updateRemind(id_To_Update,remind);
                bell_off.setImageResource(R.drawable.ic_baseline_notifications_active_24);
                displayToast("Reminder set on\n"+remind,1);
            }
            else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
                alarmManager.setExact(AlarmManager.RTC_WAKEUP,c.getTimeInMillis(),pendingIntent);
                mydb.updateRemind(id_To_Update,remind);
                bell_off.setImageResource(R.drawable.ic_baseline_notifications_active_24);
                displayToast("Reminder set on\n"+remind,1);
            }
            else {
                alarmManager.set(AlarmManager.RTC_WAKEUP,c.getTimeInMillis(),pendingIntent);
                mydb.updateRemind(id_To_Update,remind);
                bell_off.setImageResource(R.drawable.ic_baseline_notifications_active_24);
                displayToast("Reminder set on\n"+remind,1);
            }

        }
    }
    public void displayToast(String string,int t){
        Toast toast;
        if(t==0) toast = Toast.makeText(getApplicationContext(), string, Toast.LENGTH_SHORT);
        else toast = Toast.makeText(getApplicationContext(), string, Toast.LENGTH_LONG);
        toast.setGravity(Gravity.CENTER,0,0);
        toast.show();
    }



}




