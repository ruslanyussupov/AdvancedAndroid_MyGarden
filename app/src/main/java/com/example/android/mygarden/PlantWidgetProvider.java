package com.example.android.mygarden;

/*
* Copyright (C) 2017 The Android Open Source Project
*
* Licensed under the Apache License, Version 2.0 (the "License");
* you may not use this file except in compliance with the License.
* You may obtain a copy of the License at
*
*  	http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing, software
* distributed under the License is distributed on an "AS IS" BASIS,
* WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
* See the License for the specific language governing permissions and
* limitations under the License.
*/

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.RemoteViews;

import com.example.android.mygarden.provider.PlantContract;
import com.example.android.mygarden.ui.MainActivity;
import com.example.android.mygarden.ui.PlantDetailActivity;

public class PlantWidgetProvider extends AppWidgetProvider {

    // setImageViewResource to update the widget’s image
    static void updateAppWidget(Context context, AppWidgetManager appWidgetManager,
                                int imgRes, long plantId, boolean showWater, int appWidgetId) {

        // COMPLETED (4): separate the updateAppWidget logic into getGardenGridRemoteView and getSinglePlantRemoteView
        // COMPLETED (5): Use getAppWidgetOptions to get widget width and use the appropriate RemoteView method
        // COMPLETED (6): Set the PendingIntent template in getGardenGridRemoteView to launch PlantDetailActivity

        Bundle options = appWidgetManager.getAppWidgetOptions(appWidgetId);
        int width = options.getInt(AppWidgetManager.OPTION_APPWIDGET_MIN_WIDTH);
        if (width < 300) {
            appWidgetManager.updateAppWidget(appWidgetId, getSinglePlantRemoteView(context, imgRes,
                    plantId, showWater));
        } else {
            appWidgetManager.updateAppWidget(appWidgetId, getGardenGridRemoteView(context));
        }

    }

    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        //Start the intent service update widget action, the service takes care of updating the widgets UI
        PlantWateringService.startActionUpdatePlantWidgets(context);
    }

    public static void updatePlantWidgets(Context context, AppWidgetManager appWidgetManager,
                                          int imgRes, long plantId, boolean showWater, int[] appWidgetIds) {
        for (int appWidgetId : appWidgetIds) {
            updateAppWidget(context, appWidgetManager, imgRes, plantId, showWater, appWidgetId);
        }
    }

    @Override
    public void onDeleted(Context context, int[] appWidgetIds) {
        // Perform any action when one or more AppWidget instances have been deleted
    }

    @Override
    public void onEnabled(Context context) {
        // Perform any action when an AppWidget for this provider is instantiated
    }

    @Override
    public void onDisabled(Context context) {
        // Perform any action when the last AppWidget instance for this provider is deleted
    }

    @Override
    public void onAppWidgetOptionsChanged(Context context, AppWidgetManager appWidgetManager, int appWidgetId, Bundle newOptions) {
        PlantWateringService.startActionUpdatePlantWidgets(context);
        super.onAppWidgetOptionsChanged(context, appWidgetManager, appWidgetId, newOptions);
    }

    private static RemoteViews getSinglePlantRemoteView(Context context, int imgRes, long plantId,
                                                        boolean showWater) {

        Intent intent;
        if (plantId == PlantContract.INVALID_PLANT_ID) {
            intent = new Intent(context, MainActivity.class);
        } else {
            intent = new Intent(context, PlantDetailActivity.class);
            intent.putExtra(PlantDetailActivity.EXTRA_PLANT_ID, plantId);
        }

        PendingIntent pendingIntent = PendingIntent.getActivity(context, 0,
                intent, PendingIntent.FLAG_UPDATE_CURRENT);
        RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.plant_widget);
        views.setImageViewResource(R.id.widget_plant_image, imgRes);
        views.setTextViewText(R.id.widget_plant_name, String.valueOf(plantId));
        views.setOnClickPendingIntent(R.id.widget_plant_image, pendingIntent);

        if (showWater) {

            views.setViewVisibility(R.id.widget_water_button, View.VISIBLE);
            Intent wateringIntent = new Intent(context, PlantWateringService.class);
            wateringIntent.setAction(PlantWateringService.ACTION_WATER_PLANT);
            wateringIntent.putExtra(PlantWateringService.EXTRA_PLANT_ID, plantId);
            PendingIntent wateringPendingIntent = PendingIntent.getService(context, 0,
                    wateringIntent, PendingIntent.FLAG_UPDATE_CURRENT);
            views.setOnClickPendingIntent(R.id.widget_water_button, wateringPendingIntent);

        } else {

            views.setViewVisibility(R.id.widget_water_button, View.INVISIBLE);

        }

        return views;

    }

    private static RemoteViews getGardenGridRemoteView(Context context) {

        RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.widget_grid_view);
        Intent intent = new Intent(context, GridWidgetService.class);
        views.setRemoteAdapter(R.id.plants_gv, intent);

        Intent detailIntent = new Intent(context, PlantDetailActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(context, 0,
                detailIntent, PendingIntent.FLAG_UPDATE_CURRENT);
        views.setPendingIntentTemplate(R.id.plants_gv, pendingIntent);
        views.setEmptyView(R.id.plants_gv, R.id.empty_view);

        return views;

    }

}
