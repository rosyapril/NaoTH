/** 
* @file ColorTable64.h
* Declaration of class ColorTable64.
*
* @author <A href="mailto:mellmann@informatik.hu-berlin.de">Heinrich Mellmann</A>
*/

#ifndef _ColorTable64_h_
#define _ColorTable64_h_

#include "Tools/ColorClasses.h"

#include <cstring>
#include <string>
#include <fstream>

#include <Tools/DataStructures/Printable.h>
#include <Tools/ImageProcessing/ImagePrimitives.h>
#include <Tools/Debug/NaoTHAssert.h>

/**
* @class ColorTable64
*
* Contains a ColorTable64 which can decode the color for
* every 4 * 4 * 4 cube in the 255 * 255 * 255 RGB color space.
*/
class ColorTable64 : public naoth::Printable
{
public:

  ColorTable64()
  {
    reset();
  }

  virtual ~ColorTable64(){}

  inline void setColorClass(const ColorClasses::Color color, const unsigned char channel1, const unsigned char channel2, const unsigned char channel3)
  {
    unsigned int idx = ((channel1 & div4) << 10) + ((channel2 & div4) << 4) + (channel3 >> 2);
    ASSERT(idx < colorTableLength);
    colorClasses[idx] = static_cast<unsigned char>(color);
  }

  inline ColorClasses::Color getColorClass(unsigned char channel1, unsigned char channel2, unsigned char channel3) const
  {
    unsigned int idx = ((channel1 & div4) << 10) + ((channel2 & div4) << 4) + (channel3 >> 2);
    ASSERT(idx < colorTableLength);
    return static_cast<ColorClasses::Color>(colorClasses[idx]);
  }

  void reset()
  {
    memset(colorClasses, static_cast<int>(ColorClasses::none), sizeof(char) * colorTableLength);
  }

  bool loadFromFile(const std::string& fileName);

  virtual void print(std::ostream& stream) const
  {
    stream.write((const char *)colorClasses, colorTableLength);
  }

private:
  static const unsigned int colorTableLength = 64 * 64 * 64;
  // number with last two bits equal zero
  // x = y & div4, cuts the part of y which is less then 4, i.e., y is dividable by 4
  static const unsigned char div4 = ~3; 

  /** 
  * The color table.
  * Each element in the array contains the color class of a 4x4x4 cube in the color space.
  */
  char colorClasses[colorTableLength];

};

#endif  //  _ColorTable64_h_
