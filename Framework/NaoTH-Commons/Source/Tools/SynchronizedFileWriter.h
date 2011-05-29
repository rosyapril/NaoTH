/**
* @author 
*/
#ifndef __SynchronizedFileWriter_h_
#define __SynchronizedFileWriter_h_

#include <string>
#include <iostream>
#include <stdio.h>

class SynchronizedFileWriter
{
private:
  SynchronizedFileWriter(){};
  ~SynchronizedFileWriter(){};

public:

  static inline bool saveStringToFile(const std::string& content, const std::string& filename)
  {
    FILE* file = fopen(filename.c_str(), "wb");
    if (file != NULL) 
    {
      fwrite(content.c_str(), 1, content.length(), file);

      // synchronize with the filesystem, 
      // i.e., purge the fs buffer to the disk
#ifdef NAO
      fsync(file);
#endif

      fclose(file);
      return true;
    }//end if

    return false;
  }//end saveStreamToFile


  static inline bool saveStreamToFile(const std::stringstream& content, const std::string& filename)
  {
    std::string str = content.str();
    return saveStringToFile(str, filename);
  }//end saveStreamToFile

}; //end class SynchronizedFileWriter

#endif