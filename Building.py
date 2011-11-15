#-*- coding: utf-8 -*-
import json
import re
buildings = {}
CACHED_URL_PREFIX = "/cache/"
CACHED_IMAGE_TYPE = "gif"

class Cacheable(object):
  def __init__(self):
    self.cached = False
  def getCachedURL(self):
    return CACHED_URL_PREFIX+self.getFilename()+"."+CACHED_IMAGE_TYPE 
  def getURL(self):
    if self.cached:
      return self.getCachedURL()
    else:
      return self.getNonCachedURL()
  def getFileprefix(self):
    return "If you see this, something is borken"
  def getFilename(self): 
    return self.getFileprefix()+"."+CACHED_IMAGE_TYPE

class Room(Cacheable):
  def __init__(self,number,building,floor,t=u"Büro"):
    self.floor = floor
    self.building = building
    self.roomtype = t
    self.number = number
    self.cached = False
    if type(building) != Building:
      print building.name+" "+building.strasse
      print "Building has wrong type"
      raise
#raise
    if type(floor) != Floor:
      print floor
      print "Floor has wrong type: ", type(floor)
      raise
#    raise
  def getFileprefix(self):
    return "{prefix}_{suffix}".format(prefix=self.floor.getFilename(),suffix=self.number)

  def getNonCachedURL(self):
    return "http://www.rauminfo.ethz.ch/Rauminfo/grundrissplan.gif?gebaeude={building}&geschoss={floor}&raumNr={room}".format(building=self.building.name
            ,floor=self.floor.floor
            ,room=self.number)
  
  def __str__(self):
    return self.number+" "+self.roomtype
 
  # Object information type
  def objectInfo(self):
    return {'type': self.roomtype, 
      'number': self.number,
      'map' : self.getURL()
    }
  # Recursive information
  def getInfo(self):
    r = self.objectInfo()
    r["floor"]=self.floor.floor
    r["building"]=self.building.name
    return r

class Floor(Cacheable):
  def __init__(self,floor,building):
    self.building = building
    self.floor = floor
    self.rooms = {}

    if type(building) != Building:
      print building.name+" "+building.strasse

      print "Building has wrong type"
      raise
  def addRoom(self,number,roomtype):
    if number not in self.rooms:
      self.rooms[number] = Room(number,self.building,self,roomtype)

  def getFileprefix(self):
    return "{prefix}_{suffix}".format(prefix=self.building.getFilename()
        ,suffix=self.floor.floor)

  def getNonCachedURL(self):
    return  "http://www.rauminfo.ethz.ch/Rauminfo/grundrissplan.gif?gebaeude={building}&geschoss={floor}".format(building=self.building.name
            ,floor=self.floor)

  def getURL(self):
    return self.getNonCachedURL()

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
  
  # Recursive information
  def getInfo(self):
    r = self.objectInfo()
    r["building"] = self.building.name
    return r

  # Object information
  def objectInfo(self):
    rooms = []
    for k,r in self.rooms.iteritems():
      rooms.append(r.objectInfo())
    return {'floor': self.floor,
      'rooms': rooms,
      'map' : self.getURL()}

class Building(object):  
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
  
  def getFilename(self):
    return self.name

  # Recursive info
  def getInfo(self):
    r = []
    for k,f in self.floors.iteritems():
      r.append(f.objectInfo())
    k = self.objectInfo()
    k["floors"]=r
    return k

  # Object information
  def objectInfo(self):
    return  {'name': self.name,
      'street': self.strasse,
      'city': self.stadt }


## Direct access
def getInfo():
  r = []
  for k,v in buildings.iteritems():
    r.append(v.getInfo())
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
