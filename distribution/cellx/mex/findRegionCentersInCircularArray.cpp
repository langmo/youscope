#include <yvals.h>
#if (_MSC_VER >= 1400)
    #define __STDC_UTF_16__
#endif
        
#include "mex.h" 
#include "matrix.h"
#include<cmath>
#include<stdio.h>
#include<string.h>

/*
 * cmayer@bsse.ethz.ch, 2011
 *
 *
 * logicalVec = findRegionCentersInCircularArray(v, minLen, spanLen)
 *
 * Computes the centers of 1-stetches in v 
 * v is assumed to represent cirularly connected data 
 * ...,v(end),v(1),v(2),...
 *
 * v is a logical array that holds a 1 at the i-th position if the energy 
 * value of a membrane border pixel at pos i is below a threshold.

 * minLen is the minimum number of consecutive 1?s of a region in v 
 * (after smoothing with spanLen) to be included in the returned vector.
 * (minLen=0 or minLen=1 includes all regions)

 * If there are less than spanLen 0?s between two 1-regions then the two one regions are connected.
 * (spanLen=0 means no smoothing)

 * logicalVec has the same length as v and a 1 at every center.
 * If we have a vector linIdx with linear indices for the pixels on the membrane border, 
 * linIdx(ligicalVec) returns the centers of the smoothed weak regions.
*/


void mexFunction(int nlhs, mxArray *plhs[], int nrhs, const mxArray *prhs[]) {
    

    if (nrhs!=3){
        mexErrMsgTxt("Expected 3 arguments:\n\n  logialVec = findRegionCentersInCircularArray(v, minLen, spanLen)\n\n");
    }
    if ( !mxIsLogical(prhs[0]) ){
        mexErrMsgTxt("First argument must be a logical array");
    }

    
    
    const bool* vec     = mxGetLogicals(prhs[0]);
    const int n         = mxGetNumberOfElements(prhs[0]);
    const int minLen    = mxGetScalar(prhs[1]);
    const int spanLen   = mxGetScalar(prhs[2]);
    
    plhs[0] = mxDuplicateArray(prhs[0]);
    bool *dest = mxGetLogicals(plhs[0]); 
    memset(dest, false, n*sizeof(bool));

    int* d = new int[n];
    memset(d, 0, n*sizeof(int));
    
    //count lengths of zero stretches
    int counter=0;
    for(int i=0; i<n; ++i){
        if( !vec[i] ){
            d[i] = ++counter;
        }else{
            counter=0;
        }  
    }
    
    //make circular connection if more than one (zero) region exists
    if(!vec[0] && !vec[n-1] && d[n-1]!=n){
        d[0] += d[n-1];
    }
        
    
    //create smoothed version
    bool* smoothed = new bool[n];
    memset(smoothed, false, n*sizeof(bool));
    for(int i=n-1; i>=0; --i){
        //mexPrintf(" d[i]=%d\n", d[i]);
        if( vec[i] ){
            smoothed[i] = true;
        }else{
            if(d[i]<=spanLen){
                smoothed[i] = true;
            }else{
                i -= d[i]-1;
            }
          
        }
    }
       
    //count lengths of 1 stretches
    counter=0;
    for(int i=0; i<n; ++i){
        //mexPrintf(" smoothed = %d\n", smoothed[i]);     
        if( smoothed[i] ){
            d[i] = ++counter;
        }else{
            d[i]    = 0;
            counter = 0;
        }  
    }
    
    //handle ends
    const int lastOnesStretchLen  = d[n-1];
    int revStart=n-1;
    if( smoothed[n-1] && smoothed[0] && lastOnesStretchLen<n ){
        int i=0;
        while(smoothed[i]){
            d[i] += lastOnesStretchLen; 
            ++i;
        }
        revStart = n-1-d[n-1]; 
    }
    
    for(int i=revStart; i>=0; --i){
        //mexPrintf(" d[i]=%d  smoothed[i]=%d\n", d[i], smoothed[i]);
        if( d[i]>=minLen && smoothed[i]){
            //mexPrintf(" len = %d\n", d[i]);
            const int m = i - d[i]/2;
            //mexPrintf(" center = %d\n", m);
            if(m<0){
                dest[n+m] = true;
                break;
            }else{
                dest[m] = true;
                i -= d[i];
            }
        }
    }
    
    if( nlhs>1 ){
        plhs[1] = mxCreateLogicalMatrix(n,1);
        mxLogical *ptr = mxGetLogicals(plhs[1]);
        for(int i=0;i<n;++i){
            ptr[i] = smoothed[i];
        }
    }
    delete [] smoothed;
    delete [] d;
}
