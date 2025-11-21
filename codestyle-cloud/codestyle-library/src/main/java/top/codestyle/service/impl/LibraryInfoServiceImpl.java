package top.codestyle.service.impl;


import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.util.ObjUtil;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import top.codestyle.mapper.LibraryInfoFavoriteMapper;
import top.codestyle.mapper.LibraryInfoMapper;
import top.codestyle.model.VO.LibraryInfoVO;
import top.codestyle.model.entity.LibraryInfo;
import top.codestyle.model.entity.LibraryInfoFavorite;
import top.codestyle.model.entity.LibraryInfoLike;
import top.codestyle.model.entity.User;
import top.codestyle.service.LibraryInfoFavoriteService;
import top.codestyle.service.LibraryInfoLikeService;
import top.codestyle.service.LibraryInfoService;
import top.codestyle.service.UserService;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * @author LENOVO
 * @description 针对表【library_info(模板库主表)】的数据库操作Service实现
 * @createDate 2025-11-11 16:04:28
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class LibraryInfoServiceImpl extends ServiceImpl<LibraryInfoMapper, LibraryInfo>
        implements LibraryInfoService {
    @Resource
    private UserService userService;
    @Lazy
    @Resource
    private LibraryInfoLikeService libraryInfoLikeService;

    @Lazy
    @Resource
    private LibraryInfoFavoriteService libraryInfoFavoriteService;
    @Override
    public LibraryInfoVO getLibraryInfoVOById(long LibraryInfoId, HttpServletRequest request) {
        LibraryInfo libraryInfo = this.getById(LibraryInfoId);
        User loginUser = userService.getLoginUser(request);
        return this.getLibraryInfoVO(libraryInfo, loginUser);

    }

    @Override
    public LibraryInfoVO getLibraryInfoVO(LibraryInfo libraryInfo, User loginUser) {
        LibraryInfoVO libraryInfoVO = new LibraryInfoVO();
        BeanUtil.copyProperties(libraryInfo, libraryInfoVO);
        if (loginUser == null) {
            return libraryInfoVO;
        }
        LibraryInfoLike libraryInfoLike = libraryInfoLikeService.lambdaQuery()
                .eq(LibraryInfoLike::getUserId, loginUser.getId())
                .eq(LibraryInfoLike::getInfoId, libraryInfo.getId())
                .one();
        libraryInfoVO.setHasLike(libraryInfoLike != null);

        LibraryInfoFavorite libraryInfoFavorite = libraryInfoFavoriteService.lambdaQuery()
                .eq(LibraryInfoFavorite::getUserId, loginUser.getId())
                .eq(LibraryInfoFavorite::getInfoId, libraryInfo.getId())
                .one();
        libraryInfoVO.setHasFavorite(libraryInfoFavorite != null);


        return libraryInfoVO;
    }

    @Override
    public List<LibraryInfoVO> getLibraryInfoVOList(List<LibraryInfo> libraryInfoList, HttpServletRequest request) {
        User loginUser = userService.getLoginUser(request);
        Map<Long, Boolean> libraryInfoIdHasLikeMap = new HashMap<>();
        Map<Long, Boolean> libraryInfoIdHasFavoriteMap = new HashMap<>();
        if (ObjUtil.isNotEmpty(loginUser)) {
            Set<Long> libraryInfoSet = libraryInfoList.stream().map(LibraryInfo::getId).collect(Collectors.toSet());
            List<LibraryInfoLike> libraryInfoLikeList = libraryInfoLikeService.lambdaQuery()
                    .eq(LibraryInfoLike::getUserId, loginUser.getId())
                    .in(LibraryInfoLike::getInfoId, libraryInfoSet)
                    .list();
            libraryInfoLikeList.forEach(libraryInfoLike -> libraryInfoIdHasLikeMap.put(libraryInfoLike.getInfoId(), true));

            List<LibraryInfoFavorite> libraryInfoFavoriteList = libraryInfoFavoriteService.lambdaQuery()
                    .eq(LibraryInfoFavorite::getUserId, loginUser.getId())
                    .in(LibraryInfoFavorite::getInfoId, libraryInfoSet)
                    .list();
            libraryInfoFavoriteList.forEach(libraryInfoFavorite -> libraryInfoIdHasFavoriteMap.put(libraryInfoFavorite.getInfoId(), true));


        }
        return libraryInfoList.stream().map(libraryInfo -> {
            LibraryInfoVO libraryInfoVO = BeanUtil.copyProperties(libraryInfo, LibraryInfoVO.class);
            libraryInfoVO.setHasLike(libraryInfoIdHasLikeMap.get(libraryInfo.getId()));
            libraryInfoVO.setHasFavorite(libraryInfoIdHasFavoriteMap.get(libraryInfo.getId()));
            return libraryInfoVO;
        }).toList();
    }
}




