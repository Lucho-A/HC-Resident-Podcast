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
    private final MediaPlayer mPlayer = new MediaPlayer();
    private ArrayList<String> songs;
    private static Song songToPlay=null;
    private int pauseLength;

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
            throw new InitPodcastException(e);
        }
    }

    private Uri getPodcast() {
        Random rand = new Random();
        String url = songs.get(rand.nextInt(songs.size()));
        songToPlay = new Song(url);
        return Uri.parse(url);
    }

    public void play() throws MediaPlayerPlayException {
        if (mPlayer.isPlaying()) return;
        if (pauseLength != 0) {
            resume();
            return;
        }
        try {
            mPlayer.setDataSource(mContext, getPodcast());
            mPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
            mPlayer.setLooping(false);
            mPlayer.prepareAsync();
            mPlayer.setOnPreparedListener(MediaPlayer::start);
        } catch (IOException e) {
            throw new MediaPlayerPlayException(e);
        }
        mPlayer.setOnCompletionListener(mediaPlayer -> {
            mediaPlayer.reset();
            try {
                play();
            } catch (MediaPlayerPlayException e) {
                e.printStackTrace();
            }
        });
    }

    public void pause() {
        mPlayer.pause();
        pauseLength=mPlayer.getCurrentPosition();
    }

    public void resume() {
        mPlayer.seekTo(pauseLength);
        mPlayer.start();
        pauseLength=0;
    }

    public void stop() {
        mPlayer.stop();
        mPlayer.reset();
        pauseLength=0;
        songToPlay=null;
    }

    public Song getSong() {
        return songToPlay;
    }

    public boolean isPlaying() {
        return mPlayer.isPlaying();
    }
}
