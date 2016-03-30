package com.ada.article.dao;


import  com.ada.article.entity.ArticleComment;
import com.ada.data.core.CriteriaDao;
import com.ada.data.core.Pagination;
import  com.ada.data.core.Updater;

public interface ArticleCommentDao extends CriteriaDao<ArticleComment, Long>{
	public Pagination getPage(int pageNo, int pageSize);

	public ArticleComment findById(Long id);

	public ArticleComment save(ArticleComment bean);

	public ArticleComment updateByUpdater(Updater<ArticleComment> updater);

	public ArticleComment deleteById(Long id);
}