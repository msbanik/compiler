//George Burri, Madhusudan Banik, Mahmudur Rahman
//BuildSymbolTableVisitor

package semant;

import syntaxtree.*;

public class BuildSymbolTableVisitor extends visitor.DepthFirstVisitor {
    // By extending DepthFirstVisitor, we only have to override those
    // methods that differ from the generic visitor.

    private errormsg.ErrorMsg errorMsg;
    private SymbolTable classTable;
    private ClassInfo currClass;
    private MethodInfo currMethod;

    public BuildSymbolTableVisitor(errormsg.ErrorMsg e) {
        errorMsg = e;
        classTable = new SymbolTable();
        currClass = null;
        currMethod = null;
    }

    public SymbolTable getSymbolTable() {
        return classTable;
    }

    public void visit(MainClass n) {
        String id = n.i1.toString();
        classTable.addClass(id, new ClassInfo(id));
        // No fields or methods in the Main class.
    }

    public void visit(VarDecl n) //new var
    {
        String id = n.i.toString();
        if (currMethod == null) //are we in a class?
        {
            if (!currClass.addField(id, new VariableInfo(n.t))) //defined already?
                errorMsg.error(n.pos, id + " is already defined in " + currClass.getName());
        } else if (!currMethod.addVar(id, new VariableInfo(n.t))) //in a method
        {
            errorMsg.error(n.pos, id + " is already defined in " + currClass.getName() + "." + currMethod.getName() + "(...)");
        }
    }

    public void visit(MethodDecl n) //new method
    {
        String id = n.i.toString();
        if (currClass.getMethod(n.i.toString()) != null) //check if duplicate
        {
            n.duplicate = true;
            errorMsg.error(n.pos, id + " is already defined in " + currClass.getName());
            return;
        }

        if (!currClass.addMethod(id, new MethodInfo(id, n.t))) //add method to class
            errorMsg.error(n.pos, id + " is already defined in " + currClass.getName());
        else {
            //go into the method
            currMethod = currClass.getMethod(id);
            n.duplicate = false;
            for (int i = 0; i < n.fl.size(); i++) // check formal variable
                n.fl.elementAt(i).accept(this);
            String s = currMethod.getFormalsTypes();
            for (int i = 0; i < n.vl.size(); i++) // check every variable
                n.vl.elementAt(i).accept(this);
            currMethod = null; //leaving method
        }
    }

    public void visit(ClassDeclSimple n) //new class
    {
        String id = n.i.toString();
        if (!classTable.addClass(id, new ClassInfo(id))) //does it already exist?
            errorMsg.error(n.pos, "duplicate class: '" + id + "'");
        else {
            //go into the class
            currClass = classTable.get(id);
            for (int i = 0; i < n.vl.size(); i++) //check all variables
                n.vl.elementAt(i).accept(this);
            for (int i = 0; i < n.ml.size(); i++) //check all methods
                n.ml.elementAt(i).accept(this);
            currClass = null; //leaving class
        }
    }

    public void visit(ClassDeclExtends n) //new class (extends)
    {
        String id = n.i.toString();
        if (!classTable.addClass(id, new ClassInfo(id))) //does it already exist?
            errorMsg.error(n.pos, "duplicate class: '" + id + "'");
        else {
            //go into the class
            currClass = classTable.get(id);
            for (int i = 0; i < n.ml.size(); i++) //check all variables
                n.ml.elementAt(i).accept(this);
            for (int i = 0; i < n.vl.size(); i++) //check all methods
                n.vl.elementAt(i).accept(this);
            currClass = null;
        }
    }

    public void visit(Formal n) //Visit formals
    {
        String id = n.i.toString();
        if (!currMethod.addFormal(id, new VariableInfo(n.t))) //add it if not duplicate
            errorMsg.error(n.pos, id + " is already defined in " + currClass.getName() + "." + currMethod.getName() + "(...)");

    }
}