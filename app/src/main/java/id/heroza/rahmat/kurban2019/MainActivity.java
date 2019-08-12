package id.heroza.rahmat.kurban2019;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;
import android.widget.TextView;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;

public class MainActivity extends AppCompatActivity implements View.OnKeyListener {
    Button btnAdd, btnClear;
    ListView lstView;
    EditText txtCompanyName;
    DatabaseController dbController;
    int gagal, sukses, maxkupon;
    TextView sisaTV, suksesTV, gagalTV;
    private static final int READ_REQUEST_CODE = 42;
    private static final String PREFS_NAME = "MyPrefsFile";
    SharedPreferences settings;

    public class ExportDatabaseCSVTask extends AsyncTask<String, Void, Boolean> {

        private final ProgressDialog dialog = new ProgressDialog(MainActivity.this);

        @Override
        protected void onPreExecute() {
            this.dialog.setMessage("Exporting database...");
            this.dialog.show();
            dbController = new DatabaseController(MainActivity.this);
        }

        protected Boolean doInBackground(final String... args) {

            File exportDir = new File(Environment.getExternalStorageDirectory(), "/codesss/");
            if (!exportDir.exists()) { exportDir.mkdirs(); }

            File file = new File(exportDir, "kupon2019.csv");
            try {
                file.createNewFile();
                CSVWriter csvWrite = new CSVWriter(new FileWriter(file));
                Cursor curCSV = dbController.raw();
                csvWrite.writeNext(curCSV.getColumnNames());
                while(curCSV.moveToNext()) {
                    String arrStr[]=null;
                    String[] mySecondStringArray = new String[curCSV.getColumnNames().length];
                    for(int i=0;i<curCSV.getColumnNames().length;i++)
                    {
                        mySecondStringArray[i] =curCSV.getString(i);
                    }
                    csvWrite.writeNext(mySecondStringArray);
                }
                csvWrite.close();
                curCSV.close();
                return true;
            } catch (IOException e) {
                return false;
            }
        }

        protected void onPostExecute(final Boolean success) {
            if (this.dialog.isShowing()) { this.dialog.dismiss(); }
            if (success) {
                Toast.makeText(MainActivity.this, "Export successful!", Toast.LENGTH_SHORT).show();
                //ShareFile();
            } else {
                Toast.makeText(MainActivity.this, "Export failed", Toast.LENGTH_SHORT).show();
            }
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        txtCompanyName = (EditText) findViewById(R.id.txtCompanyName);
        txtCompanyName.setOnKeyListener(this);
        lstView = (ListView) findViewById(R.id.lstView);
        sisaTV = (TextView) findViewById(R.id.sisa);
        suksesTV = (TextView) findViewById(R.id.sukses);
        gagalTV = (TextView) findViewById(R.id.gagal);




        settings = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);

        if (settings.getBoolean("my_first_time", true)) {
            Log.d("Comments", "First time");

            maxkupon = 0;
            gagal = 0;
            sukses = 0;

            settings.edit().putBoolean("my_first_time", false).apply();
        }
        else {
            Log.d("Comments", "Not the first time");
            maxkupon = settings.getInt("maxkupon",0);
            gagal = settings.getInt("gagal",0);
            sukses = settings.getInt("sukses",0);
            FillList();
        }
        refreshDisplay();
    }

    @Override
    protected void onDestroy() {

        Log.d("Comments","onDestroy. sukses="+sukses);


        settings.edit().putInt("maxkupon", maxkupon).apply();
        settings.edit().putInt("sukses", sukses).apply();
        settings.edit().putInt("gagal", gagal).apply();
        super.onDestroy();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.main_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            // action with ID action_refresh was selected
            case R.id.action_reset:
                dbController.reset();
                FillList();
                gagal = 0;
                sukses = 0;
                refreshDisplay();
                break;
            case R.id.action_browse:
                Intent intent = new Intent(this, Browse.class);
                startActivity(intent);
                break;
            case R.id.action_import:
                performFileSearch();
                break;
            case R.id.action_export:
                exportDB();
                break;
            default:
                break;
        }

        return true;
    }

    public void AddData(View v) {
        String kodekupon = txtCompanyName.getText().toString();
        try {
            if (TextUtils.isEmpty(kodekupon))
                Toast.makeText(this, "Masukkan kode kupon", Toast.LENGTH_SHORT).show();
            else {
                dbController = new DatabaseController(this);
                int retval = dbController.Redeem(Integer.parseInt(kodekupon));
                if (retval == 1)
                {
                    FillList();
                    sukses++;
                }
                else{ // retval = 0
                    String s = dbController.getTimeofRedeem(Integer.parseInt(txtCompanyName.getText().toString()));
                    if (s == "00:00:00")
                        Toast.makeText(this, "Kupon "+kodekupon+" tidak ada", Toast.LENGTH_SHORT).show();
                    else
                        Toast.makeText(this, "Kupon "+kodekupon+" sudah dipakai pada "+s, Toast.LENGTH_SHORT).show();
                    gagal++;
                }

                txtCompanyName.setText("");



            }
        } catch (Exception ex) {
            Toast.makeText(this, ex.getMessage(), Toast.LENGTH_SHORT).show();
            txtCompanyName.setText("");
        }
    }

    public void FillDB(String[] tokens){
        dbController = new DatabaseController(this);
        dbController.deleteAll();
        for (String token: tokens) {
            dbController.InsertData(token);
        }
        maxkupon = tokens.length;
    }

    private void refreshDisplay(){
        suksesTV.setText(String.valueOf(sukses));
        sisaTV.setText(String.valueOf(maxkupon-sukses));
        gagalTV.setText(String.valueOf(gagal));
    }

    public void FillList() {
        try {
            int[] id = {R.id.txtListElement, R.id.txtListElement2};
            String[] CompanyName = new String[]{"KodeKupon","Timestamp"};
            if (dbController == null)
                dbController = new DatabaseController(this);
            SQLiteDatabase sqlDb = dbController.getReadableDatabase();
            Cursor c = dbController.getRedeemedKupon();

            SimpleCursorAdapter adapter = new SimpleCursorAdapter(this,
                    R.layout.list_template, c, CompanyName, id, 0);
            lstView.setAdapter(adapter);

        } catch (Exception ex) {
            Toast.makeText(MainActivity.this, ex.getMessage().toString(), Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public boolean onKey(View view, int keyCode, KeyEvent event) {
        EditText myEditText = (EditText) view;

        if (keyCode == EditorInfo.IME_ACTION_SEARCH ||
                keyCode == EditorInfo.IME_ACTION_DONE ||
                event.getAction() == KeyEvent.ACTION_DOWN &&
                        event.getKeyCode() == KeyEvent.KEYCODE_ENTER) {

            if (!event.isShiftPressed()) {
                AddData(null);
                refreshDisplay();
                return true;
            }

        }
        return false; // pass on to other listeners.

    }

    public void performFileSearch() {
        Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        intent.setType("text/plain");
        startActivityForResult(intent, READ_REQUEST_CODE);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent resultData) {
        if (requestCode == READ_REQUEST_CODE && resultCode == Activity.RESULT_OK) {
            Uri uri = null;
            String s = null;
            if (resultData != null) {
                uri = resultData.getData();
                try {
                    s = readTextFromUri(uri);
                    Log.d("texts",s);
                    FillDB(s.split("-"));
                    refreshDisplay();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    private String readTextFromUri(Uri uri) throws IOException {
        InputStream inputStream = getContentResolver().openInputStream(uri);
        BufferedReader reader = new BufferedReader(new InputStreamReader(
                inputStream));
        ArrayList<String> stringBuilder = new ArrayList<String>();
        String line;
        while ((line = reader.readLine()) != null) {
            stringBuilder.add(line);
        }
        inputStream.close();
        return TextUtils.join("-",stringBuilder);
    }

    public void exportDB(){
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
            new ExportDatabaseCSVTask().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
        } else {
            new ExportDatabaseCSVTask().execute();
        }
    }

    private void ShareFile() {
        File exportDir = new File(Environment.getExternalStorageDirectory(), "/codesss/");
        String fileName = "kupon.csv";
        File sharingGifFile = new File(exportDir, fileName);
        Intent shareIntent = new Intent(android.content.Intent.ACTION_SEND);
        shareIntent.setType("application/csv");
        Uri uri = Uri.fromFile(sharingGifFile);
        shareIntent.putExtra(Intent.EXTRA_STREAM, uri);
        startActivity(Intent.createChooser(shareIntent, "Share CSV"));
    }
}