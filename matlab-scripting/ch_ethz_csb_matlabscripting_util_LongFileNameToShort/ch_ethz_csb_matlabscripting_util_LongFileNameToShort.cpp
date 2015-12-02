#include "jni.h"
#include <Windows.h>
#include "ch_ethz_csb_matlabscripting_util_LongFileNameToShort.h"

// This file converts a given windows file name into its "old" 8.3 representation.
JNIEXPORT jstring JNICALL Java_ch_ethz_csb_matlabscripting_util_LongFileNameToShort_toShortFileName(JNIEnv * env, jobject jobj, jstring fileName) 
{
    jboolean iscopy;
    jstring jb;
    long     length = 0;
    TCHAR*   buffer = NULL;
    
    // Convert Java String to character array
    const char* mfile = env->GetStringUTFChars(fileName, &iscopy);
    
	// To obtain the 8.3 representation, we have to pass a pre-allocated character array as an argument to
	// GetShortPathName. By first passing NULL and 0 to GetShortPathName, we obtain the length the character array has
	// to have as return.
	// First obtain the size needed by passing NULL and 0.
    length = GetShortPathName(mfile, NULL, 0);
    if (length == 0) 
       return fileName;

	// Dynamically allocate the correct size 
	// (terminating null char was included in length)
    buffer = (TCHAR*)calloc(length, sizeof(TCHAR));

	// Now we can get the 8.3 representation
    length = GetShortPathName(mfile, buffer, length);
    if (length == 0) 
       return fileName;

    // Convert to a java string and delete temporary variables.
    jb = env->NewStringUTF(buffer);
    free (buffer);
    env->ReleaseStringUTFChars(fileName, mfile);
    
    // Return result.
    return (jb);
}
