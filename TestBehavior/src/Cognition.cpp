/* 
 * File:   Cognition.cpp
 * Author: thomas
 * 
 * Created on 10. August 2010, 17:27
 */

#include "Cognition.h"

#include <PlatformInterface/Platform.h>

#include <iostream>

Cognition::Cognition()
{
  Platform::getInstance().thePlatformInterface->registerCognitionCallback(this);
}

void Cognition::call()
{
  std::cout << "Cognition was called" << std::endl;
}

Cognition::~Cognition()
{
}

