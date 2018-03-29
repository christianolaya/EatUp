package com.example.micha.eatup;

import android.app.AlertDialog;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;

import org.json.JSONArray;
import org.json.JSONException;

/**
 * Created by cielm on 21/11/2017 0021.
 */

public class androidPage extends AppCompatActivity {

    public void getPopup(String Msg){
        AlertDialog.Builder noMessenger = new AlertDialog.Builder(this);
        noMessenger.setMessage(Msg);
        noMessenger.setCancelable(true);
        AlertDialog alert_NoMsg = noMessenger.create();
        alert_NoMsg.show();
    }
    public int getCode(JSONArray arr){
        try {
            return arr.getJSONObject(0).getInt("success");
        } catch (Exception e) {
            return -1;
        }
    }
    public String getMsg(JSONArray arr){
        try {
            return arr.getJSONObject(0).getString("message");
        } catch (Exception e) {
            return "getMsg: parse error";
        }
    }
    public void switchIntentOnResponse(int responseCode,String msg,Intent intent,boolean finishCurrent){
        //int responseCode = this.getCode(jsonResponse);
        if(responseCode>=1){
            this.startActivity(intent);
            if(finishCurrent){
                this.finish();
            }

        }else{
            this.getPopup(msg);
        }
    }

    public void switchIntentOnResponse(JSONArray jsonResponse,Intent intent,boolean finishCurrent){
        int responseCode = this.getCode(jsonResponse);
        if(responseCode>=1){
            this.startActivity(intent);
            if(finishCurrent){
                this.finish();
            }

        }else{
            this.getPopup(this.getMsg(jsonResponse));
        }
    }

    public Intent switchIntentOnResponse(JSONArray jsonResponse,Class intentClass,boolean finishCurrent){
        Intent nextPage;
        if(this.getCode(jsonResponse)>=1){
            nextPage = new Intent(this,intentClass);
            this.startActivity(nextPage);
            if(finishCurrent){
                this.finish();
            }
            return nextPage;

        }else{
            this.getPopup(this.getMsg(jsonResponse));
            return null;
        }
    }
}
