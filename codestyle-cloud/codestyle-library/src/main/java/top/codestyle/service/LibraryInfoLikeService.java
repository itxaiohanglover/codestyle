package top.codestyle.service;


import com.baomidou.mybatisplus.extension.service.IService;

import jakarta.servlet.http.HttpServletRequest;
import top.codestyle.model.dto.DoFavoriteRequest;
import top.codestyle.model.dto.DoLikeRequest;
import top.codestyle.model.entity.LibraryInfoLike;
/**
 * @author LENOVO
 * @description 针对表【library_info_like】的数据库操作Service
 * @createDate 2025-11-11 16:04:28
 */
public interface LibraryInfoLikeService extends IService<LibraryInfoLike> {
    /**
     * 点赞
     * @param doLikeRequest
     * @param request
     * @return (@link Boolean)
     */
    Boolean doLike(DoLikeRequest doLikeRequest, HttpServletRequest request);

    Boolean undoLike(DoLikeRequest doLikeRequest, HttpServletRequest request);

}
