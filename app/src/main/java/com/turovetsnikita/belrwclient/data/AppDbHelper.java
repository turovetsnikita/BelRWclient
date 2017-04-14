package com.turovetsnikita.belrwclient.data;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

/**
 * Created by Nikita on 1.4.17.
 */

public class AppDbHelper extends SQLiteOpenHelper {

    public static final String LOG_TAG = AppDbHelper.class.getSimpleName();

    /**
     * Имя файла базы данных
     */
    private static final String DATABASE_NAME = "rwapp.db";

    /**
     * Версия базы данных. При изменении схемы увеличить на единицу
     */
    private static final int DATABASE_VERSION = 1;

    /**
     * Конструктор {@link AppDbHelper}.
     *
     * @param context Контекст приложения
     */
    public AppDbHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    /**
     * Вызывается при создании базы данных
     */
    @Override
    public void onCreate(SQLiteDatabase db) {
        String SQL_CREATE_HISTORY_TABLE = "CREATE TABLE " + AppBase.RecentDirections.TABLE_NAME + " ("
                + AppBase.RecentDirections._ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
                + AppBase.RecentDirections.depart + " TEXT NOT NULL, "
                + AppBase.RecentDirections.arrival + " TEXT NOT NULL);";

        db.execSQL(SQL_CREATE_HISTORY_TABLE);

        String SQL_CREATE_TRAIN_TABLE = "CREATE TABLE " + AppBase.TrainCache.TABLE_NAME + " ("
                + AppBase.TrainCache._ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
                + AppBase.TrainCache.num + " TEXT NOT NULL, "
                + AppBase.TrainCache.lines + " TEXT NOT NULL, "
                + AppBase.TrainCache.tr_route + " TEXT NOT NULL, "
                + AppBase.TrainCache.t_depart + " TEXT NOT NULL, "
                + AppBase.TrainCache.t_travel + " TEXT NOT NULL, "
                + AppBase.TrainCache.t_arrival + " TEXT NOT NULL, "
                + AppBase.TrainCache.train_seats + " TEXT NOT NULL, "
                + AppBase.TrainCache.train_price + " TEXT NOT NULL, "
                + AppBase.TrainCache.reg + " TEXT NOT NULL, "
                + AppBase.TrainCache.alldays + " TEXT NOT NULL, "
                + AppBase.TrainCache.routehref + " TEXT NOT NULL, "
                + AppBase.TrainCache.seatshref + " TEXT NOT NULL);";

        db.execSQL(SQL_CREATE_TRAIN_TABLE);
    }

    /**
     * Вызывается при обновлении схемы базы данных
     */
    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        Log.w("SQLite", "Обновляемся с версии " + oldVersion + " на версию " + newVersion);
        db.execSQL("DROP TABLE IF IT EXISTS " + AppBase.RecentDirections.TABLE_NAME);
        onCreate(db);
    }
}