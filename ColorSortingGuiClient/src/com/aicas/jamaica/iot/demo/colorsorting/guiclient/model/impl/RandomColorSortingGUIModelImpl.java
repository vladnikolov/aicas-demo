package com.aicas.jamaica.iot.demo.colorsorting.guiclient.model.impl;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;

import com.aicas.jamaica.iot.demo.colorsorting.guiclient.model.ColorSortingGUIModel;

public class RandomColorSortingGUIModelImpl implements ColorSortingGUIModel
{
  static final Random random = new Random();
  static int counter = 0;

  private final In in;
  private final Out out;

  public RandomColorSortingGUIModelImpl()
  {
    in = new InImpl();
    out = new OutImpl();
  }

  public static class InImpl implements In
  {
    private Motor motor;
    private LightBarriers lightBarriers;
    private ColorSensor colorSensor;
    private Valves valves;

    public InImpl()
    {
      motor = new MotorImpl();
      lightBarriers = new LightBarriersImpl();
      colorSensor = new ColorSensorImpl();
      valves = new ValvesImpl();
    }

    public static class MotorImpl implements In.Motor
    {
      @Override
      public boolean isRotating()
      {
        return random.nextBoolean();
      }

      @Override
      public int getDirection()
      {
        return random.nextInt();
      }

      @Override
      public int getSpeed()
      {
        return random.nextInt();
      }

      @Override
      public int getDistance()
      {
        return random.nextInt();
      }

      @Override
      public int getCounter()
      {
        return counter++;
      }
    }

    public static class LightBarrierImpl implements LightBarrier
    {
      @Override
      public boolean isIntercepted()
      {
        return random.nextBoolean();
      }
    }

    public static class LightBarriersImpl
      implements LightBarriers
    {
      private final Map<LightBarrierId, LightBarrier> map = new HashMap<>();

      LightBarriersImpl()
      {
        map.put(LightBarrierId.COLORSENSOR, new LightBarrierImpl());
        map.put(LightBarrierId.EJECTION, new LightBarrierImpl());
        map.put(LightBarrierId.WHITE, new LightBarrierImpl());
        map.put(LightBarrierId.RED, new LightBarrierImpl());
        map.put(LightBarrierId.BLUE, new LightBarrierImpl());
      }

      @Override
      public LightBarrier get(LightBarrierId lightBarrierId)
      {
        return map.get(lightBarrierId);
      }
    }

    public static class ColorSensorImpl implements ColorSensor
    {
      @Override
      public int getColorValue()
      {
        return random.nextInt();
      }
    }

    public static class ValveImpl implements In.Valve
    {
      @Override
      public boolean isActive()
      {
        return random.nextBoolean();
      }
    }

    public static class ValvesImpl implements In.Valves
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

  public static class OutImpl implements Out
  {
    private final Motor motor;
    private final Valves valves;
    private final Compressor compressor;

    OutImpl()
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
        System.out.println(  "direction = " + direction);
        System.out.println(  "speed = " + speed);
        System.out.println(  "distance = " + distance);
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
