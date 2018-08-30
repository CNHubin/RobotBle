package com.hubin.bledemo;

import android.content.Context;
import android.support.test.InstrumentationRegistry;
import android.support.test.runner.AndroidJUnit4;
import android.util.Log;

import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.Random;

import static org.junit.Assert.assertEquals;

/**
 * Instrumented test, which will execute on an Android device.
 *
 * @see <a href="http://d.android.com/tools/testing">Testing documentation</a>
 */
@RunWith(AndroidJUnit4.class)
public class ExampleInstrumentedTest {
    private static final String TAG = "ExampleInstrumentedTest";
    @Test
    public void useAppContext() {
        // Context of the app under test.
        Context appContext = InstrumentationRegistry.getTargetContext();

        Random random = new Random();
        StringBuffer mStringBuffer = new StringBuffer();
        mStringBuffer.append(String.format("%02x", 0x52)+":");
        mStringBuffer.append(String.format("%02x", 0x54)+":");
        mStringBuffer.append(String.format("%02x", 0x00)+":");
        mStringBuffer.append(String.format("%02x", random.nextInt(0xff))+":");
        mStringBuffer.append(String.format("%02x", random.nextInt(0xff))+":");
        mStringBuffer.append(String.format("%02x", random.nextInt(0xff)));

        Log.d(TAG, "useAppContext: "+mStringBuffer.toString());
        assertEquals("com.hubin.bledemo", appContext.getPackageName());
    }
}
