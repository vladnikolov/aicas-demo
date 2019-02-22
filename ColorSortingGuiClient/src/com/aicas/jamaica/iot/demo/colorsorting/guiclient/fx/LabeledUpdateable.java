package com.aicas.jamaica.iot.demo.colorsorting.guiclient.fx;

import javafx.scene.control.Labeled;

public abstract class LabeledUpdateable implements Updateable
{
  private final Labeled labeled;

  public LabeledUpdateable(Labeled labeled)
  {
    this.labeled = labeled;
  }

  @Override
  public boolean update()
  {
    String currentText = labeled.getText();
    String text = updateValue();
    labeled.setText(text);
    return text.equals(currentText);
  }

  protected abstract String updateValue();

}
