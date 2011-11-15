#!/usr/bin/env python
from PIL import Image
import sys

SCAN_STEP_SIZE = 10
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
