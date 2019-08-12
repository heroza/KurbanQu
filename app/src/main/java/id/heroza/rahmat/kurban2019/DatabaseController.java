package id.heroza.rahmat.kurban2019;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;
import android.widget.Toast;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class DatabaseController extends SQLiteOpenHelper {
    private static final String LOGCAT = null;

    public DatabaseController(Context applicationcontext) {
        super(applicationcontext, "Companies.db", null, 1);
        Log.d(LOGCAT, "Created");
    }

    @Override
    public void onCreate(SQLiteDatabase database) {
        String query;
        query = "CREATE TABLE IF NOT EXISTS tblCompanies ( _id INTEGER PRIMARY KEY, KodeKupon INTEGER, Timestamp DATETIME)";
        database.execSQL(query);
    }

    public String InsertData(String kodeKupon) {
        try {
            SQLiteDatabase database = this.getWritableDatabase();
            String query = "insert into tblCompanies (KodeKupon, Timestamp) values ('" + kodeKupon + "', '00:00:00')";
            database.execSQL(query);
            database.close();
            return "Added Successfully";
        } catch (Exception ex) {
            return ex.getMessage().toString();
        }

    }

    public void deleteAll(){
        SQLiteDatabase database = this.getWritableDatabase();
        String query = "delete from tblCompanies";
        database.execSQL(query);
        database.close();
    }

    public void reset(){
        SQLiteDatabase database = this.getWritableDatabase();
        ContentValues newValues = new ContentValues();
        newValues.put("Timestamp", "00:00:00");
        String[] args = new String[]{"00:00:00"};
        database.update("tblCompanies", newValues, "Timestamp != ?", args);
        database.close();
    }

    private String getDateTime() {
        SimpleDateFormat dateFormat = new SimpleDateFormat(
                "HH:mm:ss", Locale.getDefault());
        Date date = new Date();
        return dateFormat.format(date);
    }

    public int Redeem(int kodeKupon) {
        try {
            SQLiteDatabase database = this.getWritableDatabase();

            ContentValues newValues = new ContentValues();
            String s = getDateTime();
            newValues.put("Timestamp", s);
            Log.d("time", s);
            Log.d("kodekupon string", String.valueOf(kodeKupon));

            String[] args = new String[]{String.valueOf(kodeKupon), "00:00:00"};
            int retval = database.update("tblCompanies", newValues, "KodeKupon=? AND Timestamp=?", args);
            Log.d("retval", String.valueOf(retval));
            database.close();
            return retval;
        } catch (Exception ex) {
            return 0;
        }

    }

    @Override
    public void onUpgrade(SQLiteDatabase database, int version_old,
                          int current_version) {
        String query;
        query = "DROP TABLE IF EXISTS tblCompanies";
        database.execSQL(query);
        onCreate(database);
    }

    public Cursor getCompanies() {
        try {
            String selectQuery = "SELECT * FROM tblCompanies order by _id desc";
            SQLiteDatabase database = this.getWritableDatabase();
            Cursor cursor = database.rawQuery(selectQuery, null);
            return cursor;
        } catch (Exception ex) {
            return null;
        }
    }

    public Cursor getAllKupon() {
        try {
            String selectQuery = "SELECT * FROM tblCompanies order by _id asc ";
            SQLiteDatabase database = this.getReadableDatabase();
            Cursor cursor = database.rawQuery(selectQuery, null);
            return cursor;
        } catch (Exception ex) {
            return null;
        }
    }

    public Cursor getRedeemedKupon() {
        try {
            String selectQuery = "SELECT * FROM tblCompanies where Timestamp != '00:00:00' order by Timestamp desc ";
            SQLiteDatabase database = this.getReadableDatabase();
            Cursor cursor = database.rawQuery(selectQuery, null);
            return cursor;
        } catch (Exception ex) {
            return null;
        }
    }

    public Cursor getFilteredKupon(String query) {
        String selectQuery;
        Log.d("comments",query);
        try {
            if (query == "") {
                selectQuery = "SELECT * FROM tblCompanies order by _id asc ";
                Log.d("comments 1",query);
            }
            else
            {
                selectQuery = "SELECT * FROM tblCompanies where KodeKupon = '"+query+"' order by _id asc ";
                Log.d("comments 2",query);
            }
            SQLiteDatabase database = this.getReadableDatabase();
            Cursor cursor = database.rawQuery(selectQuery, null);
            return cursor;
        } catch (Exception ex) {
            return null;
        }
    }

    public String getTimeofRedeem(int i) {
        String retval = "00:00:00";
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor res = db.rawQuery( "select * from tblCompanies where KodeKupon = ?", new String[] {String.valueOf(i)});
        res.moveToFirst();
        while(res.isAfterLast() == false) {
            retval = res.getString(res.getColumnIndex("Timestamp"));
            res.moveToNext();
        }
        return retval;
    }

    public Cursor raw() {

        SQLiteDatabase db = this.getReadableDatabase();
        Cursor res = db.rawQuery("SELECT * FROM tblCompanies" , new String[]{});

        return res;
    }
}