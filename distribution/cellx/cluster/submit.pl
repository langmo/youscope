#!/usr/bin/perl -w

# Basel, 30.03.2012, cmayer@bsse.ethz.ch
#
# This script requires the XML:DOM perl module
#
# sudo apt-get install libxml-perl libxml-dom-perl libxml-regexp-perl

use strict;
use XML::DOM;

my $cellxroot = "/home/cmayer/cellx/";
my $cellx     = "/home/cmayer/cellx/CellX.sh";
my $mcr       = "/usr/local/bsse/MCR/v715";
my $qsub      = "qsub -N ";


# BEGIN_OF_MAIN
if( scalar(@ARGV)!=3 ){
    print("USAGE: $0 config.xml series.xml series-index\n");
    exit(1);
}
my $config    = $ARGV[0];
my $series    = $ARGV[1];
my $seriesIdx = $ARGV[2];
my $setCount = &getSetCount($series, $seriesIdx);

print("\nSubmitting image set jobs ...\n\n");
for(my $k=1; $k<=$setCount; ++$k){
    my $cmd = "cd $cellxroot; $cellx $mcr $config -m series -s $series -si $seriesIdx -sj $k";
    my $jobname = "clx_$k";
    my $qcmd = "echo '$cmd' | ".$qsub.$jobname;
    print("Executing  $qcmd \n");
    system($qcmd);
}
print("\n\n");

print("Please run \n\n  runTracker.sh\n\nwhen all jobs have finished\n");

my $cmd = "cd $cellxroot; $cellx $mcr $config -m series -s $series -si $seriesIdx -st";
my $jobname = "clx_track";
my $qcmd = "echo '$cmd' | ".$qsub.$jobname;

open(OUT, ">runTracker.sh") or die "Cannot open runTracker.pl for writing\n$?"; 
print(OUT $qcmd);
close(OUT);
system("chmod +x runTracker.sh");
print("\ndone\n");
# END_OF_MAIN 



sub getSetCount(){
    my $file = shift;
    my $seriesIdx = shift;
    my $parser = new XML::DOM::Parser;
    my $doc = $parser->parsefile ($file);
    my $nodes = $doc->getElementsByTagName("CellXTimeSeries");
    my $n = $nodes->getLength;
    printf("Found %d file series in '$file'\n", $n );
    if( $seriesIdx<1 || $seriesIdx > $n ){
	die('Series index is out of range');
    }
    my $sets = $nodes->item($seriesIdx-1)->getElementsByTagName("CellXFileSet");
    my $m = $sets->getLength;
    printf("Found %d file sets in series %d\n", $m, $n );
    return $m;
}

