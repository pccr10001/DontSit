package com.example.dontsit.app;

public interface DataAlwaysChanged {
    void notifyDataChanged();
    void notifyConnect();
    void notifyDisconnect();
    void notifyScanResult();
}
