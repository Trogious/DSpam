ADB=~/Android/Sdk/platform-tools/adb


$ADB shell mkdir sdcard/DSpam
$ADB push ~/dspam/app/src/main/res/raw/mystore.bks sdcard/DSpam/dspamstoreclient.bks
$ADB push ~/dspam/app/src/main/res/raw/truststore.bks sdcard/DSpam/dspamstoretrust.bks
$ADB ls sdcard/DSpam/

