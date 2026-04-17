package org.zuel.medicineknowledge.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.BeanUtils;
import org.springframework.web.bind.annotation.*;
import org.zuel.medicineknowledge.common.BaseResponse;
import org.zuel.medicineknowledge.common.DeleteRequest;
import org.zuel.medicineknowledge.common.ErrorCode;
import org.zuel.medicineknowledge.common.ResultUtils;
import org.zuel.medicineknowledge.exception.BusinessException;
import org.zuel.medicineknowledge.exception.ThrowUtils;
import org.zuel.medicineknowledge.model.domain.Categories;
import org.zuel.medicineknowledge.model.dto.category.CategoryAddRequest;
import org.zuel.medicineknowledge.model.dto.category.CategoryQueryRequest;
import org.zuel.medicineknowledge.model.dto.category.CategoryUpdateRequest;
import org.zuel.medicineknowledge.service.CategoriesService;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.List;

@RestController
@RequestMapping("/category")
@Api(tags = "文章类别管理API", description = "提供文章类别管理相关的接口")
public class CategoriesController {
    @Resource
    CategoriesService categoriesService;
    /**
     * 创建
     *
     * @param
     * @param request
     * @return
     */
    @PostMapping("/admin/add")
    @ApiOperation("管理员-创建类别")
    public BaseResponse<Long> addCategories(@RequestBody CategoryAddRequest categoryAddRequest, HttpServletRequest request) {
        if (categoryAddRequest == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        Categories categories = new Categories();
        BeanUtils.copyProperties(categoryAddRequest, categories);
        boolean result = categoriesService.save(categories);
        ThrowUtils.throwIf(!result, ErrorCode.OPERATION_ERROR);
        long newCategoriesId = categories.getId();
        return ResultUtils.success(newCategoriesId);
    }

    /**
     * 删除
     *
     * @param deleteRequest
     * @param request
     * @return
     */
    @PostMapping("/admin/delete")
    @ApiOperation("管理员-删除类别")
    public BaseResponse<Boolean> deleteCategories(@RequestBody DeleteRequest deleteRequest, HttpServletRequest request) {
        if (deleteRequest == null || deleteRequest.getId() <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        long id = deleteRequest.getId();
        // 判断是否存在
        Categories oldCategories = categoriesService.getById(id);
        ThrowUtils.throwIf(oldCategories == null, ErrorCode.NOT_FOUND_ERROR);
        boolean b = categoriesService.removeById(id);
        return ResultUtils.success(b);
    }

    /**
     * 更新（仅管理员）
     *
     * @param categoriesUpdateRequest
     * @return
     */
    @PostMapping("/admin/update")
    @ApiOperation("更新类别")
    public BaseResponse<Boolean> updateCategories(@RequestBody CategoryUpdateRequest categoriesUpdateRequest) {
        if (categoriesUpdateRequest == null || categoriesUpdateRequest.getId() <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        Categories Categories = new Categories();
        BeanUtils.copyProperties(categoriesUpdateRequest, Categories);
        // 参数校验
        categoriesService.validCategories(Categories, false);
        Integer id = categoriesUpdateRequest.getId();
        // 判断是否存在
        Categories oldCategories = categoriesService.getById(id);
        ThrowUtils.throwIf(oldCategories == null, ErrorCode.NOT_FOUND_ERROR);
        boolean result = categoriesService.updateById(Categories);
        return ResultUtils.success(result);
    }

    /**
     * 根据 id 获取
     *
     * @param id
     * @return
     */
    @GetMapping("/get")
    @ApiOperation("根据id获取类别")
    public BaseResponse<Categories> getCategoriesVOById(long id, HttpServletRequest request) {
        if (id <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        Categories Categories = categoriesService.getById(id);
        if (Categories == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND_ERROR);
        }
        return ResultUtils.success(Categories);
    }

    /**
     * 分页获取列表
     *
     * @param categoryQueryRequest
     * @return
     */
    @PostMapping("/list/page")
    @ApiOperation("分页获取类别")
    public BaseResponse<Page<Categories>> listCategoriesByPage(@RequestBody CategoryQueryRequest categoryQueryRequest) {
        long current = categoryQueryRequest.getCurrent();
        long size = categoryQueryRequest.getPageSize();
        Page<Categories> CategoriesPage = categoriesService.page(new Page<>(current, size),
                categoriesService.getQueryWrapper(categoryQueryRequest));
        return ResultUtils.success(CategoriesPage);
    }

    /**
     * 获取类别列表
     * @return
     */
    @GetMapping("/list")
    @ApiOperation("获取所有类别")
    public BaseResponse<List<Categories>> listCategories() {
        List<Categories> list = categoriesService.list();
        return ResultUtils.success(list);
    }


}
