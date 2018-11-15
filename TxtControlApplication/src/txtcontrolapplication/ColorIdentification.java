package txtcontrolapplication;

public class ColorIdentification
{
  private int minRange_ = 3000;
  private int maxWhiteRange_ = 1390;
  private int maxRedRange_ = 1600 ;
  private int value_ ;

  public void colorSensorValue(int value)
  {
    value_ = value;
    if (value_ <= maxWhiteRange_ )
      {
        detectWhite(value_);
      }else if (value_ > maxWhiteRange_ && value_ <= 1600)
        {
          detectRed(value_);
        }else
          {
            detectBlue(value_);
          }

  }


  public void detectWhite(int whiteRange)
  {
    System.out.println("White block detected");

  }

  public void detectRed(int redRange)
  {
    System.out.println("Red block detected");

  }

  public void detectBlue(int blueRange)
  {
    System.out.println("Blue block detected");

  }


}
