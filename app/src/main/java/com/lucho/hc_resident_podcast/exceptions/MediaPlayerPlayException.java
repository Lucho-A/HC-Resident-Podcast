package com.lucho.hc_resident_podcast.exceptions;

public class MediaPlayerPlayException extends Throwable {
    private final String msg;
    public MediaPlayerPlayException(String msg) {
        this.msg=msg;
    }

    public String getMsg() {
        return msg;
    }
}
