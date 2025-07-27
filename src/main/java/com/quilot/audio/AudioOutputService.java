package com.quilot.audio;

import java.util.List;

public interface AudioOutputService {

    List<String> getAvailableOutputDevices();

    boolean selectOutputDevice(String deviceName);

    void setVolume(float volume);

    void playTestSound();

    void close();

    boolean isDeviceSelected();
}
