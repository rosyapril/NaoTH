/* 
 * File:   ParameterListDebugLoader.h
 * Author: thomas
 *
 */

#ifndef PARAMETERLISTDEBUGLOADER_H
#define	PARAMETERLISTDEBUGLOADER_H

#include <map>

#include <ModuleFramework/Module.h>
#include <DebugCommunication/DebugCommandExecutor.h>

#include <DebugCommunication/DebugServer.h>

#include <Representations/Infrastructure/CameraSettings.h>
#include "Representations/Infrastructure/FieldInfo.h"

using namespace naoth;

BEGIN_DECLARE_MODULE(ParameterListDebugLoader)
  PROVIDE(CameraSettingsRequest)  
  PROVIDE(FieldInfo)
END_DECLARE_MODULE(ParameterListDebugLoader)

class ParameterListDebugLoader : public ParameterListDebugLoaderBase, public DebugCommandExecutor
{
public:
  ParameterListDebugLoader();
  virtual ~ParameterListDebugLoader();

  virtual void execute();

  virtual void executeDebugCommand(
    const std::string& command, const std::map<std::string,std::string>& arguments,
    std::ostream &outstream);

private:
  std::map<std::string, ParameterList*> paramlists;
};

#endif	/* PARAMETERLISTDEBUGLOADER_H */

