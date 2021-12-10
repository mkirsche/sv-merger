import java.util.*;

import java.io.*;
public class ParseSvMergerResults {
	static String vcfFileList = "", mergedFileList = "", ofn = "";
public static void main(String[] args) throws Exception
{
	parseArgs(args);
	//String vcfFileList = "/home/mkirsche/git/sv-merger/testfilelist.txt";
	ArrayList<String> vcfs = getFilesFromList(vcfFileList);
	String[] vcfBasenames = new String[vcfs.size()];
	for(int i = 0; i<vcfBasenames.length; i++)
	{
		vcfBasenames[i] = vcfs.get(i).substring(vcfs.get(i).lastIndexOf('/')+1);
		vcfBasenames[i] = vcfBasenames[i].substring(0, vcfBasenames[i].indexOf('.'));
	}
	//String mergedFileList = "/home/mkirsche/git/sv-merger/mergedfilelist.txt";
	Scanner input = new Scanner(new FileInputStream(new File(mergedFileList)));
	
	ArrayList<SimpleMergedVariant> res = new ArrayList<SimpleMergedVariant>();
	while(input.hasNext())
	{
		String line = input.nextLine();
		if(line.length() == 0)
		{
			continue;
		}
		String mergedFileId = line.substring(line.lastIndexOf('/')+1);
		mergedFileId = mergedFileId.substring(0,mergedFileId.indexOf('.'));
		Scanner mergedInput = new Scanner(new FileInputStream(new File(line)));
		String lastGroup = "";
		ArrayList<String> ids = new ArrayList<String>();
		while(mergedInput.hasNext())
		{
			line = mergedInput.nextLine();
			if(line.length() == 0)
			{
				continue;
			}
			String[] tokens = line.split("\t");
			String varId = tokens[0], groupId = tokens[1];
			if(!groupId.equals(lastGroup))
			{
				// New group
				if(ids.size() > 0)
				{
					// Output new group
					String[] varIds = new String[vcfs.size()];
					Arrays.fill(varIds, ".");
					for(String id : ids)
					{
						for(int i = 0; i<vcfBasenames.length; i++)
						{
							if(id.startsWith(vcfBasenames[i] + "_"))
							{
								varIds[i] = id.substring(vcfBasenames[i].length() + 1);
								break;
							}
						}
					}
					
					res.add(new SimpleMergedVariant(varIds));
					
					ids = new ArrayList<String>();
				}
				
			}
			ids.add(varId);
			lastGroup = groupId;
		}
	}
	
	//String ofn = "/home/mkirsche/git/sv-merger/trio_svmerger_simple.tsv";
	PrintWriter out = new PrintWriter(new File(ofn));
	
	Collections.sort(res);
	
	out.println(SimpleMergedVariant.makeHeader(vcfs));
	for(SimpleMergedVariant v : res)
	{
		out.println(v);
	}
	
	input.close();
	out.close();
}

/*
 * Parse command line arguments
 */
static void parseArgs(String[] args)
{
	for(String arg : args)
	{
		int equalsIdx = arg.indexOf('=');
		if(equalsIdx == -1)
		{
			
		}
		else
		{
			String key = arg.substring(0, equalsIdx);
			String val = arg.substring(1 + equalsIdx);
			if(key.equalsIgnoreCase("merged_file"))
			{
				mergedFileList = val;
			}
			else if(key.equalsIgnoreCase("vcf_filelist"))
			{
				vcfFileList = val;
			}
			else if(key.equalsIgnoreCase("out_file"))
			{
				ofn = val;
			}
		}
		
	}
	
	if(mergedFileList.length() == 0 || ofn.length() == 0 || vcfFileList.length() == 0)
	{
		usage();
		System.exit(0);
	}
}

/*
 * Print a usage menu
 */
static void usage()
{
	System.out.println();
	System.out.println("Usage: java -cp src ParseSvMergerResults [args]");
	System.out.println("Required args:");
	System.out.println("  merged_file  (String) - the path to a list of files containing raw merging results");
	System.out.println("  out_file     (String) - the name of the file to output the merging table to");
	System.out.println("  vcf_filelist (String) - a txt file with each line containing the filename of a merged vcf");
	System.out.println();
}

/*
 * Reads the list of files from either a specified list file or the comma-separated command line argument
 */
static ArrayList<String> getFilesFromList(String fileList) throws Exception
{
	ArrayList<String> res = new ArrayList<String>();
	
	if(new File(fileList).exists())
	{
		Scanner vcfListInput = new Scanner(new FileInputStream(new File(fileList)));
				
		while(vcfListInput.hasNext())
		{
			String line = vcfListInput.nextLine();
			if(line.length() > 0)
			{
				res.add(line);
			}
		}
		vcfListInput.close();
	}
	
	return res;
}

/*
 * Representation of which input IDs make up a merged variant
 */
static class SimpleMergedVariant implements Comparable<SimpleMergedVariant>
{
	static int index;
	ArrayList<String> ids;

	SimpleMergedVariant(String[] idArray) throws Exception
	{
		ids = new ArrayList<String>();
		for(String id : idArray)
		{
			ids.add(id);
		}
	}
	static String makeHeader(ArrayList<String> filenames)
	{
		index = 0;
		StringBuilder res = new StringBuilder("");
		res.append("INDEX");
		for(String fn : filenames)
		{
			res.append("\t");
			res.append(fn);
		}
		return res.toString();
	}
	public String toString()
	{
		StringBuilder res = new StringBuilder("");
		res.append(index);
		index++;
		for(String id : ids)
		{
			res.append("\t");
			res.append(id.length() > 0 ? id : ".");
		}
		return res.toString();
	}
	public int compareTo(SimpleMergedVariant o)
	{
		for(int i = 0; i<ids.size(); i++)
		{
			String myId = ids.get(i), theirId = o.ids.get(i);
			if(myId.equals("") && !theirId.equals(""))
			{
				return 1;
			}
			if(!myId.equals("") && theirId.equals(""))
			{
				return -1;
			}
			if(!myId.equals(theirId))
			{
				return myId.compareTo(theirId);
			}
		}
		return 0;
	}
}
}
