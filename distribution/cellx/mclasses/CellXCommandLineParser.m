classdef CellXCommandLineParser < handle
    %CELLXCOMMANDLINEPARSER Summary of this class goes here
    %   Detailed explanation goes here
    
    properties (SetAccess=private)
        configFile = [];
        seriesFile = [];
        % one of {series,files,track} -> (1,2,3)
        execMode=1;
        segImage = [];
        fluoImages = cell(0,0);
        ffImages = cell(0,0);
        frameNumber = 1;
        % if defined, holds the index of the series that should be processed
        trackNumber=[];
        % if defined, holds the index of the file set that should be
        % processed
        setIndex=[];
        
        trackOnly=0;
        
        tracking = [];
        % 0 or 1
        testExit = [];
        resultDir;
        sourceDir;
        controlImage1 = [];
        controlImage2 = [];
        seedPrefix;
        maskPrefix;
        
        args;
    end
    
    
    properties (Constant)
        helpOption = '-h';
        modeOption = '-m';
        seriesOption = '-s';
        trackOption = '-t';
        resultDirOption = '-r';
        trackIdxOption = '-si';
        setIdxOption = '-sj';
        trackOnlyOption = '-st';
        
        frameIdxOption =  '-i';
        segImgOption = '-b';
        fluoImgOption = '-f';
        flatImgOption = '-ff';
        testExitPoint = '-cx';
        controlImg1Option = '-c1';
        controlImg2Option = '-c2';
        sourceDirOption = '-d';
        seedPrefixOption = '-p';
        maskPrefixOption = '-q';
    end
    
    methods
        
        
        function obj = CellXCommandLineParser()          
        end
        
        
        function r = hasTrackIndex(this)
            r = ~isempty(this.trackNumber);
        end
        
        function r = hasTrackingSwitch(this)
            r = ~isempty(this.tracking);
        end
        
        function r = hasResultDir(this)
            r = ~isempty(this.resultDir);
        end
        
        function r = isCalibrationMode(this)
            r = ~isempty(this.testExit);
        end
        
        function parse(this, args)
            this.args=args;
            
            n = size(args,2);
            if( n<1 )
                err = MException('CellXCommandLineParser:MissingParameterFile', ...
                'Missing parameter file (xml)');
                throw(err); 
            end
        
            this.configFile = args{1};
            CellXCommandLineParser.checkFileExists(this.configFile);
            k=2;
            while(k<=n)
                k = this.processArgument(k,n);
                k = k+1;
            end
            this.check();      
        end
        

        
    end
    
    
    methods (Access=private)
        
        
        function check(this)
            
            
            if( this.execMode==1 )
                if( isempty(this.seriesFile) )
                    err = MException(   'CellXCommandLineParser:MissingSeriesFile', ...
                                        'Missing series file (xml)');
                    throw(err); 
                end  
                
                if( ~isempty(this.setIndex) && isempty(this.trackNumber) )
                    err = MException(   'CellXCommandLineParser:MissingSeriesIndex', ...
                                        'The file set index option (%s) requires the series index option (%s)', this.setIdxOption, this.trackIdxOption);
                    throw(err); 
                end
                
                if( this.trackOnly==1 && isempty(this.trackNumber) )
                    err = MException(   'CellXCommandLineParser:MissingSeriesIndex', ...
                                        'The ''perform tracking only'' option (%s) requires the series index option (%s)', this.trackOnlyOption, this.trackIdxOption);
                    throw(err); 
                end
                
                if( this.trackOnly==1 && ~isempty(this.setIndex) )
                    err = MException(   'CellXCommandLineParser:IncompatibleOptions', ...
                                        'The ''perform tracking only'' option (%s) is incompatible wih the set index option (%s)', this.trackOnlyOption, this.setIdxOption);
                    throw(err); 
                end
                
                
                
            elseif(this.execMode==2)
            
                if( isempty(this.segImage) )
                    err = MException(   'CellXCommandLineParser:MissingFile', ...
                        sprintf('Missing segmentation image (%s)', this.segImgOption));
                    throw(err);
                end
                
                if( ~isempty(this.testExit) )
                    if( isempty(this.controlImage1) )
                        err = MException(   'CellXCommandLineParser:MissingFile', ...
                            sprintf('Missing filename for segmentation control image (%s)', this.controlImg1Option));
                        throw(err);
                    end        
                    if( this.testExit==1 )
                        if( numel(this.fluoImages)>0 && isempty(this.controlImage2) )
                            err = MException(   'CellXCommandLineParser:MissingFile', ...
                                sprintf('Missing filename for quantification control image (%s)', this.controlImg2Option));
                            throw(err);
                        end
                    end
                else
                    if( isempty(this.resultDir) )
                        err = MException(   'CellXCommandLineParser:MissingDirectory', ...
                            sprintf('Missing directory for results (%s)', this.resultDirOption));
                        throw(err);
                    end
                    
                    
                end
                
            elseif(this.execMode==3)
             
            end
            
            
        end
        
        
        
        
        
        
        function k = processArgument(this, k, n)

            arg = this.args{k};
            
            if( strcmp(arg, CellXCommandLineParser.helpOption) )
                err = MException('CellXCommandLineParser:UsageInvoked', ...
                    '');
                throw(err);
            end
            
            if( strcmp(arg, CellXCommandLineParser.modeOption) )
                
                [value,k] = this.getNext(k,n,arg);
                this.execMode = CellXCommandLineParser.getExecMode(value);
                
            elseif( strcmp(arg,  CellXCommandLineParser.seriesOption) )
                
                [value,k] = this.getNext(k,n,arg);
                CellXCommandLineParser.checkFileExists(value);
                this.seriesFile = value;
                
            elseif( strcmp(arg, CellXCommandLineParser.trackOption) )
                
                [value,k] = this.getNext(k,n,arg);
                value = CellXCommandLineParser.makeBoolNum(value, arg);
                this.tracking = value;
                
            elseif( strcmp(arg, CellXCommandLineParser.frameIdxOption) )
                
                [value,k] = this.getNext(k,n,arg);
                value = CellXCommandLineParser.makeNum(value, arg);
                this.frameNumber = value;
                
            elseif( strcmp(arg, CellXCommandLineParser.trackIdxOption) )
                  
                [value,k] = this.getNext(k,n,arg);
                value = CellXCommandLineParser.makeNum(value, arg);
                this.trackNumber = value;
                
            elseif( strcmp(arg, CellXCommandLineParser.resultDirOption) )
                
                [value,k] = this.getNext(k,n,arg);
                CellXCommandLineParser.makeDirectory(value);
                this.resultDir = value;
                
            elseif( strcmp(arg, CellXCommandLineParser.segImgOption) )
                
                [value,k] = this.getNext(k,n,arg);
                CellXCommandLineParser.checkFileExists(value);
                this.segImage = value;
                
            elseif( strcmp(arg, CellXCommandLineParser.fluoImgOption) )
                
                [value,k] = this.getNext(k,n,arg);
                CellXCommandLineParser.checkFileExists(value);
                this.fluoImages{end+1} = value;
                
            elseif( strcmp(arg, CellXCommandLineParser.flatImgOption) )
                
                [value,k] = this.getNext(k,n,arg);
                CellXCommandLineParser.checkFileExists(value);
                this.ffImages{end+1} = value;
                
            elseif( strcmp(arg, CellXCommandLineParser.controlImg1Option) )
                
                [value,k] = this.getNext(k,n,arg);
                this.controlImage1 = value;
                
            elseif( strcmp(arg, CellXCommandLineParser.controlImg2Option) )
                
                [value,k] = this.getNext(k,n,arg);
                this.controlImage2 = value;
                
            elseif( strcmp(arg, CellXCommandLineParser.testExitPoint) )
                
                [value,k] = this.getNext(k,n,arg);
                value = CellXCommandLineParser.makeBoolNum(value, arg);
                this.testExit = value;
                
            elseif( strcmp(arg, CellXCommandLineParser.sourceDirOption) )
                
                [value,k] = this.getNext(k,n,arg);
                CellXCommandLineParser.checkDirExists(value);
                this.sourceDir = value;
                
            elseif( strcmp(arg, CellXCommandLineParser.seedPrefixOption) )
                
                [value,k] = this.getNext(k,n,arg);
                this.seedPrefix = value;
                
            elseif( strcmp(arg, CellXCommandLineParser.maskPrefixOption) )
                
                [value,k] = this.getNext(k,n,arg);
                this.maskPrefix = value;
                                     
            elseif( strcmp(arg, CellXCommandLineParser.setIdxOption) )
                
                [value,k] = this.getNext(k,n,arg);
                value = CellXCommandLineParser.makeNum(value, arg);
                this.setIndex = value;   
                
            elseif( strcmp(arg, CellXCommandLineParser.trackOnlyOption) )
                
                this.trackOnly = 1;    
                
            else            
                err = MException('CellXCommandLineParser:UnknownArg', ...
                    ['Unknown argument: ''' arg '''']);
                throw(err);
            end
        end
         
        
        
        function [v,k] = getNext(this,k,n,arg)
            k = k+1;
            if( k>n )
                err = MException('CellXCommandLineParser:MissingValue', ...
                    ['Missing value for option ''' arg '''']);
                throw(err);
            end
            v = this.args{k};
        end
        
    end
    
    
    methods(Static)
        
        function showUsage()
            fprintf('\n');
            fprintf('USAGE (linux,mac): CellX.sh PATH_TO_MCR CALIBRATION.xml OPTIONS\n');
            fprintf('USAGE (windows)  : CellX.exe CALIBRATION.xml OPTIONS\n');
            fprintf('\n');
            fprintf('This is CellX (v%d.%02d), a program for segmentation, fluorescence quantification, and tracking of cells on microscopy images.\n', CellXConfiguration.version, CellXConfiguration.release);
            fprintf('\nWritten by S. Dimopoulos, C. Mayer\n');
            fprintf('\n');
            
            
            fprintf('OPTIONS:\n');
            fprintf('   %3s %-20s  %s\n', CellXCommandLineParser.helpOption,...
                '', 'Show this message');
            fprintf('   %3s %-20s  %s\n', CellXCommandLineParser.modeOption,...
                '[series|files|track]', 'Select mode (default=series)');
            fprintf('   %3s %-20s  %s\n', '', '', 'Mode ''series'' analyzes a series of images');
            fprintf('   %3s %-20s  %s\n', '', '', 'Mode ''files'' analyzes one explicitly given fileset');
            fprintf('   %3s %-20s  %s\n', '', '', 'Mode ''track'' calls the tracker only');
            %       fprintf('   %3s %-20s  %s\n', '-j', '', 'Number of processors/cores');
            fprintf('\n');
            
            
            fprintf('OPTIONS (mode=series)\n');
            fprintf('   %3s %-20s  %s\n', CellXCommandLineParser.seriesOption, ...
                'SERIES.xml', 'XML file set series');
            fprintf('   %3s %-20s  %s\n', CellXCommandLineParser.trackOption, ...
                '[0|1]', 'Enable/disable tracking (overwrites the settings in the file set series xml)');
            fprintf('   %3s %-20s  %s\n', CellXCommandLineParser.resultDirOption, ...
                'DST_DIR', 'Set the result directory (works only with the -si option, overwrites the result directory in the file series xml)');
            fprintf('   %3s %-20s  %s\n', CellXCommandLineParser.trackIdxOption, ...
                'i', 'Process the i-th file series of the series xml (smallest index is 1)');
            fprintf('   %3s %-20s  %s\n', CellXCommandLineParser.setIdxOption, ...
                'i', 'Process the j-th file set of the i-th series (smallest index is 1)');
            fprintf('   %3s %-20s  %s\n', CellXCommandLineParser.trackOnlyOption, ...
                ' ', 'Call the tracker only for the i-th series (all mat files for file sets must already exist)');           
            fprintf('\n');
            
            
            fprintf('OPTIONS (mode=files): \n');
            fprintf('   %3s %-20s  %s\n', CellXCommandLineParser.frameIdxOption, ...
                'int', 'Frame index, (default=1)');
            fprintf('   %3s %-20s  %s\n', CellXCommandLineParser.resultDirOption, ...
                'DST_DIR', 'Result directory');
            fprintf('   %3s %-20s  %s\n', CellXCommandLineParser.segImgOption, ...
                'IMAGE', 'The image used for segmentation (format: tif, (png, jpg, gif, bmp))');
            fprintf('   %3s %-20s  %s\n', CellXCommandLineParser.fluoImgOption, ...
                'IMAGE', 'Fluorescence image (optional, format: tif, (png, jpg, gif, bmp)), option can appear multiple times, filename format: FLUOTYPE_.*');
            fprintf('   %3s %-20s  %s\n', CellXCommandLineParser.flatImgOption, ...
                'IMAGE', 'Flat field image (optional, format: tif, (png, jpg, gif, bmp)), option can appear multiple times (order must correspond to fluorescence image order)');
            fprintf('   %3s %-20s  %s\n', CellXCommandLineParser.controlImg1Option, ...
                'OUT_IMAGE', 'Segmentation control image ');
            fprintf('   %3s %-20s  %s\n', CellXCommandLineParser.controlImg2Option, ...
                'OUT_IMAGE', 'Fluorescence membrane mask control image');
            fprintf('   %3s %-20s  %s\n', CellXCommandLineParser.testExitPoint, ...
                'EXIT_POINT', 'Calibration mode 0=Hough transform only, 1=Segmentation and fluorescence extraction');
            fprintf('\n');
            
            
            fprintf('OPTIONS (mode=track): \n');
            fprintf('   %3s %-20s  %s\n', CellXCommandLineParser.seriesOption, ...
                'SERIES.xml', 'XML file set series');
            fprintf('   or \n');
            fprintf('   %3s %-20s  %s\n', CellXCommandLineParser.sourceDirOption, ...
                'SRC_DIR', 'Directory with segmentation results');
            fprintf('   %3s %-20s  %s\n', CellXCommandLineParser.seedPrefixOption, ...
                'PREFIX', 'Prefix of seed mat objects in SRC_DIR (default=seed, required filename format: PREFIX%d.mat)');
            fprintf('   %3s %-20s  %s\n', CellXCommandLineParser.maskPrefixOption, ...
                'PREFIX', 'Prefix of mask mat objects in SRC_DIR (default=mask, required filename format: PREFIX%d.mat)');
            fprintf('\n');
            

        end
        
        function checkFileExists(file)             
            if( 0==exist(file, 'file') )
                err = MException('CellXCommandLineParser:FileNonexistent', ...
                ['File ''' file ''' does not exist']);
                throw(err); 
            end       
        end
        
         function checkDirExists(file)             
            if( 0==exist(file, 'dir') )
                err = MException('CellXCommandLineParser:DirNonexistent', ...
                ['Directory ''' file ''' does not exist']);
                throw(err); 
            end       
        end
        
        function makeDirectory(dir)               
            if( 0==exist(dir, 'dir') )
                [s,msg,~] = mkdir(dir);
                if(s==0)               
                    err = MException('CellXCommandLineParser:DirCreationFailed', ...
                        ['Cannot create directory ''' dir '''(' msg ')']);
                    throw(err);
                end
            end
        end
        
        function ret =getExecMode(value)
            if( strcmp(value, 'series') )
                ret = 1;
            elseif( strcmp(value, 'files') )
                ret = 2;
            elseif( strcmp(value, 'track') )
                ret = 3;
            else
                err = MException('CellXCommandLineParser:UnknownMode', ...
                ['Unknown mode ''' value '''']);
                throw(err); 
            end
        end
        
        
        function value = makeBoolNum(value, arg)
            if( ~isnumeric(value) )
                value = str2num(value);
            end
            
            if( value~=0 && value ~=1 )
                err = MException('CellXCommandLineParser:InvalidBoolValue', ...
                    ['Boolean value for option ' arg ' must be 0 or 1']);
                throw(err);
            end
            
        end
          
        function value = makeNum(value, arg)
            if( ~isnumeric(value) )
                [value,status] = str2num(value);
                if( ~status)
                    err = MException('CellXCommandLineParser:InvalidIntegerValue', ...
                        ['Cannot convert value for argument ' arg ' to integer']);
                    throw(err);
                end
            end
        end

        
    end
    
    
end

