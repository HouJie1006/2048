package kylec.me.g2048.utils;

import android.content.Context;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.util.DisplayMetrics;

import java.util.Locale;

/**
 * 语言切换工具类
 */
public class LanguageUtils {
    public static void set(String isEnglish, Context context){

        Resources resources = context.getResources();
        Configuration configuration = resources.getConfiguration();
        DisplayMetrics displayMetrics = resources.getDisplayMetrics();

        if (isEnglish.equals("en")) {
            //设置中文
            configuration.locale = Locale.SIMPLIFIED_CHINESE;
        } else {
            //设置英文
            configuration.locale = Locale.ENGLISH;
        }
        //更新配置
        resources.updateConfiguration(configuration,displayMetrics);

    }

}
