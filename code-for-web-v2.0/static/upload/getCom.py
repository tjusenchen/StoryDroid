import os
import shutil

dataPath = '/home/zyx/software/pythonProject/tool/code/static/js/mainUtg.js'

f = open(dataPath, "r")
txtAll = f.read()
# print txtAll.split("'image': '")
pathList = []
for list in txtAll.split("'image': '"):
    if not list.startswith('/static'):
        continue
    pathList.append(list.split("',")[0].split('activity/')[1].split('png')[0])
    print list.split("',")[0]
    # if os.path.exists('/home/zyx/software/pythonProject/tool/code' + list.split("',")[0]):
    #     os.remove('/home/zyx/software/pythonProject/tool/code' + list.split("',")[0])
print pathList

staticPath = '/home/zyx/software/pythonProject/tool/code/static/activity1'
comPath = '/home/zyx/Desktop/result/A_components_class/nonIssueXML/android.widget.Button_184'

i = 0
pngList = os.listdir(comPath)
pngList = ['101_com.bistux.surveyer_com.bistux.surveyer.MainActivity_5_Button.png', '177_com.shoutcast.stm.radiowebshalom_com.shoutcast.stm.radiowebshalom.MainActivity_3_Button.png', '42_org.coopersoft.elcalcfree_org.coopersoft.elcalcfree.Main_6_Button.png', '115_ru.dubsteplight.radio_ru.modi.dubsteponline.activities.MainActivity_5_Button.png', '114_ru.dubsteplight.radio_ru.modi.dubsteponline.activities.MainActivity_4_Button.png', '18_com.radiosenpy.masterfm_com.radiosenpy.masterfm.MainActivity_5_Button.png', '43_pe.kmh.fm_pe.kmh.fm.MainActivity_8_Button.png', '175_com.shoutcast.stm.radiowebshalom_com.shoutcast.stm.radiowebshalom.MainActivity_1_Button.png', '40_org.coopersoft.elcalcfree_org.coopersoft.elcalcfree.Main_4_Button.png', '109_a2dp.Vol_133_a2dp.Vol.main_3_Button.png', '39_org.coopersoft.elcalcfree_org.coopersoft.elcalcfree.Main_3_Button.png', '4_com.diary.superdiary_com.diary.superdiary.MainActivity_4_Button.png', '178_com.shoutcast.stm.radiowebshalom_com.shoutcast.stm.radiowebshalom.MainActivity_7_Button.png', '116_com.eonsoft.TxtViewer_com.eonsoft.TxtViewer.MainActivity_2_Button.png', '41_org.coopersoft.elcalcfree_org.coopersoft.elcalcfree.Main_5_Button.png', '121_com.mediosgt.radiofantasi_com.radio.aacttplayer.MainActivity_6_Button.png', '19_com.radiosenpy.masterfm_com.radiosenpy.masterfm.MainActivity_6_Button.png', '30_com.enclase.formulacionquimcalite_com.enclase.formulacionquimcalite.MainActivity_8_Button.png', '120_com.mediosgt.radiofantasi_com.radio.aacttplayer.MainActivity_5_Button.png', '28_com.enclase.formulacionquimcalite_com.enclase.formulacionquimcalite.MainActivity_6_Button.png', '12_com.radio40.southafric_com.radio40.radio40boilerplate.MainActivity_3_Button.png', '111_a2dp.Vol_133_a2dp.Vol.main_5_Button.png', '119_com.mediosgt.radiofantasi_com.radio.aacttplayer.MainActivity_1_Button.png', '29_com.enclase.formulacionquimcalite_com.enclase.formulacionquimcalite.MainActivity_7_Button.png', '104_com.thesmartpos.cpmobilepos_com.thesmartpos.cpmobilepos.MainActivity_10_Button.png', '105_com.thesmartpos.cpmobilepos_com.thesmartpos.cpmobilepos.MainActivity_13_Button.png', '176_com.shoutcast.stm.radiowebshalom_com.shoutcast.stm.radiowebshalom.MainActivity_2_Button.png', '10_com.sparky.ptisongs_com.sparky.ptisongs.MainActivity_3_Button.png', '44_pe.kmh.fm_pe.kmh.fm.MainActivity_9_Button.png', '179_com.shoutcast.stm.radiowebshalom_com.shoutcast.stm.radiowebshalom.MainActivity_8_Button.png', '11_cz.okhelp.german_czech_phrases_cz.okhelp.german_czech_phrases.MainPsc_1_Button.png', '110_a2dp.Vol_133_a2dp.Vol.main_4_Button.png']

while i < len(pathList):
    shutil.copyfile(os.path.join(comPath, pngList[i]), os.path.join(staticPath, pathList[i] + 'png'))
    print i
    print pngList[i].split('_')[-3].split('.')[-1]
    i = i + 1
    # os.system(compileCMD)
    # shutil.move(os.path.join(dex2jarPath, jarName), jarFPath)
    # os.rename(os.path.join(jarFPath, jarName), os.path.join(jarFPath, apkName + '.jar'))
print 'ok'