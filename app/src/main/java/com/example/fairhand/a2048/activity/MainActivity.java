package com.example.fairhand.a2048.activity;

import android.Manifest;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.gesture.GestureLibraries;
import android.gesture.GestureLibrary;
import android.gesture.GestureOverlayView;
import android.gesture.Prediction;
import android.graphics.Color;
import android.graphics.Typeface;
import android.net.Uri;
import android.os.Build;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.FileProvider;
import android.support.v4.graphics.ColorUtils;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.style.AbsoluteSizeSpan;
import android.text.style.ForegroundColorSpan;
import android.text.style.StyleSpan;
import android.util.Log;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.ScaleAnimation;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.app.hubert.guide.NewbieGuide;
import com.app.hubert.guide.listener.AnimationListenerAdapter;
import com.app.hubert.guide.model.GuidePage;
import com.app.hubert.guide.model.HighLight;
import com.arialyy.annotations.Download;
import com.arialyy.aria.core.Aria;
import com.arialyy.aria.core.download.DownloadTask;
import com.example.fairhand.a2048.R;
import com.example.fairhand.a2048.app.Config;
import com.example.fairhand.a2048.update.Update;
import com.example.fairhand.a2048.update.UpdateAPI;
import com.example.fairhand.a2048.util.PackageUtil;
import com.example.fairhand.a2048.util.SaveConfigUtil;
import com.example.fairhand.a2048.view.CustomCommonDialog;
import com.example.fairhand.a2048.view.CustomConfigDialog;
import com.example.fairhand.a2048.view.CustomOverDialog;
import com.example.fairhand.a2048.view.CustomUpdateDialog;
import com.example.fairhand.a2048.view.GameView;
import com.zhy.android.percent.support.PercentLinearLayout;

import java.io.File;
import java.util.ArrayList;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

/**
 * @author FairHand
 */
public class MainActivity extends AppCompatActivity {
    
    public static final String KEY_CHEAT = "开挂";
    public static final int KEY_MATCH_SCORE = 3;
    
    private TextView currentScores;
    private TextView bestScores;
    private TextView bestScoresRank;
    private TextView titleDescribe;
    private TextView modeDescribe;
    private Button reset;
    private Button menu;
    private ImageView cheatStar;
    
    private BroadcastReceiver myReceiver;
    private CustomConfigDialog dialog;
    private GestureOverlayView mGestureOverlayView;
    
    private CustomUpdateDialog updateDialog;
    
    private Update update;
    
    private float updateVersionCode;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initPermission();
        initView();
        initGuide();
        initData();
        initGesture();
        onUpdate(false);
    }
    
    /**
     * 初始化权限
     */
    private void initPermission() {
        Aria.download(this).register();
        // 检查有没有权限
        if (ContextCompat.checkSelfPermission(MainActivity.this,
                Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            // 申请获取权限
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                ActivityCompat.requestPermissions(MainActivity.this, new String[] {
                        Manifest.permission.WRITE_EXTERNAL_STORAGE,
                        Manifest.permission.REQUEST_INSTALL_PACKAGES,
                        Manifest.permission.READ_EXTERNAL_STORAGE}, 1);
            }
        }
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
        super.onDestroy();
    }
    
    /**
     * 初始化视图
     */
    private void initView() {
        // 隐藏标题栏
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.hide();
        }
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
        reset = findViewById(R.id.btn_reset);
        menu = findViewById(R.id.btn_option);
        mGestureOverlayView = findViewById(R.id.gesture_overlay_view);
        cheatStar = findViewById(R.id.iv_show_cheat);
        
        // 进入经典模式
        if (Config.CurrentGameMode == 0) {
            // 读取到历史最高分
            bestScores.setText(String.valueOf(Config.BestScore));
            bestScoresRank.setText(getString(R.string.best_score_rank, Config.GIRDColumnCount));
        } else {
            // 进入无限模式
            enterInfiniteMode();
        }
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
        titleDescribe.setOnClickListener(v -> showChangeModeDialog());
        
    }
    
    /**
     * 初始化引导
     */
    private void initGuide() {
        PercentLinearLayout guide1 = findViewById(R.id.ll_scores);
        PercentLinearLayout guide2 = findViewById(R.id.ll_best_score);
        GameView guide3 = findViewById(R.id.game_view);
        
        NewbieGuide.with(this)
                // 设置引导页的标签
                .setLabel("guide")
                // 添加一页引导页
                .addGuidePage(GuidePage.newInstance()
                                      // 高亮区域
                                      .addHighLight(guide1, HighLight.Shape.ROUND_RECTANGLE, 16)
                                      // 禁止点击任意地方取消
                                      .setEverywhereCancelable(false)
                                      .setBackgroundColor(ContextCompat.getColor(this, R.color.shadow))
                                      // 引导页说明布局
                                      .setLayoutRes(R.layout.guide_view_1, R.id.tv_i_know))
                .addGuidePage(GuidePage.newInstance()
                                      .addHighLight(guide2, HighLight.Shape.ROUND_RECTANGLE, 16)
                                      .setEverywhereCancelable(false)
                                      .setBackgroundColor(ContextCompat.getColor(this, R.color.shadow))
                                      .setLayoutRes(R.layout.guide_view_2, R.id.tv_i_know))
                .addGuidePage(GuidePage.newInstance()
                                      .addHighLight(titleDescribe, HighLight.Shape.ROUND_RECTANGLE, 16)
                                      .setEverywhereCancelable(false)
                                      .setBackgroundColor(ContextCompat.getColor(this, R.color.shadow))
                                      .setLayoutRes(R.layout.guide_view_3, R.id.tv_i_know))
                .addGuidePage(GuidePage.newInstance()
                                      .addHighLight(guide3, HighLight.Shape.ROUND_RECTANGLE, 16)
                                      .setEverywhereCancelable(false)
                                      .setBackgroundColor(ContextCompat.getColor(this, R.color.shadow))
                                      .setLayoutRes(R.layout.guide_view_4, R.id.tv_i_know))
                .show();
    }
    
    /**
     * 初始化手势
     */
    private void initGesture() {
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
                                animation.setAnimationListener(new AnimationListenerAdapter() {
                                    @Override
                                    public void onAnimationEnd(Animation animation) {
                                        showCheatModeDialog();
                                    }
                                });
                            }
                        }
                    }
                }
            });
        }
    }
    
    /**
     * 打开外挂模式对话框
     */
    private void showCheatModeDialog() {
        CustomCommonDialog dialog = new CustomCommonDialog(this, R.style.CustomDialog);
        dialog.setCancelable(false);
        dialog.setTitle("外挂机制")
                .setMessage("小机灵鬼，这都被你发现了~将为您随机生成一个1024小方块，确认生成吗？")
                .setOnNegativeClickListener("", v -> dialog.dismiss())
                .setOnPositiveClickedListener("", v -> {
                    Config.haveCheat = true;
                    GameView.getGameView().addDigital(true);
                    Toast.makeText(MainActivity.this, "好了，就帮你到这了...", Toast.LENGTH_SHORT).show();
                    dialog.dismiss();
                }).show();
        cheatStar.setVisibility(View.INVISIBLE);
    }
    
    /**
     * 打开切换模式对话框
     */
    private void showChangeModeDialog() {
        String subject = null;
        if (Config.CurrentGameMode == 0) {
            subject = "无限模式";
        } else if (Config.CurrentGameMode == 1) {
            subject = "经典模式";
        }
        CustomCommonDialog dialog = new CustomCommonDialog(this, R.style.CustomDialog);
        dialog.setCancelable(true);
        dialog.setTitle(getResources().getString(R.string.tip))
                .setMessage("切换到" + subject + "后将丢失当前游戏进度，确认切换？")
                .setOnPositiveClickedListener("", v -> {
                    if (Config.CurrentGameMode == 0) {
                        Toast.makeText(MainActivity.this, "已进入无限模式", Toast.LENGTH_SHORT).show();
                        enterInfiniteMode();
                    } else {
                        Toast.makeText(MainActivity.this, "已进入经典模式", Toast.LENGTH_SHORT).show();
                        enterClassicsMode();
                    }
                    dialog.dismiss();
                })
                .setOnNegativeClickListener("", v -> dialog.dismiss())
                .show();
    }
    
    /**
     * 进入无限模式
     */
    private void enterInfiniteMode() {
        Config.haveCheat = false;
        Config.CurrentGameMode = 1;
        // 保存游戏模式
        SaveConfigUtil.putCurrentGameMode(this, 1);
        titleDescribe.setText(getResources().getString(R.string.game_mode_infinite));
        bestScores.setText(String.valueOf(Config.BestScoreWithinInfinite));
        bestScoresRank.setText(getResources().getText(R.string.tv_best_score_infinite));
        currentScores.setText("0");
        modeDescribe.setText(getResources().getString(R.string.tv_describe_infinite));
        setTextStyle(titleDescribe);
        GameView.getGameView().initView(1);
    }
    
    /**
     * 进入经典模式
     */
    private void enterClassicsMode() {
        Config.haveCheat = false;
        Config.CurrentGameMode = 0;
        // 保存游戏模式
        SaveConfigUtil.putCurrentGameMode(this, 0);
        titleDescribe.setText(getResources().getString(R.string.game_mode_classics));
        // 读取到历史最高分
        bestScores.setText(String.valueOf(Config.BestScore));
        bestScoresRank.setText(getString(R.string.best_score_rank, Config.GIRDColumnCount));
        currentScores.setText("0");
        modeDescribe.setText(getResources().getString(R.string.tv_describe));
        setTextStyle(titleDescribe);
        GameView.getGameView().initView(0);
        
    }
    
    /**
     * 设置模式字体颜色大小加粗
     */
    private void setTextStyle(TextView textView) {
        SpannableString spannableString = new SpannableString(textView.getText().toString());
        StyleSpan styleSpan = new StyleSpan(Typeface.BOLD);
        ForegroundColorSpan foregroundColorSpan = new ForegroundColorSpan(Color.parseColor("#393E46"));
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
        CustomCommonDialog dialog = new CustomCommonDialog(this, R.style.CustomDialog);
        dialog.setCancelable(false);
        dialog.setTitle(getResources().getString(R.string.tip))
                .setMessage(getResources().getString(R.string.tip_reset_btn))
                .setOnNegativeClickListener("", v -> dialog.dismiss())
                .setOnPositiveClickedListener("", v -> {
                    Config.haveCheat = false;
                    GameView.getGameView().resetGame();
                    // 重置分数
                    currentScores.setText("0");
                    dialog.dismiss();
                }).show();
    }
    
    private boolean isUpdateClicked = false;
    
    /**
     * 打开配置对话框
     */
    private void showConfigDialog() {
        dialog = new CustomConfigDialog(this, R.style.CustomDialog);
        dialog.setCancelable(false);
        dialog.setOnNegativeClickListener(v -> onDialogBack())
                .setOnPositiveClickListener(v -> onDialogConfirm())
                .setOnCheckUpdateClickListener(v -> {
                    if (!isUpdateClicked) {
                        onUpdate(true);
                        isUpdateClicked = true;
                        Toast.makeText(MainActivity.this, "正在检测新版本", Toast.LENGTH_SHORT).show();
                    }
                }).show();
    }
    
    /**
     * 联网检查更新
     *
     * @param fromUser 是否来自用户手动点击更新
     */
    private void onUpdate(boolean fromUser) {
        // Retrofit请求数据
        String baseUrl = "https://raw.githubusercontent.com/kylechandev/";
        Retrofit retrofit = new Retrofit.Builder()
                                    .baseUrl(baseUrl)
                                    .addConverterFactory(GsonConverterFactory.create())
                                    .build();
        UpdateAPI updateAPI = retrofit.create(UpdateAPI.class);
        Call<Update> updateCall = updateAPI.getUpdate(
                "2048TR", "master", "update-2048log.json");
        updateCall.enqueue(new Callback<Update>() {
            @Override
            public void onResponse(@NonNull Call<Update> call, @NonNull Response<Update> response) {
                Log.d("测试", "成功");
                update = response.body();
                if (update != null) {
                    // 获取最新版本号
                    updateVersionCode = Float.valueOf(update.getNew_version_code());
                    // 获取忽略更新的版本号
                    float ignoreVersionCode = SaveConfigUtil.getIgnoreVersionCode(MainActivity.this);
                    // 当更新版本号不是忽略的版本号时
                    if (ignoreVersionCode != updateVersionCode) {
                        checkUpdate(fromUser);
                    } else if (fromUser) {
                        // 来自用户手动检查更新
                        checkUpdate(true);
                    }
                }
            }
            
            @Override
            public void onFailure(@NonNull Call<Update> call, @NonNull Throwable t) {
                Log.d("测试", "失败：" + t.getMessage());
            }
        });
    }
    
    /**
     * 检查更新
     */
    private void checkUpdate(boolean fromUser) {
        // 当版本号小于服务器中最新的版本，说明有更新版本
        if (PackageUtil.getPackageCode(MainActivity.this) < updateVersionCode) {
            // 检查有没有权限
            if (ContextCompat.checkSelfPermission(MainActivity.this,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                // 申请获取权限
                ActivityCompat.requestPermissions(MainActivity.this, new String[] {
                        Manifest.permission.WRITE_EXTERNAL_STORAGE}, 1);
            } else {
                showUpdateDialog();
            }
        } else {
            if (fromUser) {
                Toast.makeText(MainActivity.this, "已经是最新版本", Toast.LENGTH_SHORT).show();
                isUpdateClicked = false;
            }
        }
    }
    
    /**
     * 打开更新APP对话框
     */
    public void showUpdateDialog() {
        updateDialog = new CustomUpdateDialog.Builder(this, R.style.CustomDialog)
                               .setVersionName(
                                       getResources().getString(R.string.tv_find_new_version, update.getNew_version()))
                               .setVersionSize(
                                       getResources().getString(R.string.tv_version_size, update.getTarget_size()))
                               .setVersionLog(update.getUpdate_log())
                               .setUpdateButtonListener(v -> {
                                   isUpdateClicked = false;
                                   updateDialog.startUpdate();
                                   downloadAPK();
                               })
                               .setCloseButtonListener(v -> {
                                   updateDialog.dismiss();
                                   isUpdateClicked = false;
                               })
                               .setIgnoreButtonListener(v -> {
                                   updateDialog.dismiss();
                                   isUpdateClicked = false;
                                   SaveConfigUtil.putIgnoreVersionCode(MainActivity.this, updateVersionCode);
                                   Config.IgnoreVersionCode = updateVersionCode;
                               })
                               .setInstallButtonListener(v -> {
                                   installAPK();
                                   updateDialog.dismiss();
                               })
                               .build();
        updateDialog.setCancelable(false);
        updateDialog.show();
        
    }
    
    /**
     * 下载最新APK
     */
    private void downloadAPK() {
        Config.executor.execute(new Runnable() {
            @Override
            public void run() {
                Aria.download(this)
                        // 下载地址
                        .load(update.getApk_file_url())
                        // 保存路径
                        .setFilePath(Config.apkFilePath)
                        // 启动下载
                        .start();
            }
        });
    }
    
    
    /**
     * 下载任务正在运行
     */
    @Download.onTaskRunning()
    void taskRunning(DownloadTask task) {
        updateDialog.setProgressPercent(task.getPercent());
    }
    
    /**
     * 下载任务完成
     */
    @Download.onTaskComplete
    void taskComplete(DownloadTask task) {
        updateDialog.updateComplete();
        Log.d("测试", task.getTaskName() + "下载完成");
    }
    
    /**
     * 安装APK
     */
    private void installAPK() {
        File apkFile = new File(Config.apkFilePath);
        Intent toInstallIntent = new Intent(Intent.ACTION_VIEW);
        // 设置为新任务，解决部分机型无法跳转到安装界面
        toInstallIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        // 若安装包文件存在
        if (apkFile.exists()) {
            // 7.0以上的处理方式
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                Uri contentUri = FileProvider.getUriForFile(this,
                        "com.example.fairhand.a2048.fileprovider", apkFile);
                toInstallIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                toInstallIntent.setDataAndType(contentUri,
                        "application/vnd.android.package-archive");
            } else {// 7.0以下的处理方式
                toInstallIntent.addCategory(Intent.CATEGORY_DEFAULT);
                toInstallIntent.setDataAndType(Uri.fromFile(apkFile),
                        "application/vnd.android.package-archive");
            }
            // 跳转安装界面
            startActivity(toInstallIntent);
        }
    }
    
    /**
     * 配置对话框的确认按钮监听
     */
    private void onDialogConfirm() {
        // 获取选择的难度和音效状态
        int difficulty = dialog.getSelectDifficulty();
        boolean volumeState = dialog.getVolumeState();
        // 若选择的难度与当前难度一样
        if (difficulty == Config.GIRDColumnCount) {
            // 判断音效配置是否更改
            if (volumeState != Config.VolumeState) {
                Config.VolumeState = volumeState;
            }
            dialog.dismiss();
            return;
        }
        // 若进行过游戏
        if (GameView.havePlayed) {
            CustomCommonDialog dialog = new CustomCommonDialog(
                    MainActivity.this, R.style.CustomDialog);
            dialog.setCancelable(false);
            dialog.setTitle(getResources().getString(R.string.tip_reset_game))
                    .setMessage(getResources().getString(R.string.tip_reset_game_content))
                    .setOnPositiveClickedListener("", v -> {
                        changeConfiguration(MainActivity.this.dialog, difficulty, volumeState);
                        Config.haveCheat = false;
                        dialog.dismiss();
                    }).setOnNegativeClickListener("", v -> dialog.dismiss()).show();
        } else {
            Config.haveCheat = false;
            changeConfiguration(dialog, difficulty, volumeState);
        }
    }
    
    /**
     * 配置对话框的返回按钮监听
     */
    private void onDialogBack() {
        // 获取选择的难度和音效状态
        int difficulty = dialog.getSelectDifficulty();
        boolean volumeState = dialog.getVolumeState();
        if (difficulty != Config.GIRDColumnCount
                    || volumeState != Config.VolumeState) {
            CustomCommonDialog dialog = new CustomCommonDialog(
                    MainActivity.this, R.style.CustomDialog);
            dialog.setCancelable(false);
            dialog.setTitle(getResources().getString(R.string.tip))
                    .setMessage(getResources().getString(R.string.tip_back))
                    .setOnPositiveClickedListener("", v -> {
                        if (difficulty != Config.GIRDColumnCount) {
                            changeConfiguration(MainActivity.this.dialog, difficulty, volumeState);
                            Config.haveCheat = false;
                        } else {
                            Config.VolumeState = volumeState;
                            MainActivity.this.dialog.dismiss();
                        }
                        dialog.dismiss();
                    }).setOnNegativeClickListener("", v -> {
                MainActivity.this.dialog.dismiss();
                dialog.dismiss();
            }).show();
        } else {
            dialog.dismiss();
        }
    }
    
    /**
     * 更改游戏配置
     */
    private void changeConfiguration(CustomConfigDialog dialog, int difficulty,
                                     boolean volumeState) {
        Config.GIRDColumnCount = difficulty;
        Config.VolumeState = volumeState;
        GameView.getGameView().initView(0);
        // 重置得分
        currentScores.setText("0");
        bestScoresRank.setText(getString(R.string.best_score_rank, difficulty));
        bestScores.setText(String.valueOf(SaveConfigUtil.getBestScore(this)));
        // 保存游戏难度
        SaveConfigUtil.putGameDifficulty(MainActivity.this, difficulty);
        dialog.dismiss();
    }
    
    /**
     * 记录得分
     */
    private void recordScore(int score) {
        // 获取历史分数
        int historyScore = Integer.parseInt(currentScores.getText().toString());
        // 得出当前分数
        int currentScore = score + historyScore;
        currentScores.setText(String.valueOf(currentScore));
        // 当前分数大于最高分
        if (Config.CurrentGameMode == 0) {
            if (currentScore > SaveConfigUtil.getBestScore(this)) {
                updateBestScore(currentScore);
            }
        } else if (Config.CurrentGameMode == 1) {
            if (currentScore > SaveConfigUtil.getBestScoreWithinInfinite(this)) {
                updateBestScore(currentScore);
            }
        }
    }
    
    /**
     * 更新最高分
     */
    private void updateBestScore(int newScore) {
        bestScores.setText(String.valueOf(newScore));
        if (Config.CurrentGameMode == 0) {
            Config.BestScore = newScore;
            SaveConfigUtil.putBestScore(this, newScore);
        } else if (Config.CurrentGameMode == 1) {
            Config.BestScoreWithinInfinite = newScore;
            SaveConfigUtil.putBestScoreWithinInfinite(this, newScore);
        }
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
                recordScore(score);
                // 你赢啦
            } else if (action.equals(GameView.ACTION_WIN)
                               || action.equals(GameView.ACTION_LOSE)) {
                String result = intent.getStringExtra(GameView.KEY_RESULT);
                CustomOverDialog dialog = new CustomOverDialog(
                        MainActivity.this, R.style.CustomDialog);
                dialog.setCancelable(false);
                new Handler().postDelayed(() ->
                                                  dialog.setFinalScore(currentScores.getText().toString())
                                                          .setTitle(result)
                                                          .setOnShareClickListener(v -> share())
                                                          .setOnGoOnClickListener(v -> {
                                                              GameView.getGameView().initView(0);
                                                              currentScores.setText("0");
                                                              dialog.dismiss();
                                                          }).show(), 666);
            }
        }
    }
    
    /**
     * 判断颜色是否是亮色
     */
    private boolean isLightColor(int color) {
        return ColorUtils.calculateLuminance(color) >= 0.5;
    }
    
    /**
     * 分享
     */
    private void share() {
        Intent shareIntent = new Intent(Intent.ACTION_SEND);
        shareIntent.setType("text/plain");
        shareIntent.putExtra(Intent.EXTRA_TEXT,
                getResources().getString(R.string.share, currentScores.getText().toString()));
        shareIntent = Intent.createChooser(shareIntent, "分享到");
        startActivity(shareIntent);
    }
    
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        switch (requestCode) {
            case 1:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_DENIED) {
                    Toast.makeText(this, "无法获取权限", Toast.LENGTH_SHORT).show();
                    break;
                }
            default:
                break;
        }
    }
    
    @Override
    public void onBackPressed() {
        CustomCommonDialog dialog = new CustomCommonDialog(this, R.style.CustomDialog);
        dialog.setCancelable(false);
        dialog.setTitle("确认退出")
                .setMessage("")
                .setOnNegativeClickListener("狠心离开",
                        v -> android.os.Process.killProcess(android.os.Process.myPid()))
                .setOnPositiveClickedListener("我还要玩一会", v -> dialog.dismiss())
                .show();
    }
    
}
