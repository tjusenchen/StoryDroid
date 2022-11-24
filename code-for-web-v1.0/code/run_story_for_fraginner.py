# coding=utf-8
# Author Sen Chen and Lingling Fan

import os, commands, collections
import shutil

import traverse_tree
import get_act_method_code
import run_rpk_explore_apk
import create_json_withindent
import csv

launchActivity = ''
defined_pkg_name = ''
defined_app_name = ''
used_pkg_name = ''

toolPath = (os.getcwd()).split('/code')[0]
java_home_path = '/usr/local/jdk1.8'
sdk_platform_path = toolPath + '/main-folder/config/libs/android-platforms'
lib_home_path = toolPath + '/main-folder/config/libs/'
callbacks_path = toolPath + '/main-folder/config/AndroidCallbacks.txt'
jadx_path = toolPath + '/jadx-master/'
ic3_path = toolPath + '/IC3/'

# rename the app name
def rename(apk_path, apk_dir):
    global defined_pkg_name
    global used_pkg_name
    defined_pkg_name = commands.getoutput('aapt dump badging %s | grep package | awk \'{print $2}\' | sed s/name=//g | sed s/\\\'//g'%(apk_path))
    launcher = commands.getoutput(r"aapt dump badging " + apk_path + " | grep launchable-activity | awk '{print $2}'")
    # sometimes launcher is empty or launcher starts with "."
    if launcher == '' or defined_pkg_name in launcher or launcher.startswith("."):
        used_pkg_name = defined_pkg_name
    else:
        used_pkg_name = launcher.replace('.' + launcher.split('.')[-1], '').split('\'')[1]
    print 'rename pkg: ' + used_pkg_name

    version = commands.getoutput('aapt dump badging %s | grep versionName | awk \'{print $3}\' | sed s/versionCode=//g | sed s/\\\'//g'%(apk_path))
    os.system('mv %s %s'%(apk_path, apk_dir + used_pkg_name + '_' + version + '.apk')) # used_pkg_name_version.apk
    global  defined_app_name
    defined_app_name = defined_pkg_name + '_' + version
    return apk_dir + used_pkg_name + '_' + version + '.apk'

# get icon of the app
def get_icon(results_visual_icon):
    #icon_path = output + 'java_codes/' + apk_name + '/resources/res/mipmap-hdpi-v4/ic_launcher.png'
    '''need to check the path'''
    icon_path = output + 'java_code/' + apk_name + '/resources/res/drawable-xhdpi-v4/ic_launcher.png'
    if os.path.exists(icon_path):
        os.system('cp %s %s'%(icon_path, results_visual_icon))
    else:
        icon_path = output + 'java_code/' + apk_name + '/resources/res/drawable-xhdpi-v4/icon.png'
        os.system('cp %s %s' % (icon_path, results_visual_icon))

## decomile the apk and get the Java appstory
def decompile(apk_path, apk_name):
    global launchActivity
    #bin_path = output + 'jadx-master/build/jadx/bin/'
    bin_path = jadx_path + 'build/jadx/bin/'
    results_visual = output + 'outputs/' + apk_name + '/'
    results_visual_icon = results_visual + 'icon/'
    if not os.path.exists(results_visual):
        os.mkdir(results_visual)
    if not os.path.exists(results_visual_icon):
        os.mkdir(results_visual_icon)
    JavaCode_path = output + 'java_code/'
    if not os.path.exists(JavaCode_path):
        os.mkdir(JavaCode_path)
    results_JavaCode = JavaCode_path + apk_name + '/'
    if not os.path.exists(results_JavaCode):
        os.chdir(bin_path)
        os.system('./jadx -d %s %s' % (results_JavaCode, apk_path))

    # get icon
    get_icon(results_visual_icon)

    launchActivity = commands.getoutput('aapt dump badging %s | grep launchable-activity | awk \'{print $2}\' | sed s/name=//g | sed s/\\\'//g'%apk_path)
    if launchActivity == '':
        launchActivity = commands.getoutput('aapt dump badging %s | grep Activity | awk \'{print $2}\' | sed s/name=//g | sed s/\\\'//g'%apk_path)
    result_launcher = output + 'outputs/' + apk_name + '/launcher.txt'
    launcher_file = open(result_launcher, 'wb')
    launcher_file.write(launchActivity)

# execure the IC3
def run_IC3(apk_path):
    IC3_fail_file = output + 'outputs/IC3_fail.txt'
    results_IC3_dir = output + 'ic3_atgs/'
    if not os.path.exists(results_IC3_dir):
        os.makedirs(results_IC3_dir)
    results_IC3 = results_IC3_dir + apk_name + '.txt'
    if os.path.exists(results_IC3):
        return results_IC3
    open(results_IC3,'wb').write('')
    #IC3_home = output + 'IC3/ic3-0.2.0/'
    IC3_home = ic3_path + 'ic3-0.2.0/'
    IC3_jar = IC3_home + 'ic3-0.2.0-full.jar'
    IC3_android_jar = IC3_home + 'android.jar'
    open(IC3_fail_file, 'wb').write('')
    fail_writer = open(IC3_fail_file, 'ab')
    os.chdir(IC3_home)
    if (os.system('timeout 10m java -Xmx4g -jar %s -apkormanifest %s -in %s -cp %s -protobuf %s | grep "PATH: "'
                      % (IC3_jar, apk_path, sootOutput_dir, IC3_android_jar, results_IC3_dir))) != 0:
        fail_writer.write(apk_path + '\n')

    #if os.path.exists(results_IC3):
        # rename the ic3 result file, because the pkg and real pkg are inconsistant.
        #os.system('mv %s %s' % (results_IC3, results_IC3_dir + apk_name + '.txt'))
    #print results_IC3_dir + used_pkg_name + '.txt'
    #os.system('mv %s %s' % (results_IC3_dir + used_pkg_name + '.txt', results_IC3))

    return results_IC3

# parse the results of IC3
def parse_IC3(file, pkg):
    dict = {}
    f = open(file, 'rb')
    line = f.readline()
    flag = -1
    s = 0 # indicate component
    brace = 0 # indicate the number of braces
    while line:
        if '{' in line:
            brace += 1
        if '}' in line:
            brace -= 1
        if 'components {' in line:
            s = 1
            flag = -1
            tmp =''
            brace = 1
        elif s == 1 and 'name:' in line:
            tmp = line.split(': "')[1].split('"')[0]
            s = 2
        elif s == 2 and 'kind: ACTIVITY' in line:
            flag = 0
            sourceActivity = tmp
            s = 3
        elif flag == 0 and "exit_points" in line:
            flag = 1
        elif flag == 1 and 'statement' in line:
            stm = line.split(': "')[1].split('"')[0]
            flag = 2
        elif flag == 2 and 'method: "' in line:
            mtd = line.split(': "<')[1].split('>"')[0]
            flag = 3
        elif flag == 3 and 'kind: ' in line:
            if 'kind: ACTIVITY' in line:
                flag = 4
            else:
                flag = 0
        elif flag == 4 and 'kind: CLASS' in line:
            flag = 5
        elif flag == 5 and 'value' in line:
            if ': "L' in line:
                targetActivity = line.strip().split(': "L')[1].split(';"')[0].replace('/', '.')
                if targetActivity.endswith('"'):
                    targetActivity = targetActivity.split('"')[0]
            else:
                targetActivity = line.strip().split(': "')[1].split(';"')[0].replace('/', '.')
                if targetActivity.endswith('"'):
                    targetActivity = targetActivity.split('"')[0]
            if not pkg in targetActivity:
                flag = 0
                continue
            if not sourceActivity in dict.keys():
                dict[sourceActivity] = set()
            dict[sourceActivity].add(targetActivity)
            flag = 4
        if brace == 1 and s == 3: # in component, find more exit_points
            flag = 0
        line = f.readline()
    # for k,v in dict.items():
    #     for v1 in v:
    #         print k + '->' + v1
    return dict

# save the parsed results of IC3
def save_parsed_IC3(dict):
    results_parseIC3_dir = output + 'parsed_ic3/'
    if not os.path.exists(results_parseIC3_dir):
        os.makedirs(results_parseIC3_dir)
    # if not os.path.exists(output + 'parsed_ic3/' + apk_name + '.txt'):
    #     open(results_parseIC3_dir + apk_name + '.txt', 'wb').write('')
    #     return
    open(results_parseIC3_dir + apk_name + '.txt', 'wb').write('')
    for k, v in dict.items():
        for v1 in v:
            open(results_parseIC3_dir + apk_name + '.txt', 'ab').write(k + '-->' + v1 + '\n')

# get call graphs of the app
def get_callgraphs(apk_path):
    results_CG_dir = output + 'soot_cgs/'
    CG_jar = output + 'config/CGGenerator.jar'
    os.chdir(output)
    os.system('java -Xmx4g -jar %s %s %s %s %s' % (CG_jar, apk_path, results_CG_dir, sdk_platform_path, callbacks_path))

# parse the results of call graphs
def parse_CG(cg_file, pkg_name):
    if not os.path.exists(output + 'soot_cgs/' + apk_name + '.txt'):
        return
    dict = collections.defaultdict(set)
    f = open(cg_file, 'rb')
    line = f.readline()
    while line:
        print line
        key = line.split(' in <')[1].split('> ==> <')[0]
        value = line.split('> ==> <')[1][0:-1]
        if pkg_name in key and pkg_name in value and not 'EmmaInstrument' in key and not 'EmmaInstrument' in value:
            dict[key].add(value[:-1])
        line = f.readline()
    #print '[6] Parse CG: DONE'
    return dict

# save the parsed results of call graphs
def save_parsed_CG(dict):
    results_parsedCG_dir = output + 'parsed_cgs/'
    if not os.path.exists(results_parsedCG_dir):
        os.makedirs(results_parsedCG_dir)
    if not os.path.exists(output + 'soot_cgs/' + apk_name + '.txt'):
        #open(results_parseCG_dir + apk_name + '.txt', 'wb')
        return
    saved_parseCG = open(results_parsedCG_dir + apk_name + '.txt', 'wb')
    processed_cg_file = output + 'outputs/' + apk_name + '/' + apk_name + '_cgs.txt'
    saved_parseCG_visulization = open(processed_cg_file, 'wb')
    for k, v in dict.items():
        for v1 in v:
            saved_parseCG.write(k + '-->' + v1 + '\n')
    for k, v in dict.items():
        for v1 in v:
            saved_parseCG_visulization.write(k + '-->' + v1 + '\n')

# execute our enhanced tool
def run_soot(output, apk_path, pkg_name, apk_name):
    results_enhancedIC3 = output + 'storydroid_atgs/' + apk_name + '.txt'
    if os.path.exists(results_enhancedIC3):
        return
    enhancedIC3_jar = output + 'config/run_soot_study.jar'
    os.chdir(output)
    os.system('java -jar %s %s %s %s %s %s %s' % (enhancedIC3_jar, output, apk_path, pkg_name, java_home_path, sdk_platform_path, lib_home_path))

# get the results of transitions
def get_atgs(apk_name):
    results_parseIC3 = output + 'parsed_ic3/' + apk_name + '.txt'
    results_enhancedIC3 = output + 'storydroid_atgs/' + apk_name + '.txt'
    if not os.path.exists(results_enhancedIC3):
        open(results_enhancedIC3, 'wb').write('')
    ICC_path = output + 'atgs/'
    if not os.path.exists(ICC_path):
        os.makedirs(ICC_path)
    results_ICCs = output + 'atgs/' + apk_name + '.txt'
    results_visulization_ICCs = output + 'outputs/' + apk_name + '/' + apk_name + '_atgs.txt'
    file_parseIC3 = open(results_parseIC3, 'rb')
    ICCs = set()
    for line in file_parseIC3.readlines():
        ICCs.add(line)
    file_enhancedIC3 = open(results_enhancedIC3, 'rb')
    for line in file_enhancedIC3.readlines():
        ICCs.add(line)
    file_ICCs = open(results_ICCs, 'wb')
    for ICC in ICCs:
        file_ICCs.write(ICC)
    file_ICCs_visulization = open(results_visulization_ICCs, 'wb')
    for ICC in ICCs:
        file_ICCs_visulization.write(ICC)

def copy_search_file(srcDir, desDir):
    if not os.path.exists(desDir):
        os.mkdir(desDir)
    for line in os.listdir(srcDir):
        filePath = os.path.join(srcDir, line)
        if os.path.isdir(filePath):
            os.system('cp -r %s %s'%(filePath, desDir))
        else:
            os.system('cp %s %s' % (filePath, desDir))

# def get_act_not_in_atg(all_acts):
#     act_file = os.path.join(result_apkfolder, apk_name+'_atgs.txt')
#     acts_in_atg = set()
#     if len(acts_in_atg) == 0:
#         return 0
#     else:
#         for line in open(act_file,'r').readlines():
#             act = line.strip().split('-->')
#             acts_in_atg.add(act[0])
#             acts_in_atg.add(act[1])
#
#         retD = list(set(all_acts).difference(set(acts_in_atg)))
#         return retD

# def get_acy_not_launched(all_acts):
#     screenshots_dir = os.path.join(result_apkfolder,'screenshots/')
#     launched_act = []
#     if len(os.listdir(screenshots_dir)) == 0:
#         return 0
#     else:
#         for png in os.listdir(screenshots_dir):
#             act_name = png.split('.png')[0]
#             launched_act.append(act_name)
#         retD = list(set(all_acts).difference(set(launched_act)))
#         return retD


def parse(dir, results_visulization_ICCs, dynamic_explore_result):
    static_list = []
    dynamic_list = []
    if os.path.exists(results_visulization_ICCs):
        for line in open(results_visulization_ICCs,'r').readlines():
            static_list.append(line)
    if os.path.exists(dynamic_explore_result):
        for line in open(dynamic_explore_result,'r').readlines():
            line = line.split('->')[0] + '-->' + line.split('->')[2]
            dynamic_list.append(line)

        #union_list = static_list + dynamic_list
        union_list = list(set(static_list).union(set(dynamic_list)))
        union_file = open(dir + 'all_atgs.txt','ab')
        for pair in union_list:
            union_file.write(pair)
        #in dynamic but not in static
        new_unique_list = list(set(dynamic_list).difference(set(static_list)))
        #in dynmaic and static
        new_edge_list = list(set(static_list).intersection(set(dynamic_list)))

        return  union_list, static_list, new_unique_list, new_edge_list

def getSootOutput(apk_path, apk_name):
    # print sootOutput_dir
    sootOutput_jar = output + '/config/getSootOutput.jar'
    # print 'java -jar %s %s %s %s %s' % (sootOutput_jar, sootOutput_dir, apk_name, output, apk_path)
    os.chdir(output + '/config/')
    os.system('java -jar %s %s %s %s %s' % (sootOutput_jar, sootOutput_dir, apk_name, output, apk_path))
    print '[3] Get SootOutput and Check Layout Type: DONE'
# main method
if __name__ == '__main__':

    #output = sys.argv[1] # home folder
    output = '/home/senchen/Desktop/storydistiller/'
    # adb = sys.argv[2] # adb emulator
    adb = 'adb '

    apk_dir = os.path.join(output, 'apks/') # apk folder

    out_csv = os.path.join(output, 'log.csv') # log file
    csv.writer(open(out_csv, 'a')).writerow(('apk_name', 'pkg_name', 'all_act_num', 'launched_act_num',
                                             'act_not_in_atg', 'act_not_launched', 'all_atg', 'soot_atg', 'new_atg'))

    # print 'All atgs: ' + str(len(union_list))
    # print union_list
    # print 'Soot atgs: ' + str(len(static_list))
    # print static_list
    # print 'New atgs: ' + str(len(new_unique_list))
    # print new_unique_list
    # print 'New edges: ' + str(len(new_edge_list))
    # print new_edge_list

    for apk in os.listdir(apk_dir):
        if apk.endswith('.apk'):
            apk_path = apk_dir + apk
            org_apk_name = apk.split('.apk')[0]

            print 'apk path: ' + apk_path
            print '[1] Start to rename the app and get package name.'
            apk_path = rename(apk_path, apk_dir)
            apk_name = os.path.split(apk_path)[1].split('.apk')[0]
            print '[1] Rename app is done.'

            # Create output folder
            dir = output + '/outputs/' + apk_name + '/'
            if not os.path.exists(dir):
                os.makedirs(dir)

            # Save pkg name
            open(dir + 'used_pkg_name.txt', 'wb').write(used_pkg_name + '\n')
            open(dir + 'defined_pkg_name.txt', 'wb').write(defined_pkg_name + '\n')

            # Create sootOutput folder
            sootOutput_dir = output + 'sootOutput/' + apk_name + '/'
            if not os.path.exists(sootOutput_dir):
                os.makedirs(sootOutput_dir)

            print '[8] Start to get CTGs ' + apk_name
            print 'soot pkg: ' + used_pkg_name
            run_soot(output, apk_path, used_pkg_name, apk_name)
            #get_atgs(apk_name)
            print '[8] Get CTGs is done'

            '''

            print '[2] Start to decompile apk.'
            decompile(apk_path, apk_name)
            print '[2] Decompile apk is done.'

            print 'Start to get SootOutput (class) and check layout type'
            getSootOutput(apk_path, apk_name)

            print '[3] Start to run IC3 ' + apk_name
            results_IC3 = run_IC3(apk_path)
            print '[3] Run IC3 is done.'

            print '[4] Start to parse IC3.'
            dict = parse_IC3(results_IC3, used_pkg_name) # will check whether is defined_pkg_name
            save_parsed_IC3(dict)
            print '[4] Parse the result of IC3 is done.'

            print '[5] Start to get call graphs ' + apk_name
            CG_path = output + 'soot_cgs/'
            if not os.path.exists(CG_path):
                os.makedirs(CG_path)
            results_CG = CG_path + apk_name + '.txt'
            if not os.path.exists(results_CG):
                get_callgraphs(apk_path)
            print '[5] Get call graphs is done.'

            print '[6] Start to parse call graphs ' + apk_name
            dict = parse_CG(results_CG, used_pkg_name)
            save_parsed_CG(dict)
            print '[6] Parse call graphs is done'

            print '[7] Get Jimple ' + apk_name
            shutil.rmtree(sootOutput_dir)  # delete sootOutput
            #os.chdir(output + 'apktojimple')
            #os.system('./decompile.sh %s %s'%(apk_path, sootOutput_dir))
            #print '[7] Get Jimple is done'

            print '[8] Start to get CTGs ' + apk_name
            print 'soot pkg: ' + used_pkg_name
            run_soot(output, apk_path, used_pkg_name, apk_name)
            get_atgs(apk_name)
            print '[8] Get CTGs is done'

            print '[9] Start to get corresponding appstory ' + apk_name
            results_JavaCode = output + 'java_code/' + apk_name + '/'
            result_apkfolder = output + 'outputs/' + apk_name + '/'
            results_visulization_ICCs = result_apkfolder + apk_name + '_atgs.txt'
            processed_cg_file = result_apkfolder + apk_name + '_cgs.txt'
            if os.path.exists(results_JavaCode):
                getActivityMethodCode.main(results_JavaCode, result_apkfolder, results_visulization_ICCs, processed_cg_file, launchActivity)
            print '[9] Get components and method code is done'

            print '[10] Start to get method call sequence'
            if os.path.exists(processed_cg_file):
                traverseTree.main(processed_cg_file, results_visulization_ICCs, result_apkfolder)
            print '[10] Get method call sequence is done'


            ####Core####
            print '[11] Get the screenshots ' + apk_name
            if not os.path.exists(result_apkfolder + 'screenshots'):
                os.mkdir(result_apkfolder + 'screenshots')

            # 00 appstory
            all_acts = run_rpk_explore_apk.execute(apk_path, apk_name, result_apkfolder, output)
            print '[11] Get the screenshots is done'

            union_list = []
            static_list = []
            new_unique_list = []
            ###parse static and dynamic atgs
            dynamic_explore_result = os.path.join(result_apkfolder, apk_name + '_atgs_dynamic.txt')

            #union_list, static_list, new_unique_list, new_edge_list = parse(dir, results_visulization_ICCs, dynamic_explore_result)

            # print 'All atgs: ' + str(len(union_list))
            # print union_list
            # print 'Soot atgs: ' + str(len(static_list))
            # print static_list
            # print 'New atgs: ' + str(len(new_unique_list))
            # print new_unique_list
            #print 'New edges: ' + str(len(new_edge_list))
            #print new_edge_list

            if all_acts != None:
                # get some statistics
                launched_act_num = int(
                    commands.getoutput('ls %s | wc -l' % (result_apkfolder + 'screenshots')).split('\n')[0])

                # print launched_act_num
                act_not_in_atg = get_act_not_in_atg(all_acts)

                # print act_not_in_atg
                act_not_launched = get_acy_not_launched(all_acts)

                # print act_not_launched
                csv.writer(open(out_csv, 'a')).writerow((apk_name, used_pkg_name, len(all_acts), launched_act_num, act_not_in_atg, act_not_launched,
                                                         str(len(union_list)), str(len(static_list)), str(len(new_unique_list))))


            ####HTML####
            print '[12] Get Json'
            config_path = os.path.join(output, 'config/')
            copy_search_file(os.path.join(config_path, 'template/'), os.path.join(result_apkfolder, 'output/'))
            create_json_withindent.execute(result_apkfolder)
            print '[12] Get Json: DONE'

            os.remove(os.path.join(result_apkfolder, apk_name + '.apk'))
            '''

            os.remove(apk_path)