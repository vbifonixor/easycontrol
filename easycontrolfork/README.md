# EasyControlFork

## External temporary stream

`ExternalStreamActivity` is an exported gateway for connecting to a network device without creating or changing a saved device profile. It creates an in-memory temporary profile, connects immediately by default, and discards all changes when the stream closes.

Use either the URI form or same-named Intent extras. Intent extras override URI query parameters.

### URI

```text
easycontrol://connect?tcpip=192.168.1.50:5555&max-size=1600&max-fps=60&video-bit-rate=4M&video-codec=h265&audio=true
```

```powershell
adb shell am start -a android.intent.action.VIEW -d "easycontrol://connect?tcpip=192.168.1.50:5555&max-size=1600&max-fps=60&video-bit-rate=4M&video-codec=h265&audio=true"
```

### ADB example

```powershell
adb shell am start -a com.vbifonix.easycontrolfork.action.CONNECT -n com.vbifonix.easycontrolfork/.ExternalStreamActivity --es tcpip 192.168.1.50:5555 --ei max-size 1600 --ei max-fps 60 --es video-bit-rate 4M --es video-codec h265 --ez audio true
```

The device configuration screen can copy the current settings as either a deep link or a Termux-ready explicit activity command. The Termux command uses `am start -n com.vbifonix.easycontrolfork/.ExternalStreamActivity` and does not rely on an implicit action.

### Parameters

| Parameter | Default | Description |
| --- | --- | --- |
| `tcpip` | required unless `address` is set | Scrcpy-style `host[:adb-port]` connection target. |
| `address` | required unless `tcpip` is set | Hostname, IPv4 address, or IPv6 address. |
| `adb-port` | `5555` | ADB port. `tcpip` port takes precedence. |
| `server-port` | `25166` | EasyControlFork server port. |
| `connect` | `true` | Set `false` to open the app without connecting. |
| `start-app` | empty | Package name for Just One App mode. |
| `listen-clipboard` | `true` | Synchronize client clipboard to the remote device. |
| `audio` | `false` | Enable audio forwarding. |
| `max-size` | `1600` | Maximum video dimension. |
| `max-fps` | `60` | Maximum video frame rate. |
| `video-bit-rate` | `4M` | Scrcpy-style video bit rate, for example `4M` or `4000000`. |
| `max-video-bit` | `4` | Alternative video bit rate in Mbps. |
| `video-codec` | `h265` | `h264` or `h265`. |
| `wake-on-connect` | `true` | Wake the remote device after connection. |
| `turn-screen-off-on-connect` | `false` | Turn off the remote backlight after connection. |
| `show-nav-bar` | `true` | Show the navigation bar in the streamed view. |
| `start-fullscreen` | `false` | Start in full screen instead of the floating view. |
| `stay-awake` | `true` | Keep the remote device awake while connected. |
| `custom-resolution` | unset | Resolution applied on connection, formatted `WIDTHxHEIGHT`. |
| `custom-resolution-on-connect` | `false` | Enable the supplied custom-resolution width and height. |
| `custom-resolution-width` | `1080` | Custom resolution width. |
| `custom-resolution-height` | `2400` | Custom resolution height. |
| `change-resolution-on-running` | `false` | Adapt resolution to the current view. |
| `small-to-mini` | `false` | Switch a small view to mini when it loses focus. |
| `full-to-mini` | `true` | Switch a full view to mini when it loses focus. |
| `mini-timeout` | `false` | Return from mini view after inactivity. |
| `lock-on-close` | `true` | Lock the remote device on disconnect. |
| `turn-screen-on-on-close` | `false` | Turn the remote backlight on at disconnect. |
| `reconnect-on-close` | `false` | Reconnect after an unexpected disconnect. |
| `small-x`, `small-y` | `200` | Portrait small-view position. |
| `small-size` | `800` | Portrait small-view size. |
| `small-x-landscape`, `small-y-landscape` | `200` | Landscape small-view position. |
| `small-size-landscape` | `800` | Landscape small-view size. |
| `mini-y` | `200` | Mini-view vertical position. |

Boolean parameters accept `true`, `false`, `1`, `0`, `yes`, and `no`.

Profile identity (`uuid`, `name`, and connection type) and `connect-on-start` are intentionally unavailable: this API always creates one temporary network profile and never stores it.

Closing an externally launched stream from its on-screen close button removes the app task and terminates the app process. Unexpected remote disconnects do not terminate the app.

## Wireless Debugging pairing

On Android 11 or newer, open **Developer options > Wireless debugging** on the target device and enter its displayed IP address and ADB port in EasyControlFork. Select **Pair wireless debugging**, then open **Pair device with pairing code** on the target and enter the displayed code and pairing port. The app verifies the ADB connection before saving the profile.

Use the **TCP/IP** connection method for classic TCP ADB profiles.

Paired Wireless Debugging profiles use JmDNS to refresh their dynamic TLS ADB address and port when the network permits multicast DNS.

On Android versions which offer **Pair device with QR code**, use **Scan pairing QR code** in the pairing screen instead. EasyControlFork requests camera permission when the scanner is opened, then fills the address, port, and pairing code from Android's ADB QR payload. Tap **Pair** to confirm.

After pairing, EasyControlFork discovers the target's TLS ADB service and adds a network profile. Its mDNS service name is stored with the profile. When the app next starts on another network, it resolves that service name and updates the profile's IP address and ADB port automatically.
