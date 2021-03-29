package com.example.night;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.database.Cursor;
import android.database.sqlite.SQLiteCursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.util.SparseBooleanArray;
import android.view.ActionMode;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.SearchView;
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
import androidx.core.view.GravityCompat;
import androidx.customview.widget.ViewDragHelper;
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
import java.lang.reflect.Field;
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
    Integer selectedCount=0;
    LinearLayout nonote_li,list_li;
    String all_notes_t="All Notes";
    String favourites_t="Favourites";
    String deleted_t="Trash";
    String home_t="Home";
    String work_t="Work";
    String education_t="Education";
    String other_t="Other";
    String personal_t="Personal";
    String archived_t="Archived";
    int main_state;
    Bundle main_bun ;
    NavigationView navigationView;
    Toolbar toolbar;
    ActionBarDrawerToggle toggle;
    SearchView searchView;
    DrawerLayout drawerLayout;
    SparseBooleanArray sparseBooleanArray;
    Field mDragger;
    ViewDragHelper draggerObj;
    Field mEdgeSize;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        toolbar=(Toolbar) findViewById(R.id.toolbar_main);
        toolbar.setTitle(all_notes_t);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle(all_notes_t);
        drawerLayout=findViewById(R.id.drawer_lay_main);
        try {
            makeDragable();
        } catch (NoSuchFieldException e) {
            e.printStackTrace();
            displayToast(e.toString(),1);
        } catch (IllegalAccessException e) {
            e.printStackTrace();
            displayToast(e.toString(),1);
        }
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
        mylist.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE_MODAL);
        mylist.setItemsCanFocus(false);
        no_image=findViewById(R.id.nonote_img);
        no_note=findViewById(R.id.nonote_textv);
        mydb=new NotesDb(MainActivity.this);
        Cursor c=mydb.fetchAll();
        final String[] fieldNames=new String[]  {NotesDb.Id, NotesDb.Notes_C,NotesDb.Date_C,NotesDb.Time_C,NotesDb.State_C };
        final int[] display=new  int[] {R.id.id_list, R.id.note_List,R.id.date_list,R.id.time_list,R.id.state_list};
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
                dataBundle.putString("prev",toolbar.getTitle().toString());
                Intent intent = new Intent(getApplicationContext(),
                        DisplayNote.class);
                intent.putExtras(dataBundle);
                startActivity(intent);
                overridePendingTransition(R.anim.slide_out_bottom,R.anim.slide_in_bottom);
                finish();
            }
        });
        mylist.setMultiChoiceModeListener(new AbsListView.MultiChoiceModeListener() {
            @Override
            public void onItemCheckedStateChanged(ActionMode mode, int position, long id, boolean checked) {
                selectedCount=mylist.getCheckedItemCount();
                mode.setTitle(selectedCount+" selected");
                mode.invalidate();
            }

            @Override
            public boolean onCreateActionMode(ActionMode mode, Menu menu) {
                mode.getMenuInflater().inflate(R.menu.main_context_actionbar,menu);
                return true;
            }

            @Override
            public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
                MenuItem share_c=menu.findItem(R.id.share_view_con);
                if(selectedCount==1){
                    share_c.setVisible(true);
                }
                else {
                    share_c.setVisible(false);
                }
                return true;
            }

            @Override
            public boolean onActionItemClicked(ActionMode mode, MenuItem item) {
                sparseBooleanArray = mylist.getCheckedItemPositions();
                switch (item.getItemId()){
                    case R.id.fav_view_con:
                        for(int i=0;i<sparseBooleanArray.size();i++){
                            if(sparseBooleanArray.valueAt(i)){
                                SQLiteCursor sqLiteCursor = (SQLiteCursor) adapter.getItem(sparseBooleanArray.keyAt(i));
                                int id=sqLiteCursor.getInt(sqLiteCursor.getColumnIndex(NotesDb.Id));
                                if(mydb.getState(id)!=NotesDb.favorite){
                                    mydb.changeState(id,NotesDb.favorite);
                                    displayToast("added to favourites",0);
                                }
                            }
                        }
                        refresh_list();
                        mode.finish();
                        return true;
                    case R.id.delete_view_con:
                        for(int i=0;i<sparseBooleanArray.size();i++){
                            if(sparseBooleanArray.valueAt(i)){
                                SQLiteCursor sqLiteCursor = (SQLiteCursor) adapter.getItem(sparseBooleanArray.keyAt(i));
                                int id=sqLiteCursor.getInt(sqLiteCursor.getColumnIndex(NotesDb.Id));
                                if(mydb.getState(id)!=NotesDb.deleted){
                                    mydb.changeState(id,NotesDb.deleted);
                                    displayToast("moved to trash",0);
                                }

                            }
                        }
                        refresh_list();
                        mode.finish();
                        return true;
                    case R.id.share_view_con:
                        for(int i=0;i<sparseBooleanArray.size();i++){
                            if(sparseBooleanArray.valueAt(i)){
                                SQLiteCursor sqLiteCursor = (SQLiteCursor) adapter.getItem(sparseBooleanArray.keyAt(i));
                                String note =sqLiteCursor.getString(sqLiteCursor.getColumnIndex(NotesDb.Notes_C));
                                Intent send=new Intent();
                                send.setAction(Intent.ACTION_SEND);
                                send.putExtra(Intent.EXTRA_TEXT,note);
                                send.setType("text/plain");
                                startActivity(Intent.createChooser(send,"Share"));
                            }
                        }
                        mode.finish();
                        return true;
                    case R.id.unarchive_con:
                        for(int i=0;i<sparseBooleanArray.size();i++){
                            if(sparseBooleanArray.valueAt(i)){
                                SQLiteCursor sqLiteCursor = (SQLiteCursor) adapter.getItem(sparseBooleanArray.keyAt(i));
                                int id=sqLiteCursor.getInt(sqLiteCursor.getColumnIndex(NotesDb.Id));
                                if(mydb.getState(id)==NotesDb.archived){
                                    mydb.changeState(id,NotesDb.general);
                                    displayToast("unarchived",0);
                                }
                            }
                        }
                        refresh_list();
                        mode.finish();
                        return true;
                    case R.id.archive_con:
                        for(int i=0;i<sparseBooleanArray.size();i++){
                            if(sparseBooleanArray.valueAt(i)){
                                SQLiteCursor sqLiteCursor = (SQLiteCursor) adapter.getItem(sparseBooleanArray.keyAt(i));
                                int id=sqLiteCursor.getInt(sqLiteCursor.getColumnIndex(NotesDb.Id));
                                if(mydb.getState(id)!=NotesDb.archived){
                                    mydb.changeState(id,NotesDb.archived);
                                    displayToast("archived",0);
                                }
                            }
                        }
                        refresh_list();
                        mode.finish();
                        return true;
                    case R.id.retore_con:
                        for(int i=0;i<sparseBooleanArray.size();i++){
                            if(sparseBooleanArray.valueAt(i)){
                                SQLiteCursor sqLiteCursor = (SQLiteCursor) adapter.getItem(sparseBooleanArray.keyAt(i));
                                int id=sqLiteCursor.getInt(sqLiteCursor.getColumnIndex(NotesDb.Id));
                                if(mydb.getState(id)==NotesDb.deleted){
                                    mydb.changeState(id,NotesDb.general);
                                    displayToast("restored",0);
                                }
                            }
                        }
                        refresh_list();
                        mode.finish();
                        return true;
                    case R.id.unfavorite_con:
                        for(int i=0;i<sparseBooleanArray.size();i++){
                            if(sparseBooleanArray.valueAt(i)){
                                SQLiteCursor sqLiteCursor = (SQLiteCursor) adapter.getItem(sparseBooleanArray.keyAt(i));
                                int id=sqLiteCursor.getInt(sqLiteCursor.getColumnIndex(NotesDb.Id));
                                if(mydb.getState(id)==NotesDb.favorite){
                                    mydb.changeState(id,NotesDb.general);
                                    displayToast("removed from favourites",0);
                                }
                            }
                        }
                        refresh_list();
                        mode.finish();
                        return true;
                }
                return true;
            }

            @Override
            public void onDestroyActionMode(ActionMode mode) {

            }
        });
        toolbar.setTitle(all_notes_t);
        if(!mydb.haveEntries() ){
            nonote_li.setVisibility(View.VISIBLE);
            no_image.setVisibility(View.VISIBLE);
            no_note.setVisibility(View.VISIBLE);

        }
        else {
            if( !mydb.haveState(NotesDb.general)  && !mydb.haveState(NotesDb.home) && !mydb.haveState(NotesDb.work) && !mydb.haveState(NotesDb.education)&& !mydb.haveState(NotesDb.other)){
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
                dataBundle.putString("prev",toolbar.getTitle().toString());
                Intent intent = new Intent(getApplicationContext(),
                        DisplayNote.class);
                intent.putExtras(dataBundle);
                startActivity(intent);
                overridePendingTransition(R.anim.slide_out_bottom,R.anim.slide_in_bottom);
                finish();
            }
        });
        if(main_bun != null){
            String s=main_bun.getString("prev");

            if(s!=null){
                if(s.equals(all_notes_t)){
                    change_to_all();
                }
                else if(s.equals(deleted_t)){
                    change_to_deleted();
                }
                else if(s.equals(home_t)){
                    change_to_home();
                }
                else if(s.equals(work_t)){
                    change_to_work();
                }
                else if(s.equals(education_t)){
                    change_to_edu();
                }
                else if(s.equals(other_t)){
                    change_to_other();
                }
                else if(s.equals(archived_t)){
                    change_to_arc();
                }
                else if(s.equals(personal_t)){
                    change_to_personal();
                }
            }

        }
        navigationView.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                 switch (item.getItemId()){
                     case R.id.reminds_nav_id:
                         change_to_reminders();
                         drawerLayout.closeDrawer(GravityCompat.START);
                         return true;
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
                         change_to_personal();
                         drawerLayout.closeDrawer(GravityCompat.START);
                         return true;
                     case R.id.archived_cat:
                         change_to_arc();
                         drawerLayout.closeDrawer(GravityCompat.START);
                         return true;
                     case R.id.share_nav_id:
                         Intent share_app=new Intent(Intent.ACTION_SEND);
                         share_app.setType("text/plain");
                         share_app.putExtra(Intent.EXTRA_SUBJECT,R.string.app_name);
                         String m="Check out CS Notes\n";
                         m += "\n\nhttps://play.google.com/store/apps/details?id=" + BuildConfig.APPLICATION_ID ;
                         share_app.putExtra(Intent.EXTRA_TEXT,m);
                         startActivity(Intent.createChooser(share_app,"choose one"));
                         displayToast("Thank You...",1);
                         drawerLayout.closeDrawer(GravityCompat.START);
                         return true;
                     case R.id.rate_nav_id:
                         displayToast("Thank You...",1);
                         startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=sunny.app.csnotes")));
                         drawerLayout.closeDrawer(GravityCompat.START);
                         return true;
                     case R.id.trash_nav_id:
                         drawerLayout.closeDrawer(GravityCompat.START);
                         change_to_deleted();
                         return true;

                 }
                 return false;
            }
        });



    }

    private void change_to_personal() {
        toolbar.setTitle(R.string.personal_cat);
        fab_add.setVisibility(View.GONE);
        if(!mydb.haveState(NotesDb.personal)){
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
            adapter.changeCursor(mydb.fetchState(NotesDb.personal));
        }

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
        searchView=(SearchView) menu.findItem(R.id.search).getActionView();
        searchView.setQueryHint("search");
        searchView.setIconifiedByDefault(false);
        searchView.setFocusable(true);
        searchView.setIconified(false);
        searchView.setImeOptions(EditorInfo.IME_ACTION_SEARCH);
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                if(mydb.search(query)!=null){
                    list_li.setVisibility(View.VISIBLE);
                    mylist.setVisibility(View.VISIBLE);
                    nonote_li.setVisibility(View.INVISIBLE);
                    no_image.setVisibility(View.INVISIBLE);
                    no_note.setVisibility(View.INVISIBLE);
                    adapter.changeCursor(mydb.search(query));
                }
                else {
                    list_li.setVisibility(View.INVISIBLE);
                    mylist.setVisibility(View.INVISIBLE);
                    nonote_li.setVisibility(View.VISIBLE);
                    no_image.setVisibility(View.VISIBLE);
                    no_note.setVisibility(View.VISIBLE);
                }
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                if(mydb.search(newText)!=null){
                    list_li.setVisibility(View.VISIBLE);
                    mylist.setVisibility(View.VISIBLE);
                    nonote_li.setVisibility(View.INVISIBLE);
                    no_image.setVisibility(View.INVISIBLE);
                    no_note.setVisibility(View.INVISIBLE);
                    adapter.changeCursor(mydb.search(newText));
                }
                else {
                    list_li.setVisibility(View.INVISIBLE);
                    mylist.setVisibility(View.INVISIBLE);
                    nonote_li.setVisibility(View.VISIBLE);
                    no_image.setVisibility(View.VISIBLE);
                    no_note.setVisibility(View.VISIBLE);
                }
                return false;
            }
        });

        return true;
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {

        searchView.requestFocusFromTouch();
        return true;
    }

    @Override
    protected void onStart() {
        adapter.notifyDataSetChanged();
        super.onStart();
    }

    @Override
    protected void onResume() {
        adapter.notifyDataSetChanged();
        super.onResume();
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
            case R.id.restore_del:
                if(mydb.haveState(NotesDb.deleted)){
                    mydb.restore_del();
                    if(!mydb.haveState(NotesDb.deleted)){
                        displayToast("Restored notes from trash",0);
                        Intent intent=new Intent(MainActivity.this,MainActivity.class);
                        startActivity(intent);
                        overridePendingTransition(R.anim.slide_out_bottom,R.anim.slide_in_bottom);
                        finish();
                    }
                    else {
                        displayToast("Error occurred in restoring notes from trash",0);

                    }
                }
                else{
                    displayToast("Trash is empty",0);
                }
                return true;
            case R.id.delete_all:
                if(mydb.haveEntries()) {
                    AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
                    builder.setMessage("You can't recover again!!!\nExport your database for future usage");
                    builder.setIcon(R.drawable.delete_forever);
                    builder.setTitle("Delete everything");
                    builder.setPositiveButton("Delete", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            if (mydb.delAll()) {
                                displayToast("Deleted Everything successfully",0);
                                Intent intent = new Intent(MainActivity.this, MainActivity.class);
                                startActivity(intent);
                                overridePendingTransition(R.anim.slide_out_bottom,R.anim.slide_in_bottom);
                                finish();
                            } else {
                                displayToast("error occurred in deleting all",0);
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
                    displayToast("No notes to delete",0);
                }

                return true;
            case R.id.clear_del:
                if(mydb.haveState(NotesDb.deleted)) {
                    AlertDialog.Builder builder2 = new AlertDialog.Builder(MainActivity.this);
                    builder2.setMessage("You can't recover again!!!");
                    builder2.setIcon(R.drawable.delete_forever);
                    builder2.setTitle("Clear deleted");
                    builder2.setPositiveButton("Delete", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            if (mydb.clear_deleted()) {
                                displayToast("Trash cleared",0);
                                Intent intent = new Intent(MainActivity.this, MainActivity.class);
                                startActivity(intent);
                                overridePendingTransition(R.anim.slide_out_bottom,R.anim.slide_in_bottom);
                                finish();
                            } else {
                                displayToast("error occurred in clearing trash",0);
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
                    displayToast("Trash is empty",0);
                }
                return true;
            case R.id.export_db_id :
                if(ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.WRITE_EXTERNAL_STORAGE)
                        != PackageManager.PERMISSION_GRANTED){

                    ActivityCompat.requestPermissions(MainActivity.this,new String[]  { Manifest.permission.WRITE_EXTERNAL_STORAGE }, 3);
                    displayToast("Try again after granting access to write storage",1);
                }
                else {
                    File folder = new File("/storage/emulated/0/CS Notes/");
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
                    String backupDBPath= date_time+" Notes.db";
                    File  currentDB= new File(data,currentDBPath);
                    File backupDB= new File(folder,backupDBPath);
                    if(!backupDB.exists()){
                        try {
                            backupDB.createNewFile();
                        } catch (IOException e) {
                            e.printStackTrace();
                            displayToast(e.toString(),1);

                        }
                    }
                    try {
                        source= new FileInputStream(currentDB).getChannel();
                        destination=new FileOutputStream(backupDB).getChannel();
                        destination.transferFrom(source,0,source.size());
                        source.close();
                        destination.close();
                        displayToast("Saved in CS Notes folder\n\nFile name:\n"+backupDBPath,1);
                    }
                    catch (IOException e){
                        displayToast(e.toString(),1);
                    }
                }

                return true;
            case R.id.import_db_id :
                AlertDialog.Builder builder2 = new AlertDialog.Builder(MainActivity.this);
                builder2.setMessage("Your current notes will be deleted!!!\nPlease export database if you have any important notes currently");
                builder2.setIcon(R.drawable.ic_baseline_warning_24);
                builder2.setTitle("Import Database");
                builder2.setPositiveButton("Import", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
                        intent.setType("*/*");
                        startActivityForResult(intent,3333);
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


                return true;
        }
        return false;

    }
    public void refresh_list(){
        CharSequence title = toolbar.getTitle();
        if (all_notes_t.equals(title)) {
            change_to_all();
        } else if (home_t.equals(title)) {
            change_to_home();
        } else if (work_t.equals(title)) {
            change_to_work();
        } else if (education_t.equals(title)) {
            change_to_edu();
        } else if (other_t.equals(title)) {
            change_to_other();
        } else if (personal_t.equals(title)) {
            change_to_personal();
        } else if (archived_t.equals(title)) {
            change_to_arc();
        }
        else if(favourites_t.equals(title)){
            change_to_fav();
        }
        else if(deleted_t.equals(title)){
            change_to_deleted();
        }
    }
    public void change_to_fav(){
        toolbar.setTitle(favourites_t);
        fab_add.setVisibility(View.GONE);
        if(!mydb.haveState(NotesDb.favorite)){
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
            adapter.changeCursor(mydb.fetchState(NotesDb.favorite));
        }
    }

    public void change_to_deleted(){
        fab_add.setVisibility(View.GONE);
        toolbar.setTitle(deleted_t);
        if(!mydb.haveState(NotesDb.deleted)){
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
            adapter.changeCursor(mydb.fetchState(NotesDb.deleted));
        }
    }
    public void change_to_all(){
        toolbar.setTitle(all_notes_t);
        if(mydb.haveState(NotesDb.general) || mydb.haveState(NotesDb.favorite) || mydb.haveState(NotesDb.home) || mydb.haveState(NotesDb.work) || mydb.haveState(NotesDb.education) || mydb.haveState(NotesDb.other)){
            list_li.setVisibility(View.VISIBLE);
            mylist.setVisibility(View.VISIBLE);
            nonote_li.setVisibility(View.INVISIBLE);
            no_image.setVisibility(View.INVISIBLE);
            no_note.setVisibility(View.INVISIBLE);
            adapter.changeCursor(mydb.fetchAll());
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
        if(!mydb.haveState(NotesDb.home)){
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
            Cursor cursor=mydb.fetchState(NotesDb.home);
            adapter.changeCursor(cursor);

        }
    }
    public void change_to_work(){
        toolbar.setTitle(R.string.work_cat);
        fab_add.setVisibility(View.GONE);
        if(!mydb.haveState(NotesDb.work)){
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
            Cursor cursor=mydb.fetchState(NotesDb.work);
            adapter.changeCursor(cursor);
        }
    }
    public void change_to_edu(){
        toolbar.setTitle(R.string.education_cat);
        fab_add.setVisibility(View.GONE);
        if(!mydb.haveState(NotesDb.education)){
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
            Cursor cursor=mydb.fetchState(NotesDb.education);
            adapter.changeCursor(cursor);
        }
    }
    public void change_to_arc(){
        toolbar.setTitle(R.string.archived_cat);
        fab_add.setVisibility(View.GONE);
        if(!mydb.haveState(NotesDb.archived)){
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
            Cursor cursor=mydb.fetchState(NotesDb.archived);
            adapter.changeCursor(cursor);
        }
    }
    public void change_to_other(){
        toolbar.setTitle(R.string.other_cat);
        fab_add.setVisibility(View.GONE);
        if(!mydb.haveState(NotesDb.other)){
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
            Cursor cursor=mydb.fetchState(NotesDb.other);
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
                displayToast(e.toString(),1);
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
                NotesDb s= new NotesDb(MainActivity.this);
                SQLiteDatabase db= s.getReadableDatabase();
                s.onUpgrade(db,db.getVersion(),NotesDb.version);
                displayToast("Imported Successfully",0);
                Intent restart = new Intent( MainActivity.this, MainActivity.class);
                startActivity(restart);
                overridePendingTransition(R.anim.slide_out_bottom,R.anim.slide_in_bottom);
                finish();

            } catch (Exception e) {

                displayToast(e.toString(),1);

            }
            finally {
                try {
                    ip.close();
                } catch (IOException e) {
                    e.printStackTrace();
                    displayToast(e.toString(),1);

                }
            }
        }

    }

    public void change_to_reminders(){
        if(mydb.fetchReminds().getCount()>0){
            toolbar.setTitle(R.string.remainders);
            fab_add.setVisibility(View.GONE);
            list_li.setVisibility(View.VISIBLE);
            mylist.setVisibility(View.VISIBLE);
            nonote_li.setVisibility(View.INVISIBLE);
            no_image.setVisibility(View.INVISIBLE);
            no_note.setVisibility(View.INVISIBLE);
            adapter.changeCursor(mydb.fetchReminds());

        }
        else{
            displayToast("No Reminders Set",0);
        }

    }
    public void displayToast(String string,int t){
        Toast toast;
        if(t==0){
            toast=Toast.makeText(getApplicationContext(),string,Toast.LENGTH_SHORT);
        }
        else{
            toast=Toast.makeText(getApplicationContext(),string,Toast.LENGTH_LONG);
        }
        toast.setGravity(Gravity.CENTER,0,0);
        toast.show();
    }
    public void makeDragable() throws NoSuchFieldException, IllegalAccessException{
            mDragger=drawerLayout.getClass().getDeclaredField("mLeftDragger");
            mDragger.setAccessible(true);
            draggerObj=(ViewDragHelper) mDragger.get(drawerLayout);
            mEdgeSize=draggerObj.getClass().getDeclaredField("mEdgeSize");
            mEdgeSize.setAccessible(true);
            int edge=mEdgeSize.getInt(draggerObj);
            mEdgeSize.setInt(draggerObj,edge*6);
    }



}

