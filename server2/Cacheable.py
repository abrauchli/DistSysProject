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
import os
import urllib
import config
import ETHMap
## Defaults for distsysproject
#CACHED_URL_PREFIX = "/static/cache/"
#CACHE = "static/cache/"
#CACHED_IMAGE_TYPE = "gif"
LOCAL_CACHE_DIR = config.LOCAL_CACHE_DIR
CACHED_IMAGE_TYPE = config.CACHED_IMAGE_TYPE
CACHE_URL = config.CACHE_URL

def fileCached(filename):
  files = os.listdir(LOCAL_CACHE_DIR)
  if filename in files:
    return True
  return False

class Cacheable(object):
  def __init__(self):
    self.cached = False
  def getCachedURL(self):
    return CACHE_URL+self.getFilename() 
  def getURL(self):
    if self.cached:
      return self.getCachedURL()
    else:
      return self.getNonCachedURL()

  def getFileprefix(self):
    raise "You need to inherit this class"

  def getFilename(self): 
    return self.getFileprefix()+"."+CACHED_IMAGE_TYPE

  def mapValid(self):
    self.mapAvailable = ETHMap.checkIfImageIsValid(self.getFilename())


  def downloadMap(self):
    if self.cached:
      self.mapValid()
      return
    filename = self.getFilename()

    if fileCached(filename):
      self.cached = True
      #print filename+" was already cached."
      return
    dest = LOCAL_CACHE_DIR+filename
    url = self.getNonCachedURL()
    print "Downloading: {url}\nto: {dest}".format(url=url,dest=dest)
    urllib.urlretrieve(url,dest)
    self.cached = True
    self.mapValid()
