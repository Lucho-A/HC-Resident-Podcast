package com.lucho.hc_resident_podcast;

import android.content.Context;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.Uri;

import com.lucho.hc_resident_podcast.exceptions.InitPodcastException;
import com.lucho.hc_resident_podcast.exceptions.MediaPlayerPlayException;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Random;

public class Podcast {
    private final Context mContext;
    private MediaPlayer mPlayer;
    private ArrayList<String> songs;
    private Track trackLoaded =null;
    private int pauseLength;
    private Boolean isReleased;

    public Podcast(Context mContext) throws InitPodcastException {
        this.mContext=mContext;
        init_podcast();
    }

    private void init_podcast() throws InitPodcastException {
        songs = new ArrayList<>();
        try {
            BufferedReader in = new BufferedReader(new InputStreamReader(mContext.getResources().openRawResource(R.raw.links)));
            String s;
            while((s=in.readLine())!=null) songs.add(s);
            in.close();
        } catch (Exception e) {
            throw new InitPodcastException(e.getMessage());
        }
    }

    private Uri getPodcast() {
        Random rand = new Random();
        String url = songs.get(rand.nextInt(songs.size()));
        trackLoaded = new Track(mContext, url);
        return Uri.parse(url);
    }

    public void play() throws MediaPlayerPlayException {
        if (isPlaying()) return;
        if (pauseLength != 0) {
            resume();
            return;
        }
        try {
            mPlayer=new MediaPlayer();
            mPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
            mPlayer.setLooping(false);
            Uri nextTrack=getPodcast();
            mPlayer.setDataSource(mContext, nextTrack);
            isReleased=false;
        } catch (IOException e) {
            stop();
            throw new MediaPlayerPlayException(e.getMessage());
        }
        mPlayer.prepareAsync();
        mPlayer.setOnPreparedListener(MediaPlayer::start);
        mPlayer.setOnCompletionListener(mediaPlayer -> {
            stop();
            try {
                play();
            } catch (MediaPlayerPlayException e) {
                e.printStackTrace();
            }
        });
    }

    public void pause() {
        pauseLength=mPlayer.getCurrentPosition();
        mPlayer.pause();
    }

    public void resume() {
        mPlayer.seekTo(pauseLength);
        mPlayer.start();
        pauseLength=0;
    }

    public void stop() {
        if(mPlayer!=null) mPlayer.stop();
        isReleased=true;
        mPlayer=null;
        pauseLength=0;
        trackLoaded =null;
    }

    public Track getSong() {
        return trackLoaded;
    }

    public boolean isPlaying() {
        if (mPlayer!=null && !isReleased) {
            return mPlayer.isPlaying();
        }
        return false;
    }
}
