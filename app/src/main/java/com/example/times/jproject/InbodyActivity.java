package com.example.times.jproject;

import android.app.Activity;
import android.app.ActivityManager;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.example.times.jproject.Login.JoinActivity;
import com.example.times.jproject.Login.LoginActivity;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Arrays;

public class InbodyActivity extends Activity {
    private Button button1;
    private ProgressBar progressBarRightArm;
    private ProgressBar progressBarLeftArm;
    private ProgressBar progressBarUpperbody;
    private ProgressBar progressBarRightleg;
    private ProgressBar progressBarLeftleg;
    private EditText[] editTexts;
    private SharedPreferences app_ID;
    private String recv_data, sID;
    private int[] value;
    private int inbodychk = 0;
    private AlertDialog.Builder alertBuilder;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_inbodyinfo);
        value = new int[5];
        progressBarRightArm = (ProgressBar)findViewById(R.id.progressBarRightArm);
        progressBarLeftArm  = (ProgressBar)findViewById(R.id.progressBarLeftArm);
        progressBarUpperbody = (ProgressBar)findViewById(R.id.progressBarUpperbody);
        progressBarRightleg = (ProgressBar)findViewById(R.id.progressBarRightleg);
        progressBarLeftleg = (ProgressBar)findViewById(R.id.progressBarLeftleg) ;

        app_ID = getSharedPreferences("APPID", Activity.MODE_PRIVATE);
        sID = app_ID.getString("sID", "");

        editTexts = new EditText[5];
        for(int i = 0; i < 5; i++){
            int resID = getResources().getIdentifier("editText" + i, "id", getPackageName());
            editTexts[i] = (EditText)findViewById(resID);
        }

        button1 = (Button)findViewById(R.id.button1);
    }

    public void onClickSave(View v) {

        for (int i = 0; i < 5; i++){
            if (isNum(editTexts[i].getText().toString())){
                if(Integer.parseInt(editTexts[i].getText().toString()) > 0 && Integer.parseInt(editTexts[i].getText().toString()) < 100){
                    value[i] = Integer.parseInt(editTexts[i].getText().toString());
                }else{
                    value[i] = 0;
                    Log.e("error", i+1 + "번인바디 정보 입력 오류");
                }
            }else
                value[i] = 0;
        }

        progressBarRightArm.setProgress(value[0]);
        progressBarLeftArm.setProgress(value[1]);
        progressBarUpperbody.setProgress(value[2]);
        progressBarRightleg.setProgress(value[3]);
        progressBarLeftleg.setProgress(value[4]);

        Arrays.sort(value);
        int check = Arrays.binarySearch(value, 0);

        if (check < 0){
            Log.e("error", "인바디 기입");
            inbodychk = 1;
            InbodyDB inbodyDB = new InbodyDB();
            inbodyDB.execute();
        }else{
            Toast.makeText(this, "1~99 값을 입력해주세요.", Toast.LENGTH_SHORT).show();
            Log.e("error", "인바디 미기입");
        }
    }

    public void onClickPass(View v) {
        Intent intent = new Intent(this, MainActivity.class);
        startActivity(intent);
        finish();
    }

    public boolean isNum(String s){
        try {
            Integer.parseInt(s);
            return true;
        }catch (NumberFormatException e){
            return false;
        }
    }

    public class InbodyDB extends AsyncTask<Void, Integer, Void> { //AsynceTask객체는 abstract로 작성되서 상속으로 사용해야함
        ProgressDialog loading;

        @Override
        protected void onPreExecute() { //스레드 실행하기전 준비
            super.onPreExecute();
            loading = ProgressDialog.show(InbodyActivity.this, "Please Wait", null, true, true);
        }

        @Override
        protected Void doInBackground(Void... unused) { //스레드로 일처리해주는 곳
            try {
                String data = "id=" + sID + "&Inbodychk=" + inbodychk + "&LeftArm=" + value[0] + "&RightArm=" + value[1] + "&UpperBody=" + value[2] +
                        "&LeftLeg=" + value[3] + "&RightLeg=" + value[4];
                Log.e("data", "data is : " + data);
                URL url = new URL("http://114.200.11.214/inbody.php");
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
                conn.setRequestMethod("POST");
                conn.setDoInput(true);
                conn.connect();

                OutputStream wr = conn.getOutputStream();
                wr.write(data.getBytes("UTF-8"));
                wr.flush();
                wr.close();

                InputStream is = null;
                BufferedReader reader = null;
                recv_data = "";              //sql실행후 잘됬는지 안됬는지 확인

                is = conn.getInputStream();
                reader = new BufferedReader(new InputStreamReader(is), 8 * 1024);
                String line = null;
                StringBuffer buff = new StringBuffer();
                while ((line = reader.readLine()) != null) {
                    buff.append(line + "\n");
                }
                recv_data = buff.toString().trim();
                Log.e("RECV DATA", recv_data);

            } catch (MalformedURLException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void s) {   //스레드가 일을 끝내고 UI처리에 쓰임
            super.onPostExecute(s);
            loading.dismiss();

            alertBuilder = new AlertDialog.Builder(InbodyActivity.this);
            if(recv_data.equals("0")){
                Log.e("RESULT", "인바디 입력이 완료되었습니다.");
                alertBuilder
                        .setTitle("알림")
                        .setMessage("인바디 입력이 완료되었습니다.")
                        .setCancelable(true)
                        .setPositiveButton("확인", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                Intent intent = new Intent(InbodyActivity.this, MainActivity.class);
                                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                                startActivity(intent);
                                finish();
                            }
                        });
                alertBuilder.show();
            }else{
                Log.e("RESULT", "인바디입력에 실패했습니다. " + recv_data);
                alertBuilder
                        .setTitle("알림")
                        .setMessage("인바디 입력에 실패했습니다.")
                        .setCancelable(true)
                        .setPositiveButton("확인", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                            }
                        });
                alertBuilder.show();
            }
        }
    }
}
