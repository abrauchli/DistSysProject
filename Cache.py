# -*- coding: <utf-8> -*-
import urllib
import Building
import os

def fileAlreadyCached(filename):
  files = os.listdir(Building.CACHE)
  if filename in files:
    return True
  return False
def cache(obj):
  url = ""
#  if type(obj) != Cacheable:
#    print "Object has wrong type"
#    raise
  if obj.cached:
    return
  filename = obj.getFilename()
  if fileAlreadyCached(filename):
    obj.cached = True
    print filename+" was already cached."
    return

  dest = Building.CACHE+filename
  url = obj.getNonCachedURL()
  print "Downloading: {url}\nto: {dest}".format(url=url,dest=dest)
  urllib.urlretrieve(url,dest)
  obj.cached = True

