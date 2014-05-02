/**
 *
 * Copyright (C) 2013 Geoffrey Falk
 *
 */

package org.maskmedia.roboliterate.robotcomms;

public interface RobotListener {

    void startListening();

    int getScaledReading();

    void endListening();
}
