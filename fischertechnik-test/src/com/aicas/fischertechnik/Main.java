package com.aicas.fischertechnik;

import com.aicas.fischertechnik.driver.AicasTxtCommonJNIDriver;

public class Main
{

    public static void main(String[] args)
    {
        // TODO Auto-generated method stub
        AicasTxtCommonJNIDriver driver = new AicasTxtCommonJNIDriver();
        driver.initTxt();
        driver.rotateMotor(1, 1, 512, 127);
        driver.uninitTxt();
    }

}
