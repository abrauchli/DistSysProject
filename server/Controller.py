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
import ETHBuilding
import ETHFloor
import ETHRoom
import Model
import ETHReadRoomAllocation
import config
import AccessPoints

from exception import *

def parseJSONRequest(req):
  request = req["request"]
  if request == "location":
    aps = req["aps"]
    try:
      return AccessPoints.computeLocation(aps)
    except NotFoundException: 
      raise 
  if request == "freeroom":
    building = req["building"]
    floor = req.get("floor")
    stime = req.get("starttime")
    etime = req.get("endtime")
    try:
      return findFreeRoom(building,floor,stime,etime)
    except NotFoundException:
      raise
def isAllocateableRoom(room):
    if room.bookable != None:
        if room.bookable:
            return True
#if room.desc in config.ROOMTYPE_LEARNING:
#    return True
    return False

def findFreeRoom(building,floor=None,stime=None,etime=None):
  if stime == None:
    stime = 7.0
  if etime == None:
    etime = 22.0
  if floor == None:
    try:
      b = Model.findBuilding(building)
      r = b.getAllRooms()
    except:
      raise
  else:
    try:
      f = Model.findFloor(building,floor)
      r = f.getAllRooms()
    except:
      raise
  rooms = filter(lambda x: isAllocateableRoom(x),r)
  ret = []
  for room in rooms:
    if ETHReadRoomAllocation.isRoomFree(room,stime,etime):
      ret.append(room.getDetailedInfo())

  return ret

def get_campus_all():
    r = {}
    for campus, blgs in ETHBuilding.CAMPUS:
        r[campus] = blgs
    return r

