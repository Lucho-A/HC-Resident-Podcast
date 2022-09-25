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
    private Bitmap art;

    public Song(String url){
        setInfo(url);
    }

    public void setInfo(String url){
        try {
            MediaMetadataRetriever mmr = new MediaMetadataRetriever();
            mmr.setDataSource(url, new HashMap<>());
            songTitle = mmr.extractMetadata(MediaMetadataRetriever.METADATA_KEY_TITLE);
            album = mmr.extractMetadata(MediaMetadataRetriever.METADATA_KEY_ALBUM);
            byte[] artBytes = mmr.getEmbeddedPicture();
            if (artBytes != null) {
                InputStream is = new ByteArrayInputStream(mmr.getEmbeddedPicture());
                art = BitmapFactory.decodeStream(is);
            } else {
                setArt(String.valueOf(R.raw.portada));
            }
        }catch (Exception e){
            Log.e("Error: ", e.getMessage());
        }
    }

    public String getSongTitle() {
        return songTitle;
    }

    public String getAlbum() {
        return album;
    }

    public Bitmap getArt() {
        return art;
    }

    public void setArt(String bm) {
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inPreferredConfig = Bitmap.Config.ARGB_8888;
        art= BitmapFactory.decodeFile(bm, options);
    }
}
