#include <yvals.h>
#if (_MSC_VER >= 1400)
    #define __STDC_UTF_16__
#endif
        
#include "mex.h" 
#include "DisjointSets.h"


/*
 *
 * cmayer@bsse.ethz.ch 1/2012
 * 
 * edgeJoin(labels, u, v)
 *
 * simple linkage clustering by set joining 
 * 
 * u and v are arrays of the same length that specify the endpoints 
 * of connecting edges.
 * u and v must contain only values contained in the labels array (integers)
 *
 * Initially, every element of labels is in its own set (unit partition)
 *
 * For every edge given by ( u[i], v[i] )
 * i=1..n where n=length(u)=length(v) is the number of edges,
 * the two sets containing u[i] and v[i], respectively,
 * are merged if they are still disjoint.
 *
 * Returns a cell array with the resulting partition.
 * The number of cells in the cell array is the number
 * of cells in the final partition.
 * Each cell contains an array with the labels of the elements in this cell.
 *
 */

void mexFunction(int nlhs, mxArray *plhs[], int nrhs, const mxArray *prhs[]) {
    

    if (nrhs != 3){
        mexErrMsgTxt("Expected 3 arguments:\n\n  edgeJoin(labels, u, v)\n\n");
    }
   
    const double* labels        = mxGetPr(prhs[0]);
    const int nl      = mxGetNumberOfElements(prhs[0]);

    
    const double* u = mxGetPr(prhs[1]);
    const int nu    = mxGetNumberOfElements(prhs[1]);

    const double* v = mxGetPr(prhs[2]);
    const int nv    = mxGetNumberOfElements(prhs[2]);
    
    if( nv!=nu ){
        mexErrMsgTxt("Vectors must have same length");
    }
    
    DisjointSets dj(nl);
    
    for(int i=0; i<nl; ++i){
        dj.addLabel(labels[i]);
    }
    
    for(int i=0; i<nv; ++i){
        dj.merge(u[i], v[i]);
    }
    
    const int* partition = dj.getPartition();
    
    
    int *sizes = new int[nl];
    memset(sizes, 0, nl*sizeof(int));
    for(int i=0; i<nl; ++i){
        ++sizes[partition[i]];
    }

    const int dim[1] = {dj.getNumberOfSets()};
    plhs[0] = mxCreateCellArray(1, dim);
    
    
    for(int i=0; i<dj.getNumberOfSets(); ++i){
         mxSetCell(plhs[0], i, mxCreateNumericMatrix(sizes[i], 1, mxDOUBLE_CLASS, mxREAL));    
    }
    
    memset(sizes, 0, nl*sizeof(int));
    for(int i=0; i<nl; ++i){       
        double *ptr = (double*)mxGetData(mxGetCell(plhs[0], partition[i]));
        *(ptr + sizes[partition[i]]) = labels[i];
        ++sizes[partition[i]];
    }

    delete [] sizes;
    delete [] partition;    
}



