/**
 * @file SimSparkController.cpp
 *
 * @author <a href="mailto:xu@informatik.hu-berlin.de">Xu Yuan</a>
 * @breief Interface for the SimSpark simulator
 *
 */

#include "SimSparkController.h"
#include <iostream>

//#include "Tools/NaoInfo.h"

//#include "CommunicationCollectionImpl.h"
//#include "Tools/Debug/DebugRequest.h"
//#include "Tools/Debug/DebugModify.h"

using namespace std;

SimSparkController::SimSparkController()
: AbstractPlatform<SimSparkController>("SimSpark", 20)
{
  // init the name -- id maps
  theJointSensorNameMap.clear();
  theJointSensorNameMap["hj1"] = JointData::HeadYaw;
  theJointSensorNameMap["hj2"] = JointData::HeadPitch;
  theJointSensorNameMap["laj1"] = JointData::LShoulderPitch;
  theJointSensorNameMap["laj2"] = JointData::LShoulderRoll;
  theJointSensorNameMap["laj3"] = JointData::LElbowYaw;
  theJointSensorNameMap["laj4"] = JointData::LElbowRoll;
  theJointSensorNameMap["llj1"] = JointData::LHipYawPitch;
  theJointSensorNameMap["llj2"] = JointData::LHipRoll;
  theJointSensorNameMap["llj3"] = JointData::LHipPitch;
  theJointSensorNameMap["llj4"] = JointData::LKneePitch;
  theJointSensorNameMap["llj5"] = JointData::LAnklePitch;
  theJointSensorNameMap["llj6"] = JointData::LAnkleRoll;
  theJointSensorNameMap["raj1"] = JointData::RShoulderPitch;
  theJointSensorNameMap["raj2"] = JointData::RShoulderRoll;
  theJointSensorNameMap["raj3"] = JointData::RElbowYaw;
  theJointSensorNameMap["raj4"] = JointData::RElbowRoll;
  theJointSensorNameMap["rlj1"] = JointData::RHipYawPitch;
  theJointSensorNameMap["rlj2"] = JointData::RHipRoll;
  theJointSensorNameMap["rlj3"] = JointData::RHipPitch;
  theJointSensorNameMap["rlj4"] = JointData::RKneePitch;
  theJointSensorNameMap["rlj5"] = JointData::RAnklePitch;
  theJointSensorNameMap["rlj6"] = JointData::RAnkleRoll;

  // motor names
  theJointMotorNameMap.clear();
  theJointMotorNameMap[JointData::HeadYaw] = "he1";
  theJointMotorNameMap[JointData::HeadPitch] = "he2";
  theJointMotorNameMap[JointData::LShoulderPitch] = "lae1";
  theJointMotorNameMap[JointData::LShoulderRoll] = "lae2";
  theJointMotorNameMap[JointData::LElbowYaw] = "lae3";
  theJointMotorNameMap[JointData::LElbowRoll] = "lae4";
  theJointMotorNameMap[JointData::LHipYawPitch] = "lle1";
  theJointMotorNameMap[JointData::LHipRoll] = "lle2";
  theJointMotorNameMap[JointData::LHipPitch] = "lle3";
  theJointMotorNameMap[JointData::LKneePitch] = "lle4";
  theJointMotorNameMap[JointData::LAnklePitch] = "lle5";
  theJointMotorNameMap[JointData::LAnkleRoll] = "lle6";
  theJointMotorNameMap[JointData::RShoulderPitch] = "rae1";
  theJointMotorNameMap[JointData::RShoulderRoll] = "rae2";
  theJointMotorNameMap[JointData::RElbowYaw] = "rae3";
  theJointMotorNameMap[JointData::RElbowRoll] = "rae4";
  theJointMotorNameMap[JointData::RHipYawPitch] = "rle1";
  theJointMotorNameMap[JointData::RHipRoll] = "rle2";
  theJointMotorNameMap[JointData::RHipPitch] = "rle3";
  theJointMotorNameMap[JointData::RKneePitch] = "rle4";
  theJointMotorNameMap[JointData::RAnklePitch] = "rle5";
  theJointMotorNameMap[JointData::RAnkleRoll] = "rle6";

  theCameraId = 0;
  theSenseTime = 0;

  pthread_mutex_init(&theCognitionInputMutex, NULL);
  pthread_cond_init(&theCognitionInputCond, NULL);

  maxJointAbsSpeed = Math::fromDegrees(351.77);

  theTeamName = "NaoTH";
}

SimSparkController::~SimSparkController()
{
}

bool SimSparkController::init(const std::string& teamName, unsigned int num, const std::string& server, unsigned int port)
{
  theTeamName = teamName;
  // connect to the simulator
  try
  {
    theSocket.connect(server, port);
  } catch (SocketException& e)
  {
    cerr << e.what() << endl;
    return false;
  }

  // send create command to simulator
  theSocket << "(scene rsg/agent/nao/nao.rsg)" << send;
  // wait the response
  updateSensors();
  // initialize the teamname and number
  theSocket << "(init (teamname " << teamName << ")(unum " << num<< "))" << send;
  // wait the response
  while (theGameInfo.thePlayerNum == 0)
  {
    updateSensors();
  }
  // we should get the team index and player number now

  // calculate debug communicaiton port
  unsigned short debugPort = 5401;
  if (theGameInfo.theTeamIndex == SimSparkGameInfo::TI_LEFT )
  {
    debugPort = 5400 + theGameInfo.thePlayerNum;
  } else if (theGameInfo.theTeamIndex == SimSparkGameInfo::TI_RIGHT)
  {
    debugPort = 5500 + theGameInfo.thePlayerNum;
  }

  //Platform::getInstance().init(this, new SimSparkCommunicationCollection(debugPort,theGameInfo, theTeamComm));
  
  //Cognition::getInstance().init();
  //Motion::getInstance().init();

  Platform::getInstance().init(this);



  /*
  if (theGameInfo.theTeamIndex == SimSparkGameInfo::TI_LEFT ){
    thePlayerInfoInitializer.thePlayerInfo.teamColor = PlayerInfo::blue;
    thePlayerInfoInitializer.thePlayerInfo.teamNumber = 1;
  }
  else if (theGameInfo.theTeamIndex == SimSparkGameInfo::TI_RIGHT ){
    thePlayerInfoInitializer.thePlayerInfo.teamColor = PlayerInfo::red;
    thePlayerInfoInitializer.thePlayerInfo.teamNumber = 2;
  }
  thePlayerInfoInitializer.thePlayerInfo.playerNumber = theGameInfo.thePlayerNum;
  */

  cout << "NaoTH Simpark initialization successful: " << teamName << " " << theGameInfo.thePlayerNum << endl;

  //startPoseCfg = ConfigLoader::loadConfig("Config/start_pose.cfg");
  beam();

  //DEBUG_REQUEST_REGISTER("SimSparkController:beam", "beam to start pose", false);

  return true;
}

void SimSparkController::main()
{
  cout << "SImSpark Controller runs in signle thread" << endl;
  while ( updateSensors() )
  {
    //Cognition::getInstance().main();
    //Motion::getInstance().main();
    callCognition();
    callMotion();
  }//end while
}//end main

void SimSparkController::motionLoop()
{
  while ( updateSensors() )
  {    
    //Motion::getInstance().main();
    callMotion();
  }
}//end motionLoop

void SimSparkController::cognitionLoop()
{
  while (true)
  {
    //Cognition::getInstance().main();
    callCognition();
  }
}//end cognitionLoop


void* motionLoopWrap(void* c)
{
  SimSparkController* ctr = static_cast<SimSparkController*> (c);
  ctr->motionLoop();
  pthread_exit(NULL);
  return NULL;
}//end motionLoopWrap

void SimSparkController::multiThreadsMain()
{
  cout << "SimSpark Controller runs in multi-threads" << endl;

  //Motion::getInstance().main();
  callMotion();

  pthread_t motionThread;
  int mt = pthread_create(&motionThread, NULL, motionLoopWrap, this);
  ASSERT(mt == 0);

  cognitionLoop();
}//end multiThreadsMain

void SimSparkController::getMotionInput()
{
  PlatformInterface::getMotionInput();

  for (int i = 0; i < JointData::numOfJoint; i++)
  {
    theSensorJointData.hardness[i] = theLastSensorJointData.hardness[i];
  }
  theLastSensorJointData = theSensorJointData;

}

void SimSparkController::setMotionOutput()
{
  PlatformInterface::setMotionOutput();
  say();
  autoBeam();
  jointControl();
  theSocket << send;
}

void SimSparkController::getCognitionInput()
{
  pthread_mutex_lock(&theCognitionInputMutex);
  while (!isNewImage)
  {
    pthread_cond_wait(&theCognitionInputCond, &theCognitionInputMutex);
  }
  PlatformInterface::getCognitionInput();
  pthread_mutex_unlock(&theCognitionInputMutex);
}

bool SimSparkController::updateSensors()
{
  double lastSenseTime = theSenseTime;
  string msg;
  theSocket >> msg;

  if ( updateImage(msg) ){
    theSocket>>msg;
  }

//  cout << "Sensor data: " << msg << endl;

  pcont_t* pcont;
  sexp_t* sexp;
  char* c = const_cast<char*> (msg.c_str());
  pcont = init_continuation(c);
  sexp = iparse_sexp(c, msg.size(), pcont);

  pthread_mutex_lock(&theCognitionInputMutex);

  // clear FSR data, since if there is no FSR data, it means no toch
  for (int i = 0; i < FSRData::numOfFSR; i++)
  {
    theFSRData.data[i] = 0;
  }

  do
  {
    const sexp_t* t = sexp->list;
    if (SexpParser::isVal(t))
    {
      bool ok = true;
      string name(t->val);
      if ("HJ" == name) ok = updateHingeJoint(t->next); // hinge joint
      else if ("FRP" == name)
      {
        ok = updateFSR(t->next); // force sensor
      } else if ("See" == name)
      {// See
        //theVirtualVision.clear();
        ok = updateSee("", t->next);
        if ( ok ) isNewImage = true;
      } else if ("time" == name)
      {
        ok = SexpParser::parseGivenValue(t->next, "now", theSenseTime); // time
        theStepTime = theSenseTime - lastSenseTime;
        if ( static_cast<unsigned int>(theStepTime*100)*10 > getBasicTimeStep() )
          cerr<<"warnning: the step is "<<theStepTime<<" s"<<endl;
      } else if ("GYR" == name) ok = updateGyro(t->next); // gyro rate
      else if ("ACC" == name) ok = updateAccelerometer(t->next);
      else if ("GS" == name) ok = theGameInfo.update(t->next); // game state
      else if ("hear" == name)  ok = hear(t->next);// hear
      else if ("IMU" == name) ok = updateIMU(t->next); // interial sensor data
      else cerr << " Perception unknow name: " << string(t->val) << endl;
      if (!ok)
      {
        cerr << " Perception update failed: " << string(t->val) << endl;
        return false;
      }
    }
    destroy_sexp(sexp);
    sexp = iparse_sexp(c, msg.size(), pcont);
  } while (sexp);

  updateInertialSensor();

  if ( isNewImage ){
    pthread_cond_signal(&theCognitionInputCond);
  }
  pthread_mutex_unlock(&theCognitionInputMutex);

  destroy_sexp(sexp);
  destroy_continuation(pcont);

  return true;
}

bool SimSparkController::updateImage(const std::string& msg)
{
  if ("Image" == msg.substr(0, 5))
  {
    pthread_mutex_lock(&theCognitionInputMutex);
    theImageData = msg.substr(5);
    isNewImage = true;
    pthread_mutex_unlock(&theCognitionInputMutex);
    return true;
  }
  return false;
}

bool SimSparkController::updateHingeJoint(const sexp_t* sexp)
{
  // get the name
  std::string name;
  if (!SexpParser::parseGivenValue(sexp, "n", name))
  {
    cerr << "can not get the HJ name!\n";
    return false;
  }

  // get the id
  JointData::JointID jid = theJointSensorNameMap[name];
  if (JointData::numOfJoint <= jid)
  {
    cerr << "unknown the HJ name!" << name << '\n';
    return false;
  }

  // set the joint
  sexp = sexp->next;
  double ax;
  if (!SexpParser::parseGivenValue(sexp, "ax", ax))
  {
    cerr << "can not get the data of joint " << name << '\n';
    return false;
  }

  ax *= (Math::pi / 180);
  // due to the different coordination
  if (JointData::HeadPitch == jid
    || JointData::LShoulderPitch == jid
    || JointData::RShoulderPitch == jid
    || JointData::LHipPitch == jid
    || JointData::RHipPitch == jid
    || JointData::LKneePitch == jid
    || JointData::RKneePitch == jid
    || JointData::LAnklePitch == jid
    || JointData::RAnklePitch == jid)
  {
    ax *= -1;
  }

  theSensorJointData.dp[jid] = Math::clamp(Math::normalizeAngle(ax - theSensorJointData.position[jid]) / theStepTime,
    -maxJointAbsSpeed, maxJointAbsSpeed);
  theSensorJointData.position[jid] = ax;
  return true;
}

bool SimSparkController::updateGyro(const sexp_t* sexp)
{
  // get the name
  std::string name;
  if (!SexpParser::parseGivenValue(sexp, "n", name))
  {
    cerr << "can not get the Gyro name!\n";
    return false;
  }

  if ("torso" != name)
  {
    cerr << "can not handle gyro : " << name << endl;
    return false;
  }

  double data[3];
  if (!SexpParser::parseGivenArrayValue(sexp->next, "rt", 3, data))
  {
    cerr << "can not get the Gyro data!\n";
    return false;
  }

  theGyroData.data[0] = Math::fromDegrees(data[1]);
  theGyroData.data[1] = -Math::fromDegrees(data[0]);

  return true;
}

bool SimSparkController::updateAccelerometer(const sexp_t* sexp)
{
  // get the name
  std::string name;
  if (!SexpParser::parseGivenValue(sexp, "n", name))
  {
    cerr << "can not get the Accelerometer name!\n";
    return false;
  }

  if ("torso" != name)
  {
    cerr << "can not handle Accelerometer : " << name << endl;
    return false;
  }

  double acc[3];
  if (!SexpParser::parseGivenArrayValue(sexp->next, "a", 3, acc))
  {
    cerr << "can not get the Accelerometer data!\n";
    return false;
  }

  swap(acc[0], acc[1]);
  acc[1] = -acc[1];
  double k = 0.1;
  for (int i = 0; i < AccelerometerData::numOfAccelerometer; i++)
  {
    theAccelerometerData.data[i] = theAccelerometerData.data[i]*(1-k) + k*acc[i];
  }

  return true;
}

bool SimSparkController::updateFSR(const sexp_t* sexp)
{
  // get the name
  std::string name;
  if (!SexpParser::parseGivenValue(sexp, "n", name))
  {
    cerr << "can not get the ForceResistancePerceptor name!\n";
    return false;
  }

  double C[3], F[3];
  sexp = sexp->next;
  if (!SexpParser::parseGivenArrayValue(sexp, "c", 3, C))
  {
    cerr << "can not get the ForceResistancePerceptor c!\n";
    return false;
  }

  sexp = sexp->next;
  if (!SexpParser::parseGivenArrayValue(sexp, "f", 3, F))
  {
    cerr << "can not get the ForceResistancePerceptor f!\n";
    return false;
  }


  FSRData::FSRID id0, id1, id2, id3;
  if ("lf" == name)
  {
    id0 = FSRData::LFsrBL;
    id1 = FSRData::LFsrBR;
    id2 = FSRData::LFsrFL;
    id3 = FSRData::LFsrFR;
  } else if ("rf" == name)
  {
    id0 = FSRData::RFsrBL;
    id1 = FSRData::RFsrBR;
    id2 = FSRData::RFsrFL;
    id3 = FSRData::RFsrFR;
  } else
  {
    cerr << "unknow ForceResistancePerceptor name: " << name << endl;
    return false;
  }

  double f = F[2] / 4;
  double fx = f * (C[1]*1000 + 30);
  double fy = f * (-C[0]*1000);
  calFSRForce(f, fx, fy, id0, id1, id2);
  calFSRForce(f, fx, fy, id1, id2, id3);
  calFSRForce(f, fx, fy, id2, id3, id0);
  calFSRForce(f, fx, fy, id3, id0, id1);

  return true;
}

Vector3d SimSparkController::decomposeForce(double f, double fx, double fy, const Vector3d& c0, const Vector3d& c1, const Vector3d& c2)
{
  Matrix3x3<double> A(Vector3d(1, c0.x, c0.y), Vector3d(1, c1.x, c1.y), Vector3d(1, c2.x, c2.y));
  return A.invert() * Vector3d(f, fx, fy);
}

void SimSparkController::calFSRForce(double f, double x, double y, FSRData::FSRID id0, FSRData::FSRID id1, FSRData::FSRID id2)
{
  Vector3d F = decomposeForce(f, x, y, FSRData::offset[id0], FSRData::offset[id1], FSRData::offset[id2]);
  theFSRData.data[id0] += F.x;
  theFSRData.data[id1] += F.y;
  theFSRData.data[id2] += F.z;
}

bool SimSparkController::updateSee(const string& preName, const sexp_t* sexp)
{
  std::string name;
  while (sexp)
  {
    name = "";
    const sexp_t* t = sexp->list;
    if (SEXP_VALUE == t->ty)
    {
      SexpParser::parseValue(t, name);
      if ("P" == name) // a player
      {
        string teamName;
        t = t->next;
        if (!SexpParser::parseGivenValue(t, "team", teamName))
          cerr << "[SimSparkController] Vision can not get the Player's team" << endl;

        string id;
        t = t->next;
        if (!SexpParser::parseGivenValue(t, "id", id))
          cerr << "[SimSparkController] Vision can not get Player's id" << endl;
        if ( !updateSee("P "+teamName+" "+id+" ", t->next) )
          return false;
      }
      else if ("L"==name)
      {
        double p0[3], p1[3];
        if ( SexpParser::parseGivenArrayValue(t->next, "pol", 3, p0) && SexpParser::parseGivenArrayValue(t->next->next, "pol", 3, p1))
        {
          VirtualVision::Line l;
          l.p0 = Vector3d(p0[0]*1000, Math::fromDegrees(p0[1]), Math::fromDegrees(p0[2]));
          l.p1 = Vector3d(p1[0]*1000, Math::fromDegrees(p1[1]), Math::fromDegrees(p1[2]));
          theVirtualVision.lines.push_back(l);
        }
        else
        {
          cerr<<"[SimSparkController] Vision can not process line! " << endl;
        }
      }
      else
      {
        double d[3];
        if (SexpParser::parseGivenArrayValue(t->next, "pol", 3, d))
        {
          theVirtualVision.data[preName + name] = Vector3<double>(d[0]*1000, Math::fromDegrees(d[1]), Math::fromDegrees(d[2]));
        } else if (SexpParser::parseArrayValue(t->next, 3, d))
        {
          theVirtualVision.data[preName + name] = Vector3d(d[0], d[1], d[2])*1000;
        } else
        {
          cerr << "[SimSparkController] Vision can not get Object " << name << endl;
        }
      }
    }
    sexp = sexp->next;
  }

  return true;
}

void SimSparkController::get(FrameInfo& data)
{
  data.time = static_cast<unsigned int>(theSenseTime * 1000.0);
  data.frameNumber++;
}

void SimSparkController::get(SensorJointData& data)
{
  data = theSensorJointData;
  // hip joints should be the same!
  double hipYawPitch = (data.position[JointData::LHipYawPitch] + data.position[JointData::RHipYawPitch] ) * 0.5;
  data.position[JointData::LHipYawPitch] = hipYawPitch;
  data.position[JointData::RHipYawPitch] = hipYawPitch;
  hipYawPitch = (data.dp[JointData::LHipYawPitch] + data.dp[JointData::RHipYawPitch] ) * 0.5;
  data.dp[JointData::LHipYawPitch] = hipYawPitch;
  data.dp[JointData::RHipYawPitch] = hipYawPitch;
  hipYawPitch = (data.ddp[JointData::LHipYawPitch] + data.ddp[JointData::RHipYawPitch] ) * 0.5;
  data.ddp[JointData::LHipYawPitch] = hipYawPitch;
  data.ddp[JointData::RHipYawPitch] = hipYawPitch;
}

void SimSparkController::get(AccelerometerData& data)
{
  data = theAccelerometerData;
}

void SimSparkController::get(Image& data)
{
  ASSERT(isNewImage);

  const char* img = theImageData.data();
  data.setCameraInfo(Platform::getInstance().theCameraInfo);

  unsigned int width = data.cameraInfo.resolutionWidth;
  unsigned int height = data.cameraInfo.resolutionHeight;
  unsigned int resolution = width*height;

  ASSERT(resolution * 3 == theImageData.size());

  for (unsigned int x = 0; x < width; x++)
  {
    for (unsigned int y = 0; y < height; y++)
    {
      int idx = (resolution - width * (y + 1) + x)*3;
      Pixel p;
      p.y = img[idx];
      p.u = img[idx + 1];
      p.v = img[idx + 2];
/*
      ColorModelConversions::fromRGBToYCbCr(
        p.y, p.u, p.v,
        p.y, p.u, p.v);
*/
      data.set(x,y,p);
    }
  }

  //theVirtualVisionProvider.theVirtualVision = theVirtualVision;
  isNewImage = false;
}

void SimSparkController::get(GyrometerData& data)
{
  data = theGyroData;
}

void SimSparkController::get(FSRData& data)
{
  for (int i = 0; i < FSRData::numOfFSR; i++)
  {
    theFSRData.force[i] = Math::clamp(theFSRData.data[i], 0.0, 25.0);
  }
  data = theFSRData;
}

void SimSparkController::get(InertialSensorData& data)
{
    //data = theInertialSensorData;
}

void SimSparkController::updateInertialSensor()
{
  /*
  // calculate inertial sensor data by gyrometer
  const double *gyrometer = theGyroData.data;
  static double oldGyroX = gyrometer[0];
  static double oldGyroY = gyrometer[1];
  theInertialSensorData.data[InertialSensorData::X] += ((gyrometer[0]+oldGyroX) * 0.5 * theStepTime);
  theInertialSensorData.data[InertialSensorData::Y] += ((gyrometer[1]+oldGyroY) * 0.5 * theStepTime);
  oldGyroX = gyrometer[0];
  oldGyroY = gyrometer[1];

  // calculate intertial sensor data by accelerometer
  const double *acc = theAccelerometerData.data;
  double len = sqrt(Math::sqr(acc[0]) + Math::sqr(acc[1]) + Math::sqr(acc[2]));

  if (len > 2 && len < 30)
  {
    double x = asin(acc[1] / len);
    double cx = cos(x);
    double y = -atan2(acc[0] / Math::sgn(cx), acc[2] / Math::sgn(cx));

    double k = 0.04;
//    MODIFY("updateInertialSensor.k", k);
    theInertialSensorData.data[InertialSensorData::X] = (1 - k) * theInertialSensorData.data[InertialSensorData::X] + k * x;
    theInertialSensorData.data[InertialSensorData::Y] = (1 - k) * theInertialSensorData.data[InertialSensorData::Y] + k * y;
  }

//  PLOT("IMU.X", theIMU[0]);
//  PLOT("IMU.Y",theIMU[1]);
//  PLOT("IS.X", theInertialSensorData.data[InertialSensorData::X]);
//  PLOT("IS.Y", theInertialSensorData.data[InertialSensorData::Y]);
//  PLOT("Eis.X", Math::toDegrees((theIMU[0] - theInertialSensorData.data[InertialSensorData::X])));
//  PLOT("Eis.Y", Math::toDegrees((theIMU[1] - theInertialSensorData.data[InertialSensorData::Y])));
*/
}

void SimSparkController::get(BumperData& /*data*/)
{
  // unsupport yet
}

void SimSparkController::get(IRReceiveData& /*data*/)
{
  // unsupport yet
}

void SimSparkController::get(ButtonData& /*data*/)
{
  // unsupport yet
}

void SimSparkController::set(const MotorJointData& data)
{
  theMotorJointData.push_back(data);
}

void SimSparkController::jointControl()
{
  if ( theMotorJointData.size() < 2 ) return;
  
  MotorJointData data = theMotorJointData.front();
  theMotorJointData.pop_front();
  const MotorJointData& data2 = theMotorJointData.front();
  
  double d = 1.0 / theStepTime * 0.9;
  for (int i = 0; i < JointData::numOfJoint; i++)
  {
    // normalize the joint angle
    double target = data.position[i];
    double ang = theLastSensorJointData.position[i];// + theLastSensorJointData.dp[i] * theStepTime;
    double v = (target - ang) * d * data.hardness[i];
    v = Math::clamp(v, -maxJointAbsSpeed, maxJointAbsSpeed);
    ang += (v * theStepTime);
    target = data2.position[i];
    double v2 = (target - ang) * d * data2.hardness[i];
    theLastSensorJointData.hardness[i] = data2.hardness[i];
    v2 = Math::clamp(v2, -maxJointAbsSpeed, maxJointAbsSpeed);

    // due to the different coordination
    if (JointData::HeadPitch == i
      || JointData::LShoulderPitch == i
      || JointData::RShoulderPitch == i
      || JointData::LHipPitch == i
      || JointData::RHipPitch == i
      || JointData::LKneePitch == i
      || JointData::RKneePitch == i
      || JointData::LAnklePitch == i
      || JointData::RAnklePitch == i)
    {
      v2 *= -1;
    }
    theSocket << '(' << theJointMotorNameMap[(JointData::JointID)i] << ' '
      << v2
      << ')';
  }
}

void SimSparkController::set(const CameraSettingsRequest& data)
{
  // switch between two cameras is supported currently

  // switch camera
  if (theCameraId != data.data[CameraSettings::CameraSelection])
  {
    theCameraId = data.data[CameraSettings::CameraSelection];
    Pose3D p;
    const Pose3D& cameraTrans = Platform::getInstance().theCameraInfo.transformation[theCameraId];

    // due to the different coordination
    p.translation = RotationMatrix::getRotationZ(Math::fromDegrees(90)) * (cameraTrans.translation) * 0.001;
    p.rotation = RotationMatrix::getRotationZ(Math::fromDegrees(90));
    p.rotation *= cameraTrans.rotation;
    p.rotation.rotateZ(Math::fromDegrees(-90));

    theSocket << "(CameraPoseEffector " << p << ")";
  }
}

void SimSparkController::get(CurrentCameraSettings& data)
{
  data.data[CameraSettings::CameraSelection] = theCameraId;
}

void SimSparkController::set(const LEDData& /*data*/)
{
  // unsupport yet
}

void SimSparkController::set(const IRSendData& /*data*/)
{
  // unsupport yet
}

void SimSparkController::set(const UltraSoundSendData& /*data*/)
{
  // unsupport yet
}

void SimSparkController::set(const SoundData& /*data*/)
{
  // unsupport yet
}

void SimSparkController::say()
{/*
  // make sure all robot have chance to say something
  if ( ( static_cast<int>(floor(theSenseTime*1000/theBasicTimeStep/2)) % thePlayerInfoInitializer.thePlayerInfo.numOfPlayers) +1 != thePlayerInfoInitializer.thePlayerInfo.playerNumber )
    return;
  string msg = theTeamComm.peekSayMessage();
  if (!msg.empty()){
    if (msg.size()>20){
      cerr<<"SimSparkController: can not say a message longer than 20 "<<endl;
      return;
    }
    if (msg != "")
    {
//      cout<<"Nr."<<static_cast<int>(thePlayerInfoInitializer.thePlayerInfo.playerNumber)<<" say @ "<<theSenseTime<<endl;
      theSocket << ("(say "+msg+")");
    }
  }
  */
}

bool SimSparkController::hear(const sexp_t* sexp)
{
  double time;
  if (!SexpParser::parseValue(sexp, time))
  {
    std::cerr << "[SimSparkController Hear] can not get time" << std::endl;
    return false;
  }

  sexp = sexp->next;
  std::string direction;
  double dir;
  if (!SexpParser::parseValue(sexp, direction))
  {
    std::cerr << "[SimSparkController Hear] can not get direction" << std::endl;
    return false;
  }

  if ("self" == direction)
  {
    // this message come from myself, just omit it
//    return true;
  } else
  {
    if (!SexpParser::parseValue(sexp, dir))
    {
      std::cerr << "[SimSparkController Hear] can not parse the direction" << std::endl;
      return false;
    }
  }

  sexp = sexp->next;
  string msg;
  SexpParser::parseValue(sexp, msg);

  if ( !msg.empty() && msg != ""){
//    theTeamComm.addHearMessage(msg);
  }
//  std::cout << "hear message : " << time << ' ' << direction << ' ' << dir << ' ' << msg << std::endl;
  return true;
}

void SimSparkController::beam()
{
  /*
  stringstream ss;
  ss << "Player" << theGameInfo.thePlayerNum;
  double x=0, y=0, r=0;
  if (startPoseCfg.get(ss.str() + ".Pose.x", x) && startPoseCfg.get(ss.str() + ".Pose.y", y) && startPoseCfg.get(ss.str() + ".Pose.rot", r))
  {
    theSocket << "(beam "<<x<<" "<<y<<" "<<r<<")" << send;
  }
  */
}

void SimSparkController::autoBeam()
{
  /*
  DEBUG_REQUEST("SimSparkController:beam", beam(););

  static PlayerInfo::PlayMode lastPlayMode = PlayerInfo::numOfPlayMode;
  if (theGameInfo.thePlayMode == PlayerInfo::PM_GOAL_LEFT
    || theGameInfo.thePlayMode == PlayerInfo::PM_GOAL_RIGHT
    || (theGameInfo.thePlayMode == PlayerInfo::PM_BEFORE_KICK_OFF && theGameInfo.theGameTime > 1))
  {
//    if ( lastPlayMode != theGameInfo.thePlayMode ){
//      beam();// execute once
//    }
    if (int(theSenseTime / theStepTime) % 20 == 0)
    {
      beam();
    }
//    MotorJointData initJoint;
//    for (int i = 0; i < JointData::numOfJoint; i++)
//    {
//      initJoint.hardness[i] = theSensorJointData.hardness[i];
//    }
//    set(initJoint);
  }
  lastPlayMode = theGameInfo.thePlayMode;
  */
}

bool SimSparkController::updateIMU(const sexp_t* sexp)
{
  // get the name
  std::string name;
  if (!SexpParser::parseGivenValue(sexp, "n", name))
  {
    cerr << "can not get the IMU name!\n";
    return false;
  }

  if ("torso" != name)
  {
    cerr << "can not handle IMU : " << name << endl;
    return false;
  }

  double imu[9];
  if (!SexpParser::parseGivenArrayValue(sexp->next, "m", 9, imu))
  {
    cerr << "can not get the IMU data!\n";
    return false;
  }

  theIMU[0] = asin(-imu[2]);
  theIMU[1] = -atan2(imu[5], imu[8]);
  return true;
}