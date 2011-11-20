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

URL = "http://www.rauminfo.ethz.ch/Rauminfo/Rauminfo.do?region=Z&areal=Z&gebaeude=CHN&geschoss=D&raumNr=44&rektoratInListe=true&raumInRaumgruppe=true&tag=20&monat=Nov&jahr=2011&checkUsage=anzeigen"

HEADER_REGEX = "" 

FREE_ROOM_COLOR = '#99cc99'
USED_ROOM_COLOR = '#006799'
CLOSED_ROOM_COLOR = '#cccccc'
SUNDAY_ROOM_COLOR = '#eeeeee'
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
    return " free "
  if color == USED_ROOM_COLOR:
    return " used "
  if color == CLOSED_ROOM_COLOR:
    return "closed"
  if color == SUNDAY_ROOM_COLOR:
    return "sunday"
  return "unknown"


f = urllib.urlopen(URL)
html = f.read()

soup = BeautifulSoup(''.join(html))

table = soup.findAll('table')[1]
rows = table.findAll('tr')
#rowHeaders = map(lambda row: row.findAll(text=True)[1], rows[0].findAll('b'))
rowHeaders = map(lambda row: " ".join(row.findAll(text=True)), rows[0].findAll('b'))
print rowHeaders
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
    time += 0.25
    timetable.append(time)
  print ""

print timetable

roomtable = [[None]*7 for x in xrange(len(timetable))]
print roomtable

rowid = 0
for tr in rows[1:]:
  cols = tr.findAll('td')
  columnid = 0
  for td in cols:
    state = getRoomState(td)
    if ((rowid)%4) !=0:
      rowspan = findRowspan(td)
      print range(rowid,rowid+rowspan)
      sr = 0
      for i in range(rowid,rowid+rowspan):
        for c in range(sr,6):
          if roomtable[i][c] == None:
            roomtable[i][c] = state
            sr = c
            break
    columnid += 1

  rowid += 1

for row in roomtable:
  print row

