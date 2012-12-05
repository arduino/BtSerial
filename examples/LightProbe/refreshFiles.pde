// from http://stackoverflow.com/questions/4646913/android-how-to-use-mediascannerconnection-scanfile

//refresh the index of external files
//(for devices that don't have SD card slots and must transfer files over MTP)
void refreshFiles() {
  sendBroadcast(new Intent(Intent.ACTION_MEDIA_MOUNTED, Uri.parse("file://" + android.os.Environment.getExternalStorageDirectory() + "/lumos")));
}
