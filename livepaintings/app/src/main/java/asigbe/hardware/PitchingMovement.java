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
public class PitchingMovement implements Movement {

    private static final int ORIENTATION_THRESHOLD = 80;
    private static final int PITCHING_TIMEOUT      = 1500;
    private static final int PITCHING_INTERVAL     = 500;
    private static int[]     sensorsType           = new int[] { Sensor.TYPE_ORIENTATION };

    private long             lastFling             = 0;

    private boolean          firstTime             = true;

    private long             movementStartTime     = 0;
    private float            lastAngleY            = 0.0f;
    private float            movementAngleY        = 0.0f;
    private boolean          hasGoneToRight        = false;

    @Override
    public boolean hasDetected(SensorEvent event) {
	long now = System.currentTimeMillis();

	if (event.sensor.getType() == Sensor.TYPE_ORIENTATION) {
	    float currentAngleX = event.values[SensorManager.DATA_Y];
	    if (this.firstTime) {
		this.lastAngleY = currentAngleX;
		this.movementAngleY = 0;
		this.hasGoneToRight = false;
		this.firstTime = false;
	    }

	    // if ((now - this.lastTime) > TIME_THRESHOLD) {
	    // System.out.println("x : " + event.values[SensorManager.DATA_X]
	    // + ", y : " + event.values[SensorManager.DATA_Y]
	    // + ", z : " + event.values[SensorManager.DATA_Z]);
	    if (Math.abs(currentAngleX - this.lastAngleY) <= 180) {
		if (!this.hasGoneToRight && this.lastAngleY >= currentAngleX) {
		    this.movementAngleY += this.lastAngleY - currentAngleX;
		    // System.out.println("movementAngleX : " +
		    // this.movementAngleX);
		    this.hasGoneToRight = this.movementAngleY >= ORIENTATION_THRESHOLD;
		    if (this.hasGoneToRight) {
			System.out.println("hasGoneToRight : "
			        + this.movementAngleY);
			this.movementAngleY = 0;
			this.lastAngleY = currentAngleX;
		    }
		}

		if (this.lastAngleY <= currentAngleX) {
		    if (this.hasGoneToRight) {
			this.movementAngleY += currentAngleX - this.lastAngleY;
			// System.out.println("this.movementAngleX "
			// + this.movementAngleX);

			if (this.movementAngleY >= ORIENTATION_THRESHOLD
			        && (now - this.lastFling) > PITCHING_INTERVAL) {
			    // movement detected
			    this.lastFling = now;
			    this.movementStartTime = now;
			    this.hasGoneToRight = false;
			    this.movementAngleY = 0;
			    System.out.println("movement detected : "
				    + this.movementAngleY);
			    return true;
			}
		    }
		}

		if (now - this.movementStartTime > PITCHING_TIMEOUT) {
		    this.movementStartTime = now;
		    this.hasGoneToRight = false;
		    this.movementAngleY = 0;
//		    System.out.println("reinit");
		}
		// System.out.println("currentAngle " + currentAngleX +
		// ", lastAngleX " + lastAngleX);

		if (this.movementAngleY == 0) {
		    this.lastAngleY = currentAngleX;
		} else {
		    if (!this.hasGoneToRight) {
			this.lastAngleY = Math.min(this.lastAngleY,
			        currentAngleX);
		    } else {
			this.lastAngleY = Math.max(this.lastAngleY,
			        currentAngleX);
		    }
		}
	    } else {
		this.lastAngleY = currentAngleX;
	    }

	}
	// }

	return false;
    }

    @Override
    public int[] getListSensors() {
	return PitchingMovement.sensorsType;
    }

    @Override
    public int getType() {
	return Movement.PITCHING_MOVEMENT;
    }
}