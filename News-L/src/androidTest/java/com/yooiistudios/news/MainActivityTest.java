package com.yooiistudios.news;

import android.app.Activity;
import android.app.Instrumentation;
import android.test.ActivityInstrumentationTestCase2;
import android.test.UiThreadTest;

import com.yooiistudios.news.ui.activity.MainActivity;

/**
 * Created by Wooseong Kim in News-Android-L from Yooii Studios Co., LTD. on 14. 12. 29.
 *
 * SampleTest
 *  처음으로 만들어 보는 테스트
 */
public class MainActivityTest extends ActivityInstrumentationTestCase2<MainActivity> {
    Activity mActivity;
    Instrumentation mInstrumentation;

    public MainActivityTest() {
        super(MainActivity.class);
    }

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        mActivity = getActivity();
        mInstrumentation = getInstrumentation();
    }

    @Override
    protected void tearDown() throws Exception {
        super.tearDown();
    }

    @UiThreadTest
    public void testActivityOnCreate() {
        mInstrumentation.callActivityOnCreate(mActivity, null);
        assertNotNull(mActivity);
    }

    public void testTrue() {
        assertEquals(true, true);
    }

    public void testFalse() {
        assertEquals(false, false);
    }

    public void testTrueFalse() {
        assertEquals(true, false);
    }
}
