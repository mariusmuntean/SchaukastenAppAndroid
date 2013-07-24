package de.tum.os.models;

/**
 * Implement this to be informed about gestures like hand up/down/left/right.
 * Created by Marius on 7/24/13.
 */
public interface IGestureConsumer {

    /**
     * Consumes a certain gesture as declared in {@link GenericGestures}
     *
     * @param genericGesture - Gesture to be consumed/interpreted/acted upon.
     */
    public void Consume(GenericGestures genericGesture);
}
