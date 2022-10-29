import os,commands,shutil
# pro_dir = '/home/fanlingling/com.example.liwenhe.fragment2activity_1'
# result_dir = '/home/fanlingling/com.example.liwenhe.fragment2activity_1_result/'
# icc_file = '/home/fanlingling/icc.txt'
# cg_file = '/home/fanlingling/cg.txt'

def getActCode(pro_dir,result_dir,icc_file, launcher):
    os.chdir(pro_dir)
    act_dir = mkdir(result_dir + 'activity')
    for activity in getUniqAct(icc_file, launcher):
        cmd = 'find . -name %s'%(activity.split('.')[-1] + '.java')
        path = commands.getoutput(cmd).split('\n')[0]
        cpcmd = 'cp %s %s'%(path, act_dir)
        os.system(cpcmd)
def getMthStart(cls_path, mthname):
    print mthname
    lines = open(cls_path, 'rb').readlines()
    for i in range(0, len(lines)):
        if mthname + '(' in lines[i]:
            if '{' in lines[i]:
                # all the parameters are in a line
                return i
            elif '{' in lines[i+1]:
                # parameters are in 2 lines
                return i
            elif ');' in lines[i]:
                continue
    return 'NoID'
def getMethodCode(pro_dir, result_dir, cg_file):
    os.chdir(pro_dir)
    mth_dir = mkdir(result_dir + 'methods')
    m = getUniqMethod(cg_file)
    if m == None:
        return
    for mth in getUniqMethod(cg_file):
        clsname = mth.split(':')[0].split('.')[-1]
        if "$" in clsname:
            clsname = clsname.split('$')[0]+'.java'
        else:
            clsname = clsname + '.java'
        mthname = mth.split(':')[1].split('(')[0].split(' ')[-1]
        cmd = 'find . -name %s'%clsname
        cls_path = commands.getoutput(cmd).split('\n')[0]
        if not cls_path == '':
            # find the file
            lineNo = getMthStart(cls_path, mthname)
            if lineNo == 'NoID':
                continue
            lines = open(cls_path, 'rb').readlines()
            flag = 0
            inside = False
            mthbody = []
            for i in range(lineNo, len(lines)):
                if '{' in lines[i]:
                    flag += 1
                    inside = True
                if '}' in lines[i]:
                    flag -= 1
                if flag == 0 and inside == True:
                    mthbody.append(lines[i])
                    open(mth_dir + '/' + mth.split(':')[0]+'.'+mthname+'.txt','wb').write('')
                    for body in mthbody:
                        open(mth_dir + '/' + mth.split(':')[0] + '.' + mthname + '.txt', 'ab').write(body)
                    break
                mthbody.append(lines[i])

def getUniqAct(icc_file, launcher):
    uniq = []
    if not os.path.exists(icc_file):
        return
    lines = open(icc_file, 'rb').readlines()
    if len(lines) == 0:
        uniq.append(launcher)
        return uniq
    for line in lines:
        line = line.strip('\n').split('-->')
        uniq.append(line[0])
        uniq.append(line[1])
    return set(uniq)

def getUniqMethod(cg_file):
    uniq = []
    if not os.path.exists(cg_file):
        return
    lines = open(cg_file,'rb').readlines()
    for line in lines:
        line = line.strip('\n').split('-->')
        if not 'void <init>' in line[0] and not 'void <clinit>()' in line[0]:
            uniq.append(line[0])
        if not 'void <init>' in line[1] and not 'void <clinit>()' in line[1]:
            uniq.append(line[1])
    return set(uniq)

def mkdir(dir):
    if os.path.exists(dir):
        shutil.rmtree(dir)
    os.mkdir(dir)
    return dir

def main(pro_dir, result_dir,icc_file,cg_file,launcher):
    if not os.path.exists(result_dir):
        os.mkdir(result_dir)
    #Activity Code
    getActCode(pro_dir,result_dir,icc_file, launcher)
    #Method Code
    getMethodCode(pro_dir, result_dir, cg_file)
