classdef CellXExceptionHandler
    %CELLXEXCEPTIONHANDLER Summary of this class goes here
    %   Detailed explanation goes here
    
    properties
    end
    
    methods (Static)
        function ret = handleException(exc)       
            if( strcmp(exc.identifier, 'CellXCommandLineParser:MissingSeriesFile') )
                fprintf(2, '\n\nERROR: %s.\n\n', exc.message);
                ret = 11;
            elseif( strcmp(exc.identifier, 'CellXCommandLineParser:MissingParameterFile') )
                fprintf(2, '\n\nERROR: %s.\n\n', exc.message);
                ret = 12;
            elseif( strcmp(exc.identifier, 'CellXCommandLineParser:UsageInvoked') )
                ret = 13;
            elseif( strcmp(exc.identifier, 'CellXCommandLineParser:MissingValue') )
                fprintf(2, '\n\nERROR: %s.\n\n', exc.message);
                ret = 14;
            elseif( strcmp(exc.identifier, 'CellXCommandLineParser:FileNonexistent') )
                fprintf(2, '\n\nERROR: %s.\n\n', exc.message);
                ret = 15;
            elseif( strcmp(exc.identifier, 'CellXCommandLineParser:DirNonexistent') )
                fprintf(2, '\n\nERROR: %s.\n\n', exc.message);
                ret = 16;
            elseif( strcmp(exc.identifier, 'CellXCommandLineParser:DirCreationFailed') )
                fprintf(2, '\n\nERROR: %s.\n\n', exc.message);
                ret = 17;
            elseif( strcmp(exc.identifier, 'CellXCommandLineParser:UnknownMode') )
                fprintf(2, '\n\nERROR: %s.\n\n', exc.message);
                ret = 18;
            elseif( strcmp(exc.identifier, 'CellXCommandLineParser:InvalidBoolValue') )
                fprintf(2, '\n\nERROR: %s.\n\n', exc.message);
                ret = 19;
            elseif( strcmp(exc.identifier, 'CellXCommandLineParser:InvalidIntegerValue') )
                fprintf(2, '\n\nERROR: %s.\n\n', exc.message);
                ret = 20;
            elseif( strcmp(exc.identifier, 'SeedsChk:MaxNumOfSeedsExceeded') )
                fprintf(2, '\n\nERROR: Hough transform detected too many seeds (%s). \n 1) Check input image\n 2) Try modifying the min/max seed radius\n 3) Try increasing the seed detection cutoff.\n\n', exc.message);
                ret = 21;
            else
                fprintf(2, '\n\nERROR: %s\n\n', exc.message);
                ret = 22;
            end
            
        end
    end
    
end

