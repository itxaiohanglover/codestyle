package top.codestyle.service;

import com.baomidou.mybatisplus.extension.service.IService;
import jakarta.servlet.http.HttpServletRequest;
import top.codestyle.model.dto.DoFavoriteRequest;
import top.codestyle.model.entity.LibraryInfoFavorite;


public interface LibraryInfoFavoriteService  extends IService<LibraryInfoFavorite> {
    Boolean doFavorite(DoFavoriteRequest doFavoriteRequest, HttpServletRequest request);

    Boolean undoFavorite(DoFavoriteRequest doFavoriteRequest, HttpServletRequest request);
}
