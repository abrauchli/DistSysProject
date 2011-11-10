# -*- coding: utf-8 -*-
import json
import re
buildings = {}
class Room:
  def __init__(self,building,floor,number,t="Büro"):
    self.roomtype = t
    self.building = building
    self.floor = floor
    self.number = number
  def __str__(self):
    return str(self.floor)+" "+self.number+" "+self.roomtype

  def json(self):
    return str(self.number)

class Floor:
  def __init__(self,building,floor):
    self.floor = floor
    self.building = building
    self.rooms = {}

  def addRoom(self,number,roomtype):
    if number not in self.rooms:
      self.rooms[number] = Room(self.building,
          self,number,roomtype)
  
  def room(self,room):
    return self.rooms[room]
 
  def __str__(self):
    return str(self.building)+" "+self.floor
  
  def json(self):
    return dict(rooms=self.rooms)

class Building:
  
  def __init__(self,name,strasse="",stadt="Zürich"):
    self.name = name
    self.strasse = strasse
    self.stadt = stadt
    self.floors = {}

  def __str__(self):
    return self.name
  
  def addFloor(self,floor):
    floor.upper()
    if floor not in self.floors:
      self.floors[floor] = Floor(self,floor)
  
  def addRoom(self,floor,room,roomtype):
    self.addFloor(floor)
    self.floors[floor].addRoom(room,roomtype)
  
  def floor(self,floor):
    return self.floors[floor]

  def json(self):
    return self.__dict__

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
  t = roomtype.upper()
  
  #if b not in buildings:
  #  buildings[b] = Building(b)
  b = bldname.upper()
  buildings[b].addRoom(f,r,t)
