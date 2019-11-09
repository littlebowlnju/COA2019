package memory;

import memory.cacheMappingStrategy.MappingStrategy;
import memory.cacheReplacementStrategy.ReplacementStrategy;
import transformer.Transformer;

import java.util.Arrays;

/**
 * 高速缓存抽象类
 * TODO: 缓存机制实现
 */
public class Cache {	//

	public static final boolean isAvailable = true;			// 默认启用Cache

	public static final int CACHE_SIZE_B = 1 * 1024 * 1024;      // 1 MB 总大小

	public static final int LINE_SIZE_B = 1 * 1024; // 1 KB

	private CacheLinePool cache = new CacheLinePool(CACHE_SIZE_B/LINE_SIZE_B); 	// 总大小1MB / 行大小1KB = 1024个行

	private static Cache cacheInstance = new Cache();

	private Cache() {}

	public static Cache getCache() {
		return cacheInstance;
	}

	public CacheLinePool getCacheLinePool(){return cache;}

	public MappingStrategy mappingStrategy;

	/**
	 * 查询{@link Cache#cache}表以确认包含[sAddr, sAddr + len)的数据块是否在cache内
	 * 如果目标数据块不在Cache内，则将其从内存加载到Cache
	 * @param sAddr 数据起始点(32位物理地址 = 22位块号 + 10位块内地址)
	 * @param len 待读数据的字节数，[sAddr, sAddr + len)包含的数据必须在同一个数据块内
	 * @return 数据块在Cache中的对应行号
	 */
	public int fetch(String sAddr, int len) {
		// TODO
		Transformer t=new Transformer();
		int blockNum=Integer.parseInt(t.binaryToInt("0000000000"+sAddr.substring(0,22)));
		System.out.println("blockNum is:"+blockNum);
		int lineNum=mappingStrategy.map(blockNum);
		if(lineNum==-1)
			lineNum=mappingStrategy.writeCache(blockNum);
		return lineNum;
	}

	/**
	 * 读取[eip, eip + len)范围内的连续数据，可能包含多个数据块的内容
	 * @param eip 数据起始点(32位物理地址 = 22位块号 + 10位块内地址)
	 * @param len 待读数据的字节数
	 * @return
	 */
	public char[] read(String eip, int len){
		char[] data = new char[len];
		Transformer t = new Transformer();
		int addr =  Integer.parseInt(t.binaryToInt("0" + eip));
		int upperBound = addr + len;
		int index = 0;
		while (addr < upperBound) {
			int nextSegLen = LINE_SIZE_B - (addr % LINE_SIZE_B);
			if (addr + nextSegLen >= upperBound) {
				nextSegLen = upperBound - addr;
			}
			int rowNO = fetch(t.intToBinary(String.valueOf(addr)), nextSegLen);
			char[] cache_data = cache.get(rowNO).getData();
			int i=0;
			while (i < nextSegLen) {
				data[index] = cache_data[addr % LINE_SIZE_B + i];
				index++;
				i++;
			}
			addr += nextSegLen;
		}
		return data;
	}

	public void setStrategy(MappingStrategy mappingStrategy, ReplacementStrategy replacementStrategy) {
		this.mappingStrategy = mappingStrategy;
		this.mappingStrategy.setReplacementStrategy(replacementStrategy);
	}

	/**
	 * 从32位物理地址(22位块号 + 10位块内地址)获取目标数据在内存中对应的块号
	 * @param addr
	 * @return
	 */
	public int getBlockNO(String addr) {
		Transformer t = new Transformer();
		return Integer.parseInt(t.binaryToInt("0" + addr.substring(0, 22)));
	}

	/**
	 * 告知Cache某个连续地址范围内的数据发生了修改，缓存失效
	 * @param sAddr 发生变化的数据段的起始地址
	 * @param len 数据段长度
	 */
	public void invalid(String sAddr, int len) {
		int from = getBlockNO(sAddr);
		Transformer t = new Transformer();
		int to = getBlockNO(t.intToBinary(String.valueOf(Integer.parseInt(t.binaryToInt("0" + sAddr)) + len - 1)));

		for (int blockNO=from; blockNO<=to; blockNO++) {
			int rowNO = mappingStrategy.map(blockNO);
			if (rowNO != -1) {
				cache.get(rowNO).validBit = false;
			}
		}
	}

	/**
	 * 清除Cache全部缓存
	 */
	public void clear() {
		for (CacheLine line:cache.clPool) {
			if (line != null) {
				line.validBit = false;
			}
		}
	}

	/**
	 * 输入行号和对应的预期值，判断Cache当前状态是否符合预期
	 * 这个方法仅用于测试
	 * @param lineNOs
	 * @param validations
	 * @param tags
	 * @return
	 */
	public boolean checkStatus(int[] lineNOs, boolean[] validations, char[][] tags) {
		if (lineNOs.length != validations.length || validations.length != tags.length) {
			return false;
		}
		for (int i=0; i<lineNOs.length; i++) {
			CacheLine line = cache.get(lineNOs[i]);
			if (line.validBit != validations[i]) {
				return false;
			}
			if (!Arrays.equals(line.getTag(), tags[i])) {
				return false;
			}
		}
		return true;
	}

	/**
	 * 负责对CacheLine进行动态初始化
	 */
	public class CacheLinePool {
		/**
		 * @param lines Cache的总行数
		 */
		CacheLinePool(int lines) {
			clPool = new CacheLine[lines];
		}
		public CacheLine[] getClPool(){return clPool;}
		private CacheLine[] clPool;
		public CacheLine get(int lineNO) {
			if (lineNO >= 0 && lineNO <clPool.length) {
				CacheLine l = clPool[lineNO];
				if (l == null) {
					clPool[lineNO] = new CacheLine();
					l = clPool[lineNO];
				}
				return l;
			}
			return null;
		}
		public CacheLine getLine(int lineNo){return get(lineNo);}
	}

	/**
	 * Cache行，每行长度为(1+22+{@link Cache#LINE_SIZE_B})
	 */
	public class CacheLine {
		// 有效位，标记该条数据是否有效
		public boolean validBit = false;

		// 用于LFU算法，记录该条cache使用次数
		public int visited = 0;

		// 用于LRU和FIFO算法，记录该条数据时间戳
		public Long timeStamp = 0l;

		// 标记，占位长度为()22位，有效长度取决于映射策略：
		// 直接映射: 12 位
		// 全关联映射: 22 位
		// (2^n)-路组关联映射: 22-(10-n) 位
		// 注意，tag在物理地址中用高位表示，如：直接映射(32位)=tag(12位)+行号(10位)+块内地址(10位)，
		// 那么对于值为0b1111的tag应该表示为0000000011110000000000，其中前12位为有效长度
		public char[] tag = new char[22];

		// 数据
		public char[] data = new char[LINE_SIZE_B];

		char[] getData() {
			return this.data;
		}
		public char[] getTag() {
			return this.tag;
		}

	}
}
