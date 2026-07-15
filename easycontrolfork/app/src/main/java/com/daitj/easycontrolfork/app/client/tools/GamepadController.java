package com.daitj.easycontrolfork.app.client.tools;

import android.view.InputDevice;
import android.view.KeyEvent;
import android.view.MotionEvent;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.HashMap;

public final class GamepadController {
  private static final int XBOX_VENDOR_ID = 0x045e;
  private static final int XBOX_PRODUCT_ID = 0x028e;
  private static final String XBOX_NAME = "Microsoft X-Box 360 Pad";
  private static final byte[] REPORT_DESCRIPTOR = {
    0x05, 0x01, 0x09, 0x05, (byte) 0xA1, 0x01, (byte) 0xA1, 0x00, 0x05, 0x01, 0x09, 0x30, 0x09, 0x31, 0x09, 0x33, 0x09, 0x34,
    0x15, 0x00, 0x27, (byte) 0xFF, (byte) 0xFF, 0x00, 0x00, 0x75, 0x10, (byte) 0x95, 0x04, (byte) 0x81, 0x02, 0x05, 0x01, 0x09, 0x32,
    0x09, 0x35, 0x15, 0x00, 0x26, (byte) 0xFF, 0x7F, 0x75, 0x10, (byte) 0x95, 0x02, (byte) 0x81, 0x02, 0x05, 0x09, 0x19, 0x01,
    0x29, 0x10, 0x15, 0x00, 0x25, 0x01, (byte) 0x95, 0x10, 0x75, 0x01, (byte) 0x81, 0x02, 0x05, 0x01, 0x09, 0x39, 0x15, 0x01,
    0x25, 0x08, 0x75, 0x04, (byte) 0x95, 0x01, (byte) 0x81, 0x42, (byte) 0xC0, (byte) 0xC0
  };

  private final HashMap<Integer, GamepadState> gamepads = new HashMap<>();
  private final PacketWriter packetWriter;

  public GamepadController(PacketWriter packetWriter) {
    this.packetWriter = packetWriter;
  }

  public boolean handleKeyEvent(KeyEvent event) {
    if (!isGamepad(event.getSource()) || (event.getAction() != KeyEvent.ACTION_DOWN && event.getAction() != KeyEvent.ACTION_UP)) return false;
    GamepadState state = getState(event.getDeviceId());
    if (state == null) return true;
    // These controllers emit L2/R2 key events at the analog press threshold.
    if (event.getKeyCode() == KeyEvent.KEYCODE_BUTTON_L2 || event.getKeyCode() == KeyEvent.KEYCODE_BUTTON_R2) return true;
    int bit = getButtonBit(event.getKeyCode());
    if (bit == 0) return true;
    if (event.getAction() == KeyEvent.ACTION_DOWN) state.buttons |= bit;
    else state.buttons &= ~bit;
    packetWriter.write(ControlPacket.createUhidInputEvent(state.id, state.createReport()));
    return true;
  }

  public boolean handleMotionEvent(MotionEvent event) {
    if (!isGamepad(event.getSource())) return false;
    GamepadState state = getState(event.getDeviceId());
    if (state == null) return true;
    state.leftX = event.getAxisValue(MotionEvent.AXIS_X);
    state.leftY = event.getAxisValue(MotionEvent.AXIS_Y);
    state.rightX = event.getAxisValue(state.rightStickX);
    state.rightY = event.getAxisValue(state.rightStickY);
    if (state.leftTriggerAxis != -1) state.leftTrigger = event.getAxisValue(state.leftTriggerAxis);
    if (state.rightTriggerAxis != -1) state.rightTrigger = event.getAxisValue(state.rightTriggerAxis);
    state.dpadX = event.getAxisValue(MotionEvent.AXIS_HAT_X);
    state.dpadY = event.getAxisValue(MotionEvent.AXIS_HAT_Y);
    packetWriter.write(ControlPacket.createUhidInputEvent(state.id, state.createReport()));
    return true;
  }

  public void close() {
    for (GamepadState state : gamepads.values()) packetWriter.write(ControlPacket.createUhidDestroyEvent(state.id));
    gamepads.clear();
  }

  private GamepadState getState(int id) {
    GamepadState state = gamepads.get(id);
    if (state != null) return state;
    InputDevice device = InputDevice.getDevice(id);
    if (device == null) return null;
    state = new GamepadState(id, device);
    gamepads.put(id, state);
    // Match scrcpy's virtual controller identity so Android selects the expected HID mapping.
    packetWriter.write(ControlPacket.createUhidCreateEvent(id, XBOX_VENDOR_ID, XBOX_PRODUCT_ID, XBOX_NAME, REPORT_DESCRIPTOR));
    return state;
  }

  private static boolean isGamepad(int source) {
    return (source & InputDevice.SOURCE_GAMEPAD) == InputDevice.SOURCE_GAMEPAD || (source & InputDevice.SOURCE_JOYSTICK) == InputDevice.SOURCE_JOYSTICK;
  }

  private static int getButtonBit(int keyCode) {
    switch (keyCode) {
      case KeyEvent.KEYCODE_BUTTON_A: return 0x0001;
      case KeyEvent.KEYCODE_BUTTON_B: return 0x0002;
      case KeyEvent.KEYCODE_BUTTON_X: return 0x0008;
      case KeyEvent.KEYCODE_BUTTON_Y: return 0x0010;
      case KeyEvent.KEYCODE_BUTTON_L1: return 0x0040;
      case KeyEvent.KEYCODE_BUTTON_R1: return 0x0080;
      case KeyEvent.KEYCODE_BUTTON_SELECT: return 0x0400;
      case KeyEvent.KEYCODE_BUTTON_START: return 0x0800;
      case KeyEvent.KEYCODE_BUTTON_MODE: return 0x1000;
      case KeyEvent.KEYCODE_BUTTON_THUMBL: return 0x2000;
      case KeyEvent.KEYCODE_BUTTON_THUMBR: return 0x4000;
      case KeyEvent.KEYCODE_DPAD_UP: return 0x10000;
      case KeyEvent.KEYCODE_DPAD_DOWN: return 0x20000;
      case KeyEvent.KEYCODE_DPAD_LEFT: return 0x40000;
      case KeyEvent.KEYCODE_DPAD_RIGHT: return 0x80000;
      default: return 0;
    }
  }

  private static int toAxis(float value) {
    return Math.max(0, Math.min(65535, Math.round((value + 1) * 32767.5f)));
  }

  private static int toTrigger(float value) {
    return Math.max(0, Math.min(32767, Math.round(Math.max(0, value) * 32767)));
  }

  public interface PacketWriter {
    void write(ByteBuffer packet);
  }

  private static final class GamepadState {
    private final int id;
    private int buttons;
    private float leftX;
    private float leftY;
    private float rightX;
    private float rightY;
    private float leftTrigger;
    private float rightTrigger;
    private float dpadX;
    private float dpadY;

    private final int rightStickX;
    private final int rightStickY;
    private final int leftTriggerAxis;
    private final int rightTriggerAxis;

    private GamepadState(int id, InputDevice device) {
      this.id = id;
      // Sticks have a signed, centered range; triggers have a one-sided range.
      boolean zRzStick = isStickPair(device, MotionEvent.AXIS_Z, MotionEvent.AXIS_RZ);
      boolean rxRyStick = isStickPair(device, MotionEvent.AXIS_RX, MotionEvent.AXIS_RY);
      boolean useZRz = zRzStick || !rxRyStick;
      rightStickX = useZRz ? MotionEvent.AXIS_Z : MotionEvent.AXIS_RX;
      rightStickY = useZRz ? MotionEvent.AXIS_RZ : MotionEvent.AXIS_RY;
      leftTriggerAxis = hasAxis(device, MotionEvent.AXIS_LTRIGGER) ? MotionEvent.AXIS_LTRIGGER : hasAxis(device, MotionEvent.AXIS_BRAKE) ? MotionEvent.AXIS_BRAKE : useZRz && hasAxis(device, MotionEvent.AXIS_RX) ? MotionEvent.AXIS_RX : !useZRz && hasAxis(device, MotionEvent.AXIS_Z) ? MotionEvent.AXIS_Z : -1;
      rightTriggerAxis = hasAxis(device, MotionEvent.AXIS_RTRIGGER) ? MotionEvent.AXIS_RTRIGGER : hasAxis(device, MotionEvent.AXIS_GAS) ? MotionEvent.AXIS_GAS : useZRz && hasAxis(device, MotionEvent.AXIS_RY) ? MotionEvent.AXIS_RY : !useZRz && hasAxis(device, MotionEvent.AXIS_RZ) ? MotionEvent.AXIS_RZ : -1;
    }

    private static boolean hasAxis(InputDevice device, int axis) {
      return device.getMotionRange(axis) != null;
    }

    private static boolean isStickPair(InputDevice device, int xAxis, int yAxis) {
      InputDevice.MotionRange xRange = device.getMotionRange(xAxis);
      InputDevice.MotionRange yRange = device.getMotionRange(yAxis);
      return xRange != null && yRange != null && xRange.getMin() < 0 && xRange.getMax() > 0 && yRange.getMin() < 0 && yRange.getMax() > 0;
    }

    private ByteBuffer createReport() {
      ByteBuffer report = ByteBuffer.allocate(15).order(ByteOrder.LITTLE_ENDIAN);
      report.putShort((short) toAxis(leftX));
      report.putShort((short) toAxis(leftY));
      report.putShort((short) toAxis(rightX));
      report.putShort((short) toAxis(rightY));
      report.putShort((short) toTrigger(leftTrigger));
      report.putShort((short) toTrigger(rightTrigger));
      report.putShort((short) buttons);
      report.put((byte) getHat(buttons, dpadX, dpadY));
      report.flip();
      return report;
    }

    private static int getHat(int buttons, float x, float y) {
      boolean up = (buttons & 0x10000) != 0 || y < -0.5f;
      boolean down = (buttons & 0x20000) != 0 || y > 0.5f;
      boolean left = (buttons & 0x40000) != 0 || x < -0.5f;
      boolean right = (buttons & 0x80000) != 0 || x > 0.5f;
      if (up) return left ? 8 : right ? 2 : 1;
      if (down) return left ? 6 : right ? 4 : 5;
      return left ? 7 : right ? 3 : 0;
    }
  }
}
