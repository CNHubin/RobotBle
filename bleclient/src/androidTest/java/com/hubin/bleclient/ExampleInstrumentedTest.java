package com.hubin.bleclient;

import android.support.test.runner.AndroidJUnit4;

import com.hubin.bleclient.utils.TimeUtils;

import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * Instrumented test, which will execute on an Android device.
 *
 * @see <a href="http://d.android.com/tools/testing">Testing documentation</a>
 */
@RunWith(AndroidJUnit4.class)
public class ExampleInstrumentedTest {
    @Test
    public void useAppContext() {

        String s = TimeUtils.timeStamp();
        System.out.println("time="+s);

        // Context of the app under test.
//        Context appContext = InstrumentationRegistry.getTargetContext();

//        assertEquals("com.hubin.bleclient", appContext.getPackageName());


        /*Observable<Integer> observable = Observable.just(1, 2, 3, 4);

        observable.subscribe(new Observer<Integer>() {
            @Override
            public void onSubscribe(Disposable d) {

            }

            @Override
            public void onNext(Integer s) {
                System.out.println("s = [" + s + "sss" + "]");
            }

            @Override
            public void onError(Throwable e) {
                System.out.println("e = [" + e + "]");
            }

            @Override
            public void onComplete() {
                System.out.println("Complete");
            }
        });*/

    }
}
