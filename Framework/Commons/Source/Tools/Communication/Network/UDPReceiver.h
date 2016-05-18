/**
 * @file UDPReceiver.cpp
 * @author <a href="mailto:xu@informatik.hu-berlin.de">Xu, Yuan</a>
 *
 */

#ifndef UDPReceiver_H
#define UDPReceiver_H

#include <string>
#include <vector>
#include <glib.h>
#include <gio/gio.h>

namespace naoth
{
class UDPReceiver
{

public:
  UDPReceiver(unsigned int port, unsigned int buffersize=4096);

  void receive(std::vector<std::string>& data);

  virtual ~UDPReceiver();

  void loop();

private:
  unsigned int bufferSize;
  bool exiting;
  GSocket* socket;
  char* buffer;
  GThread* socketThread;
  std::vector<std::string> messageIn;
  GMutex*  messageInMutex;

  GError* bindAndListen(unsigned int port);
};
} // namespace naoth


#endif // UDPReceiver_H