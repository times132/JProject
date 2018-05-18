package com.example.times.jproject.Login;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.example.times.jproject.R;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

public class JoinActivity extends Activity {

    EditText input_Email, input_PW, input_PWCHK;
    String sEamil, sPW, sPWCHK, recv_data;
    AlertDialog.Builder alertBuilder;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_join);

        input_Email = (EditText) findViewById(R.id.input_email);
        input_PW = (EditText) findViewById(R.id.input_pw);
        input_PWCHK = (EditText) findViewById(R.id.input_pwchk);
    }

    public void SaveDataBtn(View view) {
        sEamil = input_Email.getText().toString();
        sPW = input_PW.getText().toString();
        sPWCHK = input_PWCHK.getText().toString();

        Log.d("data", "ID 널값 검사: " + sEamil.equals(""));

        if (!(sEamil.equals("")) && (sPW.equals(sPWCHK))) {         //패스워드랑 패스워드 확인이랑 일치하면 서버에 전송
            JoinDB joinDB = new JoinDB();
            joinDB.execute();
        } else if (sPW.length() < 3){
            Toast.makeText(this, "패스워드가 너무 짧습니다.", Toast.LENGTH_SHORT).show();
        } else if(!(sPW.equals(sPWCHK))){
            Toast.makeText(this, "패스워드가 일치하지않습니다.", Toast.LENGTH_SHORT).show();
        } else if(sEamil.equals("")){
            Toast.makeText(this, "아이디를 입력하세요.", Toast.LENGTH_SHORT).show();
        }
    }


    public class JoinDB extends AsyncTask<Void, Integer, Void> { //AsynceTask객체는 abstract로 작성되서 상속으로 사용해야함
        ProgressDialog loading;

        @Override
        protected void onPreExecute() { //스레드 실행하기전 준비
            super.onPreExecute();
            loading = ProgressDialog.show(JoinActivity.this, "Please Wait", null, true, true);
        }

        @Override
        protected Void doInBackground(Void... unused) { //스레드로 일처리해주는 곳
            try {

                String data = "email=" + sEamil + "&pw=" + sPW + "";
                Log.e("data", data);
                URL url = new URL("http://114.200.11.214/join.php");
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

            alertBuilder = new AlertDialog.Builder(JoinActivity.this);
            if(recv_data.equals("1062")){
                Log.e("RESULT", "회원가입에 실패했습니다. " + recv_data);
                alertBuilder
                        .setTitle("알림")
                        .setMessage("아이디가 중복됬습니다.")
                        .setCancelable(true)
                        .setPositiveButton("확인", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                            }
                        });
                alertBuilder.show();
            }else{
                Log.e("RESULT", "회원가입이 완료되었습니다.");
                alertBuilder
                        .setTitle("알림")
                        .setMessage("회원가입이 완료되었습니다.")
                        .setCancelable(true)
                        .setPositiveButton("확인", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                Intent intent = new Intent(JoinActivity.this, LoginActivity.class);
                                startActivity(intent);
                                finish();
                            }
                        });
                alertBuilder.show();
            }
        }
    }
}

