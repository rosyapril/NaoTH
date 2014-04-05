/**
* @file SuperBallDetector.h
*
* Definition of class SuperBallDetector
*/

#ifndef _SuperBallDetector_H_
#define _SuperBallDetector_H_

#include <ModuleFramework/Module.h>
#include <ModuleFramework/Representation.h>

// common tools
#include <Tools/ColorClasses.h>
#include <Tools/Math/Vector2.h>
#include <Tools/Math/Matrix_nxn.h>
#include <Tools/Math/PointList.h>
#include <Tools/DataStructures/OccupancyGrid.h>
#include <Tools/DataStructures/Area.h>
#include <Tools/DataStructures/ParameterList.h>
#include <Tools/ImageProcessing/ColorModelConversions.h>

#include <Representations/Infrastructure/Image.h>

#include "Representations/Infrastructure/FieldInfo.h"
#include <Representations/Infrastructure/FrameInfo.h>
#include "Representations/Infrastructure/ColoredGrid.h"
#include "Representations/Perception/BodyContour.h"
#include "Representations/Perception/FieldPercept.h"
#include "Representations/Perception/ArtificialHorizon.h"
#include "Representations/Perception/BallPercept.h"
#include "Representations/Perception/FieldColorPercept.h"
#include "Representations/Perception/CameraMatrix.h"
#include "Representations/Modeling/KinematicChain.h"
#include "Representations/Perception/Histograms.h"

// tools
#include "Tools/ImageProcessing/GradientSpiderScan.h"
#include "Tools/DoubleCamHelpers.h"

BEGIN_DECLARE_MODULE(SuperBallDetector)
  REQUIRE(Image)
  REQUIRE(ImageTop)
  REQUIRE(CameraMatrix)
  REQUIRE(CameraMatrixTop)
  REQUIRE(ArtificialHorizon)
  REQUIRE(ArtificialHorizonTop)
  REQUIRE(FieldPercept)
  REQUIRE(FieldPerceptTop)
  REQUIRE(FieldColorPercept)
  REQUIRE(FieldColorPerceptTop)
  REQUIRE(BodyContour)
  REQUIRE(BodyContourTop)
  REQUIRE(FieldInfo)
  REQUIRE(FrameInfo)
  REQUIRE(OverTimeHistogram)
  REQUIRE(OverTimeHistogramTop)
  REQUIRE(GoalPostHistograms)

  PROVIDE(BallPercept)
  PROVIDE(BallPerceptTop)
END_DECLARE_MODULE(SuperBallDetector)


class SuperBallDetector: private SuperBallDetectorBase
{
public:
  SuperBallDetector();
  ~SuperBallDetector(){}

  void execute(CameraInfo::CameraID id);

  virtual void execute()
  {
     execute(CameraInfo::Top);
     execute(CameraInfo::Bottom);
  }
 
private:
  CameraInfo::CameraID cameraID;

  class Parameters: public ParameterList
  {
  public:

    Parameters() : ParameterList("SuperBallDetectorParameters")
    {
      PARAMETER_REGISTER(stepSize) = 2;    
      PARAMETER_REGISTER(minOffsetToFieldY) = 100;    
      PARAMETER_REGISTER(sigmaFactorY) = 20;  
      PARAMETER_REGISTER(sigmaFactorUV) = 10; 
      PARAMETER_REGISTER(orange_thresh) = 115;    
	    
      syncWithConfig();
      DebugParameterList::getInstance().add(this);
    }

    ~Parameters()
    {
      DebugParameterList::getInstance().remove(this);
    }

	  int stepSize;
    int minOffsetToFieldY;
    double sigmaFactorY;
    double sigmaFactorUV;
    double orange_thresh;

  } params;

  Statistics::Histogram<256> filteredHistogramY;
  Statistics::Histogram<256> filteredHistogramU;
  Statistics::Histogram<256> filteredHistogramV;

private:
  bool findMaximumRedPoint(Vector2i& peakPos) const;

private:
  // double cam stuff
  DOUBLE_CAM_REQUIRE(SuperBallDetector, Image);
  DOUBLE_CAM_REQUIRE(SuperBallDetector, CameraMatrix);
  DOUBLE_CAM_REQUIRE(SuperBallDetector, ArtificialHorizon);
  DOUBLE_CAM_REQUIRE(SuperBallDetector, FieldColorPercept);
  DOUBLE_CAM_REQUIRE(SuperBallDetector, FieldPercept);
  DOUBLE_CAM_REQUIRE(SuperBallDetector, BodyContour);
  DOUBLE_CAM_REQUIRE(SuperBallDetector, OverTimeHistogram);
 
  DOUBLE_CAM_PROVIDE(SuperBallDetector, BallPercept);
          
};//end class SuperBallDetector

#endif // _SuperBallDetector_H_
