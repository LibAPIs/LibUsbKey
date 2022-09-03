package com.mclarkdev.tools.libusbkey;

import org.hid4java.HidDevice;
import org.hid4java.HidManager;
import org.hid4java.HidServices;
import org.hid4java.HidServicesListener;
import org.hid4java.HidServicesSpecification;
import org.hid4java.event.HidServicesEvent;

public class LibUsbKey implements HidServicesListener {

	private final int PACKET_SIZE = 16;

	private final int KEY_VENDOR = 0x239a;
	private final int KEY_PRODUCT = 0x80ef;

	private static final LibUsbKey libUsbKey = new LibUsbKey();

	public static LibUsbKey instance() {
		return libUsbKey;
	}

	private final HidServices hidServices;

	private boolean connected = false;

	private HidDevice usbKey = null;

	private LibUsbKey() {

		HidServicesSpecification hidServicesSpecification = //
				new HidServicesSpecification();
		hidServicesSpecification.setAutoStart(false);
		// hidServicesSpecification.setScanInterval(100);

		// Get HID services using custom specification
		hidServices = HidManager.getHidServices(hidServicesSpecification);
		hidServices.addHidServicesListener(this);
		hidServices.start();

		enumerate();
	}

	private void enumerate() {

		// Use HID services to find the UsbKey device
		usbKey = hidServices.getHidDevice(KEY_VENDOR, KEY_PRODUCT, null);
		boolean state = (usbKey != null);

		// skip if same state
		if (connected == state) {
			return;
		}

		// set new state
		connected = state;

		// skip if not connected
		if (!connected) {
			return;
		}

		// fork new thread
		fork();
	}

	private void fork() {
		new Thread() {

			public void run() {

				System.out.print("Connected: ");
				System.out.println(usbKey);

				while (usbKey.isOpen()) {

					byte[] read = new byte[PACKET_SIZE];
					int num = usbKey.read(read);
					if (num <= 0) {
						break;
					}

					String msg = new String(read, 0, num);
					System.out.println("Message: " + msg);
					System.out.println("RAW: " + bytesToHex(read));
				}

				System.out.print("Done.");
			}
		}.start();
	}

	@Override
	public void hidDeviceAttached(HidServicesEvent event) {

		System.out.print("Found: ");
		System.out.println(event.toString());

		enumerate();
	}

	@Override
	public void hidDeviceDetached(HidServicesEvent event) {

		System.out.print("Lost: ");
		System.out.println(event.toString());

		enumerate();
	}

	@Override
	public void hidFailure(HidServicesEvent event) {

		System.out.print("Failed: ");
		System.out.println(event.toString());

		enumerate();
	}

	public boolean isConnected() {
		return (usbKey != null);
	}

	public void write(byte[] bytes) {
		if (usbKey == null) {
			return;
		}

		synchronized (usbKey) {
			usbKey.write(bytes, PACKET_SIZE, (byte) 0x00);
			System.out.print("Wrote: ");
			System.out.println(bytesToHex(bytes));
		}
	}

	public static String bytesToHex(byte[] bytes) {

		if (bytes == null) {

			throw new IllegalArgumentException("supplied value cannot be null");
		}

		char[] hexArray = "0123456789ABCDEF".toCharArray();

		char[] hexChars = new char[bytes.length * 2];

		for (int j = 0; j < bytes.length; j++) {

			int v = bytes[j] & 0xFF;
			hexChars[j * 2] = hexArray[v >>> 4];
			hexChars[j * 2 + 1] = hexArray[v & 0x0F];
		}
		return new String(hexChars);
	}
}
