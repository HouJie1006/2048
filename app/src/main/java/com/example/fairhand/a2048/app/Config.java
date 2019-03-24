package com.example.fairhand.a2048.app;

import android.app.Application;
import android.os.Environment;
import android.support.annotation.NonNull;

import com.arialyy.aria.core.Aria;
import com.example.fairhand.a2048.util.SaveConfigUtil;

import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * 应用配置
 *
 * @author FairHand
 * @date 2018/10/4
 */
public class Config extends Application {
    
    /**
     * 保存最高分的SharedPreferences
     */
    public static String SAVE_BEST_SCORE = "SAVE_BEST_SCORE";
    
    /**
     * 保存游戏难度的SharedPreferences
     */
    public static String SAVE_GAME_DIFFICULTY = "SAVE_GAME_DIFFICULTY";
    
    /**
     * 保存游戏音效状态的SharedPreferences
     */
    public static String SAVE_GAME_VOLUME_STATE = "SAVE_GAME_VOLUME_STATE";
    
    /**
     * 保存达成游戏目标次数的SharedPreferences
     */
    public static String SAVE_GET_GOAL_TIME = "SAVE_GET_GOAL_TIME";
    
    /**
     * 保存游戏模式的SharedPreferences
     */
    public static String SAVE_CURRENT_GAME_MODE = "SAVE_CURRENT_GAME_MODE";
    
    /**
     * 保存忽略更新的版本号的SharedPreferences
     */
    public static String SAVE_IGNORE_VERSION_CODE = "SAVE_IGNORE_VERSION_CODE";
    
    /**
     * SharedPreferences保存4难度下最高分的KEY
     */
    public static String KEY_BEST_SCORE_WITHIN_4 = "KEY_BEST_SCORE_WITHIN_4";
    
    /**
     * SharedPreferences保存5难度下最高分的KEY
     */
    public static String KEY_BEST_SCORE_WITHIN_5 = "KEY_BEST_SCORE_WITHIN_5";
    
    /**
     * SharedPreferences保存6难度下最高分的KEY
     */
    public static String KEY_BEST_SCORE_WITHIN_6 = "KEY_BEST_SCORE_WITHIN_6";
    
    /**
     * SharedPreferences保存无限模式下最高分的KEY
     */
    public static String KEY_BEST_SCORE_WITHIN_INFINITE = "KEY_BEST_SCORE_WITHIN_INFINITE";
    
    /**
     * SharedPreferences保存游戏难度的KEY
     */
    public static String KEY_GAME_DIFFICULTY = "KEY_GAME_DIFFICULTY";
    
    /**
     * SharedPreferences保存游戏音效状态的KEY
     */
    public static String KEY_GAME_VOLUME_STATE = "KEY_GAME_VOLUME_STATE";
    
    /**
     * SharedPreferences保存达成游戏目标的KEY
     */
    public static String KEY_GET_GOAL_TIME = "KEY_GET_GOAL_TIME";
    
    /**
     * SharedPreferences保存游戏模式的KEY
     */
    public static String KEY_CURRENT_GAME_MOE = "KEY_CURRENT_GAME_MOE";
    
    /**
     * SharedPreferences保存忽略更新的版本号的KEY
     */
    public static String KEY_IGNORE_VERSION_CODE = "KEY_IGNORE_VERSION_CODE";
    
    /**
     * 最高得分
     */
    public static int BestScore;
    
    /**
     * 无限模式下最高分
     */
    public static int BestScoreWithinInfinite;
    
    /**
     * 方格数(默认4行4列)
     */
    public static int GIRDColumnCount = 4;
    
    /**
     * 游戏音效状态(默认打开)
     */
    public static boolean VolumeState = true;
    
    /**
     * 达成游戏目标次数(默认0)
     */
    public static int GetGoalTime = 0;
    
    /**
     * 当前游戏模式(默认经典模式)
     */
    public static int CurrentGameMode = 0;
    
    /**
     * 忽视更新的版本号
     */
    public static float IgnoreVersionCode;
    
    /**
     * 判断有没有使用过外挂模式(默认没有)
     */
    public static boolean haveCheat = false;
    
    /**
     * 安装包路径
     */
    public static String apkFilePath = Environment.getExternalStorageDirectory().getAbsolutePath()
                                               + "/2048TR/" + "2048TR.apk";
    
    /**
     * 手动创建一个线程池<br/>
     * corePoolSize:核心线程数<br/>
     * maximumPoolSize:最大线程数<br/>
     * keepAliveTime:非核心线程最多存活事件<br/>
     * unit:keepAliveTime的单位<br/>
     * workQueue:线程池的任务队列<br/>
     * threadFactory:给线程起个名字<br/>
     * handler:抛异常
     */
    public static ThreadPoolExecutor executor = new ThreadPoolExecutor(
            5,
            10,
            60,
            TimeUnit.SECONDS,
            new LinkedBlockingQueue<>(),
            new ThreadFactory() {
                private final AtomicInteger mCount = new AtomicInteger(1);
                
                @Override
                public Thread newThread(@NonNull Runnable r) {
                    return new Thread(r, "AsyncTask #" + mCount.getAndIncrement());
                }
            },
            new ThreadPoolExecutor.AbortPolicy());
    
    @Override
    public void onCreate() {
        super.onCreate();
        // 获取到游戏难度
        GIRDColumnCount = SaveConfigUtil.getGameDifficulty(this);
        // 获取到最高分
        BestScore = SaveConfigUtil.getBestScore(this);
        // 获取游戏音效状态
        VolumeState = SaveConfigUtil.getGameVolumeState(this);
        // 获取达成游戏目标次数
        GetGoalTime = SaveConfigUtil.getGoalTime(this);
        // 获取游戏模式
        CurrentGameMode = SaveConfigUtil.getCurrentGameMode(this);
        // 获取无限模式下最高分
        BestScoreWithinInfinite = SaveConfigUtil.getBestScoreWithinInfinite(this);
        // 获取忽略更新的版本号
        IgnoreVersionCode = SaveConfigUtil.getIgnoreVersionCode(this);
        // 初始化Aria
        Aria.init(this);
    }
    
}
