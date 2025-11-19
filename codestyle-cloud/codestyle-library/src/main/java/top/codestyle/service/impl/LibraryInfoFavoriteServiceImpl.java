package top.codestyle.service.impl;


import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.support.TransactionTemplate;
import top.codestyle.mapper.LibraryInfoFavoriteMapper;

import top.codestyle.model.dto.DoFavoriteRequest;
import top.codestyle.model.entity.LibraryInfo;
import top.codestyle.model.entity.LibraryInfoFavorite;

import top.codestyle.model.entity.User;
import top.codestyle.service.LibraryInfoFavoriteService;
import top.codestyle.service.LibraryInfoService;
import top.codestyle.service.UserService;


@Service
@Slf4j
@RequiredArgsConstructor
public class LibraryInfoFavoriteServiceImpl extends ServiceImpl<LibraryInfoFavoriteMapper, LibraryInfoFavorite>
        implements LibraryInfoFavoriteService {
    private final UserService userService;
    private final LibraryInfoService libraryInfoService;
    private final TransactionTemplate transactionTemplate;
    @Override
    public Boolean doFavorite(DoFavoriteRequest doFavoriteRequest, HttpServletRequest request) {
        if (doFavoriteRequest == null || doFavoriteRequest.getLibraryInfoId() == null) {
            throw new RuntimeException("参数错误");
        }
        User loginUser = userService.getLoginUser(request);

        //加锁
        synchronized (loginUser.getId().toString().intern()) {
            //编程式事务
            return transactionTemplate.execute(status -> {
                Long libraryInfoId = doFavoriteRequest.getLibraryInfoId();
                boolean exists = this.lambdaQuery()
                        .eq(LibraryInfoFavorite::getUserId, loginUser.getId())
                        .eq(LibraryInfoFavorite::getInfoId, libraryInfoId)
                        .exists();
                if (exists) {
                    throw new RuntimeException("用户已收藏");
                }
                boolean update = libraryInfoService.lambdaUpdate()
                        .eq(LibraryInfo::getId, libraryInfoId)
                        .setSql("totalFavoriteCount = totalFavoriteCount + 1")
                        .update();
                LibraryInfoFavorite libraryInfoFavorite = new LibraryInfoFavorite();
                libraryInfoFavorite.setUserId(loginUser.getId());
                libraryInfoFavorite.setInfoId(libraryInfoId);
                return update && this.save(libraryInfoFavorite);
            });
        }
    }

    @Override
    public Boolean undoFavorite(DoFavoriteRequest doFavoriteRequest, HttpServletRequest request) {
        if (doFavoriteRequest == null || doFavoriteRequest.getLibraryInfoId() == null) {
            throw new RuntimeException("参数错误");
        }
        User loginUser = userService.getLoginUser(request);

        //加锁
        synchronized (loginUser.getId().toString().intern()) {
            //编程式事务
            return transactionTemplate.execute(status -> {
                Long libraryInfoId = doFavoriteRequest.getLibraryInfoId();
                LibraryInfoFavorite libraryInfoFavorite = this.lambdaQuery()
                        .eq(LibraryInfoFavorite::getUserId, loginUser.getId())
                        .eq(LibraryInfoFavorite::getInfoId, libraryInfoId)
                        .one();
                if (libraryInfoFavorite == null) {
                    throw new RuntimeException("用户未收藏");
                }
                boolean update = libraryInfoService.lambdaUpdate()
                        .eq(LibraryInfo::getId, libraryInfoId)
                        .setSql("totalFavoriteCount = totalFavoriteCount - 1")
                        .update();
                return update && this.removeById(libraryInfoFavorite.getId());
            });
        }
    }
}
