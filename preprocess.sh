# Script of running pre-processing step of sv-merger
# Input: File with all tandem repeat region overlaps
# Also takes in optionally chromosome name and SV type for running on subset


if [ "$(uname -s)" = 'Linux' ]; then
    BINDIR=$(dirname "$(readlink -f "$0" || echo "$(echo "$0" | sed -e 's,\\,/,g')")")
else
    BINDIR=$(dirname "$(readlink "$0" || echo "$(echo "$0" | sed -e 's,\\,/,g')")")
fi

if [ ! -d $BINDIR/intermediate ]
then
  mkdir $BINDIR/intermediate
fi

chrnames=(chr1 chr2 chr3 chr4 chr5 chr6 chr7 chr8 chr9 chr10 chr11 chr12 chr13 chr14 chr15 chr16 chr17 chr18 chr19 chr20 chr21 chr22 chrX)

types=(DEL DUP INS INV)

JASMINE_SRC_PATH='/home/mkirsche/jasmine_data/Jasmine/src'
javac -cp $JASMINE_SRC_PATH $BINDIR/src/*.java

function preprocessSingleChromosome()
{
   BINDIR=$1
   trrOverlapFile=$2
   chrName=$3
   svType=$4
   
   filteredOverlaps=$BINDIR/intermediate/overlaps_filtered'_'$chrName'_'$svType.txt
   
   cat $trrOverlapFile | awk -v chr=$chrName -v type=$svType '{ if($1 == chr && $7 == type) { print }}' > $filteredOverlaps
   
   echo $filteredOverlaps
   
   lines=`wc -l $filteredOverlaps`
   echo $lines
   
   preclustered=$BINDIR/intermediate/$chrName'_'$svType'_'preclustered.txt
   
   if [ ! -r $preclustered ]
   then
     echo 'preclustering'
     #python $BINDIR/main.py PRE_CLUSTER $filteredOverlaps $preclustered 50 1
   fi
   
   merged=$BINDIR/intermediate/$chrName'_'$svType'_'merged.tsv
   trrmerged=$BINDIR/intermediate/$chrName'_'$svType'_'trr_merged.tsv
   if [ ! -r $merged ]
   then
     echo 'merging'
     #python $BINDIR/main.py FIND_CLIQUES $preclustered $merged $trrmerged $svType 85 50
   fi

}

trrOverlapFile=$1
vcffilelist=$2
outprefix=$3
for i in "${chrnames[@]}"
do
  for j in "${types[@]}"
  do
    echo $i $j
    preprocessSingleChromosome $BINDIR $trrOverlapFile $i $j
  done
done

mergedfilelist=$BINDIR/mergedfilelist.txt
ls $BINDIR/intermediate/*'_'*'_'*merged.tsv > $mergedfilelist
echo 'Merging complete. Parsing results'
java -cp $JASMINE_SRC_PATH:$BINDIR/src ParseSvMergerResults merged_file=$mergedfilelist vcf_filelist=$vcffilelist out_file=$BINDIR/$outprefix'_svmerger_simple.tsv'
java -cp $JASMINE_SRC_PATH:$BINDIR/src AugmentMergingTable table_file=$BINDIR/$outprefix'_svmerger_simple.tsv' out_file=$BINDIR/$outprefix'_svmerger_augmented.tsv' vcf_filelist=$vcffilelist

