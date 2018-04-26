import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Stack;

public class Program {

    private ArrayList<String> program;
    private ArrayList<Character> VN;
    private ArrayList<Character> VT;

    private Map<String, ArrayList<Character>> allFirst;
    private Map<String, ArrayList<Character>> allFollow;

    private String[][] M;

    /***
     * 求一个字符或者字符串的FIRST集合
     * @param str
     * @return
     */
    public ArrayList<Character> getOneFIRST(String str) {
        // 如果已经计算过了，那么就不用再算了， 这样可以提高效率，以免栈溢出
        if (allFirst.containsKey(str)) {
            return allFirst.get(str);
        }

        // 要返回的结果
        ArrayList<Character> result = new ArrayList<>();

        // 求FIRST有可以分为情况
        if (str.length() == 1 && !str.equals("~")) {
            Character tempCh = str.charAt(0);  // 相当于转为char

            if (VT.contains(tempCh)) {
                // FIRST(X) = {X};
                result.add(tempCh);

            } else if (VN.contains(tempCh)) {
                // 找tempCh -> 的产生式。
                for (String oneGe : program) {
                    String dep[] = oneGe.split(":");
                    // 找到
                    if (dep[0].equals(tempCh.toString())) {

                        String rights[];
                        if (dep[2].contains("|")) {
                            rights = dep[2].split("\\|");
                        } else {
                            rights = new String[]{dep[2]};
                        }

                        // 对每一个产生式
                        for (String item : rights) {
                            if (VT.contains(item.charAt(0))){
                                // X->a,....
                                result.add(item.charAt(0));

                            } else if (item.charAt(0) == '~') {
                                // X->~
                                result.add('~');

                            } else if (VN.contains(item.charAt(0))) {
                                // X->Y.... 和 X->Y1, Y2, Y3.....一起处理   注意， 我们假设这个时候的文法已经消除了左递归
                                int index = 0;
                                while (index < item.length()) {
                                    ArrayList<Character> oneFIRST = getOneFIRST(item.charAt(index) + "");
                                    if (oneFIRST.contains('~')) {
                                        // 包含空。
                                        for (Character ele : oneFIRST) {
                                            if (ele != '~') {
                                                result.add(ele);
                                            }
                                        }
                                        index ++;

                                    } else {
                                        // 不包含空， 就不能往下了
                                        for (Character ele : oneFIRST) {
                                            result.add(ele);

                                        }
                                        break;
                                    }
                                }
                                // 如果多有都遍历了, 说明都为空， 那么加入空
                                if (index == item.length()) {
                                    result.add('~');
                                }

                            }
                        }
                    }
                }
            }

        } else if (str.equals("~")) {
            result.add('~');

        } else {
            // str = X1X2X3.... 也就是求符号串的FIRST集合
            int index = 0;
            while (index < str.length()) {
                ArrayList<Character> oneFIRST = getOneFIRST(str.charAt(index) + "");
                if (oneFIRST.contains('~')) {
                    // 包含空
                    for (Character ele : oneFIRST) {
                        if (ele != '~') {
                            result.add(ele);
                        }
                        index ++;
                    }
                } else {
                    // 不包含空
                    for (Character ele : oneFIRST) {
                        result.add(ele);
                    }
                    break;
                }
            }
            // 如果多有都遍历了, 说明都为空， 那么加入空
            if (index == str.length()) {
                result.add('~');
            }

        }

        allFirst.put(str, result);
        return result;
    }

    /***
     * 求一个字符或者字符串的FOLLOW集合
     * @param str
     * @return
     */
    public ArrayList<Character> getOneFOLLOW(String str) {
        // 算过就直接返回
        if (allFollow.containsKey(str)) {
            return allFollow.get(str);
        }

        // 要返回的结果
        ArrayList<Character> result = new ArrayList<>();

        Character tempCh = str.charAt(0); // 转换成字符
        if (tempCh == 'S') {
            // 识别符号。
            result.add('$');

        } else {
            // 遍历所有产生式， 有两种情况
            // 1. A->xUy, 则 FIRST(y) - {~} ---> FOLLOW(U)
            // 2. A->xU 或者 A->xUy, y->~ , 则FOLLOW(A) ---> FOLLOW(U)
            //    y->~  则 FIRST(y).contains(~);
            for (String oneGe : program) {
                // D -> iBD 求Follow(D) 则不求， 这样会陷入死循环
                if (str.equals(oneGe.split(":")[0])) {
                    continue;
                }

                String right = oneGe.split(":")[2];
                String rights[];
                if (right.contains("|")) {
                    rights = right.split("\\|");
                } else {
                    rights = new String[]{right};
                }

                for (String one : rights) {
                    if (one.contains(str)) {
                        String temp[] = one.split(str);
                        // 1. A->xUy, 则 FIRST(y) - {~} ---> FOLLOW(U)
                        if (temp.length == 2) {
                            for (Character item : getOneFIRST(temp[1])) {
                                if (item != '~') {
                                    if (!result.contains(item))
                                        result.add(item);
                                }
                            }

                            if (getOneFIRST(temp[1]).contains('~')) {
                                // 如果 y -> ~ 则 Follow(A) ---> Follow(U)
                                for (Character item : getOneFOLLOW(oneGe.split(":")[0])) {
                                    if (!result.contains(item))
                                        result.add(item);
                                }
                            }

                        } else if (temp.length == 0 || temp.length == 1) {
                            // 2. A->xU
                            for (Character item : getOneFOLLOW(oneGe.split(":")[0])) {   // 这个地方一直栈溢出， 要做一些记忆化， 不能每次都算。 算过的就不算了
                                if (!result.contains(item))
                                    result.add(item);
                            }
                        }
                    }
                }
            }
        }

        allFollow.put(str, result);
        return result;
    }

    public void setProgram(ArrayList<String> program) {
        this.program = program;
        setVN();
        setVT();
        M = new String[VN.size()][VT.size()];
        allFirst = new HashMap<>();
        allFollow = new HashMap<>();
    }

    public void setVN() {
        ArrayList<Character> result = new ArrayList<>();

        for (int i=0; i<program.size(); i++) {
            for (int j=0; j<program.get(i).length(); j++) {
                Character curCh = program.get(i).charAt(j);  // 现在的字符

                // 如果是大写字母，就是非终结符, 并且不要重复的
                if ((int)curCh >= 65 && (int)curCh <= 89 && !result.contains(curCh)) {
                    result.add(curCh);
                }
            }
        }

        this.VN = result;
    }

    public void setVT() {
        ArrayList<Character> result = new ArrayList<>();

        for (int i=0; i<program.size(); i++) {
            for (int j=0; j<program.get(i).length(); j++) {
                Character curCh = program.get(i).charAt(j);  // 现在的字符

                if (((int)curCh < 65 || (int)curCh > 89) && curCh != ':' && curCh != '|' && curCh != '~') {
                    result.add(curCh);
                }
            }
        }

        result.add('$');
        this.VT = result;
    }

    public ArrayList<Character> getVN() {
        return VN;
    }

    public ArrayList<Character> getVT() {
        return VT;
    }

    public ArrayList<String> getProgram() {
        return program;
    }

    public String[][] getM() {
        return M;
    }

    /***
     * 算出分析表M
     */
    public void setM() {
        // 对每一条产生式做分析
        for (String item : program) {
            String depart[] = item.split("::");
            String rights[] = depart[1].split("\\|");

            // 每一条产生式，产生式右部可能有多个，对每一个做分析。
            // 求FIRST(a)
            int VNIndex = VN.indexOf(depart[0].charAt(0));
            for (String oneGeneration : rights) {
                for (Character ch : getOneFIRST(oneGeneration)) {
                    if (ch != '~') {
                        int VTIndex = VT.indexOf(ch);
                        M[VNIndex][VTIndex] = depart[0] + "::" + oneGeneration;
                    }
                }

                // ~ ---> FIRST(a)
                if (getOneFIRST(oneGeneration).contains('~')) {
                    for (Character ch : getOneFOLLOW(depart[0])) {
                        int VTIndex = VT.indexOf(ch);
                        M[VNIndex][VTIndex] = depart[0] + "::" + oneGeneration;
                    }
                }
            }
        }
    }


    /***
     * 算出M表之后，分析任何一个符号串
     * @param str
     */
    public void analyse(String str){
        // 根据书上的流程图
        // 初始化
        int j = 0;
        Stack<Character> S = new Stack<>();
        S.push('$');
        S.push('S');
        str = str + "$";
        Character ch = str.charAt(j);

        // 开始分析
        while (true) {

            System.out.print(S + "           " + str.substring(j) + "           ");

            Character top = S.peek();
            if (VT.contains(top)) {
                if (top.equals(ch)) {
                    if (top.equals('$')) {
                        System.out.println("success");
                        return;
                    } else {
                        S.pop();
                        j ++;
                        ch = str.charAt(j);
                        System.out.println();
                    }
                } else {
                    System.out.println("false");
                    return;
                }
            } else {
                int VNIndex = VN.indexOf(top);
                int VTIndex = VT.indexOf(ch);
                String ge = M[VNIndex][VTIndex];

                if (ge == null) {
                    System.out.println("false");
                    return;
                } else {
                    System.out.println(ge);
                    String right = ge.split("::")[1];
                    S.pop();
                    for (int i=right.length()-1; i>=0; i--){
                        if (right.charAt(i) != '~')
                            S.push(right.charAt(i));
                    }
                }

            }
        }
    }

}
