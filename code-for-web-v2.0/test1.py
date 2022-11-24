import imghdr
import json
import os
import shutil

name = "com.sunglab.bigbanghd_1"
dataJsPath = (os.path.dirname(__file__)) + '/static/js/data.js'

def rewrite(name, dataJsPath):
    file_data = ''
    with open(dataJsPath, "r") as f:
        for line in f:
            if line.find('"targetimg": "') != -1:
                oldTxt = line.split('"targetimg": "')[1].split(name + '/')[0]
                line = line.replace(oldTxt + name + '/', 'static')
            elif line.find('"sourceimg": "') != -1:
                oldTxt = line.split('"sourceimg": "')[1].split(name + '/')[0]
                line = line.replace(oldTxt + name + '/', 'static')
            if line != "":
                file_data += line
        # print file_data
    with open(dataJsPath, "w") as f:
        f.write(file_data)

# rewrite(name, dataJsPath)

li = []
li.append("1")
li.append("aaa")
print li
print json.dumps(li)
print json.dumps(["1", "aaa"])
print json.dumps("1", "aaa")
print os.getcwd()

f = open('/home/zyx/software/pythonProject/tool/code/static/js/data.js', "r")
lines = f.readlines()
print len(lines)
for i in range(1, 3):
    print lines[i]

path = '/home/zyx/software/pythonProject/tool/main-folder/outputs/app.fedilab.nitterizeme.activities_31/output/data.js'
shutil.copy(path, '/home/zyx/software/pythonProject/tool/code/static/icon')