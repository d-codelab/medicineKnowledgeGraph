package org.zuel.medicineknowledge.service.impl;

import cn.hutool.core.collection.CollUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.zuel.medicineknowledge.common.ErrorCode;
import org.zuel.medicineknowledge.constant.CommonConstant;
import org.zuel.medicineknowledge.exception.BusinessException;
import org.zuel.medicineknowledge.exception.ThrowUtils;
import org.zuel.medicineknowledge.model.domain.Articles;
import org.zuel.medicineknowledge.model.dto.articles.ArticleQueryRequest;
import org.zuel.medicineknowledge.service.ArticlesService;
import org.zuel.medicineknowledge.mapper.ArticlesMapper;
import org.springframework.stereotype.Service;
import org.zuel.medicineknowledge.utils.SqlUtils;

import java.util.List;

/**
* @author mumu
* @description 针对表【articles】的数据库操作Service实现
* @createDate 2024-04-24 10:28:28
*/
@Service
public class ArticlesServiceImpl extends ServiceImpl<ArticlesMapper, Articles>
    implements ArticlesService{

    @Override
    public void validArticles(Articles post, boolean add) {
        if (post == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        String title = post.getTitle();
        String content = post.getContent();
        Integer categoryId = post.getCategoryId();
        if (categoryId==null){
            throw new BusinessException(ErrorCode.PARAMS_ERROR,"类别不能为空");
        }
        // 创建时，参数不能为空
        if (add) {
            ThrowUtils.throwIf(StringUtils.isAnyBlank(title, content), ErrorCode.PARAMS_ERROR);
        }
        // 有参数则校验
        if (StringUtils.isNotBlank(title) && title.length() > 80) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "标题过长");
        }
        if (StringUtils.isNotBlank(content) && content.length() > 8192) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "内容过长");
        }
    }

    @Override
    public QueryWrapper<Articles> getQueryWrapper(ArticleQueryRequest postQueryRequest) {
        QueryWrapper<Articles> queryWrapper = new QueryWrapper<>();
        if (postQueryRequest == null) {
            return queryWrapper;
        }
        String searchText = postQueryRequest.getSearchText();
        String sortField = postQueryRequest.getSortField();
        String sortOrder = postQueryRequest.getSortOrder();
        Long id = postQueryRequest.getId();
        String title = postQueryRequest.getTitle();
        String content = postQueryRequest.getContent();
        Long notId = postQueryRequest.getId();
        Integer categoryId = postQueryRequest.getCategoryId();
        // 拼接查询条件
        if (StringUtils.isNotBlank(searchText)) {
            queryWrapper.and(qw -> qw.like("title", searchText).or().like("content", searchText));
        }
        queryWrapper.like(StringUtils.isNotBlank(title), "title", title);
        queryWrapper.like(StringUtils.isNotBlank(content), "content", content);

        queryWrapper.ne(ObjectUtils.isNotEmpty(notId), "id", notId);
        queryWrapper.eq(ObjectUtils.isNotEmpty(id), "id", id);
        queryWrapper.eq(ObjectUtils.isNotEmpty(categoryId), "category_id", categoryId);
        queryWrapper.orderBy(SqlUtils.validSortField(sortField), sortOrder.equals(CommonConstant.SORT_ORDER_ASC),
                sortField);
        return queryWrapper;
    }
}




