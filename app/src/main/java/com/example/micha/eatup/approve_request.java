package com.example.micha.eatup;

import android.app.AlertDialog;
import android.content.ActivityNotFoundException;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

import org.json.JSONObject;

import java.io.DataOutputStream;
import java.net.HttpURLConnection;
import java.net.URL;

/**
 * Created by micha on 11/28/2017.
 */

public class approve_request extends androidPage{
    String phpAddress = "/sendNotification.php";
    Boolean writeToServer = true;
    Boolean listenToResponse = true;
    String name = new String();
    JSONObject decision = new JSONObject();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        System.out.println("RRRR");
        super.onCreate(savedInstanceState);
        //Previously user_card
        setContentView(R.layout.card_view);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        Intent intent = getIntent();
        name = intent.getStringExtra("applicantId");
        final String applicantname = intent.getStringExtra("applicantName");
        System.out.println("NAME IS = " + name);
        //get passed name of user
        String text_for_view = applicantname + " would like to join you for lunch!";

        //get textview, set text to retrievedgi item
        TextView text_view = (TextView) findViewById(R.id.event_name);
        text_view.setText(text_for_view);
        if (!global.applicantPic.equals(null)) {
            ImageView image_view = (ImageView) findViewById(R.id.profile_pic);
            Picasso.with(this).load(global.applicantPic).into(image_view);

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

        Button getLunch = (Button) findViewById(R.id.get_lunch_btn);
//        Button back = (Button) findViewById(R.id.back_button);
//        back.setText("Deny Request");
        getLunch.setText("Approve Request");
        getLunch.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                System.out.println("3");

                //set variables upon response
                global.gettingLunch = true;
                global.lunchWithId = name;
                global.lunchWith = applicantname;

                Thread thread = new Thread(new Runnable() {
                    @Override
                    public void run() {
                        URL url;
                        HttpURLConnection connection = null;
                        System.out.println("4");
                        try {
                            System.out.println("4222");

                            decision.put("target", global.applicantId);

                            decision.put("applicantName", global.name);
                            decision.put("hostId", global.token.getUserId());
                            decision.put("type", "reply");
                            decision.put("approve", "yes");
                            System.out.println("Decision = " + decision.toString());

                            url = new URL(global.serverAddress + phpAddress);
                            connection = (HttpURLConnection) url.openConnection();
                            connection.setDoOutput(writeToServer);
                            connection.setDoInput(listenToResponse);
                            connection.setRequestMethod("POST"); // here you are telling that it is a POST request, which can be changed into "PUT", "GET", "DELETE" etc.
                            connection.setRequestProperty("Content-Type", "Content-Type;application/json; charset=UTF-8"); // here you are setting the `Content-Type` for the data you are sending which is `application/json`
                            connection.setRequestProperty("Accept", "Content-Type;application/json; charset=UTF-8");
                            connection.connect();
                            //Send request
                            if (writeToServer) {
                                Log.d("writeTOServer:", "Start");
                                DataOutputStream wr = new DataOutputStream(
                                        connection.getOutputStream());
                                wr.write(decision.toString().getBytes("UTF-8"));
                                wr.flush();
                                wr.close();
                            }
                        } catch (Exception e) {
                            System.out.println("CAUGHT EXCEPTION");
                            e.printStackTrace();
                        }
                        try {
                            System.out.println("RESPONSE CODE = " + connection.getResponseCode());
                        }
                        catch (Exception e) {

                        }
                        System.out.println("5");
                    }
                });
                thread.start();
                setResult(RESULT_OK, null);
                finish();
            }
        });
    }
    public void goBack(View view){
        Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {
                URL url;
                HttpURLConnection connection = null;

                try {
                    decision.put("target", global.applicantId);
//                    decision.put("applicantName", global.applicantName);
                    decision.put("applicantName", global.name);
                    decision.put("type", "reply");
                    decision.put("approve", "no");
                    decision.put("hostId", global.token.getUserId());

                    System.out.println("Decision = " + decision.toString());
                    url = new URL(global.serverAddress + phpAddress);
                    connection = (HttpURLConnection) url.openConnection();
                    connection.setDoOutput(writeToServer);
                    connection.setDoInput(listenToResponse);
                    connection.setRequestMethod("POST"); // here you are telling that it is a POST request, which can be changed into "PUT", "GET", "DELETE" etc.
                    connection.setRequestProperty("Content-Type", "Content-Type;application/json; charset=UTF-8"); // here you are setting the `Content-Type` for the data you are sending which is `application/json`
                    connection.setRequestProperty("Accept", "Content-Type;application/json; charset=UTF-8");
                    connection.connect();
                    System.out.println("RESPONSE CODE = " + connection.getResponseCode());
                    //Send request
                    if (writeToServer) {
                        Log.d("writeTOServer:", "Start");
                        DataOutputStream wr = new DataOutputStream(
                                connection.getOutputStream());
                        wr.write(decision.toString().getBytes("UTF-8"));
                        wr.flush();
                        wr.close();
                    }
                } catch (Exception e) {
                    System.out.println("CAUGHT EXCEPTION");
                    e.printStackTrace();
                }
            }
        });
        thread.start();
        setResult(RESULT_OK, null);
        finish();
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
}
