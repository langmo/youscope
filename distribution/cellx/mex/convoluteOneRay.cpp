#include "mex.h" 
#include "Ray.h"



/*
 * This function computes the cross correlation of a vector and a membraneSignal 
 *
 * The vector will be normalized to the (0,1) range and all values are 
 * shifted by the mean such that they are centered around 0.
 * 
 * The membraneSignal is assumed to be normlized already (this is not checked)
 *
 *
 * At the left end (begin) of the vector, partial convolution with the mebraneSignal is computed.
 * For this, the signal is zero padded.
 *
 * The left-most (first) value in the result vector corresponds to the convolution
 *
 *  vector:      VVVVVVVVVVVV
 *  signal:   IIImIIII000 
 *               |------| = result correlation
 *       01...i....(S-1)
 * where I...ImI...I is the membraneSignal and m is the membraneOffset.
 * 
 * and so on: 
 * vector:      VVVVVVVVVVVV
 * signal:   IIImIIII000 -> a
 *            IIImIIII00 -> b
 *             IIImIIII0 -> c
 *              IIImIIII -> d
 *
 * result:      abcd
 *
 * The location of m with respect to V denotes the location of the correlation value in the result vector. 
 * The zero padded membrane signal is re-normalized in the border cases. 
 *
 * This function is not very efficient and intended for testing/plotting purposes only.
 *
 */
void mexFunction(int nlhs, mxArray *plhs[], int nrhs, const mxArray *prhs[]) {
    
    if (nrhs != 3){
        mexErrMsgTxt("Expected 3 arguments:\n\n  computeRayConvolution(vector, membraneSignal, membraneOffset)\n\n");
    }
   
    const double* vector         = mxGetPr(prhs[0]);
    const int vectorLength       = mxGetNumberOfElements(prhs[0]);
    
    const double* membraneSignal = mxGetPr(prhs[1]);
    const int signalLength       = mxGetNumberOfElements(prhs[1]);
    const int membraneOffset     = mxGetScalar(prhs[2])-1;

    Ray r(vectorLength);
    r.bresenham(0,0,0,vectorLength,0,vector);
    r.convoluteWithOffsetZeroPad(membraneSignal, signalLength, membraneOffset); 
    //r.print();
    mxArray* ret = mxCreateNumericMatrix(vectorLength, 1, mxDOUBLE_CLASS, mxREAL);
    memcpy(mxGetPr(ret), r.convolution,  r.size*sizeof(double));
    plhs[0] = ret;
   
}