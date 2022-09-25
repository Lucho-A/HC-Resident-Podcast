package com.lucho.hc_resident_podcast;

import android.content.Context;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.Uri;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Random;

public class Podcast {
    private MediaPlayer mPlayer = new MediaPlayer();
    private ArrayList<String> songs;
    private Song songToPlay=null;
    private final Context mContext;
    private int pauseLength;

    public Podcast(Context mContext) {
        this.mContext=mContext;
        init_podcast();
    }

    private void init_podcast() {
        songs = new ArrayList<>();
        try {
            BufferedReader in = new BufferedReader(new InputStreamReader(mContext.getResources().openRawResource(R.raw.links)));
            String s;
            while((s=in.readLine())!=null) songs.add(s);
            in.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private Uri getPodcast() {
        Random rand = new Random();
        String url = songs.get(rand.nextInt(songs.size()));
        songToPlay = new Song(url);
        return Uri.parse(url);
    }

    public void play() {
        if(mPlayer!=null) {
            if (mPlayer.isPlaying()) return;
            if (pauseLength != 0) {
                resume();
                return;
            }
        }
        try {
            mPlayer=new MediaPlayer();
            mPlayer.setDataSource(mContext, getPodcast());
            mPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
            mPlayer.setLooping(false);
            mPlayer.prepare();
            mPlayer.start();
        } catch (IOException e) {
            e.printStackTrace();
        }
        mPlayer.setOnCompletionListener(mediaPlayer -> {
            mediaPlayer.reset();
            play();
        });
    }

    public void pause() {
        if(mPlayer!=null){
            mPlayer.pause();
            pauseLength=mPlayer.getCurrentPosition();
        }
    }

    public void resume() {
        if(mPlayer!=null) {
            mPlayer.seekTo(pauseLength);
            mPlayer.start();
            pauseLength=0;
        }
    }

    public void stop() {
        if(mPlayer!=null) {
            mPlayer.stop();
            mPlayer.release();
            mPlayer=null;
            pauseLength=0;
            songToPlay=null;
        }
    }

    public Song getSong() {
        return songToPlay;
    }

    public boolean isPlaying() {
        if(mPlayer!=null) return mPlayer.isPlaying();
        return false;
    }
}
