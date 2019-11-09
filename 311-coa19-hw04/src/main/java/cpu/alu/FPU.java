package cpu.alu;

import transformer.Transformer;
import util.IEEE754Float;

/**
 * floating point unit
 * 执行浮点运算的抽象单元
 * 浮点数精度：使用4位保护位进行计算，计算完毕直接舍去保护位
 * TODO: 浮点数运算
 */
public class FPU {

    static Transformer transformer=new Transformer();
    /**
     * compute the float mul of a / b
     */
    static String div(String a, String b) {
        // TODO
        //判断特殊情况
        if(b.equals(IEEE754Float.N_ZERO)||b.equals(IEEE754Float.P_ZERO)){
            if(!a.equals(IEEE754Float.N_ZERO)&&!a.equals(IEEE754Float.P_ZERO)){
                throw new ArithmeticException();}
            else{
                return IEEE754Float.NaN;
            }
        }else if(a.equals(IEEE754Float.P_ZERO)){return IEEE754Float.P_ZERO;}
        else if(a.equals(IEEE754Float.N_ZERO)){return IEEE754Float.N_ZERO;}
        else if(a.equals(IEEE754Float.P_INF)||b.equals(IEEE754Float.N_INF)){
            return a.charAt(0)==b.charAt(0)?IEEE754Float.P_INF:IEEE754Float.N_INF;
        }else{
            //确定符号
            String signR=null;
            if(a.charAt(0)==b.charAt(0))
                signR="0";
            else
                signR="1";
            //sub the exponents
            int exponentA=Integer.parseInt(transformer.binaryToInt("0"+a.substring(1,9)));//加0转成正数
            int exponentB=Integer.parseInt(transformer.binaryToInt("0"+b.substring(1,9)));
            int expoR=exponentA-exponentB+127;//add bias(+127)
            System.out.println("expoR first:"+expoR);
            if(expoR>255)
                return "overflow";  //check if exponent overflow
            if(expoR<=0)
                return "underflow";    //check if exponent underflow
            //divide the significands
            String sigA="1"+a.substring(9);
//            System.out.println("sigA:"+sigA);
            String sigB="1"+b.substring(9);
//            System.out.println("sigB:"+sigB);
            String sigR=binaryDiv(sigA,sigB);
//            System.out.println("sigR first:"+sigR);
            //normalize
            int temp=0;
            for(int i=0;i<sigR.length();i++){
                if(sigR.charAt(i)=='1'){
                    temp=i;
                    break;
                }
            }
//            System.out.println("temp:"+temp);
            expoR=expoR-temp;
//            System.out.println("expoR after:"+expoR);
            String exponentR=Integer.toBinaryString(expoR);
            for(int i=0;i<8-exponentR.length();i++){
                exponentR="0"+exponentR;
            }
//            System.out.println("exponentR:"+exponentR);
            //round
            if(sigR.length()>temp+24) {
                sigR = sigR.substring(temp + 1, temp + 24);
            }else{
                sigR=sigR.substring(temp+1);
                String tempStr=sigR;
                for(int i=0;i<23-sigR.length();i++){
                    tempStr=tempStr+"0";
                }sigR=tempStr;
            }
//            System.out.println("sigR after:"+sigR);
            return signR+exponentR+sigR;
        }

    }

    static String binaryDiv(String operand1, String operand2){
        ALU alu=new ALU();
        System.out.println(alu.div(operand1,operand2));
        return alu.div(operand1,operand2);
    }

    public static void main(String[] args){
        String dividend = transformer.floatToBinary( "0.4375" );
        String divisor = transformer.floatToBinary( "0.5" );
        System.out.println(div(dividend,divisor));
        System.out.println(transformer.floatToBinary( "0.875" ));
    }

}
