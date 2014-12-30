package asigbe.sensor;

import android.hardware.SensorEvent;

/**
 * This interface defines what function an algorithm detecting a specific movement must implement.
 * 
 * @author Delali Zigah
 */
public interface Movement {

    /** define when a shaking movement is detected **/
    public static final int SHAKING_MOVEMENT  = 0;

    /** define when a flinging movement is detecetd **/
    public static final int PITCHING_MOVEMENT = 1;
    
    /**
     * Indicates if the algorithm has detected the movement.
     */
    public boolean hasDetected(SensorEvent event);

    /**
     * Returns the list of sensors used by this algorithm.
     */
    public int[] getListSensors();
    
    /**
     * Returns the type of the movement.
     */
    public int getType();
}
