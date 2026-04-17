package org.zuel.medicineknowledge.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.zuel.medicineknowledge.common.ErrorCode;
import org.zuel.medicineknowledge.constant.CommonConstant;
import org.zuel.medicineknowledge.exception.BusinessException;
import org.zuel.medicineknowledge.model.domain.Categories;
import org.zuel.medicineknowledge.model.dto.category.CategoryQueryRequest;
import org.zuel.medicineknowledge.service.CategoriesService;
import org.zuel.medicineknowledge.mapper.CategoriesMapper;
import org.springframework.stereotype.Service;
import org.zuel.medicineknowledge.utils.SqlUtils;

/**
* @author mumu
* @description 针对表【categories】的数据库操作Service实现
* @createDate 2024-04-23 21:06:28
*/
@Service
public class CategoriesServiceImpl extends ServiceImpl<CategoriesMapper, Categories>
    implements CategoriesService{

    @Override
    public boolean validCategories(Categories post, boolean add) {
        if (post == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        String title = post.getName();
        String content = post.getDescription();
        // 有参数则校验
        if (StringUtils.isNotBlank(title) && title.length() > 10) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "名称过长");
        }
        if (StringUtils.isNotBlank(content) && content.length() > 100) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "描述内容过长");
        }
        return true;
    }
    @Override
    public QueryWrapper<Categories> getQueryWrapper(CategoryQueryRequest categoriesQueryRequest) {
        QueryWrapper<Categories> queryWrapper = new QueryWrapper<>();
        if (categoriesQueryRequest == null) {
            return queryWrapper;
        }
        String searchText = categoriesQueryRequest.getSearchText();
        String sortField = categoriesQueryRequest.getSortField();
        String sortOrder = categoriesQueryRequest.getSortOrder();
        Long id = categoriesQueryRequest.getId();
        String title = categoriesQueryRequest.getName();
        String description = categoriesQueryRequest.getDescription();
        // 拼接查询条件
        if (StringUtils.isNotBlank(searchText)) {
            queryWrapper.and(qw -> qw.like("name", searchText).or().like("description", searchText));
        }
        queryWrapper.like(StringUtils.isNotBlank(title), "name", title);
        queryWrapper.like(StringUtils.isNotBlank(description), "description", description);
        queryWrapper.eq(ObjectUtils.isNotEmpty(id), "id", id);
        queryWrapper.orderBy(SqlUtils.validSortField(sortField), sortOrder.equals(CommonConstant.SORT_ORDER_ASC),
                sortField);
        return queryWrapper;
    }

}




