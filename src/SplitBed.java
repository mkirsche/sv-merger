import java.io.File;
import java.io.FileInputStream;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Scanner;

public class SplitBed {
public static void main(String[] args) throws Exception
{
	String fn = args[0];
	int field = Integer.parseInt(args[1]);
	String outdir = args[2];
	String extension = "." + args[3];
	//String fn = "/home/mkirsche/jasmine_data/beds/Homo_sapiens.GRCh38.103.ucsc-named.gtf";
	//int field = 2;
	//String outdir = "/home/mkirsche/jasmine_data/beds/genes";
	//String extension = ".gtf";
	splitBed(fn, field, outdir, extension);
	//fn = "/home/mkirsche/jasmine_data/beds/hg38.chr_only_repeatmasker.out.bed";
	//field = 6;
	//outdir = "/home/mkirsche/jasmine_data/beds/repeats";
	//extension = ".bed";
	splitBed(fn, field, outdir, extension);
}
static void splitBed(String fn, int field, String outdir, String ext) throws Exception
{
	if(!(new File(outdir)).isDirectory())
	{
		(new File(outdir)).mkdir();
	}
	Scanner input = new Scanner(new FileInputStream(new File(fn)));
	ArrayList<String> header = new ArrayList<String>();
	HashMap<String, PrintWriter> writerMap = new HashMap<String, PrintWriter>();
	while(input.hasNext())
	{
		String line = input.nextLine();
		if(line.startsWith("#"))
		{
			header.add(line);
			continue;
		}
		String[] tokens = line.split("\t");
		String key = tokens[field];
		key = key.toLowerCase().replaceAll("/", "");
		if(key.contains("(")) continue;
		/*String[] starts = new String[] {
				"ltr", "tigger", "mamrep", "mamgyp", "made", "arthur", "charlie",
				"cr1", "erv", "eulor", "eut", "mer", "fordprefect", "gsat", "hat",
				"helitron", "herv", "hsat", "huers", "hy", "kanga", "l1", "l2", "l3", "l4",
				"l5", "mir", "mlt", "mst", "orsl", "pibl", "prim", "lor", "trna", "ucon", 
				"zaphod", "ricksha", "alu", "amnsine", "hsmar", "mamrte", "mamtip", "mare",
				"pabl", "sva", "the1"
		};
		if(key.startsWith("x") && key.endsWith("_dna")) key = "dna";
		if(key.startsWith("x") && key.endsWith("_line")) key = "line";
		for(String s : starts)
		{
			if(key.startsWith(s)) key = s;
		}*/
		if(!writerMap.containsKey(key))
		{
			PrintWriter cur = new PrintWriter(new File(outdir + "/" + key + ext));
			for(String headerLine : header)
			{
				cur.println(headerLine);
			}
			writerMap.put(key, cur);
		}
		writerMap.get(key).println(line);
	}
	for(String s : writerMap.keySet())
	{
		writerMap.get(s).close();
	}
}
}
