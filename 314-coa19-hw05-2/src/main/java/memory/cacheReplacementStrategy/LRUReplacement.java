package memory.cacheReplacementStrategy;

import memory.Cache;

import java.util.Arrays;

/**
 * 最近最少用算法
 */
public class LRUReplacement extends ReplacementStrategy {

    /**
     *
     * @param start 起始位置
     * @param end 结束位置 闭区间
     */
    @Override
    public int isHit(int start, int end,char[] addrTag) {
        // TODO
        Cache cache=Cache.getCache();

//        for(int i=start;i<end;i++){
//            cache.getCacheLinePool().get(i).timeStamp++;
//        }
        for(int i=start;i<end;i++){
            if(Arrays.equals(addrTag,cache.getCacheLinePool().get(i).tag)){
                if(cache.getCacheLinePool().get(i).validBit) {
                    //命中行的timestamp应该比其他的行数大
                    cache.getCacheLinePool().get(i).timeStamp++;
                    return i;//命中
                }
            }
        }
        return -1;
    }


    /**
     * 找到最小时间戳的行，替换
     * @param start 起始行
     * @param end 结束行 闭区间
     * @param addrTag tag
     * @param input  数据
     * @return
     */
    @Override
    public int writeCache(int start, int end, char[] addrTag, char[] input) {
        // TODO
        Cache cache=Cache.getCache();
        //写入的行的时间戳增加
        for(int i=start;i<end;i++){
            cache.getCacheLinePool().get(i).timeStamp++;
        }
        for(int i=start;i<end;i++) {
            //如果validbit为false，则该行仍为空，可以直接写入
            if (!cache.getCacheLinePool().get(i).validBit) {
                cache.getCacheLinePool().get(i).tag = addrTag;
                cache.getCacheLinePool().get(i).data = input;
                cache.getCacheLinePool().get(i).validBit = true;
                cache.getCacheLinePool().get(i).timeStamp = 1L;
                return i;
            }
        }
        //如果所有行都被占用，则找到时间戳最小的行替换
        //找到最小时间戳的行
        Long minTimeStamp=cache.getCacheLinePool().get(start).timeStamp;
        int minTimeStampLineNum=start;
        for(int i=start+1;i<end;i++){
            if(cache.getCacheLinePool().get(i).timeStamp.compareTo(minTimeStamp)<0){
                minTimeStamp=cache.getCacheLinePool().get(i).timeStamp;
                minTimeStampLineNum=i;
            }
        }
        //在该行写入数据
        cache.getCacheLinePool().get(minTimeStampLineNum).tag=addrTag;
        cache.getCacheLinePool().get(minTimeStampLineNum).data=input;
        cache.getCacheLinePool().get(minTimeStampLineNum).timeStamp=1L;
        return minTimeStampLineNum;
    }

}





























