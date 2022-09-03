package com.mclarkdev.tools.libusbkey;

public class Runner {
	public static void main(String[] args) throws InterruptedException {

		byte[] out = new byte[] { //
				0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, //
				0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00 };

		LibUsbKey usb = LibUsbKey.instance();

		while (!usb.isConnected()) {
		}

		while (true) {

			// reset
			out[0] = 0x00;
			out[1] = 0x00;
			out[2] = 0x00;
			out[3] = 0x00;

			// heartbeat
			out[0] = 0x02;
			usb.write(out);

			// get info
			out[0] = 0x04;
			usb.write(out);

			// set color
			out[0] = 0x08;

			// r -> g
			for (int v = 0; v < 255; v += 5) {

				out[2] = (byte) (255 - v);
				out[3] = (byte) v;
				out[4] = 0x00;

				out[5] = 0x00;
				out[6] = (byte) (255 - v);
				out[7] = (byte) v;

				out[8] = (byte) v;
				out[9] = (byte) 0x00;
				out[10] = (byte) 0xFF;

				out[11] = (byte) 0xFF;
				out[12] = 0x00;
				out[13] = (byte) (255 - v);

				usb.write(out);
			}

			// g -> b
			for (int v = 0; v < 255; v += 5) {

				out[2] = 0x00;
				out[3] = (byte) (255 - v);
				out[4] = (byte) v;

				out[5] = (byte) v;
				out[6] = (byte) 0x00;
				out[7] = (byte) 0xFF;

				out[8] = (byte) 0xFF;
				out[9] = 0x00;
				out[10] = (byte) (255 - v);

				out[11] = (byte) (255 - v);
				out[12] = (byte) v;
				out[13] = 0x00;

				usb.write(out);
			}

			// b -> p
			for (int v = 0; v < 255; v += 5) {

				out[2] = (byte) v;
				out[3] = (byte) 0x00;
				out[4] = (byte) 0xFF;

				out[5] = (byte) 0xFF;
				out[6] = 0x00;
				out[7] = (byte) (255 - v);

				out[8] = (byte) (255 - v);
				out[9] = (byte) v;
				out[10] = 0x00;

				out[11] = 0x00;
				out[12] = (byte) (255 - v);
				out[13] = (byte) v;

				usb.write(out);
			}

			// p -> r
			for (int v = 0; v < 255; v += 5) {

				out[2] = (byte) 0xFF;
				out[3] = 0x00;
				out[4] = (byte) (255 - v);

				out[5] = (byte) (255 - v);
				out[6] = (byte) v;
				out[7] = 0x00;

				out[8] = 0x00;
				out[9] = (byte) (255 - v);
				out[10] = (byte) v;

				out[11] = (byte) v;
				out[12] = (byte) 0x00;
				out[13] = (byte) 0xFF;

				usb.write(out);
			}
		}
	}
}
