package com.lucho.hc_resident_podcast;

import android.annotation.SuppressLint;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.os.Binder;
import android.os.Build;
import android.os.IBinder;

import androidx.core.app.NotificationCompat;

import java.util.Objects;

public class PodcastService extends Service {
    private static final String CHANNEL_ID = "Channel_PodcastService";
    private static final int NOTIFICATION_ID = 12345678;
    private NotificationCompat.Builder builder;
    private NotificationManager notificationManager;
    private PendingIntent pendingIntentPlay;
    private PendingIntent pendingIntentNext;
    private PendingIntent pendingIntentPause;
    private PendingIntent pendingIntentStop;
    private PendingIntent pendingIntentExit;
    @SuppressLint("StaticFieldLeak")
    private static Podcast podcast;
    private static PodcastService podcastService;
    private final PodcastServiceBinder binder = new PodcastServiceBinder();

    public IBinder onBind(Intent intent) {
        return binder;
    }

    public static class PodcastServiceBinder extends Binder {}

    public void play(){
        actualizar_notification("Initializing podcasting...", "", "PLAY");
        podcast.play();
        actualizar_notification("Podcasting...", podcast.getSongPlaying(), "PLAY");
    }

    private void next() {
        stop();
        play();
    }

    public void stop(){
        podcast.stop();
        actualizar_notification("Podcasting stopped...", "","STOP");
    }

    public void pause(){
        if(podcast.isPlaying()) {
            podcast.pause();
            actualizar_notification("Podecasting paused...", podcast.getSongPlaying(),"PAUSE");
        }else {
            podcast.resume();
            actualizar_notification("Podecasting...", podcast.getSongPlaying(),"PLAY");
        }
    }

    @SuppressLint("UnspecifiedImmutableFlag")
    public void onCreate() {
        Intent intentPlay = new Intent(this, NotificationReceiver.class);
        Intent intentNext = new Intent(this, NotificationReceiver.class);
        Intent intentPause = new Intent(this, NotificationReceiver.class);
        Intent intentStop = new Intent(this, NotificationReceiver.class);
        Intent intentExit = new Intent(this, NotificationReceiver.class);
        podcast = new Podcast(this.getApplicationContext());
        podcastService = this;
        intentPlay.setAction("PLAY");
        pendingIntentPlay = PendingIntent.getBroadcast(this, 0, intentPlay, PendingIntent.FLAG_UPDATE_CURRENT);
        intentNext.setAction("NEXT");
        pendingIntentNext = PendingIntent.getBroadcast(this, 0, intentNext, PendingIntent.FLAG_UPDATE_CURRENT);
        intentPause.setAction("PAUSE");
        pendingIntentPause = PendingIntent.getBroadcast(this, 0, intentPause, PendingIntent.FLAG_UPDATE_CURRENT);
        intentStop.setAction("STOP");
        pendingIntentStop = PendingIntent.getBroadcast(this, 0, intentStop, PendingIntent.FLAG_UPDATE_CURRENT);
        intentExit.setAction("EXIT");
        pendingIntentExit = PendingIntent.getBroadcast(this, 0, intentExit, PendingIntent.FLAG_UPDATE_CURRENT);
        startForeground(NOTIFICATION_ID, crear_notification());
        actualizar_notification("Ready for podcasting...", "","");
    }

    public int onStartCommand(Intent intent, int flags, int startId) {
        super.onStartCommand(intent, flags, startId);
        return START_NOT_STICKY;
    }

    public void stopService() {
        stopForeground(true);
        onDestroy();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    private void exit() {
        android.os.Process.killProcess(android.os.Process.myPid());
    }

    private Notification crear_notification() {
        Bitmap largeIcon = BitmapFactory.decodeResource(getResources(), R.raw.portada9);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(CHANNEL_ID, "Service Notification", NotificationManager.IMPORTANCE_LOW);
            notificationManager = getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(channel);
            builder = new NotificationCompat.Builder(getApplicationContext(), CHANNEL_ID)
                    .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
                    .setSmallIcon(R.drawable.headphone_48)
                    .setLargeIcon(largeIcon)
                    //.setColor(Color.argb(50,200,0,0))
                    .setColor(Color.BLACK)
                    .setStyle(new androidx.media.app.NotificationCompat.MediaStyle())
                    .addAction(R.drawable.play_24, "Play", pendingIntentPlay)
                    .addAction(R.drawable.eject_24, "Exit", pendingIntentExit);
        }
        return builder.build();
    }

    public void actualizar_notification(String title, String body, String type) {
        builder.clearActions();
        builder.setContentTitle(title);
        builder.setContentText(body);
        if(type.equals("PLAY")){
            builder.addAction(R.drawable.pause_24, "Pause", pendingIntentPause);
            builder.addAction(R.drawable.next_24, "Next", pendingIntentNext);
            builder.addAction(R.drawable.stop_24, "Stop", pendingIntentStop);
        }else if(type.equals("PAUSE")) {
            builder.addAction(R.drawable.play_24, "Play", pendingIntentPlay);
            builder.addAction(R.drawable.next_24, "Next", pendingIntentNext);
            builder.addAction(R.drawable.stop_24, "Stop", pendingIntentStop);
        }else{
            builder.addAction(R.drawable.play_24, "Play", pendingIntentPlay);
        }
        builder.addAction(R.drawable.eject_24, "Exit", pendingIntentExit);
        notificationManager.notify(NOTIFICATION_ID, builder.build());
    }

    public static class NotificationReceiver extends BroadcastReceiver {
        public void onReceive(Context context, Intent intent) {
            if (Objects.equals(intent.getAction(), "PLAY")){
                podcastService.play();
            } else if (Objects.equals(intent.getAction(),"NEXT")){
                podcastService.next();
            } else if (Objects.equals(intent.getAction(),"PAUSE")){
                podcastService.pause();
            } else if (Objects.equals(intent.getAction(),"STOP")){
                podcastService.stop();
            } else if (Objects.equals(intent.getAction(),"EXIT")){
                podcastService.stop();
                podcastService.stopService();
                podcastService.exit();
            }
        }
    }
}