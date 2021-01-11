package com.example.night;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Color;
import android.os.Bundle;
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

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.InterstitialAd;
import com.google.android.gms.ads.MobileAds;
import com.google.android.gms.ads.initialization.InitializationStatus;
import com.google.android.gms.ads.initialization.OnInitializationCompleteListener;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.floatingactionbutton.FloatingActionButton;


public class MainActivity extends AppCompatActivity {
    private AdView no_n_ad,top_ad,bot_ad;
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
    String noFav="No favourites";
    String noDel="No deleted Notes";
    String noNote="No notes";
    int main_state;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        setTitle(all_notes_t);
        main_state=0;
        new Bundle();
        Bundle main_bun;
        main_bun=getIntent().getExtras();
        /*MobileAds.initialize(this);

        no_n_ad=findViewById(R.id.ad_no_note);
        top_ad=findViewById(R.id.ad_li_top);
        bot_ad=findViewById(R.id.ad_li_bot);
        AdRequest no_adr=new AdRequest.Builder().build();
        AdRequest top_adr=new AdRequest.Builder().build();
        AdRequest bot_adr=new AdRequest.Builder().build();
        no_n_ad.loadAd(no_adr);
        top_ad.loadAd(top_adr);
        bot_ad.loadAd(bot_adr);*/
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
        if(!mydb.haveEntries() ){
            nonote_li.setVisibility(View.VISIBLE);
            no_image.setVisibility(View.VISIBLE);
            no_note.setVisibility(View.VISIBLE);

        }
        else {
            if((!mydb.haveState("1")) && (!mydb.haveState("2"))){
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
                else if(s.equals("f")){
                    change_to_fav();
                }
                else if(s.equals("d")){
                    change_to_deleted();
                }
            }

        }

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main_menu,menu);


        return true;
    }

    @Override
    protected void onResume() {
        adapter.notifyDataSetChanged();
        super.onResume();
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        super.onOptionsItemSelected(item);

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
                    builder.setMessage("You can't recover again!!!");
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
        setTitle(favourites_t);
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
        setTitle(deleted_t);
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
        setTitle(all_notes_t);
        if(mydb.haveState("1") || mydb.haveState("2")){
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

    @Override
    public void onBackPressed() {

        super.onBackPressed();
    }
}

