/**
 * @file DummyActiveGoalLocator.h
 *
 * @author <a href="mailto:mellmann@informatik.hu-berlin.de">Heinrich Mellmann</a>
 * Declaration of class DummyActiveGoalLocatorSimpleParticle
 */

#ifndef _DummyActiveGoalLocator_h_
#define _DummyActiveGoalLocator_h_

#include <ModuleFramework/Module.h>

// Representations
#include "Representations/Perception/GoalPercept.h"
#include "Representations/Infrastructure/FrameInfo.h"
#include "Representations/Modeling/GoalModel.h"
#include "Representations/Modeling/RobotPose.h"
#include "Representations/Modeling/CompassDirection.h"
#include "Representations/Infrastructure/FieldInfo.h"

//for MODIFY
#include "Tools/Debug/DebugRequest.h"
#include "Tools/Debug/DebugDrawings.h"
#include "Tools/Debug/DebugModify.h"

//////////////////// BEGIN MODULE INTERFACE DECLARATION ////////////////////

BEGIN_DECLARE_MODULE(DummyActiveGoalLocator)
  PROVIDE(DebugRequest)
  PROVIDE(DebugDrawings)
  PROVIDE(DebugModify)

  REQUIRE(FrameInfo)
  
  REQUIRE(SelfLocGoalModel)
  REQUIRE(RobotPose)

  REQUIRE(SensingGoalModel)
  REQUIRE(CompassDirection)
  REQUIRE(FieldInfo)

  REQUIRE(GoalPercept)
  REQUIRE(GoalPerceptTop)

  PROVIDE(LocalGoalModel)
END_DECLARE_MODULE(DummyActiveGoalLocator)

//////////////////// END MODULE INTERFACE DECLARATION //////////////////////

class DummyActiveGoalLocator : private DummyActiveGoalLocatorBase
{

public:
  DummyActiveGoalLocator();
  virtual ~DummyActiveGoalLocator(){}

  virtual void execute();
};

#endif //_DummyActiveGoalLocator_h_
