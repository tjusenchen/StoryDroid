'''
input: an apk
output: a repackaged apk
'''
import os
import xml.dom.minidom
import shutil
import commands


result_folder = ''
#keyPath = os.path.join(os.path.split(os.path.realpath(__file__))[0],"coolapk.keystore")  # pwd: 123456, private key path
keyPath = ''

def decompile(eachappPath):
    apktooldecomplilePath = os.path.join(result_folder, 'decompile')
    if not os.path.exists(apktooldecomplilePath):
        os.mkdir(apktooldecomplilePath)

    print "decompiling..."
    cmd = "apktool d {0} -f -o {1}".format(eachappPath, apktooldecomplilePath)
    os.system(cmd)

def modifyManifestAgain(app, line_num):
    # in order to fix an error
    ManifestPath = os.path.join(result_folder,'decompile', "AndroidManifest.xml")
    lines = open(ManifestPath,'r').readlines()
    if '@android' in lines[line_num-1]:
        #lines[line_num-1].replace('@android','@*android')
        lines[line_num-1] = lines[line_num-1].split('@android')[0] + '@*android' + lines[line_num-1].split('@android')[1]

    open(ManifestPath,'w').writelines(lines)

def recompile(apk):

    eachapkPath = os.path.join(result_folder,'decompile')

    cmd = "apktool b {0}".format(eachapkPath)

    #W: /xxx/AndroidManifest.xml:156: error: Error: Resource is not public. (at 'enabled' with value '@android:bool/config_tether_upstream_automatic').

    output = commands.getoutput(cmd)

    return output


def sign_apk(repackapp):
    repackName = repackapp + ".apk"
    resign_appName = repackapp + "_sign" + ".apk"
    #optiName = repackapp + "_z" + ".apk"
    repackAppPath = os.path.join(result_folder, "decompile/dist", repackName)
    sign_apk = os.path.join(result_folder, resign_appName)
    #optappPath = os.path.join(run.decompilePath, repackapp, "dist", optiName)
    #cmd = "zipalign -v 4 {0} {1}".format(repackAppPath, optappPath)
    #os.system(cmd)

    read, write = os.pipe()
    os.write(write, '123456')
    os.close(write)

    cmd = "jarsigner -verbose -keystore {0} -signedjar {1} {2} {3}".format(keyPath, sign_apk, repackAppPath, "coolapk")
    cmd1 = "echo '123456\r'|{0} ".format(cmd)

    returncode = os.system(cmd1)
    return returncode

def rename(apkname):

    oldNamePath = os.path.join(result_folder, apkname + '_sign.apk')

    newNamePath = os.path.join(result_folder, apkname+'.apk')

    os.rename(oldNamePath,newNamePath)

def remove_folder():
    folder = os.path.join(result_folder, 'decompile')
    if not os.path.exists(folder):
        return
    for f in os.listdir(folder):
        if not f == 'AndroidManifest.xml':
            rm_path = os.path.join(folder, f)
            if os.path.isdir(rm_path):
                shutil.rmtree(rm_path)
            else:
                os.remove(rm_path)

def addExportedTrue(line):
    if 'exported="true"' in line:
        return line
    if 'exported="false"' in line:
        return line.replace('exported="false"', 'exported="true"')
    if not 'exported' in line:
        return '<activity exported="true" ' + line.split('<activity ')[1]

def modifyManifest_00():
    newlines = []
    ManifestPath = os.path.join(result_folder, 'decompile', "AndroidManifest.xml")
    flag = 0
    for line in open(ManifestPath,'r').readlines():
        if line.strip().startswith('<activity '):
            line = addExportedTrue(line)
            newlines.append(line)
        else:
            newlines.append(line)
    open(ManifestPath,'wb').writelines(newlines)

def startRepkg(apk_path, apkname, apkresult_folder, output):
    global keyPath
    config_path = os.path.join(output, 'config/')
    keyPath = os.path.join(config_path, "coolapk.keystore")


    global result_folder
    result_folder = apkresult_folder

    #apkname = apk.rstrip('.apk')

    decompile(apk_path)

    modifyManifest_00()

    recompileInfo = recompile(apkname)
    print "recompiling..."

    builtApk = False
    for line in recompileInfo.split('\n'):
        if "Error: Resource is not public." in line:
            line_num = int(line.split('AndroidManifest.xml:')[1].split(': error')[0])
            modifyManifestAgain(apkname, line_num)
            recompileInfo = recompile(apkname)
            break
        if "Built apk..." in line:
            builtApk = True
            print "Successfully recompile an apk!!!"

    if not builtApk:
        return 'no apk'
    sign_apk(apkname)
    print "signing..."

    #os.remove(apk_path)
    #"deleting org apk..."

    rename(apkname)

    remove_folder()