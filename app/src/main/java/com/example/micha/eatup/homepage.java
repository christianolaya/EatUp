package com.example.micha.eatup;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.facebook.AccessToken;
import com.facebook.CallbackManager;
import com.facebook.FacebookException;
import com.facebook.GraphRequest;
import com.facebook.GraphResponse;
import com.facebook.HttpMethod;
import com.facebook.login.LoginBehavior;
import com.facebook.login.LoginResult;
import com.facebook.login.widget.*;
import com.facebook.FacebookCallback;
import com.facebook.login.LoginManager;

import java.sql.Timestamp;
import java.util.Date;
import java.util.concurrent.TimeUnit;

import com.squareup.picasso.*;

public class homepage extends androidPage {

    private final static String serverAddress = global.serverAddress;
    private final static String phpAddress= "/getEvent.php";
    private final static String removeAddress = "/removeEvent.php";
    private JSONArray friendsArr;
    private JSONObject eventObj;
    private JSONObject userPics = new JSONObject();
    private final int personalTagKey = 1;
    public String viewAvail;
    protected TextView myEvent;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.homepage);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

         myEvent = (TextView) findViewById(R.id.current_availability);

         viewAvail = "No Availability Indicated";

        eventObj = new JSONObject();
        final homepage current = this;

        /**
         * TODO: TEST IF WE STILL HAVE ACCESS TOKEN
         * IF NOT, SEND TO LOGIN
         */

        //on creation of page, send fb request for friends list
        //then update screen
        System.out.println("homepage: accesstoken test- "+AccessToken.getCurrentAccessToken().getToken());
        new GraphRequest(
                AccessToken.getCurrentAccessToken(),
                "/me/friends",
                new Bundle(),
                HttpMethod.GET,
                new GraphRequest.Callback() {
                    public void onCompleted(GraphResponse response) {
                        /* handle the result */

                        try {

                            JSONObject tempObj = response.getJSONObject();
                            System.out.println("tempObj: "+tempObj);
                            JSONArray userFriends = tempObj.getJSONArray("data");
                            friendsArr = userFriends;

                            //   for(int i = 0; i < userFriends.length(); i++)
                            //     friendsArr[i] = userFriends.getString(i);

                            eventObj.put("user_friends", userFriends);
                            eventObj.put("userid",global.token.getUserId());
                            System.out.println("userFriends: "+userFriends+"|id: "+global.token.getUserId());
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }

                        //create connection to backend with information from graph api request
                        homepage.pullEvents pullCurrentEvents = new homepage.pullEvents(current, eventObj, serverAddress, phpAddress);

                        pullCurrentEvents.execute();
                    }
                }
        ).executeAsync();

    }

    public void card_click(View view){
        //should pass more info including event details but i know other things are required if we are changing app flow

        if (global.gettingLunch){
            //do nothing
        } else {
            Intent intent = new Intent(this, user_view.class);

            TextView user_card_text = (TextView) view.findViewById(R.id.user_name);
            //global.potentialLuncher
            intent.putExtra("potentialLuncher", (String) user_card_text.getText());

            String hostId = (String) view.getTag();
            //global.potentialHostId = hostId;
            intent.putExtra("potentialHostId", hostId);

            //for now, ill just return the name of the user
            //intent.putExtra("user_name", global.name);
            try {
                String pic_url = userPics.getString(hostId);
                intent.putExtra("pic_url", pic_url);
            } catch (JSONException e) {
                e.printStackTrace();
            }

            startActivity(intent);
        }
    }

    public void delete_availability(View view) {

        if(global.gettingLunch) {
            global.gettingLunch = false;
            global.lunchWithId = null;
            global.lunchWith = null;

        } else {
            removeEvents remove = new removeEvents(this, eventObj, serverAddress, removeAddress);
            remove.execute();
        }

        Intent intent = new Intent(this, availability_page.class);
        getApplicationContext().startActivity(intent);
           // this.finish();
    }
    public void availability_page(View view) {

        global.gettingLunch = false;
        global.lunchWith = null;
        global.lunchWithId = null;

        Intent intent = new Intent(this, availability_page.class);
        intent.putExtra("from_homepage", true);
        startActivityForResult(intent, 1);
    }
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(resultCode==RESULT_OK){
            Intent refresh = new Intent(this, this.getClass());
            startActivity(refresh);
            System.out.println("REACHED");
            this.finish();
        }
    }
    public static class pullEvents extends connectJsonStream{

        public pullEvents(homepage current,JSONObject newUserObj,String serverAddress,String phpAddress){
            super(current,newUserObj,serverAddress,phpAddress);
        }
        public pullEvents(homepage current,JSONObject newUserObj,String serverAddress,String phpAddress,boolean write,boolean read){
            super(current,newUserObj,serverAddress,phpAddress,write,read);
        }
        @Override
        public void onPostExecute(Void what){
            homepage current = (homepage)this.parent;
            boolean existMyEvent = false;
            for (int i =0; i< jsonResponse.length(); i++) {

                String id;
                String username;
                String start_time;
                String location;
                String pic_url = null;
                String show_start_time = new String();
                String availability_time = new String();
                try {

                    JSONObject row = jsonResponse.getJSONObject(i);

                    System.out.println(row);

                    id = row.getString("hostid");
                    username = row.getString("username");
                    start_time = row.getString("StartTime");
                    pic_url = row.getString("picUrl");
                    System.out.println("OBJECT PARSE PIC: " + pic_url);

                    Date show_start_time_date = global.dateFormat.parse( start_time);
                    show_start_time = global.showCase.format(new Date(show_start_time_date.getTime()));
                    int duration = row.getInt("Duration");
                    location = row.getString("Location");

                    global.card2eventId.put(id,row.getInt("eventid"));
                    //System.out.println("Row: "+id+" " + username  + " "+start_time+" "+location);
                    Date date_start = global.dateFormat.parse( start_time);
                    Date date_end = new Date(date_start.getTime()+TimeUnit.MINUTES.toMillis(duration));
                    String start_time_short = global.showCase.format(date_start.getTime());
                    String end_time_short = global.showCase.format(date_end);
                    availability_time = start_time_short + " ~ " + (end_time_short);

                    if(id.equals(global.token.getUserId())){
                        //Self
                        //global.myStart = start_time;
                        existMyEvent=true;
                        System.out.println("Set True");
                        System.out.println("date: "+date_start+" "+end_time_short+" "+duration);

                        //global.myEnd = end_time;
                        if(start_time != null && end_time_short != null){
                            //toString() to clone
                            ((homepage) this.parent).viewAvail =new String( availability_time);//global.showCase.format(date_start.getTime()) + " ~ " + (end_time_short);
                        } else {

                            //should not happen
                            ((homepage)this.parent).myEvent.setText( "No Availibility Indicated" );

                        }
                        ((homepage)this.parent).myEvent.setText( ((homepage) this.parent).viewAvail);
                    }
                }
                catch (Exception e) {
                    e.printStackTrace();

                    System.out.println("Exception:"+e.getClass().getName()+"Error Code:"+this.parent.getMsg(jsonResponse));
                    id = "--error--";
                    username = "--error--";
                    start_time = "--error--";
                    location = "--error--";

                    //
                }

                //temporarily comment out if so i can see my own card and hopefully pic
                if(!(id.equals(global.token.getUserId()))) {
                    LinearLayout user_card_layout = (LinearLayout) current.findViewById(R.id.user_card_layout);
                    View user_card = current.getLayoutInflater().inflate(R.layout.user_card, user_card_layout, false);



                    user_card.setTag(id);

                    TextView user_name = (TextView) user_card.findViewById(R.id.user_name);
                    user_name.setText(username);

                    TextView user_start_time = (TextView) user_card.findViewById(R.id.user_availability);
                    user_start_time.setText(availability_time);

                    TextView user_location = (TextView) user_card.findViewById(R.id.user_location);
                    user_location.setText(location);

                    ImageView image_view = (ImageView) user_card.findViewById(R.id.profile_pic);
                    if (!pic_url.equals("") && !pic_url.equals(null)) {
                        System.out.println("PICTURE: " + pic_url);
                        Picasso.with(this.parent).load(pic_url).into(image_view);

                        try {
                            ((homepage) this.parent).userPics.put(id, pic_url);
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }

                    int padding_in_dp = 5;
                    final float scale = current.getResources().getDisplayMetrics().density;
                    int padding_in_px = (int) (padding_in_dp * scale + 0.5f);
                    LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                            LinearLayout.LayoutParams.MATCH_PARENT,
                            LinearLayout.LayoutParams.WRAP_CONTENT
                    );
                    params.setMargins(0, 0, 0, padding_in_px);
                    user_card.setLayoutParams(params);
                    user_card_layout.addView(user_card);
                }

            }
            if(!existMyEvent && !global.gettingLunch){
                System.out.println("flag own:"+existMyEvent);
                this.parent.finish();
            } else if ( global.gettingLunch) {
                ((homepage)this.parent).myEvent.setText( "Getting lunch with " + global.lunchWith );
            }

        }
        @Override
        protected void errorAction(){
            this.parent.getPopup(global.ERROR_GENERAL+" "+this.response);
        }
        @Override
        protected void responseAction(){

            //Intent intent = new Intent(this.parent, homepage.class);

            //for now, we know parent is a 'homepage', so its ok to cast as such

            final homepage current = (homepage)parent;

            if (jsonResponse!=null){
                System.out.println("jsonResponse: not null");

            }
            else{
                System.out.println("Null Response");
            }


            global.friendsArr = ((homepage)this.parent).friendsArr;
        }
    }

    public void refresh_page(View view) {
        Intent intent = getIntent();
        finish();
        startActivity(intent);
    }
    public static class removeEvents extends connectJsonStream{
        public removeEvents(homepage current,JSONObject newUserObj,String serverAddress,String phpAddress){
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
