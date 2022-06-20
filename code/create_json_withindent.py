import json
import collections, os, commands
import decimal
import xml.etree.ElementTree as ET


callback = ["onCreate", "onStart", "onResume", "onPause", "onStop", "onDestroy",
            "onSaveInstanceState","onRestoreInstanceState","onActivityResult","onKeyDown",
            "onAttach", "onCreateView","onActivityCreated", "onDestroyView", "onDetach"]

class Method_tree():
    def __init__(self, name=None, code=None):
        self.name = name
        self.code = code
        self.children = []
    def set_root(self, name, code):
        self.name = name
        self.code = code
    def add_method(self, method_node):
        self.children.append(method_node)
    def search_method(self,name):
        if self.name == name:
            return self
        for child in self.children:
            tmp = child.search_method(name)
            if tmp is not None:
                return tmp
        return None

class archive_json(object):
    def from_json(self, json_str):
        obj = json.loads(json_str)
        for key in vars(self):
            if key in obj:
                setattr(self, key, obj[key])

    def to_json(self):
        return json.dumps(vars(self))

class Json_tree(archive_json):
    def __init__(self, source=None, sourceimg=None, source_fullname=None, source_actcode=None,
                 methodSourceLink=None, source_layoutcode=None,
                 target=None, targetimg= None, target_fullname=None, target_actcode=None,
                 target_layoutcode=None, methodTargetLink=None,
                 type="suit"):
        self.source = source
        self.sourceimg = sourceimg
        self.source_fullname = source_fullname
        self.source_actcode = source_actcode
        self.source_layoutcode = source_layoutcode
        self.methodSourceLink = methodSourceLink
        self.target = target
        self.targetimg = targetimg
        self.target_fullname = target_fullname
        self.target_actcode = target_actcode
        self.target_layoutcode = target_layoutcode
        self.methodTargetLink = methodTargetLink
        self.type = "suit"

def getActRelatedMethods(act_fullname, input_dir):
    #pkg = open(input_dir+'pkg_name.txt').readline().strip()
    cg_file = input_dir + input_dir.split('/')[-2] + '_cgs.txt'
    if not os.path.exists(cg_file):
        return {}
    lines = open(cg_file,'rb').readlines()
    d = collections.defaultdict(list)
    for line in lines:
        tmp = line.strip().split('-->')
        if act_fullname in tmp[0] and act_fullname in tmp[1] and not "void <clinit>" in tmp[0] and not "void <clinit>" in tmp[1] \
                and not 'void <init>' in tmp[0] and not "void <init>" in tmp[1]:
            d[tmp[0].split('(')[0].split(' ')[-1]].append(tmp[1].strip('\n').split('(')[0].split(' ')[-1])
    return d

def getMtdAttrib(node, input_dir):
    file = input_dir + 'methods/' + node.split(':')[0]+'.'+node.split(':')[1].split('(')[0].split(' ')[-1]+'.txt'
    try:
        lines = open(file,'rb').readlines()
    except IOError:
        return ''
    methodcode = ''
    for line in lines:
        if not line.strip().startswith('import'):
            methodcode = methodcode + line
    return methodcode

def getMethodTree(act_fullname, input_dir):
    calls_dict = getActRelatedMethods(act_fullname, input_dir)
    # find nodes that is directly related to current activity, e.g., onCreate, onStart
    firstLayer_nodes = []
    for caller in calls_dict.keys():
            if caller in callback:
                firstLayer_nodes.append(caller)
    # append node to activity name first
    for node in firstLayer_nodes:
        #a = act_fullname.split('.')[-1]
        calls_dict["ACTIVITY"].append(node)

    methodcalllist = []
    for caller,callees in calls_dict.items():
        for callee in callees:
            d = {}
            d['source'] = caller
            d['target'] = callee
            methodcalllist.append(d)
    return methodcalllist

def getActDic(icc_file):
    d = collections.defaultdict(set)
    lines = open(icc_file, 'rb').readlines()
    for line in lines:
        line = line.strip('\n').split('-->')
        if '$' in line[0]:
            line[0] = line[0].split('$')[0]
        if '$' in line[1]:
            line[1] = line[1].split('$')[0]
        d[line[0]].add(line[1])
    return d

def indent(elem, level=0):
    i = "\n" + level*"\t"
    if len(elem):
        if not elem.text or not elem.text.strip():
            elem.text = i + "\t"
        if not elem.tail or not elem.tail.strip():
            elem.tail = i
        for elem in elem:
            indent(elem, level+1)
        if not elem.tail or not elem.tail.strip():
            elem.tail = i
    else:
        if level and (not elem.tail or not elem.tail.strip()):
            elem.tail = i

def getAttrib(act, input_dir):
    name = act.split('.')[-1]
    fullname = act
    #img = input_dir + 'activity_img/' + act + '.png'
    img = input_dir + '/screenshots/' + act + '.png'
    code_file = input_dir + 'activity/' + name + '.java'
    code = ''
    if os.path.exists(code_file):
        for line in open(code_file, 'rb').readlines():
            if not line.strip().startswith('import') and not line.strip() == '' and not line.strip().startswith('package'):
                if '<' in line.strip():
                    code = code + line.replace('<', '&lt;')
                elif '>' in line.strip():
                    code = code + line.replace('>','&gt;')
                else:
                    code = code + line
    layout = ''
    layout_file = input_dir + 'layouts/' + act + '.xml'
    # if os.path.exists(layout_file):
    #     for line in open(layout_file, 'rb').readlines():
    #         if not line.strip() == '' and not line.strip().startswith('*') and not line.strip().startswith('~')\
    #                 and not line.strip().startswith('<!--') and not line.strip().startswith('-->') \
    #                 and not line.strip().startswith('/*'):
    #             if '<' in line.strip():
    #                 layout = layout + line.replace('<', '&lt;')
    #             elif '>' in line.strip():
    #                 layout = layout + line.replace('>','&gt;')
    #             else:
    #                 layout = layout + line
    if os.path.exists(layout_file):
        parser = ET.parse(layout_file)
        root = parser.getroot()
        indent(root)
        #print ET.tostring(root)
    else:
        root = ET.Element('root')

    return name,fullname,img,code,ET.tostring(root)

def serialize_instance(obj):
    d = {}
    d.update(vars(obj))
    return d

def write_to_appinfo_jsonfile(folder):
    foldername = os.path.dirname(folder).split('/')[-1]
    pkg = foldername.split('_')[0]
    version = foldername.split('_')[-1]
    cmd = 'cat %s | wc -l'%os.path.join(folder,'all_activities.txt')
    act_number = commands.getoutput(cmd).split('\n')[0]
    f = os.path.join(folder, 'output/app_info.js')
    open(f,'w').write('')
    writer = open(f, 'a')
    writer.write('var app_info   = {' + '\n')
    writer.write('    package_name: "%s",\n'%pkg)
    writer.write('    app_version: "%s",\n' %version)
    writer.write('    act_number: %s\n}'%act_number)

def execute(result_apkfolder):
    #input_dir = '/home/fanlingling/Dropbox/CS00/CaseForShowing_new/org.connectbot_19100/'
    write_to_appinfo_jsonfile(result_apkfolder)

    input_dir = result_apkfolder
    output_folder = os.path.join(result_apkfolder, 'output')
    if not os.path.exists(output_folder):
        os.mkdir(output_folder)
    out_json = os.path.join(output_folder, 'data.js')
    with open(out_json,"w") as f:
        f.write('var links = [' + '\n')
        #f.write(json.dumps(tree, default=serialize_instance))
    icc_file = input_dir + '/' + input_dir.split('/')[-2] + '_atgs.txt'
    # activitiy fullname dict
    d = getActDic(icc_file)

    # add app icon --> launcher
    launcher = open(input_dir + 'launcher.txt', 'rb').readline().strip('\n')
    if launcher == '':
        with open(out_json,"a") as f:
            f.write('];' + '\n')
        return
    #sourceimg = input_dir + 'icon/icon.png'
    #sourceimg = './org.connectbot_19100/icon/icon.png'
    sourceimg = input_dir + '/icon/ic_launcher.png'
    target, target_fullname, targetimg, target_actcode, target_layoutcode = getAttrib(launcher, input_dir)
    methodTargetLink = getMethodTree(launcher, input_dir)
    tree = Json_tree(source="APP", sourceimg=sourceimg,target=target, target_fullname=target_fullname,
                     targetimg=targetimg, target_actcode=target_actcode, target_layoutcode=target_layoutcode,
                     methodTargetLink=methodTargetLink)
    with open(out_json, "a") as f:
        f.write(json.dumps(tree, default=serialize_instance,indent=4))
        f.write(',' + '\n')
    # add ATG
    for k,vs in d.items():
        methodSourceLink = getMethodTree(k, input_dir)
        source, source_fullname, sourceimg, source_actcode, source_layoutcode = getAttrib(k, input_dir)
        methodSourceLink = getMethodTree(k, input_dir)
        for v in vs:
            target, target_fullname, targetimg, target_actcode, target_layoutcode = getAttrib(v, input_dir)
            methodTargetLink = getMethodTree(v, input_dir)
            tree = Json_tree(source=source, source_fullname=source_fullname, sourceimg=sourceimg,
                             source_actcode=source_actcode, source_layoutcode=source_layoutcode,
                             methodSourceLink=methodSourceLink,
                             target=target, target_fullname=target_fullname, targetimg=targetimg,
                             target_actcode=target_actcode, target_layoutcode=target_layoutcode,
                             methodTargetLink=methodTargetLink)
            with open(out_json, "a") as f:
                f.write(json.dumps(tree, default=serialize_instance, indent=4))
                f.write(','+'\n')

    with open(out_json,"a") as f:
        f.write('];' + '\n')

#execute('/home/fanlingling/a2dp.Vol_135/')