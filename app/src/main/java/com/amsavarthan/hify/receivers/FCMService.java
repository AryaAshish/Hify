package com.amsavarthan.hify.receivers;

import android.content.Context;
import android.content.Intent;
import android.text.TextUtils;

import com.amsavarthan.hify.ui.activities.FriendRequestActivty;
import com.amsavarthan.hify.ui.activities.MainActivity;
import com.amsavarthan.hify.ui.activities.Notification.NotificationActivity;
import com.amsavarthan.hify.ui.activities.Notification.NotificationImage;
import com.amsavarthan.hify.ui.activities.Notification.NotificationImageReply;
import com.amsavarthan.hify.ui.activities.Notification.NotificationReplyActivity;
import com.amsavarthan.hify.utils.NotificationUtil;
import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

/**
 * Created by amsavarthan on 10/3/18.
 */

public class FCMService extends FirebaseMessagingService {

    private static final String TAG = FCMService.class.getSimpleName();

    private NotificationUtil notificationUtils;
    private String cName, cDesc;

    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {
        handleDataMessage(remoteMessage);
    }

    private void handleDataMessage(RemoteMessage remoteMessage) {

        cName = "Notifications Messages";
        cDesc = "Used for showing messages received.";
        final String title = remoteMessage.getData().get("title");
        final String body = remoteMessage.getData().get("body");
        String click_action = remoteMessage.getData().get("click_action");
        String message = remoteMessage.getData().get("message");
        String from_name = remoteMessage.getData().get("from_name");
        String from_image = remoteMessage.getData().get("from_image");
        String from_id = remoteMessage.getData().get("from_id");
        final String imageUrl = remoteMessage.getData().get("image");
        String reply_for = remoteMessage.getData().get("reply_for");
        String id = String.valueOf(remoteMessage.getData().get("notification_id"));
        String timeStamp = String.valueOf(remoteMessage.getData().get("timestamp"));

        //Friend Request Notification data
        String friend_id = remoteMessage.getData().get("friend_id");
        String friend_name = remoteMessage.getData().get("friend_name");
        String friend_email = remoteMessage.getData().get("friend_email");
        String friend_image = remoteMessage.getData().get("friend_image");
        String friend_token = remoteMessage.getData().get("friend_token");

        final Intent resultIntent;

        if (click_action.equals("com.amsavarthan.hify.TARGETNOTIFICATION")) {

            resultIntent = new Intent(getApplicationContext(), NotificationActivity.class);

        } else if (click_action.equals("com.amsavarthan.hify.TARGETNOTIFICATIONREPLY")) {

            resultIntent = new Intent(getApplicationContext(), NotificationReplyActivity.class);

        } else if (click_action.equals("com.amsavarthan.hify.TARGETNOTIFICATION_IMAGE")) {

            resultIntent = new Intent(getApplicationContext(), NotificationImage.class);

        } else if (click_action.equals("com.amsavarthan.hify.TARGETNOTIFICATIONREPLY_IMAGE")) {

            resultIntent = new Intent(getApplicationContext(), NotificationImageReply.class);

        } else if (click_action.equals("com.amsavarthan.hify.TARGET_FRIENDREQUEST")) {

            resultIntent = new Intent(getApplicationContext(), FriendRequestActivty.class);

        } else {
            resultIntent = new Intent(getApplicationContext(), MainActivity.class);
        }

        resultIntent.putExtra("title", title);
        resultIntent.putExtra("body", body);
        resultIntent.putExtra("name", from_name);
        resultIntent.putExtra("from_image", from_image);
        resultIntent.putExtra("message", message);
        resultIntent.putExtra("from_id", from_id);
        resultIntent.putExtra("notification_id", id);
        resultIntent.putExtra("timestamp", timeStamp);
        resultIntent.putExtra("reply_for", reply_for);
        resultIntent.putExtra("image", imageUrl);
        resultIntent.putExtra("reply_image", from_image);

        resultIntent.putExtra("f_id", friend_id);
        resultIntent.putExtra("f_name", friend_name);
        resultIntent.putExtra("f_email", friend_email);
        resultIntent.putExtra("f_image", friend_image);
        resultIntent.putExtra("f_token", friend_token);

        // check for image attachment
        if (TextUtils.isEmpty(imageUrl)) {

            if (!TextUtils.isEmpty(from_image)) {
                showNotificationMessage(Integer.valueOf(id), timeStamp, click_action, cName, cDesc, from_image, getApplicationContext(), title, body, resultIntent);
            } else {
                showNotificationMessage(Integer.valueOf(id), timeStamp, click_action, cName, cDesc, friend_image, getApplicationContext(), title, body, resultIntent);
            }
        } else {
            // image is present, show notification with image
            if (!TextUtils.isEmpty(from_image)) {
                showNotificationMessageWithBigImage(Integer.valueOf(id), timeStamp, click_action, cName, cDesc, from_image, getApplicationContext(), title, body, resultIntent, imageUrl);
            } else {
                showNotificationMessageWithBigImage(Integer.valueOf(id), timeStamp, click_action, cName, cDesc, friend_image, getApplicationContext(), title, body, resultIntent, imageUrl);
            }
        }

    }

    /**
     * Showing notification with text only
     */
    private void showNotificationMessage(int id, String timeStamp, String click_action, String channelName, String channelDesc, String user_image, Context context, String title, String message, Intent intent) {
        notificationUtils = new NotificationUtil(context);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        notificationUtils.showNotificationMessage(id, timeStamp, click_action, channelName, channelDesc, user_image, title, message, intent, null);
    }

    /**
     * Showing notification with text and image
     */
    private void showNotificationMessageWithBigImage(int id, String timeStamp, String click_action, String channelName, String channelDesc, String user_image, Context context, String title, String message, Intent intent, String imageUrl) {
        notificationUtils = new NotificationUtil(context);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        notificationUtils.showNotificationMessage(id, timeStamp, click_action, channelName, channelDesc, user_image, title, message, intent, imageUrl);
    }


}
