/**
 * @file CameraMatrix.cpp
 * 
 * Definition of class CameraMatrix
 */ 

#include "CameraMatrix.h"

#include "Messages/Representations.pb.h"
#include <google/protobuf/io/zero_copy_stream_impl.h>

// HACK: needed for logplayer to calculate horizon
#include <PlatformInterface/Platform.h>
#include <Tools/CameraGeometry.h>

using namespace naoth;

void Serializer<CameraMatrix>::serialize(const CameraMatrix& representation, std::ostream& stream)
{
  naothmessages::CameraMatrix msg;
  msg.mutable_pose()->mutable_translation()->set_x( representation.translation.x );
  msg.mutable_pose()->mutable_translation()->set_y( representation.translation.y );
  msg.mutable_pose()->mutable_translation()->set_z( representation.translation.z );
  for(int i=0; i<3; i++){
    msg.mutable_pose()->add_rotation()->set_x( representation.rotation[i].x );
    msg.mutable_pose()->mutable_rotation(i)->set_y( representation.rotation[i].y );
    msg.mutable_pose()->mutable_rotation(i)->set_z( representation.rotation[i].z );
  }
  google::protobuf::io::OstreamOutputStream buf(&stream);
  msg.SerializeToZeroCopyStream(&buf);
}//end serialize

void Serializer<CameraMatrix>::deserialize(std::istream& stream, CameraMatrix& representation)
{
  naothmessages::CameraMatrix msg;
  google::protobuf::io::IstreamInputStream buf(&stream);
  msg.ParseFromZeroCopyStream(&buf);

  representation.translation.x = msg.pose().translation().x();
  representation.translation.y = msg.pose().translation().y();
  representation.translation.z = msg.pose().translation().z();

  for(int i=0; i<3; i++) {
    representation.rotation[i].x = msg.pose().rotation(i).x();
    representation.rotation[i].y = msg.pose().rotation(i).y();
    representation.rotation[i].z = msg.pose().rotation(i).z();
  }

  // HACK: calculate the horizon
  Vector2<double> p1, p2;
  CameraGeometry::calculateArtificialHorizon(representation, Platform::getInstance().theCameraInfo, p1, p2);
  representation.horizon = Math::LineSegment(p1, p2);
}//end deserialize