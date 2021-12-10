import java.util.*;
import java.io.*;
public class TandemRepeatNormalizeVcf {
public static void main(String[] args) throws Exception
{
	String[] vcfFns = new String[] {args[0]};
	String sampleName = args[0].substring(args[0].lastIndexOf('/')+1);
	sampleName = sampleName.substring(0,sampleName.indexOf('.'));
	String[] sampleNames = new String[] {sampleName};
	//String bedFn = "/home/mkirsche/jasmine_data/revisions/tandem_repeats.bed";
	String ofn = args[1];//"/home/mkirsche/jasmine_data/revisions/hg002_trio_svmerger_preprocessed.tsv";
	PrintWriter out = new PrintWriter(new File(ofn));
	
	/*
	Scanner input = new Scanner(new FileInputStream(new File(bedFn)));
	HashMap<String, TreeSet<Interval>> repeats = new HashMap<String, TreeSet<Interval>>();
	while(input.hasNext())
	{
		String line = input.nextLine();
		if(line.length() == 0)
		{
			continue;
		}
		String[] tokens = line.split("\t");
		String chr = tokens[0];
		int start = Integer.parseInt(tokens[1]), end = Integer.parseInt(tokens[2]);
		if(!repeats.containsKey(chr))
		{
			repeats.put(chr, new TreeSet<Interval>());
			repeats.get(chr).add(new Interval(start, end));
		}
	}
	*/
	
	for(int i = 0; i<vcfFns.length; i++)
	{
		String vcfFn = vcfFns[i];
		Scanner input = new Scanner(new FileInputStream(new File(vcfFn)));
		while(input.hasNext())
		{
			String line = input.nextLine();
			if(line.length() == 0 || line.startsWith("#"))
			{
				continue;
			}
			VcfEntry entry = VcfEntry.fromLine(line);
			int start = (int)entry.getPos(), end = (int)entry.getEnd();
			String chr = entry.getChromosome();
			
			/*
			TreeSet<Interval> overlapCandidates = repeats.get(chr);
			if(overlapCandidates != null)
			{
				Interval firstCandidate = overlapCandidates.floor(new Interval(start, (int)2e9));
				Interval secondCandidate = overlapCandidates.ceiling(new Interval(start, start));
				
				Interval overlap = null;
				int maxOverlap = -1;
				
				if(firstCandidate != null)
				{
					int firstOverlap = Math.min(end, firstCandidate.end) - Math.max(start, firstCandidate.start);
					if(firstOverlap > maxOverlap)
					{
						overlap = firstCandidate;
						maxOverlap = firstOverlap;
					}
				}
				
				if(secondCandidate != null)
				{
					int secondOverlap = Math.min(end, secondCandidate.end) - Math.max(start, secondCandidate.start);
					if(secondOverlap > maxOverlap)
					{
						overlap = secondCandidate;
						maxOverlap = secondOverlap;
					}
				}
				
				if(maxOverlap == end - start)
				{
					start = overlap.start;
				}
			}
			*/
			
			end = start + Math.abs(entry.getLength());
			
			out.println(chr + "\t" + start + "\t" + end + "\t" + sampleName+"_"+entry.getId() + "\t" + sampleNames[i] + "\t" + sampleNames[i] + "\t" + entry.getType() + "\t"+ Math.abs(entry.getLength()));
		}
	}
	
	out.close();
	
}
static class Interval implements Comparable<Interval>
{
	int start, end;
	Interval(int ss, int ee)
	{
		start = ss;
		end = ee;
	}
	@Override
	public int compareTo(Interval o) {
		if(start != o.start) return start - o.start;
		return end - o.end;
	}
	
}
}
