package net.skoumal.emulatordetector;

import android.os.Build;
import android.util.Log;

/**
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on
 * an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations under the License.
 *
 * Copyright (C) 2013, Vladislav Gingo Skoumal (http://www.skoumal.net)
 *
 */

public class EmulatorDetector {

	private static final String TAG = "EmulatorDetector";

	private static int rating = -1;

	/**
	 * Detects if app is currenly running on emulator, or real device.
	 * @return true for emulator, false for real devices
	 */
	public static boolean isEmulator() {

		if(rating < 0) { // rating is not calculated yet
			int newRating = 0;

			if(Build.PRODUCT.equals("sdk") ||
					Build.PRODUCT.equals("google_sdk") ||
					Build.PRODUCT.equals("sdk_x86") ||
					Build.PRODUCT.equals("vbox86p")) {
				newRating ++;
			}

			if(Build.MANUFACTURER.equals("unknown") ||
					Build.MANUFACTURER.equals("Genymotion")) {
				newRating ++;
			}

			if(Build.BRAND.equals("generic") ||
					Build.BRAND.equals("generic_x86")) {
				newRating ++;
			}

			if(Build.DEVICE.equals("generic") ||
					Build.DEVICE.equals("generic_x86") ||
					Build.DEVICE.equals("vbox86p")) {
				newRating ++;
			}

			if(Build.MODEL.equals("sdk") ||
					Build.MODEL.equals("google_sdk") ||
					Build.MODEL.equals("Android SDK built for x86")) {
				newRating ++;
			}

			if(Build.HARDWARE.equals("goldfish") ||
					Build.HARDWARE.equals("vbox86")) {
				newRating ++;
			}

			if(Build.FINGERPRINT.contains("generic/sdk/generic") ||
					Build.FINGERPRINT.contains("generic_x86/sdk_x86/generic_x86") ||
					Build.FINGERPRINT.contains("generic/google_sdk/generic") ||
					Build.FINGERPRINT.contains("generic/vbox86p/vbox86p")) {
				newRating ++;
			}

			rating = newRating;
		}

		return rating > 4;
	}

	/**
	 * Returns string with human-readable listing of Build.* parameters used in {@link #isEmulator()} method.
	 * @return all involved Build.* parameters and its values
	 */
	public static String getDeviceListing() {
		return "Build.PRODUCT: " + Build.PRODUCT + "\n" +
				"Build.MANUFACTURER: " + Build.MANUFACTURER + "\n" +
				"Build.BRAND: " + Build.BRAND + "\n" +
				"Build.DEVICE: " + Build.DEVICE + "\n" +
				"Build.MODEL: " + Build.MODEL + "\n" +
				"Build.HARDWARE: " + Build.HARDWARE + "\n" +
				"Build.FINGERPRINT: " + Build.FINGERPRINT;
	}

	/**
	 * Prints all Build.* parameters used in {@link #isEmulator()} method to logcat.
	 */
	public static void logcat() {
		Log.d(TAG, getDeviceListing());
	}

}
