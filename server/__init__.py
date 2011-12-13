#!/usr/bin/python
# -*- coding: utf-8 -*-
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

from flask import Flask, request, session, g, redirect, url_for, abort, render_template, flash, jsonify
import os, os.path
import json
import Model
import Controller
import config
import AccessPoints
import ETHReadRoomAllocation
app = Flask(__name__)

def resultOkay(obj):
  return json.dumps(
      {
        "ok": True,
        "result": obj
      }
      )
def resultError(message):
  return json.dumps(
      {
        "ok": False,
        "message": message 
      }
        )

@app.route("/")
def currentRoutes():
  return """<pre>
Encapsulation in:
    {ok = true, result = {BLOB}

In case of Error:
    Send status 500
    {ok = false, message = {see below}

r/
  {
    "ok": true,
    "result":
      {
        "HIT": 
          {
            "campus": "Höngg", 
            "name": "HIT",
            "address": 
              {
                "city": "8093 Zürich", 
                "street": "Wolfgang-Pauli-Str. 27", 
                "campus": "Höngg"
              }
          }
        "HG": 
          {
            "campus": "Zentrum", 
            "name": "HG", 
            "address": 
              {
                "city": "8092 Zürich", 
                "street": "Rämistrasse 101",
                "campus": "Zentrum"
              }
          }

r/HG
  {
    "ok": true, 
    "result": 
      {
        "name": "HG", 
        "address": 
          {
            "city": "8092 Zürich", 
            "street": "Rämistrasse 101", 
            "campus": "Zentrum"
          },
        "floors": 
          {
            "DO": 
              { "map": "http://deserver.moeeeep.com:32123/static/cache/HG_DO.gif" }, 
            "C": 
              { "map": "http://deserver.moeeeep.com:32123/static/cache/HG_C.gif" }, 
            "B": 
              { "map": "http://deserver.moeeeep.com:32123/static/cache/HG_B.gif" }, 
            "E": 
              { "map": "http://deserver.moeeeep.com:32123/static/cache/HG_E.gif" }, 
            "D": 
              { "map": "http://deserver.moeeeep.com:32123/static/cache/HG_D.gif" }, 
            "G": 
              { "map": "http://deserver.moeeeep.com:32123/static/cache/HG_G.gif" }, 
            "F": 
              { "map": "http://deserver.moeeeep.com:32123/static/cache/HG_F.gif" }, 
            "H": 
              { "map": "http://deserver.moeeeep.com:32123/static/cache/HG_H.gif" }, 
            "K": 
              { "map": "http://deserver.moeeeep.com:32123/static/cache/HG_K.gif" }, 
            "J": 
              { "map": "http://deserver.moeeeep.com:32123/static/cache/HG_J.gif" },
            "0": 
              {}, 
            "JO": 
              { "map": "http://deserver.moeeeep.com:32123/static/cache/HG_JO.gif" }, 
            "GO": 
              { "map": "http://deserver.moeeeep.com:32123/static/cache/HG_GO.gif" }, 
            "FO": 
              { "map": "http://deserver.moeeeep.com:32123/static/cache/HG_FO.gif" }
          } 
      }
  }

Error Message: 
  {
    "msg": "Couldn't find building: qwerty", 
    "ok": false
  }

r/HG/E
  {
    "ok": true, 
    "result": 
      {
        "building": 
          {
            "campus": "Zentrum", 
            "name": "HG", 
            "address": 
              {
                "city": "8092 Zürich", 
                "street": 
                "Rämistrasse 101", 
                "campus": "Zentrum"
              }
          }, 
        "map": "http://deserver.moeeeep.com:32123/static/cache/HG_E.gif", 
        "rooms": 
          {
            "33.5": {"desc": "Studentenraum"}, 
            "1.1": {"desc": "Hörsaal"}, 
            "1.2": {"desc": "Hörsaal"}, 
            "33.3": {"desc": "Büro"}, 
            "30.2": {"desc": "Brandmeldeanlage"}, 
            "30.1": {"desc": "Vorraum/Nebenraum"}, 
            "30.6": {"desc": "Eingangshalle/Foyer"}, 
            "30.5": {"desc": "Kaffeebar"}, 
            "38.3": {"desc": "PSA"}, 
            "38.2": {"desc": "Büro"}, 
            "33.1": {"desc": "Büro"}, 
            "37.1": {"desc": "Betriebsraum"}, 
            "37.2": {"desc": "Büro"}, 
            "52": {"desc": "Fax"}, 
            "13.2": {"desc": "Büro"}, 
            "13.1": {"desc": "Büro"}, 
            "24": {"desc": "Computerraum"}, 
            "20.1": {"desc": "Büro"}, 
            "10.2": {"desc": "Fax"}, 
            "20": {"desc": "Büro"}, 
            "22": {"desc": "Büro"}, 
            "23": {"desc": "Computerraum"}, 
            "47": {"desc": "Fax"}, 
            "44": {"desc": "Büro"}, 
            "42": {"desc": "Pallmann-Zimmer"}, 
            "40": {"desc": "Büro"}, 
            "41": {"desc": "Büro"}, 
            "3": {"desc": "Hörsaal"}, 
            "5": {"desc": "Hörsaal"}, 
            "7": {"desc": "Hörsaal"}, 
            "40.1": {"desc": "PSA"}, 
            "69.1": {"desc": "Büro"}, 
            "69.3": {"desc": "Büro"}, 
            "69.2": {"desc": "Büro"}, 
            "20.0029": {"desc": "Verkehrsfläche"}, 
            "27": {"desc": "Computerraum"},
            "10.0042": {"desc": "Fax"}, 
            "15": {"desc": "Fax"}, 
            "43.1": {"desc": "Konferenzraum"}, 
            "26.3": {"desc": "Computerraum"}, 
            "32": {"desc": "Büro"}, 
            "26.1": {"desc": "Computerraum"}, 
            "11": {"desc": "Fax"}, 
            "68.4": {"desc": "Büro"}, 
            "39": {"desc": "Fax"}, 
            "18.2": {"desc": "Fax"}, 
            "58.1": {"desc": "Büro"}, 
            "14": {"desc": "Büro"}, 
            "16": {"desc": "Büro"}, 
            "19": {"desc": "Computerraum"}, 
            "54": {"desc": "Büro"}, 
            "30": {"desc": "Kassierstation"}, 
            "34": {"desc": "Büro"}, 
            "48.2": {"desc": "Fax"}, 
            "48.1": {"desc": "Büro"}, 
            "65.2": {"desc": "Büro"}, 
            "32.1": {"desc": "Büro"}, 
            "32.2": {"desc": "Büro"}
          }
      }
  }

r/HG/E/1.1
  {
    "ok": true, 
    "result": 
      {
        "building": "HG", 
        "map": "http://deserver.moeeeep.com:32123/static/cache/HG_E_1.1.gif", 
        "room": "1.1", 
        "floor": "E", 
        "location": 
          {
            "y": 1257, 
            "x": 651, 
            "boundingbox": [531, 1162, 772, 1353]
          }, 
        "desc": "Hörsaal"
      }
  }

Tests für die JSON Requests
===========================

AP Location Query
-----------------

curl -v -H "Content-Type:application/json" -X POST -d '{"request":"location","aps":{"00:03:52:2b:e9:01":75, "00:0f:61:be:63:13":12}}' http://deserver.moeeeep.com:32123/json

Result:
  {
    "ok": true, 
    "result": 
      {
        "location": 
          {
            "coords": 
              {
                "y": 1197, 
                "x": 1343, 
                "boundingbox": [1247, 1045, 1439, 1349]
              }, 
            "type": "room", 
            "result": 
              {
                "building": "HG", 
                "map": "http://deserver.moeeeep.com:32123/static/cache/HG_F_5.gif", 
                "room": "5", 
                "floor": "F", 
                "location": 
                  {
                    "y": 1197, 
                    "x": 1343, 
                    "boundingbox": [1247, 1045, 1439, 1349]
                  }, 
                "desc": "Hörsaal"
              }
          },

        "aps": 
          {
            "00:03:52:2b:e9:01": 
              {
                "coords": 
                  {
                    "y": 1197, 
                    "x": 1343, 
                    "boundingbox": [1247, 1045, 1439, 1349]
                  }, 
                
                "location": 
                  {
                    "building": "HG", 
                    "map": "http://deserver.moeeeep.com:32123/static/cache/HG_F_5.gif", 
                    "room": "5", 
                    "floor": "F", 
                    "location": 
                      {
                        "y": 1197, 
                        "x": 1343, 
                        "boundingbox": [1247, 1045, 1439, 1349]
                      }, 
                    "desc": "Hörsaal"}
              }, 
            "00:0f:61:be:63:13": 
              {
                "coords": 
                  {
                    "y": 240, 
                    "x": 485, 
                    "boundingbox": [371, 154, 600, 327]
                  }, 
                "location": 
                  {
                    "building": "ETF", 
                    "map": "http://deserver.moeeeep.com:32123/static/cache/ETF_E_1.gif", 
                    "room": "1", 
                    "floor": "E", 
                    "location": 
                      {
                        "y": 240, 
                        "x": 485, 
                        "boundingbox": [371, 154, 600, 327]
                      }, 
                    "desc": "Hörsaal"
                  }
              }
          } 
      }
  }


Free Room Search
----------------

curl -v -H "Content-Type:application/json" -X POST -d '{"request":"freeroom","building":"HG","floor":"E","starttime":12.0,"endtime":18.0}' http://deserver.moeeeep.com:32123/json

Result:
  {
    "ok": true, 
    "result": 
      [
        {
          "building": "HG", 
          "map": "http://deserver.moeeeep.com:32123/static/cache/HG_E_24.gif", 
          "room": "24", 
          "floor": "E", 
          "location": 
            {
              "y": 609, 
              "x": 578, 
              "boundingbox": [521, 592, 636, 627]
            }, 
          "desc": "Computerraum"
        }, 
        {
          "building": "HG", 
          "map": "http://deserver.moeeeep.com:32123/static/cache/HG_E_27.gif", 
          "room": "27", 
          "floor": "E", 
          "location": 
            {
              "y": 658, 
              "x": 806, 
              "boundingbox": [639, 602, 973, 714]
            }, 
          "desc": "Computerraum"
        }, 
        {
          "building": "HG", 
          "map": "http://deserver.moeeeep.com:32123/static/cache/HG_E_26.3.gif", 
          "room": "26.3", 
          "floor": "E", 
          "location": 
            {
              "y": 833, 
              "x": 826, 
              "boundingbox": [732, 774, 920, 892]
            }, 
          "desc": "Computerraum"
        }
      ]
  }

/json
  Input from client:
    {
      request=”location”
      aps = {   
        "00:03:52:2b:e9:01" : "75",
        ”00:0f:61:be:63:13" : "81"
      }
    }
  Output:
    {
      ok = true,
      result = {
        location : {
        type = “room” / “floor”        
        result = { information from either room/floor/building }
        coords = { // Left out, when no coordinate information
          x =
          y =
        aps = {
        "00:03:52:2b:e9:01":
            {
              coords = {
                x =
                y =
              location = {
                building = “CAB”
                floor = “E”
                room = “18.1”
              }
       ”00:0f:61:be:63:13"_
            { ... }

</pre>"""
#  {ok = true, result = {BLOB}

@app.route("/r/")
def all():
  return resultOkay(Model.getBuildings())
@app.route("/r/<building>")
def getBuilding(building):
  r = Model.getBuilding(building)
  if r != None:
    return resultOkay(r)
  else:
    return resultError("Could not find building")

@app.route("/r/<building>/<room>")
def getFloor(building,room):
  r = Model.getFloor(building,room)
  if r != None:
    return resultOkay(r)
  else:
    return resultError("Could not find floor or building")

@app.route("/r/<building>/<room>/<floor>")
def getRoom(building,room,floor):
  r = Model.getRoom(building,room,floor)
  if r!= None:
    return resultOkay(r)
  else:
    return resultError("Could not find room, floor or building")


@app.route("/r/<building>/<room>/<floor>/allocation")
def getRoomAllocation(building,room,floor):
  r = Model.findRoom(building,room,floor)
  if r!= None:
    timetable = ETHReadRoomAllocation.getRoomAllocation(r)
    return resultOkay(timetable)
  else:
    return resultError("Could not find room, floor or building")

@app.route("/json",methods=['GET', 'POST'])
def jsonRequest():
  if request.method == "POST":
    if request.json != None:
      req = Controller.parseJSONRequest(request.json)
      if req != None:      
        return resultOkay(req)
      else:
        return resultError("Either Input or output malformed")
    else:
      return resultError("Input malformed: You didn't send the request with application/json")
  else: 
    return """<pre>Expecting application/json via HTTP POST</pre>"""

if __name__ == "__main__":
  Model.init()
  AccessPoints.read()
  #AccessPoints.read()
#  print getAccessPoint("00:0f:61:b4:b6:00")
#Cache.cache(Building.findRoom("HG","F","5"))
#  print getRoom("HG","F","5")    
  app.run(port=config.SERVER_PORT,host="0.0.0.0",debug=True)
