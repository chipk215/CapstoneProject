package com.keyeswest.trackme;


import android.content.ComponentName;
import android.content.UriMatcher;
import android.content.pm.PackageManager;
import android.content.pm.ProviderInfo;
import android.net.Uri;
import android.support.test.runner.AndroidJUnit4;

import com.keyeswest.trackme.data.LocationSchema;
import com.keyeswest.trackme.data.SegmentSchema;
import com.keyeswest.trackme.data.TracksContentProvider;

import org.junit.Test;
import org.junit.runner.RunWith;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.fail;

@RunWith(AndroidJUnit4.class)
public class TracksContentProviderTests extends TracksContentProviderBaseTest {



    //Test ContentProvider Registration
    @Test
    public void testProviderIsRegistered(){
        String packageName = mContext.getPackageName();
        String tracksProviderClassName = TracksContentProvider.class.getName();
        ComponentName componentName = new ComponentName(packageName, tracksProviderClassName);

        try{
            PackageManager pm = mContext.getPackageManager();
            ProviderInfo providerInfo = pm.getProviderInfo(componentName, 0);
            String actualAuthority = providerInfo.authority;
            String expectedAuthority = packageName;

            String incorrectAuthorityMessage =
                    "Error: TaskContentProvider registered with authority: " + actualAuthority +
                            " instead of expected authority: " + expectedAuthority;
            assertEquals(incorrectAuthorityMessage,
                    actualAuthority,
                    expectedAuthority);

        }catch(PackageManager.NameNotFoundException e){

            String providerNotRegisteredAtAll = "Error: TaskContentProvider not registered at " + mContext.getPackageName();
            /*
             * This exception is thrown if the ContentProvider hasn't been registered with the
             * manifest at all. If this is the case, you need to double check your
             * AndroidManifest file
             */
            fail(providerNotRegisteredAtAll);

        }
    }


    // Test Uri Matchers

    @Test
    public void testLocationDirectoryUriMatcher(){
        Uri testLocation = LocationSchema.LocationTable.CONTENT_URI;
        UriMatcher testMatcher = TracksContentProvider.buildUriMatcher();

        String failMessage = "Error: The Location URI was matched incorrectly.";
        int actualLocationMatchCode = testMatcher.match(testLocation);
        int expectedLocationMatchCode = TracksContentProvider.LOCATION_DIRECTORY;
        assertEquals(failMessage,expectedLocationMatchCode, actualLocationMatchCode);

    }

    @Test
    public void testLocationItemUriMatcher(){
        Uri testLocation = LocationSchema.LocationTable.CONTENT_URI;
        Uri testLocationItem = testLocation.buildUpon().appendPath("1").build();

        UriMatcher testMatcher = TracksContentProvider.buildUriMatcher();

        String failMessage = "Error: The Location with ID URI was matched incorrectly.";
        int actualLocationMatchCode = testMatcher.match(testLocationItem);
        int expectedLocationMatchCode = TracksContentProvider.LOCATION_WITH_ID;
        assertEquals(failMessage,expectedLocationMatchCode, actualLocationMatchCode);

    }


    @Test
    public void testSegmentDirectoryUriMatcher(){
        Uri testSegment = SegmentSchema.SegmentTable.CONTENT_URI;
        UriMatcher testMatcher = TracksContentProvider.buildUriMatcher();

        String failMessage = "Error: The Segment URI was matched incorrectly.";
        int actualSegmentMatchCode = testMatcher.match(testSegment);
        int expectedSegmentMatchCode = TracksContentProvider.SEGMENT_DIRECTORY;
        assertEquals(failMessage,expectedSegmentMatchCode, actualSegmentMatchCode);

    }

    @Test
    public void testSegmentItemUriMatcher(){
        Uri testSegment = SegmentSchema.SegmentTable.CONTENT_URI;
        Uri testSegmentItem = testSegment.buildUpon().appendPath("1").build();

        UriMatcher testMatcher = TracksContentProvider.buildUriMatcher();

        String failMessage = "Error: The Segment with ID URI was matched incorrectly.";
        int actualSegmentMatchCode = testMatcher.match(testSegmentItem);
        int expectedSegmentMatchCode = TracksContentProvider.SEGMENT_WITH_ID;
        assertEquals(failMessage,expectedSegmentMatchCode, actualSegmentMatchCode);

    }


}
