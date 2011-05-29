/**
* @file IKMotion.h
*
* @author <a href="mailto:mellmann@informatik.hu-berlin.de">Heinrich Mellmann</a>
* @author <a href="mailto:xu@informatik.hu-berlin.de">Xu, Yuan</a>
* Implementation of class IKMotion
*/

#include "IKMotion.h"

using namespace InverseKinematic;

IKMotion::IKMotion(motion::MotionID id)
: AbstractMotion(id),
theEngine(InverseKinematicsMotionEngine::getInstance())
{
}

IKMotion::~IKMotion(){
}

Pose3D IKMotion::interpolate(const Pose3D& sp, const Pose3D& tp, double t) const
{
  ASSERT(0 <= t);
  ASSERT(t <= 1);

  Vector3<double> perr = tp.translation - sp.translation;
  
  Pose3D p;
  p.translation = sp.translation + perr * t;
  p.rotation = RotationMatrix::interpolate(sp.rotation, tp.rotation, t);

  return p;
}//end interpolate

CoMFeetPose IKMotion::interpolate(const CoMFeetPose& sp, const CoMFeetPose& tp, double t) const
{
  CoMFeetPose p;
  p.com = interpolate(sp.com, tp.com, t);
  p.lFoot = interpolate(sp.lFoot, tp.lFoot, t);
  p.rFoot = interpolate(sp.rFoot, tp.rFoot, t);
  return p;
}

CoMFeetPose IKMotion::getStandPose(double comHeight) const
{
  CoMFeetPose p;
  p.com.translation = Vector3<double>(theParameters.hipOffsetX, 0, comHeight);
  double footY = NaoInfo::HipOffsetY + theParameters.footOffsetY;
  p.lFoot.translation = Vector3<double>(0, footY, 0);
  p.rFoot.translation = Vector3<double>(0, -footY, 0);

  return p;
}//end getStandPose

HipFeetPose IKMotion::getHipFeetPoseFromKinematicChain(const KinematicChain& kc) const
{
  HipFeetPose p;

  const Kinematics::Link* link = kc.theLinks;
  // copy current pose
  p.hip = link[KinematicChain::Hip].M;
  p.lFoot = link[KinematicChain::LFoot].M;
  p.rFoot = link[KinematicChain::RFoot].M;
  return p;
}

CoMFeetPose IKMotion::getCoMFeetPoseFromKinematicChain(const KinematicChain& kc) const
{
  CoMFeetPose p;

  const Kinematics::Link* link = kc.theLinks;
  // copy current pose
  p.com.rotation = link[KinematicChain::Hip].R;
  p.com.translation = kc.CoM;
  p.lFoot = link[KinematicChain::LFoot].M;
  p.rFoot = link[KinematicChain::RFoot].M;
  return p;
}

HipFeetPose IKMotion::getHipFeetPoseBasedOnSensor() const
{
  return getHipFeetPoseFromKinematicChain(theBlackBoard.theKinematicChain);
}

CoMFeetPose IKMotion::getCoMFeetPoseBasedOnSensor() const
{
  return getCoMFeetPoseFromKinematicChain(theBlackBoard.theKinematicChain);
}

HipFeetPose IKMotion::getHipFeetPoseBasedOnModel() const
{
  return getHipFeetPoseFromKinematicChain(theBlackBoard.theKinematicChainModel);
}

CoMFeetPose IKMotion::getCoMFeetPoseBasedOnModel() const
{
  return getCoMFeetPoseFromKinematicChain(theBlackBoard.theKinematicChainModel);
}

HipFeetPose IKMotion::getCurrentHipFeetPose() const
{
  if (theBlackBoard.theSensorJointData.isLegStiffnessOn())
  {
    return getHipFeetPoseBasedOnModel();
  }

  return getHipFeetPoseBasedOnSensor();
}

CoMFeetPose IKMotion::getCurrentCoMFeetPose() const
{
  if (theBlackBoard.theSensorJointData.isLegStiffnessOn())
  {
    return getCoMFeetPoseBasedOnModel();
  }

  return getCoMFeetPoseBasedOnSensor();
}
  