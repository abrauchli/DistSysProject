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

import ETHFloor 
import ETHRoom 
from exception import *

CAMPUS = [(u"Zentrum", 
            ['IFW','RZ','NO','NW','ML','CLA','CHN','CAB','CNB','LFW',
             'ETF','ETZ','ETA','ETL', 'HG','MM','GEP']),
         (u"Höngg", 
            ['HEZ','HDB','HKK','HIT','HIF','HIL','HIK','HIR','HIQ','HIP',
               'HPF','HPM','HPK','HPL','HPW','HPT','HPI','HPZ','HPR','HPH',
               'HPV','HPP','HPS','HXE','HXD','HCI','HXA','HXC'])]
OTHER_CAMPUS_NAME = u"Other"
class Building(object):  
  def __init__(self,name,city=u"Zürich",street=""):
    if street =="noname":
      street = u""
    if city == "noname":
      city = u"Zürich"
    
    self.name = name
    self.street = street
    self.city = city
    self.floors = {}
    found_campus = False
    for campus, bldgs in CAMPUS:
        if name.upper() in bldgs:
            self.campus = campus
            found_campus = True
            break

    if found_campus == False:
        self.campus = OTHER_CAMPUS_NAME
"""
    if name.upper() in ZENTRUM:
      self.campus=u"Zentrum"
    elif name.upper() in HOENGG:
      self.campus=u"Höngg"
    else:
      self.campus=u"Other"
"""
  def __str__(self):
    return self.name
  
  def findRoom(self,floor,room):
    if floor in self.floors:
      return self.floors[floor].findRoom(room)
    raise RoomNotFoundException("Couldn't find room {room} on {floor} in {building}".format(
          room = room,
          floor = floor,
          building = self.name))

  def findFloor(self,floor):
    if floor in self.floors:
      return self.floors[floor]
    print "Couldn't find floor:" + floor
    raise FloorNotFoundException("Couldn't find floor {floor} in {building}".format(
        floor = floor,
        building = self.name))

  def addFloor(self,floor):
    floor.upper()
    if floor not in self.floors:
      self.floors[floor] = ETHFloor.Floor(floor,self)
  
  def addRoom(self,floor,room,desc):
    self.addFloor(floor)
    self.floors[floor].addRoom(room,desc)
  
  def floor(self,floor):
    return self.floors[floor]
  
  def getFilename(self):
    return self.name


  def getAddress(self):
    return { 
            "city"    : self.city,
            "street" : self.street,
            "campus": self.campus
           }

  def getFloors(self):
    r = {}
    for k,f in self.floors.iteritems():
      r[k] = f.getInfo()

    return r

  def getInfo(self):
    return {
            "name" : self.name,
            "address" : self.getAddress(),
            "campus": self.campus
           }

  def getDetailedInfo(self):
    return {"name" : self.name,
      "address" : self.getAddress(),
      "floors" : self.getFloors()
     }

  def getAllRooms(self):
    r = []
    for k,v in self.floors.iteritems():
      r.extend(v.getAllRooms())
    return r
