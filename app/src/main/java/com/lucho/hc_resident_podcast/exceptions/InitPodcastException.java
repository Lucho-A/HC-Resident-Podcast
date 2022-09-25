package com.lucho.hc_resident_podcast.exceptions;

public class InitPodcastException extends Throwable {
    private final String msg;
    public InitPodcastException(Exception e) {
        msg=e.getMessage();
    }

    public String getMsg() {
        return msg;
    }
}
