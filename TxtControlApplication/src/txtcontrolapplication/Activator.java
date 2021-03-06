package txtcontrolapplication;

import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;

import com.aicas.fischertechnik.driver.AicasTxtDriverInterface;
import com.aicas.fischertechnik.driver.AicasTxtDriverInterface.LightBarrier;
import com.aicas.fischertechnik.driver.AicasTxtDriverInterface.Valve;

public class Activator implements BundleActivator
{
  private static BundleContext context;
  ColorIdentification colorIdentification = new ColorIdentification();
  ServiceReference<AicasTxtDriverInterface> driverServiceRef;
  AicasTxtDriverInterface driverService;

  private boolean run = true;

  private int maxWhiteRange_ = 1390;
  private int maxRedRange_ = 1600 ;
  private int motorCounter;
  private int motorTarget = 0;

  static BundleContext getContext()
  {
    return context;
  }

  /*
   * (non-Javadoc)
   *
   * @see
   * org.osgi.framework.BundleActivator#start(org.osgi.framework.BundleContext)
   */
  @Override
  public void start(BundleContext bundleContext) throws Exception
  {
    Activator.context = bundleContext;

    System.out.println("AicasTxtApplication: starting");

    System.out.println("AicasTxtApplication: querying TXT driver service");

    Thread.sleep(3000);

    driverServiceRef = context.getServiceReference(AicasTxtDriverInterface.class);
    driverService = context.getService(driverServiceRef);

    System.out.println("AicasTxtApplication: TXT driver service instantiated");
    System.out.println("System Initialized...");

    new Thread()
    {
      @Override
      public void run()
      {
        while (run)
          {
            //Wait for 100ms
            try
            {
              Thread.sleep(100);
            }
            catch (InterruptedException e1)
            {
              e1.printStackTrace();
            }

            //Initialize motor

            if (driverService.getLightBarrierState(LightBarrier.COLORSENSOR) == true)
              {
                //start motor
                //driverService.rotateMotor(1, 1, 20, 10);
                System.out.println("Starting Motor M1");
                driverService.rotateMotor(1, 1, 512, 0);

                //Check for the block to reach color identification block
                //Wait for 50ms to get the colour range
                try
                {
                  Thread.sleep(50);
                }
                catch (InterruptedException e)
                {
                  // TODO Auto-generated catch block
                  e.printStackTrace();
                }
                int colorSensorValue = driverService.getColorSensorValue();

                while(colorSensorValue > 511)
                  {
                    if (colorSensorValue <= maxWhiteRange_ )
                      {
                        System.out.println("White block detected");
                        activateCompressor();
                        whiteBlockCounter();
                        colorSensorValue = 0;

                      }else if (colorSensorValue > maxWhiteRange_ && colorSensorValue <= maxRedRange_)
                        {
                          System.out.println("Red block detected");
                          activateCompressor();
                          redBlockCounter();
                          colorSensorValue = 0;


                        }else
                          {
                            System.out.println("blue block detected");
                            activateCompressor();
                            blueBlockCounter();
                            colorSensorValue = 0;
                          }
                  }
              }

            try
            {
              Thread.sleep(1000);
            } catch (InterruptedException e)
            {
              e.printStackTrace();
            }

            System.out.println("ControllApplication: Getting state of LightBarrier @ ColorSensor: "
                + driverService.getLightBarrierState(LightBarrier.COLORSENSOR));
            System.out.println("ControllApplication: Getting state of LightBarrier @ Ejection   : "
                + driverService.getLightBarrierState(LightBarrier.EJECTION));
            System.out.println("ControllApplication: Getting state of LightBarrier @ White      : "
                + driverService.getLightBarrierState(LightBarrier.WHITE));
            System.out.println("ControllApplication: Getting state of LightBarrier @ Red        : "
                + driverService.getLightBarrierState(LightBarrier.RED));
            System.out.println("ControllApplication: Getting state of LightBarrier @ Blue       : "
                + driverService.getLightBarrierState(LightBarrier.BLUE));

            System.out.println("ControllApplication: Getting value of color sensor @ I2: "
                + driverService.getColorSensorValue());
          }
      }

    }.start();
  }

  /*
   * (non-Javadoc)
   *
   * @see
   * org.osgi.framework.BundleActivator#stop(org.osgi.framework.BundleContext)
   */
  @Override
  public void stop(BundleContext bundleContext) throws Exception
  {
    Activator.context = null;
    run = false;
  }

  public void activateCompressor()
  {
    if (driverService.getColorSensorValue() > 511 && driverService.getLightBarrierState(LightBarrier.EJECTION) == true)
      {
        System.out.println("Activating Compressor");
        driverService.activateCompressor();
        //        startCounting();
        try
        {
          Thread.sleep(1000);
        }
        catch (InterruptedException e)
        {
          // TODO Auto-generated catch block
          e.printStackTrace();
        }
      }
  }

  private void whiteBlockCounter()
  {
    do
      {
        motorCounter = driverService.getMotorCounter();
        motorTarget = motorTarget + 1 ;
        System.out.println("Counting Motor steps: " + motorTarget);
      }while(motorTarget < 6);
    driverService.resetMotorCounter();
    ejectWhiteBlock();
    motorTarget = 0;
  };


  private void redBlockCounter()
  {
    do
      {
        motorCounter = driverService.getMotorCounter();
        motorTarget = motorTarget + 1 ;
        System.out.println("Counting Motor steps: " + motorTarget);
      }while(motorTarget > 15);
    driverService.resetMotorCounter();
    ejectRedBlock();
    motorTarget = 0;

  };



  private void blueBlockCounter()
  {
    do
      {
        motorCounter = driverService.getMotorCounter();
        motorTarget = motorTarget + 1 ;
        System.out.println("Counting Motor steps: " + motorTarget);
      }while(motorTarget < 27);
    driverService.resetMotorCounter();
    ejectBlueBlock();
    motorTarget = 0;
  };

  private void ejectWhiteBlock()
  {
    System.out.println("Ejecting WHITE block");
    driverService.activateValve(Valve.WHITE);
    driverService.stopMotor(1);
  }

  private void ejectRedBlock()
  {
    System.out.println("Ejecting RED block");
    driverService.activateValve(Valve.RED);
    driverService.stopMotor(1);

  }

  private void ejectBlueBlock()
  {
    System.out.println("Ejecting BLUE block");
    driverService.activateValve(Valve.BLUE);
    driverService.stopMotor(1);
  }

}
