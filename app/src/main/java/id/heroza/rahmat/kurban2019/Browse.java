package id.heroza.rahmat.kurban2019;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.os.PersistableBundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.FilterQueryProvider;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;
import android.widget.Toast;

public class Browse extends AppCompatActivity implements View.OnKeyListener {
    ListView lstView;
    EditText txtCompanyName;
    DatabaseController dbController;
    SimpleCursorAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_browse);
        txtCompanyName = (EditText) findViewById(R.id.txtCompanyName);
        txtCompanyName.setOnKeyListener(this);
        lstView = (ListView) findViewById(R.id.lstView);
        FillList();
    }

    public void FillList() {
        try {
            int[] id = {R.id.txtListElement, R.id.txtListElement2};
            String[] CompanyName = new String[]{"KodeKupon","Timestamp"};
            if (dbController == null)
                dbController = new DatabaseController(this);
            final SQLiteDatabase sqlDb = dbController.getReadableDatabase();
            Cursor c = dbController.getAllKupon();

            adapter = new SimpleCursorAdapter(this,
                    R.layout.list_template, c, CompanyName, id, 0);
            lstView.setAdapter(adapter);
            adapter.setFilterQueryProvider(new FilterQueryProvider() {

                @Override
                public Cursor runQuery(CharSequence constraint) {
                    String partialValue = constraint.toString();
                    return dbController.getFilteredKupon(partialValue);

                }
            });

        } catch (Exception ex) {
            Toast.makeText(Browse.this, ex.getMessage().toString(), Toast.LENGTH_SHORT).show();
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
                adapter.getFilter().filter(txtCompanyName.getText().toString());
                txtCompanyName.setText("");
                return true;
            }

        }
        return false; // pass on to other listeners.

    }
}
