package top.codestyle.controller;

import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import top.codestyle.common.BaseResponse;
import top.codestyle.common.ResultUtils;
import top.codestyle.model.dto.DoFavoriteRequest;

import top.codestyle.service.LibraryInfoFavoriteService;


@RestController
@RequestMapping("/favorite")
public class LibraryInfoFavoriteController {
    @Resource
    private LibraryInfoFavoriteService libraryInfofavoriteService;

    @PostMapping("/do")
    public BaseResponse<Boolean> doFavorite(@RequestBody DoFavoriteRequest doFavoriteRequest, HttpServletRequest request) {
        Boolean success = libraryInfofavoriteService.doFavorite(doFavoriteRequest, request);
        return ResultUtils.success(success);
    }

    @PostMapping("/undo")
    public BaseResponse<Boolean> undoFavorite(@RequestBody DoFavoriteRequest doFavoriteRequest, HttpServletRequest request) {
        Boolean success = libraryInfofavoriteService.undoFavorite(doFavoriteRequest, request);
        return ResultUtils.success(success);
    }
}
