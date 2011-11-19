### Config file for the distsysproject

# Server DNS Name


SERVER_PORT = 32123
SERVER_NAME = "deserver.moeeeep.com"
SERVER_URL = "http://{SERVER_NAME}:{PORT}".format(PORT=SERVER_PORT,SERVER_NAME=SERVER_NAME)
CACHE_URL= SERVER_URL+"/static/cache/"
# Defaults for the cache
LOCAL_CACHE_DIR = "static/cache/"
# You might want to change this if you want to change from gif to png or so..
CACHED_IMAGE_TYPE = "gif"

