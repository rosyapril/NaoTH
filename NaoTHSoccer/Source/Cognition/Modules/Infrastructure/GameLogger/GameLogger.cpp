#include "GameLogger.h"

GameLogger::GameLogger()
  : 
  logfileManager(true),
  lastCompleteFrameNumber(0),
  oldState(PlayerInfo::initial),
  firstRecording(true)
{
  logfileManager.openFile("/tmp/game.log");
  
  imageOutFile.open("/tmp/images.log", ios::out | ios::binary);
  lastTimeImageRecorded = getFrameInfo();

  getDebugParameterList().add(&params);
}

GameLogger::~GameLogger()
{
  logfileManager.closeFile();
  imageOutFile.close();
  getDebugParameterList().remove(&params);
}

#define LOGSTUFF(name) \
  { std::stringstream& dataStream = logfileManager.log(getFrameInfo().getFrameNumber(), #name); \
  Serializer<name>::serialize(get##name(), dataStream); } ((void)0)

void GameLogger::execute()
{
  // HACK: wait a bit before starting recording
  if(!logfileManager.is_ready()) {
    return;
  }

  if( getBehaviorStateComplete().state.IsInitialized() &&
      getBehaviorStateSparse().state.IsInitialized())
  {
    bool something_recorded = false;

    // write out the complete behavior state when it was just created in this frame
    if(getBehaviorStateComplete().state.framenumber() >= lastCompleteFrameNumber)
    {
      LOGSTUFF(BehaviorStateComplete);
      LOGSTUFF(RobotInfo);

      lastCompleteFrameNumber = getFrameInfo().getFrameNumber();
      something_recorded = true;
    }

    // condition wheather the current frame should be logged:
    bool log_this_frame = getBehaviorStateSparse().state.framenumber() == getFrameInfo().getFrameNumber();

    // NOTE: record only the first frame if the state changed to initial or finished
    if(!firstRecording && oldState == getPlayerInfo().robotState) {
      log_this_frame = log_this_frame && getPlayerInfo().robotState != PlayerInfo::initial;
      log_this_frame = log_this_frame && getPlayerInfo().robotState != PlayerInfo::finished;
    }

    if(log_this_frame)
    {
      LOGSTUFF(BehaviorStateSparse);

      // proprioception
      LOGSTUFF(OdometryData);
      LOGSTUFF(CameraMatrix);
      LOGSTUFF(CameraMatrixTop);

      if(params.logBodyStatus) {
        LOGSTUFF(BodyStatus);
      }

      // perception
      LOGSTUFF(GoalPercept);
      LOGSTUFF(GoalPerceptTop);

      LOGSTUFF(MultiBallPercept);
      
      //LOGSTUFF(BallPercept);
      //LOGSTUFF(BallPerceptTop);
      
      LOGSTUFF(ScanLineEdgelPercept);
      LOGSTUFF(ScanLineEdgelPerceptTop);
      
      if(params.logBallCandidates) {
        LOGSTUFF(BallCandidates);
        LOGSTUFF(BallCandidatesTop);
      }

      LOGSTUFF(TeamMessage);

      // record images every 1s
      if(params.logPlainImages && getFrameInfo().getTimeSince(lastTimeImageRecorded) > 2000 && imageOutFile.is_open() && !imageOutFile.fail()) {
        unsigned int frameNumber = getFrameInfo().getFrameNumber();
        imageOutFile.write((const char*)(&frameNumber), sizeof(unsigned int));

        imageOutFile.write((const char*)getImageTop().data(), getImageTop().data_size());
        lastTimeImageRecorded = getFrameInfo();
      }

      something_recorded = true;
    }

    if(something_recorded) {
      LOGSTUFF(FrameInfo);
      firstRecording = false;
    }

    // remember the old state
    oldState = getPlayerInfo().robotState;
  }
}

