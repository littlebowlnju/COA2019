package cpu.alu;

import transformer.Transformer;
import util.BinaryIntegers;
import util.IEEE754Float;

import java.util.Arrays;

/**
 * Arithmetic Logic Unit
 * ALU封装类
 * TODO: 加减乘除
 */
public class ALU {

    // 模拟寄存器中的进位标志位
    private String CF = "0";

    // 模拟寄存器中的溢出标志位
    private String OF = "0";

    static Transformer t=new Transformer();


    /**
     * 返回两个二进制整数的除法结果 operand1 ÷ operand2
     * @param operand1 32-bits
     * @param operand2 32-bits
     * @return 65-bits overflow + quotient + remainder
     */
    public static String div(String operand1, String operand2){
        // TODO

        int num=operand1.length();
        String ZERO="";
        for(int i=0;i<num;i++){
            ZERO=ZERO+"0";
        }
        //判断除数和被除数是否为0
        if(operand1.equals("10000000000000000000000000000000")&&operand2.equals( "11111111111111111111111111111111"))//唯一一种会溢出的情况：最大的负数处以-1
            return "11000000000000000000000000000000000000000000000000000000000000000";
        if(operand1.equals(ZERO)&&!operand2.equals(ZERO)){
            return "0"+ZERO+ZERO;
        }else if(operand2.equals(ZERO)&&!operand1.equals(ZERO)){
            throw new ArithmeticException();
        }else if(operand1.equals(ZERO)&&operand2.equals(ZERO)){
            return BinaryIntegers.NaN;
        }else{
            String re="";
            if(operand1.length()==32){
                for(int i=0;i<num;i++){
                    re=re+(operand1.charAt(0)=='0'?"0":"1");
                }re=re+operand1;
                String temp=re;
                for(int i=0;i<num;i++){
                    temp=shlInDiv(temp);//先左移一位
//                    System.out.println(i+" temp after shl:"+temp+" "+temp.length());
                    if(temp.charAt(0)!=operand2.charAt(0)){//此处比较的是每次的余数和除数的符号
                        String temp1=add(temp.substring(0,num),operand2)+temp.substring(num);
//                        System.out.println("After add:"+temp1);
                        if(temp1.charAt(0)!=temp.charAt(0)){//不够大，要恢复余数
                            if(temp1.substring(0,num).equals(ZERO)){//减到0其实是够的，因此商也要上1
                                temp=temp1.substring(0,temp.length())+"1";
                            }else{//真的不够的，恢复余数并且商上0
                                temp=temp+"0";}
                        }else
                            temp=temp1.substring(0,temp.length())+"1";
                    }else {
//                        System.out.println(sub(operand2, temp.substring(0, num)));
                        String temp2 = sub(operand2, temp.substring(0, num)) + temp.substring(num);
//                        System.out.println("After sub:"+temp2);
                        if (temp2.charAt(0) != temp.charAt(0)) {//不够大，要恢复余数
                            if (temp2.substring(0, num).equals(ZERO)) {
                                temp = temp2.substring(0, temp.length()) + "1";
                            } else {
                                temp = temp + "0";
                            }
                        } else {
                            temp = temp2.substring(0, temp.length()) + "1";
                        }
                    }
//                    System.out.println(i+" temp after all:"+temp);
                }
                if(operand1.charAt(0)!=operand2.charAt(0)){
                    temp=temp.substring(0,num)+getComplement(temp.substring(num));
                }return "0"+temp.substring(num)+temp.substring(0,num);
            } else{//浮点数尾数除法的情况；
                re=operand1;
                for(int i=0;i<operand1.length();i++){
                    re=re+"0";
                }//Extend the dividend
//                System.out.println("re:"+re+" "+re.length());
//            System.out.println("divisor:"+operand2+" "+operand2.length());
                String temp=re;
                for(int i=0;i<num;i++){
                    if(toInteger(temp.substring(0, num))>=toInteger(operand2)){//判断可不可以减
                        String temp2 = sub(operand2, temp.substring(0, num)) + temp.substring(num);
//                        System.out.println(sub(operand2, temp.substring(0, num)) );
//                        System.out.println("After sub:"+temp2);
                        temp = temp2+ "1";
                    }else{
                        temp = temp + "0";
                    }
                    temp=shlInDiv(temp);//后左移一位
//                    System.out.println(i+" temp after all:"+temp);
                }return temp.substring(num);
            }
        }
    }


    public static String getComplement(String x){
        //取得补码
        String re="";
        for(int i=0;i<x.length()-1;i++){
            re=re+"0";
        }re=re+"1";
        Transformer t=new Transformer();
        x=t.negation(x);
        re=add(x,re);
        return re;
    }

    static String add(String src, String dest) {
        //Full Adder
        String temp=null;
        if(src.length()<dest.length()){
            temp=src;
            for(int i=0;i<dest.length()-src.length();i++){
                temp=temp+"0";
            }src=temp;
        }else if(src.length()>dest.length()){
            temp=dest;
            for(int i=0;i<src.length()-dest.length();i++){
                temp=temp+"0";
            }dest=src;
        }
        String Ci="0";
        String Si="0";
        String[] Xs=src.split("");
        String[] Ys=dest.split("");
        StringBuilder res=new StringBuilder();
        for(int i=src.length()-1;i>=0;i--){
            Si=xor((xor(Xs[i],Ys[i])),Ci);
            res.append(Si);
            Ci=or(or(and(Xs[i],Ci),and(Ys[i],Ci)),and(Xs[i],Ys[i]));
        }res=res.reverse();
        if(Xs[0].equals(Ys[0])&&!Xs[0].equals(res.substring(0,1))){
            System.out.println("overflow");
            return res.substring(0);
        }else
            return res.toString();
    }

    static String sub(String src, String dest) {
        //dest-src;
        String reals=getComplement(src);
        return add(dest,reals);
    }

    public static String and(String src, String dest) {
        // TODO
        StringBuilder result=new StringBuilder();
        for(int i=0;i<src.length();i++){
            if(src.charAt(i)==dest.charAt(i)&&src.charAt(i)=='1'){
                result.append('1');
            }else
                result.append('0');
        }
        return result.toString();
    }

    public static String or(String src, String dest) {
        // TODO
        StringBuilder result=new StringBuilder();
        for(int i=0;i<src.length();i++){
            if(src.charAt(i)=='1'||dest.charAt(i)=='1')
                result.append('1');
            else
                result.append('0');
        }
        return result.toString();
    }

    public static String xor(String src, String dest) {
        // TODO
        StringBuilder result=new StringBuilder();
        for(int i=0;i<src.length();i++){
            if(src.charAt(i)==dest.charAt(i))
                result.append('0');
            else
                result.append('1');
        }
        return result.toString();
    }

    static String sar(String src, String dest) {
        // TODO
        int digits=toInteger(src);
        if(digits>31)
            return "00000000000000000000000000000000";
        StringBuilder result=new StringBuilder();
        for(int i=0;i<digits;i++){
            if(dest.charAt(0)=='0')
                result.append('0');
            else
                result.append('1');
        }
        result.append(dest);
        return result.toString();
    }

    public static String shr(String src, String dest) {
        // TODO
        int digits=toInteger(src);
        StringBuilder result=new StringBuilder();
        if(digits>=dest.length()){
            return "00000000000000000000000000000000";
        }
        else{
            for(int i=0;i<digits;i++)
                result.append('0');
            result.append(dest.substring(0,dest.length()-digits));
        }return result.toString();
    }

    public static int toInteger(String binay){ //把一个二进制字符串转为无符号整数
        String[] bin=binay.split("");
        int res=0;
        for(int i=0;i<bin.length;i++){
            if(bin[bin.length-1-i].equals("1"))
                res+=Math.pow(2,i);
        }
        return res;
    }

    public static String shlInDiv(String s){
        //恢复余数的除法中每次左移一位只要第一位舍掉即可
        return s.substring(1);
    }

    public static void main(String[] args){
        Transformer t = new Transformer();
        String quotient = t.intToBinary("-4");
        String reminder = t.intToBinary("0");
        System.out.println(div(t.intToBinary("-8"), t.intToBinary("2")));
        System.out.println("0"+quotient+reminder);
    }
}
