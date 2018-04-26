import java.util.ArrayList;

public class Main {

    public static void main(String[] args) {
        // 文法
        ArrayList<String> program = new ArrayList<>();
        program.add("S::A");
        program.add("A::BD");
        program.add("D::iBD|~");
        program.add("B::CE");
        program.add("E::+CE|~");
        program.add("C::)A*|(");
        
        Program program1 = new Program();
        program1.setProgram(program);

        program1.setM();


        for (Character item :
                program1.getVN()) {
            System.out.print(item);
        }
        System.out.println();
        for (Character item :
                program1.getVT()) {
            System.out.print(item);
        }
        System.out.println();

        String M[][] = program1.getM();
        for (int i=0; i<program1.getVN().size(); i++) {
            for (int j=0; j<program1.getVT().size(); j++) {
                System.out.print(M[i][j] + "  ");
            }
            System.out.println();
        }


        program1.analyse("(i(");
    }
}
