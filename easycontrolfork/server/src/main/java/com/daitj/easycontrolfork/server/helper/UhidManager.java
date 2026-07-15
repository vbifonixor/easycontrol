package com.daitj.easycontrolfork.server.helper;

import android.system.ErrnoException;
import android.system.Os;
import android.system.OsConstants;

import java.io.FileDescriptor;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;

public final class UhidManager {
  private static final int UHID_CREATE2 = 11;
  private static final int UHID_INPUT2 = 12;
  private static final short BUS_VIRTUAL = 0x06;
  private final HashMap<Integer, FileDescriptor> devices = new HashMap<>();

  public void open(int id, int vendorId, int productId, String name, byte[] descriptor) throws IOException {
    close(id);
    try {
      FileDescriptor fd = Os.open("/dev/uhid", OsConstants.O_RDWR, 0);
      try {
        byte[] request = createRequest(vendorId, productId, name, descriptor);
        Os.write(fd, request, 0, request.length);
        devices.put(id, fd);
      } catch (ErrnoException e) {
        Os.close(fd);
        throw e;
      }
    } catch (ErrnoException e) {
      throw new IOException(e);
    }
  }

  public void writeInput(int id, byte[] report) throws IOException {
    FileDescriptor fd = devices.get(id);
    if (fd == null) return;
    try {
      ByteBuffer request = ByteBuffer.allocate(6 + report.length).order(ByteOrder.nativeOrder());
      request.putInt(UHID_INPUT2);
      request.putShort((short) report.length);
      request.put(report);
      Os.write(fd, request.array(), 0, request.capacity());
    } catch (ErrnoException e) {
      throw new IOException(e);
    }
  }

  public void close(int id) {
    FileDescriptor fd = devices.remove(id);
    if (fd == null) return;
    try {
      Os.close(fd);
    } catch (ErrnoException ignored) {
    }
  }

  public void closeAll() {
    for (FileDescriptor fd : devices.values()) {
      try {
        Os.close(fd);
      } catch (ErrnoException ignored) {
      }
    }
    devices.clear();
  }

  private static byte[] createRequest(int vendorId, int productId, String name, byte[] descriptor) {
    ByteBuffer request = ByteBuffer.allocate(280 + descriptor.length).order(ByteOrder.nativeOrder());
    request.putInt(UHID_CREATE2);
    byte[] nameBytes = name.getBytes(StandardCharsets.UTF_8);
    request.put(nameBytes, 0, Math.min(nameBytes.length, 127));
    request.position(260);
    request.putShort((short) descriptor.length);
    request.putShort(BUS_VIRTUAL);
    request.putInt(vendorId);
    request.putInt(productId);
    request.putInt(0);
    request.putInt(0);
    request.put(descriptor);
    return request.array();
  }
}
