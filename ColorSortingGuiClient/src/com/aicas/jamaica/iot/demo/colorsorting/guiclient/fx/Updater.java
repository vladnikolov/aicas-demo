package com.aicas.jamaica.iot.demo.colorsorting.guiclient.fx;

import java.util.Collection;
import java.util.Timer;
import java.util.TimerTask;

import javafx.application.Platform;

public class Updater
{
  private final Timer timer;
  private final TimerTask timerTask;
  private final long period = 40L; // 41.67 = 24 Hz;

  Updater(Collection<Updateable> updateables)
  {
    timer = new Timer();
    timerTask = new TimerTask()
        {
          Runnable updateRunnable = new Runnable()
          {

            @Override
            public void run()
            {
              for (Updateable updateable : updateables)
                {
                  updateable.update();
                }
            }
          };

          @Override
          public void run()
          {
            Platform.runLater(updateRunnable);
          }

        };
  }

  public void start()
  {
    timer.schedule(timerTask, 0L, period);
  }

}
