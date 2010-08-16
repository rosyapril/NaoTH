
#include <iterator>

#include "naorunner/Representations/Infrastructure/JointData.h"
#include "naorunner/Tools/Math/Common.h"
//#include "naorunner/Tools/Config/ConfigLoader.h"
#include "naorunner/Tools/Debug/NaoTHAssert.h"

using namespace naorunner;

double JointData::min[JointData::numOfJoint];
double JointData::max[JointData::numOfJoint];

JointData::JointData()
{
  for (int i = 0; i < numOfJoint; i++) {
    position[i] = 0;
    dp[i] = 0;
    ddp[i] = 0;
  }
}

void JointData::init(const std::string& filename)
{
//  Config cfg = ConfigLoader::loadConfig(filename.c_str());
//
//  for (int i = 0; i < JointData::numOfJoint; i++)
//  {
//    double maxDeg = 0;
//    string jointName = JointData::getJointName((JointData::JointID)i);
//    if (cfg.get(jointName + "Max", maxDeg))
//    {
//      max[i] = Math::fromDegrees(maxDeg);
//    } else
//    {
//      THROW("JointData: can not get " + jointName + " max angle")
//    }
//
//    double minDeg = 0;
//    if (cfg.get(jointName + "Min", minDeg))
//    {
//      min[i] = Math::fromDegrees(minDeg);
//    } else
//    {
//      THROW("JointData: can not get " + jointName + " min angle")
//    }
//  }
}

string JointData::getJointName(JointID joint)
{
  switch (joint)
  {
    case HeadPitch: return string("HeadPitch");
    case HeadYaw: return string("HeadYaw");
    case RShoulderRoll: return string("RShoulderRoll");
    case LShoulderRoll: return string("LShoulderRoll");
    case RShoulderPitch: return string("RShoulderPitch");
    case LShoulderPitch: return string("LShoulderPitch");
    case RElbowRoll: return string("RElbowRoll");
    case LElbowRoll: return string("LElbowRoll");
    case RElbowYaw: return string("RElbowYaw");
    case LElbowYaw: return string("LElbowYaw");
    case LHipYawPitch: return string("LHipYawPitch");
    case RHipYawPitch: return string("RHipYawPitch");
    case RHipPitch: return string("RHipPitch");
    case LHipPitch: return string("LHipPitch");
    case RHipRoll: return string("RHipRoll");
    case LHipRoll: return string("LHipRoll");
    case RKneePitch: return string("RKneePitch");
    case LKneePitch: return string("LKneePitch");
    case RAnklePitch: return string("RAnklePitch");
    case LAnklePitch: return string("LAnklePitch");
    case RAnkleRoll: return string("RAnkleRoll");
    case LAnkleRoll: return string("LAnkleRoll");
    default: return string("Unknown Joint");
  }//end switch
}//end getJointName

double JointData::mirrorData(JointID joint) const
{
  switch (joint)
  {
      // Head (don't mirror)
    case HeadYaw: return -position[HeadYaw];
    case HeadPitch: return position[HeadPitch];

      // Arms
    case RShoulderPitch: return position[LShoulderPitch];
    case RShoulderRoll: return -position[LShoulderRoll];
    case RElbowYaw: return -position[LElbowYaw];
    case RElbowRoll: return -position[LElbowRoll];

    case LShoulderPitch: return position[RShoulderPitch];
    case LShoulderRoll: return -position[RShoulderRoll];
    case LElbowYaw: return -position[RElbowYaw];
    case LElbowRoll: return -position[RElbowRoll];

      // Legs
    case RHipYawPitch: return position[LHipYawPitch];
    case RHipPitch: return position[LHipPitch];
    case RHipRoll: return -position[LHipRoll];
    case RKneePitch: return position[LKneePitch];
    case RAnklePitch: return position[LAnklePitch];
    case RAnkleRoll: return -position[LAnkleRoll];


    case LHipYawPitch: return position[RHipYawPitch];
    case LHipPitch: return position[RHipPitch];
    case LHipRoll: return -position[RHipRoll];
    case LKneePitch: return position[RKneePitch];
    case LAnklePitch: return position[RAnklePitch];
    case LAnkleRoll: return -position[RAnkleRoll];
    default: return position[joint];
  }
}//end mirrorData

JointData::JointID JointData::jointIDFromName(std::string name)
{
  for (int i = 0; i < numOfJoint; i++) {
    if (name == getJointName((JointID) i)) return (JointID) i;
  }//end for

  return numOfJoint;
}//end getJointName

void JointData::mirrorFrom(const JointData& jointData)
{
  for (int i = 0; i < numOfJoint; i++)
    position[i] = jointData.mirrorData((JointID) i);
}//end mirror

void JointData::mirror()
{
  JointData tmp = *this;
  mirrorFrom(tmp);
}


void JointData::clamp(JointID id)
{
  position[id] = Math::clamp(position[id], min[id], max[id]);
}

void JointData::clamp()
{
  for( int i=0; i<numOfJoint; i++)
  {
    clamp((JointID)i);
  }
}

bool JointData::isInRange(JointData::JointID id, double ang) const
{
    const static double b = Math::fromDegrees(3);
    return ang <= (max[id] + b) && ang >= (min[id]-b);
}

bool JointData::isInRange(JointData::JointID id) const
{
    return isInRange(id, position[id]);
}

SensorJointData::SensorJointData()
{
  for (int i = 0; i < numOfJoint; i++)
  {
    position[i] = 0.0;
    hardness[i] = 0.0;
    electricCurrent[i] = 0.0;
    temperature[i] = 0.0;
  }//end for
}

void SensorJointData::print(ostream& stream) const
{
  stream << "Joint [pos(deg), hardness, temperature,current]" << endl;
  stream << "------------------------" << endl;
  for (int i = 0; i < numOfJoint; i++) 
  {
    stream.precision(4);
    stream << getJointName((JointData::JointID) i) << "\t[" << fixed << Math::toDegrees(position[i]) << ", " << hardness[i] << ", ";
    stream.precision(0);
    stream << temperature[i] << ", ";
    stream.precision(3);
    stream << electricCurrent[i] << "]" <<  endl;
  }//end for
}//end print

SensorJointData::~SensorJointData()
{
}

MotorJointData::MotorJointData()
{
  for (int i = 0; i < numOfJoint; i++) {
    position[i] = 0.0;
    hardness[i] = 0.0;
  }//end for
}

MotorJointData::~MotorJointData()
{
}

void MotorJointData::print(ostream& stream) const
{
  stream << "Joint [pos, hardness]" << endl;
  stream << "------------------------" << endl;
  for (int i = 0; i < numOfJoint; i++) {
    stream << getJointName((JointData::JointID) i) << "[" << position[i] << ", " << hardness[i] << "]" << endl;
  }//end for
}//end print