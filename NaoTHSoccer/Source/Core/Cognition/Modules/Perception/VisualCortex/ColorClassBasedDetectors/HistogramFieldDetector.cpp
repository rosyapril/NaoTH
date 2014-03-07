
/**
* @file HistogramFieldDetector.cpp
*
* Implementation of class HistogramFieldDetector
*
*/

#include "HistogramFieldDetector.h"

HistogramFieldDetector::HistogramFieldDetector()
:
  cameraID(CameraInfo::Bottom)
{
  fieldColor = ColorClasses::green;
  lineColor = ColorClasses::white;

  DEBUG_REQUEST_REGISTER("Vision:ColorClassBasedDetectors:HistogramFieldDetector:mark_rectangle", "mark boundary rectangle of the detected field on the image", false);

  getFieldPerceptRaw().setDimension(Vector2i(getImage().width(), getImage().height()));
  getFieldPerceptRawTop().setDimension(Vector2i(getImageTop().width(), getImageTop().height()));
}

void HistogramFieldDetector::execute(CameraInfo::CameraID id)
{
  cameraID = id;
  CANVAS_PX(id);
  getFieldPerceptRaw().reset();
  largestAreaRectangle.clear();

  Vector2i min(0,0);
  Vector2i max(0,0);

  getFieldRectFromHistogram(min, max);

  getFieldPerceptRaw().setField(largestAreaRectangle, getArtificialHorizon());

  //if die enclosured area of the polygon/rectangle is lower than 11200 squarepixels the area is to small
  //TODO: this could be an topic of some kind of learning
  if(largestAreaRectangle.getArea() >= 5600)
  {
    getFieldPerceptRaw().valid = true;
  }
  DEBUG_REQUEST( "Vision:ColorClassBasedDetectors:HistogramFieldDetector:mark_rectangle",
    ColorClasses::Color color = getFieldPerceptRaw().valid ? ColorClasses::green : ColorClasses::red;
      RECT_PX
      (
          color,
          getFieldPerceptRaw().getValidField().points[0].x,
          getFieldPerceptRaw().getValidField().points[0].y,
          getFieldPerceptRaw().getValidField().points[2].x,
          getFieldPerceptRaw().getValidField().points[2].y
      );
  );
}//end execute

void HistogramFieldDetector::getFieldRectFromHistogram(Vector2i& min, Vector2i& max)
{
  Vector2i actMin(-1,-1);
  Vector2i actMax(-1,-1);
  const int minXHistLevel = (int) (getColoredGrid().uniformGrid.width * 0.10);
  const int minYHistLevel = (int) (getColoredGrid().uniformGrid.height * 0.10);

  int whiteCount = 0;
  int otherCount = 0;

  //go through histogram for values along the y axis and pick the largest area of green color as partial field y axis
  for (unsigned int y = 0; y < getColoredGrid().uniformGrid.height; y++)
  {
    if
    (
      (
      getColorClassesHistograms().xHistogram[fieldColor].rawData[y] >= minXHistLevel
      || 0.9 * getColorClassesHistograms().xHistogram[fieldColor].rawData[y] + 0.1 * getColorClassesHistograms().xHistogram[lineColor].rawData[y] >= minXHistLevel
      )
      && whiteCount <= LINE_THICKNESS
    )
    {
      otherCount = 0;
      if(getColorClassesHistograms().xHistogram[fieldColor].rawData[y] >= minXHistLevel)
      {
        if(whiteCount > 0)
        {
          whiteCount = 0;
        }
      }
      if(actMin.y == -1)//actMax.y)
      {
        actMin.y = y;
        actMax.y = y;
      }
      else
      {
        actMax.y = y;
      }
    }
    else if(getColorClassesHistograms().xHistogram[lineColor].rawData[y] >= minXHistLevel && (actMax.y - actMin.y) > 1)
    {
      whiteCount++;
      otherCount = 0;
    }
  else
  {
      if(otherCount > 2)
      {
        if( (actMax.y - actMin.y) >= (max.y - min.y) )
        {
          min.y = actMin.y;
          max.y = actMax.y;
          whiteCount = 0;
        }
        actMin.y = -1;
        actMax.y = -1;
      }
      otherCount++;
    }
  }
  if( (actMax.y - actMin.y) >= (max.y - min.y) )
  {
    min.y = actMin.y;
    max.y = actMax.y;
  }

  //go through histogram for values along the x axis and pick the largest area of green color as partial field x axis
  for (unsigned int x = 0; x < getColoredGrid().uniformGrid.width; x++)
  {
    if
    (
      (
      getColorClassesHistograms().yHistogram[fieldColor].rawData[x] >= minYHistLevel
      || 0.9 * getColorClassesHistograms().yHistogram[fieldColor].rawData[x] + 0.1 * getColorClassesHistograms().yHistogram[lineColor].rawData[x] >= minYHistLevel
      )
    )
    {
      if(actMin.x == -1)//actMax.x)
      {
        actMin.x = x;
        actMax.x = x;
      }
      else
      {
        actMax.x = x;
      }
    }
    else
    {
      if( (actMax.x - actMin.x) >= (max.x - min.x) )
      {
        min.x = actMin.x;
        max.x = actMax.x;
      }
//      actMin.x = -1;
//      actMax.x = -1;
    }
  }
  if( (actMax.x - actMin.x) >= (max.x - min.x) )
  {
    min.x = actMin.x;
    max.x = actMax.x;
  }

  if(min.x > -1 && min.x > -1 && max.x > -1 && max.y > -1)
  {
    largestAreaRectangle.add(getColoredGrid().getImagePoint(min));
    Vector2i v1(min.x,max.y);
    largestAreaRectangle.add(getColoredGrid().getImagePoint(v1));
    largestAreaRectangle.add(getColoredGrid().getImagePoint(max));
    Vector2i v2(max.x,min.y);
    largestAreaRectangle.add(getColoredGrid().getImagePoint(v2));
  }

}//end getFieldRectFromHistogram