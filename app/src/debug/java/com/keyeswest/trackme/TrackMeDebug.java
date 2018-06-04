package com.keyeswest.trackme;



import timber.log.Timber;

public class TrackMeDebug extends TrackMe {

    @Override
    public void onCreate(){
        super.onCreate();

        Timber.plant(new Timber.DebugTree(){
            // include line numbers
            @Override
            protected String createStackElementTag(StackTraceElement element){
                return super.createStackElementTag(element) + ':' + element.getLineNumber();
            }
        });

    }
}