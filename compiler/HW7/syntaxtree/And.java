package syntaxtree;

import visitor.ExpVisitor;
import visitor.TypeVisitor;
import visitor.Visitor;

public class And extends Exp {
    public Exp e1, e2;

    public And(int p, Exp ae1, Exp ae2) {
        pos = p;
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
