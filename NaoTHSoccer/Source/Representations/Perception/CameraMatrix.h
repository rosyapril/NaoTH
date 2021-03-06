/**
 * @file CameraMatrix.h
 * 
 * Declaration of class CameraMatrix
 */ 

#ifndef _CameraMatrix_h_
#define _CameraMatrix_h_

#include "Tools/Math/Pose3D.h"
#include "Tools/Math/Line.h"
#include "Representations/Infrastructure/CameraInfo.h"

#include "Tools/DataStructures/Printable.h"
#include "Tools/DataStructures/Serializer.h"

#include <Tools/DataStructures/RingBuffer.h>

/**
* Matrix describing transformation from neck joint to camera.
*/
class CameraMatrix : public Pose3D, public naoth::Printable
{
public:
  bool valid;
  unsigned int timestamp;

  CameraMatrix(): valid(false), timestamp(0) {}
  CameraMatrix(const Pose3D& pose): Pose3D(pose), valid(false) {}

  virtual void print(std::ostream& stream) const;
};

/**
 * @brief A camera matrix from a secondary camera
 */
class CameraMatrixTop : public CameraMatrix
{
public:
  CameraMatrixTop() {}
  CameraMatrixTop(const Pose3D& pose): CameraMatrix(pose) {}
  virtual ~CameraMatrixTop() {}
};

namespace naoth
{
  template<>
  class Serializer<CameraMatrix>
  {
  public:
    static void serialize(const CameraMatrix& representation, std::ostream& stream);
    static void deserialize(std::istream& stream, CameraMatrix& representation);
  };

  template<>
  class Serializer<CameraMatrixTop> : public Serializer<CameraMatrix>
  {};
}

#endif // _CameraMatrix_h_
