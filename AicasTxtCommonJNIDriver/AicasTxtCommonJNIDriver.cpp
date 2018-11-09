#include "AicasTxtCommonJNIDriver.h"

#include <jni.h>
#include <stdlib.h>
#include <stdio.h>          // for printf()
#include <unistd.h>         // for sleep()
#include <string.h>
#include <pthread.h> 		// for mutexes

#include "KeLibTxtDl.h"     // TXT Lib
#include "FtShmem.h"        // TXT Transfer Area

// Common debugging stuff for RoboProLib
unsigned int DebugFlags;
FILE *DebugFile;


FISH_X1_TRANSFER    *pTArea = 0;

pthread_mutex_t mutexMotor;

#define DEBUG 1


/* Class:     com.aicas.fischertechnik.driver.AicasTxtCommonJNIDriver
 * Method:    initTxt
 * Signature: ()I */
#ifdef __cplusplus
  extern "C"
#endif
JNIEXPORT jint JNICALL
Java_com_aicas_fischertechnik_driver_AicasTxtCommonJNIDriver_initTxt(JNIEnv *env, jobject t)
{
	  int ret;

	  // start the integrated IO thread within the TXT library
	  if ((ret = StartTxtDownloadProg()) == KELIB_ERROR_NONE)
	  {
		  printf("TXT library successfully initialized\n");
          pTArea = GetKeLibTransferAreaMainAddress();
          if(!pTArea)
          {
    		  fprintf(stderr, "Error: could not acquire transfer area\n");
    		  ret = -1;
          }
	  }
	  else
	  {
		  fprintf(stderr,"Error: could not initialize TXT library\n");
	  }

	  return (jint) ret;
}

  /* Class:     com.aicas.fischertechnik.driver.AicasTxtCommonJNIDriver
   * Method:    uninitTxt
   * Signature: ()I */
#ifdef __cplusplus
  extern "C"
#endif
JNIEXPORT jint JNICALL
Java_com_aicas_fischertechnik_driver_AicasTxtCommonJNIDriver_uninitTxt(JNIEnv *env, jobject t)
{
	  return (jint) StopTxtDownloadProg();
}

/* Class:     com.aicas.fischertechnik.driver.AicasTxtCommonJNIDriver
 * Method:    rotateMotor
 * Signature: (IIII)I */
#ifdef __cplusplus
  extern "C"
#endif
JNIEXPORT jint JNICALL
Java_com_aicas_fischertechnik_driver_AicasTxtCommonJNIDriver_rotateMotor(JNIEnv *env, jobject t,
		jint id, jint direction, jint speed, jint distance)
{

#ifdef DEBUG
	printf("AicasTxtJNIDriver: moving motor %d, direction=%d, speed=%d, distance=%d\n",
			id, direction, speed, distance);
#endif
	pthread_mutex_lock(&mutexMotor);

	// Motor M1 is controlled by output contacts O1 [0] and O2 [1]
	// Motor M2 is controlled by output contacts O3 [2] and O4 [3]
	// ...
	// Motor MX is controlled by output contacts OX-1 [X * 2 - 2] and OX [X * 2 - 1]
	pTArea->ftX1out.distance[id * 2 - 2] = distance;         // Distance to drive Motor id
	pTArea->ftX1out.motor_ex_cmd_id[id * 2 - 2]++;			// Set new Distance Value for Motor id
	if (direction)
	{
		pTArea->ftX1out.duty[id * 2 - 2] = speed;			// Switch Motor id ( O1 [0] ) on with PWM Value 512 (= max speed)
		pTArea->ftX1out.duty[id * 2 - 1] = 0;   			// Switch Motor id ( O2 [1] ) with minus
	}
	else
	{
		pTArea->ftX1out.duty[id * 2 - 2] = 0;				// Switch Motor id ( O1 [0] ) on with PWM Value 512 (= max speed)
		pTArea->ftX1out.duty[id * 2 - 1] = speed;           // Switch Motor id ( O2 [1] ) with minus
	}

#ifdef DEBUG
	printf("AicasTxtJNIDriver: motor %d, command-counter-out=%d, command-counter-in=%d\n",
					id, pTArea->ftX1out.motor_ex_cmd_id[id * 2 - 2], pTArea->ftX1in.motor_ex_cmd_id[id * 2 - 2]);
#endif

	while (pTArea->ftX1in.motor_ex_cmd_id[id * 2 - 2] < pTArea->ftX1out.motor_ex_cmd_id[id * 2 - 2])
	{
		// wait until motor has reached destination
#ifdef DEBUG
		printf("AicasTxtJNIDriver: waiting for motor move to finish\n");
		pTArea->ftX1in.motor_ex_cmd_id[id * 2 - 2]++;
		printf("AicasTxtJNIDriver: motor %d, command-counter-out=%d, command-counter-in=%d\n",
				id, pTArea->ftX1out.motor_ex_cmd_id[id * 2 - 2], pTArea->ftX1in.motor_ex_cmd_id[id * 2 - 2]);

#endif
	}

	printf("AicasTxtJNIDriver: motor %d ready\n", id);

	pthread_mutex_unlock(&mutexMotor);

	return (jint) 0;
}