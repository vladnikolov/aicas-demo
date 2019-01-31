package com.aicas.fischertechnik.app.monitoring;

// COLORSENSOR, EJECTION, WHITE, RED, BLUE, MOTOR, COMPRESSOR, VALVES


public interface StatusConnector
{
    static final int STATUS_LIGHT_BARRIER_COLORSENSOR = 1;
    static final int STATUS_LIGHT_BARRIER_EJECTION = 2;
    static final int STATUS_MOTOR = 3;
    static final int STATUS_COMPRESSOR = 4;
    static final int OBJECT_ON_TRACK = 5;
    static final int ERROR = 6;
//  not used now
//  static final int STATUS_LIGHT_BARRIER_WHITE = 7;
//  static final int STATUS_LIGHT_BARRIER_RED = 8;
//  static final int STATUS_LIGHT_BARRIER_BLUE = 9;
    
    boolean sendStatus(int id, int value);
}
