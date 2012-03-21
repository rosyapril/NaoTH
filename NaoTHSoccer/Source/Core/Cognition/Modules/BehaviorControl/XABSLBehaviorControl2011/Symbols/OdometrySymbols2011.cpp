/**
* @file OdometrySymbols.cpp
*
* @author <a href="mailto:martius@informatik.hu-berlin.de">Martin Martius</a>
* Implementation of class OdometrySymbols
*/

#include "OdometrySymbols2011.h"
#include "Tools/Math/Common.h"
#include "Tools/Debug/DebugRequest.h"
#include "Tools/Debug/DebugDrawings.h"

OdometrySymbols2011* OdometrySymbols2011::theInstance = NULL;

void OdometrySymbols2011::registerSymbols(xabsl::Engine& engine)
{
  engine.registerDecimalInputSymbol("odometry.x", &getOdometry_x);
  engine.registerDecimalInputSymbol("odometry.y", &getOdometry_y);
  engine.registerDecimalInputSymbol("odometry.rotation", &getOdometry_angle);

  engine.registerDecimalOutputSymbol("odometry.relative_point.x", &relativePoint.x);
  engine.registerDecimalOutputSymbol("odometry.relative_point.y", &relativePoint.y);


  engine.registerDecimalInputSymbol("odometry.preview.x", &getOdometryPreview_x);
  engine.registerDecimalInputSymbolDecimalParameter("odometry.preview.x", "odometry.preview.x.x", &previewParameter.x);
  engine.registerDecimalInputSymbolDecimalParameter("odometry.preview.x", "odometry.preview.x.y", &previewParameter.y);

  engine.registerDecimalInputSymbol("odometry.preview.y", &getOdometryPreview_y);
  engine.registerDecimalInputSymbolDecimalParameter("odometry.preview.y", "odometry.preview.y.x", &previewParameter.x);
  engine.registerDecimalInputSymbolDecimalParameter("odometry.preview.y", "odometry.preview.y.y", &previewParameter.y);


  DEBUG_REQUEST_REGISTER("OdometrySymbols:relativePoint", "draw the relativePoint", false);
}//end registerSymbols


void OdometrySymbols2011::execute()
{
  Pose2D odometryDelta = lastRobotOdometry - odometryData;
  lastRobotOdometry = odometryData;

  relativePoint = odometryDelta * relativePoint;

  DEBUG_REQUEST("OdometrySymbols:relativePoint",
    FIELD_DRAWING_CONTEXT;
    PEN("FF0000", 1);
    FILLOVAL(relativePoint.x, relativePoint.y, 25, 25);
  );
}//end update

double OdometrySymbols2011::getOdometry_x()
{
  return theInstance->odometryData.translation.x;
}//end get x translation

double OdometrySymbols2011::getOdometry_y()
{
  return theInstance->odometryData.translation.y;
}//end get y translation

double OdometrySymbols2011::getOdometry_angle()
{
  return theInstance->odometryData.rotation;
}//end get rotation

double OdometrySymbols2011::getOdometryPreview_x()
{
  return (theInstance->motionStatus.plannedMotion.hip/theInstance->previewParameter).x;
}//end get getOdometryPreview_x

double OdometrySymbols2011::getOdometryPreview_y()
{
  return (theInstance->motionStatus.plannedMotion.hip/theInstance->previewParameter).y;
}//end get getOdometryPreview_y

