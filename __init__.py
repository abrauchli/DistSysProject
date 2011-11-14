# -*- coding: <utf-8> -*-

from flask import Flask, request, session, g, redirect, url_for, abort, render_template, flash, jsonify
import os, os.path
import json
import Building
import AccessPoints
import ETHdata
app = Flask(__name__)


@app.route("/")
def currentRoutes():
  return """
<pre>Currently mapped routes:
  /r/ <-- Lists all buildings
  /r/building
  /r/building/floor
  /r/building/floor/room
  /m/ <-- Lists all mac adresses
  /m/macadress
</pre>
  """

@app.route("/r/")
def all():
  return json.dumps(Building.getInfo())

@app.route("/m/")
def getAccessPoints():
  return json.dumps(AccessPoints.getInfo())
@app.route("/m/<macaddress>")
def getAccessPoint(macaddress):
  return json.dumps(AccessPoints.objectInfo(macaddress))
@app.route("/r/<building>")
def getBuilding(building):
  return json.dumps(Building.findBuilding(building)
      .getInfo())
      
@app.route("/r/<building>/<room>")
def getFloor(building,room):
  return json.dumps(Building.findFloor(building,room)
      .getInfo())

@app.route("/r/<building>/<room>/<floor>")
def getRoom(building,room,floor):
  return json.dumps(Building.findRoom(building,room,floor)
      .getInfo())

if __name__ == "__main__":
  ETHdata.readETHData()
  AccessPoints.read()
  print getAccessPoint("00:03:52:e5:ad:51")
  app.run(port=23032,host="0.0.0.0",debug=True)
