### Config file for the distsysproject

# Server DNS Name
SERVER_PORT = 32123
SERVER_NAME = "localhost"
SERVER_URL = "http://{SERVER_NAME}:{PORT}".format(PORT=PORT,SERVER_NAME=SERVER_NAME)
# Defaults for the cache
LOCAL_CACHE_URL = "/static/cache/" 
LOCAL_CACHE_DIR = "static/cache/"
# You might want to change this if you want to change from gif to png or so..
CACHED_IMAGE_TYPE = "gif"

