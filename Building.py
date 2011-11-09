import json
import re
buildings = {}
class Room:
  def __init__(self,building,floor,number):
    self.building = building
    self.floor = floor
    serl.number = number
  def __str__(self):
    return str(floor)+" "+number
class Floor:
  def __init__(self,building,floor):
    self.floor = floor
    self.building = building
    self.rooms = {}

  def addRoom(self,number):
    if (self.rooms[number] == null):
      self.rooms[number] = Room(self.building,
          self,number)
  
  def room(self,room):
    return self.rooms[room]
 
  def __str__(self):
    return str(self.building)+" "+floor

class Building:
  
  def __init__(self,name):
    self.name = name
    self.floors = {}

  def __str__(self):
    return name
  
  def addFloor(self,floor):
    floor.upper()
    if (self.floors[floor] == null):
      self.floors[floor] = Floor(self,floor)
  
  def addRoom(self,floor,room):
    self.addFloor(floor)
    self.floors[floor].addRoom(room)
  
  def floor(self,floor):
    return self.floors[floor]
  
def addRoom(bldname.flname,rmname):
  ## Check if building is around
  found = False
  b = bldname.upper()
  f = flname.upper()
  r = rmname.upper()
  if (buildings[b] == null)
    buildings[b] = Building(b)

  buildings[b].addRoom(f,r)
  


