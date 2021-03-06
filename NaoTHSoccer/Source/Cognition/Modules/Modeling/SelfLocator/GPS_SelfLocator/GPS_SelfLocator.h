/**
* @file GPS_SelfLocator.h
*
* @author <a href="mailto:goehring@informatik.hu-berlin.de">Daniel Goehring</a>
* Declaration of class GPS_SelfLocator
*/

#ifndef _GPS_SelfLocator_h_
#define _GPS_SelfLocator_h_

#include <ModuleFramework/Module.h>

// debug
#include "Tools/Debug/DebugRequest.h"
#include "Tools/Debug/DebugDrawings.h"
#include "Tools/Debug/DebugPlot.h"
#include "Tools/Debug/DebugModify.h"

// Representations
#include "Representations/Infrastructure/GPSData.h"

#include "Representations/Modeling/PlayerInfo.h"
#include "Representations/Modeling/RobotPose.h"
#include "Representations/Modeling/OdometryData.h"

#include "Representations/Infrastructure/FieldInfo.h"
#include "Representations/Modeling/GoalModel.h"


//////////////////// BEGIN MODULE INTERFACE DECLARATION ////////////////////

BEGIN_DECLARE_MODULE(GPS_SelfLocator)
  PROVIDE(DebugRequest)
  PROVIDE(DebugDrawings)
  PROVIDE(DebugPlot)
  PROVIDE(DebugModify)

  REQUIRE(FrameInfo)
  REQUIRE(GPSData)
  REQUIRE(OdometryData)
  REQUIRE(PlayerInfo)
  REQUIRE(FieldInfo)

  PROVIDE(RobotPose)
  PROVIDE(SelfLocGoalModel)
END_DECLARE_MODULE(GPS_SelfLocator)

//////////////////// END MODULE INTERFACE DECLARATION //////////////////////

class GPS_SelfLocator : public GPS_SelfLocatorBase
{
public:

  GPS_SelfLocator();
  ~GPS_SelfLocator(){}


  /** executes the module */
  void execute();

private:
  void drawGPSData();
  Pose2D calculateFromGPS(const GPSData& gps) const;

  Pose2D gpsRobotPose;
  Pose2D lastRobotOdometry;
};

#endif //__GPS_SelfLocator_h_
