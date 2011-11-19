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
    CAB : {
    address: {
      city:
      street:
    HG : {

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

</pre>
"""
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

@app.route("/json",methods=['GET', 'POST'])
def jsonRequest():
  if request.method == "POST":
    if request.json != None:
      return resultOkay({})
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
