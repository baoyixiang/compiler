import java.util.ArrayList;
import java.util.HashMap;
import java.util.Stack;

public class program {

    // 基本属性
    private HashMap<Character, String> pro;   // 相比于LL1， 换了一种数据结构来弄
    private ArrayList<Character> VN;
    private ArrayList<Character> VT;
    private ArrayList<Character> VNVT;

    // 简单优先分析中所用的
    private int FIRST[][];
    private int LAST[][];
    private int FIRSTPlus[][];   // FIRST 的传递闭包
    private int LASTPlus[][];    // LAST 的传递闭包
    private int FIRSTPlus_Self[][]; // FIRST 的自反传递闭包
    private int LASTPlus_Trans[][]; // LAST 的传递闭包的转置矩阵
    // 形式化构造方法
    private int equal[][];
    private int less[][];
    private int greater[][];
    private Character finalRelationship[][];



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

        // 把VN VT组合起来，方便表示后面的很多矩阵
        VNVT.addAll(VN);
        VNVT.addAll(VT);
    }

    /***
     * 得到文法的FIRST矩阵和LAST矩阵
     */
    private void setFIRST_LAST() {
        FIRST = new int[VNVT.size()][VNVT.size()];
        LAST = new int[VNVT.size()][VNVT.size()];
        for (Character item: pro.keySet()) {
            // item 表示产生式的左部， 我们通过找其右部来组建FIRST和LAST
            // 右部的首字符为FIRST， 尾字符为LAST
            String GeRight = pro.get(item);
            String all[] = GeRight.split("\\|");
            for (int i=0; i<all.length; i++) {
                Character firstCh = all[i].charAt(0);
                Character lastCh = all[i].charAt(all[i].length()-1);
                // 更新矩阵
                FIRST[VNVT.indexOf(item)][VNVT.indexOf(firstCh)] = 1;
                LAST[VNVT.indexOf(item)][VNVT.indexOf(lastCh)] = 1;
            }
        }

    }

    /***
     * 得到文法的FIRST+和LAST+矩阵
     * 由Warshall算法求得，  这个算法求的东西跟关系里面的传递闭包很类似
     */
    private void setFIRSTPlus_LASTPlus() {

        // 先初始化
        FIRSTPlus = FIRST;
        LASTPlus = LAST;

        for (int line = 0; line < VNVT.size(); line++) {
            for (int row = 0; row < VNVT.size(); row++) {

                if (FIRSTPlus[row][line] == 1) {
                    // 如果第 FIRST[row][line] = 1, 并且FIRST[line][?] 也等于1  那么FIRST[row][?] = 1
                    for (int tempLine = 0; tempLine < VNVT.size(); tempLine++) {
                        if (FIRSTPlus[line][tempLine] == 1) {
                            FIRSTPlus[row][tempLine] = 1;
                        }
                    }
                }
                // 对于LAST矩阵也一样
                if (LASTPlus[row][line] == 1) {
                    // 如果第 FIRST[row][line] = 1, 并且FIRST[line][?] 也等于1  那么FIRST[row][?] = 1
                    for (int tempLine = 0; tempLine < VNVT.size(); tempLine++) {
                        if (LASTPlus[line][tempLine] == 1) {
                            LASTPlus[row][tempLine] = 1;
                        }
                    }
                }

            }
        }

        // 在这也可以把自反传递闭包求出来
        FIRSTPlus_Self = FIRSTPlus;
        for (int i = 0; i < VNVT.size(); i++) {
            FIRSTPlus_Self[i][i] = 1;
        }

        // 再求Last+的转置矩阵
        LASTPlus_Trans = new int[VNVT.size()][VNVT.size()];
        for (int i = 0; i < VNVT.size(); i++) {
            for (int j = 0; j < VNVT.size(); j++) {
                LASTPlus_Trans[i][j] = LASTPlus[j][i];
            }
        }
    }

    /***
     * 根据文法可以直接求得这个=矩阵，主要是因为不涉及到间接推导，比较简单
     */
    private void setEqual() {
        equal = new int[VNVT.size()][VNVT.size()];
        // 遍历所有的产生式右部
        for (String item : pro.values()) {
            
            String all[] = item.split("\\|");
            for (int i = 0; i < all.length; i++) {
                // 对右部的每一个String 进行遍历
                for (int j = 0; j < all[i].length()-1; j++) {
                    equal[VNVT.indexOf(all[i].charAt(j))][VNVT.indexOf(all[i].charAt(j+1))] = 1;
                }
            }
        }
    }

    /***
     * 根据 =矩阵 和FIRST、LAST矩阵求出 <矩阵和 >矩阵
     */
    private void setLess_Greater() {
        less = multiMatrix(equal, FIRSTPlus_Self);
        greater = multiMatrix(multiMatrix(LASTPlus_Trans, equal), FIRSTPlus_Self);
    }

    /***
     * 得到最终的 简单优先关系矩阵了。
     */
    private void setFinalRelationship() {
        finalRelationship = new Character[VNVT.size()][VNVT.size()];
        // 遍历=、>和<矩阵来构建这个矩阵， 这三个矩阵的相同位置不可能同时为1， 两个符号之间的关系只可能由一种，看是哪一种
        for (int i = 0; i < VNVT.size(); i++) {
            for (int j = 0; j < VNVT.size(); j++) {
                if (equal[i][j] == 1) {

                    finalRelationship[i][j] = '=';

                } else if (less[i][j] == 1){

                    finalRelationship[i][j] = '<';

                } else if (greater[i][j] == 1) {

                    finalRelationship[i][j] = '>';

                } else {
                    finalRelationship[i][j] = 'X';
                }
            }
        }
    }

    /***
     * 以上准备工作全部做完，之后就可以做分析了。
     * @param word
     * @return
     */
    public String analysis(String word) {
        Stack<Character> S = new Stack<>();
        S.push('$');
        word = word + "$";
        // 先题条件:  $ < word里所有的字符， 并且栈顶元素 > $;

        int word_curIndex = 0; // 指向字符串的现在字符的指针。

        // 开始分析
        while (true) {
            // 结束标志
            if (S.peek() == '$' && word.charAt(word_curIndex) == '$') {
                System.out.println(S.toString() + "        " + "OK" + "        " + word.substring(word_curIndex));
                return "OK";
            }

            // 如果=="$" , 是第一步
            if (S.peek() == '$' ) {
                System.out.println(S.toString() + "        " + "<" + "        " + word.substring(word_curIndex));
                S.push(word.charAt(word_curIndex));
                word_curIndex ++;
            } else {
                // 这两个之间没有关系
                if (word.charAt(word_curIndex) != '$' && finalRelationship[VNVT.indexOf(S.peek())][VNVT.indexOf(word.charAt(word_curIndex))] == 'X') {
                    System.out.println(S.toString() + "        " + "X" + "        " + word.substring(word_curIndex));
                    return "ERROR";
                } else {
                    // 有关系, 看是什么关系了。
                    if (word.charAt(word_curIndex) == '$' || finalRelationship[VNVT.indexOf(S.peek())][VNVT.indexOf(word.charAt(word_curIndex))] == '>') {
                        System.out.print(S.toString() + "        " + ">" + "        " + word.substring(word_curIndex) + "        ");
                        // 找到一个句柄
                        String tempStr = S.peek().toString();
                        Character top = S.peek();
                        // 结束，并且成功找到
                        if (top == 'S' && word.charAt(word_curIndex) == '$')
                        {
                            System.out.println("OK");
                            return "OK";
                        }
                        S.pop();
                        while (S.peek() != '$' && finalRelationship[VNVT.indexOf(S.peek())][VNVT.indexOf(top)] != '<') {
                            top = S.peek();
                            tempStr = top + tempStr;
                            S.pop();
                        }
                        // 用这个句柄查找产生式表 看对应的哪个产生式
                        boolean flag = false;
                        for (Character item : pro.keySet()) {
                            String right[] = pro.get(item).split("\\|");
                            for (int i = 0; i < right.length; i++) {
                                if (right[i].equals(tempStr)) {
                                    // 匹配到了一个关系式, 就归约
                                    System.out.println(item + "->" + tempStr);

                                    flag = true;
                                    S.push(item);
                                    break;
                                }
                            }
                        }
                        if (flag == false) {

                            return "ERROR";
                        }

                    } else {
                        Character relationship = finalRelationship[VNVT.indexOf(S.peek())][VNVT.indexOf(word.charAt(word_curIndex))];
                        System.out.println(S.toString() + "        " + relationship + "        " + word.substring(word_curIndex));
                        S.push(word.charAt(word_curIndex));
                        word_curIndex ++;
                    }
                }
            }
        }

    }

    // 提出来的一个小方法， 计算两个矩阵的乘积， 跟上面的步骤不挂钩
    private int[][] multiMatrix(int[][] ma1, int[][] ma2) {
        int result[][] = new int[VNVT.size()][VNVT.size()];
        for (int i=0; i<VNVT.size(); i++) {
            for (int j = 0; j < VNVT.size(); j++) {

                int temp = 0;
                for (int k = 0; k < VNVT.size(); k++) {
                    temp += ma1[i][k] * ma2[k][j];
                }
                result[i][j] = temp;
            }
        }
        return result;
    }


    public void setProgram(HashMap<Character, String> program) {
        this.pro = program;
        VN = new ArrayList<>();
        VT = new ArrayList<>();
        VNVT = new ArrayList<>();
        setVN_VT();
        setFIRST_LAST();
        setFIRSTPlus_LASTPlus();
        setEqual();
        setLess_Greater();
        setFinalRelationship();
    }


    //region 所有的get函数，向外提供接口
    public ArrayList<Character> getVT() {
        return VT;
    }

    public ArrayList<Character> getVN() {
        return VN;
    }

    public ArrayList<Character> getVNVT() {
        return VNVT;
    }

    public int[][] getFIRST() {
        return FIRST;
    }

    public int[][] getLAST() {
        return LAST;
    }

    public int[][] getFIRSTPlus() {
        return FIRSTPlus;
    }

    public int[][] getFIRSTPlus_Self() {
        return FIRSTPlus_Self;
    }

    public int[][] getLASTPlus() {
        return LASTPlus;
    }

    public int[][] getEqual() {
        return equal;
    }

    public int[][] getLess() {
        return less;
    }

    public int[][] getGreater() {
        return greater;
    }

    public int[][] getLASTPlus_Trans() {
        return LASTPlus_Trans;
    }

    public Character[][] getFinalRelationship() {
        return finalRelationship;
    }

    //endregion
}
