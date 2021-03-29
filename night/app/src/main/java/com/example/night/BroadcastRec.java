package com.example.night;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import androidx.core.app.NotificationCompat;


public class BroadcastRec extends BroadcastReceiver {
    public static final String NOTIFICATION_CHANNEL_ID="CS Notes:reminder notification channel";
    NotificationManager notificationManager;

    @Override
    public void onReceive(Context context, Intent intent) {
        // TODO: This method is called when the BroadcastReceiver is receiving
        // an Intent broadcast.
        notificationManager=(NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        getNotificationBuilder(context,intent);

    }
    public void getNotificationBuilder(Context context,Intent intent){
        Bundle bundle=intent.getExtras();
        Intent notes = new Intent(context,DisplayNote.class);
        notes.putExtras(bundle);
        PendingIntent pendingIntent=PendingIntent.getActivity(context,bundle.getInt("id"),notes,PendingIntent.FLAG_UPDATE_CURRENT);
        NotificationCompat.Builder notifyBuilder=new NotificationCompat.Builder(context,NOTIFICATION_CHANNEL_ID)
                .setContentTitle("Reminder").setContentIntent(pendingIntent).setStyle(new NotificationCompat.BigTextStyle().bigText(bundle.getString("notes")))
                .setSmallIcon(R.mipmap.myicon_foreground).setAutoCancel(true).setPriority(NotificationCompat.PRIORITY_HIGH)
                .setDefaults(NotificationCompat.DEFAULT_ALL);
        notificationManager.notify(bundle.getInt("id"),notifyBuilder.build());
    }
}