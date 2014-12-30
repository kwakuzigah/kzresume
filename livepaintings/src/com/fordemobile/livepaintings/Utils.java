package com.fordemobile.livepaintings;

import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;

public class Utils {
	/*
	public static Bitmap addTransparency(Bitmap srcBitmap, int bgColor) {

		int width = srcBitmap.getWidth();
		int height = srcBitmap.getHeight();
		int count = width * height;

		int[] alphaPix = new int[count];
		srcBitmap.getPixels(alphaPix, 0, width, 0, 0, width, height);
		int totalDiff;
		for (int i = 0; i < count; ++i) {
			totalDiff = 0;
			totalDiff += Math.abs((bgColor & 0xFF) - (alphaPix[i] & 0xFF))
					+ Math.abs(((bgColor & 0xFF00) >> 8)
							- ((alphaPix[i] & 0xFF00) >> 8))
					+ Math.abs(((bgColor & 0xFF0000) >> 16)
							- ((alphaPix[i] & 0xFF0000) >> 16));
			if (totalDiff <= 70) {
				alphaPix[i] = 0x00000000f;
			}
		}
		Bitmap alpha = Bitmap.createBitmap(width, height, Config.ARGB_8888);
		alpha.setPixels(alphaPix, 0, width, 0, 0, width, height);
		return alpha;
	}*/
}
