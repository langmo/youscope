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
            //delete [] cImage;
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
        
         void drawRay(const Ray  &l){
            const double *cIt = l.cStart;
            for(int i=0; i<l.n; ++i){
                const int idx = l.x[i]*rowCount+l.y[i];
                cImage[idx] = 1.0;
            }
        }
        
        
};

/*
 *
 *
 * Returns a black (zero) image with white(1.0) rays 
 *
 *
 *
 */
void mexFunction(int nlhs, mxArray *plhs[], int nrhs, const mxArray *prhs[]) {
    

    if (nrhs !=4){
        mexErrMsgTxt("Expected 5 arguments:\n\n  createRayImage(x, y, image, interval)\n\n");
    }
    
    if ( mxIsComplex(prhs[2])|| mxIsClass(prhs[2], "sparse") || mxIsChar(prhs[2]) ){
        mexErrMsgTxt("Third argument must be an image (two dimensional matrix)");
    }
    
    const int x = mxGetScalar(prhs[0])-1;
    const int y = mxGetScalar(prhs[1])-1;
    
    const int rowCount    = mxGetM(prhs[2]);
    const int columnCount = mxGetN(prhs[2]);
    const double* image   = mxGetPr(prhs[2]);
    
    const int interval = mxGetScalar(prhs[3]);
    
    if( rowCount==0 || columnCount==0 ){
        mexErrMsgTxt("Zero dimension in image");
    }
      
    if( x<0 || x>=columnCount){
        mexErrMsgTxt("x coordinate out of range");
    }
    
    if( y<0 || y>=rowCount){
        mexErrMsgTxt("y coordinate out of range");
    }    
    
    if( interval<1 ){
        mexErrMsgTxt("interval must be >=1");
    }    
    
    if(rowCount%2==0 || columnCount%2==0){
        mexPrintf("WARNING: For best results the image width and height should be numbers that fulfill:\n width mod interval==1 && height mod interval==1!\n");
    }
    
    
    
    ConvolutionImage ci(rowCount, columnCount);
    Ray ray(rowCount+columnCount);
    
    for( int i=0; i<columnCount; i+=interval){  
        ray.bresenham(x, y, i, 0, rowCount, image);
        ci.drawRay(ray);              
        ray.bresenham(x, y, i, rowCount-1, rowCount, image);
        ci.drawRay(ray);
    }
    
    for( int i=interval; i<rowCount-1; i+=interval){   
        ray.bresenham(x, y, 0, i, rowCount, image);
        ci.drawRay(ray);
        ray.bresenham(x, y, columnCount-1 , i, rowCount, image);
        ci.drawRay(ray);
    }

    plhs[0] = mxDuplicateArray(prhs[2]);
    memcpy(mxGetPr(plhs[0]), ci.cImage, ci.size*sizeof(double));
}



