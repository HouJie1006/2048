package kylec.hj.g2048.app;

import android.app.Application;

/**
 * 应用配置
 */
public class Config extends Application {

    /**
     * 保存最高分的SharedPreferences
     */
    public static final String SAVE_BEST_SCORE = "SAVE_BEST_SCORE";
    /**
     * 保存当前得分的SharedPreferences
     */
    public static final String SAVE_CURRENT_SCORE = "SAVE_CURRENT_SCORE";

    /**
     * 保存游戏音效状态的SharedPreferences
     */
    public static final String SAVE_GAME_VOLUME_STATE = "SAVE_GAME_VOLUME_STATE";

    /**
     * 保存达成游戏目标次数的SharedPreferences
     */
    public static final String SAVE_GET_GOAL_TIME = "SAVE_GET_GOAL_TIME";


    /**
     * SharedPreferences保存4难度下最高分的KEY
     */
    public static final String KEY_BEST_SCORE_WITHIN_4 = "KEY_BEST_SCORE_WITHIN_4";


    /**
     * SharedPreferences保存游戏音效状态的KEY
     */
    public static final String KEY_GAME_VOLUME_STATE = "KEY_GAME_VOLUME_STATE";

    /**
     * SharedPreferences保存达成游戏目标的KEY
     */
    public static final String KEY_GET_GOAL_TIME = "KEY_GET_GOAL_TIME";

    /**
     * 最高得分
     */
    public static int BestScore;

    /**
     * 方格数(默认4行4列)
     */
    public static int GRIDColumnCount = 4;

    /**
     * 游戏音效状态(默认打开)
     */
    public static boolean VolumeState = true;

    /**
     * 达成游戏目标次数(默认0)
     */
    public static int GetGoalTime = 0;

    /**
     * 获取当前时间
     */
    public static long currentSecond = 0;
    /**
     * SharedPreferences保存游戏时间
     */
    public static final String SAVE_TIME = "SAVE_TIME";
    /**
     * SharedPreferences保存游戏时间的key
     */
    public static final String KEY_TIME = "KEY_TIME";

    /**
     * 游戏开始状态 0:重新开始（默认），1：继续游戏
     */
    public static String gameStatus = "0";


    @Override
    public void onCreate() {
        super.onCreate();
        //获取时间
        currentSecond = ConfigManager.getCurrentSecond(this);
        // 获取到最高分
        BestScore = ConfigManager.getBestScore(this);
        // 获取游戏音效状态
        VolumeState = ConfigManager.getGameVolumeState(this);
        // 获取达成游戏目标次数
        GetGoalTime = ConfigManager.getGoalTime(this);
    }

    public static String getTableName() {
        return Constant.TABLE_NAME_4;

    }

}
