package com.example.night;

import android.Manifest;
import android.app.Activity;
import android.app.AlarmManager;
import android.app.AlertDialog;
import android.app.PendingIntent;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.database.Cursor;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.IntentCompat;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.navigation.NavigationView;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.channels.FileChannel;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;


public class MainActivity extends AppCompatActivity {
    NotesDb mydb;
    FloatingActionButton fab_add;
    ListView mylist;
    SimpleCursorAdapter adapter;
    TextView no_note;
    ImageView no_image;
    LinearLayout nonote_li,list_li;
    String all_notes_t="All Notes";
    String favourites_t="My Favourites";
    String deleted_t="Deleted Notes";
    int main_state;
    Bundle main_bun ;
    NavigationView navigationView;
    Toolbar toolbar;
    ActionBarDrawerToggle toggle;
    DrawerLayout drawerLayout;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        toolbar=(Toolbar) findViewById(R.id.toolbar_main);
        toolbar.setTitle(all_notes_t);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle(all_notes_t);
        drawerLayout=findViewById(R.id.drawer_lay_main);
        toggle=new ActionBarDrawerToggle(this,drawerLayout,toolbar,R.string.nav_open_des,R.string.nav_close_des);
        drawerLayout.addDrawerListener(toggle);
        toggle.syncState();
        main_state=0;
        main_bun=getIntent().getExtras();
        navigationView=findViewById(R.id.main_nav_view_id);
        nonote_li=findViewById(R.id.linear_nonote);
        list_li=findViewById(R.id.linear_list);
        fab_add=findViewById(R.id.floatingActionButton);
        mylist=findViewById(R.id.List_view);
        no_image=findViewById(R.id.nonote_img);
        no_note=findViewById(R.id.nonote_textv);
        mydb=new NotesDb(MainActivity.this);
        Cursor c=mydb.fetchAll();
        final String[] fieldNames=new String[]  {NotesDb.Id, NotesDb.Notes_C,NotesDb.Date_C,NotesDb.Time_C,NotesDb.State_C };
        int[] display=new  int[] {R.id.id_list, R.id.note_List,R.id.date_list,R.id.time_list,R.id.state_list};
        adapter=new SimpleCursorAdapter(this,R.layout.note_temp,c,fieldNames,display,0);
        mylist.setAdapter(adapter);
        mylist.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> arg0, View arg1, int arg2,
                                    long arg3) {
                LinearLayout linearLayout = (LinearLayout) arg1;
                TextView m = (TextView) linearLayout.getChildAt(2);
                Bundle dataBundle = new Bundle();
                dataBundle.putInt("id",
                        Integer.parseInt(m.getText().toString()));
                Intent intent = new Intent(getApplicationContext(),
                        DisplayNote.class);
                intent.putExtras(dataBundle);
                startActivity(intent);
                finish();
            }
        });
        toolbar.setTitle(all_notes_t);
        if(!mydb.haveEntries() ){
            nonote_li.setVisibility(View.VISIBLE);
            no_image.setVisibility(View.VISIBLE);
            no_note.setVisibility(View.VISIBLE);

        }
        else {
            if( !mydb.haveState("1") && !mydb.haveState("2") && !mydb.haveState("4") && !mydb.haveState("5") && !mydb.haveState("6")&& !mydb.haveState("7")){
                nonote_li.setVisibility(View.VISIBLE);
                no_image.setVisibility(View.VISIBLE);
                no_note.setVisibility(View.VISIBLE);
            }
            else{
                list_li.setVisibility(View.VISIBLE);
                mylist.setVisibility(View.VISIBLE);
            }
        }
        fab_add.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Bundle dataBundle = new Bundle();
                dataBundle.putInt("id", 0);
                Intent intent = new Intent(getApplicationContext(),
                        DisplayNote.class);
                intent.putExtras(dataBundle);
                startActivity(intent);
                finish();
            }
        });
        if(main_bun != null){
            String s=main_bun.getString("state");

            if(s!=null){
                if(s.equals("a")){
                    change_to_all();
                }
                else if(s.equals("d")){
                    change_to_deleted();
                }
                else if(s.equals("h")){
                    change_to_home();
                }
                else if(s.equals("w")){
                    change_to_work();
                }
                else if(s.equals("e")){
                    change_to_edu();
                }
                else if(s.equals("o")){
                    change_to_other();
                }
            }

        }
        navigationView.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                 switch (item.getItemId()){
                     case R.id.home_cat:
                         change_to_home();
                         drawerLayout.closeDrawer(GravityCompat.START);
                         return true;
                     case R.id.work_cat:
                         change_to_work();
                         drawerLayout.closeDrawer(GravityCompat.START);
                         return true;
                     case R.id.education_cat:
                         change_to_edu();
                         drawerLayout.closeDrawer(GravityCompat.START);
                         return true;
                     case R.id.other_cat:
                         change_to_other();
                         drawerLayout.closeDrawer(GravityCompat.START);
                         return true;
                     case R.id.personal_cat:
                         toolbar.setTitle(R.string.personal_cat);
                         fab_add.setVisibility(View.GONE);
                         if(!mydb.haveState("8")){
                             list_li.setVisibility(View.INVISIBLE);
                             mylist.setVisibility(View.INVISIBLE);
                             nonote_li.setVisibility(View.VISIBLE);
                             no_image.setVisibility(View.VISIBLE);
                             no_note.setVisibility(View.VISIBLE);
                         }
                         else {
                             list_li.setVisibility(View.VISIBLE);
                             mylist.setVisibility(View.VISIBLE);
                             nonote_li.setVisibility(View.INVISIBLE);
                             no_image.setVisibility(View.INVISIBLE);
                             no_note.setVisibility(View.INVISIBLE);
                             Cursor cursor_d=mydb.fetchPersonal();
                             adapter.changeCursor(cursor_d);
                         }
                         drawerLayout.closeDrawer(GravityCompat.START);
                         return true;

                     case R.id.settings_nav_id:
                         Toast.makeText(MainActivity.this,"Under Construction, you will see this soon",Toast.LENGTH_SHORT).show();
                         drawerLayout.closeDrawer(GravityCompat.START);
                         return true;
                     case R.id.share_nav_id:
                         Intent share_app=new Intent(Intent.ACTION_SEND);
                         share_app.setType("text/plain");
                         share_app.putExtra(Intent.EXTRA_SUBJECT,R.string.app_name);
                         String m="Check out CS Notes\n";
                         m += "https://play.google.com/store/apps/details?id=" + BuildConfig.APPLICATION_ID ;
                         share_app.putExtra(Intent.EXTRA_TEXT,m);
                         startActivity(Intent.createChooser(share_app,"choose one"));
                         Toast toast=Toast.makeText(MainActivity.this,"Thank You...",Toast.LENGTH_LONG);
                         toast.setGravity(Gravity.CENTER,0,0);
                         toast.show();

                         return true;
                     case R.id.rate_nav_id:
                         Toast t=Toast.makeText(MainActivity.this,"Thank You...",Toast.LENGTH_LONG);
                         t.setGravity(Gravity.CENTER,0,0);
                         t.show();
                         startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=sunny.app.csnotes")));
                         return true;

                 }
                 return false;
            }
        });

    }

    @Override
    public void onBackPressed() {
        if(this.drawerLayout.isDrawerOpen(GravityCompat.START)){
            this.drawerLayout.closeDrawer(GravityCompat.START);
        }
        else{
            super.onBackPressed();
        }
    }

    @Override
    protected void onPostCreate(@Nullable Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        toggle.syncState();
    }

    @Override
    public void onConfigurationChanged(@NonNull Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        toggle.syncState();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main_menu,menu);
        return true;
    }



    @Override
    protected void onStart() {
        adapter.notifyDataSetChanged();
        super.onStart();
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {

        switch (item.getItemId()){
            case R.id.all_notes_m:
                change_to_all();
                return true;
            case R.id.fav_main_m:
                change_to_fav();
                return true;
            case R.id.deleted_m:
                change_to_deleted();
                return true;
            case R.id.restore_del:
                if(mydb.haveState("3")){
                    mydb.restore_del();
                    if(!mydb.haveState("3")){
                        Toast.makeText(MainActivity.this,"Restored all deleted notes",Toast.LENGTH_SHORT).show();
                        Intent intent=new Intent(MainActivity.this,MainActivity.class);
                        startActivity(intent);
                        finish();
                    }
                    else {
                        Toast.makeText(MainActivity.this,"Error occurred in restoring deleted notes",Toast.LENGTH_SHORT).show();
                    }
                }
                else{
                    Toast.makeText(MainActivity.this,"No Notes to Restore",Toast.LENGTH_SHORT).show();
                }


                return true;
            case R.id.delete_all:
                if(mydb.haveEntries()) {
                    AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
                    builder.setMessage("You can't recover again!!!\nPlease export your database for future usage");
                    builder.setIcon(R.drawable.delete_forever);
                    builder.setTitle("Delete everything");
                    builder.setPositiveButton("Delete", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            if (mydb.delAll()) {
                                Toast.makeText(MainActivity.this, "Deleted Everything successfully", Toast.LENGTH_SHORT).show();
                                Intent intent = new Intent(MainActivity.this, MainActivity.class);
                                startActivity(intent);
                                finish();
                            } else {
                                Toast.makeText(MainActivity.this, "error occurred in deleting all", Toast.LENGTH_SHORT).show();

                            }
                        }
                    });
                    builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.cancel();
                        }
                    });
                    final AlertDialog alertDialog = builder.create();
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
                else{
                    Toast.makeText(MainActivity.this, "There's nothing to delete", Toast.LENGTH_SHORT).show();

                }

                return true;
            case R.id.clear_del:
                if(mydb.haveState("3")) {
                    AlertDialog.Builder builder2 = new AlertDialog.Builder(MainActivity.this);
                    builder2.setMessage("You can't recover again!!!");
                    builder2.setIcon(R.drawable.delete_forever);
                    builder2.setTitle("Clear deleted");
                    builder2.setPositiveButton("Delete", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            if (mydb.clear_deleted()) {
                                Toast.makeText(MainActivity.this, "Cleared deleted notes", Toast.LENGTH_SHORT).show();
                                Intent intent = new Intent(MainActivity.this, MainActivity.class);
                                startActivity(intent);
                                finish();
                            } else {
                                Toast.makeText(MainActivity.this, "error occurred in deleting all", Toast.LENGTH_SHORT).show();

                            }
                        }
                    });
                    builder2.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.cancel();
                        }
                    });
                    final AlertDialog alertDialog2 = builder2.create();
                    alertDialog2.setOnShowListener(new DialogInterface.OnShowListener() {
                        @Override
                        public void onShow(DialogInterface dialog) {
                            alertDialog2.getButton(AlertDialog.BUTTON_NEGATIVE).setGravity(Gravity.START);
                            alertDialog2.getButton(AlertDialog.BUTTON_POSITIVE).setGravity(Gravity.END);
                            alertDialog2.getButton(AlertDialog.BUTTON_NEGATIVE).setTextColor(Color.GRAY);
                            alertDialog2.getButton(AlertDialog.BUTTON_POSITIVE).setTextColor(Color.BLACK);

                        }
                    });
                    alertDialog2.show();
                }
                else {
                    Toast.makeText(MainActivity.this, "There's nothing to clear", Toast.LENGTH_SHORT).show();
                }
                return true;
            case R.id.export_db_id :
                if(ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.WRITE_EXTERNAL_STORAGE)
                        != PackageManager.PERMISSION_GRANTED){
                    Toast.makeText(MainActivity.this, "need storage access permission to save database!!!", Toast.LENGTH_LONG).show();
                    ActivityCompat.requestPermissions(MainActivity.this,new String[]  { Manifest.permission.WRITE_EXTERNAL_STORAGE }, 3);
                }
                else {
                    File folder = new File(Environment.getExternalStorageDirectory().getAbsolutePath(),"/CS Notes");
                    if(!folder.exists()){
                        folder.mkdirs();
                    }
                    Calendar c = Calendar.getInstance();
                    SimpleDateFormat tf=new SimpleDateFormat(" hh:mm a", Locale.US);
                    String timeString=tf.format(c.getTime());
                    SimpleDateFormat df = new SimpleDateFormat(" ddMMMYYY",Locale.US);
                    String dateString= df.format(c.getTime());
                    String date_time=dateString+timeString;
                    File data = Environment.getDataDirectory();
                    FileChannel source  = null;
                    FileChannel destination = null;
                    String currentDBPath="/data/sunny.app.csnotes/databases/Notes.db";
                    String backupDBPath= date_time+"_Notes.db";
                    File  currentDB= new File(data,currentDBPath);
                    File backupDB= new File(folder,backupDBPath);
                    if(!backupDB.exists()){
                        try {
                            backupDB.createNewFile();
                        } catch (IOException e) {
                            e.printStackTrace();
                            Toast.makeText(getBaseContext(), e.toString(), Toast.LENGTH_LONG).show();

                        }
                    }
                    try {
                        source= new FileInputStream(currentDB).getChannel();
                        destination=new FileOutputStream(backupDB).getChannel();
                        destination.transferFrom(source,0,source.size());
                        source.close();
                        destination.close();
                        Toast toast=Toast.makeText(getBaseContext(), "Saved in CS Notes folder\n"+backupDB.getPath(), Toast.LENGTH_LONG);
                        toast.setGravity(Gravity.CENTER,0,0);
                        toast.show();
                    }
                    catch (IOException e){
                        Toast.makeText(getBaseContext(), e.toString(), Toast.LENGTH_LONG).show();
                    }
                }

                return true;
            case R.id.import_db_id :

                Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
                intent.setType("*/*");
                startActivityForResult(intent,3333);
                return true;
        }
        return false;

    }
    public void allNotes(){
        Cursor cursor_a=mydb.fetchAll();
        adapter.changeCursor(cursor_a);
    }
    public void favNotes(){
        Cursor cursor_f=mydb.fetchFav();
        adapter.changeCursor(cursor_f);
    }
    public void delNotes(){
        Cursor cursor_d=mydb.fetchDel();
        adapter.changeCursor(cursor_d);
    }
    public void change_to_fav(){
        toolbar.setTitle(favourites_t);
        fab_add.setVisibility(View.GONE);
        if(!mydb.haveState("2")){
            list_li.setVisibility(View.INVISIBLE);
            mylist.setVisibility(View.INVISIBLE);
            nonote_li.setVisibility(View.VISIBLE);
            no_image.setVisibility(View.VISIBLE);
            no_note.setVisibility(View.VISIBLE);
        }
        else {
            list_li.setVisibility(View.VISIBLE);
            mylist.setVisibility(View.VISIBLE);
            nonote_li.setVisibility(View.INVISIBLE);
            no_image.setVisibility(View.INVISIBLE);
            no_note.setVisibility(View.INVISIBLE);
            favNotes();
        }
    }
    public void change_to_deleted(){
        fab_add.setVisibility(View.GONE);
        toolbar.setTitle(deleted_t);
        if(!mydb.haveState("3")){
            list_li.setVisibility(View.INVISIBLE);
            mylist.setVisibility(View.INVISIBLE);
            nonote_li.setVisibility(View.VISIBLE);
            no_image.setVisibility(View.VISIBLE);
            no_note.setVisibility(View.VISIBLE);
        }
        else {
            list_li.setVisibility(View.VISIBLE);
            mylist.setVisibility(View.VISIBLE);
            nonote_li.setVisibility(View.INVISIBLE);
            no_image.setVisibility(View.INVISIBLE);
            no_note.setVisibility(View.INVISIBLE);
            delNotes();
        }
    }
    public void change_to_all(){
        toolbar.setTitle(all_notes_t);
        if(mydb.haveState("1") || mydb.haveState("2") || mydb.haveState("4") || mydb.haveState("5") || mydb.haveState("6") || mydb.haveState("7")){
            list_li.setVisibility(View.VISIBLE);
            mylist.setVisibility(View.VISIBLE);
            nonote_li.setVisibility(View.INVISIBLE);
            no_image.setVisibility(View.INVISIBLE);
            no_note.setVisibility(View.INVISIBLE);
            allNotes();
        }
        else {
            list_li.setVisibility(View.INVISIBLE);
            mylist.setVisibility(View.INVISIBLE);
            nonote_li.setVisibility(View.VISIBLE);
            no_image.setVisibility(View.VISIBLE);
            no_note.setVisibility(View.VISIBLE);
        }
        fab_add.setVisibility(View.VISIBLE);
    }


    public void change_to_home(){
        toolbar.setTitle(R.string.home_cat);
        fab_add.setVisibility(View.GONE);
        if(!mydb.haveState("4")){
            list_li.setVisibility(View.INVISIBLE);
            mylist.setVisibility(View.INVISIBLE);
            nonote_li.setVisibility(View.VISIBLE);
            no_image.setVisibility(View.VISIBLE);
            no_note.setVisibility(View.VISIBLE);
        }
        else {
            list_li.setVisibility(View.VISIBLE);
            mylist.setVisibility(View.VISIBLE);
            nonote_li.setVisibility(View.INVISIBLE);
            no_image.setVisibility(View.INVISIBLE);
            no_note.setVisibility(View.INVISIBLE);
            Cursor cursor=mydb.fetchCat("4");
            adapter.changeCursor(cursor);

        }
    }
    public void change_to_work(){
        toolbar.setTitle(R.string.work_cat);
        fab_add.setVisibility(View.GONE);
        if(!mydb.haveState("5")){
            list_li.setVisibility(View.INVISIBLE);
            mylist.setVisibility(View.INVISIBLE);
            nonote_li.setVisibility(View.VISIBLE);
            no_image.setVisibility(View.VISIBLE);
            no_note.setVisibility(View.VISIBLE);
        }
        else {
            list_li.setVisibility(View.VISIBLE);
            mylist.setVisibility(View.VISIBLE);
            nonote_li.setVisibility(View.INVISIBLE);
            no_image.setVisibility(View.INVISIBLE);
            no_note.setVisibility(View.INVISIBLE);
            Cursor cursor=mydb.fetchCat("5");
            adapter.changeCursor(cursor);
        }
    }
    public void change_to_edu(){
        toolbar.setTitle(R.string.education_cat);
        fab_add.setVisibility(View.GONE);
        if(!mydb.haveState("6")){
            list_li.setVisibility(View.INVISIBLE);
            mylist.setVisibility(View.INVISIBLE);
            nonote_li.setVisibility(View.VISIBLE);
            no_image.setVisibility(View.VISIBLE);
            no_note.setVisibility(View.VISIBLE);
        }
        else {
            list_li.setVisibility(View.VISIBLE);
            mylist.setVisibility(View.VISIBLE);
            nonote_li.setVisibility(View.INVISIBLE);
            no_image.setVisibility(View.INVISIBLE);
            no_note.setVisibility(View.INVISIBLE);
            Cursor cursor=mydb.fetchCat("6");
            adapter.changeCursor(cursor);
        }
    }
    public void change_to_other(){
        toolbar.setTitle(R.string.other_cat);
        fab_add.setVisibility(View.GONE);
        if(!mydb.haveState("7")){
            list_li.setVisibility(View.INVISIBLE);
            mylist.setVisibility(View.INVISIBLE);
            nonote_li.setVisibility(View.VISIBLE);
            no_image.setVisibility(View.VISIBLE);
            no_note.setVisibility(View.VISIBLE);
        }
        else {
            list_li.setVisibility(View.VISIBLE);
            mylist.setVisibility(View.VISIBLE);
            nonote_li.setVisibility(View.INVISIBLE);
            no_image.setVisibility(View.INVISIBLE);
            no_note.setVisibility(View.INVISIBLE);
            Cursor cursor=mydb.fetchCat("7");
            adapter.changeCursor(cursor);
        }
    }
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 3333 && resultCode == Activity.RESULT_OK) {
            InputStream ip= null;
            try {
                ip = getContentResolver().openInputStream(data.getData());
            } catch (FileNotFoundException e) {
                Toast.makeText(getBaseContext(), e.toString(), Toast.LENGTH_LONG).show();
            }

            try {

                File data1 = Environment.getDataDirectory();
                String currentDBPath = "/data/" + "sunny.app.csnotes"
                        + "/databases/" + "Notes.db";

                File backupDB = new File(data1, currentDBPath);
                if(backupDB.exists()){
                    backupDB.delete();
                    backupDB = new File(data1, currentDBPath);
                }
                OutputStream outputStream= new FileOutputStream(backupDB);
                byte[] buffer = new byte[100*1024];
                int read;
                while ((read=ip.read(buffer))!=-1){
                    outputStream.write(buffer,0,read);
                }
                outputStream.flush();

                Toast.makeText(getApplicationContext(), "Imported Successful!",
                        Toast.LENGTH_LONG).show();
                Intent restart = new Intent( MainActivity.this, MainActivity.class);
                finish();
                startActivity(restart);


            } catch (Exception e) {

                Toast.makeText(getApplicationContext(), e.toString(), Toast.LENGTH_LONG)
                        .show();

            }
            finally {
                try {
                    ip.close();
                } catch (IOException e) {
                    e.printStackTrace();
                    Toast.makeText(getBaseContext(), e.toString(), Toast.LENGTH_LONG).show();

                }
            }
        }

    }
}

