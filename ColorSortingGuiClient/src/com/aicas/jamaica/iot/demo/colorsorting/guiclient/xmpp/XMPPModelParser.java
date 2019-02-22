package com.aicas.jamaica.iot.demo.colorsorting.guiclient.xmpp;

import com.aicas.jamaica.iot.demo.colorsorting.guiclient.model.ColorSortingGUIModel.In.LightBarrierId;
import com.aicas.jamaica.iot.demo.colorsorting.guiclient.model.ColorSortingGUIModel.ValveId;
import com.aicas.jamaica.iot.demo.colorsorting.guiclient.model.impl.XMPPColorSortingGUIModelImpl;

public class XMPPModelParser
{

  private final XMPPColorSortingGUIModelImpl model;

  public XMPPModelParser(XMPPColorSortingGUIModelImpl model)
  {
    this.model = model;
  }

  public void parse(String string)
  {
    if (null != string)
      {
        String bString = string.trim();
        int ix = bString.indexOf(":");
        if (ix > 0)
          {
            String key = bString.substring(0, ix).trim();
            System.out.println("key = " + key);
            String value = bString.substring(ix + 1, bString.length()).trim();
            System.out.println("value = " + value);

            switch (key)
              {
              case "Motor.Rotating":
                model.in.motor.rotating = Boolean.parseBoolean(value);
                break;
              case "Motor.Direction":
                model.in.motor.direction = Integer.parseInt(value);
                break;
              case "Motor.Speed":
                model.in.motor.speed = Integer.parseInt(value);
                break;
              case "Motor.Distance":
                model.in.motor.distance = Integer.parseInt(value);
                break;
              case "Motor.Counter":
                model.in.motor.counter = Integer.parseInt(value);
                break;
              case "LightBarrier.ColorSensor":
                model.in.lightBarriers.map
                    .get(LightBarrierId.COLORSENSOR).intercepted =
                      Boolean.parseBoolean(value);
                break;
              case "LightBarrier.Ejection":
                model.in.lightBarriers.map
                    .get(LightBarrierId.EJECTION).intercepted =
                      Boolean.parseBoolean(value);
                break;
              case "ColorSensor.Color":
                model.in.colorSensor.colorValue = Integer.parseInt(value);
                break;
              case "Valve1.Active":
                model.in.valves.map.get(ValveId.WHITE).active = Boolean.parseBoolean(value);
                break;
              case "Valve2.Active":
                model.in.valves.map.get(ValveId.RED).active = Boolean.parseBoolean(value);
                break;
              case "Valve3.Active":
                model.in.valves.map.get(ValveId.BLUE).active = Boolean.parseBoolean(value);
                break;
              default:
                // nothing
              }
          }
      }
  }

}
