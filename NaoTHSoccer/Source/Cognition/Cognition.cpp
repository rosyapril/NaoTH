/**
 * @file Cognition.cpp
 *
 * @author <a href="mailto:mellmann@informatik.hu-berlin.de">Heinrich Mellmann</a>
 * Implementation of the class Cognition
 */

#include "Cognition.h"

#include <PlatformInterface/Platform.h>

// tools
#include "Tools/Debug/Trace.h"

/////////////////////////////////////
// Modules
/////////////////////////////////////

// infrastructure
#include "Modules/Infrastructure/IO/Sensor.h"
#include "Modules/Infrastructure/IO/Actuator.h"
#include "Modules/Infrastructure/ButtonEventMonitor/ButtonEventMonitor.h"
#include "Modules/Infrastructure/BatteryAlert/BatteryAlert.h"
#include "Modules/Infrastructure/GameController/GameController.h"
#include "Modules/Infrastructure/Debug/FrameRateCheck.h"
#include "Modules/Infrastructure/Debug/DebugExecutor.h"
#include "Modules/Infrastructure/Debug/Debug.h"
#include "Modules/Infrastructure/LEDSetter/LEDSetter.h"
#include "Modules/Infrastructure/UltraSoundControl/UltraSoundControl.h"

#include "Modules/Infrastructure/TeamCommunicator/TeamCommReceiver.h"
#include "Modules/Infrastructure/TeamCommunicator/TeamCommSender.h"

#include "Modules/Infrastructure/Debug/CameraDebug.h"
#include "Modules/Infrastructure/Camera/CameraInfoSetter.h"
#include "Modules/Infrastructure/GameLogger/GameLogger.h"

// perception
#include "Modules/SelfAwareness/CameraMatrixFinder/CameraMatrixFinder.h"
#include "Modules/SelfAwareness/KinematicChainProvider/KinematicChainProvider.h"
#include "Modules/SelfAwareness/ArtificialHorizonCalculator/ArtificialHorizonCalculator.h"
#include "Modules/SelfAwareness/BodyContourProvider/BodyContourProvider.h"
#include "Modules/SelfAwareness/CameraMatrixCorrector/CameraMatrixCorrector.h"
#include "Modules/SelfAwareness/CameraMatrixCorrectorV2/CameraMatrixCorrectorV2.h"

#include "Modules/VisualCortex/HistogramProvider.h"
#include "Modules/VisualCortex/SimpleFieldColorClassifier/SimpleFieldColorClassifier.h"
#include "Modules/VisualCortex/FieldColorClassifier.h"
#include "Modules/VisualCortex/ScanLineEdgelDetector/ScanLineEdgelDetector.h"
#include "Modules/VisualCortex/FieldDetector/FieldDetector.h"
#include "Modules/VisualCortex/LineDetector/LineGraphProvider.h"
#include "Modules/VisualCortex/GoalDetector/GoalFeatureDetector.h"
#include "Modules/VisualCortex/GoalDetector/GoalFeatureDetectorV2.h"
#include "Modules/VisualCortex/GoalDetector/GoalDetector.h"
#include "Modules/VisualCortex/GoalDetector/GoalDetectorV2.h"
#include "Modules/VisualCortex/GoalDetector/GoalCrossBarDetector.h"
#include "Modules/VisualCortex/BallDetector/RedBallDetector.h"
#include "Modules/VisualCortex/BallDetector/BallCandidateDetector.h"
#include "Modules/VisualCortex/BallDetector/BallDetector.h"
#include "Modules/VisualCortex/IntegralImageProvider.h"

#include "Modules/SelfAwareness/FakeCameraMatrixFinder/FakeCameraMatrixFinder.h"
#include "Modules/VisualCortex/FakeBallDetector/FakeBallDetector.h"

#include "Modules/Perception/VirtualVisionProcessor/VirtualVisionProcessor.h"
#include "Modules/Perception/PerceptionsVisualizer/PerceptionsVisualizer.h"

#include "Modules/VisualCortex/LineDetector/RansacLineDetector.h"
#include "Modules/VisualCortex/LineDetector/RansacLineDetectorOnGraphs.h"

#include "Modules/Modeling/CompassProvider/CompassProvider.h"

// modeling
#include "Modules/Modeling/BodyStateProvider/BodyStateProvider.h"
#include "Modules/Modeling/FieldCompass/FieldCompass.h"
#include "Modules/Modeling/ObstacleLocator/UltraSoundObstacleLocator.h"
#include "Modules/Modeling/TeamMessageStatistics/TeamCommReceiveEmulator.h"
#include "Modules/Modeling/TeamMessageStatistics/TeamMessageStatistics.h"
#include "Modules/Modeling/RoleDecision/SimpleRoleDecision/SimpleRoleDecision.h"
#include "Modules/Modeling/RoleDecision/StableRoleDecision/StableRoleDecision.h"
#include "Modules/Modeling/RoleDecision/CleanRoleDecision/CleanRoleDecision.h"
#include "Modules/Modeling/SoccerStrategyProvider/SoccerStrategyProvider.h"
#include "Modules/Modeling/PotentialFieldProvider/PotentialFieldProvider.h"
#include "Modules/Modeling/SelfLocator/GPS_SelfLocator/GPS_SelfLocator.h"
#include "Modules/Modeling/SelfLocator/MonteCarloSelfLocator/MonteCarloSelfLocator.h"
#include "Modules/Modeling/SelfLocator/OdometrySelfLocator/OdometrySelfLocator.h"
#include "Modules/Modeling/GoalModel/DummyActiveGoalLocator/DummyActiveGoalLocator.h"
#include "Modules/Modeling/GoalModel/WholeGoalLocator/WholeGoalLocator.h"
#include "Modules/Modeling/BallLocator/KalmanFilterBallLocator/KalmanFilterBallLocator.h"

#include "Modules/Modeling/BallLocator/TeamBallLocator/TeamBallLocator.h"
#include "Modules/Modeling/BallLocator/MultiKalmanBallLocator/MultiKalmanBallLocator.h"
#include "Modules/Modeling/StaticDebugModelProvider/StaticDebugModelProvider.h"

#include "Modules/Modeling/Simulation/SimulationTest.h"
#include "Modules/Modeling/Simulation/Simulation.h"
#include "Modules/Modeling/Simulation/SimulationOLD.h"
#include "Modules/Modeling/Simulation/KickDirectionSimulator.h"
#include "Modules/Modeling/SelfLocator/SituationPriorProvider/SituationPriorProvider.h"


// behavior
#include "Modules/Behavior/BasicTestBehavior/BasicTestBehavior.h"
#include "Modules/Behavior/XABSLBehaviorControl/XABSLBehaviorControl.h"
#include "Modules/Behavior/PathPlanner/PathPlanner.h"

using namespace std;

Cognition::Cognition()
  : ModuleManagerWithDebug("")
{
}

Cognition::~Cognition()
{
}


#define REGISTER_MODULE(module) \
  std::cout << "[Cognition] Register " << #module << std::endl;\
  registerModule<module>(std::string(#module))


void Cognition::init(naoth::ProcessInterface& platformInterface, const naoth::PlatformBase& platform)
{
  std::cout << "[Cognition] Cognition register start" << std::endl;

  // register input module
  ModuleCreator<Sensor>* sensor = registerModule<Sensor>(std::string("Sensor"), true);
  sensor->getModuleT()->init(platformInterface, platform);

  /* 
  * to register a module use
  *   REGISTER_MODULE(ModuleClassName);
  *
  * Remark: to enable the module don't forget 
  *         to set the value in modules.cfg
  */

  // -- BEGIN REGISTER MODULES --

  // infrastructure
  REGISTER_MODULE(TeamCommReceiver);
  REGISTER_MODULE(GameController);
  REGISTER_MODULE(BatteryAlert);
  REGISTER_MODULE(ButtonEventMonitor);
  REGISTER_MODULE(LEDSetter);
  REGISTER_MODULE(UltraSoundControl);

  REGISTER_MODULE(CameraDebug);
  REGISTER_MODULE(CameraInfoSetter);

  // perception
  REGISTER_MODULE(CameraMatrixFinder);
  REGISTER_MODULE(KinematicChainProvider);
  REGISTER_MODULE(ArtificialHorizonCalculator);
  REGISTER_MODULE(BodyContourProvider);
  REGISTER_MODULE(CameraMatrixCorrector);
  REGISTER_MODULE(CameraMatrixCorrectorV2);

  REGISTER_MODULE(HistogramProvider);
  REGISTER_MODULE(IntegralImageProvider);
  REGISTER_MODULE(SimpleFieldColorClassifier);
  REGISTER_MODULE(FieldColorClassifier);
  REGISTER_MODULE(ScanLineEdgelDetector);
  REGISTER_MODULE(FieldDetector);
  REGISTER_MODULE(LineGraphProvider);
  REGISTER_MODULE(GoalFeatureDetector);
  REGISTER_MODULE(GoalFeatureDetectorV2);
  REGISTER_MODULE(GoalDetector);
  REGISTER_MODULE(GoalDetectorV2);
  REGISTER_MODULE(GoalCrossBarDetector);

  REGISTER_MODULE(RedBallDetector);
  REGISTER_MODULE(BallCandidateDetector);
  REGISTER_MODULE(BallDetector);

  REGISTER_MODULE(FakeCameraMatrixFinder);
  REGISTER_MODULE(FakeBallDetector);

  REGISTER_MODULE(VirtualVisionProcessor);
  REGISTER_MODULE(PerceptionsVisualizer);

  REGISTER_MODULE(RansacLineDetector);
  REGISTER_MODULE(RansacLineDetectorOnGraphs);

  REGISTER_MODULE(CompassProvider);

  // modeling
  REGISTER_MODULE(SituationPriorProvider);
  REGISTER_MODULE(BodyStateProvider);
  REGISTER_MODULE(FieldCompass);
  REGISTER_MODULE(UltraSoundObstacleLocator);
  REGISTER_MODULE(TeamCommReceiveEmulator);
  REGISTER_MODULE(TeamMessageStatistics);
  REGISTER_MODULE(SimpleRoleDecision);
  REGISTER_MODULE(StableRoleDecision);
  REGISTER_MODULE(CleanRoleDecision);
  REGISTER_MODULE(SoccerStrategyProvider);
  REGISTER_MODULE(PotentialFieldProvider);
  REGISTER_MODULE(GPS_SelfLocator);
  REGISTER_MODULE(MonteCarloSelfLocator);
  REGISTER_MODULE(OdometrySelfLocator);
  REGISTER_MODULE(WholeGoalLocator);
  REGISTER_MODULE(DummyActiveGoalLocator);
  REGISTER_MODULE(KalmanFilterBallLocator);
  REGISTER_MODULE(TeamBallLocator);
  REGISTER_MODULE(MultiKalmanBallLocator);

  REGISTER_MODULE(KickDirectionSimulator);
  REGISTER_MODULE(Simulation);
  REGISTER_MODULE(SimulationOLD);
  REGISTER_MODULE(SimulationTest);
  REGISTER_MODULE(StaticDebugModelProvider);

  // behavior
  REGISTER_MODULE(BasicTestBehavior);
  REGISTER_MODULE(XABSLBehaviorControl);
  REGISTER_MODULE(PathPlanner);

  REGISTER_MODULE(TeamCommSender);
  
  // debug
  REGISTER_MODULE(GameLogger);
  REGISTER_MODULE(Debug);
  REGISTER_MODULE(FrameRateCheck);
  REGISTER_MODULE(DebugExecutor);

  // -- END REGISTER MODULES --

  // register output module
  ModuleCreator<Actuator>* actuator = registerModule<Actuator>(std::string("Actuator"), true);
  actuator->getModuleT()->init(platformInterface, platform);

  // use the configuration in order to set whether a module is activated or not
  const naoth::Configuration& config = Platform::getInstance().theConfiguration;
  
  list<string>::const_iterator name = getExecutionList().begin();
  for(;name != getExecutionList().end(); ++name)
  {
    bool active = false;
    if(config.hasKey("modules", *name)) {    
      active = config.getBool("modules", *name);      
    }
    if(active) {
      std::cout << "[Cognition] activating module " << *name << std::endl;
    }
    setModuleEnabled(*name, active);
  }//end for

  // auto-generate the execution list
  //calculateExecutionList();

  std::cout << "[Cognition] register end" << std::endl;

  stopwatch.start();
}//end init


void Cognition::call()
{
  // BEGIN cognition frame rate measuring
  stopwatch.stop();
  stopwatch.start();
  PLOT("Cognition.Cycle", stopwatch.lastValue);
  // END cognition frame rate measuring


  STOPWATCH_START("Cognition.Execute");

  // execute all modules
  list<AbstractModuleCreator*>::const_iterator iter;
  for (iter = getModuleExecutionList().begin(); iter != getModuleExecutionList().end(); ++iter)
  {
    AbstractModuleCreator* module = *iter;
    if (module != NULL && module->isEnabled())
    {
      std::string name(module->getModule()->getName());
      GT_TRACE("executing " << name);
      module->execute();
    }
  }
  
  STOPWATCH_STOP("Cognition.Execute");


  // HACK: reset all the debug stuff before executing the modules
  STOPWATCH_START("Cognition.Debug.Init");
  getDebugDrawings().reset();
  getDebugImageDrawings().reset();
  getDebugImageDrawingsTop().reset();
  getDebugDrawings3D().reset();
  STOPWATCH_STOP("Cognition.Debug.Init");
}//end call
