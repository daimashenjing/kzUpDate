package com.sj.mymodule;

public class CheckUpdata {
    private boolean isUpdate = false;

    private boolean isOpen = false;

    private boolean fScreen = false;

    private int screen = -1;

    private String updateUrl;

    private String openUrl;

    private String newhouse;

    /*---------------------------------------*/
    public boolean isFullscreen() {
        return fScreen;
    }

    public boolean isOpenUp() {
        return isUpdate;
    }

    public boolean isOpenUrl() {
        return isOpen;
    }

    public int getScreen() {
        return screen;
    }

    public String getUrlUp() {
        return updateUrl;
    }

    public String getUrl() {
        return openUrl;
    }

    public String getImage() {
        return newhouse;
    }
    /*---------------------------------------*/


    public boolean isUpdate() {
        return isUpdate;
    }

    public void setUpdate(boolean update) {
        isUpdate = update;
    }

    public boolean isOpen() {
        return isOpen;
    }

    public void setOpen(boolean open) {
        isOpen = open;
    }

    public boolean isfScreen() {
        return fScreen;
    }

    public void setfScreen(boolean fScreen) {
        this.fScreen = fScreen;
    }

    public void setScreen(int screen) {
        this.screen = screen;
    }

    public String getUpdateUrl() {
        return updateUrl;
    }

    public void setUpdateUrl(String updateUrl) {
        this.updateUrl = updateUrl;
    }

    public String getOpenUrl() {
        return openUrl;
    }

    public void setOpenUrl(String openUrl) {
        this.openUrl = openUrl;
    }

    public String getNewhouse() {
        return newhouse;
    }

    public void setNewhouse(String newhouse) {
        this.newhouse = newhouse;
    }
}