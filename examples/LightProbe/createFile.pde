// Create the output file and return its full path
// based on http://stackoverflow.com/questions/7887078/android-saving-file-to-external-storage

String createFile(String fileName) {
  //get the path of the external storage directory
  String root = android.os.Environment.getExternalStorageDirectory().toString();
  
  //Check to see if the directory exists
  try {
    File myDir = new File(root + "/" + filePrefix);
    
    //create the directory if it doesn't exist
    if (!myDir.exists()) {
    myDir.mkdirs();
  }
  }catch(Exception e) {
  }  
  
  //build the full file path and name
  String filePath = root + "/" + filePrefix + "/" + fileName;

  //create the file
  File outFile = new File(filePath);
  if (!outFile.exists()) {
    try {
      outFile.createNewFile();
    } 
    catch(Exception ex) {
      println(ex);
    }
  }

  return(filePath);
}

