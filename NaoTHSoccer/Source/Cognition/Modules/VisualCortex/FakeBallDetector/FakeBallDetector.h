#ifndef FAKEBALLDETECTOR_H
#define FAKEBALLDETECTOR_H

#include <ModuleFramework/Module.h>

#include "Tools/Debug/DebugRequest.h"
#include "Tools/Debug/DebugModify.h"

#include "Representations/Perception/BallPercept.h"
#include "Representations/Infrastructure/FrameInfo.h"

#include "Representations/Perception/CameraMatrix.h"
#include <Tools/CameraGeometry.h>

#include <Tools/naoth_eigen.h>

BEGIN_DECLARE_MODULE(FakeBallDetector)
  PROVIDE(DebugRequest)
  PROVIDE(DebugModify)

  REQUIRE(FrameInfo)

  REQUIRE(CameraInfo)
  REQUIRE(CameraMatrix)

  REQUIRE(CameraInfoTop)
  REQUIRE(CameraMatrixTop)

  PROVIDE(BallPercept)
  PROVIDE(BallPerceptTop)
END_DECLARE_MODULE(FakeBallDetector)


class FakeBallDetector: private FakeBallDetectorBase
{
public:
    FakeBallDetector();
    ~FakeBallDetector();

    virtual void execute();

private:
    FrameInfo lastFrame;

    double active;

    Eigen::Vector2d startPosition;
    Eigen::Vector2d position;
    Eigen::Vector2d velocity;

    const Eigen::Vector2d simulateConstantMovementOnField(double dt, const Eigen::Vector2d& velocity);
};

#endif // FAKEBALLDETECTOR_H
