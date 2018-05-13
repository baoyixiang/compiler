import java.util.HashMap;

public class Main {

    public static void main(String[] args) {

        SignPriority signPriority = new SignPriority();
        HashMap<Character, String> program = new HashMap<>();
        program.put('E', "E+T|T");
        program.put('T', "T*F|F");
        program.put('F', "(E)|i");

        signPriority.setPro(program);

        for (Character vt :
                signPriority.getVT()) {
            System.out.print(vt + " ");
        }
        System.out.println();
        for (int i = 0; i < signPriority.getVT().size(); i++) {
            for (int j = 0; j < signPriority.getVT().size(); j++) {
                System.out.print(signPriority.getSignRelationship()[i][j] + " ");
            }
            System.out.println();
        }

        signPriority.analysis("((+i)i");
    }
}
