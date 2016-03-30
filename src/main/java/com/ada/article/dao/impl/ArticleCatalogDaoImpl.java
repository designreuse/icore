package com.ada.article.dao.impl;

import java.util.Date;

import org.hibernate.Criteria;
import org.hibernate.SessionFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import com.ada.article.dao.ArticleCatalogDao;
import com.ada.article.entity.ArticleCatalog;
import com.ada.data.core.CriteriaDaoImpl;
import com.ada.data.core.Finder;
import com.ada.data.core.Pagination;

@Repository
public class ArticleCatalogDaoImpl extends CriteriaDaoImpl<ArticleCatalog, Integer> implements ArticleCatalogDao {
	public Pagination getPage(int pageNo, int pageSize) {
		Criteria crit = createCriteria();
		Pagination page = findByCriteria(crit, pageNo, pageSize);
		return page;
	}

	public ArticleCatalog findById(Integer id) {
		ArticleCatalog entity = get(id);
		return entity;
	}

	public ArticleCatalog save(ArticleCatalog bean) {
		getSession().save(bean);
		return bean;
	}

	public ArticleCatalog deleteById(Integer id) {
		ArticleCatalog entity = super.get(id);
		if (entity != null) {
			getSession().delete(entity);
		}
		return entity;
	}

	@Override
	protected Class<ArticleCatalog> getEntityClass() {
		return ArticleCatalog.class;
	}

	@Autowired
	public void setSuperSessionFactory(SessionFactory sessionFactory) {
		super.setSessionFactory(sessionFactory);
	}

	@Override
	public Integer updateNums(Integer id) {

		Integer result = 0;
		ArticleCatalog cur = findById(id);
		do {
			counts(cur);
			cur = cur.getParent();
			result++;
		} while (cur != null);

		return result;
	}

	private void counts(ArticleCatalog cur) {
		Finder finder = Finder.create();
		finder.append("from Article a where a.catalog.lft >= :lft ");
		finder.setParam("lft", cur.getLft());
		finder.append(" and a.catalog.rgt <= :rgt");
		finder.setParam("rgt", cur.getRgt());
		Long size = countQuery(finder);
		cur.setNums(size);
	}

	@Override
	public Integer updateNumsAndTime(Integer id) {
		Integer result = 0;
		ArticleCatalog cur = findById(id);
		if (cur == null) {
			return result;
		}
		do {
			counts(cur);
			cur.setLastDate(new Date());
			cur = cur.getParent();
			result++;
		} while (cur != null);

		return result;
	}
}