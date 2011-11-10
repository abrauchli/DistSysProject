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

class WLANAccessPoints:
  def __init__(self):
    reader = csv.reader(open(data,'rb'))
    self.accessPoints = []
    for row in reader:
      print row
      #  print ', '.join(rowi
      a = AccessPoint(row[1],row[5]) 
      self.accessPoints.append(a)

  def __str__(self):
    r = ""
    for a in self.accessPoints:
      r = r+a.__str__()+"\n"
    return r
if __name__== "__main__":
  c = WLANAccessPoints()
  print c
  
