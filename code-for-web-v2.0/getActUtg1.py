import imghdr
import json
import os
import random
import shutil

import cv2

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

                actXML = os.path.join(outputPath, apk, 'layouts', nodeId + '.xml')
                imgPath = (dirNow) + node["image"]
                if os.path.exists(actXML):
                    splitImg(outputPath, apk, nodeId, actName, dirNow, imgPath)

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

                actXML = os.path.join(outputPath, apk, 'layouts', nodeId + '.xml')
                imgPath = (dirNow) + node["image"]
                if os.path.exists(actXML):
                    splitImg(outputPath, apk, nodeId, actName, dirNow, imgPath)

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

    get_componentsUTG(dirNow)





def splitImg(outputPath, apk, nodeId, actName, dirNow, imgPath):
    # nodeId == actName
    img = cv2.imread(imgPath)
    actXML = os.path.join(outputPath, apk, 'layouts', nodeId + '.xml')
    f = open(actXML, "r")
    txtAll = f.read()
    # print txtAll
    # print imgPath
    componentsPath = (dirNow) + '/static/components'

    if not os.path.exists(os.path.join(componentsPath, actName)):
        os.makedirs(os.path.join(componentsPath, actName))
    os.path.join(componentsPath, actName)
    id_is_null = 0
    for txt in txtAll.split("<node"):
        # print txt
        if txt.find('bounds="') != -1:
            bound = txt.split('bounds="')[1].split('"')[0]
            classInfo = txt.split('class="')[1].split('"')[0].split('.')[-1]
            resource_id = txt.split('resource-id="')[1].split('"')[0].split('.')[-1]
            if resource_id.find(':id/') != -1:
                resource_id = resource_id.split(':id/')[1]
            print bound, classInfo
            components_Path = os.path.join(componentsPath, actName, classInfo)
            if not os.path.exists(components_Path):
                os.makedirs(components_Path)
            if classInfo == '':
                continue

            y0 = int(bound.split(',')[1].split(']')[0])
            y1 = int(bound.split(',')[2].split(']')[0])
            x0 = int(bound.split(',')[0].split('[')[1])
            x1 = int(bound.split(',')[1].split('[')[1])
            component_name = str(id_is_null) + '*' + apk + '*' + classInfo + '*' + resource_id
            print id_is_null
            id_is_null = id_is_null + 1

            # cropped = img[y0:y1, x0:x1]
            # # print cropped
            # if cropped.size != 0:
            #     cv2.imwrite(os.path.join(components_Path, component_name + '.png'), cropped)
            #     print os.path.join(components_Path, component_name + '.png')
            #     print (x0, x1, y0, y1)
            try:

                cropped = img[y0:y1, x0:x1]
                # print cropped
                if cropped.size != 0:
                    cv2.imwrite(os.path.join(components_Path, component_name + '.png'), cropped)
                    print os.path.join(components_Path, component_name + '.png')
                    print (x0, x1, y0, y1)
            except TypeError:
                pass
            except UnboundLocalError:
                pass


# outputPath = '/home/zyx/software/pythonProject/tool/main-folder/outputs'
# apk = 'a2dp.Vol_133'
# nodeId = 'a2dp.Vol.AppChooser'
# actName = 'appchooser'
# dirNow = os.path.dirname(os.path.abspath(__file__))
# imgPath = '/home/zyx/software/pythonProject/tool/code/static/activity/a2dp.Vol.AppChooser.png'
# splitImg(outputPath, apk, nodeId, actName, dirNow, imgPath)


# imgPath = '/home/zyx/software/pythonProject/tool/code/static/activity/a2dp.Vol.AppChooser.png'
# img = cv2.imread(imgPath)
# cropped = img[485:661, 176:485]
# print cropped
# cv2.imwrite('/home/zyx/software/pythonProject/tool/code/static/components/appchooser/TextView/14*a2dp.Vol_133*TextView*Vol:id/pi_tv_name.png', cropped)

# getActUtg()



def get_componentsUTG(dirNow):
    componentsPath = (dirNow) + '/static/components'

    for act in os.listdir(componentsPath):
        # print act
        nodeIdSet = []
        nodesSet = {}
        for cl in os.listdir(os.path.join(componentsPath, act)):
            if not os.path.isdir(os.path.join(componentsPath, act, cl)):
                continue
            for img in os.listdir(os.path.join(componentsPath, act, cl)):
                imgPath = os.path.join(componentsPath, act, cl, img)
                nodeId = img
                apk = img.split('*')[1]
                node = {}
                nodeIdSet.append(nodeId)
                node["id"] = img
                # node["id"] = act + 'Activity_' + cl
                node["shape"] = "image"
                node["label"] = img.split('*')[-1].split('.png')[0] + '_' + cl
                node["app"] = apk
                # print imgPath
                node["image"] = "/static/" + imgPath.split('static/')[1]
                node["actcode"] = ''
                node["layoutcode"] = ''
                # if ret.find('"source_layoutcode": "') == -1:
                #     node["layoutcode"] = "<root />"
                # else:
                #     node["layoutcode"] = ret.split('"source_layoutcode": "')[1].split('",')[0].replace('\\n', '\n').replace(
                #         '\\"', '\"')
                # print(node)
                # nodesSet.append(node)

                actName = act
                if cl not in nodesSet:
                    nodesSet[cl] = []
                nodesSet[cl].append(node)
                # print nodesSet[cl]

        newPath = os.path.join(componentsPath, act)
        for act in nodesSet:
            print nodesSet[act]
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

            for i in range(0, actN - 1):
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

# dirNow = os.path.dirname(os.path.abspath(__file__))
# get_componentsUTG(dirNow)

# getActUtg()
