#!/usr/bin/python
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


buildings = {}

def init():
  # Initialize Buildings

def getBuildings():
  # Return hash of buildings
  r = {}
  for k,b in buildings.iteritems():
    r[k] = b.getInfo()

  return r

def getBuilding(building):
  # Return hash of building

def getFloor(building,floor):
  # Return hash of floor in building

def getRoom(building,floor,room):
  # Return hash of room on floor in building
