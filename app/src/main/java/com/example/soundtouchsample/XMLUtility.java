package com.example.soundtouchsample;

import android.text.TextUtils;
import android.util.Log;
import android.util.Xml;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import java.io.IOException;
import java.io.StringReader;

public class XMLUtility {
    public static final String TAG = XMLUtility.class.getSimpleName();
    public static final String TARGET_KEY = "targetvolume";
    public static final String ACTUAL_KEY = "actualvolume";
    public static final String MUTE_KEY = "muteenabled";
    public static final String DEVICE_KEY = "deviceID";
    public static final String VOLUME_KEY = "volume";

    /**
     * Simple class to hold the values returned by the "volume" API call
     */
    static class VolumeResult {
        String target, actual, device;
        boolean muted;
    }

    public static VolumeResult parseVolume(String input) throws XmlPullParserException, IOException {
        final XmlPullParser parser = Xml.newPullParser();
        final VolumeResult values = new VolumeResult();
        parser.setFeature(XmlPullParser.FEATURE_PROCESS_NAMESPACES, false);
        //parser.setInput - can be input stream from okhttp3 response
        parser.setInput(new StringReader(input));
        parser.nextTag();
        //advance from start document
        parser.require(XmlPullParser.START_TAG, null, VOLUME_KEY);
        values.device = parser.getAttributeValue(null, DEVICE_KEY);

        String name;
        while (parser.next() != XmlPullParser.END_DOCUMENT) {
            name = parser.getName();
            Log.d(TAG, "name: " + name);
            if (TextUtils.isEmpty(name) || parser.getEventType() != XmlPullParser.START_TAG) {
                continue;
            } else {
                parser.next();
                //got a name, parser is at the start tag, move from the key to the value
                switch(name) {
                    case TARGET_KEY:
                        values.target = parser.getText();
                        break;
                    case ACTUAL_KEY:
                        values.actual = parser.getText();
                        break;
                    case MUTE_KEY:
                        values.muted = "true".equalsIgnoreCase(parser.getText());
                        Log.d(TAG, "mute: " + values.muted);
                }
            }
        }
        return values;
    }

    // sample xml output from API call to get volume
    // <volume deviceID="64CFD997DEF5">
    // <targetvolume>53</targetvolume>
    // <actualvolume>53</actualvolume>
    // <muteenabled>false</muteenabled>
    // </volume>

}
