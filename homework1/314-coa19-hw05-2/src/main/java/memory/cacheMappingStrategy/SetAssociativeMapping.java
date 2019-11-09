package memory.cacheMappingStrategy;

import memory.Cache;
import memory.Memory;
import memory.cacheReplacementStrategy.ReplacementStrategy;
import transformer.Transformer;

/**
 * 4路-组相连映射 n=4,   14位标记 + 8位组号 + 10位块内地址
 * 256个组，每个组4行
 */
public class SetAssociativeMapping extends MappingStrategy{

    /**
     *
     * @param blockNO 内存数据块的块号
     * @return cache数据块号 22-bits  [前14位有效]
     */
    @Override
    public char[] getTag(int blockNO) {
        // TODO
        Transformer transformer=new Transformer();
        String blockNum=transformer.intToBinary(String.valueOf(blockNO)).substring(10,24);
        blockNum=blockNum+"00000000";
        return blockNum.toCharArray();
    }

    /**
     *
     * @param blockNO 目标数据内存地址前22位int表示
     * @return -1 表示未命中
     */
    @Override
    public int map(int blockNO) {
        // TODO
        Transformer transformer=new Transformer();
        Cache cache=Cache.getCache();
        ReplacementStrategy rs=cache.mappingStrategy.replacementStrategy;
        String blockNum=transformer.intToBinary(String.valueOf(blockNO)).substring(10);
        int setNum=blockNO%256;
        //组关联映射查找时只要找块对应的组即可
        int start=4*setNum;
        int end=start+4;
        return rs.isHit(start,end,(blockNum.substring(0,14)+"00000000").toCharArray());
    }

    @Override
    public int writeCache(int blockNO) {
        // TODO
        Transformer transformer=new Transformer();
        String blockNum=transformer.intToBinary(String.valueOf(blockNO)).substring(10);
        Cache cache=Cache.getCache();
        ReplacementStrategy rs=cache.mappingStrategy.replacementStrategy;
        Memory memory=Memory.getMemory();
        char[] input=memory.read(blockNum+"0000000000",cache.CACHE_SIZE_B/cache.LINE_SIZE_B);
        int setNum=blockNO%256;
        int start=4*setNum;
        int end=4*setNum+4;
        int lineNum=rs.writeCache(start,end,getTag(blockNO),input);
        cache.getCacheLinePool().getClPool()[lineNum].validBit=true;//该行数据有效
        return lineNum;
    }
}










