# -*- coding: utf-8 -*-
import os, commands, csv, time
from scrapy.http.response import xml

import cv2
import get_widget_attributes

adb = 'adb'
result_folder = ''

def installAPP(apkPath, apkname):
    # macbook delete
    #if not commands.getoutput(adb + ' shell getprop sys.boot_completed') == '1':
    #    print commands.getoutput(adb + ' shell getprop sys.boot_completed')
    #    open('/home/chensen/break.txt')
    #print commands.getoutput(adb + ' shell getprop sys.boot_completed')
    cmd = adb + " install -r " + apkPath
    out = commands.getoutput(cmd)
    for o in out.split('\n'):
        if 'Failure' in o or 'Error' in o or 'No such file or directory' in o or 'adb: error:' in o:
            print 'install failure: %s.apk'%apkname
            #print out
            csv.writer(open('installError.csv','ab')).writerow((apkname, out.replace('\n', ', ')))
            return 'Failure'
    print 'Install Success'
    return 'Success'

def uninstallApp(package):
    cmd = adb + " uninstall " + package
    os.system(cmd)

def get_components(d_path, act):
    img = cv2.imread(os.path.join(d_path, act + '.png'))
    xml_path = os.path.join(result_folder, 'layouts', act + '.xml')
    components_path = os.path.join(result_folder, 'components', act)
    if not os.path.exists(components_path):
        os.makedirs(components_path)
    csv_path = os.path.join(result_folder, components_path, act + '.csv')
    get_widget_attributes.parse_xml(xml_path, csv_path)
    csv_reader = csv.reader(open(csv_path, 'r'))
    # next(csv_reader)
    for row in csv_reader:
        id_is_null = 0
        # print row
        if not row == '':
            bound = row[5]
            y0 = int(bound.split(',')[1].split(']')[0])
            y1 = int(bound.split(',')[2].split(']')[0])
            x0 = int(bound.split(',')[0].split('[')[1])
            x1 = int(bound.split(',')[1].split('[')[1])
            if row[3] == '' and row[4] == '':
                break
            if row[3] == '' and row[4] != '':
                component_name = str(id_is_null) + '_' + row[4].split('.')[-1]
                id_is_null = id_is_null + 1
            else:
                try:
                    component_name = row[3].split('/')[1] + '_' + row[4].split('.')[-1]
                except IndexError:
                    pass
            try:
                cropped = img[y0:y1, x0:x1]
                cv2.imwrite(os.path.join(components_path, component_name + '.png'), cropped)
            except TypeError:
                pass
            except UnboundLocalError:
                pass


def take_screenshot(act, appname):
    path = os.getcwd()
    print adb + ' shell screencap -p /sdcard/%s.png'%act
    os.system(adb + ' shell screencap -p /sdcard/%s.png'%act)
    d_path = os.path.join(result_folder, 'screenshots')
    if not os.path.exists(d_path):
        os.mkdir(d_path)
    os.chdir(d_path)
    os.system(adb + ' pull /sdcard/%s.png'%act)
    time.sleep(3)

    os.system(adb + ' shell rm /sdcard/%s.png'%act)

    '''I delele this function, 9.13.2020'''
    #get_components(d_path, act)
    os.chdir(path)

def check_focused_act(defined_pkg_name):
    cmd = adb + " shell dumpsys activity activities | grep mResumedActivity"
    cmdd = adb + " shell dumpsys activity activities | grep mFocusedActivity"
    o1 = commands.getoutput(cmd)
    o2 = commands.getoutput(cmdd)
    if not 'com.android.launcher3' in o1 and not 'com.android.launcher3' in o2:
        # mResumedActivity: ActivityRecord{b6968c9 u0 com.apkinstaller.ApkInstaller/.ui.Updater t470}
        act = ''
        if defined_pkg_name in o1:
            if o1.strip().split(defined_pkg_name+'/')[1].split('}')[0].startswith('.'):
                act = defined_pkg_name + o1.strip().split(defined_pkg_name+'/')[1].split('}')[0]
            else:
                act = o1.strip().split(defined_pkg_name+'/')[1].split('}')[0];
        if defined_pkg_name in o2:
            if o2.strip().split(defined_pkg_name+'/')[1].split('}')[0].startswith('.'):
                act = defined_pkg_name + o2.strip().split(defined_pkg_name+'/')[1].split('}')[0]
            else:
                act = o2.strip().split(defined_pkg_name+'/')[1].split('}')[0];
        if act == '':
            return None, 'abnormal'
        else: return act, 'normal'
    else:
        return None, 'abnormal'


def get_actionable_widgets(layout_path):
    actionable_widgets = {} ## id list
    xml = open(layout_path)
    line = xml.readline()
    lines = line.split('><node')
    for node in lines:
        if 'clickable="true"' in node:
            resid = node.split('resource-id="')[1].split('"')[0]
            if resid == '':
                resid = 'empty'
            if 'android.widget.Button' in node or 'android.widget.ImageButton' in node or 'android.widget.TextView' in node\
                    or 'android.widget.FrameLayout' in node:
                actionable_widgets[resid] = []
            bounds = node.split('bounds="')[1].split('"')[0]
            # bounds = [263,210][1080,328]
            clickpointx = 0
            clickpointy = 0
            if not bounds == '':
                x1 = bounds.split('[')[1].split(',')[0]
                y1 = bounds.split('][')[0].split(',')[1]
                x2 = bounds.split('][')[1].split(',')[0]
                y2 = bounds.split(',')[-1].split(']')[0]
                clickpointx = (int(x1)+int(x2))/2
                clickpointy = (int(y1)+int(y2))/2
            if 'android.widget.Button' in node:
                actionable_widgets[resid].append('android.widget.Button')
                actionable_widgets[resid].append(clickpointx)
                actionable_widgets[resid].append(clickpointy)
            if 'android.widget.ImageButton' in node:
                actionable_widgets[resid].append('android.widget.ImageButton')
                actionable_widgets[resid].append(clickpointx)
                actionable_widgets[resid].append(clickpointy)
            if 'android.widget.TextView' in node:
                actionable_widgets[resid].append('android.widget.TextView')
                actionable_widgets[resid].append(clickpointx)
                actionable_widgets[resid].append(clickpointy)
            if 'android.widget.FrameLayout' in node:
                actionable_widgets[resid].append('android.widget.FrameLayout')
                actionable_widgets[resid].append(clickpointx)
                actionable_widgets[resid].append(clickpointy)

    return actionable_widgets

def check_current_screen(activity, defined_pkg_name, apkresult_folder):

    ## add the parser of xml to get the actionable widgets

    global result_folder

    result_folder = apkresult_folder

    '''dump xml check whether it contains certain keywords:
        has stopped, isn't responding, keeps stopping, DENY, ALLOW
    '''
    keywords = ['has stopped', 'isn\'t responding', 'keeps stopping']
    '''dump xml and check'''
    layout_path = os.path.join(result_folder, 'layouts')
    if not os.path.exists(layout_path):
        os.makedirs(layout_path)
    os.system(adb + ' shell uiautomator dump /sdcard/%s.xml' % activity)
    pull_xml = adb + ' pull /sdcard/%s.xml %s' % (activity, layout_path)
    os.system(pull_xml)

    clean_xml = adb + ' shell rm /sdcard/%s.xml' % activity
    os.system(clean_xml)

    layout_path = os.path.join(layout_path, activity+'.xml')


    ## add for the revise version of TSE
    ## read the xml file and get the actionalbe widgets
    # actionable_widgets dict: {resource-id: [android.widget.Button, clickpointx, checkpointy]}

    if os.path.exists(layout_path):
        actionable_widgets = get_actionable_widgets(layout_path)
    else:
        actionable_widgets = "empty"

    # check whether it crashes
    for word in keywords:
        result = commands.getoutput('grep "%s" %s' %(word, layout_path))
        if not result == '':
            # if crash, remove xml from layout folder
            os.system('rm %s'%layout_path)
            return None, 'abnormal', actionable_widgets
    # check whether it is a permission dialog
    if not commands.getoutput('grep -i "ALLOW" %s' %(layout_path)) == '' and not commands.getoutput('grep -i "DENY" %s' %(layout_path)) == '':
        os.system(adb + ' shell input tap 782 1077') # tap ALLOW
        act, out = check_focused_act(defined_pkg_name)
        if out == 'abnormal':
            # if launcher, remove xml from layout folder
            os.system('rm %s' % layout_path)
            return None, 'abnormal', actionable_widgets
        return act, out, actionable_widgets

    act, out = check_focused_act(defined_pkg_name)
    if out == 'abnormal':
        # if launcher, remove xml from layout folder
        os.system('rm %s' % layout_path)
        return None, 'abnormal', actionable_widgets
    return act, out, actionable_widgets

def clean_logcat():
    cmd_clean = adb + ' logcat -c'
    commands.getoutput(cmd_clean)

def startAct(activity, action):
    clean_logcat()
    if action == '':
        cmd = adb + ' shell am start -n %s' %activity
    else:
        cmd = adb + ' shell am start -n %s -a %s' % (activity, action)

    os.system(cmd)
    time.sleep(3)

if __name__ == '__main__':
   #result_folder = '/home/senchen/Desktop/storydroid_plus/outputs/com.catchingnow.tinyclipboardmanager_51/'
   #get_components('/home/senchen/Desktop/storydroid_plus/outputs/com.catchingnow.tinyclipboardmanager_51/screenshots/', 'com.catchingnow.tinyclipboardmanager.ActivityMain')
   get_actionable_widgets('/home/senchen/Desktop/storydistiller/outputs/a2dp.Vol_133/layouts/a2dp.Vol.CustomIntentMaker.xml')
