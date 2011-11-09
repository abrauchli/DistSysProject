import csv
import Building
import AccessPoints
import People

ETHBUILDINGS = 'data/ethgeb.txt'

def fillBuildings():
  reader = csv.reader(open(ETHBUILDINGS, 'rb'), delimiter='|')
  for row in reader:
    Building.addBuilding(row[0],row[1],row[2])
  
if __name__== "__main__":
  fillBuildings()

