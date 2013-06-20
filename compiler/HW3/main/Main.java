/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package main;

/**
 *
 * @author vallagenah
 */

public class Main {

  public static void main(String argv[]) {
    String sourcefile = "Benchmark.java";
    //String sourcefile = argv[0];
    // Make the error messager printer.
    errormsg.ErrorMsg errorMsg = new errormsg.ErrorMsg(sourcefile);

    // Parse the MiniJava source in sourcefile.
    new parse.Parse(sourcefile, errorMsg);
    
  }

}

