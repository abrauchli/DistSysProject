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

def parseJSONRequest(req):
  request = req["request"]
  if request == "location":
    aps = req["aps"]
    return AccessPoints.computeLocation(aps)
  if request == "freerroom":
    building = req["building"]
    floor = req["floor"]
    return findFreeRoom(building,floor)
def isAllocateableRoom(room):
  if room.desc in config.ROOMTYPE_LEARNING:
    return True
  return False

def findFreeRoom(building,floor=None,stime=7.0,etime=8.0):
  if floor = None:
    building = Model.findBuilding(building)
    rooms = building.getAllRooms()
  else:
    floor = Model.findFloor(building,floor)
    rooms = floor.getAllRooms()
  rooms = filter(lambda x: isAllocateableRoom(r),rooms)
  r = []
  for room in rooms:
    if ETReadRoomAllocation.isRoomFree(room,stime,etime):
      r.append(room.getDetailedInfo())

  return r

