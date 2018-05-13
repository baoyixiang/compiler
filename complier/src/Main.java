import java.io.*;
import java.util.Iterator;
import java.util.Vector;

public class Main {

    public int ch;
    public int code;//保留字状态码
    public Vector<String> v = new Vector<String>();

    public StringBuffer strToken = new StringBuffer();//存放构成单词符号的字符串

    public String [] retainWord = new String[]{"int","if","else","return","main","void","while","break"};//保留字

    //判断是否是字母
    public boolean IsLetter(){
        if((ch>=65 && ch <= 90) || (ch >= 97 && ch <=122)){
            return true;
        }
        return false;
    }

    //判断是否是数字
    public boolean IsDigit(){
        if(ch>=48 && ch <= 57){
            return true;
        }
        return false;
    }

    //判断是否是空格
    public boolean IsBC(int ch){
        if(ch == 32){
            return true;
        }
        return false;
    }

    //连接字符
    public void Concat(char ch){
        strToken.append(ch);
    }
    //小数点
    public void Dot(char ch){
        strToken.append(ch);
    }
    //判断是否是保留字
    public int Reserve(){
        for(int i = 0;i < retainWord.length;i++){
            if(strToken.toString().equals(retainWord[i])){
                return 1;
            }
        }
        if(strToken.length() != 0){
            if(strToken.charAt(0)>='0' && strToken.charAt(0)<='9'){
                return 3;
            }
        }

        return 2;
    }

    //
    public void Retract(){
        code = Reserve();
        if(code == 1){
            System.out.println("("+1+","+strToken+")");
            v.add("("+1+", "+strToken+")");
        }else if(code == 2){
            System.out.println("("+2+", "+strToken+")");
            v.add("("+2+", "+strToken+")");
        }
        else if(code == 3){
            System.out.println("("+3+", "+strToken+")");
            v.add("("+3+", "+strToken+")");
        }
        strToken.delete(0, strToken.length());
    }


    //追加式写入文件
    public void WriteStringToFile(String filePath, String content) {
        try {
            FileWriter fw = new FileWriter(filePath, true);
            BufferedWriter bw = new BufferedWriter(fw);
            //追加文件
            bw.write(content);
            bw.newLine();
            bw.close();
            fw.close();
        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    /**
     * 读取文件
     */
    public void scanner(){
        BufferedReader br;
        try {
            br = new BufferedReader(new FileReader("src/resources/data.txt"));
            String s = null;

            while((ch = br.read()) != -1 && ch != '/'){
                //System.out.println("======="+(char)ch);
                if(IsBC(ch) == false){
                    if(IsLetter()){
                        if(IsLetter() == true || IsDigit() == true){
                            Concat((char) ch);
                        }
                    }else if(IsDigit() == true){
                        Concat((char)ch);
                    }else if(IsDigit()){
                        Concat((char) ch);
                    }else if(ch == 61){
                        if((strToken.length() != 0 )&& (strToken.charAt(0) == '=')){
                            strToken.append((char)ch);
                            System.out.println("("+4+","+strToken+")");
                            v.add("("+4+", "+strToken+")");
                            strToken.delete(0, strToken.length());
                        }else{
                            strToken.append((char)ch);
                        }
                    }else if(ch == 43){
                        Retract();
                        System.out.println("("+4+","+(char) ch+")");
                        v.add("("+4+","+(char) ch+")");
                    }else if(ch == 45){
                        Retract();
                        System.out.println("("+4+","+(char) ch+")");
                        v.add("("+4+","+(char) ch+")");
                    }else if(ch == 42){
                        Retract();
                        System.out.println("("+4+","+(char) ch+")");
                        v.add("("+4+","+(char) ch+")");
                    }else if(ch == 47){
                        Retract();
                        System.out.println("("+4+","+(char) ch+")");
                        v.add("("+4+","+(char) ch+")");
                    }else if((char) ch == ';'){
                        Retract();
                        System.out.println("("+5+","+(char) ch+")");
                        v.add("("+5+","+(char) ch+")");
                    }else if((char) ch == '('){
                        Retract();
                        System.out.println("("+5+","+(char) ch+")");
                        v.add("("+5+","+(char) ch+")");

                    }else if((char) ch == ')'){
                        Retract();
                        System.out.println("("+5+","+(char) ch+")");
                        v.add("("+5+","+(char) ch+")");
                    }else if((char) ch == '{'){
                        Retract();
                        System.out.println("("+5+","+(char) ch+")");
                        v.add("("+5+","+(char) ch+")");
                    }else if((char) ch == '}'){
                        Retract();
                        System.out.println("("+5+","+(char) ch+")");
                        v.add("("+5+","+(char) ch+")");
                    }else if((char) ch == ','){
                        Retract();
                        System.out.println("("+5+","+(char) ch+")");
                        v.add("("+5+","+(char) ch+")");
                    }

                }else{
                    Retract();
                }

            }
        } catch (FileNotFoundException e1) {
            // TODO Auto-generated catch block
            e1.printStackTrace();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        Main compile2 = new Main();
        compile2.scanner();
        String res = "";
        Iterator<String> iterator = compile2.v.iterator();
        while(iterator.hasNext()) {
            res+=iterator.next() + "\r\n";
            //System.out.println(iterator.next());
        }
        compile2.WriteStringToFile("src/resources/output.txt", res);

    }

}