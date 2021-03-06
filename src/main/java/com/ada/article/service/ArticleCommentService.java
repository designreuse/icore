package com.ada.article.service;

import com.ada.article.entity.ArticleComment;
import com.ada.article.page.ArticleCommentPage;
import com.quhaodian.data.core.Pagination;
import com.quhaodian.data.page.Page;
import com.quhaodian.data.page.Pageable;



public interface ArticleCommentService {
	Pagination getPage(int pageNo, int pageSize);

	ArticleComment findById(Long id);

	ArticleComment save(ArticleComment bean);

	ArticleComment update(ArticleComment bean);

	ArticleComment deleteById(Long id);
	
	ArticleComment[] deleteByIds(Long[] ids);
	ArticleCommentPage pageByArticle(Long articleid, int pageNo, int pageSize);
	
	Page<ArticleComment> findPage(Pageable pageable);

}