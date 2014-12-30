package com.fordemobile.livepaintings;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;

import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.graphics.AvoidXfermode;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Point;
import android.graphics.PointF;
import android.graphics.Typeface;
import android.hardware.Camera;
import android.media.FaceDetector;
import android.os.Handler;
import android.service.wallpaper.WallpaperService;
import android.util.Log;
import android.view.MotionEvent;
import android.view.SurfaceHolder;

/*
 * This animated wallpaper draws a rotating wireframe cube.
 */
public class LivePaintingsWallpaper extends WallpaperService {

	private static int dd = 0;
	private final Handler handler = new Handler();
	private static Bitmap background;
	public static final int FRAME_RATE = 25;
	private static final String TAG = "LivePaintingsWallpaper";
	private Painting painting;

	@Override
	public void onCreate() {
		super.onCreate();
	}

	@Override
	public Engine onCreateEngine() {
		return new AnimationEngine();
	}

	public static boolean isSdPresent() {

		return android.os.Environment.getExternalStorageState().equals(
				android.os.Environment.MEDIA_MOUNTED);

	}

	class AnimationEngine extends Engine implements
			SharedPreferences.OnSharedPreferenceChangeListener {
		// , Camera.FaceDetectionListener {

		private final Runnable mDrawCharacter = new Runnable() {
			public void run() {
				drawFrame();
			}
		};
		private boolean mVisible;
		private boolean isLoading;
		private Paint paint;
		private SharedPreferences prefs;
		private Point leftEyePoint;
		private Point rightEyePoint;
		private FaceDetector.Face faces[];
		private PointF midPoint;
		private int width;
		private int height;
		private Paint transparentPaint;
		private Typeface tf;

		AnimationEngine() {
			Context applicationContext = getApplicationContext();
			tf = Typeface
					.createFromAsset(getAssets(), "fonts/Poly-Regular.ttf");

			HardwareManager.initialize(applicationContext);

			this.paint = new Paint();
			this.paint.setAntiAlias(true);
			this.paint.setDither(true);
			this.paint.setFilterBitmap(true);
			// this.paint.setColor(Color.BLUE);
			// int removeColor = this.paint.getColor();
			// this.paint.setAlpha(0);
			// this.paint.setXfermode(new AvoidXfermode(Color.BLUE, 125,
			// AvoidXfermode.Mode.TARGET));

			this.transparentPaint = new Paint();
			// this.paint.setDither(true);
			// this.paint.setFilterBitmap(true);
			this.transparentPaint.setColor(Color.BLUE);
			// int removeColor = this.paint.getColor();
			this.transparentPaint.setAlpha(0);
			this.transparentPaint.setXfermode(new AvoidXfermode(Color.BLUE,
					125, AvoidXfermode.Mode.AVOID));

			this.prefs = LivePaintingsWallpaper.this.getSharedPreferences(
					Consts.SHARED_PREFS_NAME, 0);
			this.prefs.registerOnSharedPreferenceChangeListener(this);
			onSharedPreferenceChanged(this.prefs, null);
		}

		// public void loadBackground(String backgroundName, boolean landscape)
		// {
		// if (EyesTrackingWallpaper.background != null) {
		// EyesTrackingWallpaper.background.recycle();
		// EyesTrackingWallpaper.background = null;
		// }
		// if (landscape) {
		// EyesTrackingWallpaper.background = HardwareManager
		// .getBitmap("backgrounds/land/" + backgroundName
		// + ".jpg");
		// } else {
		// EyesTrackingWallpaper.background = HardwareManager
		// .getBitmap("backgrounds/" + backgroundName + ".jpg");
		// }
		// }

		public void loadBackgroundFromPath(final String backgroundPath) {
			if (LivePaintingsWallpaper.background != null) {
				LivePaintingsWallpaper.background.recycle();
				LivePaintingsWallpaper.background = null;
			}
			try {
				// BitmapFactory.Options options = new BitmapFactory.Options();
				//
				// options.inSampleSize = 8;

				// KutizWallpaper.background = BitmapFactory.decodeStream(
				// new FileInputStream(backgroundPath), null, options);
				if (backgroundPath.contains("/sdcard/") && !isSdPresent()) {
					IntentFilter filter = new IntentFilter(
							Intent.ACTION_MEDIA_MOUNTED);
					filter.addDataScheme("file");
					registerReceiver(new BroadcastReceiver() {

						@Override
						public void onReceive(Context context, Intent intent) {
							try {
								LivePaintingsWallpaper.background = BitmapFactory.decodeStream(new FileInputStream(
										backgroundPath));
							} catch (FileNotFoundException e) {
								e.printStackTrace();
							}
						}
					}, new IntentFilter(filter));
				} else {
					LivePaintingsWallpaper.background = BitmapFactory
							.decodeStream(new FileInputStream(backgroundPath));
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
		}

		@Override
		public void onSharedPreferenceChanged(SharedPreferences prefs,
				String key) {
			// BitmapFactory.Options bfo = new BitmapFactory.Options();
			// bfo.inPreferredConfig = Bitmap.Config.RGB_565;

			final String paintingPath = prefs.getString(Consts.PREF_PAINTING,
					"paintings/monalisa/");

			if (LivePaintingsWallpaper.this.painting == null
					|| !LivePaintingsWallpaper.this.painting.getDirectoryPath().equals(paintingPath)) {
				dd++;
				if (!isLoading) {
					Log.d(TAG, "**here:"+dd);
					if (LivePaintingsWallpaper.this.painting != null) {
						Log.d(TAG, "**recyle:"+dd);
						LivePaintingsWallpaper.this.painting.recycle();
						LivePaintingsWallpaper.this.painting = null;
					} else {
						Log.d(TAG, "**null");
					}
					new Thread(new Runnable() {

						public void run() {
							AnimationEngine.this.isLoading = true;
							Painting painting = new Painting(paintingPath,
									AnimationEngine.this.width,
									AnimationEngine.this.height);
							Log.d(TAG, "**load:"+dd);
							painting.loadPainting();
							painting.updateSurfaceDimension(width, height);
							LivePaintingsWallpaper.this.painting = painting;
							AnimationEngine.this.isLoading = false;
						}
					}).start();
				}
			}
		}

		@Override
		public void onCreate(SurfaceHolder surfaceHolder) {
			super.onCreate(surfaceHolder);

			// By default we don't get touch events, so enable them.
			setTouchEventsEnabled(true);
		}

		@Override
		public void onDestroy() {
			super.onDestroy();
//			if (LivePaintingsWallpaper.this.painting != null) {
//				Log.d(TAG, "**destroy");
//				LivePaintingsWallpaper.this.painting.recycle();
//				LivePaintingsWallpaper.this.painting = null;
//			}
			LivePaintingsWallpaper.this.handler
					.removeCallbacks(this.mDrawCharacter);
		}

		@Override
		public void onVisibilityChanged(boolean visible) {
			this.mVisible = visible;
			if (visible) {
				// setTouchEventsEnabled(true);
				// camera = HardwareManager.getCameraInstance();
				// if (camera != null) {
				// camera.setFaceDetectionListener(this);
				// }
				if (LivePaintingsWallpaper.this.painting != null) {
					LivePaintingsWallpaper.this.painting.updateSurfaceDimension(width, height);
				}
				drawFrame();
			} else {
				// setTouchEventsEnabled(false);
				// if (camera != null) {
				// camera.setFaceDetectionListener(null);
				// camera.release();
				// camera = null;
				// }
				LivePaintingsWallpaper.this.handler
						.removeCallbacks(this.mDrawCharacter);
			}
		}

		@Override
		public void onSurfaceChanged(SurfaceHolder holder, int format,
				int width, int height) {
			this.width = width;
			this.height = height;
			super.onSurfaceChanged(holder, format, width, height);
			// if (background != null) {
			// xOffset = (width - background.getWidth()) / 2;
			// yOffset = (height-background.getHeight() ) / 2;
			// yOffset = 0;
			// }
			if (LivePaintingsWallpaper.this.painting != null) {
				LivePaintingsWallpaper.this.painting.updateSurfaceDimension(width, height);
			}
			drawFrame();
		}

		@Override
		public void onSurfaceCreated(SurfaceHolder holder) {
			super.onSurfaceCreated(holder);
		}

		@Override
		public void onSurfaceDestroyed(SurfaceHolder holder) {
			super.onSurfaceDestroyed(holder);
			this.mVisible = false;
			LivePaintingsWallpaper.this.handler
					.removeCallbacks(this.mDrawCharacter);
		}

		@Override
		public void onOffsetsChanged(float xOffset, float yOffset, float xStep,
				float yStep, int xPixels, int yPixels) {
			drawFrame();
		}

		/*
		 * Store the position of the touch event so we can use it for drawing
		 * later
		 */
		@Override
		public void onTouchEvent(MotionEvent event) {

			int eventX = (int) event.getX();
			int eventY = (int) event.getY();
			if (event.getAction() == MotionEvent.ACTION_DOWN) {
			} else if (event.getAction() == MotionEvent.ACTION_MOVE) {
			} else if (event.getAction() == MotionEvent.ACTION_UP) {
			}
			if (LivePaintingsWallpaper.this.painting != null) {
				LivePaintingsWallpaper.this.painting.updateEyesOffset(eventX, eventY);
			}
		}

		public void look(int eventX, int eventY) {
		}

		private Camera camera;

		/*
		 * Draw one frame of the animation. This method gets called repeatedly
		 * by posting a delayed Runnable. You can do any drawing you want in
		 * here. This example draws a wireframe cube.
		 */
		void drawFrame() {
			final SurfaceHolder holder = getSurfaceHolder();

			Canvas c = null;
			try {
				c = holder.lockCanvas();
				if (c != null) {
					c.drawColor(Color.BLACK);
					if (LivePaintingsWallpaper.this.painting != null) {
						LivePaintingsWallpaper.this.painting.display(c, paint);
					}

					int y = c.getHeight() / 2;
					if (isLoading) {
						String message = getString(R.string.loading);
						this.paint.setColor(Color.WHITE);
						this.paint.setTypeface(tf);
						this.paint.setTextSize(30);
						c.drawText(
								message,
								(c.getWidth() - paint.measureText(message)) / 2,
								y, this.paint);
						y += 20;
					}

					// if (this.photo != null) {
					// c.drawBitmap(this.photo, 0, 0, paint);
					// }
					// this.paint.setColor(Color.RED);
					// if (this.leftEyePoint != null) {
					// c.drawCircle(this.leftEyePoint.x, this.leftEyePoint.y,
					// 2.0f, this.paint);
					// }
					// if (this.rightEyePoint != null) {
					// c.drawCircle(this.rightEyePoint.x,
					// this.rightEyePoint.y, 2.0f, this.paint);
					// }
					//
					// this.paint.setColor(Color.BLUE);
					// if (midPoint != null) {
					// c.drawCircle(this.midPoint.x,
					// this.midPoint.y, 2.0f, this.paint);
					// }
				}
			} finally {
				if (c != null) {
					holder.unlockCanvasAndPost(c);
				}
			}

			// Reschedule the next redraw
			LivePaintingsWallpaper.this.handler
					.removeCallbacks(this.mDrawCharacter);
			if (this.mVisible) {
				LivePaintingsWallpaper.this.handler.postDelayed(
						this.mDrawCharacter, 1000 / FRAME_RATE);
			}
		}

		// @Override
		// public void onFaceDetection(Face[] faces, Camera camera) {
		// if (faces.length > 0) {
		// Face face = faces[0];
		// this.leftEyePoint = face.leftEye;
		// this.rightEyePoint = face.rightEye;
		// Log.d(TAG,
		// "leftEye:"+this.leftEyePoint+", rightEyePoint:"+this.rightEyePoint);
		// }
		// }

	}
}
