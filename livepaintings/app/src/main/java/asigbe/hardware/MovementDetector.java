package asigbe.hardware;

import java.util.List;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;

import asigbe.sensor.Movement;
import asigbe.sensor.OnMovementListener;

/**
 * This class defines a movement detector.
 * 
 * @author Delali Zigah
 */
public class MovementDetector implements SensorEventListener {
    private final Context      context;
    private SensorManager      sensorMgr;
    private OnMovementListener onMovementListener;
    private final Movement     movement;

    /**
     * Creates a shake listener and begin listening.
     */
    public MovementDetector(Context context, Movement movement) {
	this.context = context;
	this.movement = movement;
    }

    /**
     * Resumes the shake detector.
     */
    public void resume() {
	this.sensorMgr = (SensorManager) this.context
	        .getSystemService(Context.SENSOR_SERVICE);

	if (this.sensorMgr == null) {
	    return;
	}

	for (int sensorType : movement.getListSensors()) {
	    List<Sensor> sensorList = this.sensorMgr.getSensorList(sensorType);
	    if (sensorList.size() > 0) {
		Sensor sensor = sensorList.get(0);
		if (sensor != null) {
		    boolean supported = this.sensorMgr.registerListener(this,
			    sensor, SensorManager.SENSOR_DELAY_GAME);
		    if (!supported) {
			this.sensorMgr.unregisterListener(this);
		    }
		}
	    }
	}
    }

    /**
     * Pauses the shake detector.
     */
    public void pause() {
	if (this.sensorMgr != null) {
	    this.sensorMgr.unregisterListener(this);
	    this.sensorMgr = null;
	}
    }

    @Override
    public void onAccuracyChanged(Sensor arg0, int arg1) {

    }

    @Override
    public void onSensorChanged(SensorEvent event) {
	if (this.movement.hasDetected(event)) {
	    fireMovementDetected(this.movement.getType());
	}
    }

    /**
     * Adds a listener on a shake event.
     */
    public void setOnMovementListener(OnMovementListener listener) {
	this.onMovementListener = listener;
    }

    void fireMovementDetected(int type) {
	if (this.onMovementListener != null) {
	    this.onMovementListener.movementDetected(type);
	}
    }
}
