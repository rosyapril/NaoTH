/**
 * @file: TeamSymbols.cpp
 * @author: <a href="mailto:scheunem@informatik.hu-berlin.de">Marcus Scheunemann</a>
 *
 * First created on 9. April 2009, 18:10
 */

#include "TeamSymbols2011.h"
#include "Representations/Infrastructure/FrameInfo.h"
#include "Representations/Modeling/GoalModel.h"
#include "Representations/Modeling/BodyState.h"
#include "Representations/Motion/MotionStatus.h"
#include "Representations/Motion/Request/MotionRequest.h"
#include "Tools/Debug/DebugModify.h"


void TeamSymbols2011::registerSymbols(xabsl::Engine& engine)
{
  engine.registerDecimalInputSymbol("team.members_alive_count", &getTeamMembersAliveCount);
  engine.registerBooleanInputSymbol("team.calc_if_is_striker", &calculateIfStriker);
  engine.registerBooleanInputSymbol("team.calc_if_is_striker_by_time_to_ball", &calculateIfStrikerByTimeToBall);
  engine.registerBooleanOutputSymbol("team.is_playing_as_striker",&setWasStriker, &getWasStriker);
  engine.registerBooleanInputSymbol("team.calc_if_is_the_last", &calculateIfTheLast);
}//end registerSymbols


TeamSymbols2011* TeamSymbols2011::theInstance = NULL;

void TeamSymbols2011::execute()
{
}

double TeamSymbols2011::getTeamMembersAliveCount()
{
  int counter = 0;
  unsigned int time = theInstance->frameInfo.getTime();

  for(std::map<unsigned int, TeamMessage::Data>::const_iterator i=theInstance->teamMessage.data.begin();
    i != theInstance->teamMessage.data.end(); ++i)
  {
    const TeamMessage::Data& messageData = i->second;

    // "alive" means sent something in the last 3 seconds
    if((time - messageData.frameInfo.getTime()) < 3000)
    {
      counter++;
    }
  }//end for

  return (double) counter;
}//end getTeamMembersAliveCount

bool TeamSymbols2011::getWasStriker()
{
  return theInstance->playerInfo.isPlayingStriker;
}//end getWasStriker

void TeamSymbols2011::setWasStriker(bool striker)
{
  theInstance->playerInfo.isPlayingStriker = striker;
}//end setWasStriker

bool TeamSymbols2011::calculateIfStriker()
{
  TeamMessage const& tm = theInstance->teamMessage;

  // initialize with max-values. Every Robot must start with same values!
  double shortestDistance = theInstance->fieldInfo.xFieldLength;
  unsigned int playerNearestToBall = 0; //nobody near to ball

  //if someone is striker, leave! Goalie can be striker (while f.e. clearing ball)
  for(std::map<unsigned int, TeamMessage::Data>::const_iterator i=tm.data.begin();
    i != tm.data.end(); ++i)
  {
    const TeamMessage::Data& messageData = i->second;
    const unsigned int number = i->first;

    // TODO: 100 frames or 100 ms?!
    if((theInstance->frameInfo.getTimeSince(i->second.frameInfo.getTime()) < 1000) && // the message is fresh...
        number != theInstance->playerInfo.gameData.playerNumber && // its not me...
        messageData.message.wasstriker() // the guy wants to be striker...
        )
    {
      return false; // let him go :)
    }
  }//end for

  // all team members except goalie!! otherwise goalie is nearest and all thinks he is striker, but he won't clear ball
  //should check who has best position to goal etc.
  for(std::map<unsigned int, TeamMessage::Data>::const_iterator i=tm.data.begin();
    i != tm.data.end(); ++i)
  {
    const TeamMessage::Data& messageData = i->second;
    const unsigned int number = i->first;

    if(number == 1) continue; // goalie is not considered
  
    double time_bonus = messageData.message.wasstriker()?4000.0:0.0;

    if(
        theInstance->frameInfo.getTimeSince(i->second.frameInfo.getTime()) < 1000 && // its fresh
        (messageData.message.timesinceballwasseen() < 1000+time_bonus )// the guy sees the ball
      )
    {
      Vector2<double> ballPos;
      DataConversion::fromMessage(messageData.message.ballposition(), ballPos);
      double ballDistance = ballPos.abs();

      // striker bonus
      if (messageData.message.wasstriker())
        ballDistance -= 100;

      // remember the closest guy
      if(ballDistance < shortestDistance)
      {
        shortestDistance = ballDistance;
        playerNearestToBall = number;
      }    
    }//end if
  }//end for

  // am I the closest one?
  return playerNearestToBall == theInstance->playerInfo.gameData.playerNumber;
}//end calculateIfStriker

bool TeamSymbols2011::calculateIfStrikerByTimeToBall()
{
  TeamMessage const& tm = theInstance->teamMessage;

  double shortestTime = theInstance->soccerStrategy.timeToBall;
  if ( theInstance->playerInfo.isPlayingStriker ) shortestTime-=100;

  for(std::map<unsigned int, TeamMessage::Data>::const_iterator i=tm.data.begin();
      i != tm.data.end(); ++i)
  {
    const naothmessages::TeamCommMessage& msg = i->second.message;
    if (
      i->first != theInstance->playerInfo.gameData.playerNumber
      && msg.wasstriker()
      && msg.timesinceballwasseen() + theInstance->frameInfo.getTimeSince(i->second.frameInfo.getTime()) < 2000
      )
      {
        if(msg.timetoball() < shortestTime)
        {
          return false;
        }
      }
  }//end for

  return true;

}//end calculateIfStriker

TeamSymbols2011::~TeamSymbols2011()
{
}

/** the robot which is closest to own goal is defined as the last one */
bool TeamSymbols2011::calculateIfTheLast()
{
  unsigned int fn = theInstance->frameInfo.getFrameNumber();

  TeamMessage const& tm = theInstance->teamMessage;

  // initialize with own values
  double shortestDistance = (theInstance->robotPose.translation - theInstance->fieldInfo.ownGoalCenter).abs();
  unsigned int playerNearestToOwnGoal = theInstance->playerInfo.gameData.playerNumber;

  // check all non-penalized and non-striker team members
  for(std::map<unsigned int, TeamMessage::Data>::const_iterator i=tm.data.begin();
    i != tm.data.end(); ++i)
  {
    const TeamMessage::Data& messageData = i->second;
    const int number = i->first;

    if ((fn - messageData.frameInfo.getFrameNumber()) < 100 && // alive?
        !messageData.message.ispenalized() && // not penalized?
        !messageData.message.wasstriker() &&
        number != 1 // no goalie
        )
    {
      Vector2<double> robotpos;
      robotpos.x = messageData.message.positiononfield().translation().x();
      robotpos.y = messageData.message.positiononfield().translation().y();
      double d = (robotpos-theInstance->fieldInfo.ownGoalCenter).abs();
      if ( d < shortestDistance )
      {
        shortestDistance = d;
        playerNearestToOwnGoal = number;
      }
    }//end if
  }//end for

  // is it me?
  return playerNearestToOwnGoal == theInstance->playerInfo.gameData.playerNumber;
}//end calculateIfTheLast