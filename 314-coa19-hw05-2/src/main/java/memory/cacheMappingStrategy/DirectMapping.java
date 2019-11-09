package memory.cacheMappingStrategy;

import memory.Cache;
import memory.Memory;
import transformer.Transformer;

import java.util.Arrays;

/**
 * 直接映射 12位标记 + 10位块号 + 10位块内地址
 */
public class DirectMapping extends MappingStrategy{

    /**
     * @param blockNO 内存数据块的块号
     * @return cache数据块号 22-bits  [前12位有效]
     */
    @Override
    public char[] getTag(int blockNO) {
        // TODO
        Transformer t=new Transformer();
        String res=t.intToBinary(String.valueOf(blockNO)).substring(10,22);//transformer将blockNo转成32位二进制表示，需要的是后22位，因此取得是后22位得前12位作为tag
        res=res+"0000000000";
        return res.toCharArray();
    }


    /**
     * 根据内存地址找到对应的行是否命中，直接映射不需要用到替换策略
     * @param blockNO
     * @return -1 表示未命中
     */
    @Override
    public int map(int blockNO) {
        // TODO
        Cache cache=Cache.getCache();
        int lineNum=blockNO%(cache.CACHE_SIZE_B/cache.LINE_SIZE_B);
        Cache.CacheLine cacheLine=cache.getCacheLinePool().getLine(lineNum);
        char[] CacheTag=cacheLine.getTag();
        if(!cacheLine.validBit){
            return -1;//cacheline不可用
        }else{
            if(Arrays.equals(CacheTag,getTag(blockNO))){
                return lineNum;
            }else{
                return -1;
            }
        }
    }

    /**
     * 在未命中情况下重写cache，直接映射不需要用到替换策略
     * @param blockNO
     * @return
     */
    @Override
    public int writeCache(int blockNO) {
        // TODO
        Cache cache=Cache.getCache();
        Memory memory=Memory.getMemory();
        Transformer t=new Transformer();
        String blockNum=t.intToBinary(String.valueOf(blockNO)).substring(10);
        int lineNum=blockNO%(cache.CACHE_SIZE_B/cache.LINE_SIZE_B);//将要写到的cache的行号
        cache.getCacheLinePool().getClPool()[lineNum].tag=getTag(blockNO);//修改该行的tag；
        cache.getCacheLinePool().getClPool()[lineNum].data=memory.read(blockNum+"0000000000",cache.CACHE_SIZE_B/cache.LINE_SIZE_B);//读入整个块的数据；
        cache.getCacheLinePool().getClPool()[lineNum].validBit=true;//该行数据有效
        System.out.println("lineNum is:"+lineNum);
        System.out.println(cache.getCacheLinePool().getClPool()[lineNum].data);
        return lineNum;
    }

    public static void main(String[] args){
        System.out.println(String.valueOf((long)1L));
    }


}
