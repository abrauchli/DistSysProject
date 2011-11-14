# -*- coding: <utf-8> -*-
import codecs
import json
import csv
import re
import Building 
data = "data/wlan.csv"

wlanRegex = "air-(\w+)-(\w)(\d+)i--\w"
wlanID = "air-(\w+)-(\w)(\d+)-\w"
wlanIDAlt = "air-(\w+)-(\w)(\d+)-(\d)-\w"

accessPoints = {}
class AccessPoint:
  def __init__(self,idstring,mac):
    self.mac = mac
    self.idstring = idstring    
    if (re.match(wlanID,idstring)):
      m = re.search(wlanID,idstring)
      building = m.group(1).upper()
      floor = m.group(2).upper()
      room = ""+m.group(3)
    else:
      m = re.search(wlanIDAlt,idstring)
      building = m.group(1).upper()
      floor = m.group(2).upper()
      room = ""+m.group(3)+"."+m.group(4)
    
    print building + " " + floor + " " + room
    self.room = Building.findRoom(building,floor,room)
    if self.room is None:
      Building.addRoom(building,floor,room)
      self.room = Building.findRoom(building,floor,room)
    
  def objectInfo(self):
    return {"mac":self.mac, 
      "id": self.idstring,
      "location":self.room.getInfo() }

  def getInfo(self):
    return {"mac":self.mac, 
      "bssid": self.bssid,
      }

  def __str__(self):

    return "{b} {f} {r}".format(b=self.room.building,f=self.room.floor,r=self.room)

def read():
  reader = csv.reader(open(data,'rb'))
  for row in reader:
    a = AccessPoint(row[1],row[5]) 
    accessPoints[row[5]] = a

def objectInfo(mac):
  return accessPoints[mac].objectInfo()

def getInfo():
  r = []
  for m,v in accessPoints.iteritems():
    r.append(v.getInfo())
  return r
