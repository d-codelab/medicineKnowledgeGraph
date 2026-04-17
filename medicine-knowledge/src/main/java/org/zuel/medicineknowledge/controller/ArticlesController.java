package org.zuel.medicineknowledge.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.web.bind.annotation.*;
import org.zuel.medicineknowledge.common.BaseResponse;
import org.zuel.medicineknowledge.common.DeleteRequest;
import org.zuel.medicineknowledge.common.ErrorCode;
import org.zuel.medicineknowledge.common.ResultUtils;
import org.zuel.medicineknowledge.exception.BusinessException;
import org.zuel.medicineknowledge.exception.ThrowUtils;
import org.zuel.medicineknowledge.model.domain.Articles;
import org.zuel.medicineknowledge.model.domain.Categories;
import org.zuel.medicineknowledge.model.dto.articles.ArticleAddRequest;
import org.zuel.medicineknowledge.model.dto.articles.ArticleQueryRequest;
import org.zuel.medicineknowledge.model.dto.articles.ArticleUpdateRequest;
import org.zuel.medicineknowledge.model.vo.ArticleVO;
import org.zuel.medicineknowledge.service.ArticlesService;
import org.zuel.medicineknowledge.service.CategoriesService;
import org.zuel.medicineknowledge.service.UsersService;
import org.zuel.medicineknowledge.utils.JwtUtils;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping("/articles")
@Api(tags = "文章管理API", description = "提供文章管理相关的接口")
@Slf4j
public class ArticlesController {
    @Resource
    ArticlesService articlesService;
    @Resource
    UsersService usersService;
    @Resource
    CategoriesService categoriesService;
    
    /**
     * 创建
     *
     * @param articleAddRequest
     * @param request
     * @return
     */
    @PostMapping("/admin/add")
    @ApiOperation("管理员-添加文章")
    public BaseResponse<Long> addPost(@RequestBody ArticleAddRequest articleAddRequest, HttpServletRequest request) {
        if (articleAddRequest == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        if (StringUtils.isBlank(articleAddRequest.getTitle())){
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "标题不能为空");
        }
        if (StringUtils.isBlank(articleAddRequest.getContent())){
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "内容不能为空");
        }
        if (articleAddRequest.getCategoryId() == null){
            //抛出异常
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "分类不能为空");
        }
        Articles post = new Articles();
        BeanUtils.copyProperties(articleAddRequest, post);
        Long loginUserId = JwtUtils.getLoginUserId(request);
        if (loginUserId == null){
            throw new BusinessException(ErrorCode.NOT_LOGIN_ERROR);
        }
        post.setAuthorId(loginUserId);
        boolean result = articlesService.save(post);
        ThrowUtils.throwIf(!result, ErrorCode.OPERATION_ERROR);
        long newPostId = post.getId();
        return ResultUtils.success(newPostId);
    }

    /**
     * 删除
     *
     * @param deleteRequest
     * @param request
     * @return
     */
    @PostMapping("/admin/delete")
    @ApiOperation("管理员-删除文章")
    public BaseResponse<Boolean> deletePost(@RequestBody DeleteRequest deleteRequest, HttpServletRequest request) {
        if (deleteRequest == null || deleteRequest.getId() <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        long id = deleteRequest.getId();
        // 判断是否存在
        Articles oldPost = articlesService.getById(id);
        ThrowUtils.throwIf(oldPost == null, ErrorCode.NOT_FOUND_ERROR);
        boolean b = articlesService.removeById(id);
        return ResultUtils.success(b);
    }

    /**
     * 更新（仅管理员）
     *
     * @param postUpdateRequest
     * @return
     */
    @PostMapping("/admin/update")
    @ApiOperation("管理员-更新文章")
    public BaseResponse<Boolean> updatePost(@RequestBody ArticleUpdateRequest postUpdateRequest) {
        if (postUpdateRequest == null || postUpdateRequest.getId() <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        Articles post = new Articles();
        BeanUtils.copyProperties(postUpdateRequest, post);
        // 参数校验
        articlesService.validArticles(post, false);
        long id = postUpdateRequest.getId();
        // 判断是否存在
        Articles oldPost = articlesService.getById(id);
        ThrowUtils.throwIf(oldPost == null, ErrorCode.NOT_FOUND_ERROR);
        boolean result = articlesService.updateById(post);
        return ResultUtils.success(result);
    }

    /**
     * 根据 id 获取
     *
     * @param id
     * @return
     */
    @GetMapping("/get")
    @ApiOperation("根据ID获取文章")
    public BaseResponse<ArticleVO> getPostVOById(long id, HttpServletRequest request) {
        if (id <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        Articles post = articlesService.getById(id);
        //update count views
        Integer countv = post.getCountv();
        post.setCountv(countv+1);
        articlesService.updateById(post);
        ArticleVO articleVO = new ArticleVO();
        Integer categoryId = post.getCategoryId();
        Categories categories = categoriesService.getById(categoryId);
        BeanUtils.copyProperties(post,articleVO);
        if (categories == null){
            log.info("文章暂无类别");
        }else {
            articleVO.setCategoryName(categories.getName());
        }
        if (post == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND_ERROR);
        }
        return ResultUtils.success(articleVO);
    }

    /**
     * 分页获取列表
     *
     * @param postQueryRequest
     * @return
     */
    @PostMapping("/list/page")
    @ApiOperation("分页获取文章")
    public BaseResponse< List<ArticleVO>> listPostByPage(@RequestBody ArticleQueryRequest postQueryRequest) {
        long current = postQueryRequest.getCurrent();
        long size = postQueryRequest.getPageSize();
        Page<Articles> postPage = articlesService.page(new Page<>(current, size),
                articlesService.getQueryWrapper(postQueryRequest));
        // 遍历文章列表，设置每个文章的类别名称
        List<Articles> articlesList = postPage.getRecords();
        List<ArticleVO> results = new ArrayList<>();
        for (Articles article : articlesList) {
            Integer categoryId = article.getCategoryId();
            if (categoryId != null) { // 确保categoryId不为null
                Categories category = categoriesService.getById(categoryId);
                ArticleVO vo = new ArticleVO();
                BeanUtils.copyProperties(article,vo);
                if (category != null) { // 确保category不为null
                    vo.setCategoryName(category.getName());
                } else {
                    log.info("文章无类别或类别未知类别");
                }
                results.add(vo);
            }
        }
            return ResultUtils.success(results);
    }

}
