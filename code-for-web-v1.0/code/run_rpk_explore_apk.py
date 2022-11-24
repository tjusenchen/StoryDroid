import os,sys
import repkg_apk, explore_acts


def execute(apk_path, apk_name, result_apkfolder, output):

    print '======= Starting ' + apk_name + ' ========='

    '''
    Repackge app
    '''
    if not os.path.exists(os.path.join(result_apkfolder,apk_name+'.apk')):
        r = repkg_apk.startRepkg(apk_path, apk_name, result_apkfolder, output)
        if r == 'no apk':
            print 'apk not successfully recompiled!!!!!!!!!'

    new_apkpath = os.path.join(result_apkfolder, apk_name+'.apk')

    if os.path.exists(new_apkpath):
        all_acts = explore_acts.exploreAct(new_apkpath, apk_name, result_apkfolder)
        return all_acts
    else:
        print 'No such repackaged app exist............'
	# zyx
	return 0
	# zyx

#execute('/home/senchen/Desktop/storydroid_plus/apks/org.liberty.android.fantastischmemo_223.apk','org.liberty.android.fantastischmemo_223',
#        '/home/senchen/Desktop/storydroid_plus/outputs/org.liberty.android.fantastischmemo_223')
