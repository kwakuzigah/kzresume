package asigbe.sensor;

/**
 * A listener which is used to listen to the phone movements.
 * 
 * @author Delali Zigah
 */
public interface OnMovementListener {

    /**
     * This function is called when a movement is detected.
     */
    public void movementDetected(int type);
}
