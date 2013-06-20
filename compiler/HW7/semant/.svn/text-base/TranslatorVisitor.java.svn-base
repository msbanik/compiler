package semant;

import frame.Access;
import frame.AccessList;
import syntaxtree.*;
import syntaxtree.Print;
import temp.Label;
import temp.Temp;
import tree.*;
import tree.ExpList;
import util.BoolList;

import java.util.HashMap;
import java.util.Map;


public class TranslatorVisitor extends visitor.ExpDepthFirstVisitor {

    private SymbolTable classTable;
    private frame.Frame currFrame;
    private ClassInfo currClass;
    private MethodInfo currMethod;
    private tree.Exp currThis;
    private Frag frags;        // Linked list of accumlated fragments.
    private boolean optimize;    // Do we want to optimize?
    private Map<String, tree.Exp> varMap = new HashMap<String, tree.Exp>();

    public TranslatorVisitor(SymbolTable t, frame.Frame f, boolean optim) {
        classTable = t;
        currFrame = f;
        currClass = null;
        currMethod = null;
        currThis = null;
        frags = null;
        optimize = optim;
    }

    public Frag getResult() {
        // Reverse frags and return it.
        Frag old = frags;
        frags = null;
        while (old != null) {
            Frag temp = old.next;
            old.next = frags;
            frags = old;
            old = temp;
        }
        return frags;
    }

    /**
     * 1
     * MainClass m;
     * ClassDeclList cl;
     * <p/>
     * Super class implementation is fine!
     *
     * @param n
     * @return
     */
    @Override
    public Exp visit(Program n) {
        return super.visit(n);
    }

    /**
     * 2
     * Identifier i1,i2;
     * Statement s;
     * <p/>
     * Implementation is provided, I don't have to do anything.
     *
     * @param n
     * @return
     */
    @Override
    public semant.Exp visit(MainClass n) {
        String id = n.i1.toString();

        currClass = classTable.get(id);

        currFrame = currFrame.newFrame(new temp.Label("main"), null);

        semant.Exp body = n.s.accept(this);
        procEntryExit(body, currFrame);

        return null;
    }

    /**
     * 3
     * Identifier i;
     * VarDeclList vl;
     * MethodDeclList ml;
     *
     * @param n
     * @return
     */
    @Override
    public Exp visit(ClassDeclSimple n) {
        currClass = classTable.get(n.i.s);

        //varDecal list
        InHeap inHeap;
        tree.Exp exp;
        if (n.vl.size() > 0) {
            inHeap = new InHeap(0);
            exp = inHeap.exp(new TEMP(currFrame.RVCallee()));
            varMap.put(n.vl.elementAt(0).i.s, exp);
        }
        for (int i = 1; i < n.vl.size(); i++) {
            inHeap = new InHeap(i * currFrame.wordSize());
            exp = inHeap.exp(new TEMP(currFrame.RVCallee()));
            varMap.put(n.vl.elementAt(i).i.s, exp);
        }

        for (int i = 0; i < n.ml.size(); i++) {
            n.ml.elementAt(i).accept(this);
        }
        currClass = null;
        return null;
    }

    /**
     * 4
     * Identifier i;
     * Identifier j;
     * VarDeclList v1;
     * MethodDeclList ml;
     *
     * @param n
     * @return
     */
    @Override
    public Exp visit(ClassDeclExtends n) {
        currClass = classTable.get(n.i.s);
        super.visit(n);
        currClass = null;
        return null;
    }

    /**
     * 5
     * Type t;
     * Identifier i;
     *
     * @param n
     * @return
     */
    @Override
    public Exp visit(VarDecl n) {
        tree.Exp exp;
        if (currMethod == null) {
            System.out.println(n.i.s + " : " + n.pos);
            exp = new BINOP(BINOP.PLUS, new TEMP(new Temp()), new CONST(currFrame.wordSize()));
            varMap.put(n.i.s, exp);
        } else {
            exp = currFrame.allocLocal(false).exp(new TEMP(currFrame.FP()));
            varMap.put(currMethod.getName() + "$" + n.i.s, exp);
        }
        return new Ex(exp);
    }

    /**
     * 6
     * Type t;
     * Identifier i;
     * FormalList fl;
     * VarDeclList vl;
     * StatementList sl;
     * Exp e;
     *
     * @param n
     * @return
     */
    @Override
    public Exp visit(MethodDecl n) {
        LABEL methodName = new LABEL(new Label(currClass.getName() + "$" + n.i.s));
        currMethod = currClass.getMethod(n.i.s);

        //formals
        //True: escape [Frame], False: not escape [InReg]
        BoolList formals = new BoolList(false, null);
        for (int i = 0; i < n.fl.size(); i++) {
            formals = new BoolList(false, formals);
        }

        // For this
        formals.head = false;
        currFrame = currFrame.newFrame(methodName.label, formals);

        currThis = currFrame.formals.head.exp(new TEMP(currFrame.FP()));

        AccessList tail = currFrame.formals.tail;
        for (int i = 0; i < n.fl.size(); i++) {
            tree.Exp exp = tail.head.exp(new TEMP(currFrame.FP()));
            varMap.put(currMethod.getName() + "$" + n.fl.elementAt(i).i.s, exp);
        }

        //varDecal list
        for (int i = 0; i < n.vl.size(); i++) {
            n.vl.elementAt(i).accept(this);
        }

        //Statement list
        Stm s = null;
        if (n.sl.size() > 0) {
            s = n.sl.elementAt(0).accept(this).unNx();
            for (int i = 1; i < n.sl.size(); i++) {
                s = new SEQ(s, n.sl.elementAt(i).accept(this).unNx());
            }
        }

        //Return statement
        Exp ret = n.e.accept(this);
        MOVE move = new MOVE(new TEMP(currFrame.RVCallee()), ret.unEx());

        if (s != null)
            s = new SEQ(s, move);
        else
            s = move;

        Frag func = new ProcFrag(s, currFrame);
        func.next = frags;
        frags = func;
        currMethod = null;
        return null;
    }

    /**
     * 7
     *
     * @param n
     * @return
     */
    @Override
    public Exp visit(Formal n) {
        return visit(new VarDecl(n.pos, n.t, n.i));
    }

    /**
     * 8
     *
     * @param n
     * @return
     */
    @Override
    public Exp visit(IntArrayType n) {
        return new Ex(new CONST(0));
    }

    /**
     * 9
     *
     * @param n
     * @return
     */
    @Override
    public Exp visit(BooleanType n) {
        return new Ex(new CONST(0));
    }

    /**
     * 10
     *
     * @param n
     * @return
     */
    @Override
    public Exp visit(IntegerType n) {
        return new Ex(new CONST(0));
    }

    /**
     * 11
     *
     * @param n
     * @return
     */
    @Override
    public Exp visit(IdentifierType n) {
        return new Ex(new CONST(0));
    }

    /**
     * 12
     * StatementList sl;
     *
     * @param n
     * @return
     */
    @Override
    public Exp visit(Block n) {
        tree.Stm stm;
        if (n.sl.size() == 0) {
            return new Ex(new CONST(0));
        } else {
            Exp exp1 = n.sl.elementAt(0).accept(this);

            if (n.sl.size() == 1)
                return exp1;
            //more then one stm
            stm = exp1.unNx();
            for (int i = 1; i < n.sl.size(); i++) {
                exp1 = n.sl.elementAt(i).accept(this);
                stm = new SEQ(stm, exp1.unNx());
            }
        }
        return new Nx(stm);
    }

    /**
     * 13
     * if e:
     * s1
     * else:
     * s2
     * <p/>
     * begin:
     * cjump(eq, e, 1, t, f)
     * t:
     * s1
     * jump end
     * f:
     * s2
     * end:
     *
     * @param i
     * @return
     */
    @Override
    public Exp visit(If i) {

        Exp e = i.e.accept(this);
        Exp s1 = i.s1.accept(this);
        Exp s2 = i.s2.accept(this);

        Label t = new Label();
        Label f = new Label();
        Label end = new Label();

        return new Nx(new SEQ(
                new CJUMP(CJUMP.EQ, e.unEx(), new CONST(1), t, f), new SEQ(
                new LABEL(t), new SEQ(
                s1.unNx(), new SEQ(
                new JUMP(end), new SEQ(
                new LABEL(f), new SEQ(
                s2.unNx(), new LABEL(end)
        )))))));
    }

    /**
     * 14
     * while e:
     * s
     *
     * @param w
     * @return
     */
    @Override
    public Exp visit(While w) {


        Exp e = w.e.accept(this);
        Exp s = w.s.accept(this);

        Label test = new Label();
        Label begin = new Label();
        Label end = new Label();

        return new Nx(
                new SEQ(
                        new JUMP(test),
                        new SEQ(
                                new LABEL(begin),
                                new SEQ(s.unNx(), new SEQ(
                                        new LABEL(test), new SEQ(
                                        new CJUMP(CJUMP.NE, e.unEx(), new CONST(0), begin, end), new LABEL(end)))))));

    }

    /**
     * 15
     * print
     * System.out.println(e)
     */
    @Override
    public Exp visit(Print n) {
        Exp e = n.e.accept(this);
        return new Ex(currFrame.externalCall("printInt", new ExpList(e.unEx(), null)));
    }

    /**
     * 16
     * Identifier = Exp;
     *
     * @param n
     * @return
     */
    @Override
    public Exp visit(Assign n) {
        Exp e = n.e.accept(this);
        Exp i = n.i.accept(this);

        return new Nx(new MOVE(getVar(n.i.s).unEx(), e.unEx()));
    }

    /**
     * 17
     * i[e1] = e2
     *
     * @param n
     * @return
     */
    @Override
    public Exp visit(ArrayAssign n) {

        Exp i = n.i.accept(this);
        Exp e1 = n.e1.accept(this);
        Exp e2 = n.e2.accept(this);

        return new Nx(
                new MOVE(new MEM(
                        new BINOP(BINOP.PLUS,
                                i.unEx(), new BINOP(BINOP.MUL, e1.unEx(), new CONST(currFrame.wordSize())))),
                        e2.unEx()));
    }

    /**
     * 18
     * e1 && e2
     *
     * @param a
     * @return
     */
    @Override
    public Exp visit(And a) {

        Exp e1 = a.e1.accept(this);
        Exp e2 = a.e2.accept(this);
        return new IfThenElseExp(e1, e2, new Ex(new CONST(0)));
    }


    /**
     * 19
     * flag = a < b
     * <p/>
     * r = 1
     * f:
     * r = 0
     * t:
     * r
     *
     * @param lt
     * @return
     */
    @Override
    public Exp visit(LessThan lt) {

        Exp a = lt.e1.accept(this);
        Exp b = lt.e2.accept(this);
        return new RelCx(CJUMP.LT, a.unEx(), b.unEx());
    }

    /**
     * 20
     * e1 + e2
     *
     * @param p
     * @return
     */
    @Override
    public Exp visit(Plus p) {
        Exp e1 = p.e1.accept(this);
        Exp e2 = p.e2.accept(this);
        return new Ex(
                new BINOP(
                        BINOP.PLUS, e1.unEx(), e2.unEx()));
    }

    /**
     * 21
     * e1 - e2
     *
     * @param m
     * @return
     */
    @Override
    public Exp visit(Minus m) {
        Exp e1 = m.e1.accept(this);
        Exp e2 = m.e2.accept(this);
        return new Ex(
                new BINOP(
                        BINOP.MINUS, e1.unEx(), e2.unEx()));
    }

    /**
     * 22
     * e1 * e2
     *
     * @param t
     * @return
     */
    @Override
    public Exp visit(Times t) {
        Exp e1 = t.e1.accept(this);
        Exp e2 = t.e2.accept(this);
        return new Ex(
                new BINOP(
                        BINOP.MUL, e1.unEx(), e2.unEx()));
    }

    /**
     * 23
     * <p/>
     * e1 [ e2 ]
     *
     * @param n
     * @return
     */
    @Override
    public Exp visit(ArrayLookup n) {
        Exp e1 = n.e1.accept(this);
        Exp e2 = n.e2.accept(this);

        return new Ex(new MEM(new BINOP(BINOP.PLUS, e1.unEx(),
                mul(e2.unEx(),
                        new CONST(currFrame.wordSize())))));

//        return new Ex(new MEM(new BINOP(BINOP.PLUS, e1.unEx(),
//
//                new BINOP(BINOP.MUL, e2.unEx(),
//                        new CONST(currFrame.wordSize())))));

    }

    /**
     * 24
     *
     * @param n
     * @return
     */
    @Override
    public Exp visit(ArrayLength n) {
        return new Ex(new BINOP(BINOP.PLUS, n.e.accept(this).unEx(), new CONST(0)));
    }

    /**
     * 25
     * Exp e;
     * Identifier i;
     * ExpList e1;
     * <p/>
     * e.i(el)
     *
     * @param n
     * @return
     */
    @Override
    public Exp visit(Call n) {

        Exp e = n.e.accept(this);

        ExpList expList = null;
        for (int i = n.el.size() - 1; i >= 0; i--) {
            syntaxtree.Exp exp = n.el.elementAt(i);
            expList = new ExpList(exp.accept(this).unEx(), expList);
        }
        expList = new ExpList(e.unEx(), expList);

        return new Ex(new CALL(new NAME(new Label(n.fullname)), expList));
    }

    /**
     * 26
     * int i;
     *
     * @param n
     * @return
     */
    @Override
    public semant.Exp visit(IntegerLiteral n) {
        return new Ex(new tree.CONST(n.i));
    }

    /**
     * 27
     *
     * @param t
     * @return
     */
    @Override
    public Exp visit(True t) {
        return new Ex(new CONST(1));
    }

    /**
     * 28
     *
     * @param f
     * @return
     */
    @Override
    public Exp visit(False f) {
        return new Ex(new CONST(0));
    }

    /**
     * 29
     *
     * @param n
     * @return
     */
    @Override
    public Exp visit(IdentifierExp n) {
        //For instance
        String name = currMethod.getName() + "$" + n.s;
        tree.Exp exp = varMap.get(name);
        if (exp == null)
            exp = varMap.get(n.s);

        return new Ex(exp);
    }

    /**
     * 30
     *
     * @param n
     * @return
     */
    @Override
    public Exp visit(This n) {
        AccessList accessList = currFrame.formals;
        Access access = accessList.head;
        tree.Exp exp = access.exp(new TEMP(currFrame.FP()));
        return new Ex(exp);

    }

    /**
     * 31
     *
     * @param n
     * @return
     */
    @Override
    public Exp visit(NewArray n) {

        return new Ex(currFrame.externalCall("initArray",
                new ExpList(new BINOP(BINOP.MUL,
                        new BINOP(BINOP.PLUS, n.e.accept(this).unEx(),
                                new CONST(1)), new CONST(currFrame.wordSize())), null)));

    }

    /**
     * 32
     * new i();
     *
     * @param n
     * @return
     */
    @Override
    public Exp visit(NewObject n) {

        ClassInfo info = classTable.get(n.i.s);

        ExpList expList = new ExpList(new CONST(currFrame.wordSize()), null);
        expList = new ExpList(new CONST(info.getFieldsCount()), expList);

        return new Ex(currFrame.externalCall("calloc", expList));
    }

    /**
     * 33
     * !exp
     * 1 - exp
     *
     * @param n
     * @return
     */
    @Override
    public Exp visit(Not n) {
        Exp exp = n.e.accept(this);
        return new Ex(
                new BINOP(
                        BINOP.MINUS, new CONST(1), exp.unEx()));
    }

    /**
     * 34
     *
     * @param n
     * @return
     */
    @Override
    public Exp visit(Identifier n) {
        return getVar(n.s);
    }

    private Exp getVar(String s) {
        tree.Exp exp = varMap.get(currMethod.getName() + "$" + s);
        if (exp == null) {
            exp = varMap.get(s);
        }
        return new Ex(exp);
    }

    // Now we have some auxiliary functions:

    // Create a fragment for a function and add it to the front of frags.
    private void procEntryExit(Exp body, frame.Frame funcFrame) {
        Frag func = new ProcFrag(funcFrame.procEntryExit1(body.unNx()), funcFrame);
        func.next = frags;
        frags = func;
    }

    // plus and mul are useful abbreviations that could do simple optimizations.

    private tree.Exp plus(tree.Exp e1, tree.Exp e2) {
        return new tree.BINOP(tree.BINOP.PLUS, e1, e2);
    }

    private tree.Exp mul(tree.Exp e1, tree.Exp e2) {
        if (optimize) {
            if (e1 instanceof CONST && e2 instanceof CONST) {
                CONST c1 = (CONST) e1;
                CONST c2 = (CONST) e2;
                tree.Exp exp = new CONST(c1.value * c2.value);
                return exp;
            }
//            else if (e1 instanceof TEMP && e2 instanceof CONST) {
//                CONST c2 = (CONST) e2;
//                if (c2.value == 4)
//                    return new BINOP(BINOP.LSHIFT, e1, new CONST(2));
//            }
        }
        return new tree.BINOP(tree.BINOP.MUL, e1, e2);
    }

    // Finally, we have several nested auxiliary classes:

    class InHeap extends frame.Access {
        int offset;

        InHeap(int o) {
            offset = o;
        }

        // Here the base pointer will be the "this" pointer to the object.
        public tree.Exp exp(tree.Exp basePtr) {
            return new tree.MEM(plus(basePtr, new tree.CONST(offset)));
        }
    }

    // The subclasses of semant.Exp (Ex, Nx, Cx, RelCx, IfThenElseExp, ...)
    // naturally represent the various phrases of the abstract syntax.
    // They let us hold off on generating tree code for a phrase until
    // we see the *context* in which it is used.

    class Ex extends Exp {             // page 141
        tree.Exp exp;

        Ex(tree.Exp e) {
            exp = e;
        }

        tree.Exp unEx() {
            return exp;
        }

        tree.Stm unNx() {
            return new tree.EXPR(exp);
        }

        tree.Stm unCx(temp.Label t, temp.Label f) {
            return new tree.CJUMP(tree.CJUMP.NE, exp, new tree.CONST(0), t, f);
        }
    }

    class Nx extends Exp {             // page 141
        tree.Stm stm;

        Nx(tree.Stm s) {
            stm = s;
        }

        tree.Exp unEx() {
            throw new Error("unEx applied to Nx");
        }

        tree.Stm unNx() {
            return stm;
        }

        tree.Stm unCx(temp.Label t, temp.Label f) {
            throw new Error("unCx applied to Nx");
        }
    }

    abstract class Cx extends Exp {          // page 142

        tree.Exp unEx() {
            temp.Temp r = new temp.Temp();
            temp.Label t = new temp.Label();
            temp.Label f = new temp.Label();

            return new tree.ESEQ(
                    new tree.SEQ(new tree.MOVE(new tree.TEMP(r), new tree.CONST(1)),
                            new tree.SEQ(this.unCx(t, f),
                                    new tree.SEQ(new tree.LABEL(f),
                                            new tree.SEQ(new tree.MOVE(new tree.TEMP(r), new tree.CONST(0)),
                                                    new tree.LABEL(t))))),
                    new tree.TEMP(r));
        }

        abstract tree.Stm unCx(temp.Label t, temp.Label f);

        tree.Stm unNx() {
            Label t = new Label();
            Label f = new Label();
            return unCx(t, f);
        }
    }

    class RelCx extends Cx {             // page 149
        int relop;
        tree.Exp left;
        tree.Exp right;

        RelCx(int rel, tree.Exp l, tree.Exp r) {
            relop = rel;
            left = l;
            right = r;
        }

        tree.Stm unCx(temp.Label t, temp.Label f) {
            return new CJUMP(relop, left, right, t, f);
        }
    }

    class IfThenElseExp extends Exp {         // page 150
        // cond ? a : b
        Exp cond, a, b;
        temp.Label t = new temp.Label();
        temp.Label f = new temp.Label();
        temp.Label join = new temp.Label();

        IfThenElseExp(Exp cc, Exp aa, Exp bb) {
            cond = cc;
            a = aa;
            b = bb;
        }

        tree.Exp unEx() {

            Temp r = new Temp();

            return new ESEQ(new SEQ(cond.unCx(t, f),
                    new SEQ(new LABEL(t),
                            new SEQ(new MOVE(new TEMP(r), a.unEx()), new SEQ(
                                    new JUMP(join),
                                    new SEQ(new LABEL(f),
                                            new SEQ(new MOVE(new TEMP(r), b.unEx()),
                                                    new LABEL(join))))))),
                    new TEMP(r));
        }

        tree.Stm unNx() {
            return null;
        }

        tree.Stm unCx(temp.Label tt, temp.Label ff) {
            return new CJUMP(CJUMP.EQ, unEx(), new CONST(1), tt, ff);

//            return new SEQ(cond.unCx(tt, ff), new SEQ(new SEQ(new LABEL(tt), a.unCx(t, f)),
//                    new SEQ(new LABEL(ff), b.unCx(t, f))));
        }
    }
}






















