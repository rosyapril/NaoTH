/**
 * @file MotionBlackBoard.h
 *
  * @author <a href="mailto:xu@informatik.hu-berlin.de">Xu, Yuan</a>
 *
 */

#ifndef _MOTIONBLACKBOARD_H
#define _MOTIONBLACKBOARD_H


// representations
#include <Representations/Infrastructure/FrameInfo.h>
#include <Representations/Infrastructure/FSRData.h>
#include <Representations/Infrastructure/JointData.h>
#include <Representations/Infrastructure/AccelerometerData.h>
#include <Representations/Infrastructure/GyrometerData.h>
#include <Representations/Infrastructure/InertialSensorData.h>
#include <Representations/Perception/CameraMatrix.h>
#include <Representations/Infrastructure/CameraInfo.h>
#include <Representations/Infrastructure/LEDData.h>
#include <Representations/Infrastructure/RobotInfo.h>
#include "Representations/Infrastructure/CalibrationData.h"

#include "Representations/Motion/Request/HeadMotionRequest.h"
#include "Representations/Motion/Request/MotionRequest.h"
#include "Representations/Motion/MotionStatus.h"

#include "Representations/Modeling/GroundContactModel.h"
#include "Representations/Modeling/OdometryData.h"
#include "Representations/Modeling/KinematicChain.h"
#include "Representations/Modeling/SupportPolygon.h"
#include "Representations/Modeling/InertialModel.h"


class AbstractMotion;

class FSRPositions
{
public:
  Vector3<double> pos[naoth::FSRData::numOfFSR];
};

class KinematicChainSensor: public KinematicChain {}; // former KinematicChain
class KinematicChainMotor: public KinematicChain {}; // former KinematicChainModel


class MotionBlackBoard : public naoth::Singleton<MotionBlackBoard>
{
private:
  friend class naoth::Singleton<MotionBlackBoard>;

  MotionBlackBoard();
  ~MotionBlackBoard();
private:

  void init();

  

  naoth::RobotInfo theRobotInfo;

  // data used internally
  KinematicChainSensor theKinematicChain; // data based on sensors
  KinematicChainMotor theKinematicChainModel; // data based on joint command (motor joint data)
  SupportPolygon theSupportPolygon;
  GroundContactModel theGroundContactModel;
  

  //Vector3<double> theFSRPos[naoth::FSRData::numOfFSR];
  FSRPositions theFSRPos;

  // sensory data
  naoth::FrameInfo theFrameInfo;
  naoth::AccelerometerData theAccelerometerData;
  naoth::GyrometerData theGyrometerData;
  naoth::InertialSensorData theInertialSensorData;
  naoth::FSRData theFSRData;
  naoth::MotorJointData theMotorJointData;
  //naoth::MotorJointData theLastMotorJointData;
  naoth::SensorJointData theSensorJointData;

  //naoth::LEDData theLEDData;

  CalibrationData theCalibrationData;
  
  // SerialSensorData theSerialSensorData;
  // SerialSensorDataRequest theSerialSensorDataRequest;
  CameraMatrix theCameraMatrix;
  naoth::CameraInfo theCameraInfo;
  MotionStatus theMotionStatus;
  OdometryData theOdometryData;
  InertialModel theInertialModel;

  // data copied from cognition
  HeadMotionRequest theHeadMotionRequest;
  MotionRequest theMotionRequest;

};

#endif  /* _MOTIONBLACKBOARD_H */

