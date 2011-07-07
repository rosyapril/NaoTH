/**
 * @file AbstractMotion.cpp
 * 
 * @author <a href="mailto:xu@informatik.hu-berlin.de">Xu, Yuan</a>
 */

#include "AbstractMotion.h"

using namespace naoth;

AbstractMotion::AbstractMotion(motion::MotionID id)
: theId(id),
  currentState(motion::stopped),
  theMotorJointData(MotionBlackBoard::getInstance().theMotorJointData),
  theBlackBoard(MotionBlackBoard::getInstance())
{
  init();
}

bool AbstractMotion::setStiffness(double* stiffness, double delta, JointData::JointID begin, JointData::JointID end)
{
  int readyJointNum = 0;
  for (int i = begin; i < end; i++)
  {
    double d = stiffness[i] - theBlackBoard.theSensorJointData.stiffness[i];
    if (fabs(d) < delta || i == JointData::HeadPitch || i==JointData::HeadYaw )
    {
      readyJointNum++;
      theMotorJointData.stiffness[i] = stiffness[i];
    } else
    {
      d = Math::clamp(d, -delta, delta);
      theMotorJointData.stiffness[i] = theBlackBoard.theSensorJointData.stiffness[i] + d;

      if (theMotorJointData.stiffness[i] < 0) // -1 is the special case
      {
        if ( d < 0 )
        {
          theMotorJointData.stiffness[i] = -1;
        }
        else
        {
          theMotorJointData.stiffness[i] = 0;
        }
      }
    }
  }//end for

  return readyJointNum == (end - begin);
}
