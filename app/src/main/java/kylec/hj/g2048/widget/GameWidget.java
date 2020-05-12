package kylec.hj.g2048.widget;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.Context;
import android.content.Intent;
import android.widget.RemoteViews;

import kylec.hj.g2048.R;
import kylec.hj.g2048.SplashActivity;

/**
 * 小部件点击按钮
 */
public class GameWidget extends AppWidgetProvider {

    static void updateAppWidget(Context context, AppWidgetManager appWidgetManager,
                                int appWidgetId) {

        RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.game_widget);

        Intent intent = new Intent(context, SplashActivity.class);
        PendingIntent pd = PendingIntent.getActivity(context,200,intent,PendingIntent.FLAG_CANCEL_CURRENT);
        views.setOnClickPendingIntent(R.id.appwidget_text,pd);

        appWidgetManager.updateAppWidget(appWidgetId, views);
    }

    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        for (int appWidgetId : appWidgetIds) {
            updateAppWidget(context, appWidgetManager, appWidgetId);
        }
    }

    @Override
    public void onEnabled(Context context) {
    }

    @Override
    public void onDisabled(Context context) {
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        super.onReceive(context, intent);
    }
}

