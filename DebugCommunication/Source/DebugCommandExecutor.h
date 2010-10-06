// 
// File:   DebugCommandExecutor.h
// Author: thomas
//
// Created on 8. März 2008, 11:52
//

#ifndef _DEBUGCOMMANDEXECUTOR_H
#define	_DEBUGCOMMANDEXECUTOR_H

#include <cstring>
#include <map>
#include <sstream>

#include <Interface/Tools/DataStructures/DestructureSentinel.h>

class DebugCommandExecutor : public DestructionSentinel<DebugCommandExecutor>
{
public:
  
  /**
   * Executes a specific command and returns the result as string.
   */
  virtual void executeDebugCommand(
    const std::string& command, const std::map<std::string,std::string>& arguments,
    std::stringstream &outstream) = 0;
  
  virtual ~DebugCommandExecutor() {};
};


#endif	/* _DEBUGCOMMANDEXECUTOR_H */
