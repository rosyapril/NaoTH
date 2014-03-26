/**
* @file MonteCarloSelfLocatorSimple.h
*
* @author <a href="mailto:mellmann@informatik.hu-berlin.de">Heinrich Mellmann</a>
* Declaration of class MonteCarloSelfLocatorSimple
*/

#ifndef _MonteCarloSelfLocatorSimple_h_
#define _MonteCarloSelfLocatorSimple_h_

#include <ModuleFramework/Module.h>

#include "Tools/Math/Geometry.h"
#include "Tools/CameraGeometry.h"
#include <Tools/DataStructures/ParameterList.h>
#include "Tools/Debug/DebugParameterList.h"

// representations
// basic info
#include "Representations/Infrastructure/FieldInfo.h"
#include "Representations/Infrastructure/FrameInfo.h"

// motion / kinematics
#include "Representations/Modeling/OdometryData.h"
#include "Representations/Perception/CameraMatrix.h"

// situation
#include "Representations/Motion/MotionStatus.h"
#include "Representations/Modeling/BodyState.h"
#include "Representations/Modeling/SituationStatus.h"

// sensor percepts
#include "Representations/Perception/GoalPercept.h"

// this are the results :)
#include "Representations/Modeling/RobotPose.h"
#include "Representations/Modeling/GoalModel.h"

// basic tools
#include "SampleSet.h"
#include "CanopyClustering.h"


//////////////////// BEGIN MODULE INTERFACE DECLARATION ////////////////////

BEGIN_DECLARE_MODULE(MonteCarloSelfLocatorSimple)
  REQUIRE(FieldInfo)
  REQUIRE(FrameInfo)

  REQUIRE(OdometryData)
  REQUIRE(CameraMatrix)
  REQUIRE(CameraMatrixTop)

  REQUIRE(MotionStatus)
  REQUIRE(BodyState)
  REQUIRE(SituationStatus)

  REQUIRE(GoalPercept)
  REQUIRE(GoalPerceptTop)

  PROVIDE(RobotPose)
  PROVIDE(SelfLocGoalModel)
END_DECLARE_MODULE(MonteCarloSelfLocatorSimple)

//////////////////// END MODULE INTERFACE DECLARATION //////////////////////


class MonteCarloSelfLocatorSimple : private MonteCarloSelfLocatorSimpleBase
{
public:
  MonteCarloSelfLocatorSimple();
  virtual ~MonteCarloSelfLocatorSimple(){}

  void execute();

private:
  OdometryData lastRobotOdometry;
  SampleSet theSampleSet;
  SampleSet setBeforeResampling; // sort of 'double buffering'
  CanopyClustering<SampleSet> canopyClustering;
 

  class Parameters: public ParameterList
  {
  public: 
    Parameters(): ParameterList("MCSLSParameters")
    {
      PARAMETER_REGISTER(thresholdCanopy) = 900;

      // load from the file after registering all parameters
      syncWithConfig();
      DebugParameterList::getInstance().add(this);
    }

    double thresholdCanopy;

    virtual ~Parameters() {
      DebugParameterList::getInstance().remove(this);
    }
  } parameters;

};

#endif //_MonteCarloSelfLocatorSimple_h_
