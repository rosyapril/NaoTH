#include "Representations/Infrastructure/GyrometerData.h"
#include "Messages/Framework-Representations.pb.h"
#include <google/protobuf/io/zero_copy_stream_impl.h>

using namespace naoth;
using namespace std;

void GyrometerData::print(ostream& stream) const
{
  stream << "x = " << data.x << " rad/s (" << rawData.x << ")" << endl;
  stream << "y = " << data.y << " rad/s (" << rawData.y << ")" << endl;
  stream << "z = " << data.z << " rad/s (" << rawData.z << ")" << endl;
}

void Serializer<GyrometerData>::serialize(const GyrometerData& representation, std::ostream& stream)
{
  naothmessages::GyrometerData msg;

  msg.mutable_data()->set_x(representation.data.x);
  msg.mutable_data()->set_y(representation.data.y);
  msg.mutable_data()->set_z(representation.data.z);

  msg.mutable_rawdata()->set_x(representation.rawData.x);
  msg.mutable_rawdata()->set_y(representation.rawData.y);
  msg.mutable_rawdata()->set_z(representation.rawData.z);

  msg.set_ref(representation.ref);

  ::google::protobuf::io::OstreamOutputStream buf(&stream);
  msg.SerializeToZeroCopyStream(&buf);
}

void Serializer<GyrometerData>::deserialize(std::istream& stream, GyrometerData& representation)
{
  naothmessages::GyrometerData msg;

  ::google::protobuf::io::IstreamInputStream buf(&stream);
  msg.ParseFromZeroCopyStream(&buf);

  // allow us to parse old log files
  if(msg.legacypackeddata_size() == 5)
  {
    for(int i=0; i<2; i++)
    {
      representation.data[i] = msg.legacypackeddata(i*2);
      representation.rawData[i] = msg.legacypackeddata(i*2+1);
    }
    representation.ref = msg.legacypackeddata(msg.legacypackeddata_size()-1);
  }

  // always use the non-deprecated fields if possible
  if(msg.has_data())
  {
    representation.data.x = msg.data().x();
    representation.data.y = msg.data().y();
    representation.data.z = msg.data().z();
  }
  if(msg.has_rawdata())
  {
    representation.rawData.x = msg.rawdata().x();
    representation.rawData.y = msg.rawdata().y();
    representation.rawData.z = msg.rawdata().z();
  }
  if(msg.has_ref())
  {
    representation.ref = msg.rawdata().z();
  }
}
