#-*- coding: utf-8 -*-
import json
import re
buildings = {}
class Room:
  def __init__(self,number,building,floor,t=u"Büro"):
    self.floor = floor
    self.building = building
    self.roomtype = t
    self.number = number
    if type(building) is not Building:
      print "Building has wrong type"
#raise
    if type(floor) is not Floor:
      print "Floor has wrong type"
#    raise
  def __str__(self):
    return self.number+" "+self.roomtype
 
  def getAllInfo(self):
    return {'type': self.roomtype, 'number': self.number}
  def getInfo(self):
    return {'building': self.building.getInfo(),
      'floor' : self.floor.floor,
      'room' :  self.number,
      'type'  : self.roomtype}

class Floor:
  def __init__(self,floor,building):
    self.building = building
    self.floor = floor
    self.rooms = {}

  def addRoom(self,number,roomtype):
    if number not in self.rooms:
      self.rooms[number] = Room(number,self.building,self.floor,roomtype)

  def findRoom(self,room):
    if room in self.rooms:
      return self.rooms[room]
    print "Couldn't find room: "+room
    return None

  def room(self,room):
    if room in self.rooms:
      return self.rooms[room]
    return None
 
  def __str__(self):
    return self.floor
  
  def getAllInfo(self):
    rooms = []
    for k,r in self.rooms.iteritems():
      rooms.append(r.getAllInfo())
    return {'floor': self.floor, 'rooms': rooms}

class Building:  
  def __init__(self,name,strasse="",stadt=u"Zürich"):
    if strasse =="noname":
      strasse = u""
    if stadt == "noname":
      stadt = u"Zürich"
#    print "{c} {s}".format(c=stadt,s=strasse)
    self.name = name
    self.strasse = strasse
    self.stadt = stadt
    self.floors = {}

  def __str__(self):
    return self.name
  
  def findRoom(self,floor,room):
    if floor in self.floors:
      return self.floors[floor].findRoom(room)
    return None

  def findFloor(self,floor):
    if floor in self.floors:
      return self.floors[floor]
    print "Couldn't find floor:" + floor
    return None

  def addFloor(self,floor):
    floor.upper()
    if floor not in self.floors:
      self.floors[floor] = Floor(floor,self)
  
  def addRoom(self,floor,room,roomtype):
    self.addFloor(floor)
    self.floors[floor].addRoom(room,roomtype)
  
  def floor(self,floor):
    return self.floors[floor]

  def json(self):
    return self.__dict__
  
  def getAllInfo(self):
    r = []
    for k,f in self.floors.iteritems():
      r.append(f.getAllInfo())
    return {'name': self.name,
      'street': self.strasse,
      'city': self.stadt,
      'floors': r}

  def getInfo(self):
    return  {'name': self.name,
      'street': self.strasse,
      'city': self.stadt }
def getAllInfo():
  r = []
  for k,v in buildings.iteritems():
#    print v
    r.append(v.getAllInfo())

  return r

def findRoom(bldname,flname,rmname):
  bldname.upper()
  flname.upper()
  rmname.upper()
  if bldname in buildings:
    b = buildings[bldname]
    return b.findRoom(flname,rmname)

def findBuilding(bldname):
  if bldname in buildings:
    return buildings[bldname]

def findFloor(bldname,floor):
  if bldname in buildings:
    return buildings[bldname].findFloor(floor)
  print "Couldn't find building: "+bldname
  return None

def addBuilding(bldname,stadt,strasse):
  b = bldname.upper()
  if b not in buildings:
    buildings[b] = Building(bldname,strasse,stadt)

def addRoom(bldname,flname,rmname,roomtype=u"Büro"):
  ## Check if building is around
  #  found = False
  b = bldname.upper()
  f = flname.upper()
  r = rmname.upper()
  t = roomtype  
  #if b not in buildings:
  #  buildings[b] = Building(b)
  b = bldname.upper()
  buildings[b].addRoom(f,r,t)
