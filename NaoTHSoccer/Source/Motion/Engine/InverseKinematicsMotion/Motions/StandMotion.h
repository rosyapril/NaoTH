/**
* @file StandMotion.h
*
* @author <a href="mailto:mellmann@informatik.hu-berlin.de">Heinrich Mellmann</a>
*
* the stand motion goes from current pose to the default stand pose.
* running - this motion is cyclic and there is allways running when the motion
*           request is set to stand
* stopped - the motion stopps when the motion request is not stand and the 
*           default stand pose has been reached
* 
*/

#ifndef _StandMotion_H
#define _StandMotion_H

#include "IKMotion.h"
#include "Tools/Debug/DebugPlot.h"
#include "Tools/Debug/DebugModify.h"

#include <ModuleFramework/Module.h>

// representations
#include <Representations/Infrastructure/RobotInfo.h>
#include "Representations/Motion/Request/MotionRequest.h"
#include "Representations/Modeling/GroundContactModel.h"
#include "Representations/Modeling/InertialModel.h"
#include <Representations/Infrastructure/JointData.h>
#include <Representations/Infrastructure/InertialSensorData.h>
#include <Representations/Infrastructure/GyrometerData.h>
#include <Representations/Infrastructure/CalibrationData.h>
#include "Representations/Modeling/KinematicChain.h"
#include "Representations/Infrastructure/CalibrationData.h"
#include "Representations/Infrastructure/FrameInfo.h"

#include <Representations/Modeling/BodyState.h>

//tools
#include "Tools/JointMonitor.h"
#include "Tools/JointOffsets.h"
#include "Tools/StiffnessController.h"
#include "Tools/DataStructures/Spline.h"

BEGIN_DECLARE_MODULE(StandMotion)
  PROVIDE(DebugPlot)
  PROVIDE(DebugRequest)
  PROVIDE(DebugModify)

  REQUIRE(FrameInfo)

  REQUIRE(RobotInfo)
  REQUIRE(MotionRequest)
  REQUIRE(GroundContactModel)
  REQUIRE(InertialModel)
  REQUIRE(InertialSensorData)
  REQUIRE(GyrometerData)
  REQUIRE(CalibrationData)
  REQUIRE(KinematicChainSensor)
  REQUIRE(MotionStatus)
  PROVIDE(SensorJointData)

  REQUIRE(InverseKinematicsMotionEngineService)

  // body state
  REQUIRE(BodyState)

  PROVIDE(MotionLock)
  PROVIDE(MotorJointData)
END_DECLARE_MODULE(StandMotion)

class StandMotion : private StandMotionBase, public IKMotion
{
public:
  enum State {
    GotoStandPose,
    Relax
  };

  State state;
  State lastState;
  time_t state_time;

public:

  StandMotion();
  ~StandMotion(){}

  virtual void execute();

private:
  void calcStandPose();

  bool interpolateToPose();

  void applyPose(const InverseKinematic::CoMFeetPose& p);

  void tuneJointOffsets();

  void tuneStiffness();

  bool relaxedPoseIsStillOk();
  
  void turnOffStiffnessWhenJointIsOutOfRange();


private:
  tk::spline interpolationSpline;

  double totalTime;
  double time;
  double height;
  bool standardStand;
  
  bool stiffnessIsReady;

  InverseKinematic::CoMFeetPose targetPose;
  InverseKinematic::CoMFeetPose startPose;

  //InverseKinematic::HipFeetPose relaxedPose;
  JointData relaxData;
  bool relaxedPoseInitialized;
  bool isRelaxing;
  bool resetedAfterLifting;

  JointMonitor jointMonitors[naoth::JointData::numOfJoint];

  // used by StandMotion:stiffness_controller
  FrameInfo lastStiffnessUpdate;
  StiffnessController stiffnessController[naoth::JointData::numOfJoint];

  // used by StandMotion:online_tuning
  FrameInfo lastFrameInfo;
  JointOffsets jointOffsets;
  double stiffness[naoth::JointData::numOfJoint];

};

#endif  /* _StandMotion_H */

