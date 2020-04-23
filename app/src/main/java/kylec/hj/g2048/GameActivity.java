package kylec.hj.g2048;

import android.annotation.SuppressLint;
import android.content.BroadcastReceiver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.database.sqlite.SQLiteDatabase;
import android.gesture.GestureOverlayView;
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
import android.widget.ImageView;
import android.widget.TextView;

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
 * Created by KYLE on 2018/10/2
 */
public class GameActivity extends AppCompatActivity {

    public static final String KEY_CHEAT = "开挂";
    public static final int KEY_MATCH_SCORE = 3;

    private TextView currentScores;
    private TextView bestScores;
    private TextView bestScoresRank;
    private TextView titleDescribe;
    private TextView tvTitle;
    private TextView modeDescribe;
    private Button reset;
    private Button menu;
    private ImageView cheatStar;
    private GameView gameView;

    private BroadcastReceiver myReceiver;
    private ConfigDialog configDialog;
    private GestureOverlayView mGestureOverlayView;

    private GameDatabaseHelper gameDatabaseHelper;
    private SQLiteDatabase db;
    private Context friendContext;

    private boolean isNeedSave = true;

    private Timer timer;

    private Timer gameTime;
    private boolean isPause = false;

    public static void start(Context context) {
        Intent starter = new Intent(context, GameActivity.class);
        context.startActivity(starter);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initView();
        initData();
        getTime();
//        initGesture();
    }

    /**
     * 游戏计时器
     * 04.22
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
        // 移除监听器
        mGestureOverlayView.removeAllOnGestureListeners();

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
            gameTime = null;
        }
        super.onDestroy();
    }

    /**
     * 初始化视图
     */
    private void initView() {
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
        modeDescribe = findViewById(R.id.tv_mode_describe);
        titleDescribe = findViewById(R.id.tv_title_describe);
        tvTitle = findViewById(R.id.tv_title);
        reset = findViewById(R.id.btn_reset);
        menu = findViewById(R.id.btn_option);
        mGestureOverlayView = findViewById(R.id.gesture_overlay_view);
        cheatStar = findViewById(R.id.iv_show_cheat);
        gameView = findViewById(R.id.game_view);

        // 进入经典模式
//        if (Config.CurrentGameMode == Constant.MODE_CLASSIC) {
        // 读取到历史最高分
        bestScores.setText(String.valueOf(Config.BestScore));
        bestScoresRank.setText(getString(R.string.best_score_rank, Config.GRIDColumnCount));
            currentScores.setText(String.valueOf(ConfigManager.getCurrentScore(this)));
            gameView.initView(Constant.MODE_CLASSIC);
/*        } else {
            // 进入无限模式
            enterInfiniteMode();
        }*/
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
        registerReceiver(myReceiver, filter);

        // 重置按钮，重新开始游戏
        reset.setOnClickListener(v -> showConfirmDialog());
        // 打开菜单
        menu.setOnClickListener(v -> showConfigDialog());
        // 切换模式
        //titleDescribe.setOnClickListener(v -> showChangeModeDialog());
        //共享DataFor2048的数据库（0423）
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
     * 初始化手势
     * todo 可以删除
     */
/*    private void initGesture() {
        if (!Config.haveCheat) {
            // 定义手势库
            GestureLibrary library = GestureLibraries.fromRawResource(this, R.raw.gestures);
            // 设置手势监听（手势绘制完成调用）
            mGestureOverlayView.addOnGesturePerformedListener((overlay, gesture) -> {
                // 加载手势库成功
                if (library.load()) {
                    // 从手势库中查找所有匹配的手势
                    ArrayList<Prediction> predictions = library.recognize(gesture);
                    if (!predictions.isEmpty()) {
                        // 获取第一匹配
                        Prediction prediction = predictions.get(0);
                        // 当匹配值大于3
                        if (prediction.score > KEY_MATCH_SCORE) {
                            // 进入开挂模式
                            if (KEY_CHEAT.equals(prediction.name)) {
                                cheatStar.setVisibility(View.VISIBLE);
                                // 设置缩放动画（以自身中心为缩放点，从10%缩放到原始大小）
                                ScaleAnimation animation = new ScaleAnimation(
                                        0.1f, 1, 0.1f, 1,
                                        Animation.RELATIVE_TO_SELF, 0.5f,
                                        Animation.RELATIVE_TO_SELF, 0.5f);
                                animation.setDuration(999);
                                cheatStar.startAnimation(animation);
                                animation.setAnimationListener(new Animation.AnimationListener() {
                                    @Override
                                    public void onAnimationStart(Animation animation) {
                                    }

                                    @Override
                                    public void onAnimationEnd(Animation animation) {
                                        showCheatModeDialog();
                                    }

                                    @Override
                                    public void onAnimationRepeat(Animation animation) {
                                    }
                                });
                            }
                        }
                    }
                }
            });
        }
    }*/

    /**
     * 打开外挂模式对话框
     */
/*    private void showCheatModeDialog() {
        CommonDialog dialog = new CommonDialog(this, R.style.CustomDialog);
        dialog.setCancelable(false);
        dialog.setTitle("外挂机制")
                .setMessage("小机灵鬼，这都被你发现了~将为您随机生成一个1024小方块，确认生成吗？")
                .setOnNegativeClickListener("", v -> dialog.cancel())
                .setOnPositiveClickedListener("", v -> {
                    Config.haveCheat = true;
                    gameView.addDigital(true);
                    Toast.makeText(GameActivity.this, "好了，就帮你到这了...", Toast.LENGTH_SHORT).show();
                    dialog.cancel();
                }).show();
        cheatStar.setVisibility(View.INVISIBLE);
    }*/

    /**
     * 打开切换模式对话框
     */
/*    private void showChangeModeDialog() {
        String subject = "";
        if (Config.CurrentGameMode == Constant.MODE_CLASSIC) {
            subject = "无限模式";
        } else if (Config.CurrentGameMode == Constant.MODE_INFINITE) {
            subject = "经典模式";
        }
        CommonDialog dialog = new CommonDialog(this, R.style.CustomDialog);
        dialog.setCancelable(true);
        dialog.setTitle(getResources().getString(R.string.tip))
                .setMessage("是否要切换到" + subject)
                .setOnPositiveClickedListener("", v -> {
                    if (Config.CurrentGameMode == Constant.MODE_CLASSIC) {
                        Toast.makeText(GameActivity.this, "已进入无限模式", Toast.LENGTH_SHORT).show();
                        enterInfiniteMode();
                    } else {
                        Toast.makeText(GameActivity.this, "已进入经典模式", Toast.LENGTH_SHORT).show();
                        enterClassicsMode();
                    }
                    dialog.cancel();
                })
                .setOnNegativeClickListener("", v -> dialog.cancel())
                .show();
    }*/

    /**
     * 进入无限模式
     *
     */
/*    private void enterInfiniteMode() {
        Config.haveCheat = false;
        Config.CurrentGameMode = Constant.MODE_INFINITE;
        // 保存游戏模式
        ConfigManager.putCurrentGameMode(this, Constant.MODE_INFINITE);
        titleDescribe.setText(getResources().getString(R.string.game_mode_infinite));
        bestScores.setText(String.valueOf(ConfigManager.getBestScoreWithinInfinite(this)));
        bestScoresRank.setText(getResources().getText(R.string.tv_best_score_infinite));
        currentScores.setText(String.valueOf(ConfigManager.getCurrentScoreWithinInfinite(this)));
        modeDescribe.setText(getResources().getString(R.string.tv_describe_infinite));
        setTextStyle(titleDescribe);
        gameView.initView(Constant.MODE_INFINITE);
    }*/

    /**
     * 进入经典模式
     * todo 登入界面开始游戏调用的方法参考
     */
    private void enterClassicsMode() {
        Config.haveCheat = false;
        Config.CurrentGameMode = Constant.MODE_CLASSIC;
        // 保存游戏模式
        ConfigManager.putCurrentGameMode(this, Constant.MODE_CLASSIC);
        titleDescribe.setText(getResources().getString(R.string.game_mode_classics));
        // 读取到历史最高分
        bestScores.setText(String.valueOf(Config.BestScore));
        bestScoresRank.setText(getString(R.string.best_score_rank, Config.GRIDColumnCount));
        currentScores.setText(String.valueOf(ConfigManager.getCurrentScore(this)));
        modeDescribe.setText(getResources().getString(R.string.tv_describe));
        setTextStyle(titleDescribe);
        gameView.initView(Constant.MODE_CLASSIC);

    }

    /**
     * 设置模式字体颜色大小加粗
     */
    private void setTextStyle(TextView textView) {
        SpannableString spannableString = new SpannableString(textView.getText().toString());
        StyleSpan styleSpan = new StyleSpan(Typeface.BOLD);
        ForegroundColorSpan foregroundColorSpan = new ForegroundColorSpan(Color.parseColor("#FFFFFF"));
        AbsoluteSizeSpan absoluteSizeSpan = new AbsoluteSizeSpan(52);
        spannableString.setSpan(styleSpan, 0, 4, Spannable.SPAN_INCLUSIVE_INCLUSIVE);
        spannableString.setSpan(foregroundColorSpan, 0, 4, Spanned.SPAN_INCLUSIVE_INCLUSIVE);
        spannableString.setSpan(absoluteSizeSpan, 0, 4, Spanned.SPAN_INCLUSIVE_INCLUSIVE);
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
                    Config.haveCheat = false;
                    gameView.resetGame();
                    // 重置分数
                    currentScores.setText("0");
                    saveCurrentScore(0);
                    deleteCache(Config.getTableName());
                    //重置时间04.22
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
        // 获取选择的难度和音效状态
//        int difficulty = configDialog.getSelectDifficulty();
        boolean volumeState = configDialog.getVolumeState();
        // 若选择的难度与当前难度一样
//        if (difficulty == Config.GRIDColumnCount) {
            // 判断音效配置是否更改
            if (volumeState != Config.VolumeState) {
                // 保存音效设置
                ConfigManager.putGameVolume(this, volumeState);
                Config.VolumeState = volumeState;
            }
            configDialog.cancel();
/*            return;
        }*/
        //更改游戏配置
        //changeConfiguration(configDialog, volumeState);
        Config.haveCheat = false;
    }

    /**
     * 更改游戏配置
     * @param dialog 提示框对象
     * @param difficulty 难度状态（0423删除）
     * @param volumeState 音乐开关状态
     */
/*    private void changeConfiguration(ConfigDialog dialog, boolean volumeState) {
//        Config.GRIDColumnCount = difficulty;
        Config.VolumeState = volumeState;
        gameView.initView(Constant.MODE_CLASSIC);
        // 重置得分
        currentScores.setText(String.valueOf(ConfigManager.getCurrentScore(this)));
        bestScoresRank.setText(getString(R.string.best_score_rank, Config.GRIDColumnCount));
        bestScores.setText(String.valueOf(ConfigManager.getBestScore(this)));
        // 保存游戏难度
//        ConfigManager.putGameDifficulty(GameActivity.this, difficulty);
        // 保存音效设置
        ConfigManager.putGameVolume(this, volumeState);
        dialog.cancel();
    }*/

    /**
     * 记录得分
     */
    private void recordScore(int score) {
        currentScores.setText(String.valueOf(score));
        // 当前分数大于最高分
        if (score > ConfigManager.getBestScore(this)) {
            updateBestScore(score);
        }
/*        if (Config.CurrentGameMode == Constant.MODE_CLASSIC) {
            if (score > ConfigManager.getBestScore(this)) {
                updateBestScore(score);
            }
        } else if (Config.CurrentGameMode == Constant.MODE_INFINITE) {
            if (score > ConfigManager.getBestScoreWithinInfinite(this)) {
                updateBestScore(score);
            }
        }*/
    }

    /**
     * 更新最高分
     */
    private void updateBestScore(int newScore) {
        bestScores.setText(String.valueOf(newScore));
        Config.BestScore = newScore;
        ConfigManager.putBestScore(this, newScore);
/*        if (Config.CurrentGameMode == Constant.MODE_CLASSIC) {
            Config.BestScore = newScore;
            ConfigManager.putBestScore(this, newScore);
        } else if (Config.CurrentGameMode == Constant.MODE_INFINITE) {
            Config.BestScoreWithinInfinite = newScore;
            ConfigManager.putBestScoreWithinInfinite(this, newScore);
        }*/
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
                saveCurrentScore(score + historyScore);
                recordScore(score + historyScore);
                // 游戏结束
            } else if (action.equals(GameView.ACTION_WIN)
                    || action.equals(GameView.ACTION_LOSE)) {
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
                                .setOnShareClickListener(v -> share())
                                .setOnGoOnClickListener(v -> {
                                    // 清除缓存
                                    isNeedSave = true;
                                    gameView.reset();
                                    deleteCache(Config.getTableName());
                                    saveCurrentScore(0);
                                    gameView.initView(Config.CurrentGameMode);
                                    currentScores.setText("0");
                                    //重置时间04.23
                                    titleDescribe.setText(TimeUtils.getFormatHMS(0));
                                    resetTime(0);
                                    dialog.cancel();
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

    private void saveCurrentScore(int score) {
        ConfigManager.putCurrentScore(GameActivity.this, score);
/*        if (Config.CurrentGameMode == Constant.MODE_CLASSIC) {
            ConfigManager.putCurrentScore(GameActivity.this, score);
        } else {
            ConfigManager.putCurrentScoreWithinInfinite(GameActivity.this, score);
        }*/
    }

    /**
     * 判断颜色是否是亮色
     */
    private boolean isLightColor(int color) {
        return ColorUtils.calculateLuminance(color) >= 0.5;
    }

    /**
     * 分享
     * todo 改成加入排行榜
     */
    private void share() {
        Intent shareIntent = new Intent(Intent.ACTION_SEND);
        shareIntent.setType("text/plain");
        shareIntent.putExtra(Intent.EXTRA_TEXT,
                getResources().getString(R.string.share, currentScores.getText().toString()));
        shareIntent = Intent.createChooser(shareIntent, "分享到");
        startActivity(shareIntent);
    }

    /**
     * 退出游戏时自动保存当前进度
     */
    @Override
    public void onBackPressed() {
        CommonDialog dialog = new CommonDialog(this, R.style.CustomDialog);
        dialog.setCancelable(false);
        dialog.setTitle("确认退出")
                .setMessage("")
                .setOnNegativeClickListener("狠心离开",
                        v -> {
                            saveGameProgress();
                            finish();

                        })
                .setOnPositiveClickedListener("我还要玩一会", v -> dialog.cancel())
                .show();
    }

    private void saveGameProgress() {
        // todo 加入时间time
        String tableName = Config.getTableName();
        deleteCache(tableName);
        // 保存新的数据
        ArrayList<CellEntity> data = gameView.getCurrentProcess();
        if (data.size() > 2) {
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

}
