package edu.sse.ustc;

import java.util.*;
import java.io.*;

public class Analyzer
{
	private static Vector nT = new Vector(20);

	private static Vector sumPm = new Vector(20);
	private static Vector sumPdm = new Vector(20);
	private static Vector sumR = new Vector(20);

	private static Vector counts = new Vector(20);



  public static void main(String[] args) throws IOException
  {
    //String src, des;
    String buf;
    String nTPair, Pm, Pdm, R;

    //System.out.println("Analyzing " + args[0] + " ... The output file is " + args[1]);
    //src = new String(args[0]);
    //des = new String (args[1]);
    BufferedReader in = new BufferedReader(new FileReader("analyzer.data"));
    //PrintWriter out = new PrintWriter(new FileWriter(des));
    while ((buf = in.readLine()) != null)
     {
		 // n = buf.substring(buf.indexOf("n")+4, buf.indexOf("n")+5);
		 // T = buf.substring(buf.indexOf("T")+3, buf.indexOf("Pm")-2);
		 nTPair = buf.substring(buf.indexOf("n"), buf.indexOf("Pm")-2);
		 Pm = buf.substring(buf.indexOf("Pm")+5, buf.indexOf("Pm")+10);
		 Pdm = buf.substring(buf.indexOf("Pdm")+6, buf.indexOf("Pdm")+11);
		 R = buf.substring(buf.indexOf("R")+4, buf.indexOf("R")+9);

		 updateSortedTable(nTPair, Pm, Pdm, R);

		 System.out.println(nTPair);

		 System.out.println(Pm);
		 System.out.println(Pdm);
		 System.out.println(R);
         //out.println(buf);
      }
    in.close();
    tablePrint();

    //out.close();
  }

  private static void updateSortedTable(String s, String Pm, String Pdm, String R)
  {
    int i, compareResult;

    for (i=0; i< nT.size(); i++)
      if ((compareResult = s.compareTo(nT.elementAt(i).toString())) == 0)
        /* the word is a repetition */
        {
           int k = Integer.valueOf(counts.elementAt(i).toString()).intValue();
           counts.setElementAt(new Integer(++k), i);

           double d1 = Double.valueOf(sumPm.elementAt(i).toString()).doubleValue();
           double d2 = Double.valueOf(Pm).doubleValue();
           sumPm.setElementAt(new Double(d1+d2), i);

           d1 = Double.valueOf(sumPdm.elementAt(i).toString()).doubleValue();
		   d2 = Double.valueOf(Pdm).doubleValue();
           sumPdm.setElementAt(new Double(d1+d2), i);

           d1 = Double.valueOf(sumR.elementAt(i).toString()).doubleValue();
		   d2 = Double.valueOf(R).doubleValue();
           sumR.setElementAt(new Double(d1+d2), i);

           return;
        }
      else if (compareResult < 0)
        /* i is the index position to insert the word into the table */
        {
           nT.insertElementAt(s, i);
           counts.insertElementAt(new Integer(1), i);

		              double d2 = Double.valueOf(Pm).doubleValue();
		              sumPm.insertElementAt(new Double(d2), i);

		   		      d2 = Double.valueOf(Pdm).doubleValue();
		              sumPdm.insertElementAt(new Double(d2), i);

		   		      d2 = Double.valueOf(R).doubleValue();
           			  sumR.insertElementAt(new Double(d2), i);
           return;
        }

     /* if none of the above two conditions is true, then this word
        goes to the end of the table */
			   nT.addElement(s);
			   counts.addElement(new Integer(1));

			   double d2 = Double.valueOf(Pm).doubleValue();
			   sumPm.addElement(new Double(d2));

				d2 = Double.valueOf(Pdm).doubleValue();
				sumPdm.addElement(new Double(d2));

				d2 = Double.valueOf(R).doubleValue();
				sumR.addElement(new Double(d2));
		   return;
		}

private static void tablePrint()
		{

		  for (int i=0; i< nT.size(); i++)
		    System.out.println(
		      nT.elementAt(i).toString() + ": "
		      //+ " Pm = "
		      //  + Double.valueOf(sumPm.elementAt(i).toString()).doubleValue()
		      //     /Double.valueOf(counts.elementAt(i).toString()).doubleValue()
		       + " Pdm = "
			   		        + Double.valueOf(sumPdm.elementAt(i).toString()).doubleValue()
		           /Double.valueOf(counts.elementAt(i).toString()).doubleValue()
		       + " R = "
			   		        + Double.valueOf(sumR.elementAt(i).toString()).doubleValue()
		           /Double.valueOf(counts.elementAt(i).toString()).doubleValue()

		           );
		}


}


