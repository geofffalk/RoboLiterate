/**
 * Copyright (C) 2013 Geoffrey Falk
 */

package org.maskmedia.roboliterate.robotcomms;


/**
 * Contract of methods for translating robot motor and sensor operations into bytecode
 */
public interface ByteTranslator {


    public byte[] getBeepMessage(int frequency, int duration);

    public byte[] getBeepMessage(int volume, int frequency, int duration);

    public byte[] getMotorMessage(Robot.Motor motor, int speed, int end, boolean sync);

    public byte[] getResetMessage(Robot.Motor motor);

    public byte[] getOutputStateMessage(Robot.Motor motor);

    public byte[] getInputValueMessage(Robot.Sensor sensor);

    public int getSensorReadingFromMessage(byte[] message, int readingType);

    public boolean validateStatusMessage(byte[] message, int commandGetInputValues);


    public int getTachoCountFromMessage(byte[] message);

    byte[] getInputTypeAndModeMessage(int i);

    byte[] getInputPercentMessage(Robot.Sensor sensor);
}
