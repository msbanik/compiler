package syntaxtree;

import visitor.ExpVisitor;
import visitor.TypeVisitor;
import visitor.Visitor;

public class Program {
    public int pos;
    public MainClass m;
    public ClassDeclList cl;

    public Program(int p, MainClass am, ClassDeclList acl) {
        pos = p;
        m = am;
        cl = acl;
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
