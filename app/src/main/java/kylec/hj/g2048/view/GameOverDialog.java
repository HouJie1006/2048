package kylec.hj.g2048.view;

import android.content.ContentValues;
import android.content.Context;

import androidx.annotation.NonNull;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.text.TextUtils;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import com.google.android.material.button.MaterialButton;

import kylec.hj.g2048.R;
import kylec.hj.g2048.app.Constant;
import kylec.hj.g2048.db.GameDatabaseHelper;

/**
 * 游戏结束对话框
 * <p>
 * Created by KYLE on 2018/10/6.
 */
public class GameOverDialog extends BaseDialog {

    private String finalScore;
    private String title;
    private String action;
    private EditText info;

    private View.OnClickListener onClickListener;
    private View.OnClickListener onBackClickListener;

    private Context mContext;

    public GameOverDialog(@NonNull Context context, int themeResId) {
        super(context, themeResId);
        this.mContext = context;

    }

    @Override
    protected int setView() {
        return R.layout.dialog_game_over;
    }

    @Override
    protected void initData() {
        init();
    }

    private void init() {
        TextView title = findViewById(R.id.tv_custom_title);
        TextView finalScore = findViewById(R.id.tv_final_score);
        MaterialButton on = findViewById(R.id.tv_share);
        MaterialButton back = findViewById(R.id.tv_go_on);


        if (onClickListener != null) {
            on.setOnClickListener(onClickListener);
        }
        if (onBackClickListener != null) {
            back.setOnClickListener(onBackClickListener);
        }
        if (!TextUtils.isEmpty(this.finalScore)) {
            finalScore.setText(this.finalScore);
        }
        if (!TextUtils.isEmpty(this.title)) {
            title.setText(this.title);
        }
        /**
         * 判断是否显示输入框
         */
        info = findViewById(R.id.et_name);
        if (action.equals(GameView.ACTION_WIN_IN)
                ||action.equals(GameView.ACTION_LOSE_IN)){
            info.setVisibility(View.VISIBLE);
        }else {
            info.setVisibility(View.INVISIBLE);
        }


    }

    public GameOverDialog setOnClickListener(
            View.OnClickListener onClickListener) {
        this.onClickListener = onClickListener;
        return this;
    }

    public GameOverDialog setOnBackClickListener(
            View.OnClickListener onBackClickListener) {
        this.onBackClickListener = onBackClickListener;
        return this;
    }

    /**
     * 设置最终得分
     */
    public GameOverDialog setFinalScore(String finalScore) {
        this.finalScore = finalScore;
        return this;
    }

    /**
     * 设置标题
     */
    public GameOverDialog setTitle(String title) {
        this.title = title;
        return this;
    }

    public GameOverDialog setEdit(String action){
        this.action = action;
        return this;
    }

    public void addInfo(String gameTime){
        GameDatabaseHelper helper = new GameDatabaseHelper(mContext, Constant.DB_NAME,null,1);
        SQLiteDatabase db = helper.getWritableDatabase();
        Cursor cursor = db.query("info",null,null,null,null,null,"score desc");
        if (cursor.getCount() == 10){
            cursor.moveToLast();
            db.delete("info","id=?",new String[]{String.valueOf(cursor.getInt(0))});
        }
        ContentValues values = new ContentValues();
        values.put("user_name",info.getText().toString());
        values.put("score",finalScore);
        values.put("time",gameTime);
        db.insert("info",null,values);
        db.close();

    }

}
