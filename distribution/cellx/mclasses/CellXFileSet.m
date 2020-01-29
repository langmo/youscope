classdef CellXFileSet < handle
    %UNTITLED5 Summary of this class goes here
    %   Detailed explanation goes here
    
    properties (SetAccess=private, GetAccess=public)
        
        % the frame index of the file set
        frameIdx;
           
        % 
        oofImage;
        
        % 
        fluoFiles = {};
        
        flatFieldFiles = {};
        
        % Identifiers for fluorescence images (e.g. gfp, gre,
        fluoTags = {};
        
        % the directory where the results should be saved
       resultsDir; 
       
       
       % result file names
       
       seedsTxtFile;
       seedsMatFile;
       maskMatFile;
       maskPngFile;
       controlImageFile;
       seedingImageFile;
    end
    
    
    properties (Constant)
        seedPrefix = 'cells';
        maskPrefix = 'mask';
        controlPrefix = 'control';
        seedingPrefix = 'seeding';
    end
    
    methods
        
        % constructor
        % varargin: the control image filename can be specified explicitly (full path) 
        % and needs not to reside within the results dir (used for calibration gui) 
        function this = CellXFileSet(frameIdx, oofImage) 
          this.frameIdx = frameIdx;
          this.oofImage = oofImage;      
        end
        
        % generates default filenames in the result dir
        function setResultsDirectory(this, dir)
            
            if( dir(end-1:end)~=filesep )
                dir = [dir filesep];
            end
            this.resultsDir = dir;
            base = ['_' num2str(this.frameIdx)];
            this.seedsTxtFile = [dir this.seedPrefix base '.txt'];
            this.seedsMatFile = [dir this.seedPrefix base '.mat'];
            this.maskMatFile  = [dir this.maskPrefix base '.mat'];
            this.maskPngFile  = [dir this.maskPrefix base '.png'];
            this.controlImageFile = [dir this.controlPrefix base '.png'];
            this.seedingImageFile = [dir this.seedingPrefix base '.png'];
            this.ensureResultsDirExists();
        end
        
        
        %for custom result filenames
        
        function setControlImageFileName(this, f)
            this.segmentationControlImageFile = f;
        end
        
        
        function setSeedsTxtFileName(this, f)
            this.seedsTxtFile = f;
        end

        function setSeedsMatFileName(this,f)
            this.seedsMatFile = f;
        end
        
        
        function setMaskMatFileName(this,f)
            this.maskMatFile = f;
        end
        
        function setMaskPngFileName(this,f)
            this.maskPngFile = f;
        end
        
        
        function fmt = getControlImageFormat(this)
            [~ , ~, ext] = fileparts(this.controlImageFile);
            if( length(ext)>2 )
                fmt = ext(2:end);
            else
                fmt = 'jpg';
            end
        end
        
        function addFluoImage(this, fluoImgFile, varargin)           
            [~, name] = fileparts(fluoImgFile);
            tag = regexp(name, '^(.+?)_', 'tokens');
            if(numel(tag)~=1)
                error('Cannot parse fluo tag, please use addFluoImageTag(file, tag) method');
            else
              this.addFluoImageTag(fluoImgFile, char(tag{1}), varargin);              
            end          
        end
        
        
        function addFluoImageTag(this, fluoImgFile, tag, varargin)
            this.fluoTags{end+1}  = tag;
            this.fluoFiles{end+1} = fluoImgFile;
            if( size(varargin,2)>0 )
                this.flatFieldFiles{numel(this.fluoTags)} = char(varargin{1});
            else
                this.flatFieldFiles{numel(this.fluoTags)}  = '';
            end
        end
        
        
        function n = getNumberOfFluoImages(this)
            n = numel(this.fluoTags);
        end

        function tag = getFluoTag(this, fluoIdx)
            tag = this.fluoTags{fluoIdx};
        end
        
        function f = getFluoImage(this, idx)
            f = this.fluoFiles{idx};
        end
        
        function f = getSeedsTxtFileName(this)
            f = this.seedsTxtFile;
        end
        
        function b = hasFlatFieldImage(this, idx)
            b = ~strcmp(this.flatFieldFiles{idx}, '');
        end
        
        function f = getFlatFieldImage(this, idx)
            f = this.flatFieldFiles{idx};
        end
        
        
        function f = getSeedsMatFileName(this)
             f = this.seedsMatFile;
        end
             
        function f = getMaskPngFileName(this)
              f = this.maskPngFile;
        end
        
        function f = getMaskMatFileName(this)
            f = this.maskMatFile;
        end
        
        function ensureResultsDirExists(this)
            if( ~exist(this.resultsDir, 'dir') )
                mkdir(this.resultsDir);
            end
        end

    end
    
    
    
end

