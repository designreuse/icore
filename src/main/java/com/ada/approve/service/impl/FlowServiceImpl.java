package com.ada.approve.service.impl;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.ada.approve.dao.FlowApprovalDao;
import com.ada.approve.dao.FlowDao;
import com.ada.approve.dao.FlowDefinitionDao;
import com.ada.approve.dao.FlowRecordDao;
import com.ada.approve.dao.TaskDao;
import com.ada.approve.entity.Flow;
import com.ada.approve.entity.FlowApproval;
import com.ada.approve.entity.FlowDefinition;
import com.ada.approve.entity.FlowDefinitionItem;
import com.ada.approve.entity.FlowRecord;
import com.ada.approve.entity.Task;
import com.ada.approve.page.FlowPage;
import com.ada.approve.service.FlowService;
import com.ada.data.core.Finder;
import com.ada.data.core.Pagination;
import com.ada.data.core.Updater;
import com.ada.data.page.Filter;
import com.ada.data.page.Order;
import com.ada.data.page.Page;
import com.ada.data.page.Pageable;
import com.ada.data.rest.domain.AbstractVo;
import com.ada.user.dao.UserInfoDao;
import com.ada.user.entity.UserInfo;

@Service
@Transactional
public class FlowServiceImpl implements FlowService {

	private FlowDao dao;

	@Autowired
	FlowApprovalDao flowApprovalDao;

	@Autowired
	private FlowDefinitionDao flowDefinitionDao;

	@Autowired
	FlowRecordDao flowRecordDao;

	@Autowired
	TaskDao taskDao;

	@Autowired
	UserInfoDao userInfoDao;
	@Transactional
	@Override
	public AbstractVo approve(Long taskid, Integer state, String note, Long user) {
		AbstractVo restult = new AbstractVo();
		Task curtask = taskDao.findById(taskid);
		UserInfo worker = userInfoDao.findById(user);
		if (worker == null) {
			restult.setCode(-4);
			restult.setMsg("非法用户");
		}
		if (worker != null) {
			if (worker.getId() != curtask.getUser().getId()) {
				restult.setCode(-5);
				restult.setMsg("非法操作");
			}

		}
		if (curtask == null) {
			restult.setCode(-3);
			restult.setMsg("该任务不存在");
			return restult;
		}
		if (curtask.getState() == 1) {
			restult.setCode(-1);
			restult.setMsg("该任务已经处理过了");
			return restult;
		}
		Flow flow = curtask.getFlow();
		if (flow.getState() == 2) {
			restult.setCode(-2);
			restult.setMsg("该流程已经审核过了");
			return restult;
		}

		if (flow.getHierarchy() == 0) {
			state = 1;
		}

		curtask.setState(1);

		// 同意审核
		if (state == 1) {
			FlowRecord bean = new FlowRecord();
			bean.setState(1);
			bean.setHierarchy(curtask.getFlow().getHierarchy());
			bean.setUser(curtask.getUser());
			bean.setNote(note);
			bean.setFlow(flow);
			flowRecordDao.save(bean);
			FlowApproval ap = flowApprovalDao.findNext(curtask.getFlow().getId(), curtask.getFlow().getHierarchy());
			// 审批结束
			if (ap == null) {
				if (flow != null) {

					flow.setState(2);
					// 完成以后应该有个事件
					// if (flows != null) {
					// for (OnFlowFinshed onFlowFinshed : flows) {
					// onFlowFinshed.finded(flow);
					// }
					// }

				}
			} else {
				// 该节点审核结束，进入他的下一个节点
				if (flow != null) {
					flow.setState(1);
					flow.setHierarchy(ap.getHierarchy());
				}
				Task task = new Task();
				task.setUser(ap.getUser());
				task.setCatalog(flow.getCatalog());
				task.setOid(flow.getOid());
				task.setTitle(flow.getTitle());
				task.setNote(flow.getNote());
				task.setFlow(flow);
				task.setState(0);
				task.setStyle(1);
				taskDao.save(task);
			}

		} else {
			Integer hInteger = flow.getHierarchy();
			// 拒绝审核
			flow.setHierarchy(0);
			flow.setState(-1);

			Task task = new Task();
			task.setUser(curtask.getFlow().getUser());
			task.setCatalog(flow.getCatalog());
			task.setOid(flow.getOid());
			task.setTitle(flow.getTitle());
			task.setNote(flow.getNote());
			task.setFlow(flow);
			task.setState(0);
			task.setStyle(-1);
			taskDao.save(task);

			FlowRecord bean = new FlowRecord();
			bean.setState(0);
			bean.setHierarchy(hInteger);
			bean.setUser(curtask.getUser());
			bean.setNote(note);
			bean.setFlow(flow);
			flowRecordDao.save(bean);

		}

		return restult;
	}

	@Transactional(readOnly = true)
	public long count(Filter... filters) {

		return dao.count(filters);

	}

	@Transactional
	public Flow deleteById(Long id) {
		Flow bean = dao.deleteById(id);
		return bean;
	}

	@Transactional
	public Flow[] deleteByIds(Long[] ids) {
		Flow[] beans = new Flow[ids.length];
		for (int i = 0, len = ids.length; i < len; i++) {
			beans[i] = deleteById(ids[i]);
		}
		return beans;
	}

	@Transactional(readOnly = true)
	public Flow findById(Long id) {
		Flow entity = dao.findById(id);
		return entity;
	}

	@Transactional(readOnly = true)
	public List<Flow> findList(Integer first, Integer count, List<Filter> filters, List<Order> orders) {

		return dao.findList(first, count, filters, orders);

	}

	@Transactional(readOnly = true)
	public Page<Flow> findPage(Pageable pageable) {
		return dao.findPage(pageable);
	}

	@Transactional(readOnly = true)
	public FlowPage getPage(int pageNo, int pageSize) {
		FlowPage result = null;
		Finder finder = Finder.create();
		finder.append("from Flow f ");
		finder.append(" order by f.id desc  ");
		Pagination<Flow> page = dao.find(finder, pageNo, pageSize);
		result = new FlowPage(page);
		return result;
	}
	@Transactional
	public Flow save(Flow bean) {
		dao.save(bean);
		return bean;
	}
	@Autowired
	public void setDao(FlowDao dao) {
		this.dao = dao;
	}

	@Override
	public Flow start(Flow flow) {
		FlowDefinition definition = flow.getFlow();
		if (definition == null) {
			return null;
		}
		flow.setCatalog(Integer.valueOf(""+definition.getId()));
		flow.setState(0);
		flow.setHierarchy(1);
		flow = dao.save(flow);
		FlowDefinition fs = flowDefinitionDao.findById(definition.getId());
		UserInfo taskuser = null;

		List<FlowDefinitionItem> vos = fs.getItems();
		if (vos != null) {
			for (FlowDefinitionItem approverVo : vos) {
				FlowApproval approval = new FlowApproval();
				approval.setFlow(flow);
				approval.setHierarchy(approverVo.getHierarchy());
				approval.setUser(approverVo.getUser());
				flowApprovalDao.save(approval);
				if (approverVo.getHierarchy() == 1) {
					taskuser = approverVo.getUser();
				}
			}
		}
		if (taskuser != null) {
			Task task = new Task();
			task.setUser(taskuser);
			task.setCatalog(flow.getCatalog());
			task.setOid(flow.getOid());
			task.setTitle(flow.getTitle());
			task.setNote(flow.getNote());
			task.setFlow(flow);
			task.setState(0);
			task.setStyle(1);
			taskDao.save(task);
		}
		return flow;
	}

	@Transactional
	public Flow update(Flow bean) {
		Updater<Flow> updater = new Updater<Flow>(bean);
		bean = dao.updateByUpdater(updater);
		return bean;
	}
}