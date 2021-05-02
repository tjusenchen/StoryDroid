package a2dp.Vol;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.Context;
import android.content.Intent;
import android.widget.RemoteViews;

public class Widget extends AppWidgetProvider {
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        for (int appWidgetId : appWidgetIds) {
            PendingIntent pendingIntent = PendingIntent.getService(context, 0, new Intent(context, ALauncher.class), 0);
            RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.widgetlayout);
            views.setOnClickPendingIntent(R.id.WidgetButton, pendingIntent);
            appWidgetManager.updateAppWidget(appWidgetId, views);
        }
        super.onUpdate(context, appWidgetManager, appWidgetIds);
    }

    public void onEnabled(Context context) {
        super.onEnabled(context);
    }
}
