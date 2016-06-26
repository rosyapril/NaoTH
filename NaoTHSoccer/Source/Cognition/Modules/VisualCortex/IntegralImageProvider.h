/**
* @file IntegralImageProvider.h
*
* Definition of class IntegralImageProvider
*/

#ifndef _IntegralImageProvider_H_
#define _IntegralImageProvider_H_

#include <ModuleFramework/Module.h>
#include <ModuleFramework/Representation.h>

// common tools
//#include <Tools/ColorClasses.h>
#include <Tools/Math/Vector2.h>

#include <Representations/Infrastructure/Image.h>
#include <Representations/Infrastructure/FrameInfo.h>
#include "Representations/Perception/FieldColorPercept.h"
#include "Representations/Perception/MultiChannelIntegralImage.h"
#include "Representations/Perception/BodyContour.h"

// needed?
//#include "Representations/Perception/FieldPercept.h"
//#include "Representations/Perception/BodyContour.h"

// tools
#include "Tools/DoubleCamHelpers.h"

#include "Tools/Debug/DebugRequest.h"
#include "Tools/Debug/DebugImageDrawings.h"
#include "Tools/Debug/DebugParameterList.h"
#include "Representations/Perception/ColorTable64.h"


BEGIN_DECLARE_MODULE(IntegralImageProvider)
  PROVIDE(DebugRequest)
  PROVIDE(DebugImageDrawings)
  PROVIDE(DebugImageDrawingsTop)
  PROVIDE(DebugParameterList)

  REQUIRE(FrameInfo)
  REQUIRE(Image)
  REQUIRE(ImageTop)

  REQUIRE(FieldColorPercept)
  REQUIRE(FieldColorPerceptTop)

  REQUIRE(ColorTable64)

  // HACK
  REQUIRE(BodyContour)

  PROVIDE(MultiChannelIntegralImage)
  PROVIDE(MultiChannelIntegralImageTop)
END_DECLARE_MODULE(IntegralImageProvider)


class IntegralImageProvider: private IntegralImageProviderBase
{
public:
  IntegralImageProvider();
  ~IntegralImageProvider();

  void execute(CameraInfo::CameraID id);

  virtual void execute()
  {
    execute(CameraInfo::Top);
    execute(CameraInfo::Bottom);
  }
 
private:
  CameraInfo::CameraID cameraID;

  void integralBild();
  void integralBildBottom();

private:     
  
  DOUBLE_CAM_PROVIDE(IntegralImageProvider, DebugImageDrawings);

  // double cam stuff
  DOUBLE_CAM_REQUIRE(IntegralImageProvider, Image);
  DOUBLE_CAM_REQUIRE(IntegralImageProvider, FieldColorPercept);

  DOUBLE_CAM_PROVIDE(IntegralImageProvider, MultiChannelIntegralImage);
          
};//end class IntegralImageProvider

#endif // _IntegralImageProvider_H_
