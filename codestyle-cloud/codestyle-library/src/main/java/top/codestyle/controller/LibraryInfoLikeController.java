package top.codestyle.controller;

import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import top.codestyle.common.BaseResponse;
import top.codestyle.common.ResultUtils;
import top.codestyle.model.dto.DoLikeRequest;
import top.codestyle.service.LibraryInfoLikeService;

@RestController
@RequestMapping("/like")
public class LibraryInfoLikeController {
    @Resource
    private LibraryInfoLikeService libraryInfoLikeService;

    @PostMapping("/do")
    public BaseResponse<Boolean> doLike(@RequestBody DoLikeRequest doLikeRequest, HttpServletRequest request) {
        Boolean success = libraryInfoLikeService.doLike(doLikeRequest, request);
        return ResultUtils.success(success);
    }

    @PostMapping("/undo")
    public BaseResponse<Boolean> undoLike(@RequestBody DoLikeRequest doLikeRequest, HttpServletRequest request) {
        Boolean success = libraryInfoLikeService.undoLike(doLikeRequest, request);
        return ResultUtils.success(success);
    }
}
