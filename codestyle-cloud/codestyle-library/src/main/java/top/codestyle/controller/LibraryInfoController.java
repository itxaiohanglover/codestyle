package top.codestyle.controller;

import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import top.codestyle.common.BaseResponse;
import top.codestyle.common.ResultUtils;
import top.codestyle.model.VO.LibraryInfoVO;
import top.codestyle.model.entity.LibraryInfo;
import top.codestyle.service.LibraryInfoService;

import java.util.List;

@RestController
@RequestMapping("LibraryInfo")
public class LibraryInfoController {
    @Resource
    private LibraryInfoService libraryInfoService;


    /**
     *  根据id获取模板库信息
     */

    @GetMapping("/get")
    public BaseResponse<LibraryInfoVO> get(long libraryInfoId, HttpServletRequest request){
        LibraryInfoVO libraryInfoVO = libraryInfoService.getLibraryInfoVOById(libraryInfoId,request);
        return ResultUtils.success(libraryInfoVO);
    }

    @GetMapping("/list")
    public BaseResponse<List<LibraryInfoVO>> list(HttpServletRequest request){
        List<LibraryInfo> libraryInfoList = libraryInfoService.list();
        List<LibraryInfoVO> libraryInfoVOList = libraryInfoService.getLibraryInfoVOList(libraryInfoList,request);
        return ResultUtils.success(libraryInfoVOList);
    }







}
