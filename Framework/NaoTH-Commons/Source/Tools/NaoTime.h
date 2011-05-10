/**
* @file NaoTime.h
*
* Class NaoTime provides information and calculation for time
*
* @author Oliver Welter
*/

#ifndef _NAOTIME_H
#define	_NAOTIME_H

#ifdef WIN32
#include <windows.h>
#else
#include <sys/time.h>
#include <cstdlib>
#include <ctime>
#endif

namespace naoth
{
  class NaoTime
  {
    static unsigned long long getSystemTimeInMicroSeconds();

  public:
    /*
     * return the time sinse the start of the controller
     */
    static unsigned int getNaoTimeInMilliSeconds();

    /**
     * This value is set once in the beginning of the programm to the system time
     * at this point in time
     */
    static const unsigned long long startingTimeInMicroSeconds;
  };
}
#endif	/* _NAOTIME_H */