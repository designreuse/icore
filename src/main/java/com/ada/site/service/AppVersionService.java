package com.ada.site.service;

import com.ada.site.entity.AppVersion;
import com.ada.site.page.AppVersionPage;
import com.quhaodian.data.page.Filter;
import com.quhaodian.data.page.Order;
import com.quhaodian.data.page.Page;
import com.quhaodian.data.page.Pageable;
import java.util.List;


public interface AppVersionService {

	AppVersion findById(Long id);

	AppVersion save(AppVersion bean);

	AppVersion update(AppVersion bean);

	AppVersion deleteById(Long id);
	
	AppVersion[] deleteByIds(Long[] ids);
	
	AppVersionPage getPage(int pageNo, int pageSize);
	
	
	Page<AppVersion> findPage(Pageable pageable);

	long count(Filter... filters);

	List<AppVersion> findList(Integer first, Integer count, List<Filter> filters, List<Order> orders);
	
}