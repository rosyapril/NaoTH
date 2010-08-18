
#ifndef _BATTERY_DATA_H
#define	_BATTERY_DATA_H

#include <string>
#include "Interface/PlatformInterface/PlatformInterchangeable.h"
#include "Interface/Tools/DataStructures/Printable.h"

namespace naorunner
{
  class BatteryData: public PlatformInterchangeable, public Printable
  {
  public:
    double charge;

    BatteryData();
    virtual void print(ostream& stream) const;

    virtual ~BatteryData();
  };
}

#endif	/* _BATTERY_DATA_H */
