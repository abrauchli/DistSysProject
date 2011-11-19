#!/usr/bin/python
# -*- coding: utf-8 -*-
"""
  This file is part of SurvivalGuide
  Copyleft 2011 The SurvivalGuide Team
 
  This program is free software: you can redistribute it and/or modify
  it under the terms of the GNU General Public License as published by
  the Free Software Foundation, either version 3 of the License, or
  (at your option) any later version.
 
  This program is distributed in the hope that it will be useful,
  but WITHOUT ANY WARRANTY; without even the implied warranty of
  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  GNU General Public License for more details.
 
  You should have received a copy of the GNU General Public License
  along with this program.  If not, see <http://www.gnu.org/licenses/>.
"""

from ETHBuilding import Building 
from ETHFloor import Floor
from ETHRoom import Room
import codecs
import re
import csv

ETHBUILDINGS = 'data/ethgeb.txt'
ETHROOMS = 'data/ethuint.txt'

buildings = {}

def getBuildings():
  # Return hash of buildings
  r = {}
  for k,b in buildings.iteritems():
    r[k] = b.getInfo()

  return r

def getBuilding(building):
  # Return hash of building
  return None

def getFloor(building,floor):
  # Return hash of floor in building
  return None

def getRoom(building,floor,room):
  # Return hash of room on floor in building
  return None


## Find operations
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

## Add operations
def addBuilding(name,city,street):
  b = name.upper()
  if b not in buildings:
    buildings[b] = Building(name,city,street)

def addRoom(bldname,flname,rmname,desc=u"Büro"):
  ## Check if building is around
  #  found = False
  b = bldname.upper()
  f = flname.upper()
  r = rmname.upper()
  d = desc  
  #if b not in buildings:
  #  buildings[b] = Building(b)
  b = bldname.upper()
  buildings[b].addRoom(f,r,d)

## Fill operations
def fillBuildings():
  reader = csv.reader(open(ETHBUILDINGS, 'rb'), delimiter='|')
  for row in reader:
    addBuilding(row[0],row[1],row[2])

def fillRooms():
  reader = csv.reader(open(ETHROOMS, 'rb'), delimiter='|')
  for row in reader:
    b = row[0]
    art = row[2]
    ROOMMATCH = "(\w+)\s+(\S+)"
    if re.match(ROOMMATCH,row[1]):
      m = re.search("(\w+)\s+(\S+)",row[1]) 
      f = m.group(1)
      r = m.group(2)
      addRoom(b,f,r,art)

## Init
def init():
  fillBuildings()
  fillRooms()



if __name__== "__main__":
  init()

