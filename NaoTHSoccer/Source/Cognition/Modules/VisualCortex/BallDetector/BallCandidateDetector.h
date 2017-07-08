/**
* @file BallCandidateDetector.h
*
* Definition of class BallCandidateDetector
*/

#ifndef _BallCandidateDetector_H_
#define _BallCandidateDetector_H_

#include <ModuleFramework/Module.h>
#include <ModuleFramework/ModuleManager.h>

// common tools
#include <Tools/ColorClasses.h>
#include <Tools/Math/Vector2.h>

#include <Representations/Infrastructure/Image.h>
#include <Representations/Infrastructure/FrameInfo.h>

#include "Representations/Perception/CameraMatrix.h"
#include "Representations/Perception/FieldPercept.h"
#include "Representations/Perception/BodyContour.h"
#include "Representations/Perception/FieldColorPercept.h"

#include "Representations/Perception/MultiChannelIntegralImage.h"
#include "Representations/Perception/BallCandidates.h"
#include "Representations/Perception/MultiBallPercept.h"

// tools
#include "Tools/DoubleCamHelpers.h"

// local tools
#include "Tools/BestPatchList.h"
#include "Tools/BallKeyPointExtractor.h"
#include "Tools/CVHaarClassifier.h"
#include "Classifier/CNNClassifier.h"
#include "Tools/BlackSpotExtractor.h"

// debug
#include "Representations/Debug/Stopwatch.h"
#include "Tools/Debug/DebugRequest.h"
#include "Tools/Debug/DebugImageDrawings.h"
#include "Tools/Debug/DebugParameterList.h"
#include "Tools/Debug/DebugModify.h"
#include "Tools/Debug/DebugDrawings.h"

BEGIN_DECLARE_MODULE(BallCandidateDetector)
  PROVIDE(DebugRequest)
  PROVIDE(DebugDrawings)
  PROVIDE(DebugImageDrawings)
  PROVIDE(DebugImageDrawingsTop)
  PROVIDE(DebugParameterList)
  PROVIDE(DebugModify)
  PROVIDE(StopwatchManager)

  REQUIRE(FrameInfo)

  REQUIRE(Image)
  REQUIRE(ImageTop)

  //PROVIDE(GameColorIntegralImage)
  //PROVIDE(GameColorIntegralImageTop)
  REQUIRE(BallDetectorIntegralImage)
  REQUIRE(BallDetectorIntegralImageTop)

  REQUIRE(FieldColorPercept)
  REQUIRE(FieldColorPerceptTop)

  REQUIRE(CameraMatrix)
  REQUIRE(CameraMatrixTop)

  REQUIRE(FieldPercept)
  REQUIRE(FieldPerceptTop)
  //REQUIRE(BodyContour)
  //REQUIRE(BodyContourTop)

  PROVIDE(BallCandidates)
  PROVIDE(BallCandidatesTop)
  PROVIDE(MultiBallPercept)
END_DECLARE_MODULE(BallCandidateDetector)


class BallCandidateDetector: private BallCandidateDetectorBase, private ModuleManager
{
public:
  BallCandidateDetector();
  ~BallCandidateDetector();

  void execute(CameraInfo::CameraID id);

  virtual void execute()
  {
    getMultiBallPercept().reset();
    execute(CameraInfo::Bottom);
    execute(CameraInfo::Top);
  }
 
private:
  struct Parameters: public ParameterList
  {
    Parameters() : ParameterList("BallCandidateDetector")
    {
      
      PARAMETER_REGISTER(keyDetector.borderRadiusFactorClose) = 0.5;
      PARAMETER_REGISTER(keyDetector.borderRadiusFactorFar) = 0.8;

      PARAMETER_REGISTER(heuristic.maxGreenInsideRatio) = 0.3;
      PARAMETER_REGISTER(heuristic.minGreenBelowRatio) = 0.5;
      PARAMETER_REGISTER(heuristic.blackDotsMinCount) = 1;
      PARAMETER_REGISTER(heuristic.minBlackDetectionSize) = 20;

      PARAMETER_REGISTER(haarDetector.execute) = true;
      PARAMETER_REGISTER(haarDetector.minNeighbors) = 0;
      PARAMETER_REGISTER(haarDetector.windowSize) = 12;
      PARAMETER_REGISTER(haarDetector.model_file) = "lbp1.xml";

      PARAMETER_REGISTER(maxNumberOfKeys) = 4;
      PARAMETER_REGISTER(numberOfExportBestPatches) = 2;

      PARAMETER_REGISTER(postBorderFactorClose) = 1.0;
      PARAMETER_REGISTER(postBorderFactorFar) = 0.0;
      PARAMETER_REGISTER(postMaxCloseSize) = 60;

      PARAMETER_REGISTER(contrastUse) = false;
      PARAMETER_REGISTER(contrastVariant) = 1;
      PARAMETER_REGISTER(contrastMinimum) = 50;

      PARAMETER_REGISTER(blackKeysCheck.enable) = false;
      PARAMETER_REGISTER(blackKeysCheck.minSizeToCheck) = 60;
      PARAMETER_REGISTER(blackKeysCheck.minValue) = 20;
      
      syncWithConfig();
    }

    BallKeyPointExtractor::Parameter keyDetector;

    struct Heuristics {
      double maxGreenInsideRatio;
      double minGreenBelowRatio;
      double blackDotsMinRatio;

      int blackDotsMinCount;
      int minBlackDetectionSize;
    } heuristic;

    struct HaarDetector {
      bool execute;
      int minNeighbors;
      int windowSize;
      std::string model_file;
    } haarDetector;

    struct BlackKeysCheck {
      bool enable;
      int minSizeToCheck;
      int minValue;
    } blackKeysCheck;

    int maxNumberOfKeys;
    int numberOfExportBestPatches;
    double postBorderFactorClose;
    double postBorderFactorFar;
    int postMaxCloseSize;

    bool contrastUse;
    int contrastVariant;
    double contrastMinimum;

  } params;


private:

  bool blackKeysOK(const BestPatchList::Patch& patch) 
  {
    static BestPatchList bbest;
    bbest.clear();
    BlackSpotExtractor::calculateKeyPointsBlackBetter(getBallDetectorIntegralImage(), bbest, patch.min.x, patch.min.y, patch.max.x, patch.max.y);

    if(bbest.size() == 0) {
      return false;
    }

    BestPatchList::reverse_iterator b = bbest.rbegin();
    return (*b).value > params.blackKeysCheck.minValue;
  }

private:
  CVHaarClassifier cvHaarClassifier;
  CNNClassifier cnnClassifier;
  ModuleCreator<BallKeyPointExtractor>* theBallKeyPointExtractor;
  BestPatchList best;

private:
  void calculateCandidates();
  void addBallPercept(const Vector2i& center, double radius);
  void extractPatches();
  double calculateContrast(const Image& image,  const FieldColorPercept& fielColorPercept, int x0, int y0, int x1, int y1, int size);
  double calculateContrastIterative(const Image& image,  const FieldColorPercept& fielColorPercept, int x0, int y0, int x1, int y1, int size);
  double calculateContrastIterative2nd(const Image& image,  const FieldColorPercept& fielColorPercept, int x0, int y0, int x1, int y1, int size);
  
private:
  CameraInfo::CameraID cameraID;

  DOUBLE_CAM_PROVIDE(BallCandidateDetector, DebugImageDrawings);

  // double cam stuff
  DOUBLE_CAM_REQUIRE(BallCandidateDetector, Image);
  DOUBLE_CAM_REQUIRE(BallCandidateDetector, CameraMatrix);
  DOUBLE_CAM_REQUIRE(BallCandidateDetector, FieldPercept);
  //DOUBLE_CAM_REQUIRE(BallCandidateDetector, BodyContour);
  DOUBLE_CAM_REQUIRE(BallCandidateDetector, BallDetectorIntegralImage);
  DOUBLE_CAM_REQUIRE(BallCandidateDetector, FieldColorPercept);

  DOUBLE_CAM_PROVIDE(BallCandidateDetector, BallCandidates);
};//end class BallCandidateDetector

#endif // _BallCandidateDetector_H_
