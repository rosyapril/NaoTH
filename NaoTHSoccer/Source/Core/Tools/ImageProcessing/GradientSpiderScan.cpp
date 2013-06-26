/**
* @file Tools/ImageProcessing/GradientSpiderScan.cpp
* 
* Utility class which performs a GradientSpiderScan algorithm for blob region scanning
*
*/

#include "GradientSpiderScan.h"


GradientSpiderScan::GradientSpiderScan(const Image& theImage, CameraInfo::CameraID camID)
:
  theImage(theImage),
  cameraID(camID)
{
  init();
  DEBUG_REQUEST_REGISTER("NeoVision:MaximumRedBallDetector:Y_Channel","..", false); 
  DEBUG_REQUEST_REGISTER("NeoVision:MaximumRedBallDetector:U_Channel","..", false); 
  DEBUG_REQUEST_REGISTER("NeoVision:MaximumRedBallDetector:V_Channel","..", false); 
}

void GradientSpiderScan::setMaxBeamLength(unsigned int length)
{
  max_length_of_beam = length;
}

void GradientSpiderScan::setCurrentGradientThreshold(double threshold)
{
  currentGradientThreshold = threshold;
}

void GradientSpiderScan::setCurrentMeanThreshold(double threshold)
{
  currentMeanThreshold = threshold;
}

void GradientSpiderScan::setDynamicThresholdY(double threshold)
{
  dynamicThresholdY = threshold;
  useDynamicThresholdY = true;
}

void GradientSpiderScan::setImageColorChannelNumber(int channelNumber)
{
  imageChannelNumber = channelNumber;
}

void GradientSpiderScan::setMaxNumberOfScans(unsigned int maxScans)
{
  maxNumberOfScans = maxScans;
}

void GradientSpiderScan::setUseVUdifference(bool value)
{
  useVUdifference = value;
}

void GradientSpiderScan::init()
{
  drawScanLines = false;
  max_length_of_beam = 30; //maximum length of the scan line
  currentGradientThreshold = 30; //...
  currentMeanThreshold = 30; //...
  dynamicThresholdY = 0;
  useDynamicThresholdY = false;
  imageChannelNumber = 2;
  imageChannelValidate = 1;
  maxNumberOfScans = 15; //maximum number of scanlines ...
  CANVAS_PX(cameraID);
  maxChannelDif = 0.5;
}

void GradientSpiderScan::setDrawScanLines(bool draw)
{
  drawScanLines = draw;
}

void GradientSpiderScan::scan(const Vector2<int>& start, ScanPointList& goodPoints, ScanPointList& badPoints)
{
  Scans scans; //a list of scans to perform

  scanPixelBuffer.init();

  //add the standard scan lines
  scans.add(start, Vector2<int>( 0,-2));// down
  scans.add(start, Vector2<int>(-2,-2));// to the lower left
  scans.add(start, Vector2<int>(-2, 2));// to the upper left
  scans.add(start, Vector2<int>(-2, 0));// to the left
  scans.add(start, Vector2<int>( 2, 0));// to the right
  scans.add(start, Vector2<int>( 2, 2));// to the upper right
  scans.add(start, Vector2<int>( 2,-2));// to the lower right
  scans.add(start, Vector2<int>( 0, 2));// up
  
  startPixel = theImage.get(start.x, start.y);
  scan(goodPoints, badPoints, scans);
  useDynamicThresholdY = false;
}

void GradientSpiderScan::scan(ScanPointList& goodPoints, ScanPointList& badPoints, Scans scans)
{
  //remember the number of performed scans
  int numberOfScans = 0;

  while(scans.number > 0 && numberOfScans < maxNumberOfScans) //while there are scans to perform (but not too much)
  {
    // ...
    numberOfScans++;
    //Vector2<int>& direction = scans.direction[0]; //get the direction of the scan line
//    Vector2<int> currentPoint(scans.start[0]);    //and the starting point

    int badPointsBeforeScan = badPoints.length; //remember the number of bad points (to see if one was added after the scan)

    //perform the scan, see if it was successful
    if(scanLine(scans.start[0], scans.direction[0], goodPoints, badPoints))
    {
      if(badPoints.length > badPointsBeforeScan) //if a bad point was found
      {
        const Vector2<int>& badPoint = badPoints[badPoints.length-1]; //get this point

        //if the pixel lies at the image border and was not the result of a scan along that border
        if(pixelAtImageBorder(badPoint, 2) && !isBorderScan(scans.start[0], scans.direction[0], 2))
        {
          if(badPoint.x <= 1 || (unsigned int)badPoint.x >= theImage.cameraInfo.resolutionWidth-2)
          {
            //... its not reliable and we should scan along the imageborder to find the real border point
            scans.add(badPoint, Vector2<int>(0,  1));
            scans.add(badPoint, Vector2<int>(0, -1));
          }
          //same for the vertical borders
          else if(badPoint.y <= 1 || (unsigned int)badPoint.y >= theImage.cameraInfo.resolutionHeight-2)
          {
            scans.add(badPoint, Vector2<int>( 1, 0));
            scans.add(badPoint, Vector2<int>(-1, 0));
          }

          badPoints.remove(badPoints.length-1); //kick the unreliable Pixel
        }//end if pixelAtImageBorder
      }//end if BadPointFound
    }//end if borderPointFound

    scans.remove(0); //scan done, remove it from the list
  }//end while scans left;

}//end spiderSearch

bool GradientSpiderScan::scanLine(const Vector2<int>& start, const Vector2<int>& direction, ScanPointList& goodPoints, ScanPointList& badPoints)
{
  Vector2<int> currentPoint(start);  //set the starting point
  Vector2<int> borderPoint;      // to remember the border point ...
  bool borderPointFound = false; //if one was found

  Vector2<int> lastSearchColorPoint(-1,-1);

  Pixel lastPixel = startPixel;
  scanPixelBuffer.add(startPixel.channels[imageChannelNumber]);
  currentPoint += direction;

  int maxJump = -1;
  bool jumpFound = false;
  bool isBright = false;
  Vector2<int> brightBeginPoint;
  bool brightPointFound = false;
  bool brightRegionEndFound = false;

  //expand in the selected direction
  for(unsigned int j = 1; j < max_length_of_beam  && !borderPointFound; j++)
  {

    // check the current point
    if(  // point outside the image
        !pixelInImage(currentPoint))
    {
      break;
    }//end if

    ////////////////////////////////
    // process the current point
    ////////////////////////////////

    Pixel pixel = theImage.get(currentPoint.x, currentPoint.y);
	/*DEBUG_REQUEST("NeoVision:MaximumRedBallDetector:Y_Channel",
					);*/

    int newJump = 0;
    int meanJump = 0;

    if(useVUdifference)
    {
      newJump = abs(static_cast<int>(pixel.v - pixel.u - (lastPixel.v - lastPixel.u)));
      meanJump = abs(scanPixelBuffer.getAverage() - static_cast<int>(lastPixel.v - lastPixel.u));
    }
    else
    {
      newJump = abs(static_cast<int>(pixel.channels[imageChannelNumber] - lastPixel.channels[imageChannelNumber]));
      abs(scanPixelBuffer.getAverage() - static_cast<int>(lastPixel.channels[imageChannelNumber]));
  	  //UNUSED: int validateJump = abs(static_cast<int>(pixel.channels[imageChannelValidate] - lastPixel.channels[imageChannelValidate]));
    }

    bool isBorder = false;

    if(jumpFound) 
    { // look for the maximal jump
      if(newJump > maxJump) 
      {
        maxJump = newJump;
        borderPoint = currentPoint - direction/2;
      } 
      else 
      {
        isBorder = true;
      }
    } 
    else 
    { // look for a jump
      if(!isBright && useDynamicThresholdY && pixel.y > dynamicThresholdY)
      {
		    isBright = true;
        brightBeginPoint = currentPoint;
        brightPointFound = true;
        if(drawScanLines)
        {
		      LINE_PX(ColorClasses::pink, (unsigned int)(brightBeginPoint.x-4), (unsigned int)(brightBeginPoint.y), (unsigned int)(brightBeginPoint.x+4), (unsigned int)(brightBeginPoint.y));
		      LINE_PX(ColorClasses::pink, (unsigned int)(brightBeginPoint.x), (unsigned int)(brightBeginPoint.y-4), (unsigned int)(brightBeginPoint.x), (unsigned int)(brightBeginPoint.y+4));
        }
      }
      else if(isBright && useDynamicThresholdY && pixel.y < dynamicThresholdY)
      {
        brightRegionEndFound = true;
        isBright = false;
      }

  //      jumpFound = (newJump > currentGradientThreshold && (double) newJump / (double) validateJump > maxChannelDif) || 
		  jumpFound = newJump > currentGradientThreshold  || 
                  meanJump > currentMeanThreshold;
      
      maxJump = newJump;
      borderPoint = currentPoint - direction/2;
    }

    if(isBorder)
    {
      //borderPoint = currentPoint;
      borderPointFound = true;
    }
    else if(! borderPointFound)
    {
      scanPixelBuffer.add(pixel.channels[imageChannelNumber]);
    }//end if

    if(drawScanLines)
    {
      if(!isBorder) 
      {
        // POINT_PX(ColorClasses::green, (unsigned int)(currentPoint.x), (unsigned int)(currentPoint.y));
		    POINT_PX(ColorClasses::red, (unsigned int)(borderPoint.x), (unsigned int)(borderPoint.y));
      } 
      else 
      {
        //POINT_PX(ColorClasses::red, (unsigned int)(borderPoint.x), (unsigned int)(borderPoint.y));
    	  LINE_PX(ColorClasses::green, (unsigned int) start.x, (unsigned int) start.y, (unsigned int)(currentPoint.x), (unsigned int)(currentPoint.y));
      }
    }
    lastPixel = pixel;
    currentPoint += direction;
  }//end for

  // no valid border point fount, but a ver y bright one (maybe line)
  // or a valid
  if( (!borderPointFound  && brightPointFound) || (borderPointFound && brightPointFound && !brightRegionEndFound))
  {
    borderPoint = brightBeginPoint;
    borderPointFound = true;
    if(drawScanLines)
    {
      LINE_PX(ColorClasses::skyblue, (unsigned int) start.x, (unsigned int) start.y, (unsigned int)(borderPoint.x), (unsigned int)(borderPoint.y));
    }
  }
  if(borderPointFound) //if a point was found ...
  {
    //that point was followed by a border pixel, does not lie at the images border or is the result of a scan along that border ...
    if(!pixelAtImageBorder(borderPoint, 2) || isBorderScan(borderPoint, direction, 2)) {
      goodPoints.add(borderPoint); //it's good point
      if(drawScanLines)
      {
		    LINE_PX(ColorClasses::blue, (unsigned int)(borderPoint.x-4), (unsigned int)(borderPoint.y), (unsigned int)(borderPoint.x+4), (unsigned int)(borderPoint.y));
		    LINE_PX(ColorClasses::blue, (unsigned int)(borderPoint.x), (unsigned int)(borderPoint.y-4), (unsigned int)(borderPoint.x), (unsigned int)(borderPoint.y+4));
      }
    } else {
      badPoints.add(borderPoint);  //otherwise it's a bad one
    }
  }//end if

  return borderPointFound; //return if a borderpoint was found
}//end scanLine

//check wether a scan line goes along an image border
bool GradientSpiderScan::isBorderScan(const Vector2<int>& point, const Vector2<int>& direction, int borderWidth) const
{
  return ((point.x<borderWidth || (unsigned int)point.x>=theImage.cameraInfo.resolutionWidth-borderWidth) && direction.x==0) ||
         ((point.y<borderWidth || (unsigned int)point.y>=theImage.cameraInfo.resolutionHeight-borderWidth) && direction.y==0);
}//end isBorderScan

//check wether a point is in the image
bool GradientSpiderScan::pixelInImage(const Vector2<int>& pixel) const
{
  return (pixel.x >= 0 && pixel.y >= 0 &&
          (unsigned int)pixel.x<theImage.cameraInfo.resolutionWidth &&
          (unsigned int)pixel.y<theImage.cameraInfo.resolutionHeight);
}//end pixelInImage

//check wether a point lies at the images border
bool GradientSpiderScan::pixelAtImageBorder(const Vector2<int>& pixel, int borderWidth) const
{
  return (pixel.x < borderWidth || pixel.y < borderWidth ||
          (unsigned int)pixel.x>=theImage.cameraInfo.resolutionWidth-borderWidth ||
          (unsigned int)pixel.y>=theImage.cameraInfo.resolutionHeight-borderWidth);
}//end pixelAtImageBorder