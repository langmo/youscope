// cmayer@bsse.ethz.ch, 1/2012
// Skiena's datastructure for disjoint datasets/partition
#include<cmath>
//memset
#include<string.h>
#include <map>
using namespace std;

#ifndef DISJOINT_SETS_H
#define DISJOINT_SETS_H

class DisjointSets{
    
private:
    int* forest;
    int* treeSize;
    map<int,int> map2i;
    int* i2map;
    int mapped;
    int forestSize;
    const int elementNumber;

public:
    
    DisjointSets(const int elementNumber):elementNumber(elementNumber){
        forest   = new int[elementNumber];
        treeSize = new int[elementNumber];
        for(int i=0; i<elementNumber; ++i){
           forest[i]    = i;
           treeSize[i]  = 1;
        }
        forestSize = elementNumber;
        i2map = new int[elementNumber];
        memset(i2map, -1, elementNumber*sizeof(int));
        mapped=0;
    }
    
    ~DisjointSets(){
        delete [] forest;
        delete [] treeSize;
        delete [] i2map;
    }

    void addLabel(int u){

        map<int,int>::iterator it = map2i.find(u);
        
        if( it==map2i.end() ){
            if(mapped<elementNumber){
                map2i[u] = mapped;
                i2map[mapped] = u;
                ++mapped;
            }else{
                mexErrMsgTxt("Cannot register more elements!");
            }
        }else{
            mexErrMsgTxt("Element already registered!");
        }
    }
    
    /**
     * Merges the cells that contain u and v.
     * @param p
     * @return True if i and j were in different cells.
     */
     bool merge(int u, int v){
        map<int,int>::iterator it = map2i.find(u);
        if(it==map2i.end() ) mexErrMsgTxt("Element not registered!");
        const int ri = findRootOf( it->second );
        it = map2i.find(v);
        if(it==map2i.end() ) mexErrMsgTxt("Element not registered!");
        const int rj = findRootOf( it->second );
        
        if( ri==rj ) return false;

        if( treeSize[ri]>treeSize[rj] ){
            forest[rj] = ri;
            treeSize[ri] += treeSize[rj];
        }else{
            forest[ri] = rj;
            treeSize[rj] += treeSize[ri];
        }
        --forestSize;
        return true;
    }

    int findRootOf(int i){
        if(forest[i]==i) return i;
        else return findRootOf(forest[i]);
    }
   
    int getNumberOfSets(){
        return forestSize;
    }

    const int* getMap(){
        return i2map;
    }
    
    int* getPartition(){
        int *ret = new int[elementNumber];
        int* root2SetIdx = new int[elementNumber];
        memset( root2SetIdx, -1, elementNumber*sizeof(int) );
        int setCounter=0;
        for(int i=0; i<elementNumber; ++i){
            const int r = findRootOf(i);
            if( root2SetIdx[r]==-1){
                 root2SetIdx[r] = setCounter++;
            }
            ret[i] = root2SetIdx[r];
        }
        delete [] root2SetIdx;
        return ret;
    }
};

#endif

