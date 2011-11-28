/**
 * 
 */
package com.taobao.top.analysis.job;

import java.util.Map;

/**
 * @author fangweng
 * @Email fangweng@taobao.com
 * 2011-11-28
 *
 */
public class MergedJobResult {

	int mergeCount;
	Map<String, Map<String, Object>> mergedResult;

	public int getMergeCount() {
		return mergeCount;
	}

	public void setMergeCount(int mergeCount) {
		this.mergeCount = mergeCount;
	}

	public Map<String, Map<String, Object>> getMergedResult() {
		return mergedResult;
	}

	public void setMergedResult(Map<String, Map<String, Object>> mergedResult) {
		this.mergedResult = mergedResult;
	}
}
