package com.example.night;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.DatabaseUtils;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import androidx.annotation.Nullable;




public class NotesDb extends SQLiteOpenHelper {
public static final String DatabaseName="Notes.db";
public static final String TableName="Notes_Table";
public static final String Notes_C="notes";
public static final String Id="_id";
public static final String Date_C="date";
public static final String Time_C="time";
public static final String State_C="state";



    public NotesDb(@Nullable Context context) {
        super(context,DatabaseName,null,1);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL("CREATE TABLE IF NOT EXISTS Notes_Table ( _id INTEGER PRIMARY KEY , notes TEXT NOT NULL , date TEXT, time TEXT, state INTEGER DEFAULT 1 )");
     }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS Notes_Table");
        onCreate(db);

    }
    public boolean insertNotes(String notes,String time,String date){
        SQLiteDatabase db=this.getWritableDatabase();
        ContentValues contentValues=new ContentValues();
        contentValues.put(Notes_C,notes);
        contentValues.put(Date_C,date);
        contentValues.put(Time_C,time);
        db.insert(TableName,null,contentValues);
        return true;

    }



    public Cursor getData(int id){
        SQLiteDatabase db=this.getReadableDatabase();
        return db.rawQuery("select * from "+TableName+" where _id= "+id+" ",null);
    }
    public boolean haveEntries(){
        SQLiteDatabase db=this.getReadableDatabase();
        int numrows=(int) DatabaseUtils.queryNumEntries(db,TableName);
        return numrows > 0;
    }
    public String getNote(int id){
        String note="";
        SQLiteDatabase db=this.getReadableDatabase();
        Cursor cursor= db.rawQuery("select notes from "+TableName+" where _id= "+id+" ",null);
        if(cursor!=null && cursor.moveToFirst()){
            note=cursor.getString(cursor.getColumnIndex(Notes_C));
            cursor.close();
            return note;
        }
        return note;
    }
    public int getState(int id){
        int state_v=1;
        SQLiteDatabase db=this.getReadableDatabase();
        Cursor rs = db.rawQuery("select state from "+TableName+" where _id= "+id+" ;",null);
        if(rs!=null){
            rs.moveToFirst();
            state_v= rs.getInt(rs.getColumnIndex(NotesDb.State_C));
            rs.close();
            return state_v;
        }
        return state_v;
    }

    public boolean updateNotes(Integer id,String notes,String time,String date){
        SQLiteDatabase db=this.getWritableDatabase();
        ContentValues contentValues=new ContentValues();
        contentValues.put(Notes_C,notes);
        contentValues.put(Time_C,time);
        contentValues.put(Date_C,date);
        db.update(TableName,contentValues,"_id=?",new String[]{Integer.toString(id)});
        return true;
    }

    public int deleteNotes_per(Integer id){
        SQLiteDatabase db=this.getWritableDatabase();
        return db.delete(TableName,"_id=?",new String[]{ Integer.toString(id) });
    }
    public void restore_del(){
        SQLiteDatabase db=this.getWritableDatabase();
        db.execSQL("update "+TableName+" set state=1 where state=3");
    }
    public boolean haveState(String state){
        SQLiteDatabase db1=this.getReadableDatabase();
        Cursor c=db1.rawQuery("select * from "+ TableName+ " where state="+state,null);
        if(c.getCount()>0){
            c.close();
            return true;
        }
        c.close();
        return false;
    }
    public boolean clear_deleted(){
        SQLiteDatabase db=this.getWritableDatabase();
        db.delete(TableName,"state=3",null);
        if(haveState("3")){
            return false;
        }
        return true;
    }
    public boolean delAll(){
        SQLiteDatabase db=this.getWritableDatabase();
        db.execSQL("delete from "+ TableName);
        if(!haveEntries()){
            return true;
        }
        return false;
    }
    public boolean changeState(int id,int state){
        SQLiteDatabase db=this.getWritableDatabase();
        ContentValues contentValues=new ContentValues();
        contentValues.put(State_C,state);
        db.update(TableName,contentValues,"_id=?",new String[]{Integer.toString(id)});
        return true;
    }
    public Cursor fetchAll(){
        SQLiteDatabase db=this.getReadableDatabase();
        Cursor cursor=db.rawQuery("select * from "+TableName+" where state!=3 and state!=8 order by _id DESC ;",null);
        if(cursor!=null){
            cursor.moveToFirst();
        }
        return cursor;
    }
    public Cursor fetchDel(){
        SQLiteDatabase db=this.getReadableDatabase();
        Cursor cursor=db.rawQuery("select * from "+TableName+" where state=3  order by _id DESC ;",null);
        if(cursor!=null){
            cursor.moveToFirst();
        }
        return cursor;
    }
    public Cursor fetchFav(){
        SQLiteDatabase db=this.getReadableDatabase();
        Cursor cursor=db.rawQuery("select * from "+TableName+" where state=2 order by _id DESC ;",null);
        if(cursor!=null){
            cursor.moveToFirst();
        }
        return cursor;
    }
    public Cursor fetchCat(String state){
        SQLiteDatabase db=this.getReadableDatabase();
        Cursor cursor=db.rawQuery("select * from "+TableName+" where state="+state+" order by _id DESC ;",null);
        if(cursor!=null){
            cursor.moveToFirst();
        }
        return cursor;
    }
    public Cursor fetchPersonal(){
        SQLiteDatabase db=this.getReadableDatabase();
        Cursor cursor=db.rawQuery("select * from "+TableName+" where state=8 order by _id DESC ;",null);
        if(cursor!=null){
            cursor.moveToFirst();
        }
        return cursor;
    }


    public int getMaxId(){
        int id=0;
        SQLiteDatabase db=this.getReadableDatabase();
        Cursor cursor=db.rawQuery("select  MAX(_id) FROM "+TableName+" ;",null);
        if(cursor.getCount()>0){
            cursor.moveToFirst();
            id=cursor.getInt(0);
            cursor.close();
            return id;
        }
        return id;

    }



}
