package com.example.micha.eatup;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.ActivityNotFoundException;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.ImageView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.facebook.AccessToken;
import com.facebook.CallbackManager;
import com.facebook.FacebookException;
import com.facebook.login.LoginBehavior;
import com.facebook.login.LoginResult;
import com.facebook.login.widget.*;
import com.facebook.FacebookCallback;
import com.facebook.login.LoginManager;

import com.squareup.picasso.*;



public class user_view extends androidPage {

    private final static String serverAddress = global.serverAddress;
    private final static String phpAddress= "/handleEvent.php";
    private final static String notifAddress= "/sendNotification.php";
    //private final Intent oldintent = getIntent();
    private String potentialLuncher;
    private String potentialHostId;
    private String potentialLuncherPic;

    protected JSONObject newObject = new JSONObject();
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //Previously user_card
        setContentView(R.layout.card_view);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        //get passed name of user
        final Intent oldintent = getIntent();
        potentialLuncher = oldintent.getStringExtra("potentialLuncher");
        potentialHostId = oldintent.getStringExtra("potentialHostId");
        potentialLuncherPic = oldintent.getStringExtra("pic_url");

        String text_for_view = "Lunch with... " + potentialLuncher;

        //get textview, set text to retrieved item
        TextView text_view = (TextView) findViewById(R.id.event_name);
        text_view.setText(text_for_view);

        if (!potentialLuncherPic.equals(null)) {
            ImageView image_view = (ImageView) findViewById(R.id.profile_pic);
            Picasso.with(this).load(potentialLuncherPic).resize(50,50).into(image_view);

        }



        /**
         *
         * code here if we someday want to upload an actual prof pic
         *
         * get the profPic object via id
         * get the image link via intent
         * assign link to the object
         *
         */

        JSONArray jsonResponse = new JSONArray();

    }
    public void goBack(View view){
        setResult(RESULT_OK, null);
        finish();
    }
    public void getLunch(View view){

        //code that first checks if other user has also made a request to get lunch
        //if they have not, wait for response, probably change button image
        //if they have requested lunch, combine events in database,

        //may need a request database
        //in fact we probably do
        //requests will go to database, then a matching "get lunch" click will find this entry
        //delete events from both users, make new with both user's names
        //**THIS MIGHT BE WHERE WE NEED PRIVACY
        //also, need to find a way to monitor whether a user has already chosen to have lunch with someone
        //need to avoid "double lunches"
        //can check eventregis to ensure event has not been deleted
        //TODO: define JSONObject newObject
        //TODO: regist.php
        // TODO: Distinguish the card of user him/herself

        AccessToken token = AccessToken.getCurrentAccessToken();
        String myUsrId = token.getUserId();

//        Intent oldintent = getIntent();
        String hostId = potentialHostId;

//        try{
//            newObject.put("applicant", myUsrId);
//            newObject.put("host", hostId);
//            newObject.put("applicant", myUsrId);
//            newObject.put("host", hostId);
//            newObject.put("applicantevent",global.card2eventId.get(myUsrId));
//            newObject.put("hostevent",global.card2eventId.get(hostId ));
//        } catch ( Exception e ) {
//
//            e.printStackTrace();
//        }
//        System.out.println("NEW OBJECT =" + newObject.toString());
//
//        new handleEvent(this,newObject,serverAddress,phpAddress,true,true).execute();

        Button getLunchBtn = (Button) this.findViewById(R.id.get_lunch_btn);
        getLunchBtn.setEnabled(false);

        System.out.println("Request Sent");
        getLunchBtn.setText("Requested!");

        JSONObject newNotif = new JSONObject();

        try {
            newNotif.put("applicantName", global.name);
            newNotif.put("hostName", potentialLuncher);
            newNotif.put("" +
                    "", global.token.getUserId());

            newNotif.put("hostId", potentialHostId);
            newNotif.put("target", potentialHostId);
            newNotif.put("myId", global.token.getUserId());



            String message = global.name + " would like to get lunch!";
            newNotif.put("message", message);
            newNotif.put("type", "request");

            new notify( (user_view) this, newNotif , serverAddress, notifAddress).execute();

        } catch (JSONException e) {
            e.printStackTrace();
        }


    }

    public void msgUser (View view){
        //10210325272108222 = Michael
        //open facebook messenger
        final Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse("fb-messenger://user/"+global.DefaultChat));
        try {
            startActivity(intent);
        }catch(ActivityNotFoundException e){
                System.out.println("No messenger");
            try {
                AlertDialog.Builder noMessenger = new AlertDialog.Builder(this);
                noMessenger.setMessage(global.MESSENGER_NOT_FOUND);
                noMessenger.setCancelable(true);
                noMessenger.setPositiveButton("Go to store",
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("https://messenger.com/t/")));
                                //dialog.cancel();
                            }
                        }
                        );
                noMessenger.setNegativeButton("Cancel",
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                dialog.cancel();
                            }
                        }
                );
                AlertDialog alert_NoMsg = noMessenger.create();
                alert_NoMsg.show();

            }catch(Exception e2){



            }

        }

    }


//    //not using rn
//    public static class handleEvent extends connectJsonStream{
//        public handleEvent(user_view current,JSONObject newUserObj,String serverAddress,String phpAddress){
//            super(current,newUserObj,serverAddress,phpAddress);
//        }
//        public handleEvent(user_view current,JSONObject newUserObj,String serverAddress,String phpAddress,boolean write,boolean read){
//            super(current,newUserObj,serverAddress,phpAddress,write,read);
//        }
//        @Override
//        protected void onPostExecute(Void what){
//                //Listen to the input and show message.
//
//            Button getLunchBtn = (Button) parent.findViewById(R.id.get_lunch_btn);
//            getLunchBtn.setEnabled(false);
//
//
//            int matched = 0;
//
//            try {
//
//                JSONArray tempObj = new JSONArray();
//                tempObj = jsonResponse;
//
//                matched = this.parent.getCode(tempObj);//tempObj.getInt(0);
//
//            } catch (Exception e ) {
//                e.printStackTrace();
//            }
//
//            if (matched == 0) {
//                System.out.println("Error in response from request table");
//
//            } else if (matched == 1) {
//                System.out.println("Request Sent");
//                getLunchBtn.setText("Requested!");
//
//                JSONObject newNotif = new JSONObject();
//
//                try {
//                    newNotif.put("applicantName", global.name);
//                    newNotif.put("hostName", global.potentialLuncher);
//                    newNotif.put("" +
//                            "", global.token.getUserId());
//
//                    newNotif.put("hostId", global.potentialHostId);
//                    newNotif.put("target", global.potentialHostId);
//
//
//
//                    String message = global.name + " would like to get lunch!";
//                    newNotif.put("message", message);
//                    newNotif.put("type", "request");
//
//                    new notify( (user_view) parent, newNotif , serverAddress, notifAddress).execute();
//
//                } catch (JSONException e) {
//                    e.printStackTrace();
//                }
//
//               // new notify( parent, newNotif , serverAddress, notifAddress);
//
//            } else if (matched == 2) {
//                System.out.println("Match Made!");
//                getLunchBtn.setText("Matched!");
//
//                global.lunchWith = global.potentialLuncher;
//                global.lunchWithId = global.potentialHostId;
//
//
//
//            } else {
//                System.out.print("Unknown error in response from request table");
//            }
//
//
//
//        }
//    }

    public static class notify extends connectJsonStream{
        public notify(user_view current,JSONObject newUserObj,String serverAddress,String phpAddress){
            super(current,newUserObj,serverAddress,phpAddress);
        }
        public notify(user_view current,JSONObject newUserObj,String serverAddress,String phpAddress,boolean write,boolean read){
            super(current,newUserObj,serverAddress,phpAddress,write,read);
        }
        @Override
        protected void responseAction(){
            //Listen to the input and show message.

            System.out.print("Notify From PHP:"+this.parent.getCode(jsonResponse));



        }
    }
}
