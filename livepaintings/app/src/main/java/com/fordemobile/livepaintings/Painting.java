package com.fordemobile.livepaintings;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Point;
import android.graphics.Rect;
import android.util.Xml;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

public final class Painting {

	private static final String DESCRIPTION_XML = "description.xml";
	private static final String BACKGROUND_IMAGE = "background.png";
	private static final String PREVIEW_IMAGE = "preview.jpg";
	private List<Eyes> listEyes;
	private Bitmap background;
	private final String directoryPath;
	private int surfaceWidth;
	private int surfaceHeight;
	private float zoomFactor;
	private int xOffset;
	private int yOffset;
	private Rect destRect;
	private Rect srcRect;
	private Bitmap previewImage;
	private String name;
	private String date;
	private String artistName;
	private String artistNationality;
	private String artistPeriod;
	private boolean isLandscape;
	private boolean isForSale;
	private boolean mustRotate;
	private boolean eyesAbove;
	private int lastSampleSize;

	public Painting(String directoryPath, int surfaceWidth, int surfaceHeight) {
		this.directoryPath = directoryPath;
		this.listEyes = new ArrayList<Eyes>();
		readXML();
	}

	public String getDirectoryPath() {
		return this.directoryPath;
	}

	public void loadPreview() {
		recycle();

		this.previewImage = HardwareManager.getBitmap(this.directoryPath
				+ PREVIEW_IMAGE);
	}

	public void loadPainting() {
		recycle();

		if (this.eyesAbove) {
			Bitmap background = HardwareManager.getBitmap(this.directoryPath
					+ BACKGROUND_IMAGE, false, true);
			this.background = background;
		} else {
			Bitmap background = HardwareManager.getBitmap(this.directoryPath
					+ BACKGROUND_IMAGE, true, true);
//			this.background = Utils.addTransparency(background, Color.BLUE);
//			background.recycle();
			this.background = background;
		}
		lastSampleSize = HardwareManager.getLastSampleSize();
		// this.background = background;
		this.srcRect = new Rect(0, 0, this.background.getWidth(),
				this.background.getHeight());

		for (Eyes eyes : this.listEyes) {
			eyes.loadEye();
		}
	}

	public void recycle() {
		if (this.background != null) {
			this.background.recycle();
			this.background = null;
		}
		if (this.previewImage != null) {
			this.previewImage.recycle();
			this.previewImage = null;
		}
		for (Eyes eyes : this.listEyes) {
			eyes.recycle();
		}
	}

	private void readXML() {
		XmlPullParser parser = Xml.newPullParser();
		try {
			InputStream fIn = HardwareManager.openFile(this.directoryPath
					+ DESCRIPTION_XML);
			InputStreamReader isr = new InputStreamReader(fIn);

			// auto-detect the encoding from the stream
			parser.setInput(isr);
			int eventType = parser.getEventType();
			boolean done = false;
			int x = 0;
			int y = 0;
			int minX = 0;
			int minY = 0;
			int maxX = 0;
			int maxY = 0;
			String filename = "";
			String name = "";
			while (eventType != XmlPullParser.END_DOCUMENT && !done) {
				switch (eventType) {
				case XmlPullParser.START_DOCUMENT:
					break;
				case XmlPullParser.START_TAG:
					name = parser.getName();
					if (name.equalsIgnoreCase("eye")) {
						x = Integer.parseInt(parser.getAttributeValue(0));
						y = Integer.parseInt(parser.getAttributeValue(1));
						minX = Integer.parseInt(parser.getAttributeValue(2));
						minY = Integer.parseInt(parser.getAttributeValue(3));
						maxX = Integer.parseInt(parser.getAttributeValue(4));
						maxY = Integer.parseInt(parser.getAttributeValue(5));
					}
					break;
				case XmlPullParser.TEXT:
					if (name.equalsIgnoreCase("eye")) {
						filename = parser.getText();
					} else if (name.equalsIgnoreCase("name")) {
						this.name = parser.getText();
					} else if (name.equalsIgnoreCase("date")) {
						this.date = parser.getText();
					} else if (name.equalsIgnoreCase("artistName")) {
						this.artistName = parser.getText();
					} else if (name.equalsIgnoreCase("artistNationality")) {
						this.artistNationality = parser.getText();
					} else if (name.equalsIgnoreCase("artistPeriod")) {
						this.artistPeriod = parser.getText();
					} else if (name.equalsIgnoreCase("isLandscape")) {
						this.isLandscape = Boolean.parseBoolean(parser
								.getText());
					} else if (name.equalsIgnoreCase("isForSale")) {
						this.isForSale = Boolean.parseBoolean(parser.getText());
					} else if (name.equalsIgnoreCase("eyesAbove")) {
						this.eyesAbove = Boolean.parseBoolean(parser.getText());
					}
					break;
				case XmlPullParser.END_TAG:
					if (name.equalsIgnoreCase("eye")) {
						this.listEyes.add(new Eyes(directoryPath + filename,
								new Point(x, y), new Point(minX, minY),
								new Point(maxX, maxY)));
					}
					name = "";
					break;
				}
				eventType = parser.next();
			}
		} catch (FileNotFoundException e) {
		} catch (IOException e) {
		} catch (XmlPullParserException e) {
			e.printStackTrace();
		}
	}

	public void updateEyesOffset(int eventX, int eventY) {
		for (Eyes eyes : this.listEyes) {
			eyes.updateTouchXOffset(eventX, eventY);
		}
	}

	public void updateSurfaceDimension(int width, int height) {

		if (this.background != null) {
			int backgroundWidth = this.background.getWidth()*this.lastSampleSize;
			int backgroundHeight = this.background.getHeight()*this.lastSampleSize;
			this.mustRotate = (width - height)
					* (backgroundWidth - backgroundHeight) < 1;
			if (this.mustRotate) {
				this.surfaceWidth = height;
				this.surfaceHeight = width;
			} else {
				this.surfaceWidth = width;
				this.surfaceHeight = height;
			}
			float xFactor = (float) this.surfaceWidth / backgroundWidth;
			float yFactor = (float) this.surfaceHeight / backgroundHeight;
			this.zoomFactor = Math.max(xFactor, yFactor);
			int destWidth = (int) (backgroundWidth * this.zoomFactor);
			int destHeight = (int) (backgroundHeight * this.zoomFactor);

			this.xOffset = (width - destWidth) / 2;
			this.yOffset = (height - destHeight) / 2;

			this.destRect = new Rect(this.xOffset, this.yOffset, destWidth
					+ this.xOffset, destHeight + this.yOffset);

			for (Eyes eyes : this.listEyes) {
				eyes.updateSurfaceDimension(this.surfaceWidth,
						this.surfaceHeight, this.zoomFactor, this.xOffset,
						this.yOffset, this.mustRotate);
			}
		}
	}

	public void display(Canvas c, Paint p) {
		if (this.mustRotate) {
			c.rotate(90, c.getWidth() / 2, c.getHeight() / 2);
		}

        if (this.background != null) {
            if (this.eyesAbove) {
                c.drawBitmap(this.background, this.srcRect, this.destRect, p);
                for (Eyes eyes : this.listEyes) {
                    eyes.display(c, p);
                }
            } else {
                for (Eyes eyes : this.listEyes) {
                    eyes.display(c, p);
                }
                c.drawBitmap(this.background, this.srcRect, this.destRect, p);
            }
        }
	}

	public Bitmap getPreviewImage() {
		return this.previewImage;
	}

	public String getName() {
		return this.name;
	}

	public String getDate() {
		return this.date;
	}

	public String getArtistName() {
		return this.artistName;
	}

	public String getArtistNationality() {
		return this.artistNationality;
	}

	public String getArtistPeriod() {
		return this.artistPeriod;
	}

	public String getDescription() {
		return "<b>" + this.artistName + "</b><br/>" + this.artistNationality
				+ ", " + this.artistPeriod + "<br/><br/><b><i>" + this.name
				+ ", " + this.date + "</i></b>";
	}

	public boolean isLandscape() {
		return this.isLandscape;
	}

	public boolean isForSale() {
		return this.isForSale;
	}

}
