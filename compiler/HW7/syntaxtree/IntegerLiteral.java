package syntaxtree;

import visitor.ExpVisitor;
import visitor.TypeVisitor;
import visitor.Visitor;

public class IntegerLiteral extends Exp {
    public int i;

    public IntegerLiteral(int p, int ai) {
        pos = p;
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
