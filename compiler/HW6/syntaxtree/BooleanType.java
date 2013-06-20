package syntaxtree;

import visitor.ExpVisitor;
import visitor.TypeVisitor;
import visitor.Visitor;

public class BooleanType extends Type {
    public BooleanType(int p) {
        pos = p;
    }

    // for type checking
    public BooleanType() {
        pos = -1;
    }

    public String toString() {
        return "boolean";
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
