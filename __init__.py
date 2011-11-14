# -*- coding: <utf-8> -*-

from flask import Flask, request, session, g, redirect, url_for, abort, render_template, flash, jsonify
import os, os.path
import json
import Building
import AccessPoints
import ETHdata
app = Flask(__name__)


@app.route("/")
@app.route("/r")
def all():
  return json.dumps(Building.toJSON())

@app.route("/m/")
def getAccessPoints():
  return json.dumps(AccessPoints.toJSON())
@app.route("/m/<macaddress>")
def getAccessPoint(macaddress):
  return json.dumps(AccessPoints.toJSON(macaddress))
@app.route("/r/<building>")
def getBuilding(building):
  return json.dumps(Building.findBuilding(building)
      .toJSON())
      
@app.route("/r/<building>/<room>")
def getFloor(building,room):
  return json.dumps(Building.findFloor(building,room)
      .toJSON())

@app.route("/r/<building>/<room>/<floor>")
def getRoom(building,room,floor):
  return json.dumps(Building.findRoom(building,room,floor)
      .toJSON())

if __name__ == "__main__":
  ETHdata.readETHData()
  AccessPoints.read()
  app.run(port=23032,host="0.0.0.0")
