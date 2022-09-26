package com.lucho.hc_resident_podcast;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.MediaMetadataRetriever;
import android.util.Log;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.HashMap;

public class Song {
    private String songTitle;
    private String album;
    private Bitmap cover;
    private Boolean infoUpdated;

    public Song(String url){
        setInfo(url);
    }

    public void setInfo(String url){
        songTitle="Trying to retrieve track info...";
        infoUpdated=false;
        Thread searchInfo= new Thread(() -> {
            try {
                MediaMetadataRetriever mmr = new MediaMetadataRetriever();
                mmr.setDataSource(url, new HashMap<>());
                songTitle = mmr.extractMetadata(MediaMetadataRetriever.METADATA_KEY_TITLE);
                album = mmr.extractMetadata(MediaMetadataRetriever.METADATA_KEY_ALBUM);
                byte[] artBytes = mmr.getEmbeddedPicture();
                if (artBytes!=null) {
                    InputStream is = new ByteArrayInputStream(mmr.getEmbeddedPicture());
                    cover = BitmapFactory.decodeStream(is);
                } else {
                    setCover(String.valueOf(R.raw.portada));
                }
            }catch (Exception e){
                Log.e("Error: ", e.getMessage());
            }
            infoUpdated=true;
        });
        searchInfo.start();
    }

    public Boolean getInfoUpdated() {
        return infoUpdated;
    }

    public String getSongTitle() {
        return songTitle;
    }

    public String getAlbum() {
        return album;
    }

    public Bitmap getCover() {
        return cover;
    }

    public void setCover(String bm) {
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inPreferredConfig = Bitmap.Config.ARGB_8888;
        cover = BitmapFactory.decodeFile(bm, options);
    }
}
