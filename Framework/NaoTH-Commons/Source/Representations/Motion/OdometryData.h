/**
* @file OdometryData.h
* Contains the OdometryData class.
*
* @author Max Risler (GT - Implementation)
* @author <a href="mailto:mellmann@informatik.hu-berlin.de">Heinrich Mellmann</a>
*/

#ifndef __OdometryData_h_
#define __OdometryData_h_

#include "Tools/ModuleFramework/Representation.h"
#include "Tools/DataStructures/Printable.h"
#include "Tools/Math/Pose2D.h"

/**
* OdometryData
* OdometryData contains an approximation of overall movement the robot has done.
* @attention Only use differences of OdometryData at different times.
* Position in mm
*/
class OdometryData : public Pose2D, public Printable
{
public:
  OdometryData() {};
  ~OdometryData(){};

  virtual void print(ostream& stream) const
  {
    stream << "x = " << translation.x << endl;
    stream << "y = " << translation.y << endl;
    stream << "rotation = " << rotation << endl;
  }//end print
};

REPRESENTATION_INTERFACE(OdometryData);

#endif //__OdometryData_h_
