# -*- coding: utf-8 -*-
### Config file for the distsysproject

# Server DNS Name


SERVER_NAME = "eth.rsp.li"
SERVER_URL = "http://{SERVER_NAME}".format(SERVER_NAME=SERVER_NAME)
CACHE_URL= SERVER_URL+"/static/cache/"

MONGODB_DATABASE = "DistSysProjekt"
# Defaults for the cache
LOCAL_CACHE_DIR = "static/cache/"
# You might want to change this if you want to change from gif to png or so..
CACHED_IMAGE_TYPE = "gif"

ROOMTYPE_LEARNING = [u"Seminare / Kurse", u"Computerraum", u"Hörsaal"]

##################### Runtime variables
from pymongo import Connection

mongodbConnection = Connection()
mongodb = mongodbConnection[MONGODB_DATABASE]
mongodbMapCACHE = mongodb.mapcache
mongodbMapPositionCACHE = mongodb.mappositioncache

