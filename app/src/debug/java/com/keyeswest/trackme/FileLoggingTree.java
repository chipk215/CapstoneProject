package com.keyeswest.trackme;

import android.content.Context;
import android.os.Environment;
import android.util.Log;

import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import timber.log.Timber;

public class FileLoggingTree extends Timber.DebugTree {

    private static final String TAG = FileLoggingTree.class.getSimpleName();

    private Context context;
    private File file;

    public FileLoggingTree(Context context) {
        super();
        this.context = context;

        try {
            File directory = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS) + "/trackerlogs");
            String fileName = "TrackLog.txt";

            if (!directory.exists()) {
                directory.mkdir();
            }

            file = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS) + "/trackerlogs" + File.separator + fileName);

            file.createNewFile();



        }catch (Exception e) {
            Log.e(TAG, "Error while logging into file : " + e);
        }

    }

    @Override
    protected void log(int priority, String tag, String message, Throwable t) {

        try {

            if (file.exists()) {

                OutputStream fileOutputStream = new FileOutputStream(file, true);

                String logTimeStamp = new SimpleDateFormat("E MMM dd yyyy 'at' hh:mm:ss:SSS aaa",
                        Locale.getDefault()).format(new Date());

                String formattedMessage = logTimeStamp + "  " + message + System.getProperty("line.separator");
                fileOutputStream.write(formattedMessage.getBytes());
                fileOutputStream.flush();
                fileOutputStream.close();

            }

            //if (context != null)
            //MediaScannerConnection.scanFile(context, new String[]{file.getAbsolutePath()}, null, null);

        } catch (Exception e) {
            Log.e(TAG, "Error while logging into file : " + e);
        }

        super.log(priority, tag, message, t);

    }
}