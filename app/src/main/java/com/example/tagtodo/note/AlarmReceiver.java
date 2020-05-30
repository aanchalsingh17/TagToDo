package com.example.tagtodo.note;

        import android.app.Notification;
        import android.app.PendingIntent;
        import android.content.BroadcastReceiver;
        import android.content.Context;
        import android.content.Intent;
        import android.graphics.Color;
        import android.location.LocationManager;
        import android.media.MediaPlayer;
        import android.media.RingtoneManager;
        import android.net.Uri;
        import android.os.Vibrator;
        import android.widget.Toast;

        import androidx.core.app.NotificationCompat;
        import androidx.core.app.NotificationManagerCompat;

        ;import com.example.tagtodo.R;

public class AlarmReceiver extends  BroadcastReceiver{
    private static final String CHANNEL_ID = "Channel";
    private NotificationManagerCompat notificationManager;
    LocationManager l ;
    int Notification_Id = 1;
    public static MediaPlayer mp;


    @Override
    public void onReceive(Context context, Intent intent) {
        mp=MediaPlayer.create(context, R.raw.alert);
        String title = intent.getStringExtra("Title");
        String content = intent.getStringExtra("Content");
        Toast.makeText(context, "In Alarm", Toast.LENGTH_SHORT).show();
         notificationManager = NotificationManagerCompat.from(context);
        Intent activityintent = new Intent(context,AddNote.class);
        PendingIntent contentintent = PendingIntent.getActivity(context,0,activityintent,PendingIntent.FLAG_UPDATE_CURRENT);
        Intent in = new Intent(context,Dismiss.class);
        in.putExtra("Id",Notification_Id);

        Vibrator vibrator = (Vibrator) context.getSystemService(Context.VIBRATOR_SERVICE);
        vibrator.vibrate(new long[] { 500,1000 },0);
        if(mp.isPlaying()) {
            mp.pause();
        }
        mp.start();


        PendingIntent Done  = PendingIntent.getBroadcast(context,0,in,PendingIntent.FLAG_UPDATE_CURRENT);
        Notification notification = new NotificationCompat.Builder(context,CHANNEL_ID)
                //Later use app icon as icon
                .setSmallIcon(R.drawable.ic_account)
                .setContentTitle(title)
                .setStyle(new NotificationCompat.BigTextStyle().bigText(content))
                .setColor(Color.RED)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setCategory(NotificationCompat.CATEGORY_REMINDER)
                .setOngoing(true)
                .setTimeoutAfter(5*60*60*1000)
                .setLights(255,6,7)
                .setContentIntent(contentintent)
//                        .setOnlyAlertOnce(true)
                .addAction(R.drawable.ic_done_all_black_24dp,"Done",Done)
//                        .setAutoCancel(true)
                .build();

        notificationManager.notify(Notification_Id,notification);
        Toast.makeText(context, "Complete your task!!!",
                Toast.LENGTH_LONG).show();
    }
}
