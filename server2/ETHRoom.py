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
import ETHFloor

class Room(Cacheable):
  def __init__(self,number,building,floor,t=u"Büro"):
    self.floor = floor
    self.building = building
    self.roomtype = t
    self.number = number
    self.cached = False
    self.center = None
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
    return "{prefix}_{suffix}".format(prefix=self.floor.getFileprefix(),suffix=self.number)

  def getNonCachedURL(self):
    return "http://www.rauminfo.ethz.ch/Rauminfo/grundrissplan.gif?gebaeude={building}&geschoss={floor}&raumNr={room}".format(building=self.building.name
            ,floor=self.floor.floor
            ,room=self.number)
  
  def __str__(self):
    return self.number+" "+self.roomtype
 
  def getRelativePosition(self):
    if self.center == None:
      Cache.getCenter(self)
    return self.center


