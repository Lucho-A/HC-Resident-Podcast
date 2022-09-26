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
import android.widget.Toast;

import androidx.core.app.NotificationCompat;

import com.lucho.hc_resident_podcast.exceptions.InitPodcastException;
import com.lucho.hc_resident_podcast.exceptions.MediaPlayerPlayException;

import java.util.Objects;
import java.util.Timer;
import java.util.TimerTask;

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
    private final Timer timer = new Timer();
    private Boolean trackInfoUpdated=false;

    public IBinder onBind(Intent intent) {
        return binder;
    }

    public static class PodcastServiceBinder extends Binder {}

    @SuppressLint("UnspecifiedImmutableFlag")
    public void onCreate() {
        Intent intentPlay = new Intent(this, NotificationReceiver.class);
        Intent intentNext = new Intent(this, NotificationReceiver.class);
        Intent intentPause = new Intent(this, NotificationReceiver.class);
        Intent intentStop = new Intent(this, NotificationReceiver.class);
        Intent intentExit = new Intent(this, NotificationReceiver.class);
        try {
            podcast = new Podcast(this.getApplicationContext());
        } catch (InitPodcastException e) {
            Toast.makeText(podcastService, e.getMsg(), Toast.LENGTH_LONG).show();
            exit();
        }
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
        actualizar_notification("Ready for podcasting...", "","STOP");
        Toast.makeText(getApplicationContext(), "App started OK. Running in notification area.", Toast.LENGTH_LONG).show();
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                if(podcast.getSong()!=null && !trackInfoUpdated && podcast.getSong().getInfoUpdated() && podcast.isPlaying()){
                    trackInfoUpdated=true;
                    actualizar_notification("", "", "PLAY");
                }
            }
        },0,1000);
    }

    public void play(){
        try {
            podcast.play();
        } catch (MediaPlayerPlayException e) {
            Toast.makeText(podcastService, e.getMsg(), Toast.LENGTH_LONG).show();
            exit();
        }
        trackInfoUpdated=false;
        actualizar_notification("", "", "PLAY");
    }

    private void next() {
        stop();
        play();
    }

    public void stop(){
        podcast.stop();
        actualizar_notification("Ready for podcasting...", "","STOP");
    }

    public void pause(){
        if(podcast.isPlaying()) {
            podcast.pause();
            actualizar_notification("", "","PAUSE");
        }
    }

    public int onStartCommand(Intent intent, int flags, int startId) {
        super.onStartCommand(intent, flags, startId);
        return START_NOT_STICKY;
    }

    public void stopService() {
        stopForeground(true);
    }

    private void exit() {
        stopService();
        android.os.Process.killProcess(android.os.Process.myPid());
    }

    private Notification crear_notification() {
        Bitmap largeIcon = BitmapFactory.decodeResource(getResources(), R.raw.portada);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(CHANNEL_ID, "Service Notification", NotificationManager.IMPORTANCE_LOW);
            notificationManager = getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(channel);
            builder = new NotificationCompat.Builder(getApplicationContext(), CHANNEL_ID)
                    .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
                    .setSmallIcon(R.drawable.headphone_48)
                    .setLargeIcon(largeIcon)
                    .setStyle(new androidx.media.app.NotificationCompat.MediaStyle());
        }
        return builder.build();
    }

    public void actualizar_notification(String title, String body, String type) {
        builder.clearActions();
        Bitmap largeIcon=null;
        switch (type){
            case "PLAY":
            case "PAUSE":
                builder.setContentTitle(podcast.getSong().getSongTitle());
                builder.setContentText(podcast.getSong().getAlbum());
                largeIcon=podcast.getSong().getCover();
                builder.setLargeIcon(largeIcon);
                if(type.equals("PLAY")){
                    builder.addAction(R.drawable.pause_20, "Pause", pendingIntentPause);
                }else{
                    builder.setContentTitle(podcast.getSong().getSongTitle() + " (paused)");
                    builder.addAction(R.drawable.play_20, "Play", pendingIntentPlay);
                }
                builder.addAction(R.drawable.next_20, "Next", pendingIntentNext);
                builder.addAction(R.drawable.stop_20, "Stop", pendingIntentStop);
                break;
            case "INIT":
            case "STOP":
                if(type.equals("STOP")) builder.addAction(R.drawable.play_20, "Play", pendingIntentPlay);
                builder.setContentTitle(title);
                builder.setContentText(body);
                largeIcon = BitmapFactory.decodeResource(getResources(), R.raw.portada);
                builder.setLargeIcon(largeIcon);
                break;
        }
        if(!trackInfoUpdated) {
            largeIcon = BitmapFactory.decodeResource(getResources(), R.raw.portada);
            builder.setLargeIcon(largeIcon);
        }
        if(largeIcon!=null) {
            int pixel = largeIcon.getPixel(largeIcon.getWidth() - 5, largeIcon.getHeight() - 5);
            builder.setColor(Color.argb(125, Color.red(pixel), Color.green(pixel), Color.blue(pixel)));
            builder.setColorized(true);
        }
        builder.addAction(R.drawable.eject_20, "Exit", pendingIntentExit);
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
                podcastService.exit();
            }
        }
    }
}