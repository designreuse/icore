package com.ada.user.dao.impl;

import org.hibernate.Criteria;
import org.hibernate.SessionFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import com.quhaodian.data.core.CriteriaDaoImpl;
import com.quhaodian.data.core.Pagination;
import com.ada.user.dao.UserRoleDao;
import com.ada.user.entity.UserRole;

@Repository
public class UserRoleDaoImpl extends CriteriaDaoImpl<UserRole, Long> implements UserRoleDao {
	public Pagination getPage(int pageNo, int pageSize) {
		Criteria crit = createCriteria();
		Pagination page = findByCriteria(crit, pageNo, pageSize);
		return page;
	}

	public UserRole findById(Long id) {
		UserRole entity = get(id);
		return entity;
	}

	public UserRole save(UserRole bean) {
		getSession().save(bean);
		return bean;
	}

	public UserRole deleteById(Long id) {
		UserRole entity = super.get(id);
		if (entity != null) {
			getSession().delete(entity);
		}
		return entity;
	}
	
	@Override
	protected Class<UserRole> getEntityClass() {
		return UserRole.class;
	}
	
	@Autowired
	public void setSuperSessionFactory(SessionFactory sessionFactory){
	    super.setSessionFactory(sessionFactory);
	}
}