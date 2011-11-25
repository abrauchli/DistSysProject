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

from exception import *

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
        "msg": message 
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
    CAB : {
    address: {
      city:
      street:
      location: Zentrum/Höngg/Other
: {

r/CAB
  {
    name: CAB
    address: {
        city:
        street:
    floors: {
        A : {
          map = “....”

Error Message: “Building not found”
r/CAB/E # DONE, but not tested 
  {
    building : {same as r/CAB}
    rooms: {
       18.1: {
         desc = “Büro”
    map = “....”
    mapAvailable = true/false

r/CAB/E/18.1  ## DONE, but not tested
  {
    building = “CAB”
    floor = ”E”
    name= 18.1
    desc= Büro
    map = “...”
    mapAvailable = true/false
    location =

r/CAB/E/18.1/allocation ## HIGHLY EXPERIMENTAL
  {timetable: [['closed', 'free', 'free', 'free', 'free', 'free', 'closed'],
               ['closed', 'free', 'free', 'free', 'free', 'free', 'closed'],
               ...],
   header: ['20.11', u'21.11', u'22.11', u'23.11', u'24.11', u'25.11', u'26.11'], ## The available dates
   time: [7.0, 7.25, 7.5, 7.75, 8.0, 8.25, 8.5, 8.75, 9.0, 9.25, 9.5, 9.75, 10.0, 10.25, 10.5, 10.75, 11.0, 11.25, 11.5, 11.75, 12.0, 12.25, 12.5, 12.75, 13.0, 13.25, 13.5, 13.75, 14.0, 14.25, 14.5, 14.75, 15.0, 15.25, 15.5, 15.75, 16.0, 16.25, 16.5, 16.75, 17.0, 17.25, 17.5, 17.75, 18.0, 18.25, 18.5, 18.75, 19.0, 19.25, 19.5, 19.75, 20.0, 20.25, 20.5, 20.75, 21.0, 21.25, 21.5, 21.75] ## the timeslot allocated for row


/json
  Send with:
  curl -v -H "Content-Type:application/json" -X POST -d '{"request":"location","aps":{"00:03:52:2b:e9:01":75, "00:0f:61:be:63:13":12}}' http://deserver.moeeeep.com:32123/json
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
        result = { information from either room/floor (we have some ap's which we can't match to a room) }
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

</pre>
"""
#  {ok = true, result = {BLOB}

@app.route("/r/")
def all():
  return resultOkay(Model.getBuildings())
@app.route("/r/<building>")
def getBuilding(building):
  try: 
    r = Model.getBuilding(building)
    return resultOkay(r)
  except NotFoundException as e:
    return resultError(e.getError())

@app.route("/r/<building>/<room>")
def getFloor(building,room):
  try:
    r = Model.getFloor(building,room)
    return resultOkay(r)
  except NotFoundException as e:
    return resultError(e.getError())

@app.route("/r/<building>/<room>/<floor>")
def getRoom(building,room,floor):
  try:
    r = Model.getRoom(building,room,floor)
    return resultOkay(r)
  except NotFoundException as e:
    return resultError(e.getError())


@app.route("/r/<building>/<room>/<floor>/allocation")
def getRoomAllocation(building,room,floor):
  try:
    r = Model.findRoom(building,room,floor)
    timetable = ETHReadRoomAllocation.getRoomAllocation(r)
    return resultOkay(timetable)
  except NotFoundException as e:
    return resultError(e.getError())

@app.route("/json",methods=['GET', 'POST'])
def jsonRequest():
  if request.method == "POST":
    if request.json != None:
      try: 
        req = Controller.parseJSONRequest(request.json)
        return resultOkay(req)
      except NotFoundException as e:
        return resultError(e.getError())
      except ValueError as e:
        return resultError("Input malformed:"+str(e))
      except:
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
