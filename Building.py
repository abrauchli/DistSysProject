# -*- coding: utf-8 -*-
import json
import re
buildings = {}
class Room:
  def __init__(self,number,t=u"Büro"):
    self.roomtype = t
    self.number = number

  def __str__(self):
    return self.number+" "+self.roomtype

  def toJSON(self):
    return {'type': self.roomtype, 'number': self.number}
    
class Floor:
  def __init__(self,floor):
    self.floor = floor
    self.rooms = {}

  def addRoom(self,number,roomtype):
    if number not in self.rooms:
      self.rooms[number] = Room(number,roomtype)

  def findRoom(self,room):
    if room in self.rooms:
      return self.rooms[room]
    return None

  def room(self,room):
    if room in self.rooms:
      return self.rooms[room]
    return None
 
  def __str__(self):
    return self.floor
  
  def toJSON(self):
    rooms = []
    for k,r in self.rooms.iteritems():
      rooms.append(r.toJSON())
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
    return None

  def addFloor(self,floor):
    floor.upper()
    if floor not in self.floors:
      self.floors[floor] = Floor(floor)
  
  def addRoom(self,floor,room,roomtype):
    self.addFloor(floor)
    self.floors[floor].addRoom(room,roomtype)
  
  def floor(self,floor):
    return self.floors[floor]

  def json(self):
    return self.__dict__
  
  def toJSON(self):
    r = []
    for k,f in self.floors.iteritems():
      r.append(f.toJSON())
    return {'name': self.name,
      'street': self.strasse,
      'city': self.stadt,
      'floors': r}

def toJSON():
  r = []
  for k,v in buildings.iteritems():
    print v
    r.append(v.toJSON())

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
  return None

def addBuilding(bldname,stadt,strasse):
  b = bldname.upper()
  if b not in buildings:
    buildings[b] = Building(bldname,strasse,stadt)

def addRoom(bldname,flname,rmname,roomtype):
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
