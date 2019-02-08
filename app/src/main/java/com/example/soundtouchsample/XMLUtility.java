package com.example.soundtouchsample;

import android.text.TextUtils;
import android.util.Log;
import android.util.Xml;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;

public class XMLUtility {
    public static final String TAG = XMLUtility.class.getSimpleName();
    // sample xml output from API call to get volume
    // <volume deviceID="64CFD997DEF5">
    // <targetvolume>53</targetvolume>
    // <actualvolume>53</actualvolume>
    // <muteenabled>false</muteenabled>
    // </volume>
    public static final String TARGET_KEY = "targetvolume";
    public static final String ACTUAL_KEY = "actualvolume";
    public static final String MUTE_KEY = "muteenabled";
    public static final String DEVICE_KEY = "deviceID";
    public static final String VOLUME_KEY = "volume";

    static class VolumeResult {
        String target, actual;
        boolean muted;
    }

    public static VolumeResult parseVolume(String input) throws XmlPullParserException, IOException {
        XmlPullParser parser = Xml.newPullParser();
        parser.setFeature(XmlPullParser.FEATURE_PROCESS_NAMESPACES, false);
        //parser.setInput(in, null) - can be input stream
        parser.setInput(new StringReader(input));
        parser.nextTag();
        //advance from start document
        return readVolume(parser);
    }

    private static VolumeResult readVolume(XmlPullParser parser) throws XmlPullParserException, IOException {
        VolumeResult values = new VolumeResult();
        parser.require(XmlPullParser.START_TAG, null, VOLUME_KEY);
        String name;
        while (parser.next() != XmlPullParser.END_DOCUMENT) {
            name = parser.getName();
            if (TextUtils.isEmpty(name) || parser.getEventType() != XmlPullParser.START_TAG) {
                continue;
            } else {
                parser.next();
                //got a name, m
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

}
