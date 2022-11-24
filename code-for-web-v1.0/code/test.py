# encoding: utf-8
import commands
import json
import shutil
import sys
import getActUtg
from os import path

reload(sys)
sys.setdefaultencoding('utf-8')
import os
import time
from flask import Flask, render_template, request, redirect, url_for, abort, send_from_directory

import run_storydistiller
app = Flask(__name__)
app.config['UPLOAD_EXTENSIONS'] = ['.apk']
app.config['UPLOAD_PATH'] = (os.path.dirname(os.path.abspath(__file__))).split('/code')[0]+'/main-folder/apks/'
app._static_folder = "./static"
inputFiles = []
dirNow = os.path.dirname(os.path.abspath(__file__))
print dirNow
@app.route('/')
def index():
    files = os.listdir(app.config['UPLOAD_PATH'])
    return render_template('index.html', files=files)
    # return render_template('index.html')
@app.route('/about')
def about():
    return render_template('about.html')
@app.route('/do')
def do():
    return render_template('do.html')
@app.route('/doS')
def doS():
    return render_template('doS.html')
@app.route('/doAct')
def doAct():
    return render_template('doAct.html')
@app.route('/doActS')
def doActS():
    return render_template('doActS.html')
@app.route('/protfolio')
def portfolio():
    return render_template('portfolio.html')
@app.route('/contact')
def contact():
    return render_template('contact.html')
@app.route('/loading', methods=['GET', 'POST'])
def loading():
    return render_template('loading.html', tempL=1)
@app.route('/loading2', methods=['GET', 'POST'])
def loading2():
    return render_template('loading2.html', tempL=1)
@app.route('/loadingAct', methods=['GET', 'POST'])
def loadingAct():
    return render_template('loadingAct.html', tempL=1)
@app.route('/loading12', methods=['GET', 'POST'])
def loading12():
    return render_template('loading12.html', tempL=1)
@app.route('/output')
def output():
    return render_template('output.html')
@app.route('/outputT')
def outputT():
    return render_template('outputT.html')
@app.route('/output1')
def output1():
    return render_template('output1.html')
@app.route('/output2')
def output2():
    return render_template('output2.html')
@app.route('/graph1')
def graph1():
    return render_template('graph1.html')
@app.route('/graph2')
def graph2():
    return render_template('graph2.html')
@app.route('/graph12')
def graph12():
    return render_template('graph12.html')
@app.route('/graphAct')
def graphAct():
    return render_template('graphAct.html')
@app.route('/graphAct1')
def graphAct1():
    return render_template('graphAct1.html')
@app.route('/delFolder')
def delFolder():
    return render_template('delFolder.html')

# url_for,修改静态文件（js,css,image)时，网页同步修改
# @app.context_processor
# def override_url_for():
#     return dict(url_for=dated_url_for)
#
# def dated_url_for(endpoint, **values):
#     filename = None
#     if endpoint == 'static':
#         filename = values.get('filename', None)
#     if filename:
#         file_path = path.join(app.root_path, endpoint, filename)
#         values['v'] = int(os.stat(file_path).st_mtime)
#     return url_for(endpoint, **values)

# @app.context_processor  # 上下文渲染器，给所有html添加渲染参数
# def inject_url():
#     data = {
#         "url_for": dated_url_for,
#     }
#     return data
# def dated_url_for(endpoint, **values):
#     filename = None
#     if endpoint == 'static':
#         filename = values.get('filename', None)
#     if filename:
#         file_path = os.path.join(app.root_path, endpoint, filename)
#         values['v'] = int(os.stat(file_path).st_mtime)  # 取文件最后修改时间的时间戳，文件不更新，则可用缓存
#     return url_for(endpoint, **values)


@app.route('/', methods=['POST'])
def upload_file():
    message = "Start to analyse!"
    uploaded_file = request.files['file']
    if uploaded_file.filename != '':
        file_ext = os.path.splitext(uploaded_file.filename)[1]
        if file_ext not in app.config['UPLOAD_EXTENSIONS']:
            message = "Please input an apk file!"
            return render_template("index.html", temp1=message)
        inputFiles.append(os.path.splitext(uploaded_file.filename)[0])
        uploaded_file.save((dirNow).split('/code')[0]+'/main-folder/apks/' + uploaded_file.filename)
        print("获取上传文件的名称为[%s]\n" % uploaded_file.filename)
    # return redirect(url_for('index'), temp1=message)
    return render_template("index.html", temp1=message)

@app.route('/', methods=['POST'])
def upload_files():
    message = "Start to analyse!"
    uploaded_file = request.files['file']
    filename = uploaded_file.filename
    if filename != '':
        file_ext = os.path.splitext(filename)[1]
        # if file_ext not in app.config['UPLOAD_EXTENSIONS']:
        #     return "Invalid file", 400
        uploaded_file.save(os.path.join(app.config['UPLOAD_PATH'], filename))
    # return '', 204

@app.route('/uploads/<filename>')
def upload(filename):
    return send_from_directory(app.config['UPLOAD_PATH'], filename)


@app.route("/hello", methods=['GET', 'POST'])
def Hello():
    message = "hello"
    return message
    # return render_template("index.html", temp=message)

def hhh():
    print("hhhhhh")
    # for i in range(0,10):
    #     print(i)

def rename(apk_path, apk_dir):
    global defined_pkg_name
    global used_pkg_name
    defined_pkg_name = commands.getoutput('aapt dump badging %s | grep package | awk \'{print $2}\' | sed s/name=//g | sed s/\\\'//g'%(apk_path))
    launcher = commands.getoutput(r"aapt dump badging " + apk_path + " | grep launchable-activity | awk '{print $2}'")
    # Sometimes launcher is empty or launcher starts with "."
    if launcher == '' or defined_pkg_name in launcher or launcher.startswith("."):
        used_pkg_name = defined_pkg_name
    else:
        print launcher
        used_pkg_name = launcher.replace('.' + launcher.split('.')[-1], '').split('\'')[1]
    print 'Rename pkg: ' + used_pkg_name

    version = commands.getoutput('aapt dump badging %s | grep versionName | awk \'{print $3}\' | sed s/versionCode=//g | sed s/\\\'//g'%(apk_path))
    os.system('mv %s %s'%(apk_path, apk_dir + used_pkg_name + '_' + version + '.apk')) # used_pkg_name_version.apk
    global defined_app_name
    defined_app_name = defined_pkg_name + '_' + version
    return apk_dir + used_pkg_name + '_' + version + '.apk'

def hasFile(filePath):
    if os.path.exists(filePath):
        return 1
    else:
        return 0

def reWriteJs(originalPath, newPath):
    f = open(originalPath, "r")
    txtAll = f.read()
    nodeIdSet = []
    nodesSet = []
    linksSet = []
    for ret in txtAll.split('"target_layoutcode": "'):
        # print(ret)
        link = {}
        if ret.find('"source_fullname": "') == -1:
            if ret.find('"source_fullname":') == -1:
                continue
            id = "null"
        else:
            id = ret.split('"source_fullname": "')[1].split('"')[0]
        link["from"] = id
        if id not in nodeIdSet and id != "null":
            node = {}
            nodeIdSet.append(id)
            node["id"] = id
            node["shape"] = "image"
            node["label"] = id.split('.')[-1]
            node["image"] = ret.split('"sourceimg": "')[1].split('"')[0]
            # print(node)
            nodesSet.append(node)

        if ret.find('"target_fullname": "') == -1:
            id = "null"
        else:
            id = ret.split('"target_fullname": "')[1].split('"')[0]
        link["to"] = id
        if id not in nodeIdSet and id != "null":
            node = {}
            nodeIdSet.append(id)
            node["id"] = id
            node["shape"] = "image"
            node["label"] = id.split('.')[-1]
            node["image"] = ret.split('"targetimg": "')[1].split('"')[0]
            # print(node)
            nodesSet.append(node)
        linksSet.append(link)
    print(nodesSet)
    print(linksSet)

    with open(newPath, 'w') as can:
        can.write('var utg = {')
        can.write('\n')
        can.write('"nodes": ' + str(nodesSet) + ',')
        can.write('\n')
        can.write('"edges": ' + str(linksSet))
        can.write('\n')
        can.write('}')

def reWriteJs2(originalPath, newPath):
    f = open(originalPath, "r")
    txtAll = f.read()
    nodeIdSet = []
    nodesSet = []
    linksSet = []
    for ret in txtAll.split('"target_layoutcode": "'):
        # print(ret)
        link = {}
        if ret.find('"source_fullname": "') == -1:
            if ret.find('"source_fullname":') == -1:
                continue
            id = "null"
        else:
            id = ret.split('"source_fullname": "')[1].split('"')[0]
        link["from"] = id
        if id not in nodeIdSet and id != "null":
            node = {}
            nodeIdSet.append(id)
            node["id"] = id
            node["shape"] = "image"
            node["label"] = id.split('.')[-1]
            node["image"] = ret.split('"sourceimg": "')[1].split('"')[0]
            # print(node)
            nodesSet.append(node)

        if ret.find('"target_fullname": "') == -1:
            id = "null"
        else:
            id = ret.split('"target_fullname": "')[1].split('"')[0]
        link["to"] = id
        if id not in nodeIdSet and id != "null":
            node = {}
            nodeIdSet.append(id)
            node["id"] = id
            node["shape"] = "image"
            node["label"] = id.split('.')[-1]
            node["image"] = ret.split('"targetimg": "')[1].split('"')[0]
            # print(node)
            nodesSet.append(node)
        linksSet.append(link)
    print(nodesSet)
    print(linksSet)

    with open(newPath, 'w') as can:
        can.write('var utg2 = {')
        can.write('\n')
        can.write('"nodes": ' + str(nodesSet) + ',')
        can.write('\n')
        can.write('"edges": ' + str(linksSet))
        can.write('\n')
        can.write('}')

@app.route("/endLoad", methods=['GET', 'POST'])
def endLoad():
    inputFiles1 = inputFiles
    # cmd = "python {0}".format(os.path.join(os.path.dirname(__file__), "run_storydistiller.py"))
    # print cmd
    # # commands.getoutput(cmd)
    # os.system(cmd)
    outT, outN = run_storydistiller.main()
    getActUtg.getActUtg()
    print outT
    apk1 = ''
    apk2 = ''
    apkNameL = []
    if inputFiles1 == []:
        tag = 2
    for f in list(set(inputFiles1)):
        if outT[f] ==0:
            tag = f + '.apk error!'
            del inputFiles[:]
            return str(tag)
        apk_dir = (dirNow).split('/code')[0]+'/main-folder/apksT/'
        apk_path = apk_dir + f + '.apk'
        print apk_path
        # apk_path = rename(apk_path, apk_dir)
        # apk_name = os.path.split(apk_path)[1].split('.apk')[0]
        apk_name = outN[f]
        print apk_name
        apkNameL.append(apk_name)
        os.remove(apk_path)
        inputFiles.remove(f)

        filePath = (dirNow).split('/code')[0]+'/main-folder/outputs/' + apk_name + '/output/index.html'
        print filePath
        # print inputFiles
        tag = hasFile(filePath)
        while(tag == 0):
            # print(111)
            # print(inputFiles1)
            tag = hasFile(filePath)
    time.sleep(3)
    # print(2)
    if len(apkNameL) >1:
        apk1 = apkNameL[-2]
        apk2 = apkNameL[-1]
    if len(apkNameL) ==1:
        apk1 = apkNameL[0]
        apk2 = apk1
    print json.dumps([str(tag), apk_name])
    return json.dumps([str(tag), apk1, apk2])
    # return json.dumps([str(tag), apk_name])
    # return json.dumps(["1", "a2dp.Vol_133"])


@app.route("/listFolders", methods=['GET', 'POST'])
def listFolders():
    path = (dirNow).split('/code')[0]+'/main-folder/outputs'
    pathList = list(set(os.listdir(path)))
    pathList.remove("IC3_fail.txt")
    pathList.remove("log.txt")
    pathList = json.dumps(pathList)
    return pathList

@app.route("/listActs", methods=['GET', 'POST'])
def listActs():
    actList = []
    path = (dirNow) + '/static/actJS'
    pathList = list(set(os.listdir(path)))
    for p in pathList:
        actList.append(p.split('Utg.js')[0])
    actList = json.dumps(actList)
    return actList

@app.route('/postmethod', methods = ['POST'])
def get_post_javascript_data():
    jsdata = request.form['javascript_data']
    print(jsdata)
    filepath = os.path.join((dirNow).split('/code')[0]+'/main-folder/outputs', jsdata)
    print filepath
    if os.path.isfile(filepath):
        os.remove(filepath)
        # print filepath+" removed!"
    elif os.path.isdir(filepath):
        shutil.rmtree(filepath, True)
    return jsdata

def deleteF(path):
    if os.path.isfile(path):
        os.remove(path)
        # print filepath+" removed!"
    elif os.path.isdir(path):
        shutil.rmtree(path, True)

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

def rewrite2(apk1, apk2, dataJsPath):
    file_data = ''
    with open(dataJsPath, "r") as f:
        for line in f:
            if line.find(apk1) != -1:
                name = apk1
            elif line.find(apk2) != -1:
                name = apk2
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

@app.route('/postoutput', methods = ['POST'])
def get_post_data():
    jsdata = request.form['javascript_data']
    print(jsdata)
    jsPath = (dirNow) + '/static/js'
    screenshotsPath = (dirNow) + '/static/screenshots'
    for item in os.listdir(screenshotsPath):
        deleteF(os.path.join(screenshotsPath, item))

    iconPath = (dirNow) + '/static/icon'
    for item in os.listdir(iconPath):
        deleteF(os.path.join(iconPath, item))
    app_infoPath = (dirNow) + '/static/js/app_info.js'
    deleteF(app_infoPath)
    dataJsPath = (dirNow) + '/static/js/data.js'
    deleteF(dataJsPath)
    # filepath = os.path.join(os.path.dirname(__file__) + '/upload/' + jsdata)
    outputsPath = (dirNow).split('/code')[0]+'/main-folder/outputs'
    source_screenshotsPath = os.path.join(outputsPath, jsdata, 'screenshots')
    for item in os.listdir(source_screenshotsPath):
        shutil.copy(os.path.join(source_screenshotsPath, item), (dirNow) + '/static/screenshots')

    source_iconPath = os.path.join(outputsPath, jsdata, 'icon')
    for item in os.listdir(source_iconPath):
        shutil.copy(os.path.join(source_iconPath, item), (dirNow) + '/static/icon')
    # shutil.copytree(source_iconPath, (os.path.dirname(__file__)) + '/static/')

    source_app_infoPath = os.path.join(outputsPath, jsdata, 'output', 'app_info.js')
    shutil.copy(source_app_infoPath, jsPath)

    source_dataJsPath = os.path.join(outputsPath, jsdata, 'output', 'data.js')
    shutil.copy(source_dataJsPath, jsPath)

    rewrite(jsdata, dataJsPath)

    reWriteJs(os.path.join(jsPath, 'data.js'), os.path.join(jsPath, 'utg.js'))
    print "okk"

    return jsdata


@app.route('/postoutputAct', methods = ['POST'])
def get_post_dataAct():
    jsdata = request.form['javascript_data']
    print(jsdata)
    jsPath = (dirNow) + '/static/js'
    newPath = (dirNow) + '/static/actJS'

    f = open(os.path.join(newPath, jsdata + 'Utg.js'), "r")
    txtAll = f.read()

    with open(os.path.join(jsPath, 'mainUtg.js'), 'w') as can:
        can.write(txtAll)

    return jsdata



@app.route('/postoutput2', methods = ['POST'])
def get_post_data2():
    jsdata = request.form['javascript_data']
    print(jsdata)
    apk1 = jsdata.split('+')[0]
    apk2 = jsdata.split('+')[1]
    jsPath = (dirNow) + '/static/js'
    screenshotsPath = (dirNow) + '/static/screenshots'
    for item in os.listdir(screenshotsPath):
        deleteF(os.path.join(screenshotsPath, item))

    iconPath = (dirNow) + '/static/icon'
    for item in os.listdir(iconPath):
        deleteF(os.path.join(iconPath, item))
    app_infoPath = (dirNow) + '/static/js/app_info.js'
    deleteF(app_infoPath)
    dataJsPath = (dirNow) + '/static/js/data.js'
    deleteF(dataJsPath)

    # filepath = os.path.join(os.path.dirname(__file__) + '/upload/' + jsdata)
    outputsPath = (dirNow).split('/code')[0]+'/main-folder/outputs'
    source_screenshotsPath = os.path.join(outputsPath, apk1, 'screenshots')
    for item in os.listdir(source_screenshotsPath):
        shutil.copy(os.path.join(source_screenshotsPath, item), (dirNow) + '/static/screenshots')
    source_screenshotsPath = os.path.join(outputsPath, apk2, 'screenshots')
    for item in os.listdir(source_screenshotsPath):
        shutil.copy(os.path.join(source_screenshotsPath, item), (dirNow) + '/static/screenshots')

    source_iconPath = os.path.join(outputsPath, apk1, 'icon')
    for item in os.listdir(source_iconPath):
        shutil.copy(os.path.join(source_iconPath, item), (dirNow) + '/static/icon')
    source_iconPath = os.path.join(outputsPath, apk2, 'icon')
    for item in os.listdir(source_iconPath):
        shutil.copy(os.path.join(source_iconPath, item), (dirNow) + '/static/icon')
    # shutil.copytree(source_iconPath, (os.path.dirname(__file__)) + '/static/')

    source_app_infoPath = os.path.join(outputsPath, apk1, 'output', 'app_info.js')
    # shutil.copy(source_app_infoPath, jsPath)

    f = open(source_app_infoPath, "r")
    txtAll = f.read()

    f2 = open(os.path.join(outputsPath, apk2, 'output', 'app_info.js'), "r")
    txtAll = txtAll + "\n" + f2.read().replace('app_info', 'app_info2')
    with open(os.path.join(jsPath, 'app_info.js'), 'w') as can:
        can.write(txtAll)

    f3 = open(os.path.join(outputsPath, apk1, 'output', 'data.js'), "r")
    lines = f3.readlines()
    dataAll = ''
    for i in range(0, len(lines) - 1):
        dataAll = dataAll + lines[i]
    f4 = open(os.path.join(outputsPath, apk2, 'output', 'data.js'), "r")
    lines = f4.readlines()
    for i in range(1, len(lines)):
        dataAll = dataAll + lines[i]
    with open(os.path.join(jsPath, 'data.js'), 'w') as can2:
        can2.write(dataAll)

    rewrite2(apk1, apk2, dataJsPath)

    deleteF((dirNow) + '/static/upload/data.js')
    source_app_infoPath = os.path.join(outputsPath, apk1, 'output', 'data.js')
    shutil.copy(source_app_infoPath, (dirNow) + '/static/upload')

    uploadJsPath = (dirNow) + '/static/upload'
    rewrite(apk1, os.path.join(uploadJsPath, 'data.js'))
    reWriteJs(os.path.join(uploadJsPath, 'data.js'), os.path.join(jsPath, 'utg.js'))

    deleteF((dirNow) + '/static/upload/data.js')
    source_app_infoPath = os.path.join(outputsPath, apk2, 'output', 'data.js')
    shutil.copy(source_app_infoPath, (dirNow) + '/static/upload')
    rewrite(apk2, os.path.join(uploadJsPath, 'data.js'))
    reWriteJs2(os.path.join(uploadJsPath, 'data.js'), os.path.join(jsPath, 'utg2.js'))

    # reWriteJs2(os.path.join(jsPath, 'data.js'), os.path.join(jsPath, 'utg.js'), apk1, apk2)

    # reWriteJs2(os.path.join(outputsPath, apk2, 'output', 'data.js'), os.path.join(jsPath, 'utg2.js'), apk2)
    print "okk"

    return jsdata

# web 服务器
if __name__ == '__main__':
    app.run()
