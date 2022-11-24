# encoding: utf-8
from fuzzywuzzy import fuzz

def spiltStr(str):
    if str.lower().find('activity') != -1:
        str = str.lower().replace('activity', '')
    elif str.lower().find('act') != -1 and str.lower().endswith('act'):
        str = str.lower().replace('act', '')
    else:
        str = str.lower()
    # str = str.split('.')[-1]
    return str

def getSimilarStr(actName, classifyActSet):
    similarAct = []
    similarActTag = ''
    actName = spiltStr(actName)
    for file in classifyActSet:
        if fuzz.token_set_ratio(actName, file) > 60:
            # print (fuzz.token_set_ratio(actName, fileT), fileT)
            similarAct.append((fuzz.token_set_ratio(actName, file), file))
    similarAct.sort(key=lambda x: x[0], reverse=True)
    if similarAct != []:
        similarActTag = similarAct[0][1]
    return similarActTag

def getSimilarSet(apk, actName, classifyActSet):
    act = actName
    actName = spiltStr(actName)
    if classifyActSet == {}:
        similarActTag = actName
        classifyActSet[actName] = [(apk, act)]
    else:
        similarActTag = getSimilarStr(actName, classifyActSet)
        if similarActTag != '':
            # print classifyActSet
            # print similarActTag
            classifyActSet[similarActTag].append((apk, act))
        elif actName != '':
            similarActTag = actName
            classifyActSet[actName] = [(apk, act)]
    if similarActTag =="":
        print act
    return classifyActSet, similarActTag