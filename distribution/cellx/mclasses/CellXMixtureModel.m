classdef CellXMixtureModel < handle
    %CELLXMIXTUREMODEL Summary of this class goes here
    %   Detailed explanation goes here
    
    properties(SetAccess=private)
        % input arguments
        % a vector with the possible number of 
        % gaussians to be tested
       nrOfGaussiansVector;
       
       % outputs 
       gaussianMeanVals;
       gaussianVarVals;
       gaussianMixingProportions;
       gaussianAICVals;
       gaussianBICVals; 
       gaussianNlogLVals;
       gaussianmuVals
    end
    
    methods
        function obj = CellXMixtureModel(nrOfGaussians)
            obj.nrOfGaussiansVector = nrOfGaussians;
        end
        
        function ret = getStdValues(this,varargin)
           tmp = sqrt(squeeze(this.gaussianVarVals));   
           if( size(varargin,1)>0)
                ret = tmp(varargin{1}); 
            else
                ret = tmp;
           end       
        end
        
        function ret = getMeanValues(this, varargin)
            if( size(varargin,1)>0)
                ret = this.gaussianMeanVals(varargin{1}); 
            else
                ret = this.gaussianMeanVals();
            end
        end
        
          function ret = getMixingProportions(this)
           ret = this.gaussianMixingProportions;
        end
        
        function computeMixtureModel(this,vectorValues,varargin)
           
            % if mask exists
            if size(varargin,1)==1
                % apply the mask 
                vectorValues = vectorValues(varargin{1});
            elseif size(varargin,1)>1
                error('Too many input arguments...')
            end
            
            % vectorize if the input is an image
            if size(vectorValues,2)>1
                vectorValues =vectorValues(:);
            end
            
            % select subset of pixels
            % if number of vector values are
            % more than 1e5
            NrOfInitialSamples = length(vectorValues);
            if NrOfInitialSamples > 5*1e4;
               % generate  1e5 random indices
               randInd = unidrnd(NrOfInitialSamples,1, 5*1e4);
               vectorValues = vectorValues(randInd);
            end
            
            % remove the tails of the 
            % histogram, so that the gaussian 
            % fit becomes more precice
             if numel(vectorValues)>5e3
                   nrbins=100;
             else
                   nrbins = round(numel(vectorValues)/50);                 
             end
             
             [n,xout] = hist( vectorValues,nrbins);
             MinNumberOfCountsPerBin = 0.1*length(vectorValues)/nrbins;
             % find the accepted bins
             AccBins = find(n>MinNumberOfCountsPerBin);
             % restore vector Values
             vectorValues_temp =[];
             for nrab = 1:length(AccBins)
                vectorValues_temp = [vectorValues_temp;...
                    repmat(xout(AccBins(nrab)),n(AccBins(nrab)),1)];
             end
             vectorValues = vectorValues_temp;
             
             
             % check vector values
             contTag = numel(unique(vectorValues))< max(this.nrOfGaussiansVector);
             if contTag
                 gaussianNumber =  numel(unique(vectorValues));
             end
                         
             if ~contTag
                 
                 % find the best number of gaussians
                 % that minimizes the AIC information
                 nrPossibleGaussians = length(this.nrOfGaussiansVector);
                 allres = cell(nrPossibleGaussians,1);
                 allAIC = zeros(nrPossibleGaussians,1);
                 allBIC = zeros(nrPossibleGaussians,1);
                 allNlogL = zeros(nrPossibleGaussians,1);
                 allmuVals = cell(nrPossibleGaussians,1);
                 % loop through the possible nr of gaussians
                 
                 for k=1:nrPossibleGaussians
                     
                     gaussianNumber = this.nrOfGaussiansVector(k);
                     
                     gmoptions = statset('MaxIter',500,'TolX',1e-6);
                     try
                         warning('off', 'stats:gmdistribution:FailedToConverge');
                         tempres = gmdistribution.fit(vectorValues,gaussianNumber,...
                             'Replicates',3,...
                             'Options',gmoptions);
                         
                         allres{k,1} = tempres;
                         allAIC(k,1) = tempres.AIC;
                         allBIC(k,1) = tempres.BIC;
                         allNlogL(k,1) = tempres.NlogL;
                         allmuVals{k,1} = tempres.mu;
                     catch
                         allres{k,1} = NaN;
                         allAIC(k,1) = NaN;
                         allBIC(k,1) = NaN;
                         allNlogL(k,1) = NaN;
                         allmuVals{k,1} = NaN;
                     end
                 end
                 
                 if any(isnan(allNlogL)) || isempty(allNlogL)
                     % TODO do not output this message 
                     warning('WarningMsg:MM','Number of suggested models for \n Mixture Modeling is wrong!!!')
                     
                     uniqueVectorValues = mean(vectorValues);
                     this.gaussianMeanVals = uniqueVectorValues;
                     this.gaussianVarVals =  var(vectorValues);
                     if  this.gaussianVarVals==0
                          this.gaussianVarVals = ...
                           repmat(numel(vectorValues)*eps,numel(uniqueVectorValues),1);
                     end
                     
                     for nrg = 1: numel(uniqueVectorValues)
                         this.gaussianMixingProportions(nrg,1) = sum(vectorValues == uniqueVectorValues(nrg))/numel(vectorValues) ;
                     end
                     
                     this.gaussianAICVals = NaN;
                     this.gaussianBICVals = NaN;
                     this.gaussianNlogLVals = NaN;
                     this.gaussianmuVals =  unique(vectorValues);
                     
                     
                 else
                     
                     % finalize the choice based on the minimum Negative
                     % log likelihood slope (see testMixtureModel.m function
                     % for details)
                     % substract the min NlogL for each case
                     
                     NlogLdiffs = allNlogL - min(allNlogL);
                     NlogLThresh = abs(0.025*min(allNlogL));
                     % find the ones which are less than the threshold
                     % and take the first one
                     ModelsThresholded = find( NlogLdiffs  <NlogLThresh);
                     OptModelIndex =  ModelsThresholded(1);
                     res =  allres{OptModelIndex};
                     
                     %[~,numComponents] = min(allAIC);
                     % res =  allres{numComponents};
                     
                     this.gaussianMeanVals = res.mu;
                     this.gaussianVarVals = res.Sigma;
                     this.gaussianMixingProportions = res.PComponents;
                     this.gaussianAICVals = allAIC;
                     this.gaussianBICVals = allBIC;
                     this.gaussianNlogLVals = allNlogL;
                     this.gaussianmuVals =  allmuVals; 
                                          
                 end
                 
                 
             else % case of conTag==1
                 
                 warning('WarningMsg:MM','Number of suggested models for \n Mixture Modeling is wrong!!!')
                 uniqueVectorValues = unique(vectorValues);
                 this.gaussianMeanVals = uniqueVectorValues;
                 this.gaussianVarVals = repmat(numel(vectorValues)*eps,numel(uniqueVectorValues),1);
                 
                 for nrg = 1: numel(uniqueVectorValues)
                     this.gaussianMixingProportions(nrg,1) = sum(vectorValues == uniqueVectorValues(nrg))/numel(vectorValues) ;
                 end
                 
                 this.gaussianAICVals = NaN;
                 this.gaussianBICVals = NaN;
                 this.gaussianNlogLVals = NaN;
                 this.gaussianmuVals =  unique(vectorValues);
                              
             end
             
             
        end
        
        function r = getOptimalNumberOfGaussians(this)
            r = length(this.gaussianMeanVals);            
        end
        
    end
    
end

