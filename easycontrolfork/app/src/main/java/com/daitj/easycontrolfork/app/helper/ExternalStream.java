package com.daitj.easycontrolfork.app.helper;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;

import java.util.Locale;
import java.util.UUID;

import com.daitj.easycontrolfork.app.entity.Device;

public final class ExternalStream {
  public static final String ACTION_CONNECT = "com.vbifonix.easycontrolfork.action.CONNECT";
  public static final String EXTRA_LAUNCH = "com.vbifonix.easycontrolfork.extra.EXTERNAL_LAUNCH";

  private ExternalStream() {
  }

  public static boolean isExternalLaunch(Intent intent) {
    return intent.getBooleanExtra(EXTRA_LAUNCH, false);
  }

  public static boolean shouldConnect(Intent intent) {
    return getBoolean(intent, "connect", true);
  }

  public static Device createDevice(Intent intent) {
    String address = getValue(intent, "address");
    String tcpip = getValue(intent, "tcpip");
    int adbPort = getInt(intent, "adb-port", 5555);
    if (tcpip != null && !tcpip.isEmpty()) {
      String[] connection = splitTcpip(tcpip);
      address = connection[0];
      if (connection[1] != null) adbPort = parseInt(connection[1], adbPort);
    }
    if (address == null || address.isEmpty()) return null;

    Device device = new Device(UUID.randomUUID().toString(), Device.TYPE_NETWORK);
    device.name = "----";
    device.closeAppOnClose = true;
    device.address = address;
    device.adbPort = adbPort;
    device.serverPort = getInt(intent, "server-port", device.serverPort);
    device.startApp = sanitizeStartApp(getString(intent, "start-app", device.startApp));
    device.listenClip = getBoolean(intent, "listen-clipboard", device.listenClip);
    device.isAudio = getBoolean(intent, "audio", device.isAudio);
    device.maxSize = getInt(intent, "max-size", device.maxSize);
    device.maxFps = getInt(intent, "max-fps", device.maxFps);
    device.maxVideoBit = getBitRate(intent, device.maxVideoBit);
    String codec = getValue(intent, "video-codec");
    if (codec != null) device.useH265 = "h265".equalsIgnoreCase(codec);

    device.wakeOnConnect = getBoolean(intent, "wake-on-connect", device.wakeOnConnect);
    device.lightOffOnConnect = getBoolean(intent, "turn-screen-off-on-connect", device.lightOffOnConnect);
    device.showNavBarOnConnect = getBoolean(intent, "show-nav-bar", device.showNavBarOnConnect);
    device.changeToFullOnConnect = getBoolean(intent, "start-fullscreen", device.changeToFullOnConnect);
    device.keepWakeOnRunning = getBoolean(intent, "stay-awake", device.keepWakeOnRunning);
    device.changeResolutionOnRunning = getBoolean(intent, "change-resolution-on-running", device.changeResolutionOnRunning);
    device.smallToMiniOnRunning = getBoolean(intent, "small-to-mini", device.smallToMiniOnRunning);
    device.fullToMiniOnRunning = getBoolean(intent, "full-to-mini", device.fullToMiniOnRunning);
    device.miniTimeoutOnRunning = getBoolean(intent, "mini-timeout", device.miniTimeoutOnRunning);
    device.lockOnClose = getBoolean(intent, "lock-on-close", device.lockOnClose);
    device.lightOnClose = getBoolean(intent, "turn-screen-on-on-close", device.lightOnClose);
    device.reconnectOnClose = getBoolean(intent, "reconnect-on-close", device.reconnectOnClose);

    device.customResolutionWidth = getInt(intent, "custom-resolution-width", device.customResolutionWidth);
    device.customResolutionHeight = getInt(intent, "custom-resolution-height", device.customResolutionHeight);
    String resolution = getValue(intent, "custom-resolution");
    if (resolution != null) {
      String[] size = resolution.toLowerCase(Locale.ROOT).split("x", 2);
      if (size.length == 2) {
        device.customResolutionWidth = parseInt(size[0], device.customResolutionWidth);
        device.customResolutionHeight = parseInt(size[1], device.customResolutionHeight);
        device.customResolutionOnConnect = true;
      }
    }
    device.customResolutionOnConnect = getBoolean(intent, "custom-resolution-on-connect", device.customResolutionOnConnect);

    device.smallX = getInt(intent, "small-x", device.smallX);
    device.smallY = getInt(intent, "small-y", device.smallY);
    device.smallLength = getInt(intent, "small-size", device.smallLength);
    device.smallXLan = getInt(intent, "small-x-landscape", device.smallXLan);
    device.smallYLan = getInt(intent, "small-y-landscape", device.smallYLan);
    device.smallLengthLan = getInt(intent, "small-size-landscape", device.smallLengthLan);
    device.miniY = getInt(intent, "mini-y", device.miniY);
    return device;
  }

  public static String createUri(Device device) {
    Uri.Builder builder = new Uri.Builder().scheme("easycontrol").authority("connect");
    append(builder, "address", device.address);
    append(builder, "adb-port", device.adbPort);
    append(builder, "server-port", device.serverPort);
    append(builder, "connect", true);
    append(builder, "start-app", device.startApp);
    append(builder, "listen-clipboard", device.listenClip);
    append(builder, "audio", device.isAudio);
    append(builder, "max-size", device.maxSize);
    append(builder, "max-fps", device.maxFps);
    append(builder, "video-bit-rate", device.maxVideoBit + "M");
    append(builder, "video-codec", device.useH265 ? "h265" : "h264");
    append(builder, "wake-on-connect", device.wakeOnConnect);
    append(builder, "turn-screen-off-on-connect", device.lightOffOnConnect);
    append(builder, "show-nav-bar", device.showNavBarOnConnect);
    append(builder, "start-fullscreen", device.changeToFullOnConnect);
    append(builder, "stay-awake", device.keepWakeOnRunning);
    append(builder, "custom-resolution-on-connect", device.customResolutionOnConnect);
    append(builder, "custom-resolution-width", device.customResolutionWidth);
    append(builder, "custom-resolution-height", device.customResolutionHeight);
    append(builder, "change-resolution-on-running", device.changeResolutionOnRunning);
    append(builder, "small-to-mini", device.smallToMiniOnRunning);
    append(builder, "full-to-mini", device.fullToMiniOnRunning);
    append(builder, "mini-timeout", device.miniTimeoutOnRunning);
    append(builder, "lock-on-close", device.lockOnClose);
    append(builder, "turn-screen-on-on-close", device.lightOnClose);
    append(builder, "reconnect-on-close", device.reconnectOnClose);
    append(builder, "small-x", device.smallX);
    append(builder, "small-y", device.smallY);
    append(builder, "small-size", device.smallLength);
    append(builder, "small-x-landscape", device.smallXLan);
    append(builder, "small-y-landscape", device.smallYLan);
    append(builder, "small-size-landscape", device.smallLengthLan);
    append(builder, "mini-y", device.miniY);
    return builder.build().toString();
  }

  public static String createActivityCommand(Device device) {
    StringBuilder command = new StringBuilder("am start -n com.vbifonix.easycontrolfork/.ExternalStreamActivity");
    append(command, "address", device.address);
    append(command, "adb-port", device.adbPort);
    append(command, "server-port", device.serverPort);
    append(command, "connect", true);
    append(command, "start-app", device.startApp);
    append(command, "listen-clipboard", device.listenClip);
    append(command, "audio", device.isAudio);
    append(command, "max-size", device.maxSize);
    append(command, "max-fps", device.maxFps);
    append(command, "video-bit-rate", device.maxVideoBit + "M");
    append(command, "video-codec", device.useH265 ? "h265" : "h264");
    append(command, "wake-on-connect", device.wakeOnConnect);
    append(command, "turn-screen-off-on-connect", device.lightOffOnConnect);
    append(command, "show-nav-bar", device.showNavBarOnConnect);
    append(command, "start-fullscreen", device.changeToFullOnConnect);
    append(command, "stay-awake", device.keepWakeOnRunning);
    append(command, "custom-resolution-on-connect", device.customResolutionOnConnect);
    append(command, "custom-resolution-width", device.customResolutionWidth);
    append(command, "custom-resolution-height", device.customResolutionHeight);
    append(command, "change-resolution-on-running", device.changeResolutionOnRunning);
    append(command, "small-to-mini", device.smallToMiniOnRunning);
    append(command, "full-to-mini", device.fullToMiniOnRunning);
    append(command, "mini-timeout", device.miniTimeoutOnRunning);
    append(command, "lock-on-close", device.lockOnClose);
    append(command, "turn-screen-on-on-close", device.lightOnClose);
    append(command, "reconnect-on-close", device.reconnectOnClose);
    append(command, "small-x", device.smallX);
    append(command, "small-y", device.smallY);
    append(command, "small-size", device.smallLength);
    append(command, "small-x-landscape", device.smallXLan);
    append(command, "small-y-landscape", device.smallYLan);
    append(command, "small-size-landscape", device.smallLengthLan);
    append(command, "mini-y", device.miniY);
    return command.toString();
  }

  private static void append(Uri.Builder builder, String key, Object value) {
    builder.appendQueryParameter(key, String.valueOf(value));
  }

  public static String sanitizeStartApp(String value) {
    if (value == null || value.isEmpty()) return "";
    return value.matches("[A-Za-z][A-Za-z0-9_]*(\\.[A-Za-z][A-Za-z0-9_]*)+") ? value : "";
  }

  private static void append(StringBuilder command, String key, String value) {
    command.append(" --es ").append(key).append(' ').append(shellQuote(value));
  }

  private static void append(StringBuilder command, String key, int value) {
    command.append(" --ei ").append(key).append(' ').append(value);
  }

  private static void append(StringBuilder command, String key, boolean value) {
    command.append(" --ez ").append(key).append(' ').append(value);
  }

  private static String shellQuote(String value) {
    return "'" + value.replace("'", "'\"'\"'") + "'";
  }

  private static String[] splitTcpip(String value) {
    if (value.startsWith("[")) {
      int end = value.indexOf(']');
      if (end > 0) return new String[]{value.substring(0, end + 1), end + 1 < value.length() && value.charAt(end + 1) == ':' ? value.substring(end + 2) : null};
    }
    int separator = value.lastIndexOf(':');
    return separator > 0 && separator == value.indexOf(':') ? new String[]{value.substring(0, separator), value.substring(separator + 1)} : new String[]{value, null};
  }

  private static int getBitRate(Intent intent, int defaultValue) {
    String value = getValue(intent, "video-bit-rate");
    if (value == null) value = getValue(intent, "max-video-bit");
    if (value == null) return defaultValue;
    value = value.toLowerCase(Locale.ROOT);
    try {
      if (value.endsWith("m")) return Math.max(1, Math.round(Float.parseFloat(value.substring(0, value.length() - 1))));
      int bitRate = Integer.parseInt(value);
      return bitRate >= 1000000 ? Math.max(1, bitRate / 1000000) : bitRate;
    } catch (NumberFormatException ignored) {
      return defaultValue;
    }
  }

  private static String getString(Intent intent, String key, String defaultValue) {
    String value = getValue(intent, key);
    return value == null ? defaultValue : value;
  }

  private static int getInt(Intent intent, String key, int defaultValue) {
    String value = getValue(intent, key);
    return value == null ? defaultValue : parseInt(value, defaultValue);
  }

  private static int parseInt(String value, int defaultValue) {
    try {
      return Integer.parseInt(value);
    } catch (NumberFormatException ignored) {
      return defaultValue;
    }
  }

  private static boolean getBoolean(Intent intent, String key, boolean defaultValue) {
    String value = getValue(intent, key);
    if (value == null) return defaultValue;
    return "true".equalsIgnoreCase(value) || "1".equals(value) || "yes".equalsIgnoreCase(value);
  }

  private static String getValue(Intent intent, String key) {
    Bundle extras = intent.getExtras();
    if (extras != null && extras.containsKey(key)) {
      Object value = extras.get(key);
      return value == null ? null : String.valueOf(value);
    }
    Uri data = intent.getData();
    return data == null ? null : data.getQueryParameter(key);
  }
}
