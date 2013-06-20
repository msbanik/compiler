//George Burri, Madhusudan Banik, and Mahmudur Rahman
//Straight-Line Program Interpreter

class interp {

    static int countArgs(ExpList exps) //this actually counts the arguments for print
    {
        if (exps instanceof PairExpList) //pair
        {
            PairExpList pel = (PairExpList) exps;
            return 1 + countArgs(pel.tail); //count 1, keep going
        }
        if (exps instanceof LastExpList) //last
        {
            return 1;
        }
        throw new Error("Bad Statement"); //none of the above???
    }

    static int maxargs(Stm s) //statement version
    {
        if (s instanceof CompoundStm) {
            CompoundStm cs = (CompoundStm) s;
            return Math.max(maxargs(cs.stm1), maxargs(cs.stm2));
        }
        if (s instanceof AssignStm) {
            AssignStm as = (AssignStm) s;
            return maxargs(as.exp); //need to explore the exp
        }
        if (s instanceof PrintStm) {
            PrintStm ps = (PrintStm) s;
            return Math.max(maxargs(ps.exps), countArgs(ps.exps));//embeded statements vs this statement's count
        }
        throw new Error("Bad Statement"); //none of the above???
    }

    static int maxargs(ExpList exps) //Expressions List version
    {
        if (exps instanceof PairExpList) //pair
        {
            PairExpList pel = (PairExpList) exps;
            return Math.max(maxargs(pel.head), maxargs(pel.tail));
        }
        if (exps instanceof LastExpList) //last
        {
            LastExpList lel = (LastExpList) exps;
            return maxargs(lel.head);
        }
        throw new Error("Bad Statement"); //none of the above???
    }

    static int maxargs(Exp e) //Expressions version
    {
        if (e instanceof OpExp) //check the left and right
        {
            OpExp oe = (OpExp) e;
            return Math.max(maxargs(oe.left), maxargs(oe.right));
        }
        if (e instanceof EseqExp) //check the exp and the stm
        {
            EseqExp ee = (EseqExp) e;
            return Math.max(maxargs(ee.exp), maxargs(ee.stm));
        }
        return 0; //no more nested statements
    }

    static void interp(Stm s) //initial call wrapper
    {
        // interpret s with respect to an empty Table
        interpStm(s, null);
    }

    static class Table {

        final String id;
        final int value;
        final Table tail;

        Table(String i, int v, Table t) {
            id = i;
            value = v;
            tail = t;
        }
    }

    static int lookup(Table t, String key) // Returns the value of key in Table t.
    {
        if (t == null) {
            throw new Error("unknown identifier: " + key);
        } else if (t.id.equals(key)) {
            return t.value;
        } else {
            return lookup(t.tail, key);
        }
    }

    // Returns a new Table that is the same as t except that id has value val.
    static Table update(Table t, String id, int val) {
        return new Table(id, val, t);
    }

    static class IntAndTable {

        final int i;
        final Table t;

        IntAndTable(int ii, Table tt) {
            i = ii;
            t = tt;
        }
    }

    static Table interpStm(Stm s, Table t) //handles statements
    {
        if (s instanceof CompoundStm) {
            CompoundStm cs = (CompoundStm) s;
            return interpStm(cs.stm2, interpStm(cs.stm1, t));
        }
        if (s instanceof AssignStm) {
            AssignStm as = (AssignStm) s;
            IntAndTable value = interpExp(as.exp, t); //need to interp the value!
            return update(value.t, as.id, value.i); //return new table
        }
        if (s instanceof PrintStm) {
            PrintStm ps = (PrintStm) s;
            return interpAndPrint(ps.exps, t);
        }
        throw new Error("Bad Statement"); //none of the above???
    }

    static Table interpAndPrint(ExpList exps, Table t) //handles print statements
    {
        if (exps instanceof PairExpList) //pair
        {
            PairExpList pel = (PairExpList) exps;
            IntAndTable head = interpExp(pel.head, t); //evalute
            System.out.print(head.i + " "); //print the value
            return interpAndPrint(pel.tail, head.t);
        }
        if (exps instanceof LastExpList) //last
        {
            LastExpList lel = (LastExpList) exps;
            IntAndTable value = interpExp(lel.head, t); //evalute
            System.out.print(value.i + "\n"); //print the value
            return value.t; //return the table
        }
        throw new Error("Invalid Print Arguments");
    }

    static IntAndTable interpExp(Exp e, Table t) //handles expressions
    {
        if (e instanceof NumExp) //number
        {
            NumExp ne = (NumExp) e;
            return new IntAndTable(ne.num, t);
        }
        if (e instanceof IdExp) //variable
        {
            IdExp ie = (IdExp) e;
            return new IntAndTable(lookup(t, ie.id), t);
        }
        if (e instanceof OpExp) //operation
        {
            OpExp oe = (OpExp) e;
            IntAndTable left = interpExp(oe.left, t); //eval left value
            IntAndTable right = interpExp(oe.right, left.t); //eval right value
            if (oe.oper == 1) //plus
            {
                return new IntAndTable(left.i + right.i, right.t);
            }
            if (oe.oper == 2) //minus
            {
                return new IntAndTable(left.i - right.i, right.t);
            }
            if (oe.oper == 3) //times
            {
                return new IntAndTable(left.i * right.i, right.t);
            }
            if (oe.oper == 4) //division
            {
                return new IntAndTable(left.i / right.i, right.t);
            }
            throw new Error("Invalid operand");
        }
        if (e instanceof EseqExp) //Statement + Operation
        {
            EseqExp ee = (EseqExp) e;
            t = interpStm(ee.stm, t); //run the statement
            IntAndTable value = interpExp(ee.exp, t); //run the expression
            return new IntAndTable(value.i, value.t);
        }
        throw new Error("Unknown Expression"); //none of the above???
    }

    public static void main(String args[]) {
        System.out.println("maxargs result: " + maxargs(prog.prog));
        System.out.print("interpretation result: ");
        interp(prog.prog);
    }
}