import os, commands, sys, time
from collections import defaultdict
import device

adb = 'adb'
result_folder = ''
defined_pkg_name = ''
defined_app_name = ''
used_pkg_name = ''

def get_pkgname(apk_path):
    global defined_pkg_name
    global used_pkg_name
    defined_pkg_name = commands.getoutput(
        'aapt dump badging %s | grep package | awk \'{print $2}\' | sed s/name=//g | sed s/\\\'//g' % (apk_path))
    launcher = commands.getoutput(r"aapt dump badging " + apk_path + " | grep launchable-activity | awk '{print $2}'")
    # sometimes launcher is empty or launcher starts with "."
    if launcher == '' or defined_pkg_name in launcher or launcher.startswith("."):
        used_pkg_name = defined_pkg_name
    else:
        used_pkg_name = launcher.replace('.' + launcher.split('.')[-1], '').split('\'')[1]

def init_d(activity, d):
    d[activity] = {}
    d[activity]['actions'] = ''
    d[activity]['category'] = ''

    return d

def extract_activity_action(path):
    # {activity1: {actions: action1, category: cate1}}
    # new format: {activity: [[action1, category1],[action2, category2]]}
    d = {}
    flag = 0
    for line in open(path,'rb').readlines():
        line = line.strip()
        if line.startswith('<activity'):
            activity = line.split('android:name="')[1].split('"')[0]

            if activity.startswith('.'):
                activity = used_pkg_name + activity

            if not activity in d.keys() and used_pkg_name in activity:
                #d = init_d(activity, d) # some activities may have different actions and categories
                d[activity] = []
                flag = 1

            if line.endswith('/>'): # if activity ends in one line, it has no actions, we only record its activity name.
                flag = 0
                continue

        elif line.startswith('<intent-filter') and flag == 1:
            flag = 2
            action_category_pair = ['','']
        elif line.startswith('<action') and flag == 2:
            action = line.split('android:name="')[1].split('"')[0]
            action_category_pair[0] = action
        elif line.startswith('<category') and flag == 2:
            category = line.split('android:name="')[1].split('"')[0]
            action_category_pair[1] = category
        elif line.startswith('</intent-filter>') and flag == 2:
            flag = 1
            if not action_category_pair[0] == '' or not action_category_pair[1] == '':
                d[activity].append(action_category_pair)
        elif line.startswith('</activity>'):
            flag = 0
        else: continue

    return d

def get_full_activity(component):
    '''get activity name, component may have two forms:
            1. com.google.abc/com.google.abc.mainactivity
            2. com.google.abc/.mainactivity

    RETURE com.google.abc.mainactivity
    '''
    act = component.split('/')[1]
    if act.startswith('.'):
        activity = component.split('/')[0] + act
    else:
        activity = act
    return activity

'''
Add for the revised version of TSE paper
'''
def explore_actionable_widgets(activity, actionable_widgets, appname):
    # actionable_widgets dict: {resource-id: [android.widget.Button, clickpointx, checkpointy]}
    pair = {}
    for res_id, widget_type in actionable_widgets.items():
        #device(className='android.widget.Button', resourceId='a2dp.Vol:id/Button01')
        #device(className=widget_type, resourceId=res_id)

        #print 'is clicking %s %s %s'%(widget_type[0], widget_type[1], widget_type[2])
        clickcmd = adb + ' shell input tap %d %d'%(widget_type[1], widget_type[2])
        os.system(clickcmd)
        time.sleep(3)

        cmd = adb + " shell dumpsys activity activities | grep mResumedActivity"
        cmdd = adb + " shell dumpsys activity activities | grep mFocusedActivity"

        current_acticity = ''
        return_content = commands.getoutput(cmd)
        if '/.' in return_content:
            current_acticity = return_content.split('/.')[1].split(' ')[0]
        else:
            current_acticity = return_content.split('.')[-1].split(' ')[0]
        if current_acticity == '':
            return_content = commands.getoutput(cmdd)
            if '/.' in return_content:
                current_acticity = return_content.split('/.')[1].split(' ')[0]
            else:
                current_acticity = return_content.split('.')[-1].split(' ')[0]

        if current_acticity in activity or "Launcher" in current_acticity:
            # the click dose not change the UI page
            continue

        else:
            ## record pair, also can add more info
            pair[activity] = '->' + widget_type[0] + '->' + defined_pkg_name + '.' + current_acticity

            ## adb back
            adb_back = adb + " shell input keyevent 4"
            commands.getoutput(adb_back)
            time.sleep(3)

            ###write to file
            #dynamic_explore_widget = os.path.join(result_folder, used_pkg_name + '_widget_dynamic.txt')
            #open(dynamic_explore_widget, 'w').write()

    ###write to file
    dynamic_explore_pairs = os.path.join(result_folder, appname + '_atgs_dynamic.txt')
    for org_act, new_act in pair.iteritems():
        #print org_act, 'corresponds to', pair[org_act]
        open(dynamic_explore_pairs,'a').write(org_act + pair[org_act] + '\n')

def explore(activity, appname):

    act, current_status, actionable_widgets = device.check_current_screen(activity, defined_pkg_name, result_folder)

    if current_status == 'abnormal':
        # click home and click 'ok' if crashes (two kinds of 'ok's)
        os.system(device.adb + ' shell input tap 540 1855')
        time.sleep(1)
        os.system(device.adb + ' shell input tap 899 1005')
        time.sleep(1)
        os.system(device.adb + ' shell input tap 163 1060')
        time.sleep(1)
        return
    if current_status == 'normal':
        ## screencap!!!!!!!!!!!
        device.take_screenshot(activity, appname)

        ## explore the actionable widgets
        if actionable_widgets != "empty":
            explore_actionable_widgets(activity, actionable_widgets, appname)

        #click home
        os.system(device.adb + ' shell input tap 540 1855')
        time.sleep(1)
        return 'normal'

def startAct(component, action, cate, appname, act_paras_file):
    ###component is from manifest rather than the pare file
    device.clean_logcat()
    cmd = device.adb + ' shell am start -S -n %s' % component
    if not action == '':
        cmd = cmd + ' -a ' + action
    if not cate == '':
        cmd = cmd + ' -c ' + cate

    activity = get_full_activity(component)

    extras = get_act_extra_paras(activity, act_paras_file)

    if extras != None:
        cmd = cmd + ' ' + extras
    #print 'Act cmd: ' + cmd

    os.system(cmd)
    time.sleep(3)

    return explore(activity, appname)

def parseManifest(appname):

    print "========== Parsing manifest file of '%s.apk' ==========" % appname
    decompile_Path = os.path.join(result_folder,'decompile')
    if not os.path.exists(decompile_Path):
        print "cannot find the decompiled app: " + appname
        return

    manifestPath = os.path.join(decompile_Path, "AndroidManifest.xml")

    # format of pairs: {activity1: {actions: action1, category: cate1 }} -----discard
    # new format: {activity: [[action1, category1],[action2, category2]]}
    ##get all activity and their attributes
    pairs = extract_activity_action(manifestPath)

    return pairs


def convert(api, key, extras):
    if api == 'getString' or api == 'getStringArray':
        extras = extras + ' --es ' + key + ' test'
    if api == 'getInt' or api == 'getIntArray':
        extras = extras + ' --ei ' + key + ' 1'
    if api == 'getBoolean' or api == 'getBooleanArray':
        extras = extras + ' --ez ' + key + ' False'
    if api == 'getFloat' or api == 'getFloatArray':
        extras = extras + ' --ef ' + key + ' 0.1'
    if api == 'getLong' or api == 'getLongArray':
        extras = extras + ' --el ' + key + ' 1'
    return extras


def get_act_extra_paras(activity, act_paras_file):
    if os.path.exists(act_paras_file):
        for line in open(act_paras_file,'r').readlines():
            if line.strip() == '':
                continue
            if line.split(":")[0] == activity:
                if line.split(":")[1].strip() == '':
                    return ''
                else:
                    paras = line.split(':')[1].strip()
                    extras = ''
                    for each_para in paras.split(';'):
                        if '__' in each_para:
                            # api may refer to getString, getInt, ....
                            api = each_para.split('__')[0]
                            key = each_para.split('__')[1]
                            extras = convert(api, key, extras)
                    return extras
    else:
        return ''


def exploreAct(apk_path, apk_name, apkresult_folder):

    global result_folder
    result_folder = apkresult_folder

    act_paras_file = os.path.join(apkresult_folder, 'activity_paras.txt')

    '''
    Install apk
    '''
    result = device.installAPP(apk_path, apk_name)

    if result == 'Failure':
        return

    '''
    Get package name
    '''
    get_pkgname(apk_path)

    '''
    Get all activity and their attributes such as action, category, and inter-filter
    '''
    pairs = parseManifest(apk_name)
    print "%s parsing fininshed!" % apk_name

    '''
    Format of pairs: {activity: [[action1, category1],[action2, category2]]}
    '''
    for activity, other in pairs.items():
        # This is the defined format of uiautomator
        component = defined_pkg_name + '/' + activity
        for s in other:
            action = s[0]
            category = s[1]

            # Go through all activities
            status = startAct(component, action, category, apk_name, act_paras_file)
            if status =='normal':
                break

        # Without action and category
        startAct(component, '', '', apk_name, act_paras_file)

    device.uninstallApp(defined_pkg_name)

    return pairs.keys()

# adb_back = adb + " shell input keyevent 4"
# commands.getoutput(adb_back)
# time.sleep(2)