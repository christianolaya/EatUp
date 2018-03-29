package com.example.micha.eatup;

import android.util.Log;

import com.facebook.AccessToken;
import com.facebook.appevents.AppEventsLogger;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.iid.FirebaseInstanceIdService;

import org.json.JSONObject;

import java.io.DataOutputStream;
import java.net.HttpURLConnection;
import java.net.URL;

/**
 * Created by micha on 11/19/2017.
 */
public class MyFirebaseInstanceIDService extends FirebaseInstanceIdService {
    String phpAddress = "/firebaseToken.php";
    Boolean writeToServer = true;
    Boolean listenToResponse = true;
    String refreshedToken;
    @Override
    public void onTokenRefresh() {
        // Get updated InstanceID token.
        refreshedToken = FirebaseInstanceId.getInstance().getToken();
        if (AccessToken.getCurrentAccessToken()!= null) {
            final JSONObject access_tokens = new JSONObject();
            AppEventsLogger.setPushNotificationsRegistrationId(refreshedToken);
            Thread thread = new Thread(new Runnable() {
                @Override
                public void run() {
                    URL url;
                    HttpURLConnection connection = null;

                    try {
                        access_tokens.put("firebaseToken", refreshedToken);
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
                            wr.write(access_tokens.toString().getBytes("UTF-8"));
                            wr.flush();
                            wr.close();
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            });
        }
    }
}
