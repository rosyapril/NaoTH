#include "BaseColorClassifier.h"

// Debug
#include "Tools/Debug/DebugRequest.h"
#include "Tools/Debug/DebugDrawings.h"
#include "Tools/Debug/DebugBufferedOutput.h"
#include "Tools/Debug/DebugImageDrawings.h"
#include "Tools/Debug/Stopwatch.h"
#include "Tools/Debug/DebugModify.h"

BaseColorClassifier::BaseColorClassifier()
:
  calibCount(0),
  calibrating(false),
  orangeBallCalibRectTop("OrangeBallTop", ColorClasses::orange, 140, 155, 180, 170),
  orangeBallCalibRect("OrangeBall", ColorClasses::orange, 140, 155, 180, 170),
  yellowGoalPostLeftCalibRectTop("YellowGoalPostLeftTop", ColorClasses::yellow, 60, 40, 80, 150),
  yellowGoalPostLeftCalibRect("YellowGoalPostLeft", ColorClasses::yellow, 60, 40, 80, 150),
  yellowGoalPostRightCalibRectTop("YellowGoalPostRightTop", ColorClasses::yellow, 240, 40, 260, 150),
  yellowGoalPostRightCalibRect("YellowGoalPostRight", ColorClasses::yellow, 240, 40, 260, 150),
  blueGoalLeftCalibRectTop("BlueGoalPostLeftTop", ColorClasses::skyblue, 60, 40, 80, 150),
  blueGoalLeftCalibRect("BlueGoalPostLeft", ColorClasses::skyblue, 60, 40, 80, 150),
  blueGoalRightCalibRectTop("BlueGoalPostRightTop", ColorClasses::skyblue, 240, 40, 260, 150),
  blueGoalRightCalibRect("BlueGoalPostRight", ColorClasses::skyblue, 240, 40, 260, 150),
  blueTeamCalibRectTop("BlueTeamTop", ColorClasses::blue, 200, 155, 240, 170),
  blueTeamCalibRect("BlueTeam", ColorClasses::blue, 200, 155, 240, 170),
  redTeamCalibRectTop("PinkTeamTop", ColorClasses::pink, 80, 155, 120, 170),
  redTeamCalibRect("PinkTeam", ColorClasses::pink, 80, 155, 120, 170),
  whiteLinesCalibRectTop("WhiteLinesTop", ColorClasses::white, 80, 170, 240, 175),
  whiteLinesCalibRect("WhiteLines", ColorClasses::white, 80, 170, 240, 175),
  orangeBallColorCalibratorTop("OrangeBallTop", ColorClasses::orange),
  orangeBallColorCalibrator("OrangeBall", ColorClasses::orange),
  yellowGoalColorCalibratorTop("YellowGoalTop", ColorClasses::yellow),
  yellowGoalColorCalibrator("YellowGoal", ColorClasses::yellow),
  blueGoalColorCalibratorTop("BlueGoalTop", ColorClasses::skyblue),
  blueGoalColorCalibrator("BlueGoal", ColorClasses::skyblue),
  blueTeamColorCalibratorTop("BlueTeamTop", ColorClasses::blue),
  blueTeamColorCalibrator("BlueTeam", ColorClasses::blue),
  redTeamColorCalibratorTop("RedTeamTop", ColorClasses::pink),
  redTeamColorCalibrator("RedTeam", ColorClasses::pink),
  whiteLinesColorCalibratorTop("WhiteLinesTop", ColorClasses::white),
  whiteLinesColorCalibrator("WhiteLines", ColorClasses::white)
{
  DEBUG_REQUEST_REGISTER("Vision:ColorClassifiers:BaseColorClassifier:CamTop:set_field_in_image", " ", false);
  DEBUG_REQUEST_REGISTER("Vision:ColorClassifiers:BaseColorClassifier:CamBottom:set_field_in_image", " ", false);
  DEBUG_REQUEST_REGISTER("Vision:ColorClassifiers:BaseColorClassifier:CamTop:set_ball_in_image", " ", false);
  DEBUG_REQUEST_REGISTER("Vision:ColorClassifiers:BaseColorClassifier:CamBottom:set_ball_in_image", " ", false);
  DEBUG_REQUEST_REGISTER("Vision:ColorClassifiers:BaseColorClassifier:CamTop:set_goal_in_image", " ", false);
  DEBUG_REQUEST_REGISTER("Vision:ColorClassifiers:BaseColorClassifier:CamBottom:set_goal_in_image", " ", false);
  DEBUG_REQUEST_REGISTER("Vision:ColorClassifiers:BaseColorClassifier:CamTop:set_red_team_in_image", " ", false);
  DEBUG_REQUEST_REGISTER("Vision:ColorClassifiers:BaseColorClassifier:CamBottom:set_red_team_in_image", " ", false);
  DEBUG_REQUEST_REGISTER("Vision:ColorClassifiers:BaseColorClassifier:CamTop:set_blue_team_in_image", " ", false);
  DEBUG_REQUEST_REGISTER("Vision:ColorClassifiers:BaseColorClassifier:CamBottom:set_blue_team_in_image", " ", false);
  DEBUG_REQUEST_REGISTER("Vision:ColorClassifiers:BaseColorClassifier:CamTop:set_lines_in_image", " ", false);
  DEBUG_REQUEST_REGISTER("Vision:ColorClassifiers:BaseColorClassifier:CamBottom:set_lines_in_image", " ", false);

  DEBUG_REQUEST_REGISTER("Vision:ColorClassifiers:BaseColorClassifier:CamTop:calibrate_colors:field", " ", false);
  DEBUG_REQUEST_REGISTER("Vision:ColorClassifiers:BaseColorClassifier:CamBottom:calibrate_colors:field", " ", false);
  DEBUG_REQUEST_REGISTER("Vision:ColorClassifiers:BaseColorClassifier:CamTop:calibrate_colors:line", " ", false);
  DEBUG_REQUEST_REGISTER("Vision:ColorClassifiers:BaseColorClassifier:CamBottom:calibrate_colors:line", " ", false);
  DEBUG_REQUEST_REGISTER("Vision:ColorClassifiers:BaseColorClassifier:CamTop:calibrate_colors:ball", " ", false);
  DEBUG_REQUEST_REGISTER("Vision:ColorClassifiers:BaseColorClassifier:CamBottom:calibrate_colors:ball", " ", false);
  DEBUG_REQUEST_REGISTER("Vision:ColorClassifiers:BaseColorClassifier:CamTop:calibrate_colors:blue_goal", " ", false);
  DEBUG_REQUEST_REGISTER("Vision:ColorClassifiers:BaseColorClassifier:CamBottom:calibrate_colors:blue_goal", " ", false);
  DEBUG_REQUEST_REGISTER("Vision:ColorClassifiers:BaseColorClassifier:CamTop:calibrate_colors:yellow_goal", " ", false);
  DEBUG_REQUEST_REGISTER("Vision:ColorClassifiers:BaseColorClassifier:CamBottom:calibrate_colors:yellow_goal", " ", false);
  DEBUG_REQUEST_REGISTER("Vision:ColorClassifiers:BaseColorClassifier:CamTop:calibrate_colors:red_team", " ", false);
  DEBUG_REQUEST_REGISTER("Vision:ColorClassifiers:BaseColorClassifier:CamBottom:calibrate_colors:red_team", " ", false);
  DEBUG_REQUEST_REGISTER("Vision:ColorClassifiers:BaseColorClassifier:CamTop:calibrate_colors:blue_team", " ", false);
  DEBUG_REQUEST_REGISTER("Vision:ColorClassifiers:BaseColorClassifier:CamBottom:calibrate_colors:blue_team", " ", false);

  DEBUG_REQUEST_REGISTER("Vision:ColorClassifiers:BaseColorClassifier:CamTop:calibration_areas:show_field_area", " ", false);
  DEBUG_REQUEST_REGISTER("Vision:ColorClassifiers:BaseColorClassifier:CamBottom:calibration_areas:show_field_area", " ", false);
  DEBUG_REQUEST_REGISTER("Vision:ColorClassifiers:BaseColorClassifier:CamTop:calibration_areas:show_line_area", " ", false);
  DEBUG_REQUEST_REGISTER("Vision:ColorClassifiers:BaseColorClassifier:CamBottom:calibration_areas:show_line_area", " ", false);
  DEBUG_REQUEST_REGISTER("Vision:ColorClassifiers:BaseColorClassifier:CamTop:calibration_areas:show_ball_area", " ", false);
  DEBUG_REQUEST_REGISTER("Vision:ColorClassifiers:BaseColorClassifier:CamBottom:calibration_areas:show_ball_area", " ", false);
  DEBUG_REQUEST_REGISTER("Vision:ColorClassifiers:BaseColorClassifier:CamTop:calibration_areas:show_blue_goal_area", " ", false);
  DEBUG_REQUEST_REGISTER("Vision:ColorClassifiers:BaseColorClassifier:CamBottom:calibration_areas:show_blue_goal_area", " ", false);
  DEBUG_REQUEST_REGISTER("Vision:ColorClassifiers:BaseColorClassifier:CamTop:calibration_areas:show_yellow_goal_area", " ", false);
  DEBUG_REQUEST_REGISTER("Vision:ColorClassifiers:BaseColorClassifier:CamBottom:calibration_areas:show_yellow_goal_area", " ", false);
  DEBUG_REQUEST_REGISTER("Vision:ColorClassifiers:BaseColorClassifier:CamTop:calibration_areas:show_red_team_area", " ", false);
  DEBUG_REQUEST_REGISTER("Vision:ColorClassifiers:BaseColorClassifier:CamBottom:calibration_areas:show_red_team_area", " ", false);
  DEBUG_REQUEST_REGISTER("Vision:ColorClassifiers:BaseColorClassifier:CamTop:calibration_areas:show_blue_team_area", " ", false);
  DEBUG_REQUEST_REGISTER("Vision:ColorClassifiers:BaseColorClassifier:CamBottom:calibration_areas:show_blue_team_area", " ", false);

  DEBUG_REQUEST_REGISTER("Vision:ColorClassifiers:BaseColorClassifier:CamTop:calibrate_colors:reset_data", " ", false);
  DEBUG_REQUEST_REGISTER("Vision:ColorClassifiers:BaseColorClassifier:CamBottom:calibrate_colors:reset_data", " ", false);
  
  adaptationRate = 0.9;
  initPercepts();
  getBaseColorRegionPercept().setPerceptRegions();
  goalIsCalibrating = false;

  orangeBallColorCalibratorTop.addCalibrationRect(orangeBallCalibRectTop);
  orangeBallColorCalibrator.addCalibrationRect(orangeBallCalibRect);
  yellowGoalColorCalibratorTop.addCalibrationRect(yellowGoalPostLeftCalibRectTop);
  yellowGoalColorCalibrator.addCalibrationRect(yellowGoalPostLeftCalibRect);
  yellowGoalColorCalibratorTop.addCalibrationRect(yellowGoalPostRightCalibRectTop);
  yellowGoalColorCalibrator.addCalibrationRect(yellowGoalPostRightCalibRect);
  blueGoalColorCalibratorTop.addCalibrationRect(blueGoalLeftCalibRectTop);
  blueGoalColorCalibrator.addCalibrationRect(blueGoalLeftCalibRect);
  blueGoalColorCalibratorTop.addCalibrationRect(blueGoalRightCalibRectTop);
  blueGoalColorCalibrator.addCalibrationRect(blueGoalRightCalibRect);
  blueTeamColorCalibratorTop.addCalibrationRect(blueTeamCalibRectTop);
  blueTeamColorCalibrator.addCalibrationRect(blueTeamCalibRect);
  redTeamColorCalibratorTop.addCalibrationRect(redTeamCalibRectTop);
  redTeamColorCalibrator.addCalibrationRect(redTeamCalibRect);
  whiteLinesColorCalibratorTop.addCalibrationRect(whiteLinesCalibRectTop);
  whiteLinesColorCalibrator.addCalibrationRect(whiteLinesCalibRect);

  lastFrameInfo = getFrameInfo();
}

void BaseColorClassifier::initPercepts()
{
  PixelT<int> chDist;
  PixelT<int> chIdx;
  colorPixel diffDist;
  colorPixel diffIdx;

  regionParams.orangeBallParamsTop.get(              chIdx, chDist);    
  getBaseColorRegionPerceptTop().orangeBall.set(     chIdx, chDist);
  regionParams.orangeBallParams.get(              chIdx, chDist);    
  getBaseColorRegionPercept().orangeBall.set(     chIdx, chDist);
  regionParams.yellowGoalParamsTop.get(              chIdx, chDist);    
  getBaseColorRegionPerceptTop().yellowGoal.set(     chIdx, chDist);
  regionParams.yellowGoalParams.get(              chIdx, chDist);    
  getBaseColorRegionPercept().yellowGoal.set(     chIdx, chDist);
  regionParams.blueGoalParamsTop.get(                chIdx, chDist);    
  getBaseColorRegionPerceptTop().blueGoal.set(       chIdx, chDist);
  regionParams.blueGoalParams.get(                chIdx, chDist);    
  getBaseColorRegionPercept().blueGoal.set(       chIdx, chDist);
  regionParams.redTeamParamsTop.get(           chIdx, chDist);    
  getBaseColorRegionPerceptTop().redTeam.set(  chIdx, chDist);
  regionParams.redTeamParams.get(           chIdx, chDist);    
  getBaseColorRegionPercept().redTeam.set(  chIdx, chDist);
  regionParams.blueTeamParamsTop.get(           chIdx, chDist);    
  getBaseColorRegionPerceptTop().blueTeam.set(  chIdx, chDist);
  regionParams.blueTeamParams.get(           chIdx, chDist);    
  getBaseColorRegionPercept().blueTeam.set(  chIdx, chDist);
  regionParams.whiteLineParamsTop.get(               chIdx, chDist);    
  getBaseColorRegionPerceptTop().whiteLine.set(      chIdx, chDist);
  regionParams.whiteLineParams.get(               chIdx, chDist);    
  getBaseColorRegionPercept().whiteLine.set(      chIdx, chDist);

  regionParams.orangeBallParamsTop.get(              diffIdx, diffDist);    
  getBaseColorRegionPerceptTop().orangeBall.set(     diffIdx, diffDist);
  regionParams.orangeBallParams.get(              diffIdx, diffDist);    
  getBaseColorRegionPercept().orangeBall.set(     diffIdx, diffDist);
  regionParams.yellowGoalParamsTop.get(              diffIdx, diffDist);    
  getBaseColorRegionPerceptTop().yellowGoal.set(     diffIdx, diffDist);
  regionParams.yellowGoalParams.get(              diffIdx, diffDist);    
  getBaseColorRegionPercept().yellowGoal.set(     diffIdx, diffDist);
  regionParams.blueGoalParamsTop.get(                diffIdx, diffDist);    
  getBaseColorRegionPerceptTop().blueGoal.set(       diffIdx, diffDist);
  regionParams.blueGoalParams.get(                diffIdx, diffDist);    
  getBaseColorRegionPercept().blueGoal.set(       diffIdx, diffDist);
 }


void BaseColorClassifier::execute()
{

  DEBUG_REQUEST("Vision:ColorClassifiers:BaseColorClassifier:CamTop:calibrate_colors:reset_data",
    orangeBallColorCalibratorTop.reset();
    yellowGoalColorCalibratorTop.reset();
    blueGoalColorCalibratorTop.reset();
    blueTeamColorCalibratorTop.reset();
    redTeamColorCalibratorTop.reset();
    whiteLinesColorCalibratorTop.reset();
  );
  DEBUG_REQUEST("Vision:ColorClassifiers:BaseColorClassifier:CamBottom:calibrate_colors:reset_data",
    orangeBallColorCalibrator.reset();
    yellowGoalColorCalibrator.reset();
    blueGoalColorCalibrator.reset();
    blueTeamColorCalibrator.reset();
    redTeamColorCalibrator.reset();
    whiteLinesColorCalibrator.reset();
  );
  calibrateColorRegions();
  initPercepts();

  getBaseColorRegionPercept().lastUpdated = getFrameInfo();
  getBaseColorRegionPerceptTop().lastUpdated = getFrameInfo();
  runDebugRequests();
  lastFrameInfo = getFrameInfo();
}//end execute

void BaseColorClassifier::calibrateColorRegions()
{
  colorPixel diffDist;
  colorPixel diffIdx;
  diffIdx.VminusU = 0; 
  diffIdx.UminusY = 0; 
  diffIdx.VminusY = 0;
  diffDist.VminusU = 0; 
  diffDist.UminusY = 0; 
  diffDist.VminusY = 0;

  PixelT<int> chIdx;
  PixelT<int> chDist;
  chIdx.y = 0; 
  chIdx.u = 0; 
  chIdx.v = 0;
  chDist.y = 0; 
  chDist.u = 0; 
  chDist.v = 0;

  DEBUG_REQUEST("Vision:ColorClassifiers:BaseColorClassifier:CamTop:calibrate_colors:ball",
    orangeBallColorCalibratorTop.setStrength(params.calibrationStrength);
    orangeBallColorCalibratorTop.execute(getImageTop());
    orangeBallColorCalibratorTop.get(                chIdx, chDist);
    getBaseColorRegionPerceptTop().orangeBall.set(   chIdx, chDist);
    regionParams.orangeBallParamsTop.set(            chIdx, chDist);    

    orangeBallColorCalibratorTop.get(                diffIdx, diffDist);
    getBaseColorRegionPerceptTop().orangeBall.set(   diffIdx, diffDist);
    regionParams.orangeBallParamsTop.set(            diffIdx, diffDist);    

    regionParams.orangeBallParamsTop.saveToConfig();
    regionParams.orangeBallParamsTop.syncWithConfig();
  );
  DEBUG_REQUEST("Vision:ColorClassifiers:BaseColorClassifier:CamBottom:calibrate_colors:ball",
    orangeBallColorCalibrator.setStrength(params.calibrationStrength);
    orangeBallColorCalibrator.execute(getImage());
    orangeBallColorCalibrator.get(                chIdx, chDist);
    getBaseColorRegionPercept().orangeBall.set(   chIdx, chDist);
    regionParams.orangeBallParams.set(            chIdx, chDist);    

    orangeBallColorCalibrator.get(                diffIdx, diffDist);
    getBaseColorRegionPercept().orangeBall.set(   diffIdx, diffDist);
    regionParams.orangeBallParams.set(            diffIdx, diffDist);    

    regionParams.orangeBallParams.saveToConfig();
    regionParams.orangeBallParams.syncWithConfig();
  );

  DEBUG_REQUEST("Vision:ColorClassifiers:BaseColorClassifier:CamTop:calibrate_colors:yellow_goal",
    yellowGoalColorCalibratorTop.setStrength(params.calibrationStrength);
    yellowGoalColorCalibratorTop.execute(getImageTop());
    yellowGoalColorCalibratorTop.get(                chIdx, chDist);
    getBaseColorRegionPerceptTop().yellowGoal.set(   chIdx, chDist);
    regionParams.yellowGoalParamsTop.set(            chIdx, chDist); 

    yellowGoalColorCalibratorTop.get(                diffIdx, diffDist);
    getBaseColorRegionPerceptTop().yellowGoal.set(   diffIdx, diffDist);
    regionParams.yellowGoalParamsTop.set(            diffIdx, diffDist);    

    regionParams.yellowGoalParamsTop.saveToConfig();
    regionParams.yellowGoalParamsTop.syncWithConfig();
  );
  DEBUG_REQUEST("Vision:ColorClassifiers:BaseColorClassifier:CamBottom:calibrate_colors:yellow_goal",
    yellowGoalColorCalibrator.setStrength(params.calibrationStrength);
    yellowGoalColorCalibrator.execute(getImage());
    yellowGoalColorCalibrator.get(                chIdx, chDist);
    getBaseColorRegionPercept().yellowGoal.set(   chIdx, chDist);
    regionParams.yellowGoalParams.set(            chIdx, chDist); 

    yellowGoalColorCalibrator.get(                diffIdx, diffDist);
    getBaseColorRegionPercept().yellowGoal.set(   diffIdx, diffDist);
    regionParams.yellowGoalParams.set(            diffIdx, diffDist);    

    regionParams.yellowGoalParams.saveToConfig();
    regionParams.yellowGoalParams.syncWithConfig();
  );

  DEBUG_REQUEST("Vision:ColorClassifiers:BaseColorClassifier:CamTop:calibrate_colors:blue_goal", 
    blueGoalColorCalibratorTop.setStrength(params.calibrationStrength);
    blueGoalColorCalibratorTop.execute(getImageTop());
    blueGoalColorCalibratorTop.get(                  chIdx, chDist);
    getBaseColorRegionPerceptTop().blueGoal.set(     chIdx, chDist);
    regionParams.blueGoalParamsTop.set(              chIdx, chDist);    

    blueGoalColorCalibratorTop.get(                  diffIdx, diffDist);
    getBaseColorRegionPerceptTop().blueGoal.set(     diffIdx, diffDist);
    regionParams.blueGoalParamsTop.set(              diffIdx, diffDist);    

    regionParams.blueGoalParamsTop.saveToConfig();
    regionParams.blueGoalParamsTop.syncWithConfig();
  );
  DEBUG_REQUEST("Vision:ColorClassifiers:BaseColorClassifier:CamBottom:calibrate_colors:blue_goal", 
    blueGoalColorCalibrator.setStrength(params.calibrationStrength);
    blueGoalColorCalibrator.execute(getImage());
    blueGoalColorCalibrator.get(                  chIdx, chDist);
    getBaseColorRegionPercept().blueGoal.set(     chIdx, chDist);
    regionParams.blueGoalParams.set(              chIdx, chDist);    

    blueGoalColorCalibrator.get(                  diffIdx, diffDist);
    getBaseColorRegionPercept().blueGoal.set(     diffIdx, diffDist);
    regionParams.blueGoalParams.set(              diffIdx, diffDist);    

    regionParams.blueGoalParams.saveToConfig();
    regionParams.blueGoalParams.syncWithConfig();
  );

  DEBUG_REQUEST("Vision:ColorClassifiers:BaseColorClassifier:CamTop:calibrate_colors:blue_team", 
    blueTeamColorCalibratorTop.setStrength(params.calibrationStrength);
    blueTeamColorCalibratorTop.execute(getImageTop());
    blueTeamColorCalibratorTop.get(             chIdx, chDist);
    getBaseColorRegionPerceptTop().blueTeam.set(chIdx, chDist);
    regionParams.blueTeamParamsTop.set(         chIdx, chDist);    

    regionParams.blueTeamParamsTop.saveToConfig();
    regionParams.blueTeamParamsTop.syncWithConfig();
  );
  DEBUG_REQUEST("Vision:ColorClassifiers:BaseColorClassifier:CamBottom:calibrate_colors:blue_team", 
    blueTeamColorCalibrator.setStrength(params.calibrationStrength);
    blueTeamColorCalibrator.execute(getImage());
    blueTeamColorCalibrator.get(             chIdx, chDist);
    getBaseColorRegionPercept().blueTeam.set(chIdx, chDist);
    regionParams.blueTeamParams.set(         chIdx, chDist);    

    regionParams.blueTeamParams.saveToConfig();
    regionParams.blueTeamParams.syncWithConfig();
  );

  DEBUG_REQUEST("Vision:ColorClassifiers:BaseColorClassifier:CamTop:calibrate_colors:red_team",
    redTeamColorCalibratorTop.setStrength(params.calibrationStrength);
    redTeamColorCalibratorTop.execute(getImageTop());
    redTeamColorCalibratorTop.get(             chIdx, chDist);
    getBaseColorRegionPerceptTop().redTeam.set(chIdx, chDist);
    regionParams.redTeamParamsTop.set(         chIdx, chDist);    

    regionParams.redTeamParamsTop.saveToConfig();
    regionParams.redTeamParamsTop.syncWithConfig();
  );
  DEBUG_REQUEST("Vision:ColorClassifiers:BaseColorClassifier:CamBottom:calibrate_colors:red_team",
    redTeamColorCalibrator.setStrength(params.calibrationStrength);
    redTeamColorCalibrator.execute(getImage());
    redTeamColorCalibrator.get(             chIdx, chDist);
    getBaseColorRegionPercept().redTeam.set(chIdx, chDist);
    regionParams.redTeamParams.set(         chIdx, chDist);    

    regionParams.redTeamParams.saveToConfig();
    regionParams.redTeamParams.syncWithConfig();
  );

  DEBUG_REQUEST("Vision:ColorClassifiers:BaseColorClassifier:CamTop:calibrate_colors:line",
    whiteLinesColorCalibratorTop.setStrength(params.calibrationStrength);
    whiteLinesColorCalibratorTop.execute(getImageTop());
    whiteLinesColorCalibratorTop.get(                chIdx, chDist);
    getBaseColorRegionPerceptTop().whiteLine.set(    chIdx, chDist);
    regionParams.whiteLineParamsTop.set(             chIdx, chDist);    

    regionParams.whiteLineParamsTop.saveToConfig();
    regionParams.whiteLineParamsTop.syncWithConfig();
  );
  DEBUG_REQUEST("Vision:ColorClassifiers:BaseColorClassifier:CamBottom:calibrate_colors:line",
    whiteLinesColorCalibrator.setStrength(params.calibrationStrength);
    whiteLinesColorCalibrator.execute(getImage());
    whiteLinesColorCalibrator.get(                chIdx, chDist);
    getBaseColorRegionPercept().whiteLine.set(    chIdx, chDist);
    regionParams.whiteLineParams.set(             chIdx, chDist);    

    regionParams.whiteLineParams.saveToConfig();
    regionParams.whiteLineParams.syncWithConfig();
  );
}

void BaseColorClassifier::runDebugRequests()
{
  DEBUG_REQUEST("Vision:ColorClassifiers:BaseColorClassifier:CamTop:calibration_areas:show_line_area",
    whiteLinesColorCalibratorTop.drawCalibrationAreaRects(CameraInfo::Top);
  );
  DEBUG_REQUEST("Vision:ColorClassifiers:BaseColorClassifier:CamBottom:calibration_areas:show_line_area",
    whiteLinesColorCalibrator.drawCalibrationAreaRects(CameraInfo::Bottom);
  );

  DEBUG_REQUEST("Vision:ColorClassifiers:BaseColorClassifier:CamTop:calibration_areas:show_ball_area",
    orangeBallColorCalibratorTop.drawCalibrationAreaRects(CameraInfo::Top);  
  );
  DEBUG_REQUEST("Vision:ColorClassifiers:BaseColorClassifier:CamBottom:calibration_areas:show_ball_area",
    orangeBallColorCalibrator.drawCalibrationAreaRects(CameraInfo::Bottom);  
  );

  DEBUG_REQUEST("Vision:ColorClassifiers:BaseColorClassifier:CamTop:calibration_areas:show_blue_goal_area",
    blueGoalColorCalibratorTop.drawCalibrationAreaRects(CameraInfo::Top);
  );
  DEBUG_REQUEST("Vision:ColorClassifiers:BaseColorClassifier:CamBottom:calibration_areas:show_blue_goal_area",
    blueGoalColorCalibrator.drawCalibrationAreaRects(CameraInfo::Bottom);
  );

  DEBUG_REQUEST("Vision:ColorClassifiers:BaseColorClassifier:CamTop:calibration_areas:show_yellow_goal_area",
    yellowGoalColorCalibratorTop.drawCalibrationAreaRects(CameraInfo::Top);
  );
  DEBUG_REQUEST("Vision:ColorClassifiers:BaseColorClassifier:CamBottom:calibration_areas:show_yellow_goal_area",
    yellowGoalColorCalibrator.drawCalibrationAreaRects(CameraInfo::Bottom);
  );

  DEBUG_REQUEST("Vision:ColorClassifiers:BaseColorClassifier:CamTop:calibration_areas:show_red_team_area",
    redTeamColorCalibratorTop.drawCalibrationAreaRects(CameraInfo::Top);
  );
  DEBUG_REQUEST("Vision:ColorClassifiers:BaseColorClassifier:CamBottom:calibration_areas:show_red_team_area",
    redTeamColorCalibrator.drawCalibrationAreaRects(CameraInfo::Bottom);
  );

  DEBUG_REQUEST("Vision:ColorClassifiers:BaseColorClassifier:CamTop:calibration_areas:show_blue_team_area",
    blueTeamColorCalibratorTop.drawCalibrationAreaRects(CameraInfo::Top);
  );
  DEBUG_REQUEST("Vision:ColorClassifiers:BaseColorClassifier:CamBottom:calibration_areas:show_blue_team_area",
    blueTeamColorCalibrator.drawCalibrationAreaRects(CameraInfo::Bottom);
  );

  int imageWidth = getImageTop().width();
  int imageHeight = getImageTop().height();

  DEBUG_REQUEST("Vision:ColorClassifiers:BaseColorClassifier:CamTop:set_ball_in_image",
    CANVAS_PX(CameraInfo::Top);
    for(int x = 0; x < imageWidth; x++)
    {
      for(int y = 0; y < imageHeight; y++)
      {
        const Pixel& pixel = getImageTop().get(x, y);

        if(getBaseColorRegionPerceptTop().orangeBall.inside(pixel))
        {
          POINT_PX(ColorClasses::red, x, y);
        }

      }
    }
  );

  DEBUG_REQUEST("Vision:ColorClassifiers:BaseColorClassifier:CamTop:set_goal_in_image",
    CANVAS_PX(CameraInfo::Top);
    for(int x = 0; x < imageWidth; x++)
    {
      for(int y = 0; y < imageHeight; y++)
      {
        const Pixel& pixel = getImageTop().get(x, y);

        if(getBaseColorRegionPerceptTop().yellowGoal.inside(pixel))
        {
          POINT_PX(ColorClasses::yellow, x, y);
        }

        if(getBaseColorRegionPerceptTop().blueGoal.inside(pixel))
        {
          POINT_PX(ColorClasses::skyblue, x, y);
        }

      }
    }
  );

  DEBUG_REQUEST("Vision:ColorClassifiers:BaseColorClassifier:CamTop:set_red_team_in_image",
    CANVAS_PX(CameraInfo::Top);
    for(int x = 0; x < imageWidth; x++)
    {
      for(int y = 0; y < imageHeight; y++)
      {
        const Pixel& pixel = getImageTop().get(x, y);

        if(getBaseColorRegionPerceptTop().redTeam.inside(pixel))
        {
          POINT_PX(ColorClasses::pink, x, y);
        }
      }
    }
  );

  DEBUG_REQUEST("Vision:ColorClassifiers:BaseColorClassifier:CamTop:set_blue_team_in_image",
    CANVAS_PX(CameraInfo::Top);
    for(int x = 0; x < imageWidth; x++)
    {
      for(int y = 0; y < imageHeight; y++)
      {
        const Pixel& pixel = getImageTop().get(x, y);

        if(getBaseColorRegionPerceptTop().blueTeam.inside(pixel))
        {
          POINT_PX(ColorClasses::blue, x, y);
        }
      }
    }
  );

  imageWidth = getImage().width();
  imageHeight = getImage().height();

  DEBUG_REQUEST("Vision:ColorClassifiers:BaseColorClassifier:CamBottom:set_ball_in_image",
    CANVAS_PX(CameraInfo::Bottom);
    for(int x = 0; x < imageWidth; x++)
    {
      for(int y = 0; y < imageHeight; y++)
      {
        const Pixel& pixel = getImage().get(x, y);

        if(getBaseColorRegionPercept().orangeBall.inside(pixel))
        {
          POINT_PX(ColorClasses::red, x, y);
        }

      }
    }
  );

  DEBUG_REQUEST("Vision:ColorClassifiers:BaseColorClassifier:CamBottom:set_goal_in_image",
    CANVAS_PX(CameraInfo::Bottom);
    for(int x = 0; x < imageWidth; x++)
    {
      for(int y = 0; y < imageHeight; y++)
      {
        const Pixel& pixel = getImage().get(x, y);

        if(getBaseColorRegionPercept().yellowGoal.inside(pixel))
        {
          POINT_PX(ColorClasses::yellow, x, y);
        }

        if(getBaseColorRegionPercept().blueGoal.inside(pixel))
        {
          POINT_PX(ColorClasses::skyblue, x, y);
        }

      }
    }
  );

  DEBUG_REQUEST("Vision:ColorClassifiers:BaseColorClassifier:CamBottom:set_red_team_in_image",
    CANVAS_PX(CameraInfo::Bottom);
    for(int x = 0; x < imageWidth; x++)
    {
      for(int y = 0; y < imageHeight; y++)
      {
        const Pixel& pixel = getImage().get(x, y);

        if(getBaseColorRegionPercept().redTeam.inside(pixel))
        {
          POINT_PX(ColorClasses::pink, x, y);
        }
      }
    }
  );

  DEBUG_REQUEST("Vision:ColorClassifiers:BaseColorClassifier:CamBottom:set_blue_team_in_image",
    CANVAS_PX(CameraInfo::Bottom);
    for(int x = 0; x < imageWidth; x++)
    {
      for(int y = 0; y < imageHeight; y++)
      {
        const Pixel& pixel = getImage().get(x, y);

        if(getBaseColorRegionPercept().blueTeam.inside(pixel))
        {
          POINT_PX(ColorClasses::blue, x, y);
        }
      }
    }
  );

  double factorBright = 0.35;
  MODIFY("Vision:ColorClassifiers:BaseColorClassifier:factorBright", factorBright);
  double factorDark = 0.015;
  MODIFY("Vision:ColorClassifiers:BaseColorClassifier:factorDark", factorDark);

  DEBUG_REQUEST("Vision:ColorClassifiers:BaseColorClassifier:CamTop:set_lines_in_image",
    CANVAS_PX(CameraInfo::Top);
    int imageWidth = getImageTop().width();
    int imageHeight = getImageTop().height();
    double diffBright = getOverTimeHistogramTop().spanWidthEnv.y * factorBright + 0.5;
    double diffDark = getOverTimeHistogramTop().spanWidthEnv.y * factorDark + 0.5;
    for(int x = 0; x < imageWidth; x++)
    {
      for(int y = 0; y < imageHeight; y++)
      {
        const Pixel& pixel = getImageTop().get(x, y);

        //if(getBaseColorRegionPercept().whiteLine.inside(pixel))
        if(pixel.y > getOverTimeHistogramTop().maxEnv.y - diffBright)
        {
          POINT_PX(ColorClasses::black, x, y);
        }
        if(pixel.y < getOverTimeHistogramTop().minEnv.y + diffDark)
        {
          POINT_PX(ColorClasses::white, x, y);   
        }

      }
    }
  );
  DEBUG_REQUEST("Vision:ColorClassifiers:BaseColorClassifier:CamBottom:set_lines_in_image",
    CANVAS_PX(CameraInfo::Bottom);
    int imageWidth = getImage().width();
    int imageHeight = getImage().height();
    double diffBright = getOverTimeHistogram().spanWidthEnv.y * factorBright + 0.5;
    double diffDark = getOverTimeHistogram().spanWidthEnv.y * factorDark + 0.5;
    for(int x = 0; x < imageWidth; x++)
    {
      for(int y = 0; y < imageHeight; y++)
      {
        const Pixel& pixel = getImage().get(x, y);

        //if(getBaseColorRegionPercept().whiteLine.inside(pixel))
        //{
        //  POINT_PX(ColorClasses::white, x, y);
        //}
        if(pixel.y > getOverTimeHistogram().maxEnv.y - diffBright)
        {
          POINT_PX(ColorClasses::black, x, y);
        }
        if(pixel.y < getOverTimeHistogram().minEnv.y + diffDark)
        {
          POINT_PX(ColorClasses::white, x, y);   
        }

      }
    }
  );
}