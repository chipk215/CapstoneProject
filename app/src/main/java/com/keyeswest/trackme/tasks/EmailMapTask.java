package com.keyeswest.trackme.tasks;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.AsyncTask;
import android.support.v4.content.FileProvider;
import android.widget.Toast;

import java.io.File;
import java.io.FileOutputStream;
import java.lang.ref.WeakReference;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import timber.log.Timber;


public class EmailMapTask extends AsyncTask<Void, Void, File>  {

    private static final String IMAGES_DIRECTORY = "images";
    private static final String MAPS_DIRECTORY = "maps";
    private static final String FILE_PROVIDER_AUTHORITY = "com.keyeswest.trackme.fileprovider";
    private static final String SUBJECT = "Trip Maps";


    public interface ResultsCallback{
        void onComplete(File file);
    }

    private WeakReference<Context> mContext;
    private Bitmap mBitmap;
    private ResultsCallback mCallback;

    public EmailMapTask(Context context, Bitmap snapShot, ResultsCallback callback ){
        mContext =  new WeakReference<>(context);
        mBitmap = snapShot;
        mCallback = callback;
    }

    @Override
    protected File doInBackground(Void... voids) {

        // create the file for saving the snapshot
        File imagesDirectory = new File(mContext.get().getFilesDir(), IMAGES_DIRECTORY);
        boolean mkdir = imagesDirectory.mkdir();
        if (mkdir) {
            File mapsDirectory = new File(imagesDirectory, MAPS_DIRECTORY);
            mkdir = mapsDirectory.mkdirs();
            if (! mkdir){
                return null;
            }
            SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US);
            String currentDateAndTime = sdf.format(new Date());
            File out = new File(mapsDirectory, "map_" + currentDateAndTime + ".png");

            try {
                FileOutputStream os = new FileOutputStream(out);
                mBitmap.compress(Bitmap.CompressFormat.PNG, 100, os);
                os.close();

                Uri contentUri = FileProvider.getUriForFile(mContext.get(),
                        FILE_PROVIDER_AUTHORITY, out);

                Intent mailIntent = new Intent(Intent.ACTION_SEND);
                mailIntent.setType("message/rfc822");
                mailIntent.putExtra(Intent.EXTRA_SUBJECT, SUBJECT);
                mailIntent.putExtra(Intent.EXTRA_STREAM, contentUri);

                try {
                    mContext.get().startActivity(Intent.createChooser(mailIntent, "Send email.."));
                } catch (android.content.ActivityNotFoundException ex) {
                    Toast.makeText(mContext.get(), "No email service", Toast.LENGTH_SHORT).show();
                }

                return out;

            } catch (Exception ex) {
               Timber.e(ex);
            }
        }

        return null;
    }

    @Override
    protected void onPostExecute(File file) {
        mCallback.onComplete(file);
    }
}
