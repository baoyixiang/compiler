import java.util.HashMap;

public class Main {

    public static void main(String[] args) {
        HashMap<Character, String> pro = new HashMap<>();
        pro.put('S', "(R)|a|^");
        pro.put('R', "T");
        pro.put('T', "S,T|S");

        program prog = new program();
        prog.setProgram(pro);

        System.out.println(prog.analysis("((a),a)"));

        for (Character item :
                prog.getVNVT()) {
            System.out.print(item + " ");
        }
        System.out.println();
        for (int i = 0; i < prog.getVNVT().size(); i++) {
            for (int j = 0; j < prog.getVNVT().size(); j++) {
                System.out.print(prog.getFinalRelationship()[i][j] + " ");
            }
            System.out.println();
        }

    }
}
