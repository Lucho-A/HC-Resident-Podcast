package com.lucho.hc_resident_podcast;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.MediaMetadataRetriever;
import android.util.Log;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.HashMap;

public class Track {
    private String trackTitle;
    private String trackAlbum;
    private Bitmap trackCover;
    private Boolean trackInfoUpdated;

    public Track(Context mContext, String url){
        setInfo(mContext, url);
    }

    public void setInfo(Context mContext, String url){
        trackTitle ="Retrieving track info...";
        trackAlbum ="";
        trackCover = BitmapFactory.decodeResource(mContext.getResources(), R.raw.portada);
        trackInfoUpdated =false;

        Thread searchInfo= new Thread(() -> {
            try {
                MediaMetadataRetriever mmr = new MediaMetadataRetriever();
                try {
                    mmr.setDataSource(url, new HashMap<>());
                }catch (Exception e){
                    trackInfoUpdated =true;
                    e.printStackTrace();
                }
                trackTitle = mmr.extractMetadata(MediaMetadataRetriever.METADATA_KEY_TITLE);
                if(trackTitle ==null) trackTitle ="Title not found";
                trackAlbum = mmr.extractMetadata(MediaMetadataRetriever.METADATA_KEY_ALBUM);
                if(trackAlbum ==null) trackAlbum ="Album not found";
                byte[] artBytes = mmr.getEmbeddedPicture();
                if (artBytes!=null) {
                    InputStream is = new ByteArrayInputStream(mmr.getEmbeddedPicture());
                    trackCover = BitmapFactory.decodeStream(is);
                } else {
                    trackCover = BitmapFactory.decodeResource(mContext.getResources(), R.raw.portada);
                }
            }catch (Exception e){
                Log.e("Error: ", e.getMessage());
            }
            trackInfoUpdated =true;
        });
        searchInfo.start();
    }

    public Boolean getTrackInfoUpdated() {
        return trackInfoUpdated;
    }

    public String getTrackTitle() {
        return trackTitle;
    }

    public String getTrackAlbum() {
        return trackAlbum;
    }

    public Bitmap getTrackCover() {
        return trackCover;
    }
}
