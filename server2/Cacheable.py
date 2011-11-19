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

import config
## Defaults for distsysproject
#CACHED_URL_PREFIX = "/static/cache/"
#CACHE = "static/cache/"
#CACHED_IMAGE_TYPE = "gif"
LOCAL_CACHE_URL = config.LOCAL_CACHE_URL
LOCAL_CACHE_DIR = config.LOCAL_CACHE_DIR
SERVER_URL = config.SERVER_URL
CACHED_IMAGE_TYPE = config.CACHED_IMAGE_TYPE

class Cacheable(object):
  def __init__(self):
    self.cached = False
  def getCachedURL(self):
    return SERVER_URL+LOCAL_CACHE_URL+self.getFilename() 
  def getURL(self):
    if self.cached:
      return self.getCachedURL()
    else:
      return self.getNonCachedURL()
  def getFileprefix(self):
    raise "You need to inherit this class"
  def getFilename(self): 
    return self.getFileprefix()+"."+CACHED_IMAGE_TYPE
