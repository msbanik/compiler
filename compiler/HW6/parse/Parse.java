package parse;

public class Parse {

    public syntaxtree.Program absyn;
    private errormsg.ErrorMsg errorMsg;

    public Parse(String filename, errormsg.ErrorMsg err) {
        errorMsg = err;
        java.io.InputStream inp;
        try {
            inp = new java.io.FileInputStream(filename);
        } catch (java.io.FileNotFoundException e) {
            throw new Error("File not found: " + filename);
        }
        Grm parser = new Grm(new Yylex(inp, errorMsg), errorMsg);

        try {
            absyn = (syntaxtree.Program) parser.debug_parse().value;
        } catch (Throwable e) {
            e.printStackTrace();
            throw new Error(e.toString());
        } finally {
            try {
                inp.close();
            } catch (java.io.IOException e) {
            }
        }
    }
}

