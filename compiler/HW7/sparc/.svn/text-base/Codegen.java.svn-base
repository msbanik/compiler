//Mahmudur Rahman, George Burri and Madhusudan Banik

//SPARC Instruction Selection

//*************************  Assignment 7  ****************************

/*Comments: 
* Minijava supports only PLUS, MINUS, MUL arithmetic operators and AND logical operator.
* Minijava supports only LT, EQ, NEQ conditional operators.
* Efficient usage of transient registers seems to need a lot of thinking :).
* Function call is a tricky part, needed some debugging.
* Binop expression both constant case is handled for efficiency, found during debugging process
*/

package sparc;

public class Codegen {

	SparcFrame frame;

	public Codegen(SparcFrame f) {
		frame = f;
	}

	// ilist holds the list of instructions generated so far.
	private assem.InstrList ilist = null, last = null;

	private void emit(assem.Instr inst) {
		// Add inst to the end of ilist.
		if (last != null)
			last = last.tail = new assem.InstrList(inst, null);
		else
			last = ilist = new assem.InstrList(inst, null);
	}

	// Two handy abbreviations:
	private void eOPER(String format, temp.TempList dst, temp.TempList src,
			temp.LabelList jmp) {
		emit(new assem.OPER(format, dst, src, jmp));
	}

	private void eOPER(String format, temp.TempList dst, temp.TempList src) {
		emit(new assem.OPER(format, dst, src));
	}

	// It's handy to have an abbreviation for creating TempLists. (See p. 194.)
	static temp.TempList L(temp.Temp h, temp.TempList t) {
		return new temp.TempList(h, t);
	}

	// Since Sparc instructions are often limited to 13-bit signed constants,
	// it's useful to be able to check easily for this case.
	static boolean is13bitCONST(tree.Exp e) {
		if (e instanceof tree.CONST) {
			int val = ((tree.CONST) e).value;
			return (-4096 <= val && val < 4096);
		} else
			return false;
	}

        // Here we reserve three fixed "transient" registers to reuse:
        private temp.Temp transient1 = frame.g1;
        static private temp.Temp transient2 = new temp.Temp();
        static private temp.Temp transient3 = new temp.Temp();
        
        /*-------------------------------------------------------------*          
         *                      MUNCH STATEMENTS                      *          
         *-------------------------------------------------------------*/
        
	void munchStm(tree.Stm s) {
		if (s instanceof tree.MOVE)
			munchStm((tree.MOVE) s);
		else if (s instanceof tree.EXPR)
			munchStm((tree.EXPR) s);
		else if (s instanceof tree.JUMP)
			munchStm((tree.JUMP) s);
		else if (s instanceof tree.CJUMP)
			munchStm((tree.CJUMP) s);
		else if (s instanceof tree.LABEL)
			munchStm((tree.LABEL) s);
		// Since we've canonicalized, tree.SEQ should not be a possibility.
		else
			throw new Error("munchStm dispatch");
	}

	/* MOVE Statement
         * Program 9.5 instruction pseudocode p. 194.
         */
	void munchStm(tree.MOVE s) {
                //  MOVE(TEMP(i), e2)
		if (s.dst instanceof tree.TEMP)     
			munchExp((tree.Exp) s.src, ((tree.TEMP) s.dst).temp);
		else if (s.dst instanceof tree.MEM) 
                {
                    tree.Exp e = ((tree.MEM) s.dst).exp;
                    if (e instanceof tree.BINOP) {      
                        //two possibilities
                        tree.BINOP bin = (tree.BINOP) e;
                        if (bin.left instanceof tree.CONST) {
                            //  MOVE(MEM(BINOP(PLUS, CONST(i), e1)), e2)
                            if (bin.binop == tree.BINOP.PLUS) {
                                    eOPER("\tst\t`s1, [`s0 + " + ((tree.CONST) bin.left).value + "]\n", null,
                                         L(munchExp(bin.right), L(munchExp(s.src), null)));                                      
                            }
                        }
                        else if (bin.right instanceof tree.CONST) {
                            //  MOVE(MEM(BINOP(PLUS, e1, CONST(i))), e2)
                            if (bin.binop == tree.BINOP.PLUS) {
                                eOPER("\tst\t`s1, [`s0 + " + ((tree.CONST) bin.right).value + "]\n", null,
                                      L(munchExp(bin.left), L(munchExp(s.src), null)));
                            }
                        }                       
                    }
                    //  MOVE(CONST(i), e2)
                    else if (e instanceof tree.CONST) {
                            eOPER("\tst\t`s0, [`d0 + " + ((tree.CONST) e).value + "]", L(
                                 transient1, null), L(munchExp(s.src), null));
                    }
                    //  MOVE(MEM(e1), MEM(e2))
                    else if (s.src instanceof tree.MEM) {
                            eOPER("\tld [`s0], `d0\n\tst `d0, [`s1]\n", null, 
                                    L(munchExp(((tree.MEM) s.src).exp), L(munchExp(e), null)));                           
                    }                   
                    // MOVE(MEM(e1), e2)
                    /*eOPER("\tst\t`s1, [`s0]\n", null, L(munchExp(e, transient1), 
                            L(munchExp(s.src, transient2), null)));*/
                    eOPER("\tst\t`s1, [`s0]\n", null, L(munchExp(e), 
                            L(munchExp(s.src), null)));
		} 
                else
			throw new Error("Bad MOVE destination.");
	}

        /* EXPR Statement
         * 
         */
         
	void munchStm(tree.EXPR s) {
		munchExp(s.exp, null);
	}
        
        /*JUMP Statement
	 * Unconditional jump
	 */
	void munchStm(tree.JUMP s) {
		eOPER("\tba\t" + s.targets.head.toString() + "\n\tnop\n", null, null);		
	}
        
        /*CJUMP Statement
	 * Conditional Jump
         * Branch Instructions
	 */
	void munchStm(tree.CJUMP s) {
            //At first compare instructions
            //Comparison includes 3 cases
            
		if (s.left instanceof tree.CONST) {
			eOPER("\tcmp\t`s0, " + ((tree.CONST) s.left).value+ "\n", null,
                            L(munchExp(s.right), null));
		} 
                else if (s.right instanceof tree.CONST){
			eOPER("\tcmp\t`s0, " + ((tree.CONST) s.right).value+ "\n", null,
                            L(munchExp(s.left), null));
                }
		else{
			eOPER("\tcmp\t`s0, `s1\n", null, L(munchExp(s.left), L(munchExp(s.right), null)));
                }
                //now check conditions between 1st value and 2nd value
                switch(s.relop)                 {                 
                    case tree.CJUMP.EQ:                         
                        eOPER("\tbe\t" + s.iftrue.toString() + "\n\tnop\n", null, null);                       
                        break;                 
                    case tree.CJUMP.NE:                         
                        eOPER("\tbne\t" + s.iftrue.toString() + "\n\tnop\n", null, null);                        
                        break;                 
                    case tree.CJUMP.LT:  
			eOPER("\tbl\t" + s.iftrue.toString() + "\n\tnop\n", null, null);                     
                        break;
               //GT, LE and GE are also invalid operator for minijava
                    /*case tree.CJUMP.GT:                                   
                        eOPER("\tbg\t" + s.iftrue.toString() + "\n\tnop\n", null, null);                         
                        break; 
                    case tree.CJUMP.LE:                         
                        eOPER("\tble\t" + s.iftrue.toString() + "\n\tnop\n", null, null);                       
                        break;                                             
                    case tree.CJUMP.GE:                         
                        eOPER("\tbge\t" + s.iftrue.toString() + "\n\tnop\n", null, null);                    
                        break; */                		
                    default:
                        throw new Error("Invalid operator for MINIJAVA");
                }
	}
        
        /*LABEL Statement
	 * 
	 */
        void munchStm(tree.LABEL s) {
		emit(new assem.LABEL(s.label.toString() + ":\n", s.label));		
	}
        
        /*-------------------------------------------------------------*          
         *                      MUNCH EXPRESSIONS                      *          
         *-------------------------------------------------------------*/
               
	// Here is munchExp as specified by Appel on p. 193.
	temp.Temp munchExp(tree.Exp e) {
		return munchExp(e, null);
	}

	// I give munchExp an extra parameter r that can specify a Temp in which
	// the result can safely be put. If r is null, then munchExp must come up
	// with a suitable Temp on its own (usually by generating a fresh one).
	temp.Temp munchExp(tree.Exp e, temp.Temp r) {
		if (e instanceof tree.CONST)
			return munchExp((tree.CONST) e, r);
		if (e instanceof tree.NAME)
			return munchExp((tree.NAME) e, r);
		if (e instanceof tree.TEMP)
			return munchExp((tree.TEMP) e, r);
		if (e instanceof tree.BINOP)
			return munchExp((tree.BINOP) e, r);
		if (e instanceof tree.MEM)
			return munchExp((tree.MEM) e, r);
		if (e instanceof tree.CALL)
			return munchExp((tree.CALL) e, r);
		// Since we've canonicalized, tree.ESEQ should not be a possibility.
		else
			throw new Error("munchExp dispatch");
	}

	/**
	 * CONST Expression
         * Need to check constant's bitsize
	 */
	temp.Temp munchExp(tree.CONST e, temp.Temp r) {
            if(r==null) 
                r=new temp.Temp();                                     
            
            if(is13bitCONST(e))             //LE 13 bit constant
                    eOPER("\tmov\t" + e.value + ", `d0\n", L(r, null), null);
            else                            //GT 13 bit constant
                    eOPER("\tsethi\t%hi(" + e.value+ "), `d0\n\tor\t`d0, %lo("
                                     + e.value + "), `d0\n", L(r, null), null);
            return r;
	}

        /**
	 * NAME Expression
	 */
	temp.Temp munchExp(tree.NAME e, temp.Temp r) {
		return null;
	}

        /**
	 * TEMP Expression
	 */
	temp.Temp munchExp(tree.TEMP e, temp.Temp r) {
            if(r==null)
                return e.temp;
            else{
                eOPER("\tmov\t`s0, `d0\n", L(r, null), L(e.temp, null));
		return r;
            } 
	}

	/** BINOP Expression
	 * We only consider PLUS, MINUS, MUL and AND which are available in Minijava
         * Each case is subdivided into multiple sub-cases
	 */
	temp.Temp munchExp(tree.BINOP e, temp.Temp r) {
		if (r == null)
			r = new temp.Temp();
                
		if (e.binop == tree.BINOP.PLUS) {			
                        if (is13bitCONST(e.left) && !(is13bitCONST(e.right))){
				eOPER("\tadd\t`s0, " + ((tree.CONST) e.left).value + 
                                    ", `d0\n", L(r, null), L(munchExp(e.right), null));
                        }
			else if (is13bitCONST(e.right)&& !(is13bitCONST(e.left))){
				eOPER("\tadd\t`s0, " + ((tree.CONST) e.right).value + 
                                    ", `d0\n", L(r, null), L(munchExp(e.left), null));
                        }
                        else if (is13bitCONST(e.left) && is13bitCONST(e.right)) {       //for two constants, we can produce direct output
				munchExp(new tree.CONST(((tree.CONST) e.left).value
						+ ((tree.CONST) e.right).value), r);
			}
                        else{
				eOPER("\tadd\t`s0, `s1, `d0\n", L(r, null), 
                                    L(munchExp(e.left), L(munchExp(e.right), null)));
                        }
			return r;
		}                 
                else if (e.binop == tree.BINOP.MINUS) {			
			if (is13bitCONST(e.right) && !(is13bitCONST(e.left))){
				eOPER("\tsub\t`s0, " + ((tree.CONST) e.right).value + ", `d0\n", 
                                    L(r, null), L(munchExp(e.left), null));
                        }
                        else if (is13bitCONST(e.left) && is13bitCONST(e.right)) {   //for two constants, we can produce direct output
				munchExp(new tree.CONST(((tree.CONST) e.left).value
						- ((tree.CONST) e.right).value), r);
			}
                        else{
				eOPER("\tsub\t`s0, `s1, `d0\n", L(r, null), 
                                    L(munchExp(e.left), L(munchExp(e.right), null)));
                        }
			return r;                       
		}                 
                else if (e.binop == tree.BINOP.MUL) {                     
                        if (is13bitCONST(e.left)&& !(is13bitCONST(e.right)))
                        {
                                eOPER("\tsmul\t`s0, " + ((tree.CONST) e.left).value + ", `d0\n",
                                    L(r, null), L(munchExp(e.right), null));                            
			}                      
                        else if (is13bitCONST(e.right)&& !(is13bitCONST(e.left)))
                        {
                                eOPER("\tsmul\t`s0, " + ((tree.CONST) e.right).value + ", `d0\n",
                                    L(r, null), L(munchExp(e.left), null));	                           
			}
                        else if (is13bitCONST(e.left) && is13bitCONST(e.right)) {   //for two constants, we can produce direct output
				munchExp(new tree.CONST(((tree.CONST) e.left).value
						* ((tree.CONST) e.right).value), r);
			}
                        else
				eOPER("\tsmul\t`s0, `s1, `d0\n", L(r, null),
                                    L(munchExp(e.left), L(munchExp(e.right), null)));
			return r;                      
		} 
                else if (e.binop == tree.BINOP.AND){
                        eOPER("\tand\t`s0, `s1, `d0\n", L(r, null),L(munchExp(e.left),
                            L(munchExp(e.right,null), null)));
                    return r;
                }
                // Rest invalid operators for Minijava
		else
			throw new Error("Invalid operator for MINIJAVA");
	}

	/**
	 * MEM Expression
         * Program 9.6 pseudocode P. 195
	 */
	temp.Temp munchExp(tree.MEM e, temp.Temp r) {
		if (r == null)
			r = new temp.Temp();               
                
		if (e.exp instanceof tree.BINOP) {
			tree.BINOP bin = (tree.BINOP) e.exp;
			
			if (bin.left instanceof tree.CONST) {
				eOPER("\tld\t[`s0+" + ((tree.CONST) bin.left).value + "], `d0\n", 
                                    L(r, null), L(munchExp(bin.right, null), null));
			}
                        else if (bin.right instanceof tree.CONST) {
				eOPER("\tld\t[`s0+" + ((tree.CONST) bin.right).value + "], `d0\n", 
                                    L(r, null), L(munchExp(bin.left, null), null));
			}
			else
				eOPER("\tld\t[`s0], `d0\n", L(r, null), L(munchExp(e.exp), null));
		}
                else if (e.exp instanceof tree.Exp) {
			eOPER("\tld\t[`s0], `d0\n", L(r, null), L(munchExp(e.exp), null));
		}
		else if (e.exp instanceof tree.CONST) {
			eOPER("\tld\t[`s0+" + ((tree.CONST) e.exp).value + "], `d0\n", L(r, null), null);
		}
		
		else
			throw new Error("Invalid expression format for MEM");
		return r;
	}

	/**
	 * CALL Expression
         * Procedure call: EXP(CALL(f,args))
         * Pseudocode help in P. 194
	 */       
        public temp.Temp munchExp(tree.CALL e,temp.Temp r){
            
            temp.TempList tmpL = munchArgs(0, (tree.ExpList) e.args);         
            eOPER("\tcall\t`j0\n\tnop\n", null, tmpL, new temp.LabelList
                    (((tree.NAME) e.func).label, null));
            /* 
             * retrieve the the return-value
             */
            if (r == null)
		r = frame.RVCaller();
            else
		emit(new assem.MOVE("\tmov\t`s0, `d0\n", r, frame.RVCaller()));
            
            return r;
        }  
        
        public temp.TempList munchArgs(int count, tree.ExpList args){
              if(args==null) 
                  return null;
              temp.Temp curArg=null;                //current outgoing arguement register
              temp.TempList tempL=null;             //return tempList
                    //using outgoing arguement registers
              for (; (args != null) && (curArg = SparcFrame.outgoingArgs[count]) != null; count++) {
                    munchExp(args.head, curArg);
                    args = args.tail;              //take rest arguement list
                    tempL = L(curArg, tempL);      //pick the rest one                 
              }
              return tempL;
         }
        
        /**
	 * Main codegen function
	 */
	assem.InstrList codegen(tree.Stm s) {
		munchStm(s);
		assem.InstrList list = ilist;
		ilist = last = null;
		return list;
	}
}