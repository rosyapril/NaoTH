/** 
 * @file   InitialMotionFactory.cpp
 * 
 * @author <a href="mailto:xu@informatik.hu-berlin.de">Xu, Yuan</a>
 *
 */
 
#include "InitialMotionFactory.h"

// motions
#include "DeadMotion.h"
#include "InitialMotion.h"
#include "Sit.h"

InitialMotionFactory::InitialMotionFactory()
  :
  currentMotion(NULL)
{
}

InitialMotionFactory::~InitialMotionFactory()
{
  delete currentMotion;
  currentMotion = NULL;
}

AbstractMotion* InitialMotionFactory::createMotion(const MotionRequest& motionRequest)
{
  delete currentMotion;
  currentMotion = NULL;


  switch(motionRequest.id)
  {
    case motion::dead: currentMotion = new DeadMotion(); break;
    case motion::init: currentMotion = new InitialMotion(); break;
    case motion::sit: currentMotion = new Sit(); break;
    default: currentMotion = NULL;
  }//end switch

  return currentMotion;
}//end createMotion
