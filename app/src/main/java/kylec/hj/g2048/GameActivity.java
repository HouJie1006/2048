package kylec.hj.g2048;

import android.annotation.SuppressLint;
import android.content.BroadcastReceiver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.style.AbsoluteSizeSpan;
import android.text.style.ForegroundColorSpan;
import android.text.style.StyleSpan;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.ColorUtils;

import java.util.ArrayList;
import java.util.Timer;
import java.util.TimerTask;

import kylec.hj.g2048.app.Config;
import kylec.hj.g2048.app.ConfigManager;
import kylec.hj.g2048.app.Constant;
import kylec.hj.g2048.db.CellEntity;
import kylec.hj.g2048.db.GameDatabaseHelper;
import kylec.hj.g2048.utils.TimeUtils;
import kylec.hj.g2048.view.CommonDialog;
import kylec.hj.g2048.view.ConfigDialog;
import kylec.hj.g2048.view.GameOverDialog;
import kylec.hj.g2048.view.GameView;

/**
 * 游戏界面
 */
public class GameActivity extends AppCompatActivity {

    private TextView currentScores;
    private TextView bestScores;
    private TextView bestScoresRank;
    private TextView titleDescribe;
    private TextView tvTitle;
    private Button menu;
    private Button reset;
    private Button back;
    private GameView gameView;

    private BroadcastReceiver myReceiver;
    private ConfigDialog configDialog;

    private GameDatabaseHelper gameDatabaseHelper;
    private SQLiteDatabase db;
    private Context friendContext;

    private boolean isNeedSave = true;

    private Timer timer;

    private Timer gameTime;
    private boolean isPause = false;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Intent intent = getIntent();
        String data = intent.getStringExtra("gameStatus");
        initView(data);
        initData();
        getTime();
    }

    /**
     * 游戏计时器
     */
    private void getTime(){
        @SuppressLint("HandlerLeak")
        final Handler startTimeHandler = new Handler(){
            public void handleMessage(android.os.Message msg) {
                if (null != titleDescribe) {
                    titleDescribe.setText((String) msg.obj);
                    }
                }
        };
        gameTime = new Timer();
        gameTime.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                try {
                    while(!isPause) {
                        Config.currentSecond += 1000;
                        ConfigManager.putCurrentSecond(GameActivity.this,Config.currentSecond);
                        String time = TimeUtils.getFormatHMS(Config.currentSecond);
                        Message msg = new Message();
                        msg.obj = time;
                        startTimeHandler.sendMessage(msg);

                        Thread.sleep(1000);
                    }
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }

            }
        },0,1000L);

    }

    @Override
    protected void onPause() {
        super.onPause();
        isPause = true;
    }

    @Override
    protected void onRestart() {
        super.onRestart();
        isPause = false;
    }

    @Override
    protected void onResume() {
        super.onResume();

        if (null == timer) {
            timer = new Timer();
            startTiming();
        }
    }

    private void startTiming() {
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                if (isNeedSave) {
                    // 10S保存一次游戏进度
                    saveGameProgress();
                }
            }
        }, 5 * 1000, 10 * 1000);
    }

    @Override
    protected void onDestroy() {
        // 取消注册广播
        if (myReceiver != null) {
            unregisterReceiver(myReceiver);
            myReceiver = null;
        }

        if (null != timer) {
            timer.cancel();
            timer.purge();
            timer = null;
        }
        if (null != gameDatabaseHelper) {
            gameDatabaseHelper.close();
            gameDatabaseHelper = null;
        }
        if (gameTime != null){
            gameTime.cancel();
            gameTime.purge();
            gameTime = null;
        }

        super.onDestroy();
    }

    /**
     * 初始化视图
     */
    private void initView(String gameStatus) {
        // 动态设置状态栏字体颜色
        if (isLightColor(ContextCompat.getColor(this, R.color.colorPrimaryDark))) {
            // 亮色，设置字体黑色
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);
            }
        } else {
            getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_VISIBLE);
        }

        currentScores = findViewById(R.id.tv_current_score);
        bestScores = findViewById(R.id.tv_best_score);
        bestScoresRank = findViewById(R.id.tv_best_score_rank);
        titleDescribe = findViewById(R.id.tv_title_describe);
        tvTitle = findViewById(R.id.tv_title);
        menu = findViewById(R.id.btn_option);
        reset = findViewById(R.id.btn_reset);
        back = findViewById(R.id.btn_back);
        gameView = findViewById(R.id.game_view);


        if (gameStatus.equals("0")) {
            //开始游戏
            Config.gameStatus = gameStatus;
            // 读取到历史最高分
            bestScores.setText(String.valueOf(Config.BestScore));
            bestScoresRank.setText(getString(R.string.best_score_rank, Config.GRIDColumnCount));
            currentScores.setText("0");
            saveCurrentScore(0);
            //重置时间
            titleDescribe.setText(TimeUtils.getFormatHMS(0));
            resetTime(0);
            //加载游戏界面
            gameView.initView();
        } else if (gameStatus.equals("1")){
            Config.gameStatus = gameStatus;
            //继续游戏
            bestScores.setText(String.valueOf(Config.BestScore));
            bestScoresRank.setText(getString(R.string.best_score_rank, Config.GRIDColumnCount));
            currentScores.setText(String.valueOf(ConfigManager.getCurrentScore(this)));
            titleDescribe.setText(TimeUtils.getFormatHMS(Config.currentSecond));
            gameView.initView();

        }
        setTextStyle(tvTitle);
        setTextStyle(titleDescribe);
    }

    /**
     * 初始化数据
     */
    private void initData() {
        // 注册广播
        myReceiver = new MyReceiver();
        IntentFilter filter = new IntentFilter();
        filter.addAction(GameView.ACTION_RECORD_SCORE);
        filter.addAction(GameView.ACTION_WIN);
        filter.addAction(GameView.ACTION_LOSE);
        filter.addAction(GameView.ACTION_WIN_IN);
        filter.addAction(GameView.ACTION_LOSE_IN);
        registerReceiver(myReceiver, filter);

        // 重置按钮，重新开始游戏
        reset.setOnClickListener(v -> showConfirmDialog());
        // 打开菜单
        menu.setOnClickListener(v -> showConfigDialog());
        //返回登入界面
        back.setOnClickListener(v-> onBackPressed());
        //共享DataFor2048的数据库
        try {
            friendContext = this.createPackageContext("com.hj.datafor2048"
                    ,Context.CONTEXT_IGNORE_SECURITY);
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        gameDatabaseHelper = new GameDatabaseHelper(friendContext, Constant.DB_NAME, null, 1);
        db = gameDatabaseHelper.getWritableDatabase();
    }

    /**
     * 设置模式字体颜色大小加粗
     */
    private void setTextStyle(TextView textView) {
        SpannableString spannableString = new SpannableString(textView.getText().toString());
        StyleSpan styleSpan = new StyleSpan(Typeface.BOLD);
        ForegroundColorSpan foregroundColorSpan = new ForegroundColorSpan(Color.parseColor("#FFFFFF"));
        AbsoluteSizeSpan absoluteSizeSpan = new AbsoluteSizeSpan(52);
        spannableString.setSpan(styleSpan, 0, spannableString.length(), Spannable.SPAN_INCLUSIVE_INCLUSIVE);
        spannableString.setSpan(foregroundColorSpan, 0, spannableString.length(), Spanned.SPAN_INCLUSIVE_INCLUSIVE);
        spannableString.setSpan(absoluteSizeSpan, 0, spannableString.length(), Spanned.SPAN_INCLUSIVE_INCLUSIVE);
        textView.setText(spannableString);
    }

    /**
     * 打开重置确认对话框
     */
    private void showConfirmDialog() {
        CommonDialog dialog = new CommonDialog(this, R.style.CustomDialog);
        dialog.setCancelable(false);
        dialog.setTitle(getResources().getString(R.string.tip))
                .setMessage(getResources().getString(R.string.tip_reset_btn))
                .setOnNegativeClickListener("", v -> dialog.cancel())
                .setOnPositiveClickedListener("", v -> {
                    gameView.resetGame();
                    // 重置分数
                    currentScores.setText("0");
                    saveCurrentScore(0);
                    deleteCache(Config.getTableName());
                    //重置时间
                    titleDescribe.setText(TimeUtils.getFormatHMS(0));
                    resetTime(0);
                    dialog.cancel();
                }).show();
    }

    /**
     * 打开配置对话框
     */
    private void showConfigDialog() {
        configDialog = new ConfigDialog(this, R.style.CustomDialog);
        configDialog.setOnNegativeClickListener(v -> configDialog.cancel())
                .setOnPositiveClickListener(v -> onDialogConfirm())
                .show();
    }

    /**
     * 配置对话框的确认按钮监听
     */
    private void onDialogConfirm() {
        // 获取音效状态
        boolean volumeState = configDialog.getVolumeState();
            if (volumeState != Config.VolumeState) {
                // 保存音效设置
                ConfigManager.putGameVolume(this, volumeState);
                Config.VolumeState = volumeState;
            }
            configDialog.cancel();
    }

    /**
     * 自定义广播类
     */
    private class MyReceiver extends BroadcastReceiver {



        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            assert action != null;
            if (action.equals(GameView.ACTION_RECORD_SCORE)) {
                // 获取分数
                int score = intent.getIntExtra(GameView.KEY_SCORE, 0);
                // 获取历史分数
                int historyScore = Integer.parseInt(currentScores.getText().toString());
                //保存分数
                saveCurrentScore(score + historyScore);
                //显示分数
                recordScore(score + historyScore);
                // 游戏结束
            } else if (action.equals(GameView.ACTION_WIN_IN)
                    ||action.equals(GameView.ACTION_LOSE_IN)) {
                // 清除缓存
                isNeedSave = false;
                deleteCache(Config.getTableName());
                saveCurrentScore(0);

                String result = intent.getStringExtra(GameView.KEY_RESULT);
                GameOverDialog dialog = new GameOverDialog(GameActivity.this, R.style.CustomDialog);
                dialog.setCancelable(false);
                new Handler().postDelayed(() ->
                        dialog.setFinalScore(currentScores.getText().toString())
                                .setTitle(result)
                                .setEdit(action)
                                .setOnClickListener(v -> {
                                    isNeedSave = true;
                                    dialog.addInfo(TimeUtils.getFormatHMS(Config.currentSecond));
                                    resetTime(0);
                                    Intent i = new Intent(GameActivity.this,LoginActivity.class);
                                    Toast.makeText(GameActivity.this,"添加成功，返回主界面",Toast.LENGTH_SHORT).show();
                                    startActivity(i);
                                    dialog.cancel();
                                    finish();
                                })
                                .setOnBackClickListener(v -> {
                                    //返回主界面
                                    isNeedSave = true;
                                    resetTime(0);
                                    Intent i = new Intent(GameActivity.this,LoginActivity.class);
                                    startActivity(i);
                                    dialog.cancel();
                                    finish();
                                }).show(), 666);

            }else if(action.equals(GameView.ACTION_WIN)
                    || action.equals(GameView.ACTION_LOSE)){
                // 清除缓存
                isNeedSave = false;
                deleteCache(Config.getTableName());
                saveCurrentScore(0);
                resetTime(0);

                String result = intent.getStringExtra(GameView.KEY_RESULT);
                GameOverDialog dialog = new GameOverDialog(GameActivity.this, R.style.CustomDialog);
                dialog.setCancelable(false);
                new Handler().postDelayed(() ->
                        dialog.setFinalScore(currentScores.getText().toString())
                                .setTitle(result)
                                .setEdit(action)
                                .setOnClickListener(v -> {
                                    //返回主界面
                                    isNeedSave = true;
                                    Intent i = new Intent(GameActivity.this,LoginActivity.class);
                                    startActivity(i);
                                    dialog.cancel();
                                    finish();
                                })
                                .setOnBackClickListener(v -> {
                                    //返回主界面
                                    isNeedSave = true;
                                    Intent i = new Intent(GameActivity.this,LoginActivity.class);
                                    startActivity(i);
                                    dialog.cancel();
                                    finish();
                                }).show(), 666);
            }
        }

    }
    /**
     * 重置时间
     */
    private void resetTime(long currentSecond){
        Config.currentSecond = currentSecond;
        ConfigManager.putCurrentSecond(GameActivity.this,currentSecond);
    }

    /**
     * 保存当前得分
     * @param score
     */
    private void saveCurrentScore(int score) {
        ConfigManager.putCurrentScore(GameActivity.this, score);
    }

    /**
     * 记录得分
     */
    private void recordScore(int score) {
        currentScores.setText(String.valueOf(score));
        // 当前分数大于最高分
        if (score > ConfigManager.getBestScore(this)) {
            updateBestScore(score);
        }
    }

    /**
     * 更新最高分
     */
    private void updateBestScore(int newScore) {
        bestScores.setText(String.valueOf(newScore));
        Config.BestScore = newScore;
        ConfigManager.putBestScore(this, newScore);
    }

    /**
     * 退出游戏时自动保存当前进度
     */
    @Override
    public void onBackPressed() {
        CommonDialog dialog = new CommonDialog(this, R.style.CustomDialog);
        dialog.setCancelable(false);
        dialog.setTitle(getResources().getString(R.string.go_back))
                .setMessage("")
                .setOnNegativeClickListener(getResources().getString(R.string.back),
                        v -> {
                            saveGameProgress();
                            dialog.cancel();
                            finish();

                        })
                .setOnPositiveClickedListener(getResources().getString(R.string.waite), v -> dialog.cancel())
                .show();
    }

    /**
     * 保存格子位置坐标和格子内容
     */
    private void saveGameProgress() {
        String tableName = Config.getTableName();
        deleteCache(tableName);
        ArrayList<CellEntity> data = gameView.getCurrentProcess();
        if (data.size() >= 2) {
            ContentValues values = new ContentValues();
            for (CellEntity cell : data) {
                values.put("x", cell.getX());
                values.put("y", cell.getY());
                values.put("num", cell.getNum());
                db.insert(tableName, null, values);
                values.clear();
            }
        }
    }

    /**
     * 清空缓存
     */
    private void deleteCache(String tableName) {
        db.execSQL("delete from " + tableName);
    }

    /**
     * 判断颜色是否是亮色
     */
    private boolean isLightColor(int color) {
        return ColorUtils.calculateLuminance(color) >= 0.5;
    }

}
