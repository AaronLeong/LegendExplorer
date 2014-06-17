
package com.example.legendexplorer.db;

import java.util.ArrayList;

import com.example.legendexplorer.model.FileItem;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Environment;

public class BookmarkHelper {

    private final Context mContext;
    private FileDataBase mDbHelper;
    private SQLiteDatabase mSqlDB;

    public BookmarkHelper(Context context) {
        this.mContext = context;
    }

    public BookmarkHelper open() {
        mDbHelper = FileDataBase.getInstance(mContext);
        mSqlDB = mDbHelper.getWritableDatabase();
        return this;
    }

    public void close() {
        if (mDbHelper != null) {
            mDbHelper.close();
        }
    }

    public boolean insertBookmark(ArrayList<FileItem> list) {
        try {
            mSqlDB.beginTransaction();
            for (FileItem item : list) {
                insert(item);
            }
            mSqlDB.setTransactionSuccessful();
        } finally {
            mSqlDB.endTransaction();
        }
        return true;
    }

    public long insert(FileItem item) {
        long ret = -1;
        ContentValues values = new ContentValues();
        values.put(BookmarkColumn.FILE_NAME, item.getName());
        values.put(BookmarkColumn.FILE_PATH, item.getAbsolutePath());
        ret = mSqlDB.insert(FileDataBase.TableBookmark, null, values);
        return ret;
    }

    public ArrayList<FileItem> getBookmarks() {

        ArrayList<FileItem> list = new ArrayList<FileItem>();

        Cursor cursor = mSqlDB.query(FileDataBase.TableBookmark,
                BookmarkColumn.PROJECTION, null, null, null, null, null,
                String.valueOf(10));

        if (cursor.getCount() > 0) {
            for (cursor.moveToFirst(); !cursor.isAfterLast(); cursor.moveToNext()) {
                String filePath = cursor.getString(BookmarkColumn.FILE_PATH_COLUMN);
                FileItem item = new FileItem(filePath);
                list.add(item);
            }
        }

        cursor.close();
        cursor = null;

        return list;

    }

    public boolean truncate() {
        int ret = mSqlDB.delete(FileDataBase.TableBookmark, null, null);
        return ret > 0 ? true : false;
    }

    public void initBookmarks() {

        ArrayList<FileItem> fileItems = new ArrayList<FileItem>();
        FileItem item_download = new FileItem(
                Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS));
        FileItem item_camera = new FileItem(
                Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM));
        FileItem item_movie = new FileItem(
                Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_MOVIES));
        FileItem item_music = new FileItem(
                Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_MUSIC));
        FileItem item_picture = new FileItem(
                Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES));

        fileItems.add(item_picture);
        fileItems.add(item_music);
        fileItems.add(item_movie);
        fileItems.add(item_camera);
        fileItems.add(item_download);

        open();
        truncate();
        insertBookmark(fileItems);
        close();
    }
}