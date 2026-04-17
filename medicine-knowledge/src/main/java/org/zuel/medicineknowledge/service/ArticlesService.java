package org.zuel.medicineknowledge.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import org.checkerframework.checker.units.qual.A;
import org.zuel.medicineknowledge.model.domain.Articles;
import com.baomidou.mybatisplus.extension.service.IService;
import org.zuel.medicineknowledge.model.dto.articles.ArticleQueryRequest;

/**
* @author mumu
* @description 针对表【articles】的数据库操作Service
* @createDate 2024-04-24 10:28:28
*/
public interface ArticlesService extends IService<Articles> {
    /**
     * 校验
     *
     * @param post
     * @param add
     */
    void validArticles(Articles post, boolean add);

    /**
     * 获取查询条件
     *
     * @param postQueryRequest
     * @return
     */
    QueryWrapper<Articles> getQueryWrapper(ArticleQueryRequest postQueryRequest);
}
