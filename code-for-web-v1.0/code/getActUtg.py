import imghdr
import json
import os
import random
import shutil

import get_similarACT

def getActUtg():
    dirNow = os.path.dirname(os.path.abspath(__file__))
    outputPath = (dirNow).split('/code')[0]+'/main-folder/outputs/'
    classifyActSet = {}
    nodeIdSet = []
    nodesSet = {}
    for apk in os.listdir(outputPath):
        if apk == "IC3_fail.txt" or apk == "log.txt":
            continue
        print(apk)
        for item in os.listdir(os.path.join(outputPath, apk, "screenshots")):
            shutil.copy(os.path.join(os.path.join(outputPath, apk, "screenshots"), item), (dirNow) + '/static/activity')

        dataPath = os.path.join(outputPath, apk, "output/data.js")
        f = open(dataPath, "r")
        txtAll = f.read()
        # print(txtAll)

        # linksSet = []

        for ret in txtAll.split('"target_layoutcode": "'):
            ret = '"target_layoutcode": "' + ret
            # print(ret)

            if ret.find('"source_fullname": "') == -1:
                if ret.find('"source_fullname":') == -1:
                    continue
                nodeId = "null"
            else:
                nodeId = ret.split('"source_fullname": "')[1].split('"')[0]

            # link["from"] = nodeId
            if nodeId not in nodeIdSet and nodeId != "null":
                node = {}
                nodeIdSet.append(nodeId)
                node["id"] = nodeId
                node["shape"] = "image"
                node["label"] = nodeId.split('.')[-1]
                node["app"] = apk
                node["image"] = "/static/activity/" + ret.split('"sourceimg": "')[1].split('screenshots/')[1].split('"')[0]
                node["actcode"] = ret.split('"source_actcode": "')[1].split('",')[0].replace('\\n', '\n').replace('\\"', '\"')
                if ret.find('"source_layoutcode": "') == -1:
                    node["layoutcode"] = "<root />"
                else:
                    node["layoutcode"] = ret.split('"source_layoutcode": "')[1].split('",')[0].replace('\\n', '\n').replace('\\"', '\"')
                # print(node)
                # nodesSet.append(node)

                classifyActSet, actName = get_similarACT.getSimilarSet(apk, nodeId.split('.')[-1], classifyActSet)
                if actName not in nodesSet:
                    nodesSet[actName] = []
                else:
                    nodesSet[actName].append(node)

            if ret.find('"target_fullname": "') == -1:
                nodeId = "null"
            else:
                nodeId = ret.split('"target_fullname": "')[1].split('"')[0]
            # link["to"] = id
            if nodeId not in nodeIdSet:
                node = {}
                nodeIdSet.append(nodeId)
                node["id"] = nodeId
                node["shape"] = "image"
                node["label"] = nodeId.split('.')[-1]
                node["app"] = apk
                node["image"] = "/static/activity/" + ret.split('"targetimg": "')[1].split('screenshots/')[1].split('"')[0]
                node["actcode"] = ret.split('"target_actcode": "')[1].split('",')[0].replace('\\n', '\n').replace('\\"', '\"')
                if ret.find('"target_layoutcode": "') == -1:
                    node["layoutcode"] = "<root />"
                else:
                    node["layoutcode"] = ret.split('"target_layoutcode": "')[1].split('",')[0].replace('\\n', '\n').replace('\\"', '\"').replace('\\t', '\t')
                # print(node)
                # nodesSet.append(node)

                classifyActSet, actName = get_similarACT.getSimilarSet(apk, nodeId.split('.')[-1], classifyActSet)

                if actName not in nodesSet:
                    nodesSet[actName] = []
                nodesSet[actName].append(node)

    # print(nodesSet)
    activityPath = (dirNow) + '/static/activity'
    newPath = (dirNow) + '/static/actJS'
    for act in nodesSet:
        # print act
        link = {}
        linksSet = []
        actN = len(nodesSet[act])
        if actN == 1:
            with open(os.path.join(newPath, act + 'Utg.js'), 'w') as can:
                can.write('var utg = {')
                can.write('\n')
                can.write('"nodes": ' + str(nodesSet[act]) + ',')
                can.write('\n')
                can.write('"edges": ' + str(linksSet))
                can.write('\n')
                can.write('}')
            continue

        for i in range(0, actN-1):
            link = {}
            # print(nodesSet[act][i])
            link["from"] = nodesSet[act][i]["id"]
            # toN = random.randrange(0, actN - 1)
            # if toN == i:
            #     toN = random.randrange(0, actN - 1)
            toN = i + 1
            link["to"] = nodesSet[act][toN]["id"]
            # print(toN)
            linksSet.append(link)

        if nodesSet[act] == []:
            continue

        with open(os.path.join(newPath, act + 'Utg.js'), 'w') as can:
            can.write('var utg = {')
            can.write('\n')
            can.write('"nodes": ' + str(nodesSet[act]) + ',')
            can.write('\n')
            can.write('"edges": ' + str(linksSet))
            can.write('\n')
            can.write('}')



# getActUtg()