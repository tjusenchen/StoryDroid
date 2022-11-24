import os
import get_similarACT

staticPath = '/home/zyx/software/pythonProject/tool/code/static/activity'
comPath = '/home/zyx/Desktop/result/A_components_class/nonIssueXML/android.widget.Button_184'

classifyActSet = {}
mainList = []
for com in os.listdir(comPath):
    # print com
    apk = com.split('_')[1]
    actName = com.split('_')[-3].split('.')[-1]
    # print actName
    classifyActSet, actName = get_similarACT.getSimilarSet(apk, actName, classifyActSet)
    print actName
    if actName == 'main':
        mainList.append(com)
print classifyActSet
print mainList