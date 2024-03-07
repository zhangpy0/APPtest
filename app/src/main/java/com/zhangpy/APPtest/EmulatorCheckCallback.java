package com.zhangpy.APPtest;

public interface EmulatorCheckCallback {
    //当检测到模拟器时调用
    void onEmulatorDetected(String stringBuffer);
    //当检测到真机时调用
    void onRealDeviceDetected(String result);
}
