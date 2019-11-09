package memory.cacheMappingStrategy;

import memory.Cache;
import memory.Memory;
import memory.cacheReplacementStrategy.ReplacementStrategy;
import transformer.Transformer;

public class AssociativeMapping extends MappingStrategy {  // 全相联映射

    /**
     * @param blockNO 内存数据块的块号
     * @return cache数据块号 22-bits  [前22位有效]
     */
    @Override
    public char[] getTag(int blockNO) {
        // TODO
        Transformer transformer=new Transformer();
        String blockNum=transformer.intToBinary(String.valueOf(blockNO)).substring(10);
        return blockNum.toCharArray();
    }

    @Override
    public int map(int blockNO) {
        // TODO
        Transformer transformer=new Transformer();
        Cache cache=Cache.getCache();
        ReplacementStrategy rs=cache.mappingStrategy.replacementStrategy;
        String blockNum=transformer.intToBinary(String.valueOf(blockNO)).substring(10);
        //全关联映射要遍历cache所有行来寻找是否有和blockNo相同的tag的
        return rs.isHit(0,cache.CACHE_SIZE_B/cache.LINE_SIZE_B,blockNum.toCharArray());
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
        int lineNum=rs.writeCache(0,cache.CACHE_SIZE_B/cache.LINE_SIZE_B,getTag(blockNO),input);
        cache.getCacheLinePool().getClPool()[lineNum].validBit=true;//该行数据有效
        return lineNum;
    }
}
