# -*- coding: <utf-8> -*-

from flask import Flask, request, session, g, redirect, url_for, abort, render_template, flash, jsonify
import os, os.path
import json
import Building
import AccessPoints
import ETHdata
app = Flask(__name__)


@app.route("/")
def hello():
	return "Hello World"

@app.route("/all")
def all():
  return json.dumps(Building.toJSON())

#return jsonify(Building.buildings)

if __name__ == "__main__":
  global accessPoints
  ETHdata.readETHData()
  accessPoints = AccessPoints.WLANAccessPoints()
  print Building.buildings

  print json.dumps(Building.toJSON())
  app.run(port=23032,host="0.0.0.0")
