package com.aicas.jamaica.iot.demo.colorsorting.tools;

import java.io.IOException;
import java.io.InputStream;

import javafx.scene.image.Image;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundImage;
import javafx.scene.layout.BackgroundPosition;
import javafx.scene.layout.BackgroundRepeat;
import javafx.scene.layout.BackgroundSize;
import javafx.scene.layout.Region;
import javafx.scene.text.Font;
import javafx.scene.text.Text;

public class ResourceLoader
{
  public static Image loadImage(ClassLoader classLoader, String imageName)
  {
    Image image = null;
    InputStream imageInputStream = null;
    try
      {
        if (null != classLoader)
          {
            imageInputStream = classLoader.getResourceAsStream(imageName);
          }
        if (null != imageInputStream)
          {
            try
              {
                image = new Image(imageInputStream);
              }
            catch (NullPointerException e)
              {
                System.err.println(e.getMessage());
              }
          }
      }
    finally
      {
        if (null != imageInputStream)
          {
            try
              {
                imageInputStream.close();
              }
            catch (IOException e)
              {
                // so, what?
              }
          }
      }
    return image;
  }

  public static Background createBackground(Image image)
  {
    Background background = null;
    BackgroundImage backgroundImage = null;
    if (null != image)
      {
        backgroundImage = new BackgroundImage(image, BackgroundRepeat.NO_REPEAT, BackgroundRepeat.NO_REPEAT, BackgroundPosition.CENTER, BackgroundSize.DEFAULT);
      }
    if (null != backgroundImage)
      {
        background = new Background(backgroundImage);
      }
    return background;
  }

  public static void setImageBackground(Region region, Background background)
  {
    if (null != background)
      {
        region.setBackground(background);
      }
  }

  public static void setImageBackground(Region region, Image image)
  {
    Background background = createBackground(image);
    setImageBackground(region, background);
  }

  public static void setImageBackground(Region region, ClassLoader classLoader, String imageName)
  {
    Image image = loadImage(classLoader, imageName);
    setImageBackground(region, image);
  }


  public static Font loadFont(ClassLoader classLoader, String fontName, double fontSize)
  {
    Font font = null;
    InputStream fontInputStream = null;
    try
      {
        if (null != classLoader)
          {
            fontInputStream = classLoader.getResourceAsStream(fontName);
          }
        if (null != fontInputStream)
          {
            font = Font.loadFont(fontInputStream, fontSize);
          }
        else
          {
            System.err.println("Font resource '" + fontName + "' not found.");
          }
      }
    finally
      {
        if (null != fontInputStream)
          {
            try
              {
                fontInputStream.close();
              }
            catch (IOException e)
              {
                // So, what?
              }
          }
      }
    return font;
  }

  public static void setFont(Text text, ClassLoader classLoader, String fontName, double fontSize)
  {
    Font font = loadFont(classLoader, fontName, fontSize);
    if (null != font)
      {
        text.setFont(font);
      }
  }
}
