package com.ada.user.dao.impl;

import org.hibernate.Criteria;
import org.hibernate.SessionFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import com.ada.data.core.CriteriaDaoImpl;
import com.ada.data.core.Pagination;
import com.ada.user.dao.UserTokenDao;
import com.ada.user.entity.UserToken;

@Repository
public class UserTokenDaoImpl extends CriteriaDaoImpl<UserToken, Long> implements UserTokenDao {
	public Pagination getPage(int pageNo, int pageSize) {
		Criteria crit = createCriteria();
		Pagination page = findByCriteria(crit, pageNo, pageSize);
		return page;
	}

	public UserToken findById(Long id) {
		UserToken entity = get(id);
		return entity;
	}

	public UserToken save(UserToken bean) {
		getSession().save(bean);
		return bean;
	}

	public UserToken deleteById(Long id) {
		UserToken entity = super.get(id);
		if (entity != null) {
			getSession().delete(entity);
		}
		return entity;
	}
	
	@Override
	protected Class<UserToken> getEntityClass() {
		return UserToken.class;
	}
	
	@Autowired
	public void setSuperSessionFactory(SessionFactory sessionFactory){
	    super.setSessionFactory(sessionFactory);
	}
}