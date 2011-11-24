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

class NotFoundException(Exception):
  def __init__(self,msg):
    print "Returning a {excp} with {msg}".format(
        excp= self.__class__.__name__,
        msg = msg)
    self.msg = msg
    
  def getError(self):
    return {"error": self.__class__.__name__,
      "msg": msg}

class BuildingNotFoundException(NotFoundException):
  pass
class FloorNotFoundException(NotFoundException):
  pass
class RoomNotFoundException(NotFoundException):
  pass
class MacAdressNotFoundException(NotFoundException):
  pass
class LocationNotFoundException(NotFoundException):
  pass
