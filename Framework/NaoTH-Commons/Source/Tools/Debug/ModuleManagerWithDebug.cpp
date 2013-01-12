/* 
 * File:   ModuleManagerWithDebug.cpp
 * Author: Heinrich Mellmann
 *
 */

#include "ModuleManagerWithDebug.h"
#include <DebugCommunication/DebugCommandManager.h>
#include <PlatformInterface/Platform.h>

// list the modules and representations on the blackboard
#include "Messages/Messages.pb.h"
#include <google/protobuf/io/zero_copy_stream_impl.h>


ModuleManagerWithDebug::ModuleManagerWithDebug()
{
  REGISTER_DEBUG_COMMAND("modules:list",
    "return the list of registered modules with provided and required representations", this);
  REGISTER_DEBUG_COMMAND("modules:set",
    "enables or diables the execution of a module. usage: modules:set [moduleName=on|off]+", this);
  REGISTER_DEBUG_COMMAND("modules:store",
    "store te corrent module configuration to the config file listed in the scheme", this);


  REGISTER_DEBUG_COMMAND("representation:list", "Stream out the list of all registered representations", this);
  REGISTER_DEBUG_COMMAND("representation:get", "Stream out all the representations listet", this);
  REGISTER_DEBUG_COMMAND("representation:getbinary", "Stream out serialized represenation", this);
}

ModuleManagerWithDebug::~ModuleManagerWithDebug()
{
}

void ModuleManagerWithDebug::printRepresentation(std::ostream &outstream, const std::string& name, bool binary)
{
  const BlackBoard& blackBoard = getBlackBoard();
  BlackBoard::Registry::const_iterator iter = blackBoard.getRegistry().find(name);

  if(iter != blackBoard.getRegistry().end())
  {
    const Representation& theRepresentation = iter->second->getRepresentation();
      
    if(binary)
    {
      theRepresentation.serialize(outstream);
    }
    else
    {
      theRepresentation.print(outstream);
    }
  }else
  {
    outstream << "unknown representation" << std::endl;
  }
}//end printRepresentation

void ModuleManagerWithDebug::printRepresentationList(std::ostream &outstream)
{
  const BlackBoard& blackBoard = getBlackBoard();
  BlackBoard::Registry::const_iterator iter;

  for(iter = blackBoard.getRegistry().begin(); iter != blackBoard.getRegistry().end(); ++iter)
  {
    outstream << iter->first << std::endl;
  }
}//end printRepresentationList


void ModuleManagerWithDebug::executeDebugCommand(const std::string& command, 
                                    const std::map<std::string,std::string>& arguments, 
                                    std::ostream& outstream)
{
  if (command == "modules:list")
  {
    naothmessages::ModuleList msg;
    
    std::list<std::string>::const_iterator iterModule;
    for (iterModule = getExecutionList().begin(); iterModule != getExecutionList().end(); ++iterModule)
    {
      naothmessages::Module* m = msg.add_modules();

      // get the module holder
      AbstractModuleCreator* moduleCreator = getModule(*iterModule);
      ASSERT(moduleCreator != NULL); // shold never happen

      // module name
      m->set_name(*iterModule);
      m->set_active(moduleCreator->isEnabled());
      
      if(moduleCreator->isEnabled())
      {
        Module* module = moduleCreator->getModule();
        ASSERT(module != NULL); // shold never happen


        RepresentationMap::const_iterator iterRep;
        for (iterRep = module->getRequire().begin();
          iterRep != module->getRequire().end(); iterRep++)
        {
          m->add_usedrepresentations(iterRep->second->getName());
        }

        for (iterRep = module->getProvide().begin();
          iterRep != module->getProvide().end(); iterRep++)
        {
          m->add_providedrepresentations(iterRep->second->getName());
        }
      }//end if
    }//end for iterModule

    google::protobuf::io::OstreamOutputStream buf(&outstream);
    msg.SerializeToZeroCopyStream(&buf);
  }
  else if( command == "modules:store" )
  {
    naoth::Configuration& config = naoth::Platform::getInstance().theConfiguration;
    for(std::list<std::string>::const_iterator name=getExecutionList().begin();
      name != getExecutionList().end(); name++)
    {
      config.setBool("modules", *name, getModule(*name)->isEnabled());
    }//end for

    // write the config to file
    config.save();
    outstream << "modules saved to private/modules.cfg" << std::endl;
  }
  else if (command == "modules:set")
  {
    std::map<std::string, std::string>::const_iterator iter;
    for (iter = arguments.begin(); iter != arguments.end(); ++iter)
    {
      AbstractModuleCreator* moduleCreator = getModule(iter->first);
      if (moduleCreator != NULL)
      {
        if ((iter->second).compare("on") == 0)
        {
          moduleCreator->setEnabled(true);
          outstream << "set " << (iter->first) << " on" << std::endl;
        }
        else if ((iter->second).compare("off") == 0)
        {
          moduleCreator->setEnabled(false);
          outstream << "set " << (iter->first) << " off" << std::endl;
        }
        else
        {
          outstream << "unknown value " << (iter->second) << std::endl;
        }
      }
      else
      {
        outstream << "unknown module " << (iter->first) << std::endl;
      }
    }//end for
  }
  else if (command == "representation:list")
  {
    printRepresentationList(outstream);
  }
  else if (command == "representation:get")
  {
    std::map<std::string, std::string>::const_iterator iter;
    for (iter = arguments.begin(); iter != arguments.end(); ++iter)
    {
      printRepresentation(outstream, iter->first, false);
    }//end for
  }
  else if (command == "representation:getbinary")
  {
    std::map<std::string, std::string>::const_iterator iter;
    for (iter = arguments.begin(); iter != arguments.end(); ++iter)
    {
      printRepresentation(outstream, iter->first, true);
    }
  }
}//end executeDebugCommand
