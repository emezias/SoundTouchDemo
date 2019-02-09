package com.example.soundtouchsample;

import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

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
    private static final String APP_KEY = "put your app key here";
    private static final String SOUNDTOUCH_SVC = "urn:schemas-upnp-org:device:MediaRenderer:1";
    /*
    API notes
    urn:schemas-upnp-org:device:MediaRenderer:1 for devices that can play audio (SoundTouch speakers)
    urn:schemas-upnp-org:device:MediaServer:1 for devices that contain media (SoundTouch app and music server)
     */

    private static String mSoundTouchIP;
    //discovered IP address and address coded from the app
    private static final String MEZIAS_IP = "192.168.0.22";
    //use a singleton network client for all API calls
    private static final OkHttpClient client = new OkHttpClient();

    /**
     * This method calls the speaker to ask for the current volume information
     * @param callback - implementation of the interface that reads the XML
     */
    public static void getVolume(Callback callback) {
        if (TextUtils.isEmpty(mSoundTouchIP)) {
            findSoundTouch(null, callback);
            return;
        }
        Log.d(TAG, "IP found: " + mSoundTouchIP);
        final HttpUrl url = new HttpUrl.Builder()
                .scheme("http")
                //.host(mSoundTouchIP)
                // use IP found in SoundTouch App's settings instead of discovery
                .host(MEZIAS_IP)
                .port(8090)
                .addPathSegment("volume")
                .build();

        final Request request = new Request.Builder()
                .url(url)
                .header("Bearer", APP_KEY)
                .build();
        client.newCall(request).enqueue(callback);
    }

    /**
     * This method listens on the network to find the SoundTouch with SSDP discovery
     * It is important to stopDiscovery when the discover service call has returned
     * @param textLabel - The TextView that will display the IP address
     * @param callback - the parameter for getVolume, the MainActivity
     */
    public static void findSoundTouch(final View textLabel, Callback callback) {
        final SsdpClient client = SsdpClient.create();
        final DiscoveryRequest soundTouchSpeaker = DiscoveryRequest.builder()
                .serviceType(SOUNDTOUCH_SVC)
                .build();
        //inline listener created for the service callbacks
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
