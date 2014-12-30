package com.fordemobile.livepaintings;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Point;
import android.graphics.Rect;

public final class Eyes {
	private Bitmap image;
	private final Point origin;
	private final Point minPoint;
	private final Point maxPoint;
	private Point currentPosition;
	private Point centerPoint;
	private int touchX;
	private int touchY;
	private int surfaceWidth;
	private int surfaceHeight;
	private static final String TAG = "Eyes";
	private float zoomFactor;
	private int xOffset;
	private int yOffset;
	private Rect srcRect;
	private Rect destRect;
	private final String filePath;
	private boolean mustRotate;
	private Matrix rotationMatrix;

	public Eyes(String filePath, Point origin, Point minPoint, Point maxPoint) {
		this.filePath = filePath;
		this.origin = origin;
		this.currentPosition = origin;
		this.minPoint = minPoint;
		this.maxPoint = maxPoint;
		this.touchX = -1;
		this.touchY = -1;
		this.rotationMatrix = new Matrix();
	}

	public void loadEye() {
		this.image = HardwareManager.getBitmap(filePath);
		this.centerPoint = new Point(
				(this.origin.x + this.image.getWidth()) / 2,
				(this.origin.y + this.image.getHeight()) / 2);
		this.srcRect = new Rect(0, 0, this.image.getWidth(),
				this.image.getHeight());
		this.destRect = new Rect(this.origin.x, this.origin.y, this.origin.x
				+ this.image.getWidth(), this.origin.y + this.image.getHeight());
	}

	public void updateSurfaceDimension(int width, int height, float zoomFactor,
			int xOffset, int yOffset, boolean mustRotate) {
		this.surfaceWidth = width;
		this.surfaceHeight = height;
		this.zoomFactor = zoomFactor;
		this.xOffset = xOffset;
		this.yOffset = yOffset;
		this.mustRotate = mustRotate;

		updateTouchXOffset(this.touchX, this.touchY);
	}

	public void updateTouchXOffset(int touchX, int touchY) {
		if (touchX == -1 || touchY == -1) {
			this.currentPosition = new Point(this.origin);
		} else {
			if (this.mustRotate) {
//				float[] src = { touchX, touchY };
//				float[] dist = new float[2];
//				this.rotationMatrix.mapPoints(dist, src);
//				int rotateX = Math.abs((int) dist[0]);
//				int rotateY = this.surfaceWidth-Math.abs((int) dist[1]);
//				Log.d(TAG, "x1:" + touchX + ", y1:" + touchY);
//				Log.d(TAG, "x2:" + touchY + ", y2:" + (this.surfaceHeight-touchX));
//				this.rotationMatrix.setRotate(-90, 0, 0);
				this.touchX = touchY ;
				this.touchY = this.surfaceHeight-touchX;
//				Log.d(TAG, "this.touchX:" + this.touchX + ", this.touchY:" + this.touchY);
			} else {
				this.touchX = (int) touchX;
				this.touchY = (int) touchY;
			}
			if (this.image != null) {
//				Log.d(TAG, "touchX:" + this.touchX + ", touchY:" + this.touchY);
//				Log.d(TAG, "this.centerPoint.x:" + this.centerPoint.x+ ", this.touchX:" + this.touchX);
				Point centerPoint = new Point((int)(this.xOffset + this.centerPoint.x*this.zoomFactor), (int)(this.yOffset + this.centerPoint.y*this.zoomFactor));
				if (this.touchX < centerPoint.x) {
					// has touched on the left side of the eye
					int touchDistance = centerPoint.x - this.touchX;
					int imageDistance = this.origin.x - this.minPoint.x;
					int computedDistance = (touchDistance * imageDistance)
							/ centerPoint.x;
//					 Log.d(TAG, "inftouchDistancex:" + touchDistance
//					 + ", imageDistance:" + imageDistance
//					 + ", this.origin.x:" + this.origin.x
//					 + ", this.minPoint.x:" + this.minPoint.x
//					 + ", computedDistance:" + computedDistance);

					this.currentPosition.x = this.origin.x - computedDistance;
				} else if (this.touchX > centerPoint.x) {
					// has touched on the right side of the eye
					int touchDistance = this.touchX - centerPoint.x;
					int imageDistance = this.maxPoint.x - this.origin.x;
                    if ((this.surfaceWidth - centerPoint.x) != 0) {
                        int computedDistance = (touchDistance * imageDistance)
                                / (this.surfaceWidth - centerPoint.x);

                        this.currentPosition.x = this.origin.x + computedDistance;
                    }
//					Log.d(TAG, "suptouchDistancex:" + touchDistance);
				}

//				Log.d(TAG, "this.centerPoint.y:" + this.centerPoint.y+ ", this.touchY:" + this.touchY);
				if (this.touchY < centerPoint.y) {
					// has touched above the eye
					int touchDistance = centerPoint.y - this.touchY;
					int imageDistance = this.origin.y - this.minPoint.y;
					int computedDistance = (touchDistance * imageDistance)
							/ centerPoint.y;

					this.currentPosition.y = this.origin.y - computedDistance;
//					Log.d(TAG, "touchDistancey:" + touchDistance);

				} else if (this.touchY > centerPoint.y) {
					// has touched below the eye
					int touchDistance = this.touchY - centerPoint.y;
					int imageDistance = this.maxPoint.y - this.origin.y;
                    if ((this.surfaceHeight - centerPoint.x) != 0) {
                        int computedDistance = (touchDistance * imageDistance)
                                / (this.surfaceHeight - centerPoint.x);

                        this.currentPosition.y = this.origin.y + computedDistance;
                    }
//					Log.d(TAG, "touchDistancey:" + touchDistance);
				}
			}
		}

		if (this.image != null) {
			int destWidth = (int) (this.image.getWidth() * this.zoomFactor);
			int destHeight = (int) (this.image.getHeight() * this.zoomFactor);
			int destX = (int) (this.xOffset + this.currentPosition.x
					* this.zoomFactor);
			int destY = (int) (this.yOffset + this.currentPosition.y
					* this.zoomFactor);
			this.destRect = new Rect(destX, destY, destWidth + destX,
					destHeight + destY);
		}
		// Log.d(TAG, "touchX:" + touchX);
		// Log.d(TAG, "touchY:" + touchY);
//		Log.d(TAG, "this.xOffset + this.currentPosition.x:"
//				+ (this.xOffset + this.currentPosition.x));
//		Log.d(TAG, "this.yOffset + this.currentPosition.y:"
//				+ (this.yOffset + this.currentPosition.y));
	}

	public void recycle() {
		if (this.image != null) {
			this.image.recycle();
			this.image = null;
		}
	}

	public void display(Canvas c, Paint p) {
		if (this.image != null) {
			c.drawBitmap(this.image, this.srcRect, this.destRect, p);
		}
	}
}
