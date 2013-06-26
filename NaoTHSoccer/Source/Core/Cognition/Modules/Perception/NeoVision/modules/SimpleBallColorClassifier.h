/* 
 * File:   SimpleBallColorClassifier.h
 * Author: claas
 * Author: Heinrich Mellmann
 *
 * Created on 15. M�rz 2011, 15:56
 */

#ifndef _SimpleBallColorClassifier_H_
#define _SimpleBallColorClassifier_H_

#include <ModuleFramework/Module.h>

// Representations
#include "Representations/Perception/FieldColorPercept.h"
#include "Representations/Perception/SimpleGoalColorPercept.h"
#include "Representations/Perception/SimpleBallColorPercept.h"
#include "Representations/Infrastructure/FrameInfo.h"
#include "Tools/ImageProcessing/Histograms.h"
//#include "Tools/ImageProcessing/ColoredGrid.h"
#include "Representations/Infrastructure/Image.h" // just for debug


// Tools
#include "Tools/Math/Vector2.h"
#include "Tools/Math/Vector3.h"
#include <Tools/DataStructures/ParameterList.h>
#include "Tools/Debug/DebugImageDrawings.h"

//////////////////// BEGIN MODULE INTERFACE DECLARATION ////////////////////

BEGIN_DECLARE_MODULE(SimpleBallColorClassifier)
  //REQUIRE(ColoredGrid)
  REQUIRE(FrameInfo)
  REQUIRE(Histograms)
  REQUIRE(HistogramsTop)
  REQUIRE(Image)
  REQUIRE(ImageTop)
  REQUIRE(FieldColorPercept)
  REQUIRE(FieldColorPerceptTop)
  REQUIRE(SimpleGoalColorPercept)
  REQUIRE(SimpleGoalColorPerceptTop)

  PROVIDE(SimpleBallColorPercept)
  PROVIDE(SimpleBallColorPerceptTop)
END_DECLARE_MODULE(SimpleBallColorClassifier)

//////////////////// END MODULE INTERFACE DECLARATION //////////////////////


class SimpleBallColorClassifier : public  SimpleBallColorClassifierBase
{
public:
  double histV[COLOR_CHANNEL_VALUE_COUNT];

  SimpleBallColorClassifier();
  virtual ~SimpleBallColorClassifier(){}

  void execute()
  {
    execute(CameraInfo::Bottom);
    execute(CameraInfo::Top);

    // reset the debug drawing canvas to bottom
    CANVAS_PX_BOTTOM;
  }

  void execute(const CameraInfo::CameraID id);

private:

  class Parameters: public ParameterList
  {
  public:

    Parameters() : ParameterList("SimpleBallColorClassifierParameters")
    {
      PARAMETER_REGISTER(ballColorMax.y) = 200;
      PARAMETER_REGISTER(ballColorMax.u) = 160;
      PARAMETER_REGISTER(dist2yellow.v) = 12;

      syncWithConfig();

      DebugParameterList::getInstance().add(this);
    }

    ~Parameters()
    {
      DebugParameterList::getInstance().remove(this);
    }

    DoublePixel dist2yellow;

    DoublePixel ballColorMax;
    DoublePixel ballColorMin;
  };

  Parameters ballParams;

  // id of the camera the module is curently running on
  CameraInfo::CameraID cameraID;
};

#endif  /* _SimpleBallColorClassifier_H_ */