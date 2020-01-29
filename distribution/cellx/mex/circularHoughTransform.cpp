#include "mex.h" 

#include "matrix.h"

#define _USE_MATH_DEFINES
#include<cmath>
#include<list>
#include<stdio.h>
#include<string.h>

using namespace std;


//int p2i(int x, int y, int rowCount){return x*rowCount+y;}
/*
void rasterCircle(int x0, int y0, int radius, double* accumulationImage, int rowCount, int columnCount);

void accumulate(const double* image, double* accumulationImage, int rowCount, int columnCount, int radius, const double thr);

bool isEdge(int x, int y, int rowCount, const double* image, const double thr);

void accumulatePoint(int x, int y, int rowCount, int columnCount,double* accumulationImage);

double edge(int i, int j, int rowCount, int columnCount, const double* image);

void extractMaxima(double* image, int rowCount, int columnCount, int minDist, bool);
*/

class Cell{
public:
    int x,y,r;
    Cell(int x, int y, int r):x(x),y(y),r(r){}
};


class GaussianSmoothingFilter{

    public:
        
        double** filter;
        const int w;
        const int r;
        
        GaussianSmoothingFilter(int r):w(1+2*r),r(r){
            filter = new double*[w];
            for(int i=0; i<w; ++i){
                filter[i] = new double[w];
            }
                       
            const double sigma = 2*(r/3.0);
            
            double sum=0.0;
            for(int i=0; i<w; ++i){
                for( int j=0; j<w; ++j ){                   
                    const int x = i-r;
                    const int y = j-r;                 
                    const double f = exp(- (x*x/sigma + y*y/sigma) );                    
                    filter[i][j] = f;
                    sum +=f;
                }
            }
            for(int i=0; i<w; ++i){
                for( int j=0; j<w; ++j ){                                      
                    filter[i][j]/=sum;
                }
            }
        }
    
        
        ~GaussianSmoothingFilter(){
            for(int i=0; i<w; ++i){
                delete [] filter[i];
            }
            delete [] filter;          
        }
        
        void print(){
            mexPrintf("Smoothing filter: \n\n");
           for(int i=0; i<w; ++i){
                for( int j=0; j<w; ++j ){
                    mexPrintf("%6.5f ", filter[i][j]);
                }
                mexPrintf("\n");
            }
            mexPrintf("\n");
        }
    
        
        double computeSmoothedImage(const double* src, double* dest, int rows, int cols){      
            double val,norm;  
            int startX,startY,endX,endY, srcX, srcY;
            double maxVal = 0.0;            
            for(int i=0; i<cols; ++i){               
                startX = max(-i, -r);
                endX   = min(cols-i, r); 
                
                for(int j=0; j<rows; ++j){                    
                    val    = 0.0;    
                    norm   = 0.0;
                    startY = max(-j, -r);
                    endY   = min(rows-j, r);

                    for(int k=startX; k<endX; ++k){
                        srcX = i+k;
                        for(int l=startY; l<endY; ++l){
                            srcY = j+l;
                            val += src[srcX*rows+srcY] * filter[k+r][l+r];
                            norm += filter[k+r][l+r];
                        }
                    }                    
                   // val/=norm;
                    dest[i*rows+j] = val;
                    maxVal = max(maxVal, val);                    
                }            
            }
            
            mexPrintf("max=%f\n", maxVal );
            return maxVal;
        }
        
};



class CircularHoughTransform{
public:
    const double *srcImage;
    
    /* 
     * w : width of the image (= #columns)
     * h : height of the image (= #rows)
     */    
    const int w,h;
    
    CircularHoughTransform(const int h, const int w, const double *srcImage):h(h), w(w), srcImage(srcImage){
    }
    
    void addHoughTransform(const int radius, const double gradientThreshold, double* dest){        
        /*
         * consider pixels [1..w-1][1..h-1] only to avoid special cases in
         * the computation of the gradient of pixels on the border.
         */      
        int c=0;
         //mexPrintf("Gradient threshold %f\n", gradientThreshold);
        for(int i=0; i<w; ++i){
            for(int j=0; j<h; ++j){
                const double gradient  = computeGradientAt(i, j);
                if( gradient > gradientThreshold ){
                    ++c;                 
                    rasterCircle(i, j, radius, dest, gradient);
                } 
               
            }
        }
        //mexPrintf("Rastered %d circles\n",c);
        
    }
    
    /**
    * Circular bresenham (wikipedia)
    */
    void rasterCircle(int x0, int y0, int radius, double* dest, double intensity){
        int f = 1 - radius;
        int ddF_x = 1;
        int ddF_y = -2 * radius;
        int x = 0;
        int y = radius;
        
        accumulatePoint(x0, y0 + radius, dest, intensity);
        accumulatePoint(x0, y0 - radius, dest, intensity);
        accumulatePoint(x0 + radius, y0, dest, intensity);
        accumulatePoint(x0 - radius, y0, dest, intensity);
        
        while(x < y){
            if(f >= 0) {
                --y;
                ddF_y += 2;
                f += ddF_y;
            }
            ++x;
            ddF_x += 2;
            f += ddF_x;
            
            accumulatePoint(x0 + x, y0 + y, dest, intensity);
            accumulatePoint(x0 - x, y0 + y, dest, intensity);
            accumulatePoint(x0 + x, y0 - y, dest, intensity);
            accumulatePoint(x0 - x, y0 - y, dest, intensity);
            
            accumulatePoint(x0 + y, y0 + x, dest, intensity);
            accumulatePoint(x0 - y, y0 + x, dest, intensity);
            accumulatePoint(x0 + y, y0 - x, dest, intensity);
            accumulatePoint(x0 - y, y0 - x, dest, intensity);
            
        }
    }
    
    void accumulatePoint(const int x, const int y, double* dest, double intensity){
        if(x>=0 && x<w && y>=0 && y<h) dest[x*h+y]+=intensity;
    }
 
    double computeGradientAt(const int x, const int y){
        const double c = srcImage[x*h+y];       
        double dx    = 0.0; 
        double norm  = 0.0;        
        if(x<w-1){        
            dx += c - srcImage[(x+1)*h+y];     
            ++norm;
        }
        if(x>0){        
            dx += srcImage[(x-1)*h+y] - c;     
            ++norm;
        }
        dx/=norm;
        
        double dy = 0.0;
        norm = 0.0;
        if( y<h-1 ){
            dy += c - srcImage[x*h+y+1];   
            ++norm;
        }
        if( y>0 ){
            dy += srcImage[x*h+y-1] - c;   
            ++norm;
        }
        dy/=norm;
        return sqrt(dx*dx + dy*dy);
    }
    
};


class MaximaCollector{

public:
    
    double* maxima;
    
    const int w,h;
    
    MaximaCollector(const int h, const int w):h(h),w(w){
        maxima = new double[h*w];
        memset(maxima, 0.0, h*w*sizeof(double));
    }
    ~MaximaCollector(){
        delete [] maxima;
    }

    void registerMaxima(double* src, double minVotes, int minDist){
        double *im = createMaximaImage(src, minVotes, minDist);
        registerMaxima(im);
        delete [] im;
    }

    void extractCenters(int minDist, list<Cell>& cells){
        double *remainingMaxima = createMaximaImage(maxima, 0.0, minDist);
        for(int i=0; i<w; ++i){
            for(int j=0; j<h; ++j){
                if( remainingMaxima[i*h+j]!=0.0 ){
                    Cell c(i,j,1);
                    cells.push_back(c);
                }
            }
        }
        delete [] remainingMaxima;
    }


private:
    
    double* createMaximaImage(double* src, double minVotes, int minDist){
        
        double* image = new double[h*w];      
        memcpy(image, src, h*w*sizeof(double));
        
        double m, value;
        int x, y;
        bool foundLocalMax;

        for(int i=0; i<w-minDist+1; ++i){
            for(int j=0; j<h-minDist+1; ++j){
                //search for local maximum in minDist x minDist window
                m = minVotes;
                foundLocalMax = false;
                for(int k=i; k<i+minDist; ++k){
                    for(int l=j; l<j+minDist; ++l){
                        value = image[k*h+l];
                        if( value>m ){
                            m = value;
                            x = k;
                            y = l;
                            foundLocalMax=true;
                        }
                        image[k*h+l] = 0.0;
                    }
                }               
                if( foundLocalMax ){
                    image[x*h+y] = m;
                }
            }          
        }
        return image;
    }



    void registerMaxima(double* image){
     //collect the remaining points
        for(int i=0; i<w; ++i){
            for(int j=0; j<h; ++j){
                if( image[i*h+j]!=0.0 ){
                    maxima[i*h+j] = image[i*h+j];
                }
            }
        }
    }

    
};


void mexFunction(int nlhs, mxArray *plhs[], int nrhs, const mxArray *prhs[]) {
    
    // input arguments xWidth, yWidth, connectivity
    if (nrhs !=4)
        mexErrMsgTxt("4 arguments expected (image,radii,thr,smoothing)!");

    if ( mxIsComplex(prhs[0])|| mxIsClass(prhs[0], "sparse") || mxIsChar(prhs[0]) )
        mexErrMsgTxt("Argument 0 must be real, full, and nonstring");
    
    if ( mxIsComplex(prhs[1])|| mxIsClass(prhs[1], "sparse") || mxIsChar(prhs[1]) )
        mexErrMsgTxt("Argument 1 must be real, full, and nonstring");
    
    
    const int rowCount    = mxGetM(prhs[0]);
    const int columnCount = mxGetN(prhs[0]);
    mexPrintf("rows=%d columns=%d\n", rowCount, columnCount);  
    
    if( rowCount==0 || columnCount==0 ){
        mexErrMsgTxt("Matrix must be two dimensional!");
    }
    const double* image = mxGetPr(prhs[0]);
    
    const double* radii = mxGetPr(prhs[1]);
    const int radiiCount = mxGetNumberOfElements(prhs[1]);
    
    const double thr = mxGetScalar(prhs[2]);
    mexPrintf("Edge detection threshold: %6.4f\n", thr);
    
    
    GaussianSmoothingFilter gf(radii[1]);   
    gf.print();
    
    CircularHoughTransform cht(rowCount, columnCount, image);
    
    const int size = rowCount*columnCount;
    double* accumulationArray = new double[size];
    double* smoothedArray = new double[size];
    
    // for each radius
    
    memset(accumulationArray, 0.0, sizeof(double)*size);   
    
    for( int r=radii[0]; r<radii[1]; r+=2)
        cht.addHoughTransform(r, thr, accumulationArray);
    
    
    double maximum = gf.computeSmoothedImage(accumulationArray, smoothedArray, rowCount, columnCount);

    MaximaCollector mc(rowCount, columnCount);
    mc.registerMaxima(smoothedArray, 0.2*maximum, radii[0]);
    list<Cell> cells;
    mc.extractCenters(2*radii[0], cells);
    
    
    int seedCount=cells.size();
    mxArray *seedCenters = mxCreateNumericMatrix(seedCount, 2, mxDOUBLE_CLASS, 0);
    mxArray *seedRadii   = mxCreateNumericMatrix(seedCount, 1, mxDOUBLE_CLASS, 0);
    
    
    double *seedCentersData = mxGetPr(seedCenters);
    double *seedRadiiData   = mxGetPr(seedRadii);
            
            
    list<Cell>::iterator it = cells.begin();
    int i=0;
    while( it!=cells.end() ){
        const Cell c = *it++;
        seedCentersData[i]   = c.x+1;
        seedCentersData[i+seedCount] = c.y+1;
        seedRadiiData[i]       = c.r;
        ++i;
    }    
    plhs[0] = seedCenters;
    plhs[1] = seedRadii;
    
    plhs[2] = mxCreateNumericMatrix(rowCount, columnCount, mxDOUBLE_CLASS, 0);
    mxSetData(plhs[2], accumulationArray);
    
    plhs[3] = mxCreateNumericMatrix(rowCount, columnCount, mxDOUBLE_CLASS, 0);
    mxSetData(plhs[3], smoothedArray);
    
    //double* dest = mxGetPr(plhs[3]);
    //memcpy(dest, mc.maxima, sizeof(double)*rowCount*columnCount);
    
    
    mexPrintf("done.\n");
    
}

