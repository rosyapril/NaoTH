/**
* @file CameraMatrixOffset.h
*
* @author <a href="mailto:mellmann@informatik.hu-berlin.de">Heinrich Mellmann</a>
*/

#ifndef _CameraMatrixOffset_h_
#define _CameraMatrixOffset_h_

#include <Representations/Infrastructure/CameraInfo.h>
#include "Tools/Math/Vector2.h"
#include "Tools/DataStructures/Printable.h"
#include "Tools/DataStructures/ParameterList.h"
#include <Tools/DataStructures/Serializer.h>

//serialization
#include "Tools/DataConversion.h"
#include "Messages/Representations.pb.h"
#include <google/protobuf/io/zero_copy_stream_impl.h>


class CameraMatrixOffset: public ParameterList, public naoth::Printable
{
public:

  CameraMatrixOffset() : ParameterList("CameraMatrixOffset")
  {
    PARAMETER_REGISTER(correctionOffset[naoth::CameraInfo::Top].x) = 0;
    PARAMETER_REGISTER(correctionOffset[naoth::CameraInfo::Top].y) = 0;
    PARAMETER_REGISTER(correctionOffset[naoth::CameraInfo::Bottom].x) = 0;
    PARAMETER_REGISTER(correctionOffset[naoth::CameraInfo::Bottom].y) = 0;

    syncWithConfig();
  }
  
  virtual ~CameraMatrixOffset(){}

  //experimental
  Vector2d offsetByGoalModel;
  Vector2d offset;

  Vector2d correctionOffset[naoth::CameraInfo::numOfCamera];

  typedef struct {
       Vector3d head_rot;
       Vector3d cam_rot;
   } Offsets;

   Offsets correctionOffsets[naoth::CameraInfo::numOfCamera];

  virtual void print(std::ostream& stream) const
  {
    stream << "x = " << offset.x << std::endl;
    stream << "y = " << offset.y << std::endl;
    stream << "Roll Offset Camera Top (x): "<< correctionOffset[naoth::CameraInfo::Top].x << " rad" << std::endl;
    stream << "Tilt Offset Camera Top: (y)"<< correctionOffset[naoth::CameraInfo::Top].y << " rad" <<  std::endl;
    stream << "Roll Offset Camera Bottom (x): "<< correctionOffset[naoth::CameraInfo::Bottom].x << " rad" << std::endl;
    stream << "Tilt Offset Camera Bottom: (y)"<< correctionOffset[naoth::CameraInfo::Bottom].y << " rad" <<  std::endl;

    stream << "----------Top-----------" << std::endl;
    stream << "Roll  Offset Head   (x): "<< correctionOffsets[naoth::CameraInfo::Top].head_rot.x << " rad" << std::endl;
    stream << "Pitch Offset Head   (y): "<< correctionOffsets[naoth::CameraInfo::Top].head_rot.y << " rad" << std::endl;
    stream << "Yaw   Offset Head   (z): "<< correctionOffsets[naoth::CameraInfo::Top].head_rot.z << " rad" << std::endl;
    stream << "Roll  Offset Camera (x): "<< correctionOffsets[naoth::CameraInfo::Top].cam_rot.x  << " rad" << std::endl;
    stream << "Pitch Offset Camera (y): "<< correctionOffsets[naoth::CameraInfo::Top].cam_rot.y  << " rad" << std::endl;
    stream << "Yaw   Offset Camera (z): "<< correctionOffsets[naoth::CameraInfo::Top].cam_rot.z  << " rad" << std::endl;

    stream << "---------Bottom---------" << std::endl;
    stream << "Roll  Offset Head   (x): "<< correctionOffsets[naoth::CameraInfo::Bottom].head_rot.x << " rad" << std::endl;
    stream << "Pitch Offset Head   (y): "<< correctionOffsets[naoth::CameraInfo::Bottom].head_rot.y << " rad" << std::endl;
    stream << "Yaw   Offset Head   (z): "<< correctionOffsets[naoth::CameraInfo::Bottom].head_rot.z << " rad" << std::endl;
    stream << "Roll  Offset Camera (x): "<< correctionOffsets[naoth::CameraInfo::Bottom].cam_rot.x  << " rad" << std::endl;
    stream << "Pitch Offset Camera (y): "<< correctionOffsets[naoth::CameraInfo::Bottom].cam_rot.y  << " rad" << std::endl;
    stream << "Yaw   Offset Camera (z): "<< correctionOffsets[naoth::CameraInfo::Bottom].cam_rot.z  << " rad" << std::endl;
  }
};


namespace naoth
{
template<>
class Serializer<CameraMatrixOffset>
{
  public:
  static void serialize(const CameraMatrixOffset& representation, std::ostream& stream)
  {
    naothmessages::CameraMatrixCalibration msg;
    for(int id=0; id < naoth::CameraInfo::numOfCamera; id++) {
      naoth::DataConversion::toMessage(representation.correctionOffset[id], *msg.add_correctionoffset());
      naoth::DataConversion::toMessage(representation.correctionOffsets[id].head_rot, *msg.add_correctionoffsethead());
      naoth::DataConversion::toMessage(representation.correctionOffsets[id].cam_rot, *msg.add_correctionoffsetcam());
    }
    google::protobuf::io::OstreamOutputStream buf(&stream);
    msg.SerializeToZeroCopyStream(&buf);
  }

  static void deserialize(std::istream& stream, CameraMatrixOffset& representation)
  {
    naothmessages::CameraMatrixCalibration msg;
    google::protobuf::io::IstreamInputStream buf(&stream);
    msg.ParseFromZeroCopyStream(&buf);
    
    ASSERT(msg.correctionoffset().size() == naoth::CameraInfo::numOfCamera);
    for(int id=0; id < naoth::CameraInfo::numOfCamera; id++) {
        naoth::DataConversion::fromMessage(msg.correctionoffset(id), representation.correctionOffset[id]);
        naoth::DataConversion::fromMessage(msg.correctionoffsethead(id), representation.correctionOffsets[id].head_rot);
        naoth::DataConversion::fromMessage(msg.correctionoffsetcam(id),  representation.correctionOffsets[id].cam_rot);
    }
  }
};
}

#endif // _CameraMatrixOffset_h_
