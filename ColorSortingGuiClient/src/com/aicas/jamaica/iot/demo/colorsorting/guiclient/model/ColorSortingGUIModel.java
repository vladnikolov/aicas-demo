package com.aicas.jamaica.iot.demo.colorsorting.guiclient.model;

public interface ColorSortingGUIModel
{
  public enum ValveId {
    WHITE, RED, BLUE
  }

  public interface In
  {
    public enum SensorId {
      MOTOR, LIGHTBARRIERS, VALVES
    }

    public interface Motor {
      boolean isRotating();
      int getDirection();
      int getSpeed();
      int getDistance();
      int getCounter();
    }

    Motor getMotor();

    public interface LightBarrier {
      boolean isIntercepted();
    }

    public enum LightBarrierId {
      COLORSENSOR, EJECTION, WHITE, RED, BLUE
    }

    public interface LightBarriers {
      LightBarrier get(LightBarrierId lightBarrierId);
    }

    LightBarriers getLightBarriers();

    public interface ColorSensor {
      int getColorValue();
    }

    ColorSensor getColorSensor();

    public interface Valve {
      boolean isActive();
    }

    public interface Valves {
      Valve get(ValveId valveId);
    }

    Valves getValves();
  }

  In getIn();

  public interface Out
  {
    public interface Motor {
      void rotate();
      void rotate(int direction, int speed, int distance);
      void stop();
    }

    Motor getMotor();

    public interface Compressor {
      void activate();
      void stop();
    }

    Compressor getCompressor();

    public interface Valve {
      void activate();
    }

    public interface Valves {
      Valve get(ValveId valveId);
    }

    Valves getValves();
  }

  Out getOut();

}
