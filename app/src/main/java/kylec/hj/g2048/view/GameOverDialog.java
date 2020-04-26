package kylec.hj.g2048.view;

import android.content.ContentValues;
import android.content.Context;

import androidx.annotation.NonNull;

import android.content.pm.PackageManager;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.text.TextUtils;
import android.view.LayoutInflater;
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

    private View.OnClickListener onShareClickListener;
    private View.OnClickListener onGoOnClickListener;

    private Context friendContext;
    private Context mContext;
    private GameDatabaseHelper helper;

    public GameOverDialog(@NonNull Context context, int themeResId) {
        super(context, themeResId);
        this.mContext = context;
        //创建数据库
        try {
            friendContext = mContext.createPackageContext("com.hj.datafor2048"
                    ,Context.CONTEXT_IGNORE_SECURITY);
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        helper = new GameDatabaseHelper(friendContext, Constant.DB_NAME,null,1);
    }

    @Override
    protected int setView() {
        return R.layout.dialog_game_over;
    }

    @Override
    protected void initData() {
        init();
    }

    // todo 增加保存到数据库的操作
    private void init() {
        TextView title = findViewById(R.id.tv_custom_title);
        TextView finalScore = findViewById(R.id.tv_final_score);
        MaterialButton share = findViewById(R.id.tv_share);
        MaterialButton goOn = findViewById(R.id.tv_go_on);


        if (onShareClickListener != null) {
            share.setOnClickListener(onShareClickListener);
        }
        if (onGoOnClickListener != null) {
            goOn.setOnClickListener(onGoOnClickListener);
        }
        if (!TextUtils.isEmpty(this.finalScore)) {
            finalScore.setText(this.finalScore);
        }
        if (!TextUtils.isEmpty(this.title)) {
            title.setText(this.title);
        }
        /**
         * 获取输入名
         */
        info = findViewById(R.id.et_name);
        if (action.equals(GameView.ACTION_WIN_IN)
                ||action.equals(GameView.ACTION_LOSE_IN)){
            info.setVisibility(View.VISIBLE);
        }else {
            info.setVisibility(View.INVISIBLE);
        }


    }

    public GameOverDialog setOnShareClickListener(
            View.OnClickListener onShareClickListener) {
        this.onShareClickListener = onShareClickListener;
        return this;
    }

    public GameOverDialog setOnGoOnClickListener(
            View.OnClickListener onGoOnClickListener) {
        this.onGoOnClickListener = onGoOnClickListener;
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
        SQLiteDatabase db = helper.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("user_name",info.getText().toString());
        values.put("score",finalScore);
        values.put("time",gameTime);
        db.insert("info",null,values);
        db.close();

    }

}
