# -*- coding: <utf-8> -*-
import codecs
import re
import csv
import Building
import AccessPoints
import People

ETHBUILDINGS = 'data/ethgeb.txt'
ETHROOMS = 'data/ethuint.txt'
def fillBuildings():
  reader = csv.reader(open(ETHBUILDINGS, 'rb'), delimiter='|')
  for row in reader:
    print row
    Building.addBuilding(row[0],row[1],row[2])

def fillRooms():
  reader = csv.reader(open(ETHROOMS, 'rb'), delimiter='|')
  for row in reader:
    print row
    b = row[0]
    art = row[2]
    ROOMMATCH = "(\w+)\s+(\S+)"
    if re.match(ROOMMATCH,row[1]):
      m = re.search("(\w+)\s+(\S+)",row[1]) 
      f = m.group(1)
      r = m.group(2)
      Building.addRoom(b,f,r,art)
def readETHData():
  fillBuildings()
  fillRooms()

if __name__== "__main__":
  readETHData()  

