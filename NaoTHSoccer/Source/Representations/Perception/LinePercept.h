/** 
 * @file BallPercept.h
 * @author <a href="mailto:mohr@informatik.hu-berlin.de">Christian Mohr</a>
 * Declaration of class LinePercept
 */

#ifndef _LinePercept_h_
#define _LinePercept_h_

#include <vector>

#include "Tools/Math/Vector2.h"
#include "Tools/Math/Line.h"
#include "Tools/CameraGeometry.h"
#include <Tools/DataStructures/Printable.h>
#include <Tools/DataStructures/Serializer.h>

#include "Representations/Infrastructure/FrameInfo.h"


class LinePercept : public naoth::Printable
{ 
public:

  enum LineType
  {
    unknown, //equals ColorClasses::none
    O, //line along field bounds, equals ColorClasses::orange
    I, //line inside of field, equals ColorClasses::yellow
    C, //circle line, equals ColorClasses::skyblue
    none //invalid line, equals ColorClasses::white
  };

  enum LineID
  {
    center_line,
    goal_line_own,
    goal_line_opponent,
    unknown_id
  };

  /**
   The class LineSegmentImage describes a segment of a line in the image coordinates.
  */
  class LineSegmentImage
  {
  public:
    LineSegmentImage()
      :
      thickness(0.0),
      angle(0.0),
      type(LinePercept::unknown),
      valid(false)
    {}

    /** the line detected in the image (pixel coordinates) */
    Math::LineSegment segment;

    // information in image
    double thickness;
    double angle;

    /** the type of the line estimated in image */
    LineType type;

    // what do we need it for?
    bool valid;
  };



  class FieldLineSegment
  {
    public:
      FieldLineSegment()
        :
        //valid(false),
        type(LinePercept::unknown),
        seen_id(unknown_id)
      {}

      // TODO: when exactly is a line segment valid?
      //bool valid; 

      // TODO: is the type filled properly?
      /** the type of the detected line */
      LineType type;

      /** estimated id of the line, e.g., center line, goal line etc. */
      LineID seen_id;

      /** the line detected in the image (pixel coordinates) */
      LineSegmentImage lineInImage;

      /** the projection of the line segment on the ground (robot coordinates) */
      Math::LineSegment lineOnField;
  };//end class FieldLineSegment


  /** 
    currently used only in 3DSim to represent the seen corners.
  */
  class Flag
  {
  public:
    Flag(const Vector2d& seenPosOnField, const Vector2d& absolutePosOnField)
      : 
      seenPosOnField(seenPosOnField),
      absolutePosOnField(absolutePosOnField)
    {}
    Vector2d seenPosOnField;
    Vector2d absolutePosOnField; // model of the flag (i.e. its known absolute osition on the field)
  };//end class Flag



  class Intersection
  {
  public:

    Intersection()
      : type(Math::Intersection::unknown)
    {
    }

    Intersection(const Vector2d& pos)
      :
      type(Math::Intersection::unknown),
      pos(pos)
    {
    }

    
    void setSegments(int segOne, int segTwo)
    {
      segmentIndices[0] = segOne;
      segmentIndices[1] = segTwo;
    }

    void setSegments(int segOne, int segTwo, double distOne, double distTwo)
    {
      segmentIndices[0] = segOne;
      segmentsDistanceToIntersection[0] = distOne;
      segmentIndices[1] = segTwo;
      segmentsDistanceToIntersection[1] = distTwo;
    }

    void setType(Math::Intersection::IntersectionType typeToSet){ type = typeToSet; }
    void setPosOnField(const Vector2d& p) { posOnField = p; }
    void setPosInImage(const Vector2<unsigned int>& p) { pos = p; }
    

    // getters
    Math::Intersection::IntersectionType getType() const { return type; }
    const Vector2<unsigned int>& getSegmentIndices() const { return segmentIndices; }
    const Vector2d& getSegmentsDistancesToIntersection() const { return segmentsDistanceToIntersection; }
    const Vector2d& getPos() const { return pos; }
    const Vector2d& getPosOnField() const { return posOnField; }

  private:
    Math::Intersection::IntersectionType type;
    Vector2<unsigned int> segmentIndices;
    Vector2d segmentsDistanceToIntersection;
    Vector2d pos;
    Vector2d posOnField;
  };

  // seen lines
  std::vector<FieldLineSegment> lines;
  std::vector<FieldLineSegment> short_lines;
  std::vector<FieldLineSegment> extended_lines;
  std::vector<int> edgelLineIDs;

  // seen corners
  std::vector<Intersection> intersections;
  // seen flags (only S3D)
  std::vector<Flag> flags;

  // middle circle was seen
  bool middleCircleWasSeen;
  Vector2d middleCircleCenter;
  bool middleCircleOrientationWasSeen;
  Vector2d middleCircleOrientation;


  // representationc for the closest line
  // TODO: this calculations can be made sowhere else
  double closestLineSeenLength;
  Vector2d closestPoint;
  Vector2d estOrthPointOfClosestLine;
  Vector2d closestPointOfClosestLine;

  // a line was seen
  // TODO: do we need it? (lines.empty() also does the job)
  bool lineWasSeen;

  // TODO: do we need it?
  naoth::FrameInfo frameInfoWhenLineWasSeen;


  LinePercept()
  :
    middleCircleWasSeen(false),
    middleCircleOrientationWasSeen(false),
    closestLineSeenLength(0.0),
    lineWasSeen(false)
  {
    reset();
  }

  ~LinePercept() {}
  
  /* reset percept */
  void reset()
  {
    lines.clear();
    short_lines.clear();
    extended_lines.clear();
    //lines.reserve(INITIAL_NUMBER_OF_LINES);

    intersections.clear();
    //intersections.reserve(INITIAL_NUMBER_OF_LINES);

    flags.clear();

    middleCircleWasSeen = false;
    middleCircleCenter.x = 0.0;
    middleCircleCenter.y = 0.0;
    
  }//end reset

  virtual void print(std::ostream& stream) const;

};

class LinePerceptTop : public LinePercept
{
public:
  virtual ~LinePerceptTop() {}
};

namespace naoth
{
  template<>
  class Serializer<LinePercept>
  {
  public:
    static void serialize(const LinePercept& representation, std::ostream& stream);
    static void deserialize(std::istream& stream, LinePercept& representation);
  };

  template<>
  class Serializer<LinePerceptTop> : public Serializer<LinePercept>
  {};

}

#endif // _LinePercept_h_


