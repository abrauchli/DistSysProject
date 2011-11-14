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
  #Building.addRoom(self.building,self.floor,self.room)
    room = Building.findRoom(building,floor,room)
    if (room != None):
      self.room = room
      self.building = Building.findBuilding(building)
       
  def __str__(self):
    return "{b} {f} {r}".format(b=self.building,f=self.floor,r=self.room)

def read():
  reader = csv.reader(open(data,'rb'))
  for row in reader:
    print row
    #  print ', '.join(rowi
    a = AccessPoint(row[1],row[5]) 
    accessPoints[row[5]] = a

def toJSON(mac):
  return accessPoints[mac]

def toJSON():
  r = []
  for a in accessPoints:
    r.append(a)
  return r
