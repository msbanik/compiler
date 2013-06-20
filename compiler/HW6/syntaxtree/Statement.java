package syntaxtree;

import visitor.ExpVisitor;
import visitor.TypeVisitor;
import visitor.Visitor;

public abstract class Statement {
    public int pos;

    public abstract void accept(Visitor v);

    public abstract Type accept(TypeVisitor v);

    public abstract semant.Exp accept(ExpVisitor v);
}
