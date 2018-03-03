package com.amsavarthan.hify.receivers;


import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.media.RingtoneManager;
import android.net.Uri;
import android.support.v4.app.NotificationCompat;

import com.amsavarthan.hify.R;
import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;

/**
 * Created by amsavarthan on 22/2/18.
 */

public class FCMMessaging extends FirebaseMessagingService {

    private String dataReply_for;
    private String dataImage;
    private String dataFromImage;

    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {
        super.onMessageReceived(remoteMessage);
        Uri uri= RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);

        String messageTitle=remoteMessage.getNotification().getTitle();
        String messageBody=remoteMessage.getNotification().getBody();
        String click_action=remoteMessage.getNotification().getClickAction();

        String dataMessage=remoteMessage.getData().get("message");
        String dataFrom_name=remoteMessage.getData().get("from_name");
        String dataFrom_image=remoteMessage.getData().get("from_image");
        String dataFrom_id=remoteMessage.getData().get("from_id");

        NotificationCompat.Builder builder=new NotificationCompat.Builder(this,getString(R.string.default_notification_channel_id))
                .setContentTitle(messageTitle)
                .setContentText(messageBody)
                .setSound(uri)
                .setAutoCancel(true)
                .setColor(Color.parseColor("#2591FC"))
                .setSmallIcon(R.drawable.ic_default_notification);

        if(click_action=="com.amsavarthan.hify.TARGETNOTIFICATION") {
            dataReply_for = "empty";


        }else if(click_action== "com.amsavarthan.hify.TARGETNOTIFICATIONREPLY") {

            dataReply_for = remoteMessage.getData().get("reply_for");

        }else if(click_action=="com.amsavarthan.hify.TARGETNOTIFICATION_IMAGE"){

            dataReply_for="empty";
            dataImage=remoteMessage.getData().get("image");

            builder.setStyle(new NotificationCompat.BigPictureStyle()
                    .bigPicture(getBitmapfromUrl(dataImage)));

        }else if(click_action=="com.amsavarthan.hify.TARGETNOTIFICATIONREPLY_IMAGE"){

            dataReply_for = remoteMessage.getData().get("reply_for");
            dataFromImage=remoteMessage.getData().get("from_image");

            builder.setStyle(new NotificationCompat.BigPictureStyle()
                    .bigPicture(getBitmapfromUrl(dataImage)));

        }


        int id=(int)System.currentTimeMillis();

        Intent resultIntent=new Intent(click_action);
        resultIntent.putExtra("name",dataFrom_name);
        resultIntent.putExtra("image",dataFrom_image);
        resultIntent.putExtra("message",dataMessage);
        resultIntent.putExtra("from_id",dataFrom_id);
        resultIntent.putExtra("notification_id",id);
        resultIntent.putExtra("reply_for",dataReply_for);
        resultIntent.putExtra("image",dataImage);
        resultIntent.putExtra("reply_image",dataFromImage);

        PendingIntent pendingIntent=PendingIntent.getActivity(this,0,resultIntent,PendingIntent.FLAG_UPDATE_CURRENT);

        builder.setContentIntent(pendingIntent);


        NotificationManager notificationManager=(NotificationManager)getSystemService(NOTIFICATION_SERVICE);
        notificationManager.notify(id,builder.build());

    }

    public Bitmap getBitmapfromUrl(String imageUrl) {
        try {
            URL url = new URL(imageUrl);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setDoInput(true);
            connection.connect();
            InputStream input = connection.getInputStream();
            Bitmap bitmap = BitmapFactory.decodeStream(input);
            return bitmap;

        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
            return null;

        }
    }

}
