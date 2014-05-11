package com.digiburo.mellow.heeler.lib.content;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.io.File;

/**
 * @author gsc
 */
public class DataBaseHelper extends SQLiteOpenHelper {
  public static final String DATABASE_FILE_NAME = "heeler.db";
  public static final int DATABASE_VERSION = 1;

  //
  public final String LOG_TAG = getClass().getName();

  //
  public DataBaseHelper(Context context) {
    // super(context, DATABASE_FILE_NAME, null, DATABASE_VERSION);
    super(context, context.getExternalFilesDir(null).getAbsolutePath() + File.separator + DATABASE_FILE_NAME, null, DATABASE_VERSION);
  }

  @Override
  public void onCreate(SQLiteDatabase db) {
    db.execSQL(ObservationTable.CREATE_TABLE);
  }

  @Override
  public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
    //empty
  }
}
/*
 * Copyright 2014 Digital Burro, INC
 * Created on May 11, 2014 by gsc
 */
