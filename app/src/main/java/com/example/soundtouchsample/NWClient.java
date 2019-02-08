package com.example.soundtouchsample;

import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import io.resourcepool.ssdp.client.SsdpClient;
import io.resourcepool.ssdp.model.DiscoveryListener;
import io.resourcepool.ssdp.model.DiscoveryRequest;
import io.resourcepool.ssdp.model.SsdpService;
import io.resourcepool.ssdp.model.SsdpServiceAnnouncement;
import okhttp3.Callback;
import okhttp3.HttpUrl;
import okhttp3.OkHttpClient;
import okhttp3.Request;

public class NWClient {
    private static final String TAG = NWClient.class.getSimpleName();
    private static final String APP_KEY = "Ln5MXIhIZQA8GnvKo4jxGKCNGQ9zJOoG";
    private static String mSoundTouchIP;
    private static final String MEZIAS_IP = "192.168.0.22";
    //use a single network client for all calls
    private static final OkHttpClient client = new OkHttpClient();

    public static void getVolume(Callback callback) {
        if (TextUtils.isEmpty(mSoundTouchIP)) {
            findSoundTouch(null, callback);
            return;
        }
        final HttpUrl url = new HttpUrl.Builder()
                .scheme("http")
                .host(MEZIAS_IP)
                .port(8090)
                .addPathSegment("volume")
                .build();
        Log.d(TAG, "url " + url.toString());
        final Request request = new Request.Builder()
                .url(url)
                .header("Bearer", APP_KEY)
                .build();
        client.newCall(request).enqueue(callback);
    }

    /*
    API notes
    urn:schemas-upnp-org:device:MediaRenderer:1 for devices that can play audio (SoundTouch speakers)
    urn:schemas-upnp-org:device:MediaServer:1 for devices that contain media (SoundTouch app and music server)
     */

    public static void findSoundTouch(final View textLabel, Callback callback) {
        final SsdpClient client = SsdpClient.create();
        DiscoveryRequest soundTouchSpeaker = DiscoveryRequest.builder()
                .serviceType("urn:schemas-upnp-org:device:MediaRenderer:1")
                .build();
        client.discoverServices(soundTouchSpeaker, new DiscoveryListener() {
            @Override
            public void onServiceDiscovered(SsdpService service) {
                //U have a fixed IP - sample code for production here
                mSoundTouchIP = service.getRemoteIp().getHostAddress();
                Log.d(TAG, service.getRemoteIp().getHostAddress());
                if (textLabel != null) {
                    if (!TextUtils.isEmpty(mSoundTouchIP)) {
                        ((TextView)textLabel).setText("API Address: " + mSoundTouchIP);
                    } else {
                        ((TextView)textLabel).setText("problem getting address");
                    }
                } else {
                    getVolume(callback);
                }
                client.stopDiscovery();
            }

            @Override
            public void onServiceAnnouncement(SsdpServiceAnnouncement announcement) {
                Log.v(TAG, "Service announced something: " + announcement);
            }

            @Override
            public void onFailed(Exception ex) {
                Log.e(TAG, "Discovery failed " + ex.toString());
                client.stopDiscovery();
            }
        });
    }
}
