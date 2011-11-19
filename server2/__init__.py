#!/usr/bin/python
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
import Building
import AccessPoints
import ETHdata
import Cache
app = Flask(__name__)


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
r/CAB/E
  {
    building : {same as r/CAB}
    rooms: {
       18.1: {
         desc = “Büro”
          map = “....”

r/CAB/E/18.1
  {
    building = “CAB”
    floor = ”E”
    name= 18.1
    desc= Büro
    map = “...”
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

@app.route("/r/")
@app.route("/r/<building>")
@app.route("/r/<building>/<room>")
@app.route("/r/<building>/<room>/<floor>")

