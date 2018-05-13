import javafx.util.Pair;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Stack;

public class SignPriority {

    // 基本属性
    private HashMap<Character, String> pro;   // 相比于LL1， 换了一种数据结构来弄
    private ArrayList<Character> VN;
    private ArrayList<Character> VT;

    private HashMap<Character, ArrayList<Character>> FIRSTVT;
    private HashMap<Character, ArrayList<Character>> LASTVT;
    private Character signRelationship[][];  // 算符优先关系矩阵

    /***
     * 得到VN和VT
     */
    private void setVN_VT() {
        // 遍历语法的左边
        for (Character item : pro.keySet()) {
            // 假设左边都属于VN
            if (!VN.contains(item))
                VN.add(item);
        }
        // 遍历语法的右边
        for (String item : pro.values()) {
            for (int i=0; i<item.length(); i++) {

                Character curCh = item.charAt(i);
                if (curCh >= 'A' && curCh <='Z' && !VN.contains(curCh))
                    VN.add(curCh);
                else if ((curCh < 'A' || curCh > 'Z') && curCh != '|' && !VT.contains(curCh))
                    VT.add(curCh);
            }
        }
    }

    /***
     * 得到FIRSTVT集合
     */
    private void setFIRSTVT() {
        // 初始化FIRSTVT
        FIRSTVT = new HashMap<>();
        for (Character vn : VN) {
            FIRSTVT.put(vn, new ArrayList<>());
        }

        Stack<Pair<Character, Character> > S = new Stack<>();
        // 遍历语法，初始化栈
        for (Character item: pro.keySet()) {
            String geRight[] = pro.get(item).split("\\|");
            for (int i = 0; i < geRight.length; i++) {
                // 如果符合 U --> b... 或者 U ---> Vb.... 则入栈
                if (VT.contains(geRight[i].charAt(0))) {
                    // U --> b...
                    Pair<Character, Character> temp = new Pair<>(item, geRight[i].charAt(0));
                    S.push(temp);
                } else if (VN.contains(geRight[i].charAt(0)) && geRight[i].length() >1 && VT.contains(geRight[i].charAt(1))){
                    // U --> Vb...
                    Pair<Character, Character> temp = new Pair<>(item, geRight[i].charAt(1));
                    S.push(temp);
                }
            }
        }

        // 然后进行出栈入栈的分析
        while (!S.empty()) {
            Pair<Character, Character> cur = S.peek();
            // 出栈的时候要先更新FIRSTVT
            FIRSTVT.get(cur.getKey()).add(cur.getValue());
            S.pop();

            for (Character item: pro.keySet()) {
                String geRight[] = pro.get(item).split("\\|");
                for (int i = 0; i < geRight.length; i++) {
                    if (geRight[i].equals(cur.getKey().toString())) {
                        // 找到一个可以入栈的
                        Pair<Character, Character> in = new Pair<>(item, cur.getValue());
                        S.push(in);
                    }
                }
            }
        }
    }

    /**
     * 得到LASTVT集合
     */
    private void setLASTVT() {
        // 求LAST跟求FIRST的方法相差无几

        // 初始化FIRSTVT
        LASTVT = new HashMap<>();
        for (Character vn : VN) {
            LASTVT.put(vn, new ArrayList<>());
        }

        Stack<Pair<Character, Character> > S = new Stack<>();
        // 遍历语法，初始化栈
        for (Character item: pro.keySet()) {
            String geRight[] = pro.get(item).split("\\|");
            for (int i = 0; i < geRight.length; i++) {
                // 如果符合 U --> ...a 或者 U ---> ...aV 则入栈
                if (VT.contains(geRight[i].charAt(geRight[i].length()-1))) {
                    // U --> ...a
                    Pair<Character, Character> temp = new Pair<>(item, geRight[i].charAt(geRight[i].length()-1));
                    S.push(temp);
                } else if (VN.contains(geRight[i].charAt(geRight[i].length()-1)) && geRight[i].length() >1 && VT.contains(geRight[i].charAt(geRight[i].length()-2))){
                    // U --> Vb...
                    Pair<Character, Character> temp = new Pair<>(item, geRight[i].charAt(geRight[i].length()-2));
                    S.push(temp);
                }
            }
        }

        // 然后进行出栈入栈的分析
        while (!S.empty()) {
            Pair<Character, Character> cur = S.peek();
            // 出栈的时候要先更新FIRSTVT
            LASTVT.get(cur.getKey()).add(cur.getValue());
            S.pop();

            for (Character item: pro.keySet()) {
                String geRight[] = pro.get(item).split("\\|");
                for (int i = 0; i < geRight.length; i++) {
                    if (geRight[i].equals(cur.getKey().toString())) {
                        // 找到一个可以入栈的
                        Pair<Character, Character> in = new Pair<>(item, cur.getValue());
                        S.push(in);
                    }
                }
            }
        }
    }

    /**
     * 得到算符优先关系矩阵
     */
    private void setSignRelationship() {
        // 还是要遍历所有的产生式
        // 初始化
        signRelationship = new Character[VT.size()][VT.size()];
        // 把数组初始化为 'X'
        for (int i = 0; i < VT.size(); i++) {
            for (int j = 0; j < VT.size(); j++) {
                signRelationship[i][j] = 'X';
            }
        }

        for (Character key : pro.keySet()) {
            String geRight[] = pro.get(key).split("\\|");
            // 遍历其中一个产生式所有的右部
            for (int i = 0; i < geRight.length; i++) {
                String oneStr = geRight[i];
                for (int j = 0; j < oneStr.length()-1; j++) {
                    // 看=关系
                    if (VT.contains(oneStr.charAt(j)) && VT.contains(oneStr.charAt(j+1))) {
                        signRelationship[VT.indexOf(oneStr.charAt(j))][VT.indexOf(oneStr.charAt(j+1))] = '=';
                    }
                    if (VT.contains(oneStr.charAt(j)) && VN.contains(oneStr.charAt(j+1))) {
                        // < 关系
                        for (Character item : FIRSTVT.get(oneStr.charAt(j+1))) {
                            signRelationship[VT.indexOf(oneStr.charAt(j))][VT.indexOf(item)] = '<';
                        }

                        // 这里还有一种 = 关系
                        if (j+2 < oneStr.length() && VT.contains(oneStr.charAt(j+2))) {
                            signRelationship[VT.indexOf(oneStr.charAt(j))][VT.indexOf(oneStr.charAt(j+2))] = '=';
                        }
                    }
                    // > 关系
                    if (VN.contains(oneStr.charAt(j)) && VT.contains(oneStr.charAt(j+1))) {
                        for (Character item : LASTVT.get(oneStr.charAt(j))) {
                            signRelationship[VT.indexOf(item)][VT.indexOf(oneStr.charAt(j+1))] = '>';
                        }
                    }
                }
            }
        }
    }

    // 以上完成了所有准备工作，接下来就可以分析了

    /**
     * 分析一个字符串是否符合这个语法
     * @param word
     */
    public void analysis(String word) {
        // 每次找最左素短语来归约

        // 先初始化
        Stack<Character> S = new Stack<>();
        S.push('$');
        String words = word + '$';
        int curWordIndex = 0;  // 指向现在字符的指针
        while (true) {
            if (words.charAt(curWordIndex) == '$') {
                if (S.size() == 2) {
                    if (S.peek() == 'V') {
                        // 成功
                        System.out.println(S.toString() + "        " + "OK" + "        " + words.substring(curWordIndex));
                        return;
                    } else {
                        System.out.println("FALSE");
                        return;
                    }
                }
            }
            if (S.peek() == '$') {
                // 第一次， 直接移进
                System.out.println("[$]" + "        " + "<" + "        " + words);
                S.push(words.charAt(curWordIndex));
                curWordIndex ++;
            } else {
                System.out.print(S.toString() + "        ");
                // 找第一个非终结符
                Character tempVN = null; // 如果栈顶不是VT 需要存储一下
                if (S.peek() == 'V') {
                    tempVN = S.peek();
                    S.pop();
                }
                // 然后找素短语
                Character tempVT = S.peek();
                if (tempVT == '$') {
                    System.out.println("<" + "        " + words.substring(curWordIndex));
                    if (tempVN != null) S.push(tempVN);  // 把V存回去
                    S.push(words.charAt(curWordIndex));
                    curWordIndex ++;
                    continue;
                }
                if (words.charAt(curWordIndex) == '$' || signRelationship[VT.indexOf(tempVT)][VT.indexOf(words.charAt(curWordIndex))] == '>') {
                    // 可以找了, 每次遇到非终结符要跳过，  要找到第一个 < 关系     也就是    <.....>  中间的串就是素短语
                    System.out.print(">" + "        " + words.substring(curWordIndex) + "        ");
                    String curSu;
                    curSu = (tempVN == null ? "" : "" + tempVN);
                    Character P = null;
                    while (true) {
                        if (S.peek() == 'V') {
                            // 跳过非终结符
                            curSu = S.peek() + curSu;
                            S.pop();
                        } else {
                            // 终结符
                            if (P == null) {
                                // 第一个，不用比较
                                P = S.peek();
                                curSu = S.peek() + curSu;
                                S.pop();
                            } else {
                                if (S.peek() == '$' || signRelationship[VT.indexOf(S.peek())][VT.indexOf(P)] == '<') {
                                    // 找到了首元素了, 归约
                                    boolean isFind = false;
                                    for (Character keyset : pro.keySet()) {
                                        String geRight[] = pro.get(keyset).split("\\|");
                                        for (int i = 0; i < geRight.length; i++) {
                                            // 把要比较的产生式右部的VN换成'V', 再来做比较
                                            String tempRight = geRight[i];
                                            for (int j = 0; j < tempRight.length(); j++) {
                                                if (VN.contains(tempRight.charAt(j))) {
                                                    tempRight = tempRight.replace(tempRight.charAt(j), 'V');
                                                }
                                            }
                                            if (tempRight.equals(curSu)) {
                                                // 归约
                                                System.out.println(curSu);
                                                S.push('V');
                                                isFind = true;
                                                break;
                                            }
                                        }
                                    }
                                    if (isFind == false) {
                                        System.out.println("FALSE");
                                        return;
                                    }
                                    break;

                                } else {
                                    // 没找到，继续找
                                    P = S.peek();
                                    curSu = S.peek() + curSu;
                                    S.pop();
                                }
                            }
                        }
                    }

                } else {
                    // 不符合找的条件，移进
                    if (tempVN != null) S.push(tempVN);
                    System.out.println("<" + "        " + words.substring(curWordIndex));
                    S.push(words.charAt(curWordIndex));
                    curWordIndex ++;
                }
            }
        }
    }

    public void setPro(HashMap<Character, String> pro) {
        this.pro = pro;
        VN = new ArrayList<>();
        VT = new ArrayList<>();
        setVN_VT();
        setFIRSTVT();
        setLASTVT();
        setSignRelationship();
    }

    //region 所有的get方法
    public ArrayList<Character> getVN() {
        return VN;
    }

    public ArrayList<Character> getVT() {
        return VT;
    }

    public HashMap<Character, ArrayList<Character>> getFIRSTVT() {
        return FIRSTVT;
    }

    public HashMap<Character, ArrayList<Character>> getLASTVT() {
        return LASTVT;
    }

    public Character[][] getSignRelationship() {
        return signRelationship;
    }

    //endregion
}
