package com.ada.approve.dao;


import java.util.List;

import  com.ada.approve.entity.FlowRecord;
import  com.ada.data.core.CriteriaDao;
import com.ada.data.core.Pagination;
import  com.ada.data.core.Updater;

public interface FlowRecordDao extends CriteriaDao<FlowRecord, Long>{
	public Pagination getPage(int pageNo, int pageSize);

	public FlowRecord findById(Long id);

	public FlowRecord save(FlowRecord bean);

	public FlowRecord updateByUpdater(Updater<FlowRecord> updater);

	public FlowRecord deleteById(Long id);

	public List<FlowRecord> findByFlow(Long id);
}