/* 
 * File:   ButtonData.h
 * Author: thomas
 *
 * Created on 6. März 2009, 10:40
 */

#ifndef _BUTTONDATA_H
#define	_BUTTONDATA_H

#include "Interface/PlatformInterface/PlatformInterchangeable.h"
#include "Interface/Tools/DataStructures/Printable.h"

namespace naorunner
{
  class ButtonData : public PlatformInterchangeable, public Printable
  {
  public:
    ButtonData();
    virtual ~ButtonData();

    enum ButtonID
    {
      Chest,
      LeftFootLeft,
      LeftFootRight,
      RightFootLeft,
      RightFootRight,
      numOfButtons
    };

    static string getButtonName(ButtonID id);

    /** Whether the button is pressed in this frame. */
    bool isPressed[numOfButtons];
    /** Overall number of contiguous pressing events */
    int eventCounter[numOfButtons];
    /** The number of frames the button was contiguous pressed. Should be 1 already
     * in the first pressed frame. */
    int numOfFramesPressed[numOfButtons];

    virtual void print(ostream& stream) const;
  private:

  };
}

#endif	/* _BUTTONDATA_H */
