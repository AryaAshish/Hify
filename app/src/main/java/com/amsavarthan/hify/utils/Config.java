package com.amsavarthan.hify.utils;

/**
 * Created by amsavarthan on 10/3/18.
 */

public class Config {

    // global topic to receive app wide push notifications
    public static final String TOPIC_GLOBAL = "global";

    // broadcast receiver intent filters
    public static final String REGISTRATION_COMPLETE = "registrationComplete";
    public static final String PUSH_NOTIFICATION = "pushNotification";

    // id to handle the notification in the notification tray
    public static final int NOTIFICATION_ID = 100;
    public static final int NOTIFICATION_ID_BIG_IMAGE = 101;

    public static final String SHARED_PREF = "ah_firebase";

    public static final String ADMIN_CHANNEL_ID = "admin_id";
    public static final String KEY_REPLY = "key_reply";

    public static final String FIREBASE_AUTH_KEY = "AAAADyWKubA:APA91bHXU3xlkiYE38rgO3Cpivz_TBL3ed4QsC2WjmF_eBtXjRR1pFqBNLiQo6q2y8fi_sACdR6rMKETQDPsCOb5_ASSxEDF8gsyVnFHxc7kJYpq1kQHnaH9pXim_ZGS_FffNrzyyCJs";
}
