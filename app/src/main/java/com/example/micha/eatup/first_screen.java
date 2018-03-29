package com.example.micha.eatup;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.FragmentActivity;
import android.support.v7.app.AppCompatActivity;

import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import com.facebook.AccessToken;
import com.facebook.CallbackManager;
import com.facebook.FacebookException;
import com.facebook.login.LoginBehavior;
import com.facebook.login.LoginResult;
import com.facebook.login.widget.*;
import com.facebook.FacebookCallback;
import com.facebook.login.LoginManager;
import com.facebook.FacebookSdk;

import com.facebook.GraphRequest;
import com.facebook.GraphRequestAsyncTask;
import com.facebook.GraphResponse;
import com.google.firebase.iid.FirebaseInstanceId;


import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.CookieHandler;
import java.net.CookieManager;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Comparator;

public class first_screen extends androidPage {
    protected  CallbackManager callbackManager;
    protected LoginButton loginButton;
    protected JSONObject forIntent;
    private final static String serverAddress = global.serverAddress;

    private final static String phpAddress= "/login.php";
    protected String userId = null;
    protected String tokenStr = null;
    protected AccessToken token = null;
    protected first_screen current = this; //Resolve the scope issue of "this".
    protected String full_name;
    //Since I have a final intent Current, I can put those code of graph call outside:
    // Reason easier to change the control flow if those operations are outside of the graph call
    JSONObject newUserObj = new JSONObject();

    protected Intent getNextView(FragmentActivity current, Class NextPage){
        Intent intent = new Intent(current,NextPage);
        current.startActivity(intent);
        return intent;
    }

    public void initializeToken(){
        token = AccessToken.getCurrentAccessToken();
        if(token !=null){
            userId = token.getUserId();
            tokenStr = token.getToken();
        }
    }
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        CookieManager cookieManager = new CookieManager();
        CookieHandler.setDefault(cookieManager);
        setContentView(R.layout.activity_first_screen);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        //final first_screen current = this;
        callbackManager = CallbackManager.Factory.create();
        loginButton = (LoginButton) findViewById(R.id.connectWithFbButton);
        loginButton.setReadPermissions( "user_friends");
        getSupportActionBar().setDisplayShowTitleEnabled(false);

        // If session not expired then directly goto the second page.
        initializeToken();
        if (token!=null && !token.isExpired()) {
            try {
                //NO matter whether logged in or not, repeat those steps.
                //Hence easier to handle with one single php file
                // plus it allows to update the username if necessary.
                GraphRequest request = sub_GetGraphRequest();
                sub_sendGraphRequest(request);



            } catch (Exception e) {
                e.printStackTrace();
            }
          //  Intent intent = getNextView(current, availability_page.class);

        }
        LoginManager.getInstance().registerCallback(callbackManager,
                new FacebookCallback<LoginResult>() {
                    @Override
                    public void onSuccess(LoginResult loginResult) {
                        // App code
                        System.out.println("trigger: Success");
                        //graph API request
                        initializeToken();
                        GraphRequest request = sub_GetGraphRequest();
                        //Static class extends connectJsonStream otherwise there will be leakage
                        // Override in the nested class here.
                        // Less convenient for resusability.
                        sub_sendGraphRequest(request);
                       // connectJsonStream postUser = new postUserInfo(current,newUserObj,serverAddress,phpAddress);
                        //postUser.execute();

                    }

                    @Override
                    public void onCancel() {
                        // App code
                        System.out.println("trigger: Cancel");
                    }

                    @Override
                    public void onError(FacebookException exception) {
                        // App code
                        System.out.println("trigger: Error");
                    }



                });
    }
    public void nextPage(View view) {
        Intent refresh = new Intent(this, first_screen.class);
        startActivity(refresh);
        this.finish();
    }
    // Send the Facebook LoginResult to callback manager and fire the trigger action
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        Button nextPage = (Button) findViewById(R.id.button_id);
        System.out.println("THE RESULT CODE IS = " + resultCode);
        if (resultCode==7 | resultCode==0) {
            nextPage.setVisibility(View.VISIBLE);
        }
        else {
            nextPage.setVisibility(View.GONE);
            callbackManager.onActivityResult(requestCode, resultCode, data);
        }
    }
    protected void sub_sendGraphRequest(GraphRequest request){
        Bundle parameters = new Bundle();

        parameters.putString("fields", "first_name, last_name,id,name,picture.type(large)");
        //parameters.putString("type","large");
        request.setParameters(parameters);
        request.executeAsync();
    }
    protected GraphRequest sub_GetGraphRequest(){ //first_screen currentScreen
        if(token==null){
            token = AccessToken.getCurrentAccessToken();
            tokenStr = token.getToken();
            userId = token.getUserId();

        }
        return GraphRequest.newMeRequest(
                token,
                new GraphRequest.GraphJSONObjectCallback() {
                    @Override
                    public void onCompleted(
                            JSONObject object,
                            GraphResponse response) {
                        //was outside this function, tried moving inside
                        System.out.println("In graph:");
                        try {


                            System.out.println("Graph response: " + response);

                            String token = FirebaseInstanceId.getInstance().getToken();
                            newUserObj.put("userid", userId);
                            newUserObj.put("firebaseToken", token);
                            System.out.println("firebase Token = " + token);
                            newUserObj.put("tokenStr",tokenStr);
                            System.out.println("parsed userid and token (as email)");
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                        try {
                            String first_name = object.getString("first_name");
                            String last_name = object.getString("last_name");
                            String userID_Test = object.getString("id");
                            String user_name_test =  object.getString("name");


                            JSONObject tempArray = response.getJSONObject();
                            System.out.println("tempArray: " + tempArray);
                            //index 5 is position in field parameter request from above
                            //JSONObject data_obj = tempArray.getJSONObject(0);
                            JSONObject picture_obj = tempArray.getJSONObject("picture");
                            System.out.println("picture_obj: " + picture_obj);

                            JSONObject data_obj = picture_obj.getJSONObject("data");
                            System.out.println("data_obj: " + data_obj);

                            String prof_pic = data_obj.getString("url");

                            //String prof_pic = object.getString("picture");
                            //String prof_pic_url = object.getString("url");
                            System.out.println("MY URL IS: " + prof_pic);



                            System.out.println("parsed first and last name");
                            System.out.println("test field: "+userID_Test+" ;"+user_name_test);
                            full_name = first_name + " " + last_name;
                            //backend needs full name
                            newUserObj.put("username", full_name);
                            newUserObj.put("id_Test",userID_Test);
                            newUserObj.put("name_Test",user_name_test);
                            newUserObj.put("pic_url",prof_pic);
                            connectJsonStream postUser = new postUserInfo(current,newUserObj,serverAddress,phpAddress);
                            postUser.execute();
                            System.out.println("Reuse token and skip Login: "+userId+" "+newUserObj.get("username")+"\n"+newUserObj.get("name_Test")+"\n"+newUserObj.get("id_Test"));
                            System.out.println("parsed userid and token (as email)");

                        } catch (Exception e) {
                            e.printStackTrace();
                            getPopup(global.ERROR_FACEBOOK_LOGIN);
                        }
                    }
                });

    }
        public static class postUserInfo extends connectJsonStream{
            public postUserInfo(first_screen current,JSONObject newUserObj,String serverAddress,String phpAddress){
                super(current,newUserObj,serverAddress,phpAddress);
            }
            public postUserInfo(first_screen current,JSONObject newUserObj,String serverAddress,String phpAddress,boolean write,boolean read){
                super(current,newUserObj,serverAddress,phpAddress,write,read);
            }
            @Override
            protected void responseAction(){
              //  Intent intent = ((first_screen)parent).getNextView(this.parent, availability_page.class);
               //Must putExtra before startActivity: otherwise the getExtra methods will return null


               // global.tokenList.add(0,((first_screen)parent).token);
                global.token = ((first_screen)parent).token;
                global.name = ((first_screen)parent).full_name;
               // intent.putExtra("token", global.tokenList);
                //intent.putExtra("userId", ((first_screen)parent).userId);
                System.out.println("firebase:"+ FirebaseInstanceId.getInstance().getToken());
                //intent.putExtra("tokenStr", ((first_screen)parent).tokenStr);
                System.out.println("got response from server: new user entered");
                System.out.println("Refresh ID:"+FirebaseInstanceId.getInstance().getId());
                int responseCode = this.parent.getCode(jsonResponse);
                System.out.println("got response code:"+this.parent.getCode(jsonResponse));
                if(responseCode<=2) {

                    //make sure aval is in the stack of pages
                    //thus do startActivity twice
                    Intent intent = new Intent(this.parent,availability_page.class);
                    parent.startActivityForResult(intent, 7);
                }

                if(responseCode==2){
                    Intent intent = new Intent(this.parent,homepage.class);
                    parent.startActivityForResult(intent, 7);
                }
                else if(responseCode<1){
                    parent.getPopup(parent.getMsg(jsonResponse));
                }
               // this.parent.finish();
                //this.parent.switchIntentOnResponse(responseCode,parent.getMsg(jsonResponse),intent,true);

            }

        }
}