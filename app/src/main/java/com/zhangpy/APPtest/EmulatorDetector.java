package com.zhangpy.APPtest;

import static java.lang.System.getProperty;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorManager;

import java.util.List;

public class EmulatorDetector {
    private EmulatorCheckCallback emulatorCheckCallback;

    @Deprecated
    public boolean readSysProperty() {
        return readSysProperty(null, null);
    }

    public boolean readSysProperty(Context context, EmulatorCheckCallback callback) {
        this.emulatorCheckCallback = callback;
        int suspectCount = 0;

        String baseBandVersion = getProperty("gsm.version.baseband");
        if (null == baseBandVersion || baseBandVersion.contains("1.0.0.0"))
            ++suspectCount;//基带信息

        String buildFlavor = getProperty("ro.build.flavor");
        if (null == buildFlavor || buildFlavor.contains("vbox") || buildFlavor.contains("sdk_gphone"))
            ++suspectCount;//渠道

        String productBoard = getProperty("ro.product.board");
        if (null == productBoard || productBoard.contains("android") | productBoard.contains("goldfish"))
            ++suspectCount;//芯片

        String boardPlatform = getProperty("ro.board.platform");
        if (null == boardPlatform || boardPlatform.contains("android"))
            ++suspectCount;//芯片平台

        String hardWare = getProperty("ro.hardware");
        if (null == hardWare) ++suspectCount;
        else if (hardWare.toLowerCase().contains("ttvm")) suspectCount += 10;//天天
        else if (hardWare.toLowerCase().contains("nox")) suspectCount += 10;//夜神

        String cameraFlash = "";
        String sensorNum = "sensorNum";
        int sensorSize = 0;
        if (context != null) {
            boolean isSupportCameraFlash = context.getPackageManager().hasSystemFeature("android.hardware.camera.flash");//是否支持闪光灯
            if (!isSupportCameraFlash) ++suspectCount;
            cameraFlash = isSupportCameraFlash ? "support CameraFlash" : "unsupport CameraFlash";

            SensorManager sm = (SensorManager) context.getSystemService(Context.SENSOR_SERVICE);
            List<Sensor> sensorList = sm.getSensorList(Sensor.TYPE_ALL);
            sensorSize = sm.getSensorList(Sensor.TYPE_ALL).size();
            if (sensorSize < 20) ++suspectCount;//传感器个数
            sensorNum = String.valueOf(sensorSize);
        }

/*
        String userApps = CommandUtil.getSingleInstance().exec("pm list package -3");
        String userAppNum = "userAppNum";
        int userAppSize = getUserAppNums(userApps);
        if (userAppSize < 5) ++suspectCount;//用户安装的app个数
        userAppNum = userAppNum + userAppSize;

        String filter = CommandUtil.getSingleInstance().exec("cat /proc/self/cgroup");
        if (null == filter) ++suspectCount;//进程租
*/
        boolean isEmulator = sensorSize < 20;
        if (emulatorCheckCallback != null) {
            StringBuffer stringBuffer = new StringBuffer("start\n")
                    .append("baseBandVersion:").append(baseBandVersion).append("\n")
                    .append("buildFlavor:").append(buildFlavor).append("\n")
                    .append("productBoard:").append(productBoard).append("\n")
                    .append("boardPlatform:").append(boardPlatform).append("\n")
                    .append("hardWare:").append(hardWare).append("\n")
                    .append("cameraFlash:").append(cameraFlash).append("\n")
                    .append("sensorNum:").append(sensorNum);
            if (isEmulator) {
                emulatorCheckCallback.onEmulatorDetected(stringBuffer.toString());
            } else {
                emulatorCheckCallback.onRealDeviceDetected(stringBuffer.toString());
            }
            emulatorCheckCallback = null;
        }

        return isEmulator;
    }
}
