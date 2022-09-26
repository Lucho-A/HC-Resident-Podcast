package com.lucho.hc_resident_podcast.exceptions;

public class InitPodcastException extends Throwable {
    private final String msg;
    public InitPodcastException(String msg) {
        this.msg=msg;
    }

    public String getMsg() {
        return msg;
    }
}
