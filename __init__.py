from sqlalchemy import sql, schema, create_engine
from flask import Flask, request, session, g, redirect, url_for, abort, render_template, flash
import os, os.path
app = Flask(__name__)


@app.route("/")
def hello():
	return "Hello World"


def connect():
  global schema, dbengine
  if not dbengine:
    engine = create_engine('sqlite:///:memory:', echo=True)
    metadata.bind = engine

    metadata.create_all(checkfirst=True)
  return dbengine

if __name__ == "__main__":
	app.run(port=23032,host="0.0.0.0")
