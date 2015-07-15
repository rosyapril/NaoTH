/**
* @file TeamBallLocator.h
*
* Declaration of class TeamBallLocator
*/

#ifndef __TeamBallLocator_h_
#define __TeamBallLocator_h_

#include <ModuleFramework/Module.h>
#include <algorithm>

// Debug
#include "Tools/Debug/DebugRequest.h"
#include "Tools/Debug/DebugDrawings.h"

// Representations
#include "Representations/Modeling/TeamMessage.h"
#include "Representations/Modeling/RobotPose.h"
#include "Representations/Modeling/TeamBallModel.h"

BEGIN_DECLARE_MODULE(TeamBallLocator)
  PROVIDE(DebugRequest)
  PROVIDE(DebugDrawings)
  REQUIRE(TeamMessage)
  REQUIRE(RobotPose)

  PROVIDE(TeamBallModel)
END_DECLARE_MODULE(TeamBallLocator)

class TeamBallLocator : protected TeamBallLocatorBase
{

public:
  TeamBallLocator();
  ~TeamBallLocator(){}

  virtual void execute();

private:
  std::map<unsigned int, TeamMessage::Data> msgData;
  std::vector<Vector2dTS> ballPosHist;

  class Vector2dTS
  {
    public:
      Vector2dTS() {}
      bool operator< (const Vector2dTS &v1, const Vector2dTS &v2) const
      {
        return v1.t < v2.t;
      }
    private:
      Vector2d vec;
      double t;
  }
};

#endif //__TeamBallLocator_h_



