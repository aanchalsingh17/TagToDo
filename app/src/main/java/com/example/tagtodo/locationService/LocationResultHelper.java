package com.example.tagtodo.locationService;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.media.MediaPlayer;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Vibrator;
import android.preference.PreferenceManager;

import java.io.File;
import java.io.IOException;
import java.text.DateFormat;
import java.util.Date;
import java.util.List;

import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.core.app.TaskStackBuilder;

import com.example.tagtodo.MainActivity;
import com.example.tagtodo.R;
import com.example.tagtodo.model.Note;
import com.example.tagtodo.note.AlarmReceiver;
import com.example.tagtodo.note.Dismiss;
import com.example.tagtodo.note.NoteDetails;

public class LocationResultHelper {

    public static final String KEY_LOCATION_RESULTS = "key-location-results";
    private Context mContext;
    private List<Location> mLocationList;
    int Notification_Id = 2;




    public LocationResultHelper(Context mContext, List<Location> mLocationList) {
        this.mContext = mContext;
        this.mLocationList = mLocationList;
    }

    public String getLocationResultText() {

        if (mLocationList.isEmpty()) {
            return "Location not received";
        } else {
            StringBuilder sb = new StringBuilder();
            for (Location location : mLocationList) {
                sb.append("(");
                sb.append(location.getLatitude());
                sb.append(", ");
                sb.append(location.getLongitude());
                sb.append(")");
                sb.append("\n");
            }
            return sb.toString();

        }

    }

    private CharSequence getLocationResultTitle() {

        String result = mContext.getResources().getQuantityString
                (R.plurals.num_locations_reported, mLocationList.size(), mLocationList.size());

        return result + " : " + DateFormat.getDateTimeInstance().format(new Date());
    }

    public void showNotification(String key, Note note, String imageUri) {

        Intent notificationIntent = new Intent(mContext, NoteDetails.class);
        notificationIntent.putExtra("title", note.getTitle());          //Send title data(with key title) from adapter to NoteDetails when note number-> position is clicked
        notificationIntent.putExtra("content", note.getContent());      //Send content data(with key content) from adapter to NoteDetails when note number-> position is clicked
        notificationIntent.putExtra("location",note.getLocation());     //Send location data
        notificationIntent.putExtra("latitude",note.getLatitude());     //Send latitude
        notificationIntent.putExtra("longitude",note.getLongitude());   //Send longitude
        notificationIntent.putExtra("imageUri",imageUri);                //Image uri
        // Construct a task stack.
        TaskStackBuilder stackBuilder = TaskStackBuilder.create(mContext);

        // Add the main Activity to the task stack as the parent.
        stackBuilder.addParentStack(MainActivity.class);

        // Push the content Intent onto the stack.
        stackBuilder.addNextIntent(notificationIntent);

        // Get a PendingIntent containing the entire back stack.
        PendingIntent notificationPendingIntent =
                stackBuilder.getPendingIntent(0, PendingIntent.FLAG_UPDATE_CURRENT);

        Vibrator vibrator = (Vibrator) mContext.getSystemService(Context.VIBRATOR_SERVICE);
        vibrator.vibrate(new long[] { 500,1000 },0);

        MediaPlayer mp = MyBackgroundLocationService.mp;
        mp.start();


        Intent in = new Intent(mContext, DismissLoc.class);
        in.putExtra("Id",Notification_Id);
        PendingIntent Done  = PendingIntent.getBroadcast(mContext,0,in,PendingIntent.FLAG_UPDATE_CURRENT);

        NotificationCompat.Builder notificationBuilder = null;
        notificationBuilder = new NotificationCompat.Builder(mContext,
                App.CHANNEL_ID)
                .setContentTitle(note.getTitle())
                .setSmallIcon(R.drawable.ic_my_location_white_24dp)
                .setAutoCancel(true)
//                .setOngoing(true)
                .addAction(R.drawable.ic_done_all_black_24dp,"Done",Done)
                .setStyle(new NotificationCompat.BigTextStyle().bigText(note.getContent()))
                .setContentIntent(notificationPendingIntent);

        getNotificationManager().notify(0, notificationBuilder.build());

    }

    private NotificationManager getNotificationManager() {

        NotificationManager manager = (NotificationManager) mContext.getSystemService(Context.NOTIFICATION_SERVICE);
        return manager;

    }

    public void saveLocationResults() {
        PreferenceManager.getDefaultSharedPreferences(mContext)
                .edit()
                .putString(KEY_LOCATION_RESULTS, getLocationResultTitle() + "\n" +
                        getLocationResultText())
                .apply();
    }

    public static String getSavedLocationResults(Context context) {

        return PreferenceManager.getDefaultSharedPreferences(context)
                .getString(KEY_LOCATION_RESULTS, "default value");

    }

}
