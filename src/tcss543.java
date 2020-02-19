//
// @Author: Hanfei Yu
//

import java.util.*;
import java.io.*;

import algorithm.*;
import util.*;

//
// Main method to start three algorithms
//

public class tcss543 {
	
	@SuppressWarnings("static-access")
	public static void main (String [] args) {
		
		LinkedList<String> fileList = new LinkedList<String>();
		
		// Input graphs
		Scanner scanner = new Scanner(System.in);	
		scanner.useDelimiter("\r\n");
		
        while (scanner.hasNext()) {
            fileList.add(scanner.next());
        }
        scanner.close();
		
        // Start running algorithms
        for (int i=0; i<fileList.size(); i++) {
    		String fileName = fileList.get(i);
    		String filePath;
    		
			File dir = new File("../" + fileName); 
			try {
				filePath = dir.getCanonicalPath();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				return;
			} 
		
			SimpleGraph graph = new SimpleGraph();
			Hashtable table = GraphInput.LoadSimpleGraph(graph, filePath);
		
			System.out.println("\nGraph name: " + fileName);
			
			// Ford Fulkerson
			new Ford_Fulkerson().FF(graph, table);
			
			// Scaling Ford Fulkerson
			new Scaling_Ford_Fulkerson().SFF(graph, table);
			
			// Preflow Push
			new Preflow_Push().PP(graph, table);
        }
	}
}
