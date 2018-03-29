package com.example.micha.eatup;

import android.support.v4.app.FragmentActivity;

import com.facebook.AccessToken;

import org.json.JSONArray;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;

/**
 * Created by cielm on 14/11/2017 0014.
 */

public class global {
    public final static String MESSENGER_NOT_FOUND = "Facebook Messenger Not Found.";
  //  public static ArrayList<AccessToken> tokenList = new ArrayList<AccessToken>(1);
    public final static String DefaultChat = "602814669838217";
    public final static String serverAddress = "https://cse437.codenamedante.com/g13";
    public final static String registEvent = "/regist.php";
    public static String potentialLuncher = null;
    public static String potentialHostId = null;

    public static String lunchWith = null;
    public static boolean gettingLunch = false;

    public static String lunchWithId = null;
    public static String lunchId = null;
    public static String applicantId = null;
    public static String applicantName = null;
    public static String applicantPic;



  public static AccessToken token = null;
    public static String name = null;
    public static JSONArray friendsArr;
    public static String firebaseToken = null;

    public final static String[] defaultLocation = {"DUC","BD","Law Cafe","Holmes","Eta's","Hillman","Stanley's","Bauer","this is local"};
  //  public static FragmentActivity dummyFirebase = new FragmentActivity();
  public final static String ERROR_FACEBOOK_LOGIN = "Error: Facebook Login failed.";
  public final static String ERROR_PULL_LOCATION = "Sever error: Fail to pull location list. Using default";
  public final static String ERROR_GENERAL = "Server Error";
  public final static SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
  public final static SimpleDateFormat dateFormat2 = new SimpleDateFormat("yyyy-MM-dd HH:mm");
  public final static SimpleDateFormat showCase = new SimpleDateFormat("MM-dd HH:mm a");
  public static HashMap<String,Integer> card2eventId = new HashMap<String,Integer>();
  public static HashMap<String,String> card2picUrl = new HashMap<String,String>();
  public static void main(String args){
    }
}
