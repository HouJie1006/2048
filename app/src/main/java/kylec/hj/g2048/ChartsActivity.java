package kylec.hj.g2048;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Context;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;

import java.util.ArrayList;
import java.util.List;

import kylec.hj.g2048.adapter.ChartsAdapter;
import kylec.hj.g2048.app.Constant;
import kylec.hj.g2048.db.GameDatabaseHelper;
import kylec.hj.g2048.db.Gamer;

public class ChartsActivity extends AppCompatActivity {

    List<Gamer> mGamer = new ArrayList<>();
    private Context friendContext;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_charts);
        getData();
        RecyclerView recyclerView = findViewById(R.id.recycle_view);
        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(layoutManager);
        ChartsAdapter adapter = new ChartsAdapter(mGamer);
        recyclerView.setAdapter(adapter);
    }

    public void getData(){
        try {
            friendContext = this.createPackageContext("com.hj.datafor2048"
                    ,Context.CONTEXT_IGNORE_SECURITY);
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        GameDatabaseHelper helper = new GameDatabaseHelper(friendContext, Constant.DB_NAME,null,1);
        SQLiteDatabase db = helper.getWritableDatabase();
        Cursor cursor = db.query("info",null,null,null,null,null,"score desc");
        if (cursor != null){
            while (cursor.moveToNext()){
                Integer id = cursor.getInt(cursor.getColumnIndex("id"));
                String name = cursor.getString(cursor.getColumnIndex("user_name"));
                int score = cursor.getInt(cursor.getColumnIndex("score"));
                String time = cursor.getString(cursor.getColumnIndex("time"));
                mGamer.add(new Gamer(id,name,score,time));
            }
        }
        db.close();
    }
}
