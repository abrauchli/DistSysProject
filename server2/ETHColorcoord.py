#!/usr/bin/env python
# Copyleft 2011 Andreas Brauchli <a.brauchli@elementarea.net>
#
# This program is free software: you can redistribute it and/or modify
# it under the terms of the GNU General Public License as published by
# the Free Software Foundation, either version 3 of the License, or
# (at your option) any later version.
#
# This program is distributed in the hope that it will be useful,
# but WITHOUT ANY WARRANTY; without even the implied warranty of
# MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
# GNU General Public License for more details.
#
# You should have received a copy of the GNU General Public License
# along with this program.  If not, see <http://www.gnu.org/licenses/>.
from PIL import Image
import sys

def getCenter(filename):
  print "Computing boundaries on: ",filename
  im = Image.open(filename, "r").convert("RGB")
  pix = im.load() # get pixel 2d array
  for i in range(im.size[0]):
    for j in range(im.size[1]):
      if pix[i, j] != (0, 255, 255):
        pix[i, j] = (0, 0, 0)
#im.show() # show filtered layer
  bbox = im.getbbox() # bounding box (x1, y1, x2, y2)
    
  # (x, y, w, h) # bounding box
  # res = (bbox[0], bbox[1], bbox[2] - bbox[0], bbox[3] - bbox[2])
  
  # (x, y) # center only
  res = (bbox[0] + ((bbox[2] - bbox[0]) / 2), bbox[1] + ((bbox[3] - bbox[1]) / 2))
  assert res[0] < im.size[0] and res[1] < im.size[1]
  print res
  return res

def main(argv=None):
  if argv is None:
    argv = sys.argv
  getCenter(argv[1])
if __name__ == "__main__":
	main()
