package com.lucho.hc_resident_podcast.exceptions;

public class MediaPlayerPlayException extends Throwable {
    private final String msg;
    public MediaPlayerPlayException(Exception e) {
        msg=e.getMessage();
    }

    public String getMsg() {
        return msg;
    }
}
