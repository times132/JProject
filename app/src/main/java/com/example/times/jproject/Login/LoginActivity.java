package com.example.times.jproject.Login;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.example.times.jproject.InbodyActivity;
import com.example.times.jproject.MainActivity;
import com.example.times.jproject.R;
import com.facebook.AccessToken;
import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.FacebookSdk;
import com.facebook.GraphRequest;
import com.facebook.GraphResponse;
import com.facebook.login.LoginResult;
import com.facebook.login.widget.LoginButton;
import com.kakao.auth.ISessionCallback;
import com.kakao.auth.Session;
import com.kakao.network.ErrorResult;
import com.kakao.usermgmt.UserManagement;
import com.kakao.usermgmt.callback.MeResponseCallback;
import com.kakao.usermgmt.response.model.UserProfile;
import com.kakao.util.exception.KakaoException;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

public class LoginActivity extends Activity {
    private LoginButton Login_facebook;
    private com.kakao.usermgmt.LoginButton Login_kakao;
    private CallbackManager callbackManager;
    private SessionCallback sessionCallback; //카톡
    private SharedPreferences settings, app_ID; //로그인 유지를 위해
    private EditText input_Email, input_PW;
    private String sEmail, sPW, recv_data, inbody_data;
    private ImageView fakeFacebook, fakeKakao;
    private Boolean loginChecked, autologin = false;
    private Button LoginBtn;
    AlertDialog.Builder alertBuilder;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        FacebookSdk.sdkInitialize(getApplicationContext()); //SDK 초기화 setContentView 보다 먼저실행되야됨
        setContentView(R.layout.activity_login);
        LoginBtn = (Button)findViewById(R.id.LoginBtn);
        input_Email = (EditText) findViewById(R.id.input_email);
        input_PW = (EditText) findViewById(R.id.input_pw);
        fakeKakao = (ImageView) findViewById(R.id.fake_kakao);
        fakeFacebook = (ImageView) findViewById(R.id.fake_facebook);

        if(NetworkConnection() == false){
            NotConnected_showAlert();
        }

        settings = getSharedPreferences("settings", Activity.MODE_PRIVATE); //폰에 저장된 로그인 정보 가져오기
        loginChecked = settings.getBoolean("LoginChecked", false);
        if (loginChecked){
            input_Email.setText(settings.getString("sEmail", ""));
            input_PW.setText(settings.getString("sPW", ""));
            LoginBtn.performClick();
        }

        if(isLogin()) {
            Toast.makeText(LoginActivity.this, "이미 로그인 되어있습니다", Toast.LENGTH_SHORT).show();
            Intent intent = new Intent(LoginActivity.this, InbodyActivity.class);
            startActivity(intent);

        }
        callbackManager = CallbackManager.Factory.create(); //로그인 응답 처리할 콜백관리자
        Login_facebook = (LoginButton)findViewById(R.id.loginBtn_facebook); //페북 로그인 버튼
        Login_facebook.setReadPermissions("public_profile", "user_friends", "email");
        Login_kakao = (com.kakao.usermgmt.LoginButton)findViewById(R.id.loginBtn_kakao) ;

        //페북에서 제공하는 기본 버튼
        Login_facebook.registerCallback(callbackManager, new FacebookCallback<LoginResult>() {
            @Override
            public void onSuccess(LoginResult loginResult) {
                //editor = mPrefs.edit();
                //editor.putString("access_token", loginResult.getAccessToken().getToken());
                //editor.commit();

                Log.e("토큰", loginResult.getAccessToken().getToken());
                Log.e("유저아이디", loginResult.getAccessToken().getUserId());
                Log.e("퍼미션 리스트", loginResult.getAccessToken().getPermissions()+"");

                //loginResult.getAccessToken 정보를 가지고 유저 정보를 가져올수 있다.
                GraphRequest request = GraphRequest.newMeRequest(loginResult.getAccessToken(),
                        new GraphRequest.GraphJSONObjectCallback() {
                            @Override
                            public void onCompleted(JSONObject object, GraphResponse response) {
                                try {
                                    Log.e("user profile", object.toString());
                                }catch(Exception e){
                                    System.out.println("연동 에러");
                                }
                            }
                        });
                request.executeAsync();
                NextActivity(InbodyActivity.class);
            }

            @Override
            public void onCancel() { }

            @Override
            public void onError(FacebookException error) {
                System.out.println("에러");
            }

        });

        sessionCallback = new SessionCallback();
        Session.getCurrentSession().addCallback(sessionCallback);
        Session.getCurrentSession().checkAndImplicitOpen();
    }

    @Override
    protected void onStop(){
        super.onStop();
        Log.e("autologin", "Autologin is: " + autologin);
        if (autologin){
            settings = getSharedPreferences("settings", Activity.MODE_PRIVATE);
            SharedPreferences.Editor editor = settings.edit();

            editor.putString("sEmail", input_Email.getText().toString());
            editor.putString("sPW", input_PW.getText().toString());
            editor.putBoolean("LoginChecked", true);
            Log.e("sEmail", sEmail);
            Log.e("sPW", sPW);
            editor.commit();
        }else{
            settings = getSharedPreferences("settings", Activity.MODE_PRIVATE);
            SharedPreferences.Editor editor = settings.edit();
            editor.clear();
            editor.commit();
        }
        finish();
    }

    public void onSnslogin(View v){
        switch (v.getId()) {
            case R.id.fake_kakao:  //fake_naver 내 버튼을 눌렀을 경우
                Login_kakao.performClick(); //performClick 클릭을 실행하게 만들어 자동으로 실행되도록 한다.
                break;
            case R.id.fake_facebook:  //fake_naver 내 버튼을 눌렀을 경우
                Login_facebook.performClick(); //performClick 클릭을 실행하게 만들어 자동으로 실행되도록 한다.
                break;
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if(Session.getCurrentSession().handleActivityResult(requestCode, resultCode, data)){
            return;
        } //카카오
        super.onActivityResult(requestCode, resultCode, data);
        callbackManager.onActivityResult(requestCode, resultCode, data); //페북
    }

    public boolean isLogin(){                               //페북 로그인인지 확인
        AccessToken token = AccessToken.getCurrentAccessToken();

        Log.d("(old token): ", ""+ token);

        return token != null;
    }

    public void request(){
        UserManagement.requestMe(new MeResponseCallback() {
            @Override
            public void onSessionClosed(ErrorResult errorResult) {
                Log.e("error", "Session Closed Error is " + errorResult.toString());
            }

            @Override
            public void onNotSignedUp() {

            }

            @Override
            public void onSuccess(UserProfile result) {
                //Toast.makeText(LoginActivity.this, "사용자 이름은 " + result.getNickname(), Toast.LENGTH_SHORT).show();
                Log.e("kakaoid", "kakaoID is : " + result.getId());
                NextActivity(InbodyActivity.class);
            }
        });
    }

    private class SessionCallback implements ISessionCallback {

        @Override
        public void onSessionOpened() {
            request();
        }

        @Override
        public void onSessionOpenFailed(KakaoException exception) {
            Toast.makeText(LoginActivity.this, "카카오톡 로그아웃되었습니다.", Toast.LENGTH_SHORT).show();
            //Log.e("error", "Session Fail Error is " + exception.getMessage().toString());
        }
    }

    public void onClickJoin(View v){       //회원가입 버튼 눌렀을때
        NextActivity(JoinActivity.class);
    }

    public void onClickLogin(View v){       //로그인 버튼 눌렀을때
        try{
            sEmail = input_Email.getText().toString();
            sPW = input_PW.getText().toString();
        }catch(NullPointerException e){
            Log.e("ERROR", e.getMessage());
        }
        LoginDB loginDB = new LoginDB();
        loginDB.execute();
    }

    public class LoginDB extends AsyncTask<Void, Integer, Void>{
        @Override
        protected Void doInBackground(Void... unuseds) {

            String data = "email=" + sEmail +"&pw=" + sPW;
            Log.e("POST", data);

            try{
                URL url = new URL("http://114.200.11.214/login.php");
                HttpURLConnection conn = (HttpURLConnection)url.openConnection();
                conn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
                conn.setRequestMethod("POST");
                conn.setDoInput(true);
                conn.connect();

                OutputStream wr = conn.getOutputStream();
                wr.write(data.getBytes("UTF-8"));
                wr.flush();
                wr.close();

                InputStream is = null;
                BufferedReader in = null;
                recv_data = "";

                is = conn.getInputStream();
                in = new BufferedReader(new InputStreamReader(is), 8 * 1024);
                String line = null;
                StringBuffer buff = new StringBuffer();
                while ((line = in.readLine()) != null){
                    Log.e("Line", "line is:" + line);
                    buff.append(line + "\n");
                }
                Log.e("RECV DATA", "buff: " + buff);
                recv_data = buff.toString().trim().split(" ")[0];
                inbody_data = buff.toString().trim().split(" ")[1];

                app_ID = getSharedPreferences("APPID", Activity.MODE_PRIVATE);
                SharedPreferences.Editor editor = app_ID.edit();

                editor.putString("sID", recv_data);
                editor.commit();

                Log.e("RECV DATA", "recv: " + recv_data + "   inbodydata: " + inbody_data);

            }catch (MalformedURLException e){
                e.printStackTrace();
            }catch (IOException e){
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);

            alertBuilder = new AlertDialog.Builder(LoginActivity.this);
            if (recv_data.equals("0")){
                Log.e("RESULT", "아이디가 없거나 비밀번호가 일치하지 않습니다.");
                alertBuilder
                        .setTitle("알림")
                        .setMessage("아이디가 없거나 비밀번호가 일치하지 않습니다.")
                        .setCancelable(true)
                        .setPositiveButton("확인", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                            }
                        });
                AlertDialog dialog = alertBuilder.create();
                dialog.show();
            }else{
                autologin = true;
                Log.e("RESULT", "로그인이 완료되었습니다.");
                Toast.makeText(getApplicationContext(), "로그인되었습니다.", Toast.LENGTH_SHORT).show();
                if (inbody_data.equals("1") || inbody_data.equals("0")){
                    NextActivity(MainActivity.class);
                }else{
                    NextActivity(InbodyActivity.class);
                }
            }
        }
    }

    private boolean NetworkConnection(){
        ConnectivityManager manager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        boolean isMobileAvailable = manager.getNetworkInfo(ConnectivityManager.TYPE_MOBILE).isAvailable();
        boolean isMobileConnect = manager.getNetworkInfo(ConnectivityManager.TYPE_MOBILE).isConnectedOrConnecting();
        boolean isWifiAvabilable = manager.getNetworkInfo(ConnectivityManager.TYPE_WIFI).isAvailable();
        boolean isWifiConnect = manager.getNetworkInfo(ConnectivityManager.TYPE_WIFI).isConnectedOrConnecting();

        if ((isWifiAvabilable && isWifiConnect) || (isMobileAvailable && isMobileConnect)){
            return true;
        }else{
            return false;
        }
    }

    private void NotConnected_showAlert(){
        AlertDialog.Builder builder = new AlertDialog.Builder(LoginActivity.this);
        builder.setTitle("네트워크 연결 오류");
        builder.setMessage("네트워크에 접속되지 않았습니다\n" + "무선 네트워크 연결상태를 확인해 주세요.")
                .setCancelable(false)
                .setPositiveButton("확인", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        finish();
                        android.os.Process.killProcess(android.os.Process.myPid());
                    }
                });
        AlertDialog dialog = builder.create();
        dialog.show();
    }

    public void NextActivity(Class nextactivity){
        Intent intent = new Intent(LoginActivity.this, nextactivity);
        startActivity(intent);
        finish();
    }
}
