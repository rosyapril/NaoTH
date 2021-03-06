from struct import Struct
from .Math import Vector2, Vector3, Pose2D
from naoth import Representations_pb2


class MixedTeamMessage(Struct):
    def __init__(self):
        super(MixedTeamMessage, self).__init__('Q4B')
        self.timestamp = 0
        self.teamID = 0
        self.isPenalized = False
        self.whistleDetected = False
        self.dummy = 42

    def pack(self):
        return Struct.pack(self,
                           self.timestamp,
                           self.teamID,
                           self.isPenalized,
                           self.whistleDetected,
                           self.dummy
                          )

class SPLMessage(Struct):
    """Representation of the standard SPLMessage."""

    SPL_STANDARD_MESSAGE_STRUCT_HEADER = b'SPL '
    SPL_STANDARD_MESSAGE_STRUCT_VERSION = 6
    SPL_STANDARD_MESSAGE_DATA_SIZE = 780
    SPL_STANDARD_MESSAGE_MAX_NUM_OF_PLAYERS = 5

    def __init__(self, teamnumber=0, playernumber=0):
        super(SPLMessage, self).__init__('4s3b?12f6B2h2bh')
        self.playerNumber = playernumber
        self.teamNumber = teamnumber
        self.fallen = False
        self.pose = Pose2D(0.0, 0.0, 0.0)  # x, y, r | +/-4500, +/-3000
        self.walkingTo = Vector2(0.0, 0.0)
        self.shootingTo = Vector2(0.0, 0.0)
        self.ballAge = -1
        self.ballPosition = Vector2(0.0, 0.0)
        self.ballVelocity = Vector2(0.0, 0.0)
        self.suggestion = [0 for x in range(self.SPL_STANDARD_MESSAGE_MAX_NUM_OF_PLAYERS)]
        self.intention = 0
        self.averageWalkSpeed = 200  # see TeamCommSender
        self.maxKickDistance = 3000  # see TeamCommSender
        self.currentPositionConfidence = 100
        self.currentSideConfidence = 100

        self._mixed = MixedTeamMessage()

        self.data = Representations_pb2.BUUserTeamMessage()

        # set known default values of custom message part
        for field in self.data.DESCRIPTOR.fields:
            if field.has_default_value:
                setattr(self.data, field.name, field.default_value)

        self.numOfDataBytes = self.data.ByteSize() + self._mixed.size

    def pack(self):
        return Struct.pack(self,
                           self.SPL_STANDARD_MESSAGE_STRUCT_HEADER,
                           self.SPL_STANDARD_MESSAGE_STRUCT_VERSION,
                           self.playerNumber,
                           self.teamNumber,
                           self.fallen,
                           *self.pose.__dict__.values(),
                           *self.walkingTo.__dict__.values(),
                           *self.shootingTo.__dict__.values(),
                           self.ballAge,
                           *self.ballPosition.__dict__.values(),
                           *self.ballVelocity.__dict__.values(),
                           *self.suggestion,
                           self.intention,
                           self.averageWalkSpeed,
                           self.maxKickDistance,
                           self.currentPositionConfidence,
                           self.currentSideConfidence,
                           (self.data.ByteSize() + self._mixed.size),
                           ) + self._mixed.pack()\
                             + self.data.SerializeToString()

    def __repr__(self):
        """Returns all 'active' message fields as string."""
        result = ""
        for attr in self.__dict__:
            if attr == "data":
                fields = self.__dict__[attr].DESCRIPTOR.fields_by_name
                for custom_attr in fields:
                    # do not show deprecated custom fields!
                    if fields[custom_attr].GetOptions().deprecated:
                        continue
                    result += "\t" + custom_attr + " = " + str(getattr(self.__dict__[attr], custom_attr)) + "\n"
            elif attr.startswith("_"):
                # skip attributes starting with '_' (private)
                continue
            else:
                result += "\t" + attr + " = " + str(self.__dict__[attr]) + "\n"
        return result
