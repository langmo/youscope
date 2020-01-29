// cmayer@bsse.ethz.ch, 1/2012
#include "mex.h"
//mxAssert
#include "matrix.h"
//DBL_MIN
#define _USE_MATH_DEFINES
#include<cmath>
#include<cfloat>
//memset
#include<string.h>

#include<assert.h>

#ifndef RAY_H
#define RAY_H
class Ray{
public:  
    //the point coordinates
    int* x;
    int* y;
    
    //the value of each point
    double *value;
    
    //convolution values
    double *convolution;
    
    // the average value
    double mean;
    
    // the maximum convolution value on the ray
    double max;
    

    //the number of points
    int n;
    
    //the start and end pointer of the convolution values; 
    double *cStart, *cEnd;
    //the start and end index of convolution values on the ray
    int iStart,iEnd;
    
    const int size;
    
    Ray(int size):size(size),n(0),mean(0.0){
        if(size<=0)
            mexErrMsgTxt("Ray size cannot be <=0");
        x = new int[size];
        y = new int[size];
        value = new double[size];
        convolution = new double[size];            
        memset(x, 0, size*sizeof(int));
        memset(y, 0, size*sizeof(int));
        memset(value, 0.0, size*sizeof(double));
        memset(convolution, 0.0, size*sizeof(double));
    }
    
    ~Ray(){
        delete [] x;
        delete [] y;
        delete [] value;
        delete [] convolution;
    }
    
    void print(){
        mexPrintf("\n  Allocation length: %d\n", size);
        mexPrintf("\n%5s %5s %10s  %10s \n\n", "x", "y", "value", "conv");
        for( int i=0; i<n; ++i){
            mexPrintf("%5d %5d %10.4f  %10.4f \n" , x[i], y[i], value[i], convolution[i]);
        }
        mexPrintf("\n  Mean:   %6.4f\n", mean);
        
    }
    
    /*
     * Bresenham, wikipedia, modified
     */
    void bresenham(int x0, int y0, int x1, int y1, int rowCount, const double *image){
        const int dx = abs(x1-x0);
        const int dy = abs(y1-y0); 
        int err = dx-dy;
   
        int sx, sy;
        if (x0 < x1)
            sx = 1;
        else
            sx = -1;
   
        if (y0 < y1)
            sy = 1;
        else
            sy = -1;

        int i=0, e2=0;
        while( i<size ){
            x[i] = x0;
            y[i] = y0;
            const double intensity =  image[x0*rowCount+y0]; 
            value[i] = intensity;
            mean += intensity;       
            ++i;
            if (x0 == x1 && y0 == y1) break;      
            e2 = 2*err;
            if (e2 > -dy){
                err = err - dy;
                x0  = x0 + sx;
            }    
            if (e2 < dx){
                err = err + dx;
                y0  = y0 + sy;
            }
        }
        n=i;
        mean /= i;   
    }

    /*
     * Convolute with offset but only where the entire signal fits,
     * other positions remain zero.
     */
    void convoluteWithOffset(const double* signal, const int signalLength, const int offset){
        mxAssert(signalLength>0, "Signal length is <=0");
        mxAssert(offset>=0, "Offset is <0");
        mxAssert(offset<signalLength, "Offset is >= signal length");
        
        iStart = offset;
        cStart = convolution + iStart;
        iEnd   = n-signalLength+offset+1;
        cEnd   = convolution + iEnd;
        const double *sIt, *vIt, *srcIt=value;
        const double *sEnd = signal + signalLength;
        double *cIt = cStart;
        double d;
        max = DBL_MIN;
        while(cIt<cEnd){
            d=0.0;
            vIt = srcIt++;
            sIt = signal;
            while(sIt<sEnd){
                //mexPrintf("i=%d v=%f  signal=%f  d=%f\n", i, *vIt, *sIt, d );
                d += (*vIt - mean) * (*sIt);
                ++vIt;
                ++sIt;
            }
            *cIt = d;
            if(d>max){
                max=d;
            }
            ++cIt;
        }
    }
    
    /*
     * with zero padding on the left 
     */
    void convoluteWithOffsetZeroPad(const double* signal, const int signalLength, const int offset){

        
        convoluteWithOffset(signal, signalLength, offset);
        
        const double *vIt;
        double sigMean = 0.0;
        double d;
        for(int i=0; i<offset; ++i){
            const int sigStart = offset-i;
            //mexPrintf("i=%d, signal start=%d\n", i, sigStart);
            for(int j=sigStart; j<signalLength; ++j){
                sigMean += signal[j];
            }
            sigMean /= signalLength-sigStart;
            d=0.0;
            vIt = value;
            for(int j=sigStart; j<signalLength; ++j){
                d += (*vIt-mean)*(signal[j]-sigMean);
                ++vIt;
            }
            if(d>max){
                max=d;
            }
            convolution[i]=d;
        }
        iStart = 0;
        cStart = convolution;
    }
    
    
    
};

#endif
