// print((print(1, 2), 3))
class prog4 {

    static Stm prog = new PrintStm(
            new LastExpList(new 
            EseqExp(new 
            PrintStm(new 
            PairExpList(new NumExp(1), new 
            LastExpList(new NumExp(2)))), new NumExp(3))));
}
// Should produce the following output:
//
// maxargs result: 2
// interpretation result: 1 2
// 3

