/**
 * @file Cognition.cpp
 *
 * @author <a href="mailto:mellmann@informatik.hu-berlin.de">Heinrich Mellmann</a>
 * Implementation of the class Cognition
 */

#include "Cognition.h"

#include <PlatformInterface/Platform.h>

/////////////////////////////////////
// Modules
/////////////////////////////////////

// infrastructure
#include "Modules/Infrastructure/IO/Sensor.h"
#include "Modules/Infrastructure/IO/Actuator.h"
#include "Modules/Infrastructure/ButtonEventMonitor/ButtonEventMonitor.h"
#include "Modules/Infrastructure/BatteryAlert/BatteryAlert.h"
#include "Modules/Infrastructure/Debug/FrameRateCheck.h"
#include "Modules/Infrastructure/Debug/DebugExecutor.h"
#include "Modules/Infrastructure/Debug/Debug.h"

// behavior
#include "Modules/Behavior/BasicTestBehavior/BasicTestBehavior.h"

#include "Modules/Infrastructure/Debug/CameraDebug.h"
#include "Modules/SelfAwareness/CameraMatrixFinder/CameraMatrixFinder.h"
#include "Modules/SelfAwareness/KinematicChainProvider/KinematicChainProvider.h"

// tools
#include "Tools/Debug/Trace.h"

using namespace std;

Cognition::Cognition()
  : ModuleManagerWithDebug("Cognition")
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
//  REGISTER_MODULE(TeamCommReceiver);
//  REGISTER_MODULE(GameController);
//  REGISTER_MODULE(OpenCVGrayScaleImageProvider);
//  REGISTER_MODULE(OpenCVImageProvider);
  REGISTER_MODULE(BatteryAlert);
  REGISTER_MODULE(ButtonEventMonitor);
 
  REGISTER_MODULE(CameraDebug);
  REGISTER_MODULE(CameraMatrixFinder);
  REGISTER_MODULE(KinematicChainProvider);

  // debug
  REGISTER_MODULE(Debug);
  REGISTER_MODULE(FrameRateCheck);
  REGISTER_MODULE(DebugExecutor);

  // behavior
  REGISTER_MODULE(BasicTestBehavior);

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
