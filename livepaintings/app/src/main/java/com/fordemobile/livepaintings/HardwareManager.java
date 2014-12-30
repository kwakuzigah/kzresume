package com.fordemobile.livepaintings;

import android.app.Service;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.BitmapFactory;
import android.hardware.Camera;
import android.media.MediaPlayer;
import android.os.Vibrator;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Display;
import android.view.WindowManager;

import java.io.IOException;
import java.io.InputStream;

import asigbe.hardware.MovementDetector;
import asigbe.hardware.ShakingMovement;
import asigbe.sensor.OnMovementListener;

public class HardwareManager {
	private Context context;
	private boolean mustVibrate = true;
	private boolean mustPlaySound = true;
	private static MovementDetector movementDetector;

	private MediaPlayer mediaPlayer;
	private static int lastSampleSize = 1;
	private final static HardwareManager hardwareManager = new HardwareManager();

	private HardwareManager() {

	}

	public static Camera getCameraInstance() {
		Camera c = null;
		try {
			int cameraCount = 0;
			Camera.CameraInfo cameraInfo = new Camera.CameraInfo();
			cameraCount = Camera.getNumberOfCameras();
			Log.w("HardwareManager", "Camera count: " + cameraCount);
			try {
				c = Camera.open();
				Log.w("HardwareManager", "worked");
			} catch (RuntimeException e) {
				Log.e("HardwareManager",
						"Camera failed to open: " + e.getLocalizedMessage());
			}
			/*
			 * for ( int camIdx = 0; camIdx < cameraCount; camIdx++ ) {
			 * Camera.getCameraInfo( camIdx, cameraInfo ); // if (
			 * cameraInfo.facing == Camera.CameraInfo.CAMERA_FACING_FRONT ) {
			 * try { c = Camera.open( camIdx ); } catch (RuntimeException e) {
			 * Log.e("HardwareManager", "Camera failed to open: " +
			 * e.getLocalizedMessage()); } // } }
			 */
		} catch (Exception e) {
			// Camera is not available (in use or does not exist)
		}
		return c; // returns null if camera is unavailable
	}
	
	public static int getLastSampleSize() {
		return lastSampleSize;
	}

	public static HardwareManager getInstance() {
		return hardwareManager;
	}

	public static void initialize(Context context) {
		hardwareManager.context = context;
		hardwareManager.mediaPlayer = new MediaPlayer();

		movementDetector = new MovementDetector(context, new ShakingMovement());
	}

	public static void vibrate() {
		vibrate(50);
	}

	public static void vibrate(int time) {
		if (hardwareManager.mustVibrate) {
			((Vibrator) hardwareManager.context
					.getSystemService(Service.VIBRATOR_SERVICE)).vibrate(time);
		}
	}

	public static void setMustVibrate(boolean mustVibrate) {
		hardwareManager.mustVibrate = mustVibrate;
	}

	public static boolean getMustVibrate() {
		return hardwareManager.mustVibrate;
	}

	public static void setMustPlaySound(boolean mustPlaySound) {
		hardwareManager.mustPlaySound = mustPlaySound;
	}

	public static boolean getMustPlaySound() {
		return hardwareManager.mustPlaySound;
	}

	public static void playSound(int resource, boolean looping) {
		if (hardwareManager.mustPlaySound) {
			if (hardwareManager.mediaPlayer != null) {
				stopSound();
			}
			try {
				hardwareManager.mediaPlayer = MediaPlayer.create(
						hardwareManager.context, resource);
				hardwareManager.mediaPlayer.setLooping(looping);
				hardwareManager.mediaPlayer.start();
			} catch (IllegalArgumentException e) {
			} catch (IllegalStateException e) {
			}
		}
	}

	public static void stopSound() {
		if (hardwareManager.mediaPlayer != null) {
			hardwareManager.mediaPlayer.stop();
			hardwareManager.mediaPlayer.release();
			hardwareManager.mediaPlayer = null;
		}
	}

	public static Bitmap getSmallBitmap(String path) {
		String prefix = "";
		switch (hardwareManager.context.getResources().getDisplayMetrics().densityDpi) {
		// case DisplayMetrics.DENSITY_LOW:
		// prefix = "ldpi";
		// break;
		// case DisplayMetrics.DENSITY_MEDIUM:
		// prefix = "mdpi";
		// break;
		// case DisplayMetrics.DENSITY_HIGH:
		// case DisplayMetrics.DENSITY_XHIGH:
		default:
			prefix = "hdpi";
			break;
		}

		Bitmap bitmap = null;
		try {
			BitmapFactory.Options options = new BitmapFactory.Options();
			options.inSampleSize = 8;
			bitmap = BitmapFactory.decodeStream(hardwareManager.context
					.getAssets().open(prefix + "/" + path), null, options);
		} catch (IOException e) {
			return null;
		}
		return bitmap;
	}

	public static float getDensityFactor() {
		float factory = 1.0f;
		switch (hardwareManager.context.getResources().getDisplayMetrics().densityDpi) {
		case DisplayMetrics.DENSITY_LOW:
			factory = 1.0f / 2.0f;
			break;
		case DisplayMetrics.DENSITY_MEDIUM:
			factory = 2.0f / 3.0f;
			break;
		case DisplayMetrics.DENSITY_XHIGH:
			factory = 1.5f;
			break;
		case DisplayMetrics.DENSITY_HIGH:
		default:
			factory = 1.0f;
			break;
		}

		return factory;
	}

	public static InputStream openFile(String path) {
		InputStream fileInputStream = null;
		try {
			fileInputStream = hardwareManager.context.getAssets().open(path);
		} catch (IOException e) {
			return null;
		}
		return fileInputStream;
	}

	public static Bitmap getBitmap(String path, boolean transparency,
			boolean dynamic) {
		String prefix = "";
		// switch
		// (hardwareManager.context.getResources().getDisplayMetrics().densityDpi)
		// {
		// case DisplayMetrics.DENSITY_LOW:
		// prefix = "ldpi";
		// break;
		// case DisplayMetrics.DENSITY_MEDIUM:
		// prefix = "mdpi";
		// break;
		// case DisplayMetrics.DENSITY_HIGH:
		// case DisplayMetrics.DENSITY_XHIGH:
		// default:
		// prefix = "hdpi";
		// break;
		// }

		Bitmap bitmap = null;
		try {
			BitmapFactory.Options resample = new BitmapFactory.Options();
			if (dynamic) {
				BitmapFactory.Options opt = new BitmapFactory.Options();
				opt.inJustDecodeBounds = true;
				bitmap = BitmapFactory.decodeStream(hardwareManager.context
						.getAssets().open(path), null, opt);
				if (opt.outWidth != -1) { // TODO: Error }
					int width = opt.outWidth;
					int height = opt.outHeight;
					Display display = ((WindowManager) hardwareManager.context
							.getSystemService(Context.WINDOW_SERVICE))
							.getDefaultDisplay();
					int maxWidth = display.getWidth();
					int maxHeight = display.getHeight();
					boolean withinBounds = width <= maxWidth
							&& height <= maxHeight;
					if (!withinBounds) {
						float sampleSizeWidth = (float) width
								/ (float) maxWidth;
						float sampleSizeHeight = (float) height
								/ (float) maxHeight;
						int sampleSize = Math.round(Math.min(sampleSizeWidth,
								sampleSizeHeight));
                        if (sampleSize > 0) {
                            resample.inSampleSize = sampleSize;
                            HardwareManager.lastSampleSize = resample.inSampleSize;
                        }
					}
				}
			}

			if (transparency) {
				resample.inPreferredConfig = Config.ARGB_8888;
			} else {
				resample.inPreferredConfig = Config.RGB_565;
			}

			bitmap = BitmapFactory.decodeStream(hardwareManager.context
					.getAssets().open(path), null, resample);
		} catch (IOException e) {
			return null;
		}
		return bitmap;
	}

	public static Bitmap getBitmap(String path) {
		return getBitmap(path, false, false);
	}

	public static void setOnMovementListener(
			OnMovementListener onMovementListener) {
		HardwareManager.movementDetector
				.setOnMovementListener(onMovementListener);
	}

	public static void startListeningMovement() {
		// hardwareManager.movementDetector.resume();
	}

	public static void stopListeningMovement() {
		// hardwareManager.movementDetector.pause();
	}
}
