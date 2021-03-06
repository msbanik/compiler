
//George Burri, Madhusudan Banik, Mahmudur Rahman
//TypeCheckVisitor

package semant;

import syntaxtree.*;

import java.util.*; //imported for map

public class TypeCheckVisitor extends visitor.TypeDepthFirstVisitor {
    // By extending TypeDepthFirstVisitor, we only have to override those
    // methods that differ from the generic visitor.

    private errormsg.ErrorMsg errorMsg;
    private SymbolTable classTable;
    private ClassInfo currClass;
    private MethodInfo currMethod;
    private Map<String, Boolean> assigned = new HashMap<String, Boolean>(); //use to keep track of what is initialized
    private Map<String, Boolean> seen = new HashMap<String, Boolean>(); //use to keep track of what is seen

    // Type constants
    final IntegerType INTTY = new IntegerType();
    final IntArrayType INTARRTY = new IntArrayType();
    final BooleanType BOOLTY = new BooleanType();

    public TypeCheckVisitor(errormsg.ErrorMsg e, SymbolTable s) {
        errorMsg = e;
        classTable = s;
        currClass = null;
        currMethod = null;
    }

    public Type visit(MainClass n) {
        // Mostly you just need to typecheck the body of 'main' here.
        // But as shown in Foo.java, you need care concerning 'this'.
        n.s.accept(this);
        return null;
    }

    // Exp e1,e2;
    public Type visit(Plus n) {
        Type t1 = n.e1.accept(this);
        Type t2 = n.e2.accept(this);
        if (!equal(t1, t2, INTTY)) //need ints!
            errorMsg.error(n.pos, eIncompBiop("+", t1.toString(), t2.toString()));
        return INTTY;
    }

    // Exp e1,e2;
    public Type visit(Minus n) {
        Type t1 = n.e1.accept(this);
        Type t2 = n.e2.accept(this);
        if (!equal(t1, t2, INTTY)) //need ints!
            errorMsg.error(n.pos, eIncompBiop("-", t1.toString(), t2.toString()));
        return INTTY;
    }

    // Exp e1,e2;
    public Type visit(Times n) {
        Type t1 = n.e1.accept(this);
        Type t2 = n.e2.accept(this);
        if (!equal(t1, t2, INTTY)) //need ints!
            errorMsg.error(n.pos, eIncompBiop("*", t1.toString(), t2.toString()));
        return INTTY;
    }

    // Exp e1,e2;
    public Type visit(LessThan n) {
        Type t1 = n.e1.accept(this);
        Type t2 = n.e2.accept(this);
        if (!equal(t1, t2, INTTY)) //need ints!
            errorMsg.error(n.pos, eIncompBiop("<", t1.toString(), t2.toString()));
        return BOOLTY;
    }

    // Exp e1,e2;
    public Type visit(And n) {
        Type t1 = n.e1.accept(this);
        Type t2 = n.e2.accept(this);
        if (!equal(t1, t2, BOOLTY)) //need ints!
            errorMsg.error(n.pos, eIncompBiop("AND", t1.toString(), t2.toString()));
        return BOOLTY;
    }

    // Id i, Exp e;
    public Type visit(Assign n) {
        assigned.put(n.i.toString(), true);
        seen.put(n.i.toString(), true); //mark as initialized
        Type t1 = n.i.accept(this);
        Type t2 = n.e.accept(this);
        if (!(equal(t1, t2, t2))) //check that types are matched
            errorMsg.error(n.e.pos, eIncompTypes(t2.toString(), t1.toString()));
        return null;
    }

    public Type visit(Not n) {
        Type t1 = n.e.accept(this);
        if (!equal(t1, BOOLTY, BOOLTY)) //only works on bool
            errorMsg.error(n.pos, eIncompBiop("!", t1.toString(), ""));
        return BOOLTY;
    }

    public Type visit(Print n) {
        Type t1 = n.e.accept(this);
        return null;
    }

    public Type visit(Program n) {
        currClass = classTable.get(n.m.i1.toString());
        n.m.accept(this); //Go into main class
        currClass = null;
        for (int i = 0; i < n.cl.size(); i++) //go into other classes
            n.cl.elementAt(i).accept(this);
        currClass = null;
        return null;
    }

    public Type visit(ClassDeclSimple n) {
        currClass = classTable.get(n.i.toString());
        for (int i = 0; i < n.ml.size(); i++) //visit methods
            n.ml.elementAt(i).accept(this);
        for (int i = 0; i < n.vl.size(); i++) //visit vars
            n.vl.elementAt(i).accept(this);
        currClass = null;
        return null;
    }

    public Type visit(ClassDeclExtends n) {
        currClass = classTable.get(n.i.toString());
        for (int i = 0; i < n.ml.size(); i++) //visit methods
            n.ml.elementAt(i).accept(this);
        for (int i = 0; i < n.vl.size(); i++) //visit vars
            n.vl.elementAt(i).accept(this);
        currClass = null;
        return null;
    }

    public Type visit(VarDecl n) {
        Type t1 = n.i.accept(this);
        return null;
    }

    public Type visit(MethodDecl n) {
        currMethod = currClass.getMethod(n.i.toString()); //set method as current
        n.i.accept(this);
        for (int i = 0; i < n.fl.size(); i++) //visit formals
            n.fl.elementAt(i).accept(this);
        for (int i = 0; i < n.vl.size(); i++) //visit vars
            n.vl.elementAt(i).accept(this);
        for (int i = 0; i < n.sl.size(); i++) //visit statements
            n.sl.elementAt(i).accept(this);
        Type t1 = n.e.accept(this);
        //check return type
        if (!(equal(n.t, INTTY, INTTY) || equal(n.t, BOOLTY, BOOLTY) || equal(n.t, INTARRTY, INTARRTY)) && classTable.get(n.t.toString()) == null)
            errorMsg.error(n.t.pos, unkownSymbol(n.t.toString(), currClass.getName()));//type does not exist
        else if (t1 != null)
            if (!t1.toString().equals(n.t.toString()))//wrong type returned?
                errorMsg.error(n.e.pos, eIncompTypes(t1.toString(), n.t.toString()));
        currMethod = null;//close method
        return null;
    }

    public Type visit(Formal n) {
        assigned.put(n.i.toString(), true);
        seen.put(n.i.toString(), true);  //mark as initialized
        return null;
    }

    public Type visit(FormalList n) {
        return null;
    }

    //these three return their types
    public Type visit(IntArrayType n) {
        Type t1 = n.accept(this);
        return INTARRTY;
    }

    public Type visit(BooleanType n) {
        Type t1 = n.accept(this);
        return BOOLTY;
    }

    public Type visit(IntegerType n) {
        Type t1 = n.accept(this);
        return INTTY;
    }

    public Type visit(IdentifierType n) {
        return null;
    }

    public Type visit(Block n) {
        for (int i = 0; i < n.sl.size(); i++) //go through entire block
            n.sl.elementAt(i).accept(this);
        return null;
    }

    public Type visit(If n) {
        Type t1 = n.e.accept(this);
        Type t2 = n.s1.accept(this);
        Type t3 = n.s2.accept(this);
        if (!equal(t1, BOOLTY, BOOLTY)) //must be bool
            errorMsg.error(n.pos, eIncompTypes(t1.toString(), "bool"));
        return null;
    }

    public Type visit(While n) {
        Type t1 = n.e.accept(this);
        Type t2 = n.s.accept(this);
        if (!equal(t1, BOOLTY, BOOLTY)) //must be bool
            errorMsg.error(n.pos, eIncompTypes(t1.toString(), "bool"));
        return null;
    }

    public Type visit(ArrayAssign n) {
        Type t1 = n.i.accept(this);
        Type t2 = n.e1.accept(this);
        Type t3 = n.e2.accept(this);
        if (!(equal(t1, INTARRTY, INTARRTY))) //must be an array!
            errorMsg.error(n.e1.pos, notArray(t1.toString()));
        if (!(equal(t2, INTTY, INTTY))) //only ints can be used as index
            errorMsg.error(n.e1.pos, eIncompTypes(t2.toString(), "int"));
        if (!(equal(t3, INTTY, INTTY))) //only ints can be used as value
            errorMsg.error(n.e2.pos, eIncompTypes(t3.toString(), "int"));
        return null;
    }

    public Type visit(ArrayLookup n) {
        Type t1 = n.e1.accept(this);
        Type t2 = n.e2.accept(this);
        if (!(equal(t1, INTARRTY, INTARRTY))) //must be an array!
            errorMsg.error(n.e1.pos, notArray(t1.toString()));
        if (!(equal(t2, INTTY, INTTY))) //only ints can be used as index
            errorMsg.error(n.e2.pos, eIncompTypes(t2.toString(), "Int"));
        return INTTY;
    }

    public Type visit(ArrayLength n) {
        Type t1 = n.e.accept(this);
        if (!(equal(t1, INTARRTY, INTARRTY))) //must be an array!
            errorMsg.error(n.e.pos, notArray(t1.toString()));
        return INTTY;
    }

    //these three return base values
    public Type visit(IntegerLiteral n) {
        return INTTY;
    }

    public Type visit(True n) {
        return BOOLTY;
    }

    public Type visit(False n) {
        return BOOLTY;
    }

    public Type visit(IdentifierExp n) {
        Type t1;
        if (currMethod != null) //There might be var overides in method, so check here first
        {
            if (currMethod.getVar(n.s.toString()) != null) //look for var
            {
                t1 = currMethod.getVar(n.s.toString()).type;
                check(n.s, t1.pos); //check if intitialized
                if (!(equal(t1, INTTY, INTTY) || equal(t1, BOOLTY, BOOLTY) || equal(t1, INTARRTY, INTARRTY)) && classTable.get(t1.toString()) == null) //make sure type exists!
                    errorMsg.error(t1.pos, unkownSymbol(t1.toString(), currClass.getName()));//type does not exist
                return t1;
            }
        }


        if (currClass != null)//make sure we are in a class!
        {
            if (currClass.getField(n.s.toString()) != null) //is the var defined in the class?
            {
                t1 = currClass.getField(n.s.toString()).type;
                if (!(equal(t1, INTTY, INTTY) || equal(t1, BOOLTY, BOOLTY) || equal(t1, INTARRTY, INTARRTY)) && classTable.get(t1.toString()) == null) //make sure type exists!
                    errorMsg.error(t1.pos, unkownSymbol(t1.toString(), currClass.getName()));//type does not exist
                return t1;
            }

            if (currClass.getMethod(n.s.toString()) != null)// check if the id is actually a method
            {
                t1 = currClass.getMethod(n.s.toString()).getReturnType();
                if (!(equal(t1, INTTY, INTTY) || equal(t1, BOOLTY, BOOLTY) || equal(t1, INTARRTY, INTARRTY)) && classTable.get(t1.toString()) == null)
                    errorMsg.error(t1.pos, unkownSymbol(t1.toString(), currClass.getName()));//type does not exist
                String s = currClass.getMethod(n.s.toString()).getFormalsTypes();
                return t1;
            }
        }

        if (classTable.get(n.s.toString()) != null)//it must be a reference to anothe class, else...
            return new IdentifierType(n.s.toString());
        ;

        errorMsg.error(n.pos, unkownSymbol(n.s, currClass.getName())); //unknown symbol!
        return null;

    }

    public Type visit(This n) {
        return null;
    }

    public Type visit(NewArray n) {
        Type t1 = n.e.accept(this);
        if (!(equal(t1, INTTY, INTTY))) //must be an int
            errorMsg.error(n.e.pos, eIncompTypes(t1.toString(), "int"));
        return INTARRTY;
    }

    public Type visit(NewObject n) {
        Type t1 = n.i.accept(this);
        //currClass=classTable.get(n.i.toString()); //we're briefly switching the class context so that it's methods can be found
        return t1;
    }

    public Type visit(Identifier n) {
        Type t1;
        if (currMethod != null) //There might be overrides from vars!
        {
            if (currMethod.getVar(n.s.toString()) != null) //look in the method
            {
                t1 = currMethod.getVar(n.s.toString()).type;
                check(n.s, t1.pos); //check if intitialized
                if (!(equal(t1, INTTY, INTTY) || equal(t1, BOOLTY, BOOLTY) || equal(t1, INTARRTY, INTARRTY)) && classTable.get(t1.toString()) == null)
                    errorMsg.error(t1.pos, unkownSymbol(t1.toString(), currClass.getName()));//type does not exist
                return t1;
            }
        }

        if (currClass != null) //look in the class
        {
            if (currClass.getMethod(n.s.toString()) != null)// is it a method?
            {
                t1 = currClass.getMethod(n.s.toString()).getReturnType();
                if (!(equal(t1, INTTY, INTTY) || equal(t1, BOOLTY, BOOLTY) || equal(t1, INTARRTY, INTARRTY)) && classTable.get(t1.toString()) == null)
                    errorMsg.error(t1.pos, unkownSymbol(t1.toString(), currClass.getName()));//type does not exist
                String s = currClass.getMethod(n.s.toString()).getFormalsTypes();
                return t1;
            }


            if (currClass.getField(n.s.toString()) != null) //is it a var?
            {
                t1 = currClass.getField(n.s.toString()).type;
                if (!(equal(t1, INTTY, INTTY) || equal(t1, BOOLTY, BOOLTY) || equal(t1, INTARRTY, INTARRTY)) && classTable.get(t1.toString()) == null)
                    errorMsg.error(t1.pos, unkownSymbol(t1.toString(), currClass.getName()));//type does not exist
                return t1;
            }

        }
        if (classTable.get(n.s.toString()) != null)//external class reference
            return new IdentifierType(n.s.toString());

        //undefined symbol
        if (currClass != null) //prevent exceptions
            errorMsg.error(0, unkownSymbol(n.s, currClass.getName()));
        else
            errorMsg.error(0, unkownSymbol(n.s, "main"));
        return null;
    }

    public Type visit(Call n) {
        ClassInfo temp = currClass; //keep reminder of the current class
        Type t2 = n.e.accept(this);
        if (t2 != null) {
            if (classTable.get(t2.toString()) != null) //we are switching context to deal with lookups from the other class
                currClass = classTable.get(t2.toString());
        }
        Type t1 = n.i.accept(this);
        Type t3;
        String args = "(";
        for (int i = 0; i < n.el.size(); i++)//go through parameters list
        {
            t3 = n.el.elementAt(i).accept(this);
            if (t3 != null)
                args = args + t3;
            else if (n.el.elementAt(i).toString().contains("This")) //We found 'this'
            {
                args = args + temp.getName(); //use the actual current class!
                if (currMethod == null)
                    errorMsg.error(n.el.elementAt(i).pos, badContext("this")); //static error
            }

            if (i + 1 != n.el.size()) //build args list
                args = args + ", ";
        }
        args = args + ")"; //close list
        String needed = "()"; //empty default
        String classname = "main"; //default
        String methodname = "main"; //default
        if (currMethod != null)
            methodname = currMethod.getName(); //get real name
        if (currClass != null) {
            classname = currClass.getName(); //get real name
            n.fullname = classname + "$" + n.i.toString(); //assign fullname
            if (currClass.getMethod(n.i.toString()) != null)
                needed = currClass.getMethod(n.i.toString()).getFormalsTypes(); //get the required formals
        }
        currClass = temp;//go back to actual class context
        if (needed == "()") //empty arguments is a problem
        {
            errorMsg.error(n.e.pos, unkownSymbol(n.i.toString(), currClass.getName()));
            return null;
        } else if (!args.equals(needed)) //arguments did not match
            errorMsg.error(n.e.pos, badArgs(n.i.toString(), needed, args, currClass.getName()));
        return t1;
    }

    public void check(String x, int pos) //function to check if a variable is initialized
    {
        if (seen.get(x) == null) {
            assigned.put(x, false);
            seen.put(x, true);
            return;
        }
        if (!assigned.get(x))//unitiliazed!
        {
            errorMsg.error(pos, undefined(x));
            assigned.put(x.toString(), true);
        }
    }

    // Check whether t1 == t2 == target, but suppress error messages if
    // either t1 or t2 is null.
    private boolean equal(Type t1, Type t2, Type target) {
        if (t1 == null || t2 == null)
            return true;

        if (target == null)
            throw new Error("target argument in method equal cannot be null");

        if (target instanceof IdentifierType && t1 instanceof IdentifierType
                && t2 instanceof IdentifierType)
            return ((IdentifierType) t1).s.equals(((IdentifierType) t2).s);

        if (!(target instanceof IdentifierType) &&
                t1.toString().equals(target.toString()) &&
                t2.toString().equals(target.toString()))
            return true;

        return false;
    }

    // Methods for error reporting:

    private String eIncompTypes(String t1, String t2) {
        return "incompatible types \nfound   : " + t1
                + "\nrequired: " + t2;
    }

    private String eIncompBiop(String op, String t1, String t2) {
        return "operator " + op + " cannot be applied to " + t1 + "," + t2;
    }

    private String unkownSymbol(String name, String classin) {
        return "cannot resolve symbol: " + name + "\nlocation: " + classin;
    }

    private String badContext(String name) {
        return "non-static variable '" + name + "' cannot be referenced from a static context";
    }

    private String badArgs(String func, String needed, String got, String inclass) {
        return func + needed + " in " + inclass + " cannot be applied to " + got;
    }

    private String notArray(String got) {
        return "array required, but found type " + got;
    }

    private String undefined(String var) {
        return "variable " + var + " might not have been initialized";
    }

}