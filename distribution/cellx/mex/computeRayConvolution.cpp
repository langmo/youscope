#include <yvals.h>
#if (_MSC_VER >= 1400)
    #define __STDC_UTF_16__
#endif
        
#include "mex.h" 
#include "matrix.h"

#include "Ray.h" 



using namespace std;

class ConvolutionImage{

    public:
        double* cImage;
        int* rayCrossings;
        
        const int rowCount,columnCount,size;
        
        ConvolutionImage(int rowCount, int columnCount):rowCount(rowCount),columnCount(columnCount),size(rowCount*columnCount){
            cImage       = new double[size];
            rayCrossings = new int[size];
            memset(cImage, 0.0, size*sizeof(double));
            memset(rayCrossings, 0 , size*sizeof(int));
        }
        
        ~ConvolutionImage(){
            delete [] cImage;
            delete [] rayCrossings;
        }
    
        void normalize(){
            for(int i=0; i<size; ++i){
                if( rayCrossings[i]!=0 ){
                    cImage[i] /= rayCrossings[i];
                }
            }         
        }
        
        void registerRay(const Ray  &l){
            const double *cIt = l.cStart;
            for(int i=l.iStart; i<l.iEnd; ++i){
                const int idx = l.x[i]*rowCount+l.y[i];
                cImage[idx] += *cIt++;
                ++rayCrossings[idx];
            }
        }
        
};


void mexFunction(int nlhs, mxArray *plhs[], int nrhs, const mxArray *prhs[]) {
    

    if (nrhs !=5){
        mexErrMsgTxt("Expected 5 arguments:\n\n  computeRayConvolution(x, y, image, membraneSignal, membraneOffset)\n\n");
    }
    
    if ( mxIsComplex(prhs[2])|| mxIsClass(prhs[2], "sparse") || mxIsChar(prhs[2]) ){
        mexErrMsgTxt("Third argument must be an image (two dimensional matrix)");
    }
    
    const int x = mxGetScalar(prhs[0])-1;
    const int y = mxGetScalar(prhs[1])-1;
    
    const int rowCount    = mxGetM(prhs[2]);
    const int columnCount = mxGetN(prhs[2]);
    const double* image   = mxGetPr(prhs[2]);
    
    const double* membraneSignal = mxGetPr(prhs[3]);
    const int signalLength       = mxGetNumberOfElements(prhs[3]);
    const int membraneOffset     = mxGetScalar(prhs[4])-1;
    
    if( rowCount==0 || columnCount==0 ){
        mexErrMsgTxt("Zero dimension in image");
    }
    
    
    if( x<0 || x>=columnCount){
        mexErrMsgTxt("x coordinate out of range");
    }
    
    if( y<0 || y>=rowCount){
        mexErrMsgTxt("y coordinate out of range");
    }    
    
    if( membraneOffset<0 ){
        mexErrMsgTxt("Membrane offset must be >=1");
    }  
    
    if( membraneOffset>=signalLength ){
        mexErrMsgTxt("Membrane offset must be within the signal length");
    }
    
    
    //mexPrintf("Membrane offset %d\n", membraneOffset);
    
    ConvolutionImage ci(rowCount, columnCount);
    Ray ray(rowCount+columnCount);
    
    const int numberOfRays = 2*rowCount + 2*columnCount;
    mxArray* mxMaxConvolutionValues = mxCreateNumericMatrix(numberOfRays, 1, mxDOUBLE_CLASS, mxREAL);
    double* maxConvValue = mxGetPr(mxMaxConvolutionValues);
    
    for( int i=0; i<columnCount; ++i){  
        ray.bresenham(x, y, i, 0, rowCount, image);
        ray.convoluteWithOffsetZeroPad(membraneSignal, signalLength, membraneOffset);
        *maxConvValue++=ray.max;
        ci.registerRay(ray);              
        ray.bresenham(x, y, i, rowCount-1, rowCount, image);
        ray.convoluteWithOffsetZeroPad(membraneSignal, signalLength, membraneOffset);
        *maxConvValue++=ray.max;
        ci.registerRay(ray);
    }
    
     for( int i=1; i<rowCount-1; ++i){   
        ray.bresenham(x, y, 0, i, rowCount, image);
        ray.convoluteWithOffsetZeroPad(membraneSignal, signalLength, membraneOffset);
        *maxConvValue++=ray.max;
        ci.registerRay(ray);
        ray.bresenham(x, y, columnCount-1 , i, rowCount, image);
        ray.convoluteWithOffsetZeroPad(membraneSignal, signalLength, membraneOffset);
        *maxConvValue++=ray.max;
        ci.registerRay(ray);
    }
     
    ci.normalize();
        
    // send the convolution image to matlab ...
    plhs[0] = mxDuplicateArray(prhs[2]);
    memcpy(mxGetPr(plhs[0]), ci.cImage, ci.size*sizeof(double));
    if(nlhs>1){
        plhs[1] = mxMaxConvolutionValues; 
    }
     if(nlhs>2){
        plhs[2] = mxCreateNumericMatrix(columnCount, rowCount, mxINT32_CLASS, mxREAL);
        memcpy(mxGetPr(plhs[2]), ci.rayCrossings, ci.size*sizeof(int));
    }
}



