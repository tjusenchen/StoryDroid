# -*- coding: utf-8 -*-
import os, csv, sys
from xml.etree import ElementTree as ET

reload(sys)
sys.setdefaultencoding('utf8')

'''
input: xml file
output: csv file containing attribute info
'''
#xml_file = '/home/fanlingling/com.venmo.controller.ActionableAuthorizationsActivity.xml'

#out_csv = '/home/fanlingling/test-output.csv'

#csv.writer(open(outcsv, 'a')).writerow(('act name', 'pkg', 'text', 'id', 'class', 'bounds'))

outcsv = ''

def iter(child, act_name):
    cs = len(child.getchildren())
    if len(child.getchildren()) == 0:
        print(child.attrib)
        if not child.attrib['class'] == 'android.view.View':
            csv.writer(open(outcsv, 'a')).writerow((act_name, child.attrib['package'],child.attrib['text'],child.attrib['resource-id'],
                                                      child.attrib['class'], child.attrib['bounds']))
    else:
        for c in child.getchildren():
            iter(c, act_name)

def parse_xml(xml_file, out_csv):
    global outcsv
    outcsv = out_csv
    tree = ET.parse(xml_file)
    act_name = os.path.basename(xml_file).split('.xml')[0]
    root = tree.getroot()
    childs = root.getchildren()
    for child in childs:
        iter(child, act_name)

#parse_xml('/home/fanlingling/com.venmo.controller.ActionableAuthorizationsActivity.xml', '/home/fanlingling/test-output.csv')