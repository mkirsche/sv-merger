import java.io.File;
import java.io.FileInputStream;
import java.io.PrintWriter;
import java.util.HashSet;
import java.util.Scanner;
import java.util.TreeMap;

public class AnnotateVcf {
public static void main(String[] args) throws Exception
{
	String fn = args[0];//"/home/mkirsche/jasmine_data/figures/figure2/hg002_hifi.jasmine.vcf";
	String bedlistFn = args[1];//"/home/mkirsche/jasmine_data/beds/hg002_hifi_jasmine.filelist.txt";
	String ofn = args[2];//"/home/mkirsche/jasmine_data/beds/hg002_hifi_annotated.vcf";
	//String fn = //args[0];
	//String bedlistFn = args[1];
	//String ofn = args[2];
	
	TreeMap<String, HashSet<String>> tagToIdSet = new TreeMap<String,HashSet<String>>();
	Scanner input = new Scanner(new FileInputStream(new File(bedlistFn)));
	while(input.hasNext())
	{
		String curFn = input.nextLine().trim();
		if(curFn.length() == 0)
		{
			continue;
		}
		System.out.println(curFn);
		String category = curFn.substring(1 + curFn.lastIndexOf('/'));
		category = category.substring(1 + category.indexOf('.'));
		category = category.substring(0, category.indexOf('.'));
		System.out.println(category);
		tagToIdSet.put(category, new HashSet<String>());
		Scanner curInput = new Scanner(new FileInputStream(new File(curFn)));
		while(curInput.hasNext())
		{
			String line = curInput.nextLine();
			if(line.length() == 0 || line.startsWith("#"))
			{
				continue;
			}
			String id = line.split("\t")[2];
			tagToIdSet.get(category).add(id);
		}
		curInput.close();
	}
	input.close();
	
	input = new Scanner(new FileInputStream(new File(fn)));
	PrintWriter out = new PrintWriter(new File(ofn)); 
	boolean printedHeader = false;
	VcfHeader header = new VcfHeader();
	while(input.hasNext())
	{
		String line = input.nextLine();
		if(line.startsWith("#"))
		{
			header.addLine(line);
			//out.println(line);
			continue;
		}
		if(!printedHeader)
		{
			for(String s : tagToIdSet.keySet())
			{
				String field = "INTERSECTS_" + s.toUpperCase();
				header.addInfoField(field, "1", "String", "Whether the SV intersects at least one "+ s);
			}
			header.print(out);
			printedHeader = true;
		}
		VcfEntry entry = new VcfEntry(line);
		for(String s : tagToIdSet.keySet())
		{
			String field = "INTERSECTS_" + s.toUpperCase();
			if(tagToIdSet.get(s).contains(entry.getId()))
			{
				entry.setInfo(field, "1");
			}
			else
			{
				entry.setInfo(field, "0");
			}
		}
		out.println(entry);
	}
	input.close();
	out.close();
}
}
