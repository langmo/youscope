#include <yvals.h>
#if (_MSC_VER >= 1400)
    #define __STDC_UTF_16__
#endif
        
#include "mex.h" 

#include "matrix.h"

#define _USE_MATH_DEFINES
#include<cmath>

struct Properties{
    double distance,angle;
};

const int getEdgeCount(const int xWidth, const int yWidth, const int connectivity, const int directed);
const Properties getProperties(const int u, const int v, const int xWidth);
const void setEdge(const int u, const int v, unsigned int*& c1, unsigned int*& c2, double*& dist, double*& angle, const int xWidth, const int directed);

const double PI_2   = M_PI/2.0;
const double PI_4   = M_PI/4.0;
const double SQRT_2 = sqrt(2.0);

void mexFunction(int nlhs, mxArray *plhs[], int nrhs, const mxArray *prhs[]) {
    
    // input arguments xWidth, yWidth, connectivity
    if (nrhs !=4)
        mexErrMsgTxt("Must have 4 arguments: xWidth, yWidth, connectivity, directed");

    if ( mxIsComplex(prhs[0])|| mxIsClass(prhs[0], "sparse") || mxIsChar(prhs[0]) )
        mexErrMsgTxt("Input must be real, full, and nonstring");
    
    const unsigned int xWidth = mxGetScalar(prhs[0]);
    const unsigned int yWidth = mxGetScalar(prhs[1]);
    const unsigned int connectivity = mxGetScalar(prhs[2]);
    const unsigned int directed = mxGetScalar(prhs[3]);
    
    if( connectivity!=4 && connectivity!=8 )
        mexErrMsgTxt("Connectivity must be 4 or 8");
    
    if( xWidth<2 )
        mexErrMsgTxt("xWidth must be > 2");
    
    if( yWidth<2 )
        mexErrMsgTxt("yWidth must be > 2");
    
    if( directed!=0 && directed!=1){
        mexErrMsgTxt("4th argument 'directed' must be 0 or 1\n");
    }
   //mexPrintf("xWidth:%d  yWidth:%d  connectivity:%d\n", xWidth, yWidth, connectivity);
   
   //mexPrintf("Number of edges: %d\n", getEdgeCount(xWidth,yWidth,connectivity));
   
   const int edgeCount = getEdgeCount(xWidth, yWidth, connectivity, directed);
    
    plhs[0] = mxCreateNumericMatrix(edgeCount, 2, mxUINT32_CLASS, mxREAL);
    plhs[1] = mxCreateDoubleMatrix(edgeCount, 2, mxREAL);
    
    unsigned int* data = (unsigned int*)mxGetData(plhs[0]);
    double* props = (double*)mxGetData(plhs[1]);
 
    unsigned int *ptrColumn1 = data;
    unsigned int *ptrColumn2 = data+edgeCount;
    
    double *ptrDist  = props;
    double *ptrAngle = props+edgeCount;
    
    Properties p;
    
    for(int i=0; i<yWidth; ++i){
        for( int j=0; j<xWidth; ++j){
            const unsigned int v = i*xWidth + j + 1;
            if( i==yWidth-1 ){
                if( j!=xWidth-1 ){
                    // create bottom edges
                    setEdge(v, v+1, ptrColumn1, ptrColumn2, ptrDist, ptrAngle, xWidth, directed);
                }
            }else{
                if( j==0 ){
                    // left border - create 3 edges [4x4]: (1,5) (1,6) (1,2)
                    setEdge(v, v+1, ptrColumn1, ptrColumn2, ptrDist, ptrAngle, xWidth, directed);
                    
                    setEdge(v, v+xWidth, ptrColumn1, ptrColumn2, ptrDist, ptrAngle, xWidth, directed);
                                      
                    if(connectivity==8){
                        setEdge(v, v+xWidth+1, ptrColumn1, ptrColumn2, ptrDist, ptrAngle, xWidth, directed);
                    }
                    
                }else if( j==xWidth-1 ){
                    // right border - 2 edges [4x4]: (4,7) (4,8)
                    if(connectivity==8){
                        setEdge(v, v+xWidth-1, ptrColumn1, ptrColumn2, ptrDist, ptrAngle, xWidth, directed);
                    }
                    setEdge(v, v+xWidth, ptrColumn1, ptrColumn2, ptrDist, ptrAngle, xWidth, directed);
                }else{
                    //create 4 edges (2,5) (2,6) (2,7) (2,3)
                    setEdge(v, v+1, ptrColumn1, ptrColumn2, ptrDist, ptrAngle, xWidth, directed);

                    setEdge(v, v+xWidth, ptrColumn1, ptrColumn2, ptrDist, ptrAngle, xWidth, directed);
                    
                    if(connectivity==8){
                        setEdge(v, v+xWidth+1, ptrColumn1, ptrColumn2, ptrDist, ptrAngle, xWidth, directed);
                        
                        setEdge(v, v+xWidth-1, ptrColumn1, ptrColumn2, ptrDist, ptrAngle, xWidth, directed);
                    }
                }       
            }
        }
    }
} 

const int getEdgeCount(const int xWidth, const int yWidth, const int connectivity, const int directed){
    const int ret = (xWidth-1)*(yWidth-1)*connectivity/2 + xWidth + yWidth - 2;
    if( directed )
        return 2*ret;
    else
        return ret;
}        

const Properties getProperties(const int u, const int v, const int xWidth){
    Properties ret;
    const int d = abs(u-v);
    if( abs(xWidth - d)==1){
        ret.distance = SQRT_2;
    }else{
        ret.distance = 1.0;
    } 
    ret.angle=0.0;
    if( d==1 ){
      ret.angle = PI_2;
    }else if( d==xWidth+1 || d==xWidth-1 ){
      ret.angle = PI_4;
    }  
    return ret;
}

const void setEdge(const int u, const int v, unsigned int*& c1, unsigned int*& c2, double*& dist, double*& angle, const int xWidth, const int directed){
    *c1++ = u;
    *c2++ = v;     
    Properties p = getProperties(u, v, xWidth);
    *dist++  = p.distance;
    *angle++ = p.angle; 
    
    if(directed){
        *c1++ = v;
        *c2++ = u;     
        p = getProperties(v, u, xWidth);
        *dist++  = p.distance;
        *angle++ = p.angle; 
    }   
    
}


