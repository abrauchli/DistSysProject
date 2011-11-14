# -*- coding: <utf-8> -*-
import codecs
import json
import csv
import re
import Building 
#data = "data/wlan.csv"
data = ["data/wlan_honegg.csv", "data/wlan_zentrum.csv"]

wlanRegex = "air-(\w+)-(\w+)(\d+)i--\w"
wlanID = "air-(\w+)-(\w+)(\d+)-\w"
wlanIDAlt = "air-(\w+)-(\w+)(\d+)-(\d+)-\w"
wlanIDSpecial = "air-(\w+)-(\w+)-\w"
accessPoints = {}
class AccessPoint:
  def __init__(self,idstring,mac,bssid):
    self.mac = mac
    self.idstring = idstring
    self.bssid = bssid
    if (re.search(wlanID,idstring)):
      m = re.match(wlanID,idstring)
      building = m.group(1).upper()
      floor = m.group(2).upper()
      room = ""+m.group(3)
    else:
      m = re.match(wlanIDAlt,idstring)
      building = m.group(1).upper()
      floor = m.group(2).upper()
      room = ""+m.group(3)+"."+m.group(4)
    
#    print building + " " + floor + " " + room
    self.room = Building.findRoom(building,floor,room)
    if self.room is None:
      print "Adding Room"
      Building.addRoom(building,floor,room)
      self.room = Building.findRoom(building,floor,room)
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
