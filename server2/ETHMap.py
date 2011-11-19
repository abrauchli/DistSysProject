#!/usr/bin/env python
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

import urllib
import os
import ETHColorcoord

from PIL import Image
def checkIfImageIsValid(filename):
  print filename
  im = Image.open(filename, "r").convert("RGB")
  if im is None:
    raise "Image doesn't exist"
  pix = im.load() # get pixel 2d array
  if pix[0,0] == (241,229,193):
    return False
  return True

def test():
  errorfile = "static/cache/1058_D.gif"
  goodfile = "static/cache/ADM_A.gif"

  print "Error file: ",checkIfImageIsValid(errorfile)
  print "Good file: ",checkIfImageIsValid(goodfile)

if __name__ == "__main__":
  test()
