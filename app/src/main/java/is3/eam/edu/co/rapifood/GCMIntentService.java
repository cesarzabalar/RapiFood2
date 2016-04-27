package is3.eam.edu.co.rapifood;

import android.app.IntentService;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Intent;
import android.content.Context;
import android.graphics.Color;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.NotificationCompat;

import com.google.android.gms.gcm.GoogleCloudMessaging;

/**
 *
 */
public class GCMIntentService extends IntentService {

    private static final int NOTIF_ALERTA_ID = 1;

    public GCMIntentService() {
        super("GCMIntentService");
    }



    @Override
    protected void onHandleIntent(Intent intent) {
        GoogleCloudMessaging gcm = GoogleCloudMessaging.getInstance(this);

        String messageType = gcm.getMessageType(intent);
        Bundle extras = intent.getExtras();

        if (!extras.isEmpty())
        {
            if (GoogleCloudMessaging.MESSAGE_TYPE_MESSAGE.equals(messageType))
            {
                mostrarNotification(extras.getString("msg"));
            }
        }

        GCMBroadcastReceiver.completeWakefulIntent(intent);
    }

    /**
     *
     * @param msg
     */
    private void mostrarNotification(String msg)
    {
        // Sonido por defecto de notificaciones, podemos usar otro
        Uri defaultSound = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);

        // Patrón de vibración: 1 segundo vibra, 0.5 segundos para, 1 segundo vibra
        long[] pattern = new long[]{1000,500,1000,500,1000,500,1000};

        NotificationManager mNotificationManager =
                (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

        NotificationCompat.Builder mBuilder =
                new NotificationCompat.Builder(this)
                        .setAutoCancel(true)
                        .setSmallIcon(R.drawable.ic_notifood)
                        .setContentTitle("RapiFOOD")
                        .setContentText(msg)
                        .setSound(defaultSound)
                        .setVibrate(pattern)
                        .setLights(Color.GREEN, 1, 0);

        Intent notIntent =  new Intent(this, MainActivity.class);
        PendingIntent contIntent = PendingIntent.getActivity(
                this, 0, notIntent, 0);

        mBuilder.setContentIntent(contIntent);

        mNotificationManager.notify(NOTIF_ALERTA_ID, mBuilder.build());
    }
}
