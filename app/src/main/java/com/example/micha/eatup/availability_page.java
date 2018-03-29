package com.example.micha.eatup;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URLEncoder;
import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.FrameLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Spinner;
import android.widget.TimePicker;
import java.text.SimpleDateFormat;
import org.json.JSONObject;
import java.sql.Timestamp;

import java.net.URL;
import java.io.DataOutputStream;
import org.json.JSONException;
import org.json.JSONArray;
import java.net.HttpURLConnection;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import android.os.AsyncTask;

import com.facebook.AccessToken;
import com.facebook.GraphRequest;
import com.facebook.GraphResponse;
import com.facebook.HttpMethod;

import static android.R.attr.data;
import java.util.Calendar;
import java.io.BufferedReader;
import java.util.TimeZone;


/**
 * Created by micha on 10/17/2017.
 */

public class availability_page extends androidPage {
    private static boolean test_Local = false;
    private final static String serverAddress = global.serverAddress;
    private final static String phpAddress= "/addEvent.php";
    private final static String localtionResource = "/locationList.php";
    private String userId;
    private AccessToken userToken;
    private String userTokenStr;
    private JSONObject eventObj;
    private JSONArray friendsArr;
    TimePicker start;


    private static boolean from_homepage;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.availability_page);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        //Set locations for location spinner: using getLocation
        getLocation fetchLoc = new getLocation(this,null,serverAddress,localtionResource,false,true);

        fetchLoc.execute();
        System.out.println("av: "+fetchLoc.isError);
        eventObj = new JSONObject();
        userId = global.token.getUserId();
        System.out.println("ID2: " + userId);
        //Cannot directly put any type to intents
        // Primitive types or bundles or ArrayList<Parcelable>
        // Since accessToken implements Parcelable: we add it to an ArrayList
        // You can directly use the global.tokenList but from a designing view it is not good.
        // Actually this entire creepy structure sucks.
        userToken = global.token;//(AccessToken) (getIntent().getParcelableArrayListExtra("token")).get(0);

        userTokenStr =userToken.getToken();// getIntent().getStringExtra("tokenStr");
        start = (TimePicker) findViewById(R.id.start_picker);
//        start.setIs24HourView(true);
//        start.setCurrentHour(Calendar.getInstance().get(Calendar.HOUR_OF_DAY));
    }
    private String getUserID(){
        //returns userId as a string

        return userId;
    }
    private int getSeat(){
        /**
         * Todo: This should pass the current Seat Setting. Default value here and in database is 2.
         */
        return 2;
    }

    private int getDuration(RadioGroup durationRG){
        int radioButtonID = durationRG.getCheckedRadioButtonId();
        View radioButton = durationRG.findViewById(radioButtonID);
        int idx = durationRG.indexOfChild(radioButton);
        RadioButton r = (RadioButton)  durationRG.getChildAt(idx);
        String selectedtext = r.getText().toString();
        int duration;
        switch (selectedtext) {
            case "15 min":
                duration = 15;
                break;
            case "30 min":
                duration = 30;
                break;
            case "45 min":
                duration = 45;
                break;
            case "1 hour":
                duration = 60;
                break;
            case "2 hours":
                duration = 120;
                break;
            default:
                System.out.println("selectedtext: "+selectedtext);
                throw new IllegalArgumentException("Invalid duration " + selectedtext);
        }
        return duration;
    }
    /** Called when the user taps the Send button */

    public void sendMessage(View view) {


            TimePicker start = (TimePicker) findViewById(R.id.start_picker);
            int start_hour = start.getHour();
            int start_minute = start.getMinute();

            Spinner location = (Spinner) findViewById(R.id.location_spinner);
            String user_location = location.getSelectedItem().toString();
            //Create json object for input
            //JSONArray array = new JSONArray();

            Calendar c = Calendar.getInstance();
            //TimeZone.setDefault(TimeZone.getTimeZone( "UTC-6"));
            System.out.println("TIme picker:"+start.getHour()+" "+start.getBaseline());
        System.out.println("THE CURRENT DATE IS: " +c.get(Calendar.DAY_OF_MONTH));
            c.set(Calendar.HOUR_OF_DAY, start.getHour());
            c.set(Calendar.MINUTE, start.getMinute());
            Calendar currentCalendar = Calendar.getInstance();


        if (start_hour < currentCalendar.get(Calendar.HOUR_OF_DAY) || start_hour ==  currentCalendar.get(Calendar.HOUR_OF_DAY) && start_minute < currentCalendar.get(Calendar.MINUTE)) {
                getPopup("Cannot set start time to earlier than current time");
                return;
            }



            /**
             *  Todo: Attention: events that starts at the end of the day.
             */
            RadioGroup durationRG = (RadioGroup) findViewById(R.id.durationRG);
            int duration = this.getDuration(durationRG);



            String currTimeStamp = global.dateFormat.format(new Date(c.getTime().getTime()));
            System.out.println("date avil: " + currTimeStamp);
            //based on cases, only allows 1 or 2 hours or differing minute values
            //create end time timestamp to store and display
//
//            final Calendar c2 = Calendar.getInstance();
//            c2.set(Calendar.HOUR, end_hour);
//            c2.set(Calendar.MINUTE, end_minute);
//
//            //format end timestamp and store globally
//            String endTimeStamp = dateFormat.format(c2.getTime());
//            global.myEnd = endTimeStamp;
//            System.out.println("end time: " + endTimeStamp);

            try {
                eventObj.put("userid", getUserID());
                eventObj.put("StartTime", currTimeStamp);
                eventObj.put("Duration", duration);
                eventObj.put("Seat", getSeat());
                eventObj.put("Location", user_location);
                eventObj.put("Phone_Time", new Timestamp(System.currentTimeMillis()));
            } catch (JSONException e) {

                e.printStackTrace();

            }

            final availability_page current = this;

            sendEvent postEvents = new sendEvent(current, eventObj, serverAddress, phpAddress);

            postEvents.execute();

            new GraphRequest(
                    AccessToken.getCurrentAccessToken(),
                    "/me/friends",
                    null,
                    HttpMethod.GET,
                    new GraphRequest.Callback() {
                        public void onCompleted(GraphResponse response) {
                        /* handle the result */

                            try {
                                JSONObject tempObj = response.getJSONObject();
                                JSONArray userFriends = tempObj.getJSONArray("data");
                                friendsArr = userFriends;

                                //   for(int i = 0; i < userFriends.length(); i++)
                                //     friendsArr[i] = userFriends.getString(i);

                                eventObj.put("user_friends", userFriends);
                                System.out.println("Test in availPage: "+friendsArr);
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }


                           // sendAndPullEvents postEvents = new sendAndPullEvents(current, eventObj, serverAddress, phpAddress);

                         //   postEvents.execute();
                        }
                    }
            ).executeAsync();


    }


    public static class sendEvent extends connectJsonStream{
        public sendEvent(availability_page current,JSONObject newUserObj,String serverAddress,String phpAddress){
            super(current,newUserObj,serverAddress,phpAddress);
        }
        public sendEvent(availability_page current,JSONObject newUserObj,String serverAddress,String phpAddress,boolean write,boolean read){
            super(current,newUserObj,serverAddress,phpAddress,write,read);
        }
        @Override
       public void errorAction(){
            this.parent.getPopup(global.ERROR_GENERAL);
        }
        @Override
        protected void responseAction(){
           // Intent intent = new Intent(this.parent, homepage.class);

            int responseVal = 0;
            String responseMsg = new String();

                        try{

                    responseVal = this.parent.getCode(jsonResponse);
                    responseMsg =this.parent.getMsg(jsonResponse);
             //       this.parent.switchIntentOnResponse(jsonResponse,intent,false);
                    System.out.println("json response debug:"+responseVal+" "+responseMsg);
                    this.parent.switchIntentOnResponse(jsonResponse,homepage.class,true);
                } catch (Exception e) {
                    e.printStackTrace();
                    System.out.println("excp in avail:"+jsonResponse);
                    this.parent.getPopup("Response is empty");

                }

            //global.friendsArr = ((availability_page)this.parent).friendsArr;
            //intent.putExtra("friends", ((availability_page)this.parent).friendsArr.toString());
            //intent.putExtra("userId", ((availability_page)this.parent).userId);
            //intent.putExtra("userToken", global.tokenList);
            //intent.putExtra("userTokenStr", ((availability_page)this.parent).userTokenStr);
           // parent.startActivity(intent);
        }
    }

    public static class getLocation extends connectJsonStream{
        public getLocation(availability_page current,JSONObject newUserObj,String serverAddress,String phpAddress){
            super(current,newUserObj,serverAddress,phpAddress);
        }
        public getLocation(availability_page current,JSONObject newUserObj,String serverAddress,String phpAddress,boolean write,boolean read){
            super(current,newUserObj,serverAddress,phpAddress,write,read);
        }
        @Override
        public void errorAction(){
            this.parent.getPopup("Connection Error: Code "+this.response);
        }
        @Override
        public void onPostExecute(Void what){
            Spinner spinner = (Spinner) this.parent.findViewById(R.id.location_spinner);
            ArrayList<CharSequence> locations = new ArrayList<CharSequence>();
            String[] locList = null;
           // locations.add("test1");
           // locations.add("test2");
            try {

                System.out.println("drop down: "+jsonResponse);

               //Make sure not null
                int jsLen = jsonResponse.length();
                if(jsonResponse==null || jsLen<=0){
                    throw new NullPointerException();
                }else{
                    locList  = new String[jsLen];
                    for(int i =0;i<jsLen;i++){
                        locList[i] = jsonResponse.getJSONObject(i).getString("Location");
                    }
                }
            } catch (JSONException|NullPointerException e) {
                e.printStackTrace();
                if(e.getClass()==JSONException.class){

                }else{

                }
                super.parent.getPopup(global.ERROR_PULL_LOCATION);
                locList = global.defaultLocation;
            }finally{
                for(int i=0;i<locList.length;i++){
                    System.out.println("loc: "+locList[i]);
                    locations.add(locList[i]);
                }
            }
            ArrayAdapter<CharSequence> adapter = new  ArrayAdapter<CharSequence>(this.parent,android.R.layout.simple_spinner_dropdown_item,locations);
            //ArrayAdapter.createFromResource(this.parent,
            //R.array.location_array, android.R.layout.simple_spinner_item);
            //adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            spinner.setAdapter(adapter);
        }
    }
    @Override
    public void onBackPressed() {
        setResult(7, null);
        System.out.println("RETURNING");
        finish();
    }
}
