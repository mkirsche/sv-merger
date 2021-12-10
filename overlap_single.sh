#if [ "$(uname -s)" = 'Linux' ]; then
#    BINDIR=$(dirname "$(readlink -f "$0" || echo "$(echo "$0" | sed -e 's,\\,/,g')")")
#else
#    BINDIR=$(dirname "$(readlink "$0" || echo "$(echo "$0" | sed -e 's,\\,/,g')")")
#fi

chrnames=(chr1 chr2 chr3 chr4 chr5 chr6 chr7 chr8 chr9 chr10 chr11 chr12 chr13 chr14 chr15 chr16 chr17 chr18 chr19 chr20 chr21 chr22 chrX)

javac $BINDIR/src/*.java

startIdx=$2
endIdx=$3
ctr=0

vcffilelist=$1
for x in `cat $vcffilelist`
do
    ctr=$((ctr+1))
    if [ $ctr -lt $startIdx ] || [ $ctr -gt $endIdx ]
    then
        continue
    fi

    echo $x
    bn=`basename $x`
    echo $x $bn
    noTra=$BINDIR/intermediate/$bn'_noTra.vcf'
    cat $x | grep -v 'SVTYPE=TRA' > $noTra

	for i in "${chrnames[@]}"
	do
	  echo '  '$i
	  notraCurVcf=$BINDIR/intermediate/$bn'_'$i.vcf
	  cat $noTra | grep '#' > $notraCurVcf
	  cat $noTra | awk -v var=$i '{if($1 == var){print}}' >> $notraCurVcf
	  notraCurTsv=$BINDIR/intermediate/$bn'_'$i.tsv

	  java -cp $BINDIR/src TandemRepeatNormalizeVcf $notraCurVcf $notraCurTsv
	  
	  	  trOverlaps=$BINDIR/intermediate/$bn'_'$i'troverlaps.txt'
	  python $BINDIR/main.py FIND_TRR_OVERLAPS $notraCurTsv $BINDIR/trf_coords/$i.trf.sorted.gor $trOverlaps 5

	done
done
