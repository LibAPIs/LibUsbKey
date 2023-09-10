package com.mclarkdev.tools.libusbkey;

import java.io.IOException;

import org.hid4java.HidDevice;
import org.hid4java.HidManager;
import org.hid4java.HidServices;
import org.hid4java.HidServicesListener;
import org.hid4java.HidServicesSpecification;
import org.hid4java.event.HidServicesEvent;

import com.mclarkdev.tools.libextras.LibExtrasStrings;
import com.mclarkdev.tools.liblog.LibLog;

/**
 * LibUsbKey // LibUsbKey
 * 
 * A basic USB HID interface for communicating with ATSAMD21 firmware.
 */
public class LibUsbKey {

	private final int deviceVendor;
	private final int deviceProduct;
	private final int devicePacketSize;

	private final HidServices hidServices;

	private boolean connected = false;

	private HidDevice usbKey = null;

	/**
	 * Create a new object instance with the desired VENDOR/PRODUCT key.
	 * 
	 * @param vendor  device vendor code
	 * @param product device product code
	 */
	public LibUsbKey(int vendor, int product) {
		this(vendor, product, 16);
	}

	/**
	 * Create a new object instance with the desired VENDOR/PRODUCT key.
	 * 
	 * @param vendor     device vendor code
	 * @param product    device product code
	 * @param packetSize device packet size
	 */
	public LibUsbKey(int vendor, int product, int packetSize) {

		// device specifics
		this.deviceVendor = vendor;
		this.deviceProduct = product;
		this.devicePacketSize = packetSize;

		// Configuration for auto-start
		HidServicesSpecification hidServicesSpecification = //
				new HidServicesSpecification();
		hidServicesSpecification.setAutoStart(false);

		// Get HID services using custom specification
		hidServices = HidManager.getHidServices(hidServicesSpecification);
		hidServices.addHidServicesListener(serviceListener);
		hidServices.start();
		enumerate();
	}

	/**
	 * Enumerate all attached HID devices.
	 */
	private void enumerate() {

		// Use HID services to find the UsbKey device
		usbKey = hidServices.getHidDevice(deviceVendor, deviceProduct, null);
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

	/**
	 * Fork a background thread to process incoming packets.
	 */
	private void fork() {
		new Thread() {

			public void run() {

				LibLog.log("USB", "Connected: " + usbKey.toString());

				while (usbKey.isOpen()) {

					byte[] read = new byte[devicePacketSize];
					int num = usbKey.read(read);
					if (num <= 0) {
						break;
					}

					LibLog.log("USB", "RAW: " + LibExtrasStrings.bytesToHex(read));
				}

				LibLog.log("USB", "Done.");
			}
		}.start();
	}

	private HidServicesListener serviceListener = new HidServicesListener() {

		/**
		 * Called when a new device is attached.
		 */
		@Override
		public void hidDeviceAttached(HidServicesEvent event) {

			LibLog.log("USB", "Found: " + event.toString());
			enumerate();
		}

		/**
		 * Called when a device is disconnected.
		 */
		@Override
		public void hidDeviceDetached(HidServicesEvent event) {

			LibLog.log("USB", "Lost: " + event.toString());
			enumerate();
		}

		/**
		 * Called when a device detects a failure.
		 */
		@Override
		public void hidFailure(HidServicesEvent event) {

			LibLog.log("USB", "Failed: " + event.toString());
			enumerate();
		}
	};

	/**
	 * Check if the device is connected.
	 * 
	 * @return device is connected and ready
	 */
	public boolean isConnected() {
		return (usbKey != null && usbKey.isOpen());
	}

	/**
	 * Write a given payload to the device.
	 * 
	 * @param bytes the data to write
	 */
	public void write(byte[] bytes) throws IOException {

		if (usbKey == null || (!usbKey.isOpen())) {
			throw new IOException("no device");
		}

		synchronized (usbKey) {

			try {
				usbKey.write(bytes, devicePacketSize, (byte) 0x00);
				LibLog.log("USB", "Wrote: " + LibExtrasStrings.bytesToHex(bytes));
			} catch (IllegalStateException e) {
				throw new IOException(e);
			}
		}
	}
}
