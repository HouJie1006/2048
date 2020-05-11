package kylec.me.g2048.app;

import android.content.Context;
import android.content.SharedPreferences;

/**
 * 保存配置工具
 * 用SharedPreferences保存
 */
public class ConfigManager {

    /**
     * 保存当前时间
     */
    public static void putCurrentSecond(Context context,long currentSecond){
        SharedPreferences.Editor editor = context.getSharedPreferences(
                Config.SAVE_TIME,Context.MODE_PRIVATE).edit();
        editor.putLong(Config.KEY_TIME,currentSecond).apply();
    }

    /**
     * 获取当前时间
     */
    public static long getCurrentSecond(Context context){
        SharedPreferences sharedPreferences =
                context.getSharedPreferences(Config.SAVE_TIME,Context.MODE_PRIVATE);
        return sharedPreferences.getLong(Config.KEY_TIME,0);
    }

    /**
     * 保存最高分
     */
    public static void putBestScore(Context context, int bestScore) {
        SharedPreferences.Editor editor = context.getSharedPreferences(
                Config.SAVE_BEST_SCORE, Context.MODE_PRIVATE).edit();
        editor.putInt(Config.KEY_BEST_SCORE_WITHIN_4, bestScore).apply();
    }

    /**
     * 获取最高分
     */
    public static int getBestScore(Context context) {
        SharedPreferences sharedPreferences =
                context.getSharedPreferences(Config.SAVE_BEST_SCORE, Context.MODE_PRIVATE);
        return sharedPreferences.getInt(Config.KEY_BEST_SCORE_WITHIN_4, 0);
    }

    /**
     * 保存当前得分
     */
    public static void putCurrentScore(Context context, int currentScore) {
        SharedPreferences.Editor editor = context.getSharedPreferences(
                Config.SAVE_CURRENT_SCORE, Context.MODE_PRIVATE).edit();
        editor.putInt("KEY_CURRENT_SCORE_4", currentScore).apply();
    }

    /**
     * 获取当前得分
     */
    public static int getCurrentScore(Context context) {
        SharedPreferences sharedPreferences =
                context.getSharedPreferences(Config.SAVE_CURRENT_SCORE, Context.MODE_PRIVATE);
        return sharedPreferences.getInt("KEY_CURRENT_SCORE_4", 0);
    }

    /**
     * 保存游戏音效状态
     */
    public static void putGameVolume(Context context, boolean volumeState) {
        SharedPreferences.Editor editor = context.getSharedPreferences(
                Config.SAVE_GAME_VOLUME_STATE, Context.MODE_PRIVATE).edit();
        editor.putBoolean(Config.KEY_GAME_VOLUME_STATE, volumeState).apply();
    }

    /**
     * 获取游戏音效状态
     */
    static boolean getGameVolumeState(Context context) {
        SharedPreferences sharedPreferences =
                context.getSharedPreferences(Config.SAVE_GAME_VOLUME_STATE, Context.MODE_PRIVATE);
        return sharedPreferences.getBoolean(Config.KEY_GAME_VOLUME_STATE, true);
    }

    /**
     * 保存达成游戏目标次数
     */
    public static void putGoalTime(Context context, int time) {
        SharedPreferences.Editor editor = context.getSharedPreferences(
                Config.SAVE_GET_GOAL_TIME, Context.MODE_PRIVATE).edit();
        editor.putInt(Config.KEY_GET_GOAL_TIME, time).apply();
    }

    /**
     * 获取达成游戏目标次数
     */
    public static int getGoalTime(Context context) {
        SharedPreferences sharedPreferences =
                context.getSharedPreferences(Config.SAVE_GET_GOAL_TIME, Context.MODE_PRIVATE);
        return sharedPreferences.getInt(Config.KEY_GET_GOAL_TIME, 0);
    }





}
