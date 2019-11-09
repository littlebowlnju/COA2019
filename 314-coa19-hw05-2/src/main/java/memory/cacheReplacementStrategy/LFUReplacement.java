package memory.cacheReplacementStrategy;

import memory.Cache;

import java.util.Arrays;

/**
 * 最近不经常使用算法
 */
public class LFUReplacement extends ReplacementStrategy {

    @Override
    public int isHit(int start, int end, char[] addrTag) {
        // TODO
        Cache cache=Cache.getCache();
        for(int i=start;i<end;i++){
            if(Arrays.equals(addrTag,cache.getCacheLinePool().get(i).tag)){
                if(cache.getCacheLinePool().get(i).validBit) {
                    cache.getCacheLinePool().get(i).visited++;//该行命中即要被使用，因此visited++
                    return i;//命中
                }
            }
        }
        return -1;
    }
    /**
     * 找到最小visited的行，替换
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
        //如果所有行都被占用，则找到visited最小的行替换
        //找到最小visited的行
        int minVisited=cache.getCacheLinePool().get(start).visited;
        int minVisitedLineNum=start;
        for(int i=start+1;i<end;i++){
            if(cache.getCacheLinePool().get(i).visited<minVisited){
                minVisited=cache.getCacheLinePool().get(i).visited;
                minVisitedLineNum=i;
            }
        }
        //在该行写入数据
        cache.getCacheLinePool().get(minVisitedLineNum).tag=addrTag;
        cache.getCacheLinePool().get(minVisitedLineNum).data=input;
        cache.getCacheLinePool().get(minVisitedLineNum).visited=1;//该行数据被重新写入，因此使用次数从1开始
        return minVisited;
    }
}
