/* 
 * File:   DebugServer.h
 * Author: thomas
 *
 * Created on 10. September 2010, 15:38
 */

#ifndef DEBUGSERVER_H
#define	DEBUGSERVER_H

#include <glib.h>

#include <Tools/DataStructures/DestructureSentinel.h>

#include "DebugCommandExecutor.h"
#include "DebugCommunicator.h"

class DebugServer : public DebugCommandExecutor, public DestructionListener<DebugCommandExecutor>
{
public:
  DebugServer();
  virtual ~DebugServer();

  virtual void start(unsigned short port);

  /**
   * Register a command and a handler for this command.
   * @param commmand The name of the command
   * @param description A description displayed to the user if he uses the help function.
   *                    You might want to describe possible arguments here, too.
   * @param executor The callback handler.
   * @return
   */
  bool registerCommand(std::string command, std::string description,
    DebugCommandExecutor* executor);
  
  virtual void execute();

  virtual void executeDebugCommand(const std::string& command,
    const std::map<std::string,std::string>& arguments,
    std::ostream& out);

  virtual void objectDestructed(DebugCommandExecutor* object);

  static void* reader_static(void* ref);
  static void* writer_static(void* ref);
private:

  DebugCommunicator comm;
  GThread* readerThread;
  GThread* writerThread;

  GAsyncQueue* commands;
  GAsyncQueue* answers;

  /** hash map with all registered callback function  */
  std::map<std::string, DebugCommandExecutor*> executorMap;
  std::map<std::string, std::string> descriptionMap;

  void mainReader();
  void mainWriter();
  void handleCommand(char* cmdRaw, GString* answer);
  void handleCommand(std::string command, std::map<std::string,std::string> arguments,
    GString* answer, bool encodeBase64);
  
};

#endif	/* DEBUGSERVER_H */

