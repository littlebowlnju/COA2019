package cpu.present.transformers;

import cpu.present.Number;
import cpu.present.PresentType;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Collections;

public class TransformerFactory {

	/**
     * calculate the Binary Code {@link Transformer#code} from possible data-presentation
     * If raw data-type is Integer, the Binary Code should be a Two-Complement Code or a 8421BCD code
     * Else if raw data-type is Float, the Binary Code should be a True-Value Presentation(IEEE 754, default 32 bits with 8-bits length exponents and 23-bits length fraction)
     **/
    public static Transformer getTransformer(PresentType rawType, PresentType storageType, String... args) {
        String code = args[0];
        int eLength;
        int sLength;

        if (args.length > 1) {
            eLength = Integer.parseInt(args[1]);
            sLength = Integer.parseInt(args[2]);
        } else {
            eLength = 0;
            sLength = Number.DATA_SIZE_LIMITATION;
        }

        if (rawType.equals(PresentType.BCD.W8421)) {
            // nothing to do
        } else if (rawType.equals(PresentType.BIN.TWOS_COMPLEMENT)) {
            // nothing to do
        } else if (rawType.equals(PresentType.DEC.INTEGER)) {
            int inte=Integer.parseInt(code);
            if (storageType.equals(PresentType.BCD.W8421)) {
                // TODO
                String[] transcode=new String[32];
                transcode[0]="1"; transcode[1]="1";
                if(inte<0)
                    transcode[3]="1";
                while(inte!=0){
                    int x=inte%10;
                    int radix=31;
                    for(int i=0;i<4;i++){
                        if(x%2==0)
                            transcode[i--]="0";
                        else
                            transcode[i--]="1";
                        x/=2;
                    }inte/=10;
                }
                for(int i=0;i<32;i++){
                    if (transcode[i]!=null)
                        transcode[i]="0";
                }StringBuffer str=new StringBuffer();
                for(String s:transcode)
                    str.append(s);
                code=str.toString();
            } else if(storageType.equals(PresentType.BIN.TWOS_COMPLEMENT)) {
                // TODO
                char[] chs=new char[32];
                for(int i=0;i<32;i++){
                    chs[31-i]=(char)((inte>>i&1)+'0');
                }code=new String(chs);
            }
        } else if (rawType.equals(PresentType.DEC.FLOAT)) {
            // TODO from DEC_FLOAT to IEEE754_BIN_FLOAT

        } else if (rawType.equals(PresentType.BIN.FLOAT)) {
            // nothing to do
        }

        return new Transformer(storageType, code, String.valueOf(eLength), String.valueOf(sLength));
    }

    

}