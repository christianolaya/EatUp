package com.example.micha.eatup;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.support.v4.app.NotificationCompat;
import android.os.Bundle;
import android.util.Log;

import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Map;

/**
 * Created by micha on 11/21/2017.
 */

public class MyFirebaseMessagingService extends FirebaseMessagingService {

    private final static String serverAddress = global.serverAddress;
    private final static String removeAddress = "/removeEvent.php";

    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {
        // ...
        Context context = this;
        // TODO(developer): Handle FCM messages here.
        // Not getting messages here? See why this may be: https://goo.gl/39bRNJ
        Log.d("getFrom", "From: " + remoteMessage.getFrom());

        // Check if message contains a data payload.
        if (remoteMessage.getData().size() > 0) {
            Log.d("getData", "Message data payload: " + remoteMessage.getData());
            Map<String, String> server_response = remoteMessage.getData();
            System.out.println("REACHED");
            Intent intent = new Intent(context, approve_request.class);
            global.applicantId = server_response.get("");
            global.applicantName = server_response.get("applicantName");
            global.applicantPic = server_response.get("picUrl");
            intent.putExtra("applicantId", server_response.get("applicantId"));
            intent.putExtra("applicantName", server_response.get("applicantName"));
            System.out.println("APPLICANT ID TO BE SENT IS =" + server_response.get("applicantId"));

            NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(this);
            if (server_response.get("type").equals("reply")) {
                System.out.println("RESPONSE IS REPLY");
                System.out.println("APPROVE IS: " + server_response.get("approve"));
                if (server_response.get("approve").equals("yes")) {
                    mBuilder = new NotificationCompat.Builder(this).setSmallIcon(R.mipmap.ic_launcher).setContentTitle("EatUp").setContentText(server_response.get("applicantName") + " wants to eat with you too!");

                    //delete if this doesnt work
                    global.gettingLunch = true;
                    global.lunchWith = global.applicantName;
                    global.lunchWithId = global.applicantId;
                }
                else {
                    mBuilder = new NotificationCompat.Builder(this).setSmallIcon(R.mipmap.ic_launcher).setContentTitle("EatUp").setContentText(server_response.get("applicantName") + " does not want to eat with you.");

//                    global.gettingLunch = true;
//                    global.lunchWith = global.applicantId;
//                    global.lunchWithId = global.applicantId;
                }
                intent = new Intent(context, homepage.class);

            }
            else {
                System.out.println("RESPONSE IS REQUEST");
                mBuilder = new NotificationCompat.Builder(this).setSmallIcon(R.mipmap.ic_launcher).setContentTitle("EatUp").setContentText(server_response.get("applicantName") + " wants to eat with you! Click to respond.");
            }
            PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
            NotificationManager mNotificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
            mBuilder.setContentIntent(pendingIntent);
            mNotificationManager.notify(001, mBuilder.build());
//            if (/* Check if data needs to be processed by long running job */ true) {
//            // For long-running tasks (10 seconds or more) use Firebase Job Dispatcher.
//            scheduleJob();
//        } else {
//            // Handle message within 10 seconds
//            handleNow();
//            }
        System.out.println(server_response.toString());
        }

        // Check if message contains a notification payload.
        //Yes we get it. But how to ?
        if (remoteMessage.getNotification() != null) {
            Log.d("getBody", "Message Notification Body: " + remoteMessage.getNotification().getBody());
        }

        // Also if you intend on generating your own notifications as a result of a received FCM
        // message, here is where that should be initiated. See sendNotification method below.
     /*   Bundle data = new Bundle();
        for (Map.Entry<String, String> entry : remoteMessage.getData().entrySet()) {
            data.putString(entry.getKey(), entry.getValue());
        }
        data.putString("1","2");
//Check this.
       NotificationsManager.presentNotification(
                this,
                data,
                new Intent(getApplicationContext(), homepage.class)
        );*/

     //Test
//        Bundle data = new Bundle();
//        for (Map.Entry<String, String> entry : remoteMessage.getData().entrySet()) {
//            data.putString(entry.getKey(), entry.getValue());
//        }
//
//        Context context = this.getApplicationContext();
//        Intent defaultAction = new Intent(context, homepage.class)
//                .setAction(Intent.ACTION_DEFAULT)
//                .putExtra("push", data);
//
//        String title = data.getString("title");
//        String body = data.getString("body");
////We need an intent : some kinds of floating title/notification stuff
//        NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(context)
//                .setSmallIcon(R.drawable.notification_template_icon_bg)
//                .setContentTitle(title == null ? "" : title)
//                .setContentText(body == null ? "Hello world" : body)
//                .setAutoCancel(true)
//                .setContentIntent(PendingIntent.getActivity(
//                        context,
//                        0,
//                        defaultAction,
//                        PendingIntent.FLAG_UPDATE_CURRENT
//                ));
//
//        NotificationManager mNotificationManager = (NotificationManager)getSystemService(Context.NOTIFICATION_SERVICE);
//        mNotificationManager.notify(123, mBuilder.build());
    }

    public static class removeEvents extends connectJsonStream{
        public removeEvents(homepage current, JSONObject newUserObj, String serverAddress, String phpAddress){
            super(current,newUserObj,serverAddress,phpAddress);
        }
        @Override
        protected void errorAction(){
            this.parent.getPopup(global.ERROR_GENERAL+": "+this.response);
        }
        @Override
        public void onPostExecute(Void what){
            try{
                if(this.parent.getCode(jsonResponse)>0){
                    System.out.println("delete: positive");
                    this.parent.finish();
                    return;
                }else{
                    this.parent.getPopup(parent.getMsg(jsonResponse));
                }
            }catch (Exception e){
                String msg;
                if(jsonResponse==null){
                    msg = "Invalid Response";
                }else{
                    msg = this.parent.getMsg(jsonResponse);
                }
                this.parent.getPopup(msg);

            }

        }

    }



}
