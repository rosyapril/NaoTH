/**
 * @file MotionBlackBoard.h
 *
  * @author <a href="mailto:xu@informatik.hu-berlin.de">Xu, Yuan</a>
 *
 */

#ifndef _MOTIONBLACKBOARD_H
#define	_MOTIONBLACKBOARD_H


//#include "PlatformInterface/PlatformInterchangeable.h"

// representations
#include <Representations/Infrastructure/FrameInfo.h>
#include <Representations/Infrastructure/FSRData.h>
#include <Representations/Infrastructure/JointData.h>
#include <Representations/Infrastructure/AccelerometerData.h>
#include <Representations/Infrastructure/GyrometerData.h>
#include <Representations/Infrastructure/InertialSensorData.h>
#include <Representations/Infrastructure/BumperData.h>
#include <Representations/Perception/CameraMatrix.h>

#include "Core/Representations/Motion/Request/HeadMotionRequest.h"
#include "Core/Representations/Motion/Request/MotionRequest.h"
#include "Core/Representations/Motion/MotionStatus.h"

#include "Core/Representations/Modeling/OdometryData.h"
#include "Core/Representations/Modeling/KinematicChain.h"
#include "Core/Representations/Modeling/SupportPolygon.h"

//#include "MotionEngine/InverseKinematicsMotionEngine/Tools/ReachibilityGrid.h"
//#include "MotionEngine/InverseKinematicsMotionEngine/Tools/IKMotionParameters.h"

class AbstractMotion;

class MotionBlackBoard : public Singleton<MotionBlackBoard>
{
private:
  friend class Singleton<MotionBlackBoard>;

  MotionBlackBoard();

public:

  void init();

  ~MotionBlackBoard();

  // data used internally
  KinematicChain theKinematicChain; // data based on sensors
  KinematicChain theKinematicChainModel; // data based on joint command (motor joint data)
  SupportPolygon theSupportPolygon;
  // InertialMeasurement theInertialMeasurement;

  Vector3<double> theFSRPos[FSRData::numOfFSR];
  FrameInfo theFrameInfo;
  //unsigned int lastTimeStamp;

//  ReachibilityGrid reachibilityGrid;
//  IKMotionParameters theIKMotionParameters;

  // data copied to cognition
  AccelerometerData theAccelerometerData;
  GyrometerData theGyrometerData;
  InertialSensorData theInertialSensorData;
  FSRData theFSRData;
  MotorJointData theMotorJointData;
  MotorJointData theLastMotorJointData;
  SensorJointData theSensorJointData;
  // SerialSensorData theSerialSensorData;
  // SerialSensorDataRequest theSerialSensorDataRequest;
  // BumperData theBumperData;
  CameraMatrix theCameraMatrix;
  MotionStatus theMotionStatus;
  OdometryData theOdometryData;

  // data copied from cognition
  HeadMotionRequest theHeadMotionRequest;
  MotionRequest theMotionRequest;
  
  AbstractMotion* currentlyExecutedMotion;
};

#endif	/* _MOTIONBLACKBOARD_H */

