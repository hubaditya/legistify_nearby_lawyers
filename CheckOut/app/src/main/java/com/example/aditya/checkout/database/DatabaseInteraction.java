package com.example.aditya.checkout.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.widget.Toast;

import com.example.aditya.checkout.model.ColorList;

public class DatabaseInteraction extends SQLiteOpenHelper
{
    private static final int DATABASE_VERSION = 1;
    private static final String DATABASE_NAME = "ServiceManager";
    private static final String TABLE_SERVICE = "Service";
    private SQLiteDatabase db;

    Context context;

    private static final String ID = "id";
    private static final String COLORS = "color";
    private static final String NAME = "name";

    public DatabaseInteraction(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
        this.context = context;
    }

    @Override
    public void onCreate(SQLiteDatabase db)
    {
        String CREATE_SERVICE_TABLE="CREATE TABLE "+TABLE_SERVICE+ " ( " + ID + " INTEGER PRIMARY KEY, " + NAME + " TEXT, "
                + COLORS + " TEXT " + " ) ";
        db.execSQL(CREATE_SERVICE_TABLE);

    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int i, int i1) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_SERVICE);
        onCreate(db);
    }

    public long addItem(ColorList item)
    {
        db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLORS, item.getColor());
        values.put(NAME, item.getName());
        long rowID = db.insert(TABLE_SERVICE, null, values);
        db.close();
        return rowID;
    }

    public void updateItemZero(String name, String color)
    {
        db = this.getWritableDatabase();
        db.execSQL("UPDATE " + TABLE_SERVICE + " SET " + COLORS + " = ? WHERE " + NAME + " = '" + name + "'",
                new String[]{ color });
        db.execSQL("UPDATE " + TABLE_SERVICE + " SET " + COLORS + " = 1 WHERE " + NAME + " = 'Registration'");
    }

    public void updateItemOne(String name, String color)
    {
        db = this.getWritableDatabase();
        db.execSQL("UPDATE " + TABLE_SERVICE + " SET " + COLORS + " = 0");
        db.execSQL("UPDATE " + TABLE_SERVICE + " SET " + COLORS + " = ? WHERE " + NAME + " = '" + name + "'",
                new String[]{ color });
    }

    public Cursor getItemDetail(String name)
    {
        db = this.getReadableDatabase();
        String selectQuery = "SELECT  * FROM " + TABLE_SERVICE + " WHERE " + NAME + " = '" +name+"'";
        Cursor c = db.rawQuery(selectQuery, null);
        return c;
    }

    public Cursor getAllItems()
    {
        db = this.getReadableDatabase();
        String selectQuery = "SELECT  * FROM " + TABLE_SERVICE;
        Cursor c = db.rawQuery(selectQuery, null);
        return c;
    }
}
