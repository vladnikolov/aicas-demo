package com.aicas.jamaica.iot.demo.colorsorting.guiclient.model.impl;

import java.util.HashMap;
import java.util.Map;

import com.aicas.jamaica.iot.demo.colorsorting.guiclient.model.ColorSortingGUIModel;

public class XMPPColorSortingGUIModelImpl implements ColorSortingGUIModel
{
  public final XMPPInImpl in;
  public final XMPPOutImpl out;

  public XMPPColorSortingGUIModelImpl()
  {
    in = new XMPPInImpl();
    out = new XMPPOutImpl();
  }

  public static class XMPPInImpl implements In
  {
    public final XMPPMotorImpl motor;
    public final XMPPLightBarriersImpl lightBarriers;
    public final XMPPValvesImpl valves;
    public final XMPPColorSensorImpl colorSensor;

    public XMPPInImpl()
    {
      motor = new XMPPMotorImpl();
      lightBarriers = new XMPPLightBarriersImpl();
      colorSensor = new XMPPColorSensorImpl();
      valves = new XMPPValvesImpl();
    }

    public static class XMPPMotorImpl implements In.Motor
    {
      public boolean rotating = false;
      public int direction;
      public int speed;
      public int distance;
      public int counter;

      @Override
      public boolean isRotating()
      {
        return rotating;
      }

      @Override
      public int getDirection()
      {
        return direction;
      }

      @Override
      public int getSpeed()
      {
        return speed;
      }

      @Override
      public int getDistance()
      {
        return distance;
      }

      @Override
      public int getCounter()
      {
        return counter;
      }
    }

    public static class XMPPLightBarrierImpl implements LightBarrier
    {
      public boolean intercepted = false;

      @Override
      public boolean isIntercepted()
      {
        return intercepted;
      }
    }

    public static class XMPPLightBarriersImpl
      implements LightBarriers
    {
      public final Map<LightBarrierId, XMPPLightBarrierImpl> map =
        new HashMap<>();

      XMPPLightBarriersImpl()
      {
        map.put(LightBarrierId.COLORSENSOR, new XMPPLightBarrierImpl());
        map.put(LightBarrierId.EJECTION, new XMPPLightBarrierImpl());
        map.put(LightBarrierId.WHITE, new XMPPLightBarrierImpl());
        map.put(LightBarrierId.RED, new XMPPLightBarrierImpl());
        map.put(LightBarrierId.BLUE, new XMPPLightBarrierImpl());
      }

      @Override
      public LightBarrier get(LightBarrierId lightBarrierId)
      {
        return map.get(lightBarrierId);
      }
    }

    public static class XMPPColorSensorImpl implements ColorSensor
    {
      public int colorValue = 0;

      @Override
      public int getColorValue()
      {
        return colorValue;
      }
    }

    public static class XMPPValveImpl implements In.Valve
    {
      public boolean active = false;

      @Override
      public boolean isActive()
      {
        return active;
      }
    }

    public static class XMPPValvesImpl implements In.Valves
    {
      public final Map<ValveId, XMPPValveImpl> map = new HashMap<>();

      XMPPValvesImpl()
      {
        map.put(ValveId.WHITE, new XMPPValveImpl());
        map.put(ValveId.RED, new XMPPValveImpl());
        map.put(ValveId.BLUE, new XMPPValveImpl());
      }

      @Override
      public Valve get(ValveId valveId)
      {
        return map.get(valveId);
      }
    }

    @Override
    public In.Motor getMotor()
    {
      return motor;
    }

    @Override
    public In.LightBarriers getLightBarriers()
    {
      return lightBarriers;
    }

    @Override
    public In.ColorSensor getColorSensor()
    {
      return colorSensor;
    }

    @Override
    public Valves getValves()
    {
      return valves;
    }
  }

  @Override
  public In getIn()
  {
    return in;
  }

  public static class XMPPOutImpl implements Out
  {
    private final Motor motor;
    private final Valves valves;
    private final Compressor compressor;

    XMPPOutImpl()
    {
      motor = new MotorImpl();
      valves = new ValvesImpl();
      compressor = new CompressorImpl();
    }

    public static class MotorImpl implements Out.Motor
    {
      @Override
      public void rotate()
      {
        System.out.println(this + " rotate().");
      }

      @Override
      public void rotate(int direction, int speed, int distance)
      {
        System.out.println(this + " rotate(");
        System.out.println("direction = " + direction);
        System.out.println("speed = " + speed);
        System.out.println("distance = " + distance);
        System.out.println(this + ").");
      }

      @Override
      public void stop()
      {
        System.out.println(this + " stop().");
      }
    }

    public static class CompressorImpl implements Out.Compressor
    {

      @Override
      public void activate()
      {
        System.out.println(this + " activate().");
      }

      @Override
      public void stop()
      {
        System.out.println(this + " stop().");
      }

    }

    public static class ValveImpl implements Out.Valve
    {
      @Override
      public void activate()
      {
        System.out.println(this + " activate().");
      }
    }

    public static class ValvesImpl implements Valves
    {
      private final Map<ValveId, Valve> map = new HashMap<>();

      ValvesImpl()
      {
        map.put(ValveId.WHITE, new ValveImpl());
        map.put(ValveId.RED, new ValveImpl());
        map.put(ValveId.BLUE, new ValveImpl());
      }

      @Override
      public Valve get(ValveId valveId)
      {
        return map.get(valveId);
      }
    }

    @Override
    public Motor getMotor()
    {
      return motor;
    }

    @Override
    public Compressor getCompressor()
    {
      return compressor;
    }

    @Override
    public Valves getValves()
    {
      return valves;
    }
  }

  @Override
  public Out getOut()
  {
    return out;
  }
}
