package syntaxtree;

import visitor.ExpVisitor;
import visitor.TypeVisitor;
import visitor.Visitor;

public class ArrayAssign extends Statement {
    public Identifier i;
    public Exp e1, e2;

    public ArrayAssign(int p, Identifier ai, Exp ae1, Exp ae2) {
        pos = p;
        i = ai;
        e1 = ae1;
        e2 = ae2;
    }

    public void accept(Visitor v) {
        v.visit(this);
    }

    public Type accept(TypeVisitor v) {
        return v.visit(this);
    }

    public semant.Exp accept(ExpVisitor v) {
        return v.visit(this);
    }

}

