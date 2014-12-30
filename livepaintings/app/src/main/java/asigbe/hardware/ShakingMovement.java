package asigbe.hardware;

import asigbe.sensor.Movement;

import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorManager;

/**
 * Creates a detector which detects when the phone is shaken.
 * 
 * @author Delali Zigah
 */
public class ShakingMovement implements Movement {

    private static final int FORCE_MAG          = 20;
    private static final int TIME_THRESHOLD     = 100;
    private static final int SHAKE_TIMEOUT      = 1500;
    private static final int SHAKE_DURATION     = 300;
    private static final int SHAKE_INTERVAL     = 1000;
    private static final int SHAKE_COUNT        = 10;
    private static int[]     sensorsType        = new int[] {
	    Sensor.TYPE_ACCELEROMETER, Sensor.TYPE_ORIENTATION };

    private long             lastTime           = 0;
    private int              shakeCount         = 0;
    private long             firstShake         = 0;
    private long             lastShake          = 0;
    private long             lastForce          = 0;
    private int              countOrientation   = 0;
    private float            speed              = 0.0f;

    private float            lastXAverage       = 0.0f;
    private float            lastYAverage       = 0.0f;
    private float            lastZAverage       = 0.0f;

    private float            lastXMagAverage    = 0.0f;
    private float            lastYMagAverage    = 0.0f;
    private float            lastZMagAverage    = 0.0f;
    private float            mag                = 0.0f;
    private int              countAccelerometer = 0;
    private float            currentXAverage    = 0.0f;
    private float            currentYAverage    = 0.0f;
    private float            currentZAverage    = 0.0f;
    private float            currentXMagAverage = 0.0f;
    private float            currentYMagAverage = 0.0f;
    private float            currentZMagAverage = 0.0f;
    private boolean          firstTime          = true;

    @Override
    public boolean hasDetected(SensorEvent event) {
	long now = System.currentTimeMillis();
	if ((now - this.lastForce) > SHAKE_TIMEOUT) {
	    this.shakeCount = 0;
	}

	if (event.sensor.getType() == Sensor.TYPE_ACCELEROMETER) {
	    this.currentXAverage += event.values[SensorManager.DATA_X];
	    this.currentYAverage += event.values[SensorManager.DATA_Y];
	    this.currentZAverage += event.values[SensorManager.DATA_Z];
	    this.countAccelerometer++;
	} else if (event.sensor.getType() == Sensor.TYPE_ORIENTATION) {
	    this.currentXMagAverage += event.values[SensorManager.DATA_X];
	    this.currentYMagAverage += event.values[SensorManager.DATA_Y];
	    this.currentZMagAverage += event.values[SensorManager.DATA_Z];
	    this.countOrientation++;
	}

	if ((now - this.lastTime) > TIME_THRESHOLD) {
	    this.currentXAverage = this.currentXAverage
		    / this.countAccelerometer;
	    this.currentYAverage = this.currentYAverage
		    / this.countAccelerometer;
	    this.currentZAverage = this.currentZAverage
		    / this.countAccelerometer;

	    this.currentXMagAverage = this.currentXMagAverage
		    / this.countOrientation;
	    this.currentYMagAverage = this.currentYMagAverage
		    / this.countOrientation;
	    this.currentZMagAverage = this.currentZMagAverage
		    / this.countOrientation;

	    if (!this.firstTime) {
		this.speed = Math
		        .abs((this.lastXAverage - this.currentXAverage)
		                + (this.lastYAverage - this.currentYAverage)
		                + (this.lastZAverage - this.currentZAverage));
		this.mag = Math
		        .abs((this.lastXMagAverage - this.currentXMagAverage)
		                + (this.lastYMagAverage - this.currentYMagAverage)
		                + (this.lastZMagAverage - this.currentZMagAverage));
		// System.out.println("this.speed " + this.speed);
		// System.out.println("this.mag " + this.mag);

		if (!Float.isNaN(this.speed) && !Float.isNaN(this.mag)
		        && this.mag > FORCE_MAG) {
		    if ((this.lastXAverage * this.currentXAverage < 0)
			    || (this.lastYAverage * this.currentYAverage < 0)
			    || (this.lastZAverage * this.currentZAverage < 0)) {
			if (this.shakeCount == 0) {
			    this.firstShake = now;
			}

			if ((++this.shakeCount >= SHAKE_COUNT)
			        && (now - this.firstShake > SHAKE_DURATION)
			        && ((now - this.lastShake) > SHAKE_INTERVAL)) {
			    this.lastShake = now;
			    this.shakeCount = 0;

			    return true;
			}
		    }
		    this.lastForce = now;
		}
	    }

	    this.firstTime = false;
	    this.lastTime = now;
	    this.lastXAverage = this.currentXAverage;
	    this.lastYAverage = this.currentYAverage;
	    this.lastZAverage = this.currentZAverage;
	    this.lastXMagAverage = this.currentXMagAverage;
	    this.lastYMagAverage = this.currentYMagAverage;
	    this.lastZMagAverage = this.currentZMagAverage;
	    this.currentXMagAverage = 0.0f;
	    this.currentYMagAverage = 0.0f;
	    this.currentZMagAverage = 0.0f;
	    this.currentXAverage = 0.0f;
	    this.currentYAverage = 0.0f;
	    this.currentZAverage = 0.0f;
	    this.countOrientation = 0;
	    this.countAccelerometer = 0;
	}
	return false;
    }

    @Override
    public int[] getListSensors() {
	return ShakingMovement.sensorsType;
    }

    @Override
    public int getType() {
	return Movement.SHAKING_MOVEMENT;
    }
}