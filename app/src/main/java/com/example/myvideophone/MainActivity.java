package com.example.myvideophone;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.Socket;
import java.net.URLEncoder;

public class MainActivity extends AppCompatActivity {
    private Socket socketForServer;  //소켓생성
    private Socket socketForRasp;  //소켓생성
    BufferedReader in;      //서버로부터 온 데이터를 읽는다.
    PrintWriter out1;        //서버에 데이터를 전송한다.
    PrintWriter out2;        //서버에 데이터를 전송한다.
    EditText editText;
    Button button;
    Button button2;
    WebView webView;
    TextView textView;
    String rstFaceRecog;
    String ReturnEditText;
    //QLiteDatabase visitorsDB = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        webView =(WebView)findViewById(R.id.webView);
        editText = (EditText)findViewById(R.id.editText);
        button = (Button)findViewById(R.id.button);
        button2 = (Button)findViewById(R.id.button2);
        textView = (TextView) findViewById(R.id.textView);

        webView.setWebViewClient(new WebViewClient());
        webView.setBackgroundColor(255);
        webView.getSettings().setLoadWithOverviewMode(true);
        webView.getSettings().setUseWideViewPort(true);

        WebSettings webSettings = webView.getSettings();
        webSettings.setJavaScriptEnabled(true);

        webView.loadData("<html><head><style type='text/css'>body{margin:auto auto;text-align:center;} img{width:100%25;}div{overflow:hidden;}</style></head><body><div><img src='http://192.168.0.17:8090/stream/video.mjpeg'/></div></body></html>","text/html","UTF-8");
        webView.loadUrl("http://192.168.219.104:8090/?action=stream");

        Thread worker1 = new Thread() {    //worker 를 Thread 로 생성
            public void run() { //스레드 실행구문
                try {
                    //소켓을 생성하고 입출력 스트립을 소켓에 연결한다.
                    socketForServer = new Socket("192.168.219.107", 8000); //소켓생성
                    out1 = new PrintWriter(socketForServer.getOutputStream(),true); //데이터를 전송시 stream 형태로 변환하여                                                                                                                       //전송한다.
                    in = new BufferedReader(new InputStreamReader(
                            socketForServer.getInputStream())); //데이터 수신시 stream을 받아들인다.
                } catch (IOException e) {
                    e.printStackTrace();
                }
                //소켓에서 데이터를 읽어서 화면에 표시한다.
                try {
                    while (true) {
                        rstFaceRecog = in.readLine(); // in으로 받은 데이타를 String 형태로 읽어 data 에 저장
                        textView.post(new Runnable() {
                            public void run() {
                                if (rstFaceRecog.equals("True")){
                                    textView.setText("택배기사입니다"); //글자출력칸에 서버가 보낸 메시지를 받는다.
                                }
                                else {
                                    Log.d("jinjin",rstFaceRecog);
                                    textView.setText("낯선 사람입니다");
                                }
                            }
                        });
                    }
                } catch (Exception e) {
                }
            }
        };
        worker1.start();  //onResume()에서 실행.

        Thread worker2 = new Thread() {    //worker 를 Thread 로 생성
            public void run() { //스레드 실행구문
                try {
                    //소켓을 생성하고 입출력 스트립을 소켓에 연결한다.
                    socketForRasp = new Socket("192.168.219.104", 8000); //소켓생성
                    out2 = new PrintWriter(socketForRasp.getOutputStream(),true); //데이터를 전송시 stream 형태로 변환하여                                                                                                                       //전송한다.
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        };
        worker2.start();  //onResume()에서 실행
    }

    @Override
    protected void onStop() {  //앱 종료시
        super.onStop();
        try {
            socketForServer.close(); //소켓을 닫는다.
            socketForRasp.close(); //소켓을 닫는다.
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void requestTTS(View v){
        // 버튼 누르면 자동으로 키보드 내려가게 하기
        InputMethodManager mInputMethodManager = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
        mInputMethodManager.hideSoftInputFromWindow(editText.getWindowToken(), 0);


        ReturnEditText = editText.getText().toString();
        Log.d("jinjin",ReturnEditText);
        if (ReturnEditText != null) { // 아무것도 입력된 것이 아니라면
            Thread sender = new Thread() {
                public void run() {
                    out2.print(ReturnEditText);
                    out2.flush();
                }
            };
            sender.start();
        }
        Toast.makeText(this.getApplicationContext(),"말하기 요청을 완료했습니다.", Toast.LENGTH_SHORT).show();
    }

    public void requestFaceRecog(View v){
        String ss = "recognize";
        Thread sender = new Thread(){
            public void run(){
                out1.print("recognize");
                out1.flush();
            }
        };
        sender.start();
    }

}
