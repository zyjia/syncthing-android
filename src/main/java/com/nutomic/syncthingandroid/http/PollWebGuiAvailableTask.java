package com.nutomic.syncthingandroid.http;


import android.util.Log;

import javax.net.ssl.HttpsURLConnection;
import java.io.IOException;
import java.net.URL;

/**
 * Polls to load the web interface, until we receive http status 200.
 */
public abstract class PollWebGuiAvailableTask extends RestTask<Void, Void, Void> {

    private static final String TAG = "PollWebGuiAvailableTask";

    /**
     * Interval in ms, at which connections to the web gui are performed on first start
     * to find out if it's online.
     */
    private static final long WEB_GUI_POLL_INTERVAL = 100;

    public PollWebGuiAvailableTask(URL url, String httpsCertPath, String apiKey) {
        super(url, "", httpsCertPath, apiKey);
    }

    @Override
    protected Void doInBackground(Void... aVoid) {
        int status = 0;
        do {
            try {
                HttpsURLConnection connection = openConnection();
                connection.connect();
                status = connection.getResponseCode();
            } catch (IOException e) {
                // We catch this in every call, as long as the service is not online, so we ignore and continue.
                try {
                    Thread.sleep(WEB_GUI_POLL_INTERVAL);
                } catch (InterruptedException e2) {
                    Log.w(TAG, "Failed to sleep", e2);
                }
            }
        } while (status != HttpsURLConnection.HTTP_OK);
        return null;
    }

}
