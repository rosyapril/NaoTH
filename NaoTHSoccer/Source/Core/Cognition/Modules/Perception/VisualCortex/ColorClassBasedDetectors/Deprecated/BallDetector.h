/**
* @file BallDetector.h
*
* @author <a href="mailto:mellmann@informatik.hu-berlin.de">Heinrich Mellmann</a>
* Definition of class BallDetector
*/

#ifndef __BallDetector_H_
#define __BallDetector_H_

#include <ModuleFramework/Module.h>
#include <ModuleFramework/Representation.h>

// common tools
#include <Tools/ColorClasses.h>
#include <Tools/Math/Vector2.h>
#include <Tools/Math/Matrix_nxn.h>
#include <Tools/Math/PointList.h>
#include <Tools/DataStructures/OccupancyGrid.h>
#include <Tools/DataStructures/Area.h>
#include <Tools/ImageProcessing/ColorModelConversions.h>

#include <Representations/Infrastructure/Image.h>

#include "Representations/Infrastructure/ColorTable64.h"
#include "Representations/Infrastructure/FieldInfo.h"
#include "Representations/Infrastructure/ColoredGrid.h"
#include "Representations/Infrastructure/FrameInfo.h"
#include "Representations/Perception/BodyContour.h"
#include "Representations/Perception/FieldPercept.h"
#include "Representations/Perception/CameraMatrix.h"
#include "Representations/Perception/ArtificialHorizon.h"
#include "Representations/Perception/BallPercept.h"
#include "Representations/Modeling/ColorClassificationModel.h"
#include "Representations/Modeling/KinematicChain.h"

// tools
#include "Tools/ImageProcessing/BlobFinder.h"
#include "Tools/ImageProcessing/SpiderScan.h"

#include "Tools/DoubleCamHelpers.h"

BEGIN_DECLARE_MODULE(BallDetector)
  REQUIRE(Image)
  REQUIRE(ImageTop)
  REQUIRE(ColoredGrid)
  REQUIRE(ColoredGridTop)
  REQUIRE(ColorClassificationModel)
  REQUIRE(ColorClassificationModelTop)
  REQUIRE(CameraMatrix)
  REQUIRE(CameraMatrixTop)
  REQUIRE(CameraInfo)
  REQUIRE(CameraInfoTop)
  REQUIRE(ArtificialHorizon)
  REQUIRE(ArtificialHorizonTop)
  REQUIRE(KinematicChain)
  REQUIRE(FieldPercept)
  REQUIRE(FieldPerceptTop)
  REQUIRE(BodyContour)
  REQUIRE(BodyContourTop)
  REQUIRE(FrameInfo)
  REQUIRE(FieldInfo)

  PROVIDE(BallPercept)
  PROVIDE(BallPerceptTop)
END_DECLARE_MODULE(BallDetector)


class BallDetector: private BallDetectorBase
{
public:
  BallDetector();
  ~BallDetector(){};

  void execute(const Vector2i& start);

  // override the Module execute method
  virtual void execute(CameraInfo::CameraID id);

  void execute()
  {
     execute(CameraInfo::Bottom);
     execute(CameraInfo::Top);
  }
 
private:
  CameraInfo::CameraID cameraID;
  typedef PointList<20> BallPointList;

  bool connectedColors[ColorClasses::numOfColors];
  BlobFinder theBlobFinder;
  BlobFinder theBlobFinderTop;

  bool calculateCircle( const BallPointList& ballPoints, Vector2d& center, double& radius );
  bool randomScan(ColorClasses::Color color, Vector2i& result, const Vector2i& orgMin, const Vector2i& orgMax) const;

  void regionGrowExpandArea( const Vector2i& startingPoint, Vector2d& result, double& radius);

  double calculateBase(Vector2i& x, Vector2i& y, Vector2i& z);


  // double cam interface
  inline BlobFinder& getBlobFinder() {
    return (cameraID == CameraInfo::Top)?theBlobFinderTop:theBlobFinder;
  }

  DOUBLE_CAM_REQUIRE(BallDetector,Image);
  DOUBLE_CAM_REQUIRE(BallDetector,ColoredGrid);
  DOUBLE_CAM_REQUIRE(BallDetector,ColorClassificationModel);
  DOUBLE_CAM_REQUIRE(BallDetector,CameraMatrix);
  DOUBLE_CAM_REQUIRE(BallDetector,CameraInfo);
  DOUBLE_CAM_REQUIRE(BallDetector,ArtificialHorizon);
  DOUBLE_CAM_REQUIRE(BallDetector,FieldPercept);
  DOUBLE_CAM_REQUIRE(BallDetector,BodyContour);
  
  DOUBLE_CAM_PROVIDE(BallDetector,BallPercept);
          
};//end class BallDetector

#endif // __BallDetector_H_