package com.example.tagtodo.locationService;


        import android.content.BroadcastReceiver;
        import android.content.Context;
        import android.content.Intent;
        import android.os.Vibrator;

        import androidx.core.app.NotificationManagerCompat;


public class DismissLoc extends BroadcastReceiver {

    private NotificationManagerCompat notificationManager;
    private Vibrator vibrator;

    @Override
    public void onReceive(Context context, Intent intent1) {

        MyBackgroundLocationService.mp.stop();
        vibrator = (Vibrator) context.getSystemService(Context.VIBRATOR_SERVICE);
        notificationManager = NotificationManagerCompat.from(context);
        int id = intent1.getIntExtra("Id", 2);
        notificationManager.cancel(id);
        vibrator.cancel();
    }
}