# LibUsbKey

A basic USB HID interface for communicating with ATSAMD21 firmware.

## Maven Dependency

Include the library in your project by adding the following dependency to your pom.xml

```
<dependency>
	<groupId>com.mclarkdev.tools</groupId>
	<artifactId>libusbkey</artifactId>
	<version>1.5.1</version>
</dependency>
```

## Example

Create a new instance of LibUsbKey with the user defined hardware device ID.

```
LibUsbKey usb = new LibUsbKey(0x239A, 0x80EF, 16);

// Wait for connection
while (!usb.isConnected())
	Thread.sleep(50);

// Ask device for info
usb.write(LibUsbKeyPackets._PKT_INFO);

// Send device heartbeat
usb.write(LibUsbKeyPackets._PKT_HEARTBEAT);
```

# License

Open source & free for all. ❤
