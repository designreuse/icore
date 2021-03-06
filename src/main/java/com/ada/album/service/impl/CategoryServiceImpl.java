package com.ada.album.service.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.quhaodian.data.core.Finder;
import com.quhaodian.data.core.Pagination;
import com.quhaodian.data.core.Updater;
import com.ada.album.dao.CategoryDao;
import com.ada.album.entity.Category;
import com.ada.album.service.CategoryService;
import com.ada.album.page.CategoryPage;

import com.quhaodian.data.page.Filter;
import com.quhaodian.data.page.Order;
import com.quhaodian.data.page.Page;
import com.quhaodian.data.page.Pageable;
import java.util.List;


@Service
@Transactional
public class CategoryServiceImpl implements CategoryService {
	

	@Transactional(readOnly = true)
	public Category findById(String id) {
		Category entity = dao.findById(id);
		return entity;
	}

    @Transactional
	public Category save(Category bean) {
		dao.save(bean);
		return bean;
	}

    @Transactional
	public Category update(Category bean) {
		Updater<Category> updater = new Updater<Category>(bean);
		bean = dao.updateByUpdater(updater);
		return bean;
	}

    @Transactional
	public Category deleteById(String id) {
		Category bean = dao.deleteById(id);
		return bean;
	}

    @Transactional	
	public Category[] deleteByIds(String[] ids) {
		Category[] beans = new Category[ids.length];
		for (int i = 0,len = ids.length; i < len; i++) {
			beans[i] = deleteById(ids[i]);
		}
		return beans;
	}

	private CategoryDao dao;

	@Autowired
	public void setDao(CategoryDao dao) {
		this.dao = dao;
	}
	
	@Transactional(readOnly = true)
	public CategoryPage getPage(int pageNo, int pageSize) {
	    CategoryPage result = null;
		Finder finder=Finder.create();
		finder.append("from Category f ");
		finder.append(" order by f.id desc  ");
		Pagination<Category> page = dao.find(finder,pageNo, pageSize);
		result = new CategoryPage(page);
		return result;
	}
	
	
	@Transactional(readOnly = true)
	public Page<Category> findPage(Pageable pageable){
	     return dao.findPage(pageable);
	}

	@Transactional(readOnly = true)
	public long count(Filter... filters){
	     
	     return dao.count(filters);
	     
	}

	@Transactional(readOnly = true)
	public List<Category> findList(Integer first, Integer count, List<Filter> filters, List<Order> orders){
	
		     return dao.findList(first,count,filters,orders);
	
	}
}