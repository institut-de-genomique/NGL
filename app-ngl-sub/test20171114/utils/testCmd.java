package utils;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Map;

import org.junit.Test;

public class testCmd extends AbstractTestsSRA {
	
	@Test
	public void test() throws IOException {
    //String[] command = {"ls", "-al"}; // commande et option ou argument
    //ProcessBuilder processBuilder = new ProcessBuilder(command);


    //Here is an example that starts a process with a modified working directory and environment:
	String cible = "/env/cns/submit_traces/SRA/SNTS_output_xml/AUP/17_02_2014";
	String lien = "/env/cns/submit_traces/SRA/SNTS_output_xml/mesTEST/lastTest/AUP_17_02_2014";
    ProcessBuilder pb = new ProcessBuilder("ln", "-s", "-f",cible, lien);
 
         
    Process p = pb.start();
    
    // get the command list
    System.out.println(""+pb.command());
     

    Process process = pb.start();
    //Read out dir output
    BufferedReader br = new BufferedReader(new InputStreamReader(process.getInputStream()));
    String line;

    while ((line = br.readLine()) != null) {
        System.out.println(line);
    }

    //Wait to get exit value
    try {
        int exitValue = process.waitFor();
        System.out.println("\n\nExit Value is " + exitValue);
    } catch (InterruptedException e) {
        // TODO Auto-generated catch block
        e.printStackTrace();
    }
	}
}
