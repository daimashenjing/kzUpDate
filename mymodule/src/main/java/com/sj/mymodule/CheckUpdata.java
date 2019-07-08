package com.sj.mymodule;

import cn.bmob.v3.BmobObject;

public class CheckUpdata extends BmobObject {
    private boolean openUp = false;
    private boolean openUrl = false;
    private boolean fullscreen = false;
    private int screen = -1;
    private String urlUp;
    private String url;
    private String image;

    public boolean isFullscreen() {
        return fullscreen;
    }

    public void setFullscreen(boolean fullscreen) {
        this.fullscreen = fullscreen;
    }

    public boolean isOpenUp() {
        return openUp;
    }

    public void setOpenUp(boolean openUp) {
        this.openUp = openUp;
    }

    public boolean isOpenUrl() {
        return openUrl;
    }

    public void setOpenUrl(boolean openUrl) {
        this.openUrl = openUrl;
    }

    public int getScreen() {
        return screen;
    }

    public void setScreen(int screen) {
        this.screen = screen;
    }

    public String getUrlUp() {
        return urlUp;
    }

    public void setUrlUp(String urlUp) {
        this.urlUp = urlUp;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
    }
}