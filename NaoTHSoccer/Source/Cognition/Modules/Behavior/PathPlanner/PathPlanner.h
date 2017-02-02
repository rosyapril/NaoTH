/**
* @file PathPlanner.h
*
* @author <a href="mailto:akcayyig@hu-berlin.de">Yigit Can Akcay</a>
* Definition of class PathPlanner
*/

#ifndef _PathPlanner_H_
#define _PathPlanner_H_

#include <ModuleFramework/Module.h>

// representations
#include "Representations/Infrastructure/FrameInfo.h"
#include "Representations/Infrastructure/FieldInfo.h"
#include "Representations/Modeling/OdometryData.h"
#include "Representations/Perception/BallPercept.h"
#include "Representations/Perception/MultiBallPercept.h"
#include "Representations/Modeling/BallModel.h"

#include "Representations/Motion/Request/HeadMotionRequest.h"
#include "Representations/Motion/Request/MotionRequest.h"
#include "Representations/Motion/MotionStatus.h"

#include "Representations/Modeling/PathModel.h"

// debug
#include "Tools/Debug/DebugRequest.h"
#include "Tools/Debug/DebugModify.h"

BEGIN_DECLARE_MODULE(PathPlanner)
  PROVIDE(DebugRequest)
  PROVIDE(DebugModify)

  REQUIRE(FrameInfo)
  REQUIRE(FieldInfo)
  REQUIRE(MotionStatus)
  REQUIRE(BallPercept)
  REQUIRE(BallPerceptTop)
  REQUIRE(MultiBallPercept)
  PROVIDE(BallModel)
  REQUIRE(OdometryData)

  PROVIDE(PathModel)

  PROVIDE(HeadMotionRequest)
  PROVIDE(MotionRequest)
END_DECLARE_MODULE(PathPlanner)

class PathPlanner: public PathPlannerBase
{
public:
  PathPlanner();
  ~PathPlanner(){};

  virtual void execute();

  void goToBall(double distance, double yOffset);
  void moveAroundBall(double direction, double radius);
  void fastForwardKick();
  void kickWithFoot();
  void sidekick();

  // Helping functions
  void lookAtBall();
  
private:
};

#endif // _PathPlanner_H_
