/**
* @file GradientGoalDetector.h
*
* @author <a href="mailto:mellmann@informatik.hu-berlin.de">Heinrich Mellmann</a>
* @author <a href="mailto:critter@informatik.hu-berlin.de">CNR</a>
* Definition of class GradientGoalDetector
*/

#ifndef _GradientGoalDetector_H_
#define _GradientGoalDetector_H_

#include <ModuleFramework/Module.h>

#include <Representations/Infrastructure/Image.h>
#include "Representations/Perception/CameraMatrix.h"
#include "Representations/Perception/ArtificialHorizon.h"
#include "Representations/Perception/FieldColorPercept.h"
#include <Representations/Infrastructure/FrameInfo.h>
#include "Representations/Perception/GoalPercept.h"
#include "Representations/Perception/Histograms.h"
#include "Representations/Perception/FieldPercept.h"

// tools
#include "Tools/Math/Matrix_nxn.h"
#include "Tools/Math/Matrix_mxn.h"

#include "Tools/DoubleCamHelpers.h"
#include <Tools/DataStructures/RingBuffer.h>
#include <Tools/DataStructures/RingBufferWithSum.h>

#include <Tools/DataStructures/ParameterList.h>
#include "Tools/Debug/DebugParameterList.h"

#include <vector>

#include "Tools/naoth_opencv.h"

BEGIN_DECLARE_MODULE(GradientGoalDetector)
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
  REQUIRE(FrameInfo)

  PROVIDE(GoalPercept)
  PROVIDE(GoalPerceptTop)
  PROVIDE(GoalPostHistograms)
END_DECLARE_MODULE(GradientGoalDetector)


class GradientGoalDetector: private GradientGoalDetectorBase
{
public:

  GradientGoalDetector();
  virtual ~GradientGoalDetector(){}

  // override the Module execute method
  virtual bool execute(CameraInfo::CameraID id, bool horizon = true);

  void execute()
  {
    bool topScanned = execute(CameraInfo::Top);

    if(!topScanned) {
      execute(CameraInfo::Top, false);
    }
    debugStuff(CameraInfo::Top);
    
    if(topScanned && getGoalPercept().getNumberOfSeenPosts() == 0) {
      if( !execute(CameraInfo::Bottom)) {
        execute(CameraInfo::Bottom, false);
      }
      debugStuff(CameraInfo::Bottom);
    }
  }

private:
  static const int imageBorderOffset = 5;
  CameraInfo::CameraID cameraID;

  RingBuffer<Vector2i, 5> pointBuffer;
  RingBufferWithSum<double, 5> valueBuffer;
  RingBufferWithSum<double, 5> valueBufferY;

  class Parameters: public ParameterList
  {
  public:

    Parameters() : ParameterList("GradientGoalDetectorParameters")
    {
      PARAMETER_REGISTER(numberOfScanlines) = 5;
      PARAMETER_REGISTER(scanlinesDistance) = 6;
      PARAMETER_REGISTER(thresholdUV) = 60;
      PARAMETER_REGISTER(thresholdY) = 140;

      PARAMETER_REGISTER(maxFeatureDeviation) = 5;
      PARAMETER_REGISTER(maxFootScanSquareError) = 4.0;
      PARAMETER_REGISTER(minGoodPoints) = 3;
      PARAMETER_REGISTER(footGreenScanSize) = 10;
      PARAMETER_REGISTER(maxFeatureWidthError) = 0.2;
      PARAMETER_REGISTER(enableFeatureWidthCheck) = false;
      PARAMETER_REGISTER(enableGreenCheck) = false;

      PARAMETER_REGISTER(colorRegionDeviation) = 2;

      syncWithConfig();
      DebugParameterList::getInstance().add(this);
    }

    virtual ~Parameters() {
      DebugParameterList::getInstance().remove(this);
    }

    int numberOfScanlines;
    int scanlinesDistance;
    int thresholdUV;
    int thresholdY;

    int maxFeatureDeviation;
    double maxFootScanSquareError;
    int minGoodPoints;

    bool enableGreenCheck;
    int footGreenScanSize; // number of pixels to scan for green below the footpoint
    
    double maxFeatureWidthError;
    bool enableFeatureWidthCheck;

    double colorRegionDeviation;
  };

  Parameters params;

  class Feature
  {
  public:
    Vector2i begin;
    Vector2i center;
    Vector2i end;

    Vector2d responseAtBegin;
    Vector2d responseAtEnd;

    double width;

    bool possibleObstacle;
    bool used;

    Feature()
    :
      begin(-1,-1),
      end(-1, -1),
      responseAtBegin(0.0, 0.0),
      responseAtEnd(0.0, 0.0),
      width(0.0),
      possibleObstacle(false),
      used(false)
    {

    }
  };

  std::vector<std::vector<Feature> > features;
  std::vector<Feature> goodFeatures;

  // NOTE: needed by checkForGoodFeatures (has to have the same size as features)
  std::vector<int> lastTestFeatureIdx;


  void findFeatureCandidates(const Vector2d& scanDir, const Vector2d& p1, double threshold, double thresholdY);
  void checkForGoodFeatures(const Vector2d& scanDir, Feature& candidate, int scanLineId, double threshold, double thresholdY);
  void scanForFootPoints(const Vector2d& scanDir, Vector2i pos, double threshold, double thresholdY);
  void scanForTopPoints(GoalPercept::GoalPost& post, Vector2i pos, double threshold, double thresholdY);
  void scanForStatisticsToFootPoint(Vector2i footPoint, Vector2i pos);
  void debugStuff(CameraInfo::CameraID camID);

  Math::Line fitLine(const std::vector<Feature>& features) const;

  // double cam stuff
  DOUBLE_CAM_REQUIRE(GradientGoalDetector, Image);
  DOUBLE_CAM_REQUIRE(GradientGoalDetector, CameraMatrix);
  DOUBLE_CAM_REQUIRE(GradientGoalDetector, ArtificialHorizon);
  DOUBLE_CAM_REQUIRE(GradientGoalDetector, FieldColorPercept);
  DOUBLE_CAM_REQUIRE(GradientGoalDetector, FieldPercept);

  DOUBLE_CAM_PROVIDE(GradientGoalDetector, GoalPercept);

};//end class GradientGoalDetector

#endif // _GradientGoalDetector_H_
