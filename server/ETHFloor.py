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

import Cacheable
import ETHBuilding
import ETHRoom
import ETHMap

class Floor(Cacheable.Cacheable):
  def __init__(self,floor,building):
    self.building = building
    self.floor = floor
    self.rooms = {}
#    self.mapAvailable = False
    self.cached = False
    if type(building) != ETHBuilding.Building:
      print building.name+" "+building.strasse

      print "Building has wrong type"
      raise
    self.downloadMap()
    self.location = None
  def addRoom(self,number,desc):
    if number not in self.rooms:
      self.rooms[number] = ETHRoom.Room(number,self.building,self,desc)

  def getFileprefix(self):
    return "{prefix}_{suffix}".format(prefix=self.building.getFilename()
        ,suffix=self.floor)

  def getNonCachedURL(self):
    return  "http://www.rauminfo.ethz.ch/Rauminfo/grundrissplan.gif?gebaeude={building}&geschoss={floor}".format(building=self.building.name
            ,floor=self.floor)

  def findRoom(self,room):
    if room in self.rooms:
      return self.rooms[room]
    print "Couldn't find room: "+room
    return RoomNotFoundException("Couldn't find room {room} on {floor} in {building}".format(
          room = room,
          floor = self.floor,
          building = self.building.name)

  def room(self,room):
    if room in self.rooms:
      return self.rooms[room]
    return None
 
  def __str__(self):
    return self.floor

  def getRooms(self):
    r = {}
    for k,v in self.rooms.iteritems():
      r[k] = v.getInfo() 
    return r
    
  def getInfo(self):
    return {
      "map" : self.getURL(),
      "mapAvailable" : self.mapAvailable,
    }
  def getDetailedInfo(self):
     return {
      "building" : self.building.getInfo(),
      "map"      : self.getURL(),
      "mapAvailable" : self.mapAvailable,
      "rooms"    : self.getRooms()

      }

  def getAllRooms(self):
    r = []
    for k,v in self.rooms.iteritems():
      r.append(v)

    return r
