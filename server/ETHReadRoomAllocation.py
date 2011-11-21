#!/usr/bin/env python
# -*- coding: utf-8 -*-
# Copyleft 2011 Pascal Spörri <pascal.spoerri@gmail.com>
#
# This program is free software: you can redistribute it and/or modify
# it under the terms of the GNU General Public License as published by
# the Free Software Foundation, either version 3 of the License, or
# (at your option) any later version.
#
# This program is distributed in the hope that it will be useful,
# but WITHOUT ANY WARRANTY; without even the implied warranty of
# MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
# GNU General Public License for more details.
#
# You should have received a copy of the GNU General Public License
# along with this program.  If not, see <http://www.gnu.org/licenses/>.
from BeautifulSoup import BeautifulSoup
import urllib
import re
import time
import datetime
from ETHRoom import Room
URL = "http://www.rauminfo.ethz.ch/Rauminfo/Rauminfo.do?region=Z&areal=Z&gebaeude=CHN&geschoss=D&raumNr=44&rektoratInListe=true&raumInRaumgruppe=true&tag=20&monat=Nov&jahr=2011&checkUsage=anzeigen"

DT = 0.25
HEADER_REGEX = "" 

FREE_ROOM_COLOR = '#99cc99'
USED_ROOM_COLOR = '#006799'
CLOSED_ROOM_COLOR = '#cccccc'
SUNDAY_ROOM_COLOR = '#eeeeee'


ROOM_CLOSED = 2
ROOM_USED = 1
ROOM_OPEN = 0


availableRooms = []
"""
  </td> <td>  <select name="monat"><option value="Jan">Jan</option>
  <option value="Feb">Feb</option>
  <option value="Mär">Mär</option>
  <option value="Apr">Apr</option>
  <option value="Mai">Mai</option>

  <option value="Jun">Jun</option>
  <option value="Jul">Jul</option>
  <option value="Aug">Aug</option>
  <option value="Sep">Sep</option>
  <option value="Okt">Okt</option>
  <option value="Nov" selected="selected">Nov</option>
  <option value="Dez">Dez</option></select>
  """

monthMap = {
    1: "Jan",
    2: "Feb",
    3: "Mär",
    4: "Apr",
    5: "Mai",
    6: "Jun",
    7: "Jul",
    8: "Aug",
    9: "Sep",
    10:"Okt",
    11:"Nov",
    12:"Dez"}

def findRowspan(td):
  attrs = td.attrs
  for a in attrs:
    key,value = a
    if key == "rowspan":
      return int(value)
def findColor(td):
  attrs = td.attrs
  for a in attrs:
    key,value = a
    if key == "bgcolor":
      return value

def getRoomState(td):
  color = findColor(td)
  if color == FREE_ROOM_COLOR:
    return ROOM_OPEN
  if color == USED_ROOM_COLOR:
    return ROOM_USED
  if color == CLOSED_ROOM_COLOR:
    return ROOM_CLOSED
  if color == SUNDAY_ROOM_COLOR:
    return ROOM_CLOSED
  return "unknown"

def parseRaumInfoURL(url):
  f = urllib.urlopen(url)
  html = f.read()

  soup = BeautifulSoup(''.join(html))

  table = soup.findAll('table')[1]
  rows = table.findAll('tr')
 
  # Parse headers
  rowHeaders = map(lambda row: " ".join(row.findAll(text=True)), rows[0].findAll('b'))
  rowHeaders = map(lambda item: re.match("\w+\s+(\d+\.\d+)", item).group(1), rowHeaders)
  # Add sunday
  m = re.match("(\d+)\.(\d+)",rowHeaders[0])
  mday = int(m.group(1))
  mmonth = int(m.group(2))
  myear = datetime.date.today().year 
  dmonday = datetime.date(myear,mmonth,mday)
  dsunday = dmonday-datetime.timedelta(days=1)
  rowHeaders = ["{day}.{month}".format(day=dsunday.day,month=dsunday.month)]+rowHeaders

  # Find the time on the left side
  time = 0
  timetable = []
  ## Find out the number of time values we have
  for tr in rows[1:]:
    cols = tr.findAll('td')
    htime = cols[0].find('b')
    if htime is not None:  
      m = re.match("(\d+)\-\d+",htime.find(text=True))
      time = float(m.group(1))
      timetable.append(time)
    else:
      time += DT
      timetable.append(time)

  # Create the roomtable
  roomtable = [[None]*7 for x in xrange(len(timetable))]

  rowid = 0
  for tr in rows[1:]:
    cols = tr.findAll('td')
    columnid = 0
    for td in cols:
      state = getRoomState(td)
      if state != "unknown":
        rowspan = findRowspan(td)
        startcolum = 0
        for c in range(0,7):
          if roomtable[rowid][c] == None:
            startcolum = c
            break
        for i in range(rowid,rowid+rowspan):
          roomtable[i][startcolum] = state
        columnid += 1
    rowid += 1
#
#  for row in roomtable:
#    print row
#  return {"header":rowHeaders, "time": timetable, "timetable": roomtable, "dt": DT }
  return {"header": rowHeaders, "stime":rowHeaders[0],"etime":(rowHeaders[:]+DT),"timetable": roomtable, "dt": DT}

def parseRaumInfoWebsite(building,floor,room,date):
  s = "http://www.rauminfo.ethz.ch/Rauminfo/Rauminfo.do?region=Z&areal=Z&gebaeude={building}&geschoss={floor}&raumNr={room}&rektoratInListe=true&raumInRaumgruppe=true&tag={day}&monat={month}&jahr={year}&checkUsage=anzeigen".format(
      building=building,
      floor=floor,
      room=room,
      year=date.year,
      month=monthMap[date.month],
      day=date.day)
  return parseRaumInfoURL(s)

def getRoomAllocation(room,date=datetime.date.today()):
  if type(room) != Room:
    print "Input has wrong type"
    raise
  building=room.building.name
  floor=room.floor.floor
  room=room.number
  return parseRaumInfoWebsite(building,floor,room,date)
 
