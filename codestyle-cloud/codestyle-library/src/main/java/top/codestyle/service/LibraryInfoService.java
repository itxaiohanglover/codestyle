package top.codestyle.service;


import com.baomidou.mybatisplus.extension.service.IService;
import jakarta.servlet.http.HttpServletRequest;
import top.codestyle.model.VO.LibraryInfoVO;
import top.codestyle.model.entity.LibraryInfo;
import top.codestyle.model.entity.User;

import java.util.List;

/**
* @author LENOVO
* @description 针对表【library_info(模板库主表)】的数据库操作Service
* @createDate 2025-11-11 16:04:28
*/
public interface LibraryInfoService extends IService<LibraryInfo> {
    LibraryInfoVO getLibraryInfoVOById(long LibraryInfoId, HttpServletRequest request);

    LibraryInfoVO getLibraryInfoVO(LibraryInfo libraryInfo, User loginUser);

    List<LibraryInfoVO> getLibraryInfoVOList(List<LibraryInfo> libraryInfoList, HttpServletRequest request);
}
