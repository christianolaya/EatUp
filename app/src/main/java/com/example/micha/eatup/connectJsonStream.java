package com.example.micha.eatup;

import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.support.v4.app.FragmentActivity;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URL;


public  class connectJsonStream extends AsyncTask<Void,Void,Void> {
protected androidPage parent;
protected Object postData;
protected Object friendsList;
protected String serverAddress;
protected String phpAddress;
protected boolean disconnectWhenDone  = true;
protected JSONArray jsonResponse = new JSONArray();
protected boolean  listenToResponse = true;
protected boolean writeToServer = true;
public boolean isError = false;
public int response = 0;
Context c;
    HttpURLConnection connection;
public String errMsg = "";
    connectJsonStream(androidPage parent, Object postData, String serverAddress, String phpAddress, boolean writeToServer, boolean listenToResponse, boolean disconnectWhenDone){
        super();
        this.parent = parent;
        this.postData = postData;
        this.serverAddress = serverAddress;
        this.phpAddress = phpAddress;
        this.listenToResponse = listenToResponse;
        this.disconnectWhenDone = disconnectWhenDone;
        this.writeToServer = writeToServer;
    }
    connectJsonStream(Context context, androidPage parent, Object postData, String serverAddress, String phpAddress){
        super();
        c = context;
        this.parent = parent;
        this.postData = postData;
        this.serverAddress = serverAddress;
        this.phpAddress = phpAddress;
    }
    connectJsonStream(androidPage parent, Object postData, String serverAddress, String phpAddress, boolean writeToServer, boolean listenToResponse){
            this(parent,postData,serverAddress,phpAddress, writeToServer,listenToResponse,true);
    }
    //Using default Data
    connectJsonStream(androidPage parent, Object postData, String serverAddress, String phpAddress){
        super();
        this.parent = parent;
        this.postData = postData;
        this.serverAddress = serverAddress;
        this.phpAddress = phpAddress;
    }
protected void setFlag(boolean flag){
    this.disconnectWhenDone=flag;
}
protected JSONArray getResponse(InputStream is){
    StringBuilder responseStrBuilder = new StringBuilder();
    String inputStr;
    try {
        BufferedReader streamReader = new BufferedReader(new InputStreamReader(is, "UTF-8"));
        while ((inputStr = streamReader.readLine()) != null){
            responseStrBuilder.append(inputStr);
            System.out.println("From:"+this.getClass().getName()+"inputStr:"+inputStr);
        }
        System.out.println("ResponseBuilder: "+responseStrBuilder.toString());
        return new JSONArray(responseStrBuilder.toString());
    } catch (JSONException|NullPointerException|IOException e) {

        e.printStackTrace();

        return null;
    }

    //return null;
}
@Override
protected Void doInBackground(Void...params){

    Object obj1 = this.postData;

        //HTTP POST REQUEST
        URL url;


        try {
            url = new URL(serverAddress+phpAddress);
            connection = (HttpURLConnection)url.openConnection();
            connection.setDoOutput(writeToServer);
            connection.setDoInput(listenToResponse);
            connection.setRequestMethod("POST"); // here you are telling that it is a POST request, which can be changed into "PUT", "GET", "DELETE" etc.
            connection.setRequestProperty("Content-Type", "Content-Type;application/json; charset=UTF-8"); // here you are setting the `Content-Type` for the data you are sending which is `application/json`
            connection.setRequestProperty("Accept", "Content-Type;application/json; charset=UTF-8");
            connection.connect();
            //Send request
            if(writeToServer) {
                Log.d("writeTOServer:","Start");
                DataOutputStream wr = new DataOutputStream(
                        connection.getOutputStream());
                wr.write(obj1.toString().getBytes("UTF-8"));
                wr.flush();
                wr.close();
            }

            this.response = connection.getResponseCode();
            System.out.println("Out: "+response);
            Log.d("response:",String.valueOf(response)+" ,"+connection.getResponseMessage());

            InputStream is;
            actionWithoutResponse();
            if(listenToResponse){
                if (response >= 200 && response <=399){
                    //return is = connection.getInputStream();
                        is = connection.getInputStream();
                } else {
                    is = connection.getErrorStream();
                    System.out.println("connectJSON: get error stream");
                    isError  = true;

                }
                jsonResponse = this.getResponse(is);
                responseAction();
                if(isError){
                    errorAction();
                }
                is.close();
            }

        } catch (Exception e) {
             e.printStackTrace();
        } finally {
            if(connection != null) {
                 connection.disconnect();
            }
        }
        if(disconnectWhenDone && connection!=null){
            connection.disconnect();
        }


             return null;
        }
        protected  void responseAction(){

                    System.out.println("Response Action of SuperClass");
        };
        protected void actionWithoutResponse(){

        }
        protected void  errorAction(){

        }

}
