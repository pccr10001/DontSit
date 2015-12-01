package com.example.dontsit.app;

/**
 * Created by IDIC on 2015/12/1.
 */
public interface BLEConnectable {
    public void ScanResultThenDo();
    public void ConnectThenDo();
    public void NotifyThenDo();
}
