package com.keyeswest.trackme;



import timber.log.Timber;

public class TrackMeDebug extends TrackMe {

    @Override
    public void onCreate(){
        super.onCreate();

        Timber.plant(new FileLoggingTree(getApplicationContext()){
            // include line numbers
            @Override
            protected String createStackElementTag(StackTraceElement element){
                return super.createStackElementTag(element) + ':' + element.getLineNumber();
            }
        });

    }
}