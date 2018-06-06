package com.keyeswest.trackme;



import com.facebook.stetho.Stetho;

import timber.log.Timber;

public class TrackMeMock extends TrackMe {

    @Override
    public void onCreate(){

        super.onCreate();
        Stetho.initializeWithDefaults(this);

        Timber.plant(new Timber.DebugTree(){
            // include line numbers
            @Override
            protected String createStackElementTag(StackTraceElement element){
                return super.createStackElementTag(element) + ':' + element.getLineNumber();
            }
        });
    }
}

