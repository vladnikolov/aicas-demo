package com.aicas.fischertechnik.app.monitoring;

// COLORSENSOR, EJECTION, WHITE, RED, BLUE, MOTOR, COMPRESSOR, VALVES


public interface StatusConnector
{
    static final int STATUS_LIGHT_BARRIER_COLORSENSOR = 0;
    static final int STATUS_LIGHT_BARRIER_EJECTION = 1;
    static final int STATUS_MOTOR = 2;
    static final int STATUS_COMPRESSOR = 3;
    static final int OBJECTS_ON_TRACK = 4;
    static final int ERROR = 5;
    static final int MOTOR_COUNTER = 6;
    static final int DETECTED_COLOR = 7;
    static final int STATUS_VALVE_WHITE = 8;
    static final int STATUS_VALVE_RED = 9;
    static final int STATUS_VALVE_BLUE = 10;

//  not used now
//  static final int STATUS_LIGHT_BARRIER_WHITE = 7;
//  static final int STATUS_LIGHT_BARRIER_RED = 8;
//  static final int STATUS_LIGHT_BARRIER_BLUE = 9;

    boolean sendStatus(int id, int value);
}
