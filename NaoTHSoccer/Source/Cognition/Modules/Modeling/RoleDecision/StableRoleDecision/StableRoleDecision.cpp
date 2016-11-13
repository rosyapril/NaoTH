/**
* @file StableRoleDecision.h
*
* @author <a href="mailto:schahin.tofangchi@hu-berlin.de">Schahin Tofangchi</a>
*/

#include "StableRoleDecision.h"
#include <PlatformInterface/Platform.h>
#include <Tools/DataConversion.h>

#include <math.h>
#include <list>

using namespace std;


StableRoleDecision::StableRoleDecision()
{
  getDebugParameterList().add(&parameters);
}

StableRoleDecision::~StableRoleDecision()
{
  getDebugParameterList().remove(&parameters);
}

void StableRoleDecision::execute() {
    getRoleDecisionModel().resetRobotStates();
    computeStrikers();

}//end execute

void StableRoleDecision::computeStrikers()
{
    // container storing robots, which want to be striker, and their time to ball
    std::map<unsigned int, unsigned int> possible_striker;

    // iterate over all robots(messages)
    TeamMessage const& tm = getTeamMessage();
    for (std::map<unsigned int, TeamMessage::Data>::const_iterator i=tm.data.begin(); i != tm.data.end(); ++i) {
        unsigned int robotNumber = i->first;
        const TeamMessage::Data& msg = i->second;

        // if striker lost the ball, he gets a time bonus before he lost the ball completely ...
        double loose_ball_bonus = msg.playerNum==getRoleDecisionModel().firstStriker?parameters.strikerBonusTime:0.0;

        // check if the robot is able to play and sees the ball
        bool isRobotInactive = msg.fallen
                || msg.isPenalized
                || msg.ballAge < 0 //Ball hasn't been seen
                || (msg.ballAge + getFrameInfo().getTimeSince(msg.frameInfo.getTime()) > parameters.maxBallLostTime + loose_ball_bonus); //Ball isn't fresh

        // ignore "DEAD" and inactive robots
        if(isRobotDead(robotNumber) || isRobotInactive) { continue; }

        // for all active robots, which sees the ball AND previously announced to want to be striker ...
        if (msg.wasStriker) {
            // ... remember them as possible striker
            possible_striker[robotNumber] = msg.timeToBall;
        }
    }//end for

    // i want to be striker, if i'm not the goalie and i'm "active" (not fallen/panelized, see the ball)!!!
    getRoleDecisionModel().wantsToBeStriker = !getPlayerInfo().isGoalie() && amIactive();

    // if i'm striker, i get a time bonus!
    double ownTimeToBall = getSoccerStrategy().timeToBall - (getPlayerInfo().isPlayingStriker?300:0);

    // clear for new striker decision
    getRoleDecisionModel().resetStriker();
    // set the new striker
    for (auto it = possible_striker.cbegin(); it != possible_striker.cend(); ++it) {
        //If two robots want to be striker, the one with a smaller number is favoured => is the first in the map!!
        if(getRoleDecisionModel().firstStriker == std::numeric_limits<int>::max()) {
            getRoleDecisionModel().firstStriker = it->first;
        } else if (getRoleDecisionModel().secondStriker == std::numeric_limits<int>::max()) {
            getRoleDecisionModel().secondStriker = it->first;
        }
        // if there's a robot closer to the ball than myself, i don't want to be striker!
        if(it->second < ownTimeToBall) {
            getRoleDecisionModel().wantsToBeStriker = false;
        }
    }

    PLOT(std::string("StableRoleDecision:FirstStrikerDecision"), getRoleDecisionModel().firstStriker);
    PLOT(std::string("StableRoleDecision:SecondStrikerDecision"), getRoleDecisionModel().secondStriker);
}

bool StableRoleDecision::isRobotDead(unsigned int robotNumber) {
    double failureProbability = 0.0;
    std::map<unsigned int, double>::const_iterator robotFailure = getTeamMessageStatisticsModel().failureProbabilities.find(robotNumber);
    if (robotFailure != getTeamMessageStatisticsModel().failureProbabilities.end()) {
      failureProbability = robotFailure->second;
    }
    bool isDead = failureProbability > parameters.minFailureProbability && robotNumber != getPlayerInfo().playerNumber;
    // update dead/alive lists
    if (isDead) { //Message is not fresh
      getRoleDecisionModel().deadRobots.push_back(robotNumber);
    } else {
      getRoleDecisionModel().aliveRobots.push_back(robotNumber);
    }
    return isDead;
}
