package syntaxtree;

import visitor.ExpVisitor;
import visitor.TypeVisitor;
import visitor.Visitor;

public class Formal {
    public int pos;
    public Type t;
    public Identifier i;

    public Formal(int p, Type at, Identifier ai) {
        pos = p;
        t = at;
        i = ai;
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
