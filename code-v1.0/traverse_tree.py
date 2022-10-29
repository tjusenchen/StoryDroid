'''
Author: Lingling Fan
'''
from treelib import Node, Tree
import treelib, shutil, os

cg_file = ''
icc_file = ''


def getUniqAct():
    uniq = []
    lines = open(icc_file,'rb').readlines()
    for line in lines:
        line = line.strip('\n').split('-->')
        uniq.append(line[0])
        uniq.append(line[1])
    return set(uniq)
def formatMethodLeft(line):
    m = line[0].split('(')[0].split(':')
    m = m[0].split('.')[-1] +':'+ m[1]
    return m
def formatMethodRight(line):
    m = line[-1].split('(')[0].split(':')
    m = m[0].split('.')[-1] +':'+ m[1]
    return m
def create_tree(activity, visited_root):
    tree = Tree()
    root = False
    #if not os.path.exists(cg_file):
    #    return
    lines = open(cg_file,'rb').readlines()
    tree_root = ''
    for line in lines:
        line = line.strip('\n').split('-->')
        v_root = formatMethodLeft(line)
        if activity in line[0] and root == False and not v_root in visited_root\
                and not 'void <init>' in line[0] and not 'void <clinit>()' in line[0]:
            tree_root = formatMethodLeft(line)
            tree.create_node(tree_root, tree_root)  # root node
            root = True
        if not 'void <init>' in line[1] and not 'void <clinit>()' in line[1] and root == True:
            node = formatMethodRight(line)
            parent = formatMethodLeft(line)
            try:
                tree.create_node(node,node,parent=parent)
            except treelib.exceptions.NodeIDAbsentError:
                pass
            except treelib.exceptions.DuplicatedNodeIdError:
                pass
    return tree, root, tree_root
def main(cg, icc, folder):
    global  cg_file, icc_file
    cg_file = cg
    icc_file = icc
    if os.path.exists(folder + 'call_sequence/'):
        shutil.rmtree(folder + 'call_sequence/')
    os.mkdir(folder + 'call_sequence/')
    for activity in getUniqAct():
        open(folder + 'call_sequence/' + activity + '.txt', 'wb').write('')
        visited_root = []
        root = True
        while root:
            tree, root, treeroot = create_tree(activity, visited_root)
            if not treeroot == '':
                visited_root.append(treeroot)
            #tree.show()

            for path in tree.paths_to_leaves():
                #print path
                for i in range(0, len(path)-1):
                    open(folder + 'call_sequence/' + activity + '.txt', 'ab').write(path[i]+'-->')
                open(folder + 'call_sequence/' + activity + '.txt', 'ab').write(path[-1] + '\n')

# main('/home/senchen/Desktop/Storyboard/visulization/org.secuso.privacyfriendlytodolist_4/org.secuso.privacyfriendlytodolist_4_CG.txt',
#       '/home/senchen/Desktop/Storyboard/visulization/org.secuso.privacyfriendlytodolist_4/org.secuso.privacyfriendlytodolist_4_ICCs.txt',
#       '/home/senchen/Desktop/Storyboard/visulization/org.secuso.privacyfriendlytodolist_4/')