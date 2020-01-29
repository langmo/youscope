outDir = ['..' filesep 'mex'];
mex( '-outdir', outDir, ['..' filesep 'mex' filesep 'computeRayConvolution.cpp']);
mex( '-outdir', outDir, ['..' filesep 'mex' filesep 'initGridGraph.cpp']);
mex('-largeArrayDims', '-outdir', [outDir filesep 'maxflow'], ['..' filesep 'mex' filesep 'maxflow' filesep 'maxflowmex.cpp']); 
mex('-outdir', outDir, ['..' filesep 'mex' filesep 'findRegionCentersInCircularArray.cpp']); 
mex('-outdir', outDir, ['..' filesep 'mex' filesep 'edgeJoin.cpp']); 