/**
* @file FootStepPlanner.h
*
* @author <a href="mailto:xu@informatik.hu-berlin.de">Xu, Yuan</a>
* plan the foot step according to motion request
*/

#ifndef _FOOT_STEP_PLANNER_H
#define _FOOT_STEP_PLANNER_H

#include "FootStep.h"
#include "../IKParameters.h"
#include "Representations/Motion/Request/WalkRequest.h"

class FootStepPlanner
{
public:
  FootStepPlanner();
  
  void updateParameters(const IKParameters& parameters);

  FootStep nextStep(const FootStep& lastStep, const WalkRequest& req);

  FootStep controlStep(const FootStep& lastStep, const WalkRequest& req);
  
  FootStep finalStep(const FootStep& lastStep, const WalkRequest& req);

  FootStep zeroStep(const FootStep& lastStep) const;

private:
  FootStep calculateNextWalkStep(const InverseKinematic::FeetPose& pose, const Pose2D& offset, const Pose2D& lastStepRequest, FootStep::Foot movingFoot, const WalkRequest& req, bool stepControl = false);
  FootStep firstStep(const InverseKinematic::FeetPose& pose, const Pose2D& offset, const Pose2D& lastStepRequest, const WalkRequest& req);

  void restrictStepSize(Pose2D& step, double character) const;
  void restrictStepSizeControlStep(Pose2D& step, double character) const;
  void restrictStepChange(Pose2D& step, const Pose2D& lastStep) const;

private:

  // parameters
  double theMaxTurnInner;
  double theMaxStepTurn;
  double theMaxStepLength;
  double theMaxStepLengthBack;
  double theMaxStepWidth;
  double theFootOffsetY;
  double theMaxChangeTurn;
  double theMaxChangeX;
  double theMaxChangeY;

  double theMaxCtrlTurn;
  double theMaxCtrlLength;
  double theMaxCtrlWidth;

};

#endif // _FOOT_STEP_PLANNER_H
