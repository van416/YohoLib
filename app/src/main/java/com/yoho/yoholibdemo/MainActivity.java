package com.yoho.yoholibdemo;

import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;

import com.yoho.lib.http.HttpClient;
import com.yoho.lib.http.HttpClientFactory;

import java.io.IOException;
import java.util.List;
import java.util.Map;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "MainActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        new AsyncTask<Void, Void, Void>() {
            @Override
            protected Void doInBackground(Void... params) {
                try {
                    HttpClient client = HttpClientFactory.getInstance();
                    String result = client.get("http://www.baidu.com");
                    client.setListener(new HttpClient.HttpClientListener() {
                        @Override
                        public void onError(HttpClient client, Throwable e) {

                        }

                        @Override
                        public void onData(HttpClient client, byte[] buffer, int offset, int length) {

                        }

                        @Override
                        public void onComplete(HttpClient client) {
                            Log.e(TAG, "thread: " + Thread.currentThread().getName());
                        }

                        @Override
                        public void onCancel(HttpClient client) {

                        }

                        @Override
                        public void onHeaders(HttpClient client, Map<String, List<String>> headerFields) {

                        }
                    });
                    client.cancel();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                return null;
            }
        }.execute();

    }
}
