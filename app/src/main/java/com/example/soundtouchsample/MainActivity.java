package com.example.soundtouchsample;

import android.os.Bundle;
import android.text.Html;
import android.text.TextUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;

import com.google.android.material.snackbar.Snackbar;

import org.xmlpull.v1.XmlPullParserException;

import java.io.IOException;
import java.net.HttpURLConnection;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;

public class MainActivity extends AppCompatActivity implements Callback {
    public static final String TAG = MainActivity.class.getSimpleName();
    TextView mVolumeText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mVolumeText = findViewById(R.id.labelVolume);
        final Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        ImageButton button = findViewById(R.id.findSpeaker);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                (new NWClient()).findSoundTouch(findViewById(R.id.labelIPAddress), null);
            }
        });
        //reduce, reuse, recycle - can use one local variable for both listeners
        button = findViewById(R.id.volumeButton);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "show status indicator here?", Snackbar.LENGTH_LONG).show();
                NWClient.getVolume(MainActivity.this);
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        if (item.getItemId() == R.id.action_settings) {
            Snackbar.make(findViewById(R.id.findSpeaker), "add some settings", Snackbar.LENGTH_LONG).show();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onFailure(Call call, IOException ex) {
        Log.e(TAG, call.request().url().toString() + " Network call failed " + ex.toString());
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                mVolumeText.setText(call.request().url().toString() + " Network call failed " + ex.toString());
            }
        });
    }

    @Override
    public void onResponse(Call call, final Response response) throws IOException {
        Log.d(TAG, "response to " + call.request().url().toString());
        //this is where the network call to get the API result will finish
        String vol_response;
        if (response.code() == HttpURLConnection.HTTP_OK) {
            vol_response = response.body().string();
            Log.d(TAG, vol_response);
            if (!TextUtils.isEmpty(vol_response)) {
                try {
                    XMLUtility.VolumeResult result = XMLUtility.parseVolume(vol_response);
                    vol_response = "Volume set: " + result.actual;
                } catch (XmlPullParserException e) {
                    e.printStackTrace();
                    vol_response = "Error with return: " + e.getMessage();
                }
            } else {
                vol_response = "Error, empty API call response";
            }

        } else {
            Log.e(TAG, "Error code:" + response.code() + response.message());
            vol_response = Html.fromHtml(response.body().string(), Html.FROM_HTML_MODE_COMPACT).toString();
        }
        showResult(vol_response);
    }

    /**
     * Display the result from the API call to the user on the main thread
     * @param parseResult - API result to display
     */
    private void showResult(final String parseResult) {
        //network response is an Async callback coming in on a separate thread
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                mVolumeText.setText(parseResult);
            }
        });
    }
}
