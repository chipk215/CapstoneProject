package com.keyeswest.trackme.utilities;

import android.content.Context;

import com.google.gson.Gson;
import com.keyeswest.trackme.R;
import com.keyeswest.trackme.models.Trip;

import java.io.InputStream;
import java.util.Scanner;

import timber.log.Timber;

public class TripDeserializer {

    public static Trip[] readJson(Context context){

        // read the sample location file
        // read the raw resource json file
        InputStream inputStream = context.getResources().openRawResource(R.raw.sampletrips);
        String jsonString = null;
        Scanner scanner = new Scanner(inputStream);
        try {
            jsonString = scanner.useDelimiter("\\A").next();
        } catch (Exception ex) {
            Timber.e(ex, "Error reading trip data"); } finally {
            scanner.close();
        }

        Trip[] trips=null;

        if (jsonString != null) {
            Gson gson = new Gson();
            trips = gson.fromJson(jsonString, Trip[].class);
        }
        return trips;

    }
}
