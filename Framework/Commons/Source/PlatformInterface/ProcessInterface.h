/**
 * @author <a href="mailto:xu@informatik.hu-berlin.de">Xu, Yuan</a>
 * @author <a href="mailto:mellmann@informatik.hu-berlin.de">Mellmann, Heinrich</a>
 */

#ifndef _ProcessInterface_h_
#define _ProcessInterface_h_

#include "Process.h"
#include "ActionList.h"

#include <typeinfo>

#undef PRINT_DEBUG
#ifdef DEBUG_PLATFORM
#  define PRINT_DEBUG(m) std::err << m << std::endl
#else
#  define PRINT_DEBUG(m) ((void)0)
#endif

namespace naoth
{

/*
 *
 */
class ProcessInterface
{
public:
  ProcessInterface(Process& process, ProcessEnvironment& environment)
    : process(process), environment(environment)
  {

  }

  template<class T>
  bool registerInput(T& data)
  {
    bool result = registerAction(data, environment.inputActions, process.preActions);
    if(result) {
      PRINT_DEBUG("[ProcessInterface] register input: " << typeid(T).name());
    } else {
      std::cerr << "[ProcessInterface] WARNING: platform doesn't provide input: " << typeid(T).name() << std::endl;
    }
    return result;
  }


  template<class T>
  bool registerOutput(const T& data)
  {
    bool result = registerAction(data, environment.outputActions, process.postActions);
    if(result)
      PRINT_DEBUG("[ProcessInterface] register output: " << typeid(T).name());
    else
      std::cerr << "[ProcessInterface] WARNING: platform doesn't provide output: " << typeid(T).name() << std::endl;
    return result;
  }


  template<class T>
  bool registerInputChanel(T& data) 
  {
    AbstractAction* action = environment.channelActionCreator.createInputChanelAction<T>(data);
    if(action != NULL) process.preActions.push_back(action);
    return (action != NULL);
  }

  template<class T, int maxSize>
  bool registerBufferedInputChanel(RingBuffer<T, maxSize>& buffer)
  {
    AbstractAction* action =
        environment.channelActionCreator.createBufferedInputChanelAction<T, maxSize>(buffer);
    if(action != NULL) process.preActions.push_back(action);
    return (action != NULL);
  }

  template<class T>
  bool registerOutputChanel(const T& data)
  { 
    AbstractAction* action = environment.channelActionCreator.createOutputChanelAction<T>(data);
    if(action != NULL) process.postActions.push_back(action); 
    return (action != NULL);
  }

private:

  template<class T>
  bool registerAction(T& data, const TypedActionCreatorMap& avaliableActions, ActionList& actionList)
  {
    AbstractAction* action = avaliableActions.createAction(data);
      
    // if an action could be created put it in the list
    if(action) actionList.push_back(action);

    return action != NULL;
  }


  Process& process;
  ProcessEnvironment& environment;
};// end ProsessInterface

}// namespace naoth

#endif  /* _ProcessInterface_h_ */

