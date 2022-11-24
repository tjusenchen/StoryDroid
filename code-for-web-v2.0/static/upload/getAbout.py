import os

comPath = '/home/zyx/Desktop/result/A_components_class/nonIssueXML/android.widget.Button_184'
for png in os.listdir(comPath):
    # print png
    if png.find('About') != -1:
        print png