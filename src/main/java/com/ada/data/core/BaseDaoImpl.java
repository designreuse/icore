package com.ada.data.core;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.hibernate.Criteria;
import org.hibernate.LockMode;
import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.criterion.CriteriaSpecification;
import org.hibernate.criterion.Criterion;
import org.hibernate.criterion.Projection;
import org.hibernate.criterion.Projections;
import org.hibernate.criterion.Restrictions;
import org.hibernate.hql.internal.ast.QueryTranslatorImpl;
import org.hibernate.internal.CriteriaImpl;
import org.hibernate.internal.SessionFactoryImpl;
import org.hibernate.metadata.ClassMetadata;
import org.hibernate.transform.ResultTransformer;
import org.hibernate.transform.Transformers;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.orm.hibernate4.support.HibernateDaoSupport;
import org.springframework.util.Assert;

import com.ada.data.page.Filter;
import com.ada.data.page.Filter.Operator;
import com.ada.data.page.Order;
import com.ada.data.page.Order.Direction;
import com.ada.data.page.Page;
import com.ada.data.page.Pageable;

public abstract class BaseDaoImpl<T, ID extends Serializable> extends HibernateDaoSupport implements BaseDao<T, ID> {
	/**
	 * @see Session.get(Class,Serializable)
	 * @param id
	 * @return 持久化对象
	 */
	protected T get(ID id) {
		return get(id, false);
	}
	
	public List<T> list(Integer first, Integer count, List<Filter> filters, List<Order> orders){
		List<T> result = null;
		Finder finder = makeFinder(filters, orders);
		
		
		finder.setFirstResult(first);
		finder.setMaxResults(count);
		result =  find(finder);
		return result;
	}

	public Page<T> page(Pageable pageable) {
		Page<T> result = null;
		List<Filter> filters = pageable.getFilters();
		List<Order> orders=	pageable.getOrders();

		Finder finder = makeFinder(filters, orders);
		
		
		
		Pagination<T> page = find(finder, pageable.getPageNumber(), pageable.getPageSize());
		result = new Page<T>(page.getList(), page.getTotalCount(), pageable);
		return result;
	}

	/**
	 * @param filters
	 * @param orders
	 * @return
	 */
	private Finder makeFinder(List<Filter> filters, List<Order> orders) {
		Finder finder = Finder.create();
		finder.append("select distinct model from " + getEntityClass().getSimpleName());
		finder.append(" model where 1= 1 ");
		if (filters != null) {
			for (Filter filter : filters) {
				finder.append(" and  model." + filter.getProperty());
				if (filter.getOperator() == Operator.eq) {
					finder.append(" =:" + getProperty(filter));
				}
				else if (filter.getOperator() == Operator.ne) {
					finder.append(" !=:" + getProperty(filter));
				}
				else if (filter.getOperator() == Operator.ge) {
					finder.append(" >=:" + getProperty(filter));
				}
				else if (filter.getOperator() == Operator.gt) {
					finder.append(" >:" + getProperty(filter));
				}
				else if (filter.getOperator() == Operator.le) {
					finder.append(" <=:" + getProperty(filter));
				}
				else if (filter.getOperator() == Operator.lt) {
					finder.append(" <:" + getProperty(filter));
				}
				else if (filter.getOperator() == Operator.like) {
					finder.append(" like:" + getProperty(filter));
				}
				
				
				if (filter.getOperator() == Operator.like) {
					finder.setParam(getProperty(filter), "%"+filter.getValue()+"%");
				}else{
					finder.setParam(getProperty(filter), filter.getValue());
				}
			}
		}
		
		if (orders!=null&&orders.size()>0) {
			
			finder.append(" order by ");
			int num=0;
			int all=orders.size();
			for (Order order : orders) {
				num++;
				finder.append(" model."+order.getProperty());
				if (order.getDirection()==Direction.asc) {
					finder.append(" asc ");
				}
				else{
					finder.append(" desc ");
				}
				
				if (num!=all) {
					finder.append(" , ");
				}
			}
		}
		return finder;
	}
	/**
	 * @param filter
	 * @return
	 */
	private String getProperty(Filter filter) {
		String result=filter.getProperty();
		result=result.replaceAll("\\.", "");
		return result;
	}
	
	
	public T findOne(Finder finder) {
		T entity = null;
		List list = find(finder);
		if (list != null && list.size() > 0) {
			entity = (T) list.get(0);
		}
		return entity;
	}

	/**
	 * @see Session.get(Class,Serializable,LockMode)
	 * @param id
	 *            对象ID
	 * @param lock
	 *            是否锁定，使用LockMode.UPGRADE
	 * @return 持久化对象
	 */
	@SuppressWarnings("unchecked")
	protected T get(ID id, boolean lock) {
		T entity;
		if (lock) {
			entity = (T) getSession().get(getEntityClass(), id, LockMode.UPGRADE);
		} else {
			entity = (T) getSession().get(getEntityClass(), id);
		}
		return entity;
	}

	protected Session getSession() {
		// TODO Auto-generated method stub
		return getSessionFactory().getCurrentSession();
	}

	/**
	 * 按属性查找对象列表
	 */
	@SuppressWarnings("unchecked")
	public List<T> findByProperty(String property, Object value) {
		Assert.hasText(property);
		return createCriteria(Restrictions.eq(property, value)).list();
	}

	/**
	 * 按属性查找唯一对象
	 */
	@SuppressWarnings("unchecked")
	protected T findUniqueByProperty(String property, Object value) {
		Assert.hasText(property);
		Assert.notNull(value);
		return (T) createCriteria(Restrictions.eq(property, value)).uniqueResult();
	}

	/**
	 * 按属性统计记录数
	 * 
	 * @param property
	 * @param value
	 * @return
	 */
	protected int countByProperty(String property, Object value) {
		Assert.hasText(property);
		Assert.notNull(value);
		return ((Number) (createCriteria(Restrictions.eq(property, value)).setProjection(Projections.rowCount())
				.uniqueResult())).intValue();
	}

	/**
	 * 按Criterion查询列表数据.
	 * 
	 * @param criterion
	 *            数量可变的Criterion.
	 */
	@SuppressWarnings("unchecked")
	public List findByCriteria(Criterion... criterion) {
		return createCriteria(criterion).list();
	}

	/**
	 * 通过Updater更新对象
	 * 
	 * @param updater
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public T updateByUpdater(Updater<T> updater) {
		ClassMetadata cm = getSessionFactory().getClassMetadata(getEntityClass());
		T bean = updater.getBean();
		T po = (T) getSession().get(getEntityClass(), cm.getIdentifier(bean));
		updaterCopyToPersistentObject(updater, po, cm);
		return po;
	}

	/**
	 * 将更新对象拷贝至实体对象，并处理many-to-one的更新。
	 * 
	 * @param updater
	 * @param po
	 */
	private void updaterCopyToPersistentObject(Updater<T> updater, T po, ClassMetadata cm) {
		String[] propNames = cm.getPropertyNames();
		String identifierName = cm.getIdentifierPropertyName();
		T bean = updater.getBean();
		Object value;
		for (String propName : propNames) {
			if (propName.equals(identifierName)) {
				continue;
			}
			try {
				value = MyBeanUtils.getSimpleProperty(bean, propName);
				if (!updater.isUpdate(propName, value)) {
					continue;
				}
				cm.setPropertyValue(po, propName, value);
			} catch (Exception e) {
				throw new RuntimeException("copy property to persistent object failed: '" + propName + "'", e);
			}
		}
	}

	/**
	 * 根据Criterion条件创建Criteria,后续可进行更多处理,辅助函数.
	 */
	protected Criteria createCriteria(Criterion... criterions) {
		Criteria criteria = getSession().createCriteria(getEntityClass());
		for (Criterion c : criterions) {
			criteria.add(c);
		}
		return criteria;
	}

	/**
	 * 获得Dao对于的实体类
	 * 
	 * @return
	 */
	abstract protected Class<T> getEntityClass();

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.mendao.data.core.BaseDao#add(T)
	 */
	@Override
	public T add(T t) {
		getHibernateTemplate().save(t);
		return t;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.mendao.data.core.BaseDao#delete(T)
	 */
	@Override
	public T delete(T t) {
		getHibernateTemplate().delete(t);
		return t;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.mendao.data.core.BaseDao#update(T)
	 */
	@Override
	public T update(T t) {
		getHibernateTemplate().update(t);
		return t;
	}

	@Override
	public T merge(T t) {
		getHibernateTemplate().merge(t);
		return t;
	}

	/**
	 * 日志，可用于子类
	 */
	protected Logger log = LoggerFactory.getLogger(getClass());
	/**
	 * HIBERNATE 的 order 属性
	 */
	protected static final String ORDER_ENTRIES = "orderEntries";

	/**
	 * 通过HQL查询对象列表
	 * 
	 * @param hql
	 *            hql语句
	 * @param values
	 *            数量可变的参数
	 */
	@SuppressWarnings("unchecked")
	public List find(String hql, Object... values) {
		return createQuery(hql, values).list();
	}

	/**
	 * 通过HQL查询唯一对象
	 */
	protected Object findUnique(String hql, Object... values) {
		return createQuery(hql, values).uniqueResult();
	}

	/**
	 * 通过Finder获得分页数据
	 * 
	 * @param finder
	 *            Finder对象
	 * @param pageNo
	 *            页码
	 * @param pageSize
	 *            每页条数
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public Pagination<T> find(Finder finder, int pageNo, int pageSize) {
		int totalCount = countQueryResult(finder);
		Pagination<T> p = new Pagination<T>(pageNo, pageSize, totalCount);
		if (totalCount < 1) {
			p.setList(new ArrayList());
			return p;
		}
		if (p.getPageNo() < pageNo) {

			p.setList(new ArrayList());
			return p;
		}
		Query query = getSessionFactory().getCurrentSession().createQuery(finder.getOrigHql());
		finder.setParamsToQuery(query);
		query.setFirstResult(p.getFirstResult());
		query.setMaxResults(p.getPageSize());
		if (finder.isCacheable()) {
			query.setCacheable(true);
		}
		List<T> list = query.list();
		p.setList(list);
		return p;
	}

	public <X> Pagination<X> findSql(Finder finder, int pageNo, int pageSize, Class<X> otoclass) {
		int totalCount = countQuerySqlResult(finder);
		Pagination p = new Pagination(pageNo, pageSize, totalCount);
		if (totalCount < 1) {
			p.setList(new ArrayList());
			return p;
		}
		if (p.getPageNo() < pageNo) {

			p.setList(new ArrayList<X>());
			return p;
		}
		Query query = getSessionFactory().getCurrentSession().createSQLQuery(finder.getOrigHql());
		finder.setParamsToQuery(query);
		query.setFirstResult(p.getFirstResult());
		query.setMaxResults(p.getPageSize());
		query.setResultTransformer(Transformers.aliasToBean(otoclass));
		if (finder.isCacheable()) {
			query.setCacheable(true);
		}
		List<X> list = query.list();
		p.setList(list);
		return p;

	}

	/**
	 * 通过Finder获得列表数据
	 * 
	 * @param finder
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public List find(Finder finder) {
		Query query = finder.createQuery(getSessionFactory().getCurrentSession());
		List list = query.list();
		return list;
	}

	/**
	 * 根据查询函数与参数列表创建Query对象,后续可进行更多处理,辅助函数.
	 */
	protected Query createQuery(String queryString, Object... values) {
		Assert.hasText(queryString);
		Query queryObject = getSessionFactory().getCurrentSession().createQuery(queryString);
		if (values != null) {
			for (int i = 0; i < values.length; i++) {
				queryObject.setParameter(i, values[i]);
			}
		}
		return queryObject;
	}

	/**
	 * 通过Criteria获得分页数据
	 * 
	 * @param crit
	 * @param pageNo
	 * @param pageSize
	 * @param projection
	 * @param orders
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public Pagination findByCriteria(Criteria crit, int pageNo, int pageSize) {
		CriteriaImpl impl = (CriteriaImpl) crit;
		// 先把Projection、ResultTransformer、OrderBy取出来,清空三者后再执行Count操作
		Projection projection = impl.getProjection();
		ResultTransformer transformer = impl.getResultTransformer();
		List<CriteriaImpl.OrderEntry> orderEntries;
		try {
			orderEntries = (List) MyBeanUtils.getFieldValue(impl, ORDER_ENTRIES);
			MyBeanUtils.setFieldValue(impl, ORDER_ENTRIES, new ArrayList());
		} catch (Exception e) {
			throw new RuntimeException("cannot read/write 'orderEntries' from CriteriaImpl", e);
		}

		int totalCount = ((Number) crit.setProjection(Projections.rowCount()).uniqueResult()).intValue();
		Pagination p = new Pagination(pageNo, pageSize, totalCount);
		if (totalCount < 1) {
			p.setList(new ArrayList());
			return p;
		}

		// 将之前的Projection,ResultTransformer和OrderBy条件重新设回去
		crit.setProjection(projection);
		if (projection == null) {
			crit.setResultTransformer(CriteriaSpecification.ROOT_ENTITY);
		}
		if (transformer != null) {
			crit.setResultTransformer(transformer);
		}
		try {
			MyBeanUtils.setFieldValue(impl, ORDER_ENTRIES, orderEntries);
		} catch (Exception e) {
			throw new RuntimeException("set 'orderEntries' to CriteriaImpl faild", e);
		}
		crit.setFirstResult(p.getFirstResult());
		crit.setMaxResults(p.getPageSize());
		p.setList(crit.list());
		return p;
	}

	/**
	 * 获得Finder的记录总数
	 * 
	 * @param finder
	 * @return
	 */
	public int countQueryResult(Finder finder) {
		int result = 0;
		try {
			Query query = getSessionFactory().getCurrentSession().createQuery(finder.getRowCountHql());
			finder.setParamsToQuery(query);
			if (finder.isCacheable()) {
				query.setCacheable(true);
			}
			result = ((Number) query.iterate().next()).intValue();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return result;
	}

	/**
	 * 获得Finder的记录总数
	 * 
	 * @param finder
	 * @return
	 */
	public Long countQuery(Finder finder) {
		Long result = 0l;
		try {
			Query query = getSessionFactory().getCurrentSession().createQuery(finder.getRowCountHql());
			finder.setParamsToQuery(query);
			if (finder.isCacheable()) {
				query.setCacheable(true);
			}
			result = ((Number) query.iterate().next()).longValue();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return result;
	}

	/**
	 * 获得Finder的记录总数
	 * 
	 * @param finder
	 * @return
	 */
	public int countQuerySqlResult(Finder finder) {
		Query query = getSessionFactory().getCurrentSession().createSQLQuery(finder.getRowCountHql());
		finder.setParamsToQuery(query);
		if (finder.isCacheable()) {
			query.setCacheable(true);
		}
		List<?> ls = query.list();
		Object o = ls.get(0);
		if (o instanceof Number) {
			return ((Number) o).intValue();

		} else {
			return 0;

		}
	}

	public <X> X hql(Finder finder) {
		Query query = getSessionFactory().getCurrentSession().createQuery(finder.getOrigHql());
		finder.setParamsToQuery(query);
		if (finder.isCacheable()) {
			query.setCacheable(true);
		}
		List<?> ls = query.list();
		Object o = ls.get(0);
		X result = (X) o;
		return result;
	}

	public String transHqlToSql(String hql) {
		// 当hql为null或空时,直接返回null
		if (hql == null || hql.equals("")) {
			return null;
		}
		// 获取当前session
		Session session = getSession();
		// 得到session工厂实现类
		SessionFactoryImpl sfi = (SessionFactoryImpl) session.getSessionFactory();
		// 得到Query转换器实现类
		QueryTranslatorImpl queryTranslator = new QueryTranslatorImpl(hql, hql, Collections.EMPTY_MAP, sfi);
		queryTranslator.compile(Collections.EMPTY_MAP, false);
		// 得到sql
		String sql = queryTranslator.getSQLString();
		// 关闭session
		return sql;
	}

	public <X> List<X> listSQL(String sql, Class<X> otoclass) {
		List<X> result = null;
		Query query = getSessionFactory().getCurrentSession().createSQLQuery(sql);
		query.setResultTransformer(Transformers.aliasToBean(otoclass));
		result = query.list();
		return result;
	}

	public <X> X oneSQL(String sql, Class<X> otoclass) {
		X result = null;
		List<X> results = listSQL(sql,0,2, otoclass);
		if (results != null && results.size() > 0) {
			result = results.get(0);
		}

		return result;
	}

	public <X>  List<X> listSQL(String sql, Integer stat, Integer max, Class<X> otoclass) {
		List<X> result = null;
		Query query = getSessionFactory().getCurrentSession().createSQLQuery(sql);
		query.setResultTransformer(Transformers.aliasToBean(otoclass));
		query.setFirstResult(stat);
		query.setMaxResults(max);
		result = query.list();
		return result;
	}

}
