# -*- coding: <utf-8> -*-
import codecs
import json
import csv
import re
import Model 
from ETHRoom import Room
from ETHFloor import Floor
from ETHBuilding import Building
#data = "data/wlan.csv"
data = ["data/wlan_honegg.csv", "data/wlan_zentrum.csv"]

wlanRegex = "air-(\w+)-([a-zA-Z]+)(\d+)i--\w"
wlanID = "air-(\w+)-([a-zA-Z]+)(\d+)-[a-zA-Z]+"
wlanIDAlt = "air-(\w+)-([a-zA-Z]+)(\d+)-(\d+)-[a-zA-Z]+"
wlanIDSpecial = "air-([a-zA-Z]+)-(\w+)-\w"
accessPoints = {}
class AccessPoint:
  def __init__(self,idstring,mac,bssid):
    self.mac = mac
    self.idstring = idstring
    self.bssid = bssid
    if (re.match(wlanID,idstring)):
      m = re.match(wlanID,idstring)
      building = m.group(1).upper()
      floor = m.group(2).upper()
      room = ""+m.group(3)
    else:
      m = re.match(wlanIDAlt,idstring)
      building = m.group(1).upper()
      floor = m.group(2).upper()
      room = ""+m.group(3)+"."+m.group(4)
   
    print idstring +" -> "+building+" "+floor+" "+room
    
    self.room = Model.findRoom(building,floor,room)
    if self.room is None:
      print "Adding Room"
      Model.addRoom(building,floor,room)
      self.room = Model.findRoom(building,floor,room)
#      print self.room.number 
  def objectInfo(self):
    return {"mac":self.mac, 
      "id": self.idstring,
      "location":self.room.getInfo(),
      "bssid": self.bssid}

  def getInfo(self):
    return {"mac":self.mac, 
      "id": self.idstring,
      "bssid": self.bssid
      }

  def __str__(self):

    return "{b} {f} {r}".format(b=self.room.building,f=self.room.floor,r=self.room)

def read():
  for f in data:
    reader = csv.reader(open(f,'rb'))
    for row in reader:
      mac = row[5]
      bssid = row[0]
      idstring = row[1]
      if not re.search(wlanID,idstring) and not re.search(wlanIDAlt,idstring):
        print "Ignoring row: ",row
      else:
        a = AccessPoint(idstring,mac,bssid) 
        accessPoints[row[5]] = a
    
def objectInfo(mac):
  return accessPoints[mac].objectInfo()

def getInfo():
  r = []
  for m,v in accessPoints.iteritems():
    r.append(v.getInfo())
  return r

def getRoom(mac):
  return accesspoints[mac]

def computeLocation(aps):
  strength = 0.0
  mac = ""

  apsResult = {}
  for k,v in aps.iteritems():
    if abs(float(v)) >= abs(float(strength)):
      mac = k
    ap = accessPoints[k]
    if ap != None:
      apsResult[k] = {
          "coords" : ap.room.location,
          "location" : ap.room.getDetailedInfo()
        }

  if mac == "":
    return None
  room = accessPoints[mac].room
  postype = "unknown"
  if type(room) == Room:
    postype = "room"
  elif type(room) == Floor:
    postype = "floor"

  return {
    "location" : { 
       "type" : postype,
       "result" : room.getDetailedInfo(),
       "coords" : room.location
      },
    "aps" : apsResult
   }


"""
  x = float(0)
  y = float(0)
  s = float(0)
  r = {}
  m = None
  mS = 0.0
  print aps
  for k,v in aps.iteritems():
      mac = k
      strength = v
      room = accessPoints[mac].room
      if room == None:
        r[mac] = {"error": "Not Found"}
      else: ## Code doesn't work
        center = room.getRelativePosition()
        r[mac] = room.getInfo()
        x += float(center[0])*strength
        y += float(center[1])*strength
        s += float(strength)
        if (abs(strength) > abs(mS)):
          mS = strength
          m = room
  if (s != 0.0):
    x = x/s
    y = y/s
    # r["position"] = {"x": x, "y": y}
  if (m != None):
    t = m.getInfo()
    t["x"] = m.getRelativePosition()[0]
    t["y"] = m.getRelativePosition()[1]
    r["position"] = t
    return r
  else:
    r["error"] = "Not data found"
    return r
"""

if __name__== "__main__":
  read()
