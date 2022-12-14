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
import android.support.v4.media.session.MediaSessionCompat;
import android.view.KeyEvent;
import android.widget.Toast;

import androidx.core.app.NotificationCompat;

import com.lucho.hc_resident_podcast.exceptions.InitPodcastException;
import com.lucho.hc_resident_podcast.exceptions.MediaPlayerPlayException;

import java.sql.SQLException;
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
    private Track trackPlaying =null;
    private static boolean isUpdated=true;

    public IBinder onBind(Intent intent) {
        return binder;
    }

    public static class PodcastServiceBinder extends Binder {}

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
        intentNext.setAction("NEXT");
        intentPause.setAction("PAUSE");
        intentStop.setAction("STOP");
        intentExit.setAction("EXIT");
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            pendingIntentPlay = PendingIntent.getBroadcast(this, 0, intentPlay, PendingIntent.FLAG_IMMUTABLE);
            pendingIntentNext = PendingIntent.getBroadcast(this, 0, intentNext, PendingIntent.FLAG_IMMUTABLE);
            pendingIntentPause = PendingIntent.getBroadcast(this, 0, intentPause, PendingIntent.FLAG_IMMUTABLE);
            pendingIntentStop = PendingIntent.getBroadcast(this, 0, intentStop, PendingIntent.FLAG_IMMUTABLE);
            pendingIntentExit = PendingIntent.getBroadcast(this, 0, intentExit, PendingIntent.FLAG_IMMUTABLE);
        }
        startForeground(NOTIFICATION_ID, crear_notification());
        actualizar_notification("Ready for podcasting...", "", "STOP");
        Toast.makeText(getApplicationContext(), "App started OK. Running in notification area.", Toast.LENGTH_LONG).show();
        Timer askForLooping = new Timer();
        askForLooping.schedule(new TimerTask() {
            @Override
            public void run() {
                if (podcast.getReadyForLooping()) play();
            }
        }, 0, 1000);
        configureMediaSession();
        try {
            MySQL_HOME mySQL_home = new MySQL_HOME();
            isUpdated = mySQL_home.isUpdated();
        } catch (SQLException | ClassNotFoundException e) {
            e.printStackTrace();
        }
        if (!isUpdated) Toast.makeText(getApplicationContext(), "App out-of-date. ", Toast.LENGTH_LONG).show();
    }

    public void play(){
        actualizar_notification("Fetching next song...", "","INIT");
        try {
            trackPlaying =null;
            podcast.play();
        } catch (MediaPlayerPlayException e) {
            Toast.makeText(podcastService, e.getMsg(), Toast.LENGTH_LONG).show();
        }
        trackPlaying =podcast.getTrackLoaded();
        actualizar_notification("", "", "PLAY");
        Timer timerFetchingTrackInfo = new Timer();
        timerFetchingTrackInfo.schedule(new TimerTask() {
            @Override
            public void run() {
                if (trackPlaying != null && trackPlaying.getTrackInfoUpdated() && podcast.isPlaying()) {
                    actualizar_notification("", "", "PLAY");
                    timerFetchingTrackInfo.cancel();
                }
            }
        },0,1000);
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
        Bitmap cover=null;
        switch (type){
            case "PLAY":
            case "PAUSE":
                builder.setContentTitle(trackPlaying.getTrackTitle());
                builder.setContentText(trackPlaying.getTrackAlbum());
                cover= trackPlaying.getTrackCover();
                if(type.equals("PLAY")){
                    builder.addAction(R.drawable.pause_20, "Pause", pendingIntentPause);
                }else{
                    builder.setContentTitle(podcast.getTrackLoaded().getTrackTitle() + " (paused)");
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
                cover = BitmapFactory.decodeResource(getResources(), R.raw.portada);
                break;
        }
        builder.setLargeIcon(cover);
        if(cover!=null) {
            int pixel = cover.getPixel(cover.getWidth()-25, cover.getHeight()/2);
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

    private void configureMediaSession() {
        MediaSessionCompat mediaSession = new MediaSessionCompat(this, "MyMediaSession");
        mediaSession.setCallback(new MediaSessionCompat.Callback() {
            @Override
            public boolean onMediaButtonEvent(Intent mediaButtonIntent) {
                KeyEvent ke = mediaButtonIntent.getParcelableExtra(Intent.EXTRA_KEY_EVENT);
                if (ke != null && ke.getAction() == KeyEvent.ACTION_DOWN && ke.getKeyCode()==KeyEvent.KEYCODE_MEDIA_NEXT) next();
                if (ke != null && ke.getAction() == KeyEvent.ACTION_DOWN && ke.getKeyCode()==KeyEvent.KEYCODE_MEDIA_PLAY) play();
                if (ke != null && ke.getAction() == KeyEvent.ACTION_DOWN && ke.getKeyCode()==KeyEvent.KEYCODE_MEDIA_PAUSE) pause();
                return super.onMediaButtonEvent(mediaButtonIntent);
            }
        });
        mediaSession.setFlags(MediaSessionCompat.FLAG_HANDLES_MEDIA_BUTTONS | MediaSessionCompat.FLAG_HANDLES_TRANSPORT_CONTROLS);
        mediaSession.setActive(true);
    }
}